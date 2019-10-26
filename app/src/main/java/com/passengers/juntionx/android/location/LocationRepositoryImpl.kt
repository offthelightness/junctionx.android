package com.passengers.juntionx.android.location

import android.Manifest
import android.content.Context
import android.location.LocationManager
import com.github.florent37.runtimepermission.rx.RxPermissions
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleObserver
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider

class LocationRepositoryImpl constructor(
    private val context: Context,
    private val locationProvider: ReactiveLocationProvider = ReactiveLocationProvider(context)
) : LocationRepository {

    companion object {
        val EMPTY_LATLNG = LatLng(0.0, 0.0)
    }

    override fun getUpdates(): Observable<LatLng> {
        val locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setNumUpdates(5)
            .setInterval(100)

        return Observable.merge(

            locationProvider
                .getUpdatedLocation(locationRequest)
                .map { location ->
                    LatLng(
                        location.latitude,
                        location.longitude
                    )
                }.onErrorReturn { error : Throwable -> EMPTY_LATLNG},

            getLasKnownLocation(),

            isLocationEnabled()
                .onErrorReturn { false }
                .filter { isEnabled: Boolean -> !isEnabled}
                .flatMapObservable<LatLng> { locationProvidersDisabled: Boolean -> Observable.just(EMPTY_LATLNG) }
        )
    }

    override fun getUpdatesWithPermissionsRequest(rxPermissions: RxPermissions): Observable<LatLng> {
        return rxPermissions.request(Manifest.permission.ACCESS_FINE_LOCATION)
            .flatMap { permissionResult -> if (permissionResult.isAccepted) getUpdates() else Observable.empty<LatLng>() }
    }

    override fun getUpdatesWithPermissions(rxPermissions: RxPermissions): Single<LatLng> {
        return rxPermissions.request(Manifest.permission.ACCESS_FINE_LOCATION)
            .flatMap{permissionResult -> if (permissionResult.isAccepted) getUpdates() else Observable.just(EMPTY_LATLNG) }
            .firstOrError()
    }

    override fun getLasKnownLocation(): Observable<LatLng> {
        return locationProvider.lastKnownLocation
            .map { location -> LatLng(location.latitude, location.longitude) }
    }


    override fun isLocationEnabled(): Single<Boolean> {
        return object : Single<Boolean>() {
            override fun subscribeActual(observer: SingleObserver<in Boolean>) {
                var isLocationEnabled = false
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
                if (lm != null) {
                    val isGpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    val isNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                    isLocationEnabled = isGpsEnabled || isNetworkEnabled
                }
                observer.onSuccess(isLocationEnabled)
            }
        }
    }

}