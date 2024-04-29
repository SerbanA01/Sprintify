//package com.example.runpath
//
package com.example.runpath
import FeedReaderDbHelper
import HomePage
import LoginPage
import android.os.Bundle
import androidx.activity.compose.setContent
import com.example.runpath.ui.theme.RunPathTheme
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.runpath.database.SessionManager
import com.example.runpath.databinding.ActivityMapsBinding
import com.example.runpath.ui.theme.MainInterface
import com.example.runpath.ui.theme.RegisterPage
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState


//class MainActivity : AppCompatActivity(), OnMapReadyCallback {
//    private lateinit var mMap: GoogleMap
//    private lateinit var binding: ActivityMapsBinding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        binding = ActivityMapsBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        val mapFragment = supportFragmentManager
//            .findFragmentById(R.id.map) as SupportMapFragment
//        mapFragment.getMapAsync(this)
//    }
//
//    override fun onMapReady(googleMap: GoogleMap) {
//        mMap = googleMap
//
//        // Add a marker in Sydney and move the camera
//        val sydney = LatLng(-34.0, 151.0)
//        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
//    }
//}

class MainActivity : AppCompatActivity() /*, OnMapReadyCallback*/ {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionManager = SessionManager(this)

        setContent {

            RunPathTheme {
                val dbHelper = FeedReaderDbHelper(this@MainActivity)
                val navController = rememberNavController()
                //pentru cazul in care userul nu este logat in cont
                if (sessionManager.isLoggedIn() == false) {
                    NavHost(navController, startDestination = "homePage")
                    {
                        composable("homePage") {
                            HomePage(dbHelper, navController)
                        }
                        composable("loginPage") {
                            LoginPage(navController, dbHelper)
                        }
                        composable("registerPage") { RegisterPage(navController, dbHelper) }
                        composable("mainInterface") { MainInterface() }
                    }
//                Box(contentAlignment = Alignment.Center,modifier = Modifier.fillMaxWidth())
//                {
//                    HomePage(dbHelper)
//                }
                }
                //pentru cazul in care userul este deja logat in cont
                else {
                    NavHost(navController, startDestination = "HomePage")
                    {
                        composable("homePage") {
                            HomePage(dbHelper, navController)
                        }
                        composable("loginPage") {
                            LoginPage(navController, dbHelper)
                        }
                        composable("registerPage") { RegisterPage(navController, dbHelper) }
                        composable("mainInterface") { MainInterface() }
                    }
                }

            }

        }
    }
}