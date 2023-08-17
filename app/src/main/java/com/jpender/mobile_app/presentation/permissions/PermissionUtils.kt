package com.jpender.mobile_app.presentation.permissions

import android.os.Build
//import com.jpender.pedal_connect.Manifest

object PermissionUtils {

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
        listOf(
            android.Manifest.permission.BLUETOOTH_SCAN, android.Manifest.permission.BLUETOOTH_CONNECT,
            android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }else {
        listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
}