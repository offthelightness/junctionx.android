package com.passengers.juntionx.android.location

import com.github.florent37.runtimepermission.rx.RxPermissions
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Observable
import io.reactivex.Single

interface LocationRepository {
    fun getUpdates(): Observable<LatLng>
    fun getUpdatesWithPermissionsRequest(rxPermissions: RxPermissions): Observable<LatLng>
    fun getUpdatesWithPermissions(rxPermissions: RxPermissions): Single<LatLng>
    fun getLasKnownLocation(): Observable<LatLng>
    fun isLocationEnabled(): Single<Boolean>
}