package com.jpender.mobile_app.presentation

import  androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun Navigation(
    onBluetoothStateChanged:() -> Unit
) {

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.TemperatureHumidityScreen.route){
        composable(Screen.StartScreen.route){//Need to look into what a composable is, as well as this route function
            StartScreen(navController = navController )
        }

        composable(Screen.TemperatureHumidityScreen.route){
            TemperatureHumidityScreen(
                onBluetoothStateChanged
            )
        }
    }

}

sealed class Screen(val route:String){
    object StartScreen:Screen("start_screen")
    object TemperatureHumidityScreen:Screen("temp_humid_screen")
}

