package com.jpender.mobile_app

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jpender.mobile_app.presentation.Navigation
import com.jpender.mobile_app.ui.theme.Pedal_connectTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BtMenuActivity : ComponentActivity(){

    @Inject
    lateinit var  bluetoothAdapter: BluetoothAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_bt_menu)
        setContent {
            Pedal_connectTheme {
                Navigation(
                    onBluetoothStateChanged = {
                        showBluetoothDialogue()
                    }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        showBluetoothDialogue()
    }

    private var isBluetoothDialogueAlreadyShown = false

    private fun showBluetoothDialogue(){
        if(bluetoothAdapter.isEnabled == false){
            if (isBluetoothDialogueAlreadyShown) {
                val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                val intent = Intent(this@BtMenuActivity, MainActivity::class.java)
                requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH)
//                startBluetoothIntentForResult.launch(enableBluetoothIntent)
//                Toast.makeText(this@BtMenuActivity,"Bluetooth Not Enabled", Toast.LENGTH_SHORT).show()
                isBluetoothDialogueAlreadyShown = true
            }
        }
    }

    private fun requestBluetoothPermission(){
        when{
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED -> {
                Toast.makeText(this@BtMenuActivity,"Bluetooth Permission Also Granted", Toast.LENGTH_SHORT).show()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.BLUETOOTH
            ) -> {
                requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH)
                Toast.makeText(this@BtMenuActivity,"Bluetooth Permission Also Not Granted", Toast.LENGTH_SHORT).show()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH)
//                showBluetoothDialogue()
//                Toast.makeText(this@BtMenuActivity,"Bluetooth Permission Also Not Granted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val startBluetoothIntentForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result: ActivityResult ->
            isBluetoothDialogueAlreadyShown = false
            if(result.resultCode != Activity.RESULT_OK){
                Toast.makeText(this@BtMenuActivity,"result code: " + result.resultCode.toString(), Toast.LENGTH_SHORT).show()
                showBluetoothDialogue()
            }
        }
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){
            isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this@BtMenuActivity,"Bluetooth Permission Granted", Toast.LENGTH_SHORT).show()
//                requestBluetoothPermission()

            }
            else {
//                showBluetoothDialogue()
//                Toast.makeText(this@BtMenuActivity,"Bluetooth Permission Not Granted", Toast.LENGTH_SHORT).show()
                requestBluetoothPermission()
            }
        }
}