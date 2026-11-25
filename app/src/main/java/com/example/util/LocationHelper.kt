package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

data class LocationResult(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double?,
    val addressName: String?
)

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): LocationResult? = withContext(Dispatchers.IO) {
        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()

            val currentLoc: Location? = kotlinx.coroutines.withTimeoutOrNull(7000L) {
                suspendCancellableCoroutine { continuation ->
                    fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                        .addOnSuccessListener { loc ->
                            if (continuation.isActive) continuation.resume(loc)
                        }
                        .addOnFailureListener {
                            if (continuation.isActive) continuation.resume(null)
                        }
                    continuation.invokeOnCancellation {
                        cts.cancel()
                    }
                }
            }

            val location: Location? = currentLoc ?: run {
                val fusedLast: Location? = kotlinx.coroutines.withTimeoutOrNull(2000L) {
                    suspendCancellableCoroutine { continuation ->
                        fusedClient.lastLocation
                            .addOnSuccessListener { loc ->
                                if (continuation.isActive) continuation.resume(loc)
                            }
                            .addOnFailureListener {
                                if (continuation.isActive) continuation.resume(null)
                            }
                    }
                }
                fusedLast ?: run {
                    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                    locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        ?: locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        ?: locationManager?.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
                }
            }

            if (location != null) {
                val address = resolveAddress(context, location.latitude, location.longitude)
                LocationResult(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    altitude = if (location.hasAltitude()) location.altitude else null,
                    addressName = address
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun resolveAddress(context: Context, latitude: Double, longitude: Double): String? =
        withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale("id", "ID"))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { continuation ->
                        geocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: MutableList<Address>) {
                                if (addresses.isNotEmpty()) {
                                    continuation.resume(formatAddress(addresses[0]))
                                } else {
                                    continuation.resume(null)
                                }
                            }

                            override fun onError(errorMessage: String?) {
                                continuation.resume(null)
                            }
                        })
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        formatAddress(addresses[0])
                    } else {
                        null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    private fun formatAddress(address: Address): String {
        val parts = mutableListOf<String>()

        // 1. Jalan (gabung nomor rumah + nama jalan bila tersedia)
        val street = buildString {
            val sub = address.subThoroughfare?.trim().orEmpty()
            val road = address.thoroughfare?.trim().orEmpty()
            if (road.isNotEmpty()) {
                if (sub.isNotEmpty() && !road.contains(sub, ignoreCase = true)) {
                    append(sub).append(" ")
                }
                append(road)
            }
        }.trim()
        if (street.isNotEmpty()) {
            parts.add(street)
        }

        // 2. Kelurahan / Desa
        address.subLocality?.trim()?.takeIf { it.isNotEmpty() }?.let { parts.add(it) }

        // 3. Kecamatan
        address.locality?.trim()?.takeIf { it.isNotEmpty() }?.let { parts.add(it) }

        // 4. Provinsi
        address.adminArea?.trim()?.takeIf { it.isNotEmpty() }?.let { parts.add(it) }

        val uniqueParts = parts.distinct()
        return if (uniqueParts.isNotEmpty()) {
            uniqueParts.joinToString("\n")
        } else {
            address.getAddressLine(0) ?: "Koordinat GPS Terdeteksi"
        }
    }
}
