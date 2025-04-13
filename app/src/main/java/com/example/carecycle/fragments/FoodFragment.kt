package com.example.carecycle.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.carecycle.R
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon


class FoodFragment : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var geoFire: GeoFire
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var geoQuery: GeoQuery? = null
    private val markers = mutableListOf<Marker>()
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getRealLocation()
        } else {
            showPermissionDenied()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Configuration.getInstance().userAgentValue = requireContext().packageName

        val postsRef = FirebaseDatabase.getInstance().getReference("posts")
        geoFire = GeoFire(postsRef)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_food, container, false)

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = view.findViewById(R.id.mapView)
        mapView.setTileSource(TileSourceFactory.MAPNIK)

        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getRealLocation()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                showPermissionRationale()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun getRealLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val userLocation = GeoPoint(it.latitude, it.longitude)
                updateMap(userLocation)
                setupGeoQuery(it.latitude, it.longitude)
            } ?: run {
                Toast.makeText(context, "Enable location services", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateMap(center: GeoPoint) {
        mapView.controller.apply {
            setZoom(15.0)
            setCenter(center)
        }
        drawRadiusCircle(center, 1000.0) // 1 km radius
    }

    private fun drawRadiusCircle(center: GeoPoint, radiusMeters: Double) {
        mapView.overlays.removeAll { it is Polygon }

        val circlePoints = (0..360 step 10).map { angle ->
            val distanceX = radiusMeters * Math.cos(Math.toRadians(angle.toDouble()))
            val distanceY = radiusMeters * Math.sin(Math.toRadians(angle.toDouble()))
            GeoPoint(
                center.latitude + (distanceX / 111319.9),
                center.longitude + (distanceY / (111319.9 * Math.cos(Math.toRadians(center.latitude))))
            )
        }

        Polygon().apply {
            points = circlePoints
            fillColor = 0x22FF0000 // 30% transparent red
            strokeColor = android.graphics.Color.RED
            strokeWidth = 2f
        }.also { mapView.overlays.add(it) }

        mapView.invalidate()
    }

    private fun setupGeoQuery(lat: Double, lng: Double) {
        geoQuery?.removeAllListeners()
        geoQuery = geoFire.queryAtLocation(GeoLocation(lat, lng), 1.0) // 1 km radius

        geoQuery?.addGeoQueryEventListener(object : GeoQueryEventListener {
            override fun onKeyEntered(postId: String, location: GeoLocation) {
                addPostMarker(postId, location)
            }

            override fun onKeyExited(postId: String) {
                removePostMarker(postId)
            }

            override fun onKeyMoved(postId: String, location: GeoLocation) {
                updatePostMarker(postId, location)
            }

            override fun onGeoQueryReady() = Unit
            override fun onGeoQueryError(error: DatabaseError) {
                Toast.makeText(context, "GeoQuery error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addPostMarker(postId: String, location: GeoLocation) {
        Marker(mapView).apply {
            position = GeoPoint(location.latitude, location.longitude)
            title = "Post $postId"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }.also {
            mapView.overlays.add(it)
            markers.add(it)
        }
        mapView.invalidate()
    }

    private fun removePostMarker(postId: String) {
        val iterator = markers.iterator()
        while (iterator.hasNext()) {
            val marker = iterator.next()
            if (marker.title == "Post $postId") {
                mapView.overlays.remove(marker)
                iterator.remove()
            }
        }
        mapView.invalidate()
    }
    private fun updatePostMarker(postId: String, location: GeoLocation) {
        removePostMarker(postId)
        addPostMarker(postId, location)
    }

    private fun showPermissionRationale() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Location Needed")
            .setMessage("This app needs location access to show nearby posts")
            .setPositiveButton("Allow") { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            .setNegativeButton("Deny") { _, _ -> }
            .show()
    }

    private fun showPermissionDenied() {
        Toast.makeText(
            requireContext(),
            "Location permission denied - using default location",
            Toast.LENGTH_LONG
        ).show()
        // Optionally show default location
        updateMap(GeoPoint(28.7041, 77.1025)) // Default to Delhi coordinates
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        geoQuery?.removeAllListeners()
        mapView.onPause()
    }
}


