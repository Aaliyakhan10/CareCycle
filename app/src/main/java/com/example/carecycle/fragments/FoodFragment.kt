package com.example.carecycle.fragments
import com.google.firebase.database.Transaction
import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carecycle.R
import com.example.carecycle.Utils
import com.example.carecycle.adapter.ExpiredItemAdapter
import com.example.carecycle.adapter.PostAdapter
import com.example.carecycle.databinding.ActivityCreatePostBinding
import com.example.carecycle.databinding.FragmentFoodBinding
import com.example.carecycle.databinding.PostItemBinding
import com.example.carecycle.model.ExpiredPostData
import com.example.carecycle.model.Post
import com.example.carecycle.viewmodel.ExpiredItemViewModel
import com.example.carecycle.viewmodel.FoodDonationAuth
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import kotlin.getValue
import kotlin.properties.Delegates


class FoodFragment : Fragment() {
  private lateinit var binding:FragmentFoodBinding
    private lateinit var mapView: MapView
    private lateinit var geoFire: GeoFire
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var geoQuery: GeoQuery? = null
    private val markers = mutableListOf<Marker>()
       private var lan by Delegates.notNull<Double>()
    private var lat by Delegates.notNull<Double>()
    private lateinit var postAdapter: PostAdapter

    private val currentPosts = mutableMapOf<String, Post>()
    private val postListeners = mutableMapOf<String, ValueEventListener>()
    private val viewModel: FoodDonationAuth by viewModels()

    private lateinit var mutableItemList: MutableList<Post>

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
        binding= FragmentFoodBinding.inflate(layoutInflater,container,false)
        Configuration.getInstance().userAgentValue = requireContext().packageName

        val postsRef = FirebaseDatabase.getInstance().getReference("posts")
        geoFire = GeoFire(postsRef)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())



        getItemList()
            // Make sure this is after binding is set
            binding.postsRecyclerView.adapter = postAdapter
            binding.postsRecyclerView.layoutManager = LinearLayoutManager(requireContext())



            getUserInfo()
            onAddbuttonclikc()

            return binding.root



    }

    private fun onAddbuttonclikc() {
        val uid= Utils.getCurrentUsersId()
        binding.floatingActionButton.setOnClickListener {
            var alertDialog: AlertDialog?=null
            val postbinding= ActivityCreatePostBinding.inflate(layoutInflater)
            alertDialog= AlertDialog.Builder(requireContext())
                .setView(postbinding.root)
                .create()
            alertDialog.show()
            postbinding.btnSubmitPost.setOnClickListener {
                val description=postbinding.editTextDescription.text.toString()
                val foodname=postbinding.Food.text.toString()
                val noofplates=postbinding.editTextPlatesAvailable.text.toString().toInt()
                viewModel.savePost(uid,foodname, description,noofplates,lat,lan)
               alertDialog.hide()
            }

        }
    }
    private fun getItemList() {
        val uid = Utils.getCurrentUsersId()
        mutableItemList = mutableListOf()

        postAdapter = PostAdapter(mutableItemList)
        binding.postsRecyclerView.adapter = postAdapter

        lifecycleScope.launch {
            viewModel.getListOfItem(requireContext()).observe(viewLifecycleOwner, Observer { lists ->
                mutableItemList.clear()
                mutableItemList.addAll(lists)
                postAdapter.notifyDataSetChanged()
            })
        }
    }


    private fun getUserInfo() {
        Utils.userInfo { name,address,email,usertype,coin,imgUrl ->
            binding.userNametxt.text=name
            binding.numofcoins.text="Coins: $coin"
            Glide.with(this).load(imgUrl).circleCrop().into(binding.profileImageHome)
        }
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
    private fun removePost(postId: String) {
            currentPosts.remove(postId)?.let {
                postListeners[postId]?.let { listener ->
                    FirebaseDatabase.getInstance().getReference("posts").removeEventListener(listener)
                }
                postListeners.remove(postId)
                updateRecyclerView()
            }
        }

                private fun updateRecyclerView() {
            val postsList = currentPosts.values.toList().sortedByDescending { it.platesAvailable }
                    Log.d("RecyclerView", "Updating with ${postsList.size} posts")

        }

                private fun claimPost(post: Post) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
                Toast.makeText(context, "Not logged in", Toast.LENGTH_SHORT).show()
                return
            }

            if (post.platesAvailable <= 0 || post.claimants.containsKey(userId)) {
                Toast.makeText(context, "Cannot claim", Toast.LENGTH_SHORT).show()
                return
            }

            val postRef = FirebaseDatabase.getInstance().getReference("posts")
            postRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    val currentPost = currentData.getValue(Post::class.java) ?: return Transaction.success(currentData)
                    if (currentPost.platesAvailable <= 0) return Transaction.success(currentData)
                    if (currentPost.claimants.containsKey(userId)) return Transaction.success(currentData)

                    currentPost.platesAvailable--
                    // Make a mutable copy of the claimants map
                    val mutableClaimants = currentPost.claimants.toMutableMap()

                    mutableClaimants[userId] = System.currentTimeMillis()

                    currentPost.claimants = mutableClaimants
                    currentData.value = currentPost
                    return Transaction.success(currentData)
                }

                override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                    if (error != null) {
                        Toast.makeText(context, "Claim failed", Toast.LENGTH_SHORT).show()
                    } else if (committed) {
                        Toast.makeText(context, "Claim successful", Toast.LENGTH_SHORT).show()
                    }
                }
            })
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
                lat=it.latitude
                lan=it.longitude
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
                Log.d("GeoQuery", "Post $postId entered at $location") // ← Add this

                addPostMarker(postId, location)

                val postRef = FirebaseDatabase.getInstance().getReference("posts").child(postId)
                val listener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val post = snapshot.getValue(Post::class.java)
                        if (post != null) {
                            post.id = postId
                            currentPosts[postId] = post
                            updateRecyclerView()
                            Log.d("RecyclerView", "Post added: $postId")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("Firebase", "Error fetching post $postId: ${error.message}")
                    }
                }

                postRef.addValueEventListener(listener)
                postListeners[postId] = listener
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


