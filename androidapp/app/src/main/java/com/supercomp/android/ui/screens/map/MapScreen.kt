package com.supercomp.android.ui.screens.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import com.google.maps.android.compose.*
import com.supercomp.android.ui.theme.*

data class NearbyPlace(
    val name: String,
    val latLng: LatLng,
    val address: String = "",
    val isOpen: Boolean? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun MapScreen(
    navController: NavController,
    supermarketBrand: String? = null
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val placesClient = remember { Places.createClient(context) }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
        )
    }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var isLoadingLocation by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var nearbyPlaces by remember { mutableStateOf<List<NearbyPlace>>(emptyList()) }
    var selectedPlace by remember { mutableStateOf<NearbyPlace?>(null) }
    var searchDone by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        hasPermission = it
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // Fix: lastLocation + getCurrentLocation fallback for emulator / fresh devices
    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            isLoadingLocation = true
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    userLocation = LatLng(loc.latitude, loc.longitude)
                    isLoadingLocation = false
                } else {
                    val cts = CancellationTokenSource()
                    fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        cts.token
                    ).addOnSuccessListener { freshLoc ->
                        freshLoc?.let { userLocation = LatLng(it.latitude, it.longitude) }
                        isLoadingLocation = false
                    }.addOnFailureListener {
                        isLoadingLocation = false
                    }
                }
            }.addOnFailureListener {
                isLoadingLocation = false
            }
        }
    }

    val defaultPosition = LatLng(37.1773, -3.5986) // Granada fallback
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultPosition, 13f)
    }

    // Move camera whenever we get a real location
    LaunchedEffect(userLocation) {
        userLocation?.let {
            cameraPositionState.animate(
                com.google.android.gms.maps.CameraUpdateFactory.newCameraPosition(
                    CameraPosition.fromLatLngZoom(it, 14f)
                )
            )
        }
    }

    // Search nearby supermarkets using Places API
    fun searchNearbySupemarkets() {
        val location = userLocation ?: return
        isSearching = true
        searchDone = false
        nearbyPlaces = emptyList()
        selectedPlace = null

        val placeFields = listOf(
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS,
            Place.Field.BUSINESS_STATUS
        )

        val circle = CircularBounds.newInstance(
            com.google.android.gms.maps.model.LatLng(location.latitude, location.longitude),
            2000.0 // 2km radius
        )

        val request = SearchNearbyRequest.builder(circle, placeFields)
            .setIncludedTypes(listOf("supermarket", "grocery_store"))
            .setMaxResultCount(20)
            .build()

        placesClient.searchNearby(request)
            .addOnSuccessListener { response ->
                val results = response.places.filter { place ->
                    if (!supermarketBrand.isNullOrBlank()) {
                        place.name?.contains(supermarketBrand, ignoreCase = true) == true
                    } else {
                        true
                    }
                }.mapNotNull { place ->
                    val latLng = place.latLng ?: return@mapNotNull null
                    NearbyPlace(
                        name = place.name ?: "Unknown",
                        latLng = latLng,
                        address = place.address ?: "",
                        isOpen = place.businessStatus == Place.BusinessStatus.OPERATIONAL
                    )
                }
                nearbyPlaces = results
                isSearching = false
                searchDone = true

                // Move camera to first result
                if (results.isNotEmpty()) {
                    cameraPositionState.move(
                        com.google.android.gms.maps.CameraUpdateFactory.newCameraPosition(
                            CameraPosition.fromLatLngZoom(results.first().latLng, 14f)
                        )
                    )
                }
            }
            .addOnFailureListener {
                isSearching = false
                searchDone = true
                android.util.Log.e("MapScreen", "Places search failed: ${it.message}")
            }
    }

    fun openGoogleMapsSearch() {
        val brand = when (supermarketBrand?.lowercase()) {
            "mercadona" -> "Mercadona"
            "lidl" -> "Lidl"
            "carrefour" -> "Carrefour"
            "alcampo" -> "Alcampo"
            "dia" -> "Supermercados Dia"
            else -> supermarketBrand ?: "supermercado"
        }
        val uri = if (userLocation != null) {
            Uri.parse("geo:${userLocation!!.latitude},${userLocation!!.longitude}?q=${Uri.encode(brand)}")
        } else {
            Uri.parse("geo:0,0?q=${Uri.encode("$brand cerca de mi")}")
        }
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val webUri = Uri.parse(
                "https://www.google.com/maps/search/${Uri.encode(brand)}/@${userLocation?.latitude ?: 37.1773},${userLocation?.longitude ?: -3.5986},14z"
            )
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (!supermarketBrand.isNullOrBlank()) "Nearest $supermarketBrand"
                        else "Supermarkets Near You",
                        fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SuperTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = SuperGreen)
                    }
                },
                actions = {
                    IconButton(onClick = { openGoogleMapsSearch() }) {
                        Icon(Icons.Default.OpenInNew, null, tint = SuperGreen)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = SuperNavy)
            )
        },
        containerColor = SuperNavy
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {

            if (!hasPermission) {
                Box(Modifier.fillMaxSize().background(Color.Black.copy(0.7f)), Alignment.Center) {
                    Card(
                        modifier = Modifier.padding(32.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SuperSurface)
                    ) {
                        Column(
                            Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Location access is required to show nearest stores.",
                                textAlign = TextAlign.Center, color = SuperTextPrimary
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                                colors = ButtonDefaults.buttonColors(containerColor = SuperGreen)
                            ) {
                                Text("Grant Permission", color = Color.Black)
                            }
                        }
                    }
                }
            } else {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = true),
                    uiSettings = MapUiSettings(myLocationButtonEnabled = true)
                ) {
                    // User location marker — blue
                    userLocation?.let {
                        Marker(
                            state = MarkerState(position = it),
                            title = "You are here",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                        )
                    }

                    // Nearby supermarket markers
                    nearbyPlaces.forEach { place ->
                        Marker(
                            state = MarkerState(position = place.latLng),
                            title = place.name,
                            snippet = place.address,
                            icon = BitmapDescriptorFactory.defaultMarker(
                                if (place.isOpen == true) BitmapDescriptorFactory.HUE_GREEN
                                else BitmapDescriptorFactory.HUE_RED
                            ),
                            onClick = {
                                selectedPlace = place
                                false
                            }
                        )
                    }
                }

                // Loading location indicator
                if (isLoadingLocation) {
                    Box(
                        Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = SuperSurface)
                        ) {
                            Row(
                                Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    color = SuperGreen,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Getting your location...", color = SuperTextPrimary, fontSize = 13.sp)
                            }
                        }
                    }
                }

                // Searching indicator
                if (isSearching) {
                    Box(
                        Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = SuperSurface)
                        ) {
                            Row(
                                Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    color = SuperGreen,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Searching for ${supermarketBrand ?: "supermarkets"}...",
                                    color = SuperTextPrimary, fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                // No results message
                if (searchDone && nearbyPlaces.isEmpty()) {
                    Box(
                        Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = SuperSurface)
                        ) {
                            Text(
                                "No ${supermarketBrand ?: "supermarkets"} found nearby",
                                color = Color.Red,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                // Selected place info card
                selectedPlace?.let { place ->
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SuperSurface)
                    ) {
                        Row(
                            Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Store,
                                null,
                                tint = SuperGreen,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    place.name,
                                    color = SuperTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                if (place.address.isNotBlank()) {
                                    Text(
                                        place.address,
                                        color = SuperTextPrimary.copy(alpha = 0.7f),
                                        fontSize = 12.sp
                                    )
                                }
                                Text(
                                    if (place.isOpen == true) "🟢 Open" else "🔴 Closed",
                                    fontSize = 12.sp,
                                    color = if (place.isOpen == true) SuperGreen else Color.Red
                                )
                            }
                            IconButton(onClick = { selectedPlace = null }) {
                                Icon(Icons.Default.Close, null, tint = SuperTextPrimary)
                            }
                        }
                    }
                }

                // Bottom search card
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SuperSurface)
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            if (!supermarketBrand.isNullOrBlank())
                                "Search for $supermarketBrand near you"
                            else "Search for supermarkets near you",
                            color = SuperTextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        if (searchDone && nearbyPlaces.isNotEmpty()) {
                            Text(
                                "${nearbyPlaces.size} found nearby",
                                color = SuperGreen,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = { searchNearbySupemarkets() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = SuperGreen),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isSearching && userLocation != null
                        ) {
                            Icon(
                                Icons.Default.Search,
                                null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (!supermarketBrand.isNullOrBlank()) "Find $supermarketBrand near you"
                                else "Find supermarkets near you",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Small Google Maps button in the corner
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 140.dp)
                ) {
                    SmallFloatingActionButton(
                        onClick = { openGoogleMapsSearch() },
                        containerColor = Color.White,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (!supermarketBrand.isNullOrBlank()) "Open $supermarketBrand in Maps"
                                else "Open in Google Maps",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}