package com.passengers.juntionx.android.ui

import android.annotation.SuppressLint
import android.content.res.Resources.NotFoundException
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateMargins
import com.github.florent37.runtimepermission.rx.RxPermissions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.passengers.juntionx.android.R
import com.passengers.juntionx.android.filter.Filter
import com.passengers.juntionx.android.filter.FilterRepository
import com.passengers.juntionx.android.location.LocationRepository
import com.passengers.juntionx.android.location.LocationRepositoryImpl
import com.passengers.juntionx.android.network.ATMApiProvider
import com.passengers.juntionx.android.network.model.AtmOutputData
import com.passengers.juntionx.android.network.model.AtmSearchResult
import com.passengers.juntionx.android.network.model.AtmWithDistance
import com.passengers.juntionx.android.network.model.GetAtmResponseWithDistance
import com.passengers.juntionx.android.utils.createSearchArea
import com.passengers.juntionx.android.utils.toSimpleString
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Action
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import io.reactivex.functions.Function3
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var selectedMarker: Marker? = null
    private lateinit var items: List<AtmOutputData>
    private lateinit var map: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var rxPermission: RxPermissions
    private lateinit var locationRepository: LocationRepository

    private lateinit var mapReadySubject: BehaviorSubject<Any>

    private val NORTH_EAST_IX_DISRICT_BOUNDS = "47.488235, 19.121869"
    private val SOUTH_WEST_IX_DISRICT_BOUNDS = "47.488235, 19.121869"
    private val SEARCH_ATM_RADIUS = 750
    private val DEFAULT_MAP_ZOOM = 14f

    lateinit var panel: SlidingUpPanelLayout
    lateinit var fabContainer: ViewGroup
    lateinit var atmItemViewHolder: AtmItemViewHolder

    val userLocationSubject = BehaviorSubject.create<LatLng>()
    val mapBoundsSubject = BehaviorSubject.create<LatLngBounds>()

    val markers = HashMap<String, Marker>()

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_maps)

        onFindViews()
        onBindView()

        rxPermission = RxPermissions(this)
        locationRepository = LocationRepositoryImpl(this)
        mapReadySubject = BehaviorSubject.create()

        Observable.combineLatest(
            FilterRepository.filterSubject.doOnNext {
                markers.values.forEach {
                    it.remove()
                }
                markers.clear()
            },
            userLocationSubject,
            mapBoundsSubject,
            Function3<Filter,LatLng, LatLngBounds, SearchData> {f,u,m ->
                SearchData(f,u,m)
            }
        ).debounce(500, TimeUnit.MILLISECONDS)
            .flatMap { searchData ->
                val northeast = searchData.mapBounds.northeast.toSimpleString()
                val southwest = searchData.mapBounds.southwest.toSimpleString()
                if (searchData.userLocation !== LocationRepositoryImpl.EMPTY_LATLNG) {
//                val searchBounds: Pair<LatLng, LatLng> = it.createSearchArea(SEARCH_ATM_RADIUS)
                    ATMApiProvider.get()
                        .getATMsv3(
                            searchData.filter.canDeposit,
                            northeast,
                            southwest,
                            searchData.userLocation.toSimpleString(),
                            searchData.filter.withPredict
                        )
                        .subscribeOn(Schedulers.io())

                } else {
                    ATMApiProvider.get()
                        .getATMsv3(
                            searchData.filter.canDeposit,
                            northeast,
                            southwest,
                            null,
                            searchData.filter.withPredict
                        )
                        .subscribeOn(Schedulers.io())
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .onErrorReturnItem(AtmSearchResult(null, emptyList()))
            .subscribe({ atmSearchResult ->
                items = atmSearchResult.items
                atmSearchResult.items.forEach {
                    if (!markers.containsKey(it.atm.id)) {
                        markers[it.atm.id] = map.addMarker(
                            MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin))
                                .position(LatLng(it.atm.geoX, it.atm.geoY))
                                .draggable(false)
                        )
                    }
                }
            }, {
                Timber.e(it)
            })

        locationRepository
            // request location with permission
            .getUpdatesWithPermissionsRequest(rxPermission)
            // if have permission move camera to user
            .doOnNext {
                if (it !== LocationRepositoryImpl.EMPTY_LATLNG) {

                }
            }.subscribe(userLocationSubject::onNext)

        userLocationSubject
            .filter { it !== LocationRepositoryImpl.EMPTY_LATLNG }
            .firstOrError()
            .subscribe(Consumer {
                Timber.d("User location: %s", it.toSimpleString())
                map.moveCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder()
                            .target(it)
                            .zoom(DEFAULT_MAP_ZOOM)
                            .build()
                    )
                )
            })

//            // if have permission get
//            .flatMap {
//                if (it !== LocationRepositoryImpl.EMPTY_LATLNG) {
//                    val searchBounds: Pair<LatLng, LatLng> = it.createSearchArea(SEARCH_ATM_RADIUS)
//                    ATMApiProvider.get()
//                        .getATMsv3(
//                            false,
//                            searchBounds.first.toSimpleString(),
//                            searchBounds.second.toSimpleString(),
//                            it.toSimpleString()
//                        )
//                        .subscribeOn(Schedulers.io())
//
//                } else {
//                    ATMApiProvider.get()
//                        .getATMs(false, NORTH_EAST_IX_DISRICT_BOUNDS, SOUTH_WEST_IX_DISRICT_BOUNDS)
//                        .subscribeOn(Schedulers.io())
//                }
//            }
//            .observeOn(AndroidSchedulers.mainThread())
//            .zipWith(mapReadySubject,
//                BiFunction<AtmSearchResult, Any, AtmSearchResult> { response, mapReady ->
//                    response
//                }
//            )
//            .subscribe({
//                items = it.items
//                it.items.map {
//                    map.addMarker(
//                        com.google.android.gms.maps.model.MarkerOptions()
//                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin))
//                            .position(LatLng(it.atm.geoX, it.atm.geoY))
//                            .draggable(false)
//                    )
//                }
//            }, {
//                Timber.e(it)
//            })
    }

    private fun onFindViews() {
        panel = findViewById(R.id.panel)
        fabContainer = findViewById(R.id.fab_container)
        atmItemViewHolder = AtmItemViewHolder(findViewById(R.id.atm_item))
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        findViewById<FloatingActionButton>(R.id.fab_filter).setOnClickListener {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.filter_container, FilterFragment())
                .addToBackStack("FilterFragment")
                .commit()
        }
    }

    private fun onBindView() {
        mapFragment.getMapAsync(this)

        panel?.addPanelSlideListener(object : SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelSlide(panel: View?, slideOffset: Float) {
                val margin = (slideOffset * TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    224.0f,
                    resources.displayMetrics
                )).toInt()
                Timber.d("margin $margin")
                val layoutParams = fabContainer.layoutParams as FrameLayout.LayoutParams
                layoutParams.setMargins(
                    layoutParams.leftMargin,
                    layoutParams.topMargin,
                    layoutParams.rightMargin,
                    margin
                )
                fabContainer.layoutParams = layoutParams
            }

            override fun onPanelStateChanged(
                panel: View?,
                previousState: SlidingUpPanelLayout.PanelState?,
                newState: SlidingUpPanelLayout.PanelState?
            ) {
                if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    if (selectedMarker != null) {
                        selectedMarker?.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin))
                    }
                }
            }

        })
    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        mapReadySubject.onNext(Any())
        try {
            val success =
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            googleMap.setOnCameraIdleListener {
                mapBoundsSubject.onNext(googleMap.projection.visibleRegion.latLngBounds)
            }
            googleMap.setOnMarkerClickListener { onMarkerClick(it) }
            googleMap.setOnMapClickListener {
                if (selectedMarker != null) {
                    selectedMarker?.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin))
                }
                selectedMarker = null
                panel.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            }
            if (!success) {
                Timber.e("Style parsing failed.")
            }
        } catch (e: NotFoundException) {
            Timber.e(e, "Can't find style. Error: ")
        }
    }

    fun onMarkerClick(marker: Marker): Boolean {
        if (selectedMarker != null) {
            selectedMarker?.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin))
        }
        val atmWithDistance = items.find {
            it.atm.geoX == marker.position.latitude
                    && it.atm.geoY == marker.position.longitude
        }
        atmWithDistance?.let {
            if (it.atm.canDeposit) {
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_can_deposit))
            } else {
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_cant_deposit))
            }
            atmItemViewHolder.bind(it)
            panel.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
        }
        selectedMarker = marker

//        marker.setIcon()
//        if (marker == myMarker) {
//            //handle click here
//        }
        return true
    }
}


class SearchData(
    val filter: Filter,
    val userLocation: LatLng,
    val mapBounds: LatLngBounds
)