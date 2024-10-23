package com.pg.gajamap.data.manager

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

@SuppressLint("MissingPermission")
class LocationManager @Inject constructor(
    private val locationClient: FusedLocationProviderClient,
    private val application: Application
) : LocationTracker {

    private var locationCallback: LocationCallback? = null
    private var cancellationTokenSource: CancellationTokenSource? = null

    override suspend fun getCurrentLocation(): Location? {

        fun checkLocationService(): Boolean {
            val locationManager =
                application.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }

        if (!checkLocationService()) {
            Toast.makeText(application, "GPS를 켜주세요.", Toast.LENGTH_SHORT).show()
            return null
        }

        return suspendCancellableCoroutine { cont ->
            locationClient.lastLocation.apply {

                if (isComplete) {
                    if (isSuccessful) {
                        cont.resume(result)
                    } else {
                        cont.resume(null)
                    }

                    return@suspendCancellableCoroutine
                }

                addOnSuccessListener {
                    cont.resume(it)
                }
                addOnFailureListener() {
                    Toast.makeText(application, "위치 정보를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                    cont.resume(null)
                }
                addOnCanceledListener {
                    cont.cancel()
                }
            }
        }
    }

    override fun startTracker(locationListener: (Location) -> Unit) {
        fun checkLocationService(): Boolean {
            val locationManager =
                application.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }

        if (!checkLocationService()) {
            Toast.makeText(application, "GPS를 켜주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        cancellationTokenSource = CancellationTokenSource()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationResult.lastLocation?.let { location ->
                    locationListener.invoke(location)
                }
            }
        }

        locationClient.requestLocationUpdates(
            locationRequest,
            locationCallback as LocationCallback,
            application.mainLooper
        ).addOnFailureListener {
            Toast.makeText(
                application,
                "위치 업데이트를 요청하는 데 실패했습니다.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    companion object {
        // 설정에 따라 위치 업데이트 동작을 변경할 수 있습니다.
        private val locationRequest = LocationRequest.create().apply {
            interval = 10000 // 위치 업데이트 간격 (밀리초)
            fastestInterval = 5000 // 가장 빠른 위치 업데이트 간격 (밀리초)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY // 정확도 설정
        }
    }

    override fun stopTracker() {
        locationCallback?.let {
            locationClient.removeLocationUpdates(it)
            locationCallback = null
        }
        cancellationTokenSource?.cancel()
    }
}

interface LocationTracker {
    suspend fun getCurrentLocation(): Location?
    fun startTracker(locationListener: (Location) -> Unit)
    fun stopTracker()
}