package com.example.runpath.ui.theme.Maps

import CircuitsPage
import RunPage
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.UiModeManager
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.runpath.R
import com.example.runpath.database.RunDAO
import com.example.runpath.database.SessionManager
import com.example.runpath.models.Run
import com.example.runpath.others.MyLatLng
import com.example.runpath.ui.theme.ProfileAndCommunity.CommunityPage
import com.example.runpath.ui.theme.CircuitAndRun.Previous_runs
import com.example.runpath.ui.theme.ProfileAndCommunity.ProfilePage
import com.example.runpath.ui.theme.ProfileAndCommunity.UserProfilePage
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.GeoApiContext
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

// bara de navigare de jos
sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    data object Map : BottomNavItem("mapPage", Icons.Default.Home, "Map")
    data object Community : BottomNavItem("CommunityPage", Icons.Default.Star, "Community")

    data object Run : BottomNavItem("run", Icons.Default.Add, "Run")
    data object Circuit : BottomNavItem("circuit", Icons.Default.LocationOn, "Circuit")
    data object Profile : BottomNavItem("ProfilePage", Icons.Default.AccountBox, "Profile")
    companion object {
        val values = listOf(Map, Community, Run, Circuit, Profile)
    }
}


@Composable
fun BottomNavigationBar(navController: NavController) {
    BottomNavigation(
        backgroundColor = Color.Gray,
        contentColor = Color.White
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        BottomNavItem.values.forEach { item ->
            BottomNavigationItem(
                selected = currentRoute == item.route,

                onClick = {

                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = null) },
                label = { Text(item.label) }
            )
        }
    }
}

// functii pentru permisiuni si locatie
fun savePermissionStatus(context: Context, isGranted: Boolean) {
    val sharedPreferences = context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putBoolean("LocationPermissionGranted", isGranted)
    editor.apply()
}

fun getPermissionStatus(context: Context): Boolean {
    val sharedPreferences = context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
    println(
        "LocationPermissionGranted: ${
            sharedPreferences.getBoolean(
                "LocationPermissionGranted",
                false
            )
        }"
    )
    return sharedPreferences.getBoolean("LocationPermissionGranted", false) // fals ca default
}

// functie pentru a cere permisiunea de locatie
@Composable
fun RequestLocationPermission(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
) {
    val context = LocalContext.current
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                savePermissionStatus(context, true) // salveaza statusul permisiunii
                onPermissionGranted()
            } else {
                savePermissionStatus(context, false)
                onPermissionDenied()
            }
        }
    )

    LaunchedEffect(Unit) {
        // verifica daca permisiunea a fost deja acordata
        if (getPermissionStatus(context)) {
            println("Permission was granted")
            // permisiunea a fost deja acordata
            onPermissionGranted()
        } else {
            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}

// functie pentru a obtine locatia curenta
@SuppressLint("MissingPermission")
fun getCurrentLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (Location) -> Unit
) {
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            onLocationReceived(location)
        } else {
            // construieste un nou request pentru locatie
            val locationRequest = LocationRequest.Builder(10000L) // seteaza intervalul la 10s
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxUpdateDelayMillis(5000L)
                .build()

            val locationCallback = object : com.google.android.gms.location.LocationCallback() {
                override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                    locationResult.locations.firstOrNull()?.let {
                        onLocationReceived(it)
                        fusedLocationClient.removeLocationUpdates(this) // eliminam callback-ul
                    }
                }
            }
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }.addOnFailureListener { e ->
        println("Error getting location: ${e.message}")
    }
}


// codul pentru live tracking
fun calculateDistance(point1: LatLng, point2: LatLng): Double {
    val earthRadius = 6371.0 // Radius of the Earth in kilometers
    val latDiff = Math.toRadians(point2.latitude - point1.latitude)
    val lngDiff = Math.toRadians(point2.longitude - point1.longitude)
    val a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
            Math.cos(Math.toRadians(point1.latitude)) * Math.cos(Math.toRadians(point2.latitude)) *
            Math.sin(lngDiff / 2) * Math.sin(lngDiff / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return earthRadius * c
}

// data class pentru segment
data class Segment(val startIndex: Int, val endIndex: Int, val color: Color)

// functie pentru a plasa un marker pe harta
@Composable
fun placeMarker(location: LatLng, title: String) {
    Marker(
        state = MarkerState(position = location),
        title = title,
        snippet = "Marker at $title",
        icon = BitmapDescriptorFactory.fromResource(R.drawable.current_location_icon)
    )
}

// buton pentru a incepe/pauza tracking-ul
@Composable
fun RunControlButton(
    isRunActive: MutableState<Boolean>,
    startedRunningFlag: MutableState<Boolean>,
    locationPoints: SnapshotStateList<LatLng>,
    segments: SnapshotStateList<Segment>,
    onButtonClick: () -> Unit,
    totalDistance: MutableState<Double>
) {
    val buttonText = if (isRunActive.value) "Pause Run" else "Start Run"
    var time by remember { mutableStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf(0L) }
    var startTimeDb by remember { mutableStateOf(0L) }
    var totalPausedTime by remember { mutableStateOf(0L) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedRating by remember { mutableStateOf(0) }
    val context = LocalContext.current
    // add max distance so we can fix the bug where distance is not calculated correctly
    var maxdistance by remember { mutableStateOf(0.0) }
    val userId = SessionManager(context).getsharedPreferences().getString(SessionManager.KEY_USER_ID, "N/A")!!
    //logic for running on a circuit
    val circuitId = "N/A"
    var paceTrackerDb by remember { mutableStateOf(0.0) }
    val runDAO = RunDAO()


    Button(
        onClick = {


            if (!startedRunningFlag.value) {
                startedRunningFlag.value = true
                startTimeDb = System.currentTimeMillis()
            }
            val currentColor = if (isRunActive.value) Color.Red else Color.Blue
            //calculate the max distance
            if (totalDistance.value > maxdistance) {
                maxdistance = totalDistance.value
            }
            if (segments.isNotEmpty()) {
                val lastSegment = segments.last()
                if (lastSegment.color != currentColor) {
                    segments.add(
                        Segment(
                            lastSegment.endIndex,
                            locationPoints.size - 1,
                            currentColor
                        )
                    )

                }
            } else {
                segments.add(Segment(0, locationPoints.size - 1, currentColor))
            }

            onButtonClick()
            isRunActive.value = !isRunActive.value

            if (isRunActive.value) {
                startTime = System.currentTimeMillis() - totalPausedTime
            } else {
                totalPausedTime += System.currentTimeMillis() - startTime
            }

            },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
    ) {
        Text(text = buttonText)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = FormatTime(time),
            modifier = Modifier
                .background(Color.LightGray) // Add background
                .border(2.dp, Color.Black) // Add border
                .padding(4.dp) // Add padding for better visual effect
        )
        Text(
            text = "Distance: ${"%.3f".format(totalDistance.value)} km",
            modifier = Modifier
                .background(Color.LightGray) // Add background
                .border(2.dp, Color.Black) // Add border
                .padding(4.dp) // Add padding for better visual effect
        )
    }


    if(isRunActive.value) {
        if(!isRunning) {
            isRunning = true
            startTime = System.currentTimeMillis()
        }
        time = System.currentTimeMillis() - startTime + totalPausedTime
    } else {
        isRunning = false
    }

    // buton pentru a opri tracking-ul
    if (startedRunningFlag.value) {
        Column {
            val formattedDistance = String.format("%.2f",maxdistance)
            Button(

                onClick =
                {
                    //save the run in the database
                    val run = Run(
                        userId = userId,
                        circuitId = circuitId,
                        startTime = FormatTime2(startTimeDb),
                        endTime = FormatTime2(System.currentTimeMillis()),
                        pauseTime = FormatTime(totalPausedTime),
                        timeTracker = FormatTime(time),
                        paceTracker = paceTrackerDb,
                        distanceTracker = formattedDistance.toDouble(),
                        coordinate = locationPoints.map { MyLatLng(it.latitude, it.longitude) }
                    )
                    runDAO.insertRun(run) { newRun ->
                        println("Run added to database with ID: ${newRun.runId}")
                    }


                    segments.clear()
                    locationPoints.clear()
                    startedRunningFlag.value = false
                    isRunActive.value = false
                    totalPausedTime = 0
                    time = 0
                    startTime = 0
                    totalDistance.value = 0.0
                    selectedRating = 0
                    //we will save the run in the previous runs database


                }, modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp)


            ) {
                Text("Stop Run")
            }

        }

    }

}


fun FormatTime(time: Long): String {
    val miliseconds = time % 1000
    val seconds = TimeUnit.MILLISECONDS.toSeconds(time) % 60
    val minutes = TimeUnit.MILLISECONDS.toMinutes(time) % 60

    return String.format("%02d:%02d:%03d", minutes, seconds, miliseconds)
}
fun FormatTime2(time: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(time))
}


// functie pentru a obtine locatia curenta si a incepe tracking-ul
@SuppressLint("MissingPermission")
fun getCurrentLocationAndTrack(
    fusedLocationClient: FusedLocationProviderClient,
    locationPoints: SnapshotStateList<LatLng>,
    segments: SnapshotStateList<Segment>,
    isRunActive: MutableState<Boolean>,
    startedRunningFlag: MutableState<Boolean>,
    currentLocation: MutableState<LatLng?>,
    totalDistance: MutableState<Double>
) {
    val locationRequest = LocationRequest.create().apply {
        // seteaza intervalul pentru a obtine locatia curenta a utilizatorului
        interval = 3000
        fastestInterval = 1000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    // callback pentru locatie
    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations
            if (locationList.isNotEmpty() && startedRunningFlag.value) {
                val newLocation = locationList.last()
                val newLatLng = LatLng(newLocation.latitude, newLocation.longitude)
                currentLocation.value = newLatLng
                locationPoints.add(newLatLng)

                if (locationPoints.size >= 2 && isRunActive.value) {
                    val lastPoint = locationPoints[locationPoints.size - 2]
                    val distance = calculateDistance(lastPoint, newLatLng)
                    totalDistance.value += distance
                }

                if (segments.isNotEmpty()) {
                    val lastSegment = segments.last()
                    if (isRunActive.value && lastSegment.color == Color.Blue) {
                        segments.add(
                            Segment(
                                lastSegment.endIndex,
                                locationPoints.size - 1,
                                Color.Red
                            )
                        )
                    } else if (!isRunActive.value && lastSegment.color == Color.Red) {
                        segments.add(
                            Segment(
                                lastSegment.endIndex,
                                locationPoints.size - 1,
                                Color.Blue
                            )
                        )
                    } else {
                        segments[segments.lastIndex] =
                            lastSegment.copy(endIndex = locationPoints.size - 1)
                    }
                } else {
                    val initialColor = if (isRunActive.value) Color.Red else Color.Blue
                    segments.add(Segment(0, locationPoints.size - 1, initialColor))
                }
            }
        }
    }
    fusedLocationClient.requestLocationUpdates(
        locationRequest,
        locationCallback,
        Looper.getMainLooper()
    )
}

// functie pentru a obtine punctele de pe ruta
@Composable
fun placeMarkerOnMap(location: LatLng, title: String) {
    Marker(
        state = MarkerState(position = location),
        title = title,
        snippet = "Marker at $title"
    )
}

// functie pentru a afisa harta
@Composable
fun GMap(
    currentLocation: MutableState<LatLng?>,
    searchedLocation: MutableState<LatLng?>,
    cameraPositionState: CameraPositionState,
    locationPoints: SnapshotStateList<LatLng>,
    segments: SnapshotStateList<Segment>,
    startedRunningFlag: MutableState<Boolean>,
    route: List<LatLng>?
) {
    // creez un nou obiect MapsActivity
    val mapsActivity = MapsActivity()
    val routePoints = remember { mutableStateOf(listOf<LatLng>()) }
    val thresholdDistance = 0.025

    LaunchedEffect(
        key1 = currentLocation.value,
        key2 = searchedLocation.value,
        key3 = startedRunningFlag.value
    ) {
        if (currentLocation.value != null && searchedLocation.value != null && !startedRunningFlag.value) {
            routePoints.value =
                mapsActivity.getRoutePoints(currentLocation.value!!, searchedLocation.value!!)
        }
    }

//    val cameraPositionState = rememberCameraPositionState()
//    LaunchedEffect(currentLocation.value) {
//        val previousLocation = cameraPosition.value
//        val newLocation = currentLocation.value
//
//        if (previousLocation != null && newLocation != null) {
//            if (calculateDistance(previousLocation, newLocation) > thresholdDistance) {
//                cameraPositionState.position = CameraPosition.fromLatLngZoom(newLocation, 15f)
//                cameraPosition.value = newLocation
//            }
//        } else if (newLocation != null) {
//            cameraPositionState.position = CameraPosition.fromLatLngZoom(newLocation, 15f)
//            cameraPosition.value = newLocation
//        }
//    }
//
//
//    LaunchedEffect(key1 = cameraTilt.value) {
//        if(cameraTilt.value != cameraPositionState.position.tilt && currentLocation.value != null) {
//            cameraPositionState.position = CameraPosition.builder()
//                .target(currentLocation.value!!)
//                .zoom(15f)
//                .tilt(cameraTilt.value)
//                .build()
//        }
//    }
//
//    LaunchedEffect(cameraPosition.value) {
//        cameraPosition.value?.let {
//            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 15f)
//        }
//    }

    val context = LocalContext.current
    val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
    val isNightMode = when (uiModeManager.nightMode) {
        UiModeManager.MODE_NIGHT_YES -> true
        else -> false
    }
    val mapProperties: MapProperties
    val mapStyle = if (isNightMode) {
        mapProperties = remember {
            MapProperties(
                mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.dark_mode_map)
            )
        }
    } else {
        mapProperties = remember {
            MapProperties(
                mapStyleOptions = MapStyleOptions.loadRawResourceStyle(
                    context,
                    R.raw.light_mode_map
                )
            )
        }
    }
    // afisez harta
    GoogleMap(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 56.dp),
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        onMapLongClick = { latLng ->
            if (!startedRunningFlag.value) {
                searchedLocation.value = latLng
            }
        }
    ) {
        // marker pentru locatia curenta
        currentLocation.value?.let {
            placeMarker(
                location = it,
                title = "Current Location"
            )
        }

        // marker pentru locatia cautata
        searchedLocation.value?.let {
            placeMarkerOnMap(location = searchedLocation.value!!, title = "Searched Location")
        }

        if (startedRunningFlag.value) {
            segments.forEach { segment ->
                // Ensure indices are within bounds
                val startIndex =
                    segment.startIndex.coerceAtLeast(0).coerceAtMost(locationPoints.size)
                val endIndex = (segment.endIndex + 1).coerceAtLeast(startIndex)
                    .coerceAtMost(locationPoints.size)

                if (startIndex < endIndex) { // Check to avoid empty sublist
                    Polyline(
                        points = locationPoints.subList(startIndex, endIndex),
                        color = segment.color,
                        width = 5f
                    )
                }
            }
        }

        if(route != null){
            Polyline(

                points = route,
                color = Color.Red,
                width = 10f
            )
            //calculam centrul rutei pentru a centra camera
            val minLat = route.minOf { it.latitude }
            val maxLat = route.maxOf { it.latitude }
            val minLng = route.minOf { it.longitude }
            val maxLng = route.maxOf { it.longitude }

            val midLat = (minLat + maxLat) / 2
            val midLng = (minLng + maxLng) / 2

            val midPoint = LatLng(midLat, midLng)

            cameraPositionState.position = CameraPosition.fromLatLngZoom(midPoint, 15f)


        }

        if (routePoints.value.isNotEmpty()) { // afiseaza ruta pe harta sub forma unui polyline
            Polyline(
                points = routePoints.value,
                color = Color.Red,
                width = 10f
            )
        }

    }
}

// functie pentru a cauta locatii
@Composable
fun LocationSearchBar(
    placesClient: PlacesClient,
    searchedLocation: MutableState<LatLng?>
) {
    val context = LocalContext.current
    val geocoder = remember { Geocoder(context) }
    val searchQuery = remember { mutableStateOf("") }
    val suggestions = remember { mutableStateOf(listOf<AutocompletePrediction>()) }
    val showSuggestions = remember { mutableStateOf(true) }

    // efect pentru a cauta locatii
    LaunchedEffect(searchQuery.value) {
        if (showSuggestions.value) {
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(searchQuery.value)
                .build()

            placesClient.findAutocompletePredictions(request).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val response = task.result
                    suggestions.value = response.autocompletePredictions
                } else {
                    // eroarea in cazul in care cautarea a esuat
                    println("Autocomplete prediction fetch failed: ${task.exception?.message}")
                }
            }
        }
    }

    // afiseaza campul de cautare
    Column {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = searchQuery.value,
            onValueChange = {
                searchQuery.value = it
                showSuggestions.value = true  // sugereaza locatii
            },
            label = { Text("Search location") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                val addresses: List<Address> =
                    geocoder.getFromLocationName(searchQuery.value, 1) ?: listOf()
                if (addresses.isNotEmpty()) {
                    val location = LatLng(addresses[0].latitude, addresses[0].longitude)
                    searchedLocation.value = location
                }
            })
        )
        // afiseaza sugestiile
        if (showSuggestions.value) {
            suggestions.value.forEach { prediction ->
                Text(
                    text = prediction.getFullText(null).toString(),
                    modifier = Modifier.clickable {
                        showSuggestions.value = false // Hide suggestions on click
                        val placeId = prediction.placeId
                        val placeFields = listOf(Place.Field.LAT_LNG)
                        val request = FetchPlaceRequest.newInstance(placeId, placeFields)

                        placesClient.fetchPlace(request).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val place = task.result
                                searchedLocation.value = place.place.latLng
                                searchQuery.value = prediction.getFullText(null)
                                    .toString() // Update the search bar text
                                showSuggestions.value =
                                    false  // Ensure it remains hidden after the update
                            } else {
                                // Handle the error.
                                println("Place fetch failed: ${task.exception?.message}")
                            }
                        }
                    }
                )
            }
        }
    }
}

// buton pentru a merge la locatia curenta
@Composable
fun CurrentLocationButton(
    currentLocation: MutableState<LatLng?>,
    cameraPosition: MutableState<LatLng?>,
    fusedLocationClient: FusedLocationProviderClient
) {

    Button(onClick = {
        getCurrentLocation(fusedLocationClient) { location ->
            val latLng = LatLng(location.latitude, location.longitude)
            currentLocation.value = latLng
            cameraPosition.value = currentLocation.value
            println("Current location: ${currentLocation.value}")
        }
    }) {
        Text("Go to Current Location")
    }
}

@Composable
fun TiltButton(cameraTilt: MutableState<Float>) {

    Button(onClick = {
        // schimba inclinatia camerei din 0 in 45 si invers
        cameraTilt.value = if (cameraTilt.value == 0f) 45f else 0f
    }) {
        Text("Toggle Tilt")
    }

}

// ecranul pentru harta
@SuppressLint("MissingPermission")
@Composable
fun MapScreen(
    currentLocation: MutableState<LatLng?>,
    searchedLocation: MutableState<LatLng?>,
    placesClient: PlacesClient,
    route: List<LatLng>? = null
) {

    val context = LocalContext.current
    val contextMap = GeoApiContext.Builder()
        .apiKey("AIzaSyBcDs0jQqyNyk9d1gSpk0ruLgvbd9pwZrU")
        .build()

    // Location tracker
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Lists for the location points and the segments that
    // make up the polyline
    val locationPoints = remember { mutableStateListOf<LatLng>() }
    val segments = remember { mutableStateListOf<Segment>() }

    // Flag for run activation
    val isRunActive = remember { mutableStateOf(false) }

    // Camera position
    val cameraPosition = remember { mutableStateOf<LatLng?>(null) }

    // Camera tilt
    val cameraTilt = remember { mutableStateOf(0f) } // inclinarea initila este 0

    // Flag for when the run is started (distinguishes between run started / run paused)
    val startedRunningFlag = remember { mutableStateOf(false) }

    val totalDistance = remember { mutableStateOf(0.0) }

    val orientationListener = remember {OrientationListener(context)}
    val cameraPositionState = rememberCameraPositionState()

    // Variable for the current bearing of the camera
    var currentBearing by remember {mutableStateOf(0f)}

    fun animateCameraPosition(
        currentLatLng: LatLng,
        currentBearing: Float,
        targetLatLng: LatLng,
        targetBearing: Float,
        tilt: Float,
        zoom: Float,
        duration: Long = 1000 // Animation duration in milliseconds
    ) {
        val startLatLng = currentLatLng
        val startBearing = currentBearing

        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = duration
        animator.addUpdateListener { animation ->
            val fraction = animation.animatedValue as Float

            val interpolatedLatLng = LatLng(
                orientationListener.lerp(startLatLng.latitude.toFloat(), targetLatLng.latitude.toFloat(), fraction).toDouble(),
                orientationListener.lerp(startLatLng.longitude.toFloat(), targetLatLng.longitude.toFloat(), fraction).toDouble()
            )
            val interpolatedBearing = orientationListener.lerp(startBearing, targetBearing, fraction)

            val cameraPosition = CameraPosition.Builder()
                .target(interpolatedLatLng)
                .bearing(interpolatedBearing)
                .tilt(tilt)
                .zoom(zoom)
                .build()

            cameraPositionState.position = cameraPosition
        }
        animator.start()
    }

    RequestLocationPermission(
        onPermissionGranted = {
            getCurrentLocation(fusedLocationClient) { location ->
                val latLng = LatLng(location.latitude, location.longitude)
                currentLocation.value = latLng
                /* if (searchedLocation.value == null) {
                     searchedLocation.value = latLng // seteaza locatia curenta ca locatie cautata
                 }*/

                getCurrentLocationAndTrack(
                    fusedLocationClient,
                    locationPoints,
                    segments,
                    isRunActive,
                    startedRunningFlag,
                    currentLocation,
                    totalDistance= totalDistance
                )
            }
        },
        onPermissionDenied = {
            println("Permission denied")
        }
    )

    // Camera update logic
    LaunchedEffect(currentLocation.value, orientationListener.azimuth, cameraTilt.value) {
        val location = currentLocation.value
        location?.let {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                val bearing = location?.bearing ?: orientationListener.azimuth
//                val smoothedBearing = orientationListener.lowPassFilter(rawBearing, currentBearing, 0.75f)
//                currentBearing = smoothedBearing

                val targetLatLng = LatLng(location.latitude, location.longitude)
                animateCameraPosition(
                    currentLatLng = LatLng(cameraPositionState.position.target.latitude, cameraPositionState.position.target.longitude),
                    currentBearing = cameraPositionState.position.bearing,
                    targetLatLng = targetLatLng,
                    targetBearing = bearing,
                    tilt = cameraTilt.value,
                    zoom = 15f
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // search bar pentru locatii

        if (startedRunningFlag.value == false) {
            LocationSearchBar(
                placesClient = placesClient,
                searchedLocation = searchedLocation
            )
        }

        Box(
            modifier = Modifier.weight(1f)
        ) {
            // afiseaza mapa si butoanele
            GMap(
                currentLocation = currentLocation,
                searchedLocation = searchedLocation,
                cameraPositionState = cameraPositionState,
                locationPoints = locationPoints,
                segments = segments,
                startedRunningFlag = startedRunningFlag,
                route = route
            )


            // start/pause button
            Column {
                RunControlButton(
                    isRunActive = isRunActive,
                    startedRunningFlag = startedRunningFlag,
                    locationPoints = locationPoints,
                    segments = segments,
                    totalDistance =  totalDistance,
                    onButtonClick = {}
                )

            }




            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart) // pozitioneaza butoanele in coltul din stanga jos
                    .padding(bottom = 56.dp) // padding optional

            ) {
                CurrentLocationButton(
                    currentLocation = currentLocation,
                    cameraPosition = cameraPosition,
                    fusedLocationClient = fusedLocationClient
                )
                TiltButton(cameraTilt = cameraTilt)
            }
        }
    }
}

@Composable
fun NavigationHost(navController: NavHostController) {
    val context = LocalContext.current
    val sessionManager = SessionManager(context)
    val currentLocation = remember { mutableStateOf<LatLng?>(null) }
    val searchedLocation = remember { mutableStateOf<LatLng?>(null) }
    if (!Places.isInitialized()) {
        Places.initialize(context, "AIzaSyBcDs0jQqyNyk9d1gSpk0ruLgvbd9pwZrU")
    }
    val placesClient = Places.createClient(context)

    NavHost(navController, startDestination = BottomNavItem.Map.route) {
        composable(BottomNavItem.Map.route) {
            MapScreen(currentLocation, searchedLocation, placesClient)
        }
        composable(BottomNavItem.Community.route) {
            CommunityPage(navController, sessionManager)
        }
        composable(BottomNavItem.Run.route) {
            RunPage { option ->
                when (option) {
                    "From a Circuit" -> navController.navigate("circuitScreen")
                    "From a Previous Run" -> navController.navigate("previous_runs")
                    "Freemode" -> navController.navigate("freemodeScreen")
                }
            }
        }
        composable(BottomNavItem.Circuit.route) {
            CircuitsPage(navController, sessionManager)
        }
        //redurect to map screen with the route drawn from a circuit
        composable("mapPage/route={route}") { backStackEntry ->
            val routeString = backStackEntry.arguments?.getString("route")
            val route = routeString?.split("|")?.map {
                val latLng = it.split(",")
                LatLng(latLng[0].toDouble(), latLng[1].toDouble())
            }
            MapScreen(currentLocation, searchedLocation, placesClient,route)
        }
        //redirect to map screen with the poluline drawn from the previous run
        composable("previous_run/route={route}") { backStackEntry ->
            val routeString = backStackEntry.arguments?.getString("route")
            val route = routeString?.split("|")?.map {
                val latLng = it.split(",")
                LatLng(latLng[0].toDouble(), latLng[1].toDouble())
            }
            MapScreen(currentLocation, searchedLocation, placesClient,route)
        }
        composable(BottomNavItem.Profile.route) {
            ProfilePage(navController, sessionManager)
        }
        //this one will show the previsous runs with the select button
        composable("previous_runs") {
            Previous_runs(navController, sessionManager,true)
        }
        //history will send a false value to the previous_runs composable to not show the select button
        composable("previous_runsHistory") {
            Previous_runs(navController, sessionManager,false)
        }
        composable("circuitScreen") {
            CircuitsPage(navController, sessionManager, true)
        }
        composable("userProfile/{username}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            UserProfilePage(navController, username)
        }
    }
}

fun interpolatePoints(start: LatLng, end: LatLng, steps: Int): List<LatLng> {
    val latStep = (end.latitude - start.latitude) / steps
    val lngStep = (end.longitude - start.longitude) / steps

    return (1 until steps).map {
        LatLng(start.latitude + it * latStep, start.longitude + it * lngStep)
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MainInterface() {
    val navController = rememberNavController()
    val context = LocalContext.current
    if (!Places.isInitialized()) {
        Places.initialize(context, "AIzaSyBcDs0jQqyNyk9d1gSpk0ruLgvbd9pwZrU") // Replace "YOUR_API_KEY" with your actual Places API key
    }
    val placesClient = Places.createClient(context)
    val totalDistance = remember { mutableStateOf(0.0) }
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        NavigationHost(navController = navController)

    }
}
