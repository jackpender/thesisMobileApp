package com.jpender.mobile_app.presentation

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jpender.mobile_app.MainActivity
import com.jpender.mobile_app.Preset
import com.jpender.mobile_app.data.ConnectionState
import com.jpender.mobile_app.data.TemperatureAndHumidityReceiveManager
import com.jpender.mobile_app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TempHumidityViewModel @Inject constructor(
    private val temperatureAndHumidityReceiveManager: TemperatureAndHumidityReceiveManager
) : ViewModel() {

    var deviceName by mutableStateOf<String?>("")

    var initializingMessage by mutableStateOf<String?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var temperature by mutableStateOf(0f)
        private set

    var humidity by mutableStateOf(0f)
        private set

    var connectionState by mutableStateOf<ConnectionState>(ConnectionState.Uninitialized)


    val main: MainActivity = MainActivity()

    var volume: Int = 0
    var gain: Int = 0
    var tone: Int = 0


//    val database = Firebase.database
//    val myRef = database.getReference("Presets")
    var database : DatabaseReference = FirebaseDatabase.getInstance().getReference("Presets")


    private fun subscribeToChanges(){
        viewModelScope.launch {
            temperatureAndHumidityReceiveManager.data.collect{ result ->
                when(result){
                    is Resource.Success -> {
                        deviceName = result.data.deviceName
                        connectionState = result.data.connectionState
//                        temperature = result.data.temperature
//                        humidity = result.data.humidity
                    }
                    is Resource.Loading -> {
                        initializingMessage = result.message
                        connectionState = ConnectionState.CurrentlyInitializing
                    }
                    is Resource.Error -> {
                        result.errorMessage
                        connectionState = ConnectionState.Uninitialized
                    }
                }
            }
        }
    }

    fun disconnect(){
        temperatureAndHumidityReceiveManager.disconnect()
    }

    fun reconnect(){
        temperatureAndHumidityReceiveManager.reconnect()
    }

    fun initializeConnection(){
        errorMessage = null
//        subscribeToChanges()
        viewModelScope.launch {
            temperatureAndHumidityReceiveManager.data.collect{ result ->
                when(result){
                    is Resource.Success -> {
                        deviceName = result.data.deviceName
                        connectionState = result.data.connectionState
//                        temperature = result.data.temperature
//                        humidity = result.data.humidity
                    }
                    is Resource.Loading -> {
                        initializingMessage = result.message
                        connectionState = ConnectionState.CurrentlyInitializing
                    }
                    is Resource.Error -> {
                        result.errorMessage
                        connectionState = ConnectionState.Uninitialized
                    }
                }
            }
        }
        temperatureAndHumidityReceiveManager.startReceiving()

        loadDatabaseToList()
    }

    override fun onCleared(){
        super.onCleared()
        temperatureAndHumidityReceiveManager.closeConnection()
    }


    fun loadDatabaseToList(){
        database.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                main.presetList.clear()
                if (snapshot.exists()){
                    for (presetSnap in snapshot.children){
                        val presetData = presetSnap.getValue(Preset::class.java)

                        main.presetList.add(presetData!!)
                    }
                }
                Log.w(TAG, "Database: " + main.presetList)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }


    fun save(preset: Preset) {
        database.child(preset.presetName).setValue(preset)
    }

    fun readData(presetNameValue: String) {
        database.child(presetNameValue).get().addOnSuccessListener {
            if (it.exists()){
                val volumeValue = it.child("volumeValue").value
                val gainValue = it.child("gainValue").value
                val toneValue = it.child("toneValue").value

//                main.transmitVolume(volumeValue.toString().toInt())
//                main.transmitGain(gainValue.toString().toInt())
//                main.transmitTone(toneValue.toString().toInt())

                volume = volumeValue.toString().toInt()
                gain = gainValue.toString().toInt()
                tone = toneValue.toString().toInt()

            }
        }
    }
}