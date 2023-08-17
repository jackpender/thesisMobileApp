package com.jpender.mobile_app.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import com.jpender.mobile_app.data.ConnectionState
import com.jpender.mobile_app.data.TempHumidityResult
import com.jpender.mobile_app.data.TemperatureAndHumidityReceiveManager
import com.jpender.mobile_app.util.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import com.jpender.mobile_app.MainActivity
//import com.jpender.pedal_connect.volumeCharacteristic

//import com.jpender.pedal_connect.characteristic

@SuppressLint("MissingPermission")
class TemperatureAndHumidityBLEReceiveManager @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter,
    private val context: Context
) : TemperatureAndHumidityReceiveManager {

    private val DEVICE_NAME = "Nano 33 IoT" //Need to come back and fill this in properly
    val TEMP_HUMIDITY_SERVICE_UUID = "0000180a-0000-1000-8000-00805f9b34fb"
    val TEMP_HUMIDITY_CHARACTERISTICS_UUID = "00002a57-0000-1000-8000-00805f9b34fb" //Need to come back and properly fill these in, but also potentially don't need them

    override val data: MutableSharedFlow<Resource<TempHumidityResult>> = MutableSharedFlow()


    private val blescanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) //For scanning over a short period of time
        .build()

    val device = bluetoothAdapter.getRemoteDevice("C8:C9:A3:E5:EB:62")
    var gatt: BluetoothGatt? = null
//    var gatt1: BluetoothGatt? = null

    val main: MainActivity = MainActivity()


    private var isScanning = false

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val scanCallback = object : ScanCallback(){
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if(result.device.name == DEVICE_NAME){
                coroutineScope.launch {
                    data.emit(Resource.Loading(message = "Connecting to device..."))
//                    gatt1 = result.device.connectGatt(context, false, gattCallback)
//                    this@TemperatureAndHumidityBLEReceiveManager.gatt = device.connectGatt(context, false, gattCallback)
//                    gatt1 = this@TemperatureAndHumidityBLEReceiveManager.gatt
                    Log.w(TAG, "Characteristic Value3 : " + this@TemperatureAndHumidityBLEReceiveManager.gatt.toString())
                }
                if(isScanning){
                    result.device.connectGatt(context, true, gattCallback, BluetoothDevice.TRANSPORT_LE) //BluetoothDevice.TRANSPORT_LE added
                                                                                                                    // as the arduino has both bt and ble
                                                                                                                    // and so need to ensure that it is
                                                                                                                    // still possible to discover ble
                                                                                                                    // services
                    isScanning = false
                    blescanner.stopScan(this)
//                    this@TemperatureAndHumidityBLEReceiveManager.gatt = device.connectGatt(context, false, gattCallback)
                }
            }
        }
    }

    private var currentConnectionAttempt = 1
    private var MAXIMUM_CONNECTION_ATTEMPTS = 5

    private val gattCallback = object : BluetoothGattCallback(){
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if(status == BluetoothGatt.GATT_SUCCESS){
                if (newState == BluetoothProfile.STATE_CONNECTED){
                    coroutineScope.launch {
                        data.emit(Resource.Loading(message = "Discovering Services..."))
                    }
                    gatt.discoverServices()
                    this@TemperatureAndHumidityBLEReceiveManager.gatt = gatt
//                    this@TemperatureAndHumidityBLEReceiveManager.gatt1 = gatt
//                    gatt1.discoverServices()
                    Log.w(TAG, "Characteristic Value4 : " + this@TemperatureAndHumidityBLEReceiveManager.gatt.toString())
                    Log.w(TAG, "Characteristic Value5 : " + gatt.toString())
                } else if(newState == BluetoothProfile.STATE_DISCONNECTED){
                    coroutineScope.launch {
                        data.emit(Resource.Success(data = TempHumidityResult("", ConnectionState.Disconnected)))
                    }
                    gatt.close()
                }
            }else{
                gatt.close()
                currentConnectionAttempt+=1
                coroutineScope.launch {
                    data.emit(
                        Resource.Loading(
                            message ="Attempting to connect $currentConnectionAttempt/$MAXIMUM_CONNECTION_ATTEMPTS"
                        )
                    )
                }
                if (currentConnectionAttempt<=MAXIMUM_CONNECTION_ATTEMPTS){
                    startReceiving()
                }else{
                    coroutineScope.launch{
                        data.emit(Resource.Error(errorMessage = "Could not connect to BLE device"))
                    }
                }
            }
        }


        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            with(gatt){
                printGattTable()   // Extension provided in Github of tutorial, shows what BLE services are discovered and what services are provided
                coroutineScope.launch {
                    data.emit(Resource.Loading(message = "Adjusting MTU Space..."))
//                    Log.w(TAG, "Device Connected " + DEVICE_NAME)
                }
                gatt.requestMtu(517) //MTU is the maximum amount of data that can be sent from the BLE Device
                                                //to our phone. By default this is 20 Bytes and 517 is the maximum, but that
                                                //doesn't mean all of it is used.

            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            val characteristic = findCharacteristic(TEMP_HUMIDITY_SERVICE_UUID, TEMP_HUMIDITY_CHARACTERISTICS_UUID)
            if (characteristic == null){
                coroutineScope.launch {
                    data.emit(Resource.Error(errorMessage = "Could not find temp and humidity publisher"))
                }
                return
            }
//            Log.w(TAG, "Gatt Value2 : " + this@TemperatureAndHumidityBLEReceiveManager.gatt?.getService(UUID.fromString(TEMP_HUMIDITY_SERVICE_UUID))?.getCharacteristic(UUID.fromString(TEMP_HUMIDITY_CHARACTERISTICS_UUID)))
            enableNotification(characteristic) //Receives notification if there is a change in temp or humidity
        }


        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
//            main.gatt = gatt
//            Log.w(TAG, "Characteristic Value2 : " + main.gatt)
            with(characteristic){
                when(uuid){
                    UUID.fromString(TEMP_HUMIDITY_CHARACTERISTICS_UUID) -> {
                        // GO to user manual of device and find the specific format that this data is received
                        // Tutorial format was XX XX XX XX XX XX
                        // postive/negative, temp before comma, temp after comma, blank space, humid before comma, humid afer comma
                        val deviceName = DEVICE_NAME
//                        val multiplicator = if(value.first().toInt() > 0) -1 else 1
//                        val temperature = 10f
//                        val humidity = 10f
                        val tempHumidityResult = TempHumidityResult(deviceName,
                            ConnectionState.Connected,
                        )
                        coroutineScope.launch{
                            data.emit(
                                Resource.Success(data = tempHumidityResult)
                            )
                        }
//                        var characteristic1:BluetoothGattCharacteristic
//                        characteristic.setValue(1, android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8,0)
//                        gatt?.writeCharacteristic(characteristic)
//                        example(gatt, characteristic, 1)
//                        Log.w(TAG, "Characteristic Value4 : " + characteristic)
                    }
                    else -> Unit
                }
            }
//            example(gatt1, characteristic, 1)
//            Log.w(TAG, "Characteristic Value5 : " + characteristic.toString())
//            this@TemperatureAndHumidityBLEReceiveManager.volumeCharacteristic = characteristic
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
             characteristic?.value
//            Log.w(TAG, "Device Connected ")

            Log.w(TAG, "Characteristic Value3 : " + characteristic)
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
//            characteristic?.value
//            super.onCharacteristicWrite(gatt, characteristic, status)
//            Log.w(TAG, "Device Connected ")
//            Log.w(TAG, "Characteristic Value3 : " + characteristic)
        }
    }

//    val device = bluetoothAdapter.getRemoteDevice(bluetoothAdapter.address)
//    var gatt: BluetoothGatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
//    var gatt1: BluetoothGatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
//    var gatt: BluetoothGatt? = null
    var gatt1: BluetoothGatt? = null
    var initGatt1 = false

//    var volumeCharacteristic: BluetoothGattCharacteristic? = null
//    var gainCharacteristic: BluetoothGattCharacteristic? = null
//    var toneCharacteristic: BluetoothGattCharacteristic? = null

    fun initializeGatt1(){
        if (initGatt1 == false){
            this@TemperatureAndHumidityBLEReceiveManager.gatt1 = device.connectGatt(context, true, gattCallback, BluetoothDevice.TRANSPORT_LE)
            initGatt1 = true
        }
    }


    fun writeVolumeCharacteristic(
//        gatt: BluetoothGatt?,
        characteristic: String, //need to figure out a way to get rid of this. maybe pass a string and turn that into the characteristic, but not sure
        value: Int
    ){ //example function of how to read or write values from/to BLE device

//        val device = bluetoothAdapter.getRemoteDevice(bluetoothAdapter.address)
//        gatt1 = device.connectGatt(context, false, gattCallback)

//        var gatt1 = this@TemperatureAndHumidityBLEReceiveManager.gatt

//        var gatt1 = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)

        initializeGatt1()

        //ISSUE IS THE GATT IS NULL SO EVERYTHING IT GETS IS NULL
        var volumeCharacteristic = this@TemperatureAndHumidityBLEReceiveManager.gatt?.getService(UUID.fromString(TEMP_HUMIDITY_SERVICE_UUID))?.getCharacteristic(UUID.fromString(characteristic))
//        volumeCharacteristic = findCharacteristic(TEMP_HUMIDITY_SERVICE_UUID, characteristic)
//        this@TemperatureAndHumidityBLEReceiveManager.gatt1?.readCharacteristic(volumeCharacteristic)

        /////THE CHARACTERISTIC IS THE REAL ISSUE, THE GATT WILL SEND IF CORRECT CHARACTERISTIC IS PASSED. ABOVE IS NOT CORRECT CHARACTERISTIC

        Log.w(TAG, "Volume Value : " + this@TemperatureAndHumidityBLEReceiveManager.gatt.toString())
        volumeCharacteristic?.setValue(value, android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8,0)
        if (volumeCharacteristic != null) {
//            this@TemperatureAndHumidityBLEReceiveManager.gatt1.readCharacteristic(volumeCharacteristic)
            this@TemperatureAndHumidityBLEReceiveManager.gatt?.writeCharacteristic(
                volumeCharacteristic
            )
        }

        //Then go to gatt callback and write functions for onCharacteristicRead or onCharacteristicWrite, whatever is needed
    }

    fun writeGainCharacteristic(
        characteristic: String, //need to figure out a way to get rid of this. maybe pass a string and turn that into the characteristic, but not sure
        value: Int
    ){ //example function of how to read or write values from/to BLE device
        var gainCharacteristic = this@TemperatureAndHumidityBLEReceiveManager.gatt?.getService(UUID.fromString(TEMP_HUMIDITY_SERVICE_UUID))?.getCharacteristic(UUID.fromString(characteristic))

        Log.w(TAG, "Gain Value : " + gainCharacteristic.toString())
        gainCharacteristic?.setValue(value, android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8,0)
        if(gainCharacteristic != null) {
            this@TemperatureAndHumidityBLEReceiveManager.gatt?.writeCharacteristic(
                gainCharacteristic
            )
        }
    }

    fun writeToneCharacteristic(
        characteristic: String, //need to figure out a way to get rid of this. maybe pass a string and turn that into the characteristic, but not sure
        value: Int
    ){ //example function of how to read or write values from/to BLE device
        var toneCharacteristic = this@TemperatureAndHumidityBLEReceiveManager.gatt?.getService(UUID.fromString(TEMP_HUMIDITY_SERVICE_UUID))?.getCharacteristic(UUID.fromString(characteristic))

        Log.w(TAG, "Tone Value : " + toneCharacteristic.toString())
        toneCharacteristic?.setValue(value, android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8,0)
        if(toneCharacteristic != null) {
            this@TemperatureAndHumidityBLEReceiveManager.gatt?.writeCharacteristic(
                toneCharacteristic
            )
        }
    }

    private fun enableNotification(characteristic: BluetoothGattCharacteristic){
        val cccdUuid = UUID.fromString(CCCD_DESCRIPTOR_UUID)
        val payload = when {
            characteristic.isIndicatable() -> BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
            characteristic.isNotifiable() ->  BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            else -> return
        }
        characteristic.getDescriptor(cccdUuid)?.let { cccdDescriptor ->
            if (gatt?.setCharacteristicNotification(characteristic, true) == false){
                Log.d("BLEReceiveManager","Set characteristics notification failed")
                return
            }
            writeDescription(cccdDescriptor, payload)
        }
//        Log.w(TAG,"Device Connected")
//        Log.w(TAG, "Characteristic Value4 : " + characteristic)
    }

    private fun writeDescription(descriptor: BluetoothGattDescriptor, payload: ByteArray){
        gatt?.let{ gatt ->
            descriptor.value = payload
            gatt.writeDescriptor(descriptor)
        } ?: error("Not connected to BLE device!")
    }


    fun findCharacteristic(serviceUUID: String, characteristicsUUID: String):BluetoothGattCharacteristic?{
        return gatt?.services?.find {service ->
            service.uuid.toString() == serviceUUID
        }?.characteristics?.find { characteristics ->
            characteristics.uuid.toString() == characteristicsUUID
        }
    }

    override fun startReceiving() {
        coroutineScope.launch {
            data.emit(Resource.Loading(message = "Scanning BLE Devices..."))
        }
        isScanning = true
        blescanner.startScan(null,scanSettings,scanCallback)
    }

    override fun reconnect() {
        gatt?.connect()
    }

    override fun disconnect() {
        gatt?.disconnect()
    }

    override fun closeConnection() {
        blescanner.stopScan(scanCallback) //Stop the scan if connection closed so it doesn't drain the battery
        val characteristic = findCharacteristic(TEMP_HUMIDITY_SERVICE_UUID, TEMP_HUMIDITY_CHARACTERISTICS_UUID)
        if (characteristic != null){
            disconnectCharacteristic(characteristic)
        }
        gatt?.close()
    }

    private fun disconnectCharacteristic(characteristic: BluetoothGattCharacteristic){
        val cccdUuid = UUID.fromString(CCCD_DESCRIPTOR_UUID)
        characteristic.getDescriptor(cccdUuid)?.let{cccdDescriptor ->
            if(gatt?.setCharacteristicNotification(characteristic, false) == false){
                Log.d("TempHumidReceiveManager", "Set characteristics notification failed!")
                return
            }
            writeDescription(cccdDescriptor, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)
        }
    }
}