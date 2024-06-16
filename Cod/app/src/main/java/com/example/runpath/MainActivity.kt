package com.example.runpath

import CommunityPage
import HomePage
import LoginPage
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.runpath.database.SessionManager
import com.example.runpath.ui.theme.Maps.MainInterface
import com.example.runpath.ui.theme.Maps.RunPathTheme
import com.example.runpath.ui.theme.ProfileAndCommunity.ProfilePage
import com.example.runpath.ui.theme.RegisterLogin.RegisterPage

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sessionManager = SessionManager(this)

        setContent {
            RunPathTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = if (sessionManager.isLoggedIn()) "mainInterface" else "homePage"
                ) {
                    composable("homePage") {
                        HomePage(navController)
                    }
                    composable("loginPage") {
                        LoginPage(navController)
                    }
                    composable("registerPage") {
                        RegisterPage(navController, sessionManager)
                    }
                    composable("mainInterface") {
                        MainInterface()
                    }
                    composable("profilePage") {
                        ProfilePage(navController, sessionManager)
                    }

                    composable("community") {
                        CommunityPage(navController, sessionManager)
                    }

                }
            }
        }
    }
}


