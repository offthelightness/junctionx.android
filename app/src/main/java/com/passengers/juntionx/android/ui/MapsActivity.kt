package com.passengers.juntionx.android.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.res.Resources.NotFoundException
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.florent37.runtimepermission.rx.RxPermissions
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MapStyleOptions
import com.passengers.juntionx.android.R
import com.passengers.juntionx.android.network.ATMApiProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var mRxPermission: RxPermissions

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_maps)

        onFindViews()
        onBindView()

        mRxPermission = RxPermissions(this)

        mRxPermission.request(Manifest.permission.ACCESS_FINE_LOCATION)
            .subscribe(
                {
                    Toast.makeText(this, "success", Toast.LENGTH_LONG).show()
                },
                {
                    val result = (it as RxPermissions.Error).result

                    if (result.hasDenied()) {
                        Toast.makeText(this, "hasDenied", Toast.LENGTH_LONG).show()
                    }

                    if (result.hasForeverDenied()) {
                        Toast.makeText(this, "hasForeverDenied", Toast.LENGTH_LONG).show()
                    }
                })

        ATMApiProvider.api.getATMs(true, "47.488605,19.124344", "47.444361,19.066079")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    Timber.d("downloaded")
                },
                {
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
        mMap = googleMap
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
