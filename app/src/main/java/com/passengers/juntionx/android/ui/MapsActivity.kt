package com.passengers.juntionx.android.ui

import android.annotation.SuppressLint
import android.content.res.Resources.NotFoundException
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.florent37.runtimepermission.rx.RxPermissions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.passengers.juntionx.android.R
import com.passengers.juntionx.android.location.LocationRepository
import com.passengers.juntionx.android.location.LocationRepositoryImpl
import com.passengers.juntionx.android.network.ATMApiProvider
import com.passengers.juntionx.android.network.model.GetAtmResponseWithDistance
import com.passengers.juntionx.android.utils.createSearchArea
import com.passengers.juntionx.android.utils.toSimpleString
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var rxPermission: RxPermissions
    private lateinit var locationRepository: LocationRepository

    private lateinit var mapReadySubject: BehaviorSubject<Any>

    private val NORTH_EAST_IX_DISRICT_BOUNDS  = "47.488235, 19.121869"
    private val SOUTH_WEST_IX_DISRICT_BOUNDS  = "47.488235, 19.121869"
    private val SEARCH_ATM_RADIUS = 750
    private val DEFAULT_MAP_ZOOM = 14f

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_maps)

        onFindViews()
        onBindView()

        rxPermission = RxPermissions(this)
        locationRepository = LocationRepositoryImpl(this)
        mapReadySubject = BehaviorSubject.create()

        locationRepository
            // request location with permission
            .getUpdatesWithPermissionsRequest(rxPermission)
            // if have permission move camera to user
            .doOnNext {
                if (it !== LocationRepositoryImpl.EMPTY_LATLNG) {
                    Timber.d("User location: %s", it.toSimpleString())
                    map.moveCamera(
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition.Builder()
                                .target(it)
                                .zoom(DEFAULT_MAP_ZOOM)
                                .build()
                        )
                    )
                }
            }
            // if have permission get
            .flatMap {
                if (it !== LocationRepositoryImpl.EMPTY_LATLNG) {
                    val searchBounds: Pair<LatLng, LatLng> =it.createSearchArea(SEARCH_ATM_RADIUS)
                    ATMApiProvider.get()
                        .getATMs(false, searchBounds.first.toSimpleString(),searchBounds.second.toSimpleString())
                        .subscribeOn(Schedulers.io())

                } else {
                    ATMApiProvider.get()
                        .getATMs(false, NORTH_EAST_IX_DISRICT_BOUNDS, SOUTH_WEST_IX_DISRICT_BOUNDS)
                        .subscribeOn(Schedulers.io())
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .zipWith(mapReadySubject,
                BiFunction<GetAtmResponseWithDistance, Any, GetAtmResponseWithDistance> { response, mapReady ->
                    response
                }
            )
            .subscribe({
                it.items.map {
                    map.addMarker(
                        com.google.android.gms.maps.model.MarkerOptions()
                            .position(LatLng(it.atm.geoX, it.atm.geoY))
                            .draggable(false)
                    )
                }
            }, {
                Timber.e(it)
            })
    }

    private fun onFindViews() {
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
    }

    private fun onBindView() {
        mapFragment.getMapAsync(this)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        mapReadySubject.onNext(Any())
        try {
            val success =
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            if (!success) {
                Timber.e("Style parsing failed.")
            }
        } catch (e: NotFoundException) {
            Timber.e(e, "Can't find style. Error: ")
        }

    }
}
