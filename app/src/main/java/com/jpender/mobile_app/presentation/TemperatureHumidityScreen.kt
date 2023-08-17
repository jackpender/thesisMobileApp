package com.jpender.mobile_app.presentation

import android.bluetooth.BluetoothAdapter
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.jpender.mobile_app.MainActivity
import com.jpender.mobile_app.Preset
import com.jpender.mobile_app.R
import com.jpender.mobile_app.bluetoothAdapter
import com.jpender.mobile_app.data.ConnectionState
import com.jpender.mobile_app.data.ble.TemperatureAndHumidityBLEReceiveManager
import com.jpender.mobile_app.presentation.permissions.PermissionUtils
import com.jpender.mobile_app.presentation.permissions.SystemBroadcastReceiver
import com.jpender.mobile_app.toneCharacteristic
import org.w3c.dom.Text
import kotlin.math.roundToInt


@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TemperatureHumidityScreen(
    onBluetoothStateChanged:()-> Unit,
    viewModel: TempHumidityViewModel = hiltViewModel()
) {

    SystemBroadcastReceiver(systemAction = BluetoothAdapter.ACTION_STATE_CHANGED) { bluetoothState ->
        val action = bluetoothState?.action ?: return@SystemBroadcastReceiver
        if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
            onBluetoothStateChanged()
        }
    }

    val permissionState =
        rememberMultiplePermissionsState(permissions = PermissionUtils.permissions)
    val lifecycleOwner = LocalLifecycleOwner.current
    val bleConnectionState = viewModel.connectionState
    val presetList = mutableListOf(
        Preset("Preset 1", "50", "50", "50")
    )

    DisposableEffect( //Disposable so it only triggers if lifeCycleOwner changes, also to remove the observer upon the disposable of this composable
        key1 = lifecycleOwner,
        effect = {
            val observer =
                LifecycleEventObserver { _, event -> // _ as this variable isn't important
                    if (event == Lifecycle.Event.ON_START) {
                        permissionState.launchMultiplePermissionRequest()
                        if (permissionState.allPermissionsGranted && bleConnectionState == ConnectionState.Disconnected) {
                            viewModel.reconnect()
                        }
                    }
                    if (event == Lifecycle.Event.ON_STOP) {
                        if (bleConnectionState == ConnectionState.Connected) {
                            viewModel.disconnect()
                        }
                    }
                }
            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    )

    LaunchedEffect(key1 = permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted) {
            if (bleConnectionState == ConnectionState.Uninitialized) {
                viewModel.initializeConnection()
            }
        }
    }

    val main: MainActivity = MainActivity()

    var volumeSliderPosition by remember { mutableStateOf(0f) }
    val volumeSliderValue = volumeSliderPosition.toInt()

//    var gainSliderPosition by remember { mutableStateOf(0f) }
//    val gainSliderValue = gainSliderPosition.toInt()
//
//    var toneSliderPosition by remember { mutableStateOf(0f) }
//    val toneSliderValue = toneSliderPosition.toInt()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
        modifier = Modifier,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Slider(
                modifier = Modifier,
                value = volumeSliderPosition,
                valueRange = 0f..3f,
                onValueChange = { volumeSliderPosition = it },
                onValueChangeFinished = { main.transmitVolume(volumeSliderValue) }
            )
            Text(
                text = volumeSliderValue.toString()
            )

//            Slider(
//                modifier = Modifier,
//                value = gainSliderPosition,
//                valueRange = 0f..3f,
//                onValueChange = { gainSliderPosition = it },
//                onValueChangeFinished = { main.transmitGain(gainSliderValue) }
//            )
//            Text(
//                text = gainSliderValue.toString()
//            )

//            Slider(
//                modifier = Modifier,
//                value = toneSliderPosition,
//                valueRange = 0f..3f,
//                onValueChange = { toneSliderPosition = it },
//                onValueChangeFinished = { main.transmitTone(toneSliderValue) }
//            )
//            Text(
//                text = toneSliderValue.toString()
//            )

//            Row(
//                horizontalArrangement = Arrangement.Center,
//                verticalAlignment = Alignment.CenterVertically,
//                modifier = Modifier
//                    .border(1.dp, Color.Green, RoundedCornerShape(10.dp))
//                    .padding(30.dp)
//            ){
//                var volume by remember{
//                    mutableStateOf(0f)
//                }
//                val barCount = 20f
//                RotaryKnob(
//                    modifier = Modifier.size(100.dp),
//                ){
//                    volume = it
////                    Log.w(TAG, "Tone Value From Knob: " + volume)
//                    main.transmitTone((volume * 100).toInt())
//                }
//                Spacer(modifier = Modifier.width(20.dp))
//                VolumeBar(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(30.dp),
//                    activeBars = (barCount * volume).roundToInt(),
//                    barCount = barCount.toInt()
//                )
//            }

//        main.transmitVolume(volumeSliderValue)
        }

//    main.openConnection(sliderValue)
//        Log.w(TAG, "Volume Slider Value: " + volumeSliderValue)
//        Log.w(TAG, "Gain Slider Value: " + gainSliderValue)
//        Log.w(TAG, "Tone Slider Value: " + toneSliderValue)

//    Column(
//        modifier = Modifier
//            .fillMaxWidth(),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally,
//    ){
//        Image(
//            painter = painterResource(id = R.drawable.overdrive_pedal_interface),
//            contentDescription = "Overdrive pedal",
//        )
//    }

        Column(
            modifier = Modifier,
//        contentAlignment = Alignment.Center
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
//               .aspectRatio(1f)
//               .border(
//                   BorderStroke(
//                       5.dp, Color.Blue
//                   ),
//                   RoundedCornerShape(10.dp)
//               ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (bleConnectionState == ConnectionState.CurrentlyInitializing) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        if (viewModel.initializingMessage != null) {
                            Text(
                                text = viewModel.initializingMessage!!
                            )
                        }
                    }
                } else if (!permissionState.allPermissionsGranted) {
                    Text(
                        text = "Go to the app settings and allow the missing permissions.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(10.dp),
                        textAlign = TextAlign.Center
                    )
                } else if (viewModel.errorMessage != null) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = viewModel.errorMessage!!
                        )
                        Button(
                            onClick = {
                                if (permissionState.allPermissionsGranted) {
                                    viewModel.initializeConnection()
                                }
                            }
                        ) {
                            Text("Try again!")
                        }
                    }
                } else if (bleConnectionState == ConnectionState.Connected) { //Shows up if I change the state to Disconnected for example but not when it's Connected, need to figure this out

                    //Preset Variables
                    var name = remember {
                        mutableStateOf("")
                    }
                    var volume by remember{
                        mutableStateOf(0f)
                    }
                    var gain by remember{
                        mutableStateOf(0f)
                    }
                    var tone by remember {
                        mutableStateOf(0f)
                    }


                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    )
                    {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
//                        var name = remember {
//                            mutableStateOf("")
//                        }
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                TextField(
                                    value = name.value,
                                    onValueChange = { newName -> name.value = newName },
                                    label = { Text("Enter preset name") }
                                )
                                Button(
                                    onClick = {
//                                    var preset = Preset().apply {
//                                        presetName = name.value
//                                        VolumeValue = volume.toInt().toString()
//                                        GainValue = gain.toInt().toString()
//                                        ToneValue = tone.toInt().toString()
//                                    }
//                                    viewModel.save(preset)
                                        var presetNameValue = name.value
                                        if (presetNameValue.isNotEmpty()) {
                                            viewModel.readData(presetNameValue)
                                        }
                                        volume = viewModel.volume.toFloat()
                                        gain = viewModel.gain.toFloat()
                                        tone = viewModel.tone.toFloat()

                                        main.transmitVolume((viewModel.volume))
                                        main.transmitGain((viewModel.gain))
                                        main.transmitTone((viewModel.tone))
                                    },
                                    modifier = Modifier,
                                    //                            .fillMaxWidth(0.8f)
                                    //                            .size(140.dp),
                                    //                            .offset(x = 0.dp, y = 330.dp),
                                    shape = RectangleShape,
                                    //                        colors = ButtonDefaults.buttonColors(containerColor= Color.Transparent)
                                ) {
                                    Text(
                                        text = "Get Preset",
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
//                        contentAlignment = Alignment.Center,
                        ) {
//                   Text(
//                       text = "Hello There",
//                       style = MaterialTheme.typography.bodyMedium
//                   )
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.TopCenter
                            ) {

//                            TextField(
//                                value = name.value,
//                                onValueChange = { newName -> name.value = newName},
//                                label = {Text("Enter preset name")}
//                            )


                                Image(
                                    modifier = Modifier.fillMaxSize(),
                                    painter = painterResource(id = R.drawable.overdrive_pedal_interface),
                                    contentDescription = "Overdrive pedal",
                                )
//                        Text(
//                            text = "Device name: ${viewModel.deviceName}",
//                            style = MaterialTheme.typography.headlineSmall
//                        )
//                            var volume by remember{
//                                mutableStateOf(0f)
//                            }
                                val barCount = 20f
                                RotaryKnob(
                                    modifier = Modifier
                                        .size(75.dp)
                                        .offset(x = -65.dp, y = 43.dp),
                                ) {
                                    volume = it * 100
//                    Log.w(TAG, "Tone Value From Knob: " + volume)
                                    main.transmitVolume(volume.toInt())
                                }
                                Text(
                                    text = volume.toInt().toString(),
                                    modifier = Modifier
                                        .offset(x = -110.dp, y = 43.dp),
                                )
//                            var gain by remember{
//                                mutableStateOf(0f)
//                            }
//                        val barCount = 20f
                                RotaryKnob(
                                    modifier = Modifier
                                        .size(75.dp)
                                        .offset(x = 65.dp, y = 43.dp)
                                ) {
                                    gain = it * 100
//                    Log.w(TAG, "Tone Value From Knob: " + volume)
                                    main.transmitGain(gain.toInt())

                                }
                                Text(
                                    text = gain.toInt().toString(),
                                    modifier = Modifier
                                        .offset(x = 20.dp, y = 43.dp),
                                )
//                            var tone by remember{
//                                mutableStateOf(0f)
//                            }
//                        val barCount = 20f
                                ToneKnob(
                                    modifier = Modifier
                                        .size(55.dp)
                                        .offset(x = 0.dp, y = 100.dp)
                                ) {
                                    tone = it * 100
//                    Log.w(TAG, "Tone Value From Knob: " + volume)
                                    main.transmitTone(tone.toInt())

                                }
                                Text(
                                    text = tone.toInt().toString(),
                                    modifier = Modifier
                                        .offset(x = -35.dp, y = 130.dp),
                                )

                                Button(
                                    onClick = {
//                                    presetList.add(
//                                        Preset(
//                                            "Preset " + (presetList.size + 1).toString(),
//                                            volume.toInt(),
//                                            gain.toInt(),
//                                            tone.toInt()
//                                        )
//                                    )
//                                    for (i in presetList){
//                                        Log.w("Preset: ", i.toString())
//                                    }
                                        var preset = Preset().apply {
                                            presetName = name.value
                                            VolumeValue = volume.toInt().toString()
                                            GainValue = gain.toInt().toString()
                                            ToneValue = tone.toInt().toString()
                                        }
                                        viewModel.save(preset)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth(0.8f)
                                        .size(140.dp)
                                        .offset(x = 0.dp, y = 330.dp),
                                    shape = RectangleShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                                ) {
                                    Text(
                                        text = "Save Preset",
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                }

                            }
//                   Text(
//                       text = "Humidity: ${viewModel.humidity}",
//                       style = MaterialTheme.typography.headlineSmall
//                   )
//                   Text(
//                       text = "Temperature: ${viewModel.temperature}",
//                       style = MaterialTheme.typography.headlineSmall
//                   )
                        }
                    }
                } else if (bleConnectionState == ConnectionState.Disconnected) {
                    Button(onClick = {
                        viewModel.initializeConnection()
                    }) {
                        Text("Initialize again.")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun VolumeBar(
    modifier: Modifier = Modifier,
    activeBars: Int = 0,
    barCount: Int = 10,
){
    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        val barWidth = remember{
            constraints.maxWidth / (2f * barCount)
        }
        Canvas(
            modifier = modifier
        ){
            for (i in 0 until barCount){
                drawRoundRect(
                    color = if (i in 0..activeBars) Color.Green else Color.DarkGray,
                    topLeft = Offset(i * barWidth * 2f + barWidth / 2f, 0f),
                    size = Size(barWidth, constraints.maxWidth.toFloat()),
                    cornerRadius = CornerRadius(0f)
                )
            }
        }
    }

}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RotaryKnob(
    modifier: Modifier = Modifier,
    limitingAngle: Float = 25f,
    onValueChange: (Float) -> Unit, //return the percentage that the knob has been rotated
){
    var rotation by remember{ //this throws an error unless you import SetValue and GetValue
        mutableStateOf(limitingAngle)
    }
    var touchX by remember{
        mutableStateOf(0f)
    }
    var touchY by remember{
        mutableStateOf(0f)
    }
    var centerX by remember{
        mutableStateOf(0f)
    }
    var centerY by remember{
        mutableStateOf(0f)
    }

    Image(
        painter = painterResource(id = R.drawable.volume_gain_knob),
        contentDescription = "Volume/Gain Knob",
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned {
                val windowBounds = it.boundsInWindow()
                centerX = windowBounds.size.width / 2f
                centerY = windowBounds.size.height / 2f
            }
            .pointerInteropFilter { event -> //Similar to the onTouch event from xml, both provide a MotionEvent
                touchX = event.x
                touchY = event.y
                val angle = -Math.atan2(
                    centerX.toDouble() - touchX.toDouble(),
                    centerY.toDouble() - touchY.toDouble()
                ) * (180f / Math.PI).toFloat() //add the minus in order to get the correct angle,
                // otherwise it rotates the wrong direction
                // Then we do the multiplication to
                //convert from radians to degrees
                when (event.action) {
                    MotionEvent.ACTION_DOWN,
                    MotionEvent.ACTION_MOVE -> {
                        if (angle !in -limitingAngle..limitingAngle) { //check if angle is not in the range of each limiting angle
                            val fixedAngle = if (angle in -180f..-limitingAngle) {
                                360f + angle
                            } else {
                                angle
                            }
                            rotation = fixedAngle.toFloat()

                            val percent =
                                (fixedAngle - limitingAngle) / (360f - (2 * limitingAngle))
                            onValueChange(percent.toFloat())
                            true
                        } else {
                            false
                        }
                    }

                    else -> false
                }
            }
            .rotate(rotation) //must be at the end or the touch co-ordiniates will also be rotated
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ToneKnob(
    modifier: Modifier = Modifier,
    limitingAngle: Float = 25f,
    onValueChange: (Float) -> Unit, //return the percentage that the knob has been rotated
){
    var rotation by remember{ //this throws an error unless you import SetValue and GetValue
        mutableStateOf(limitingAngle)
    }
    var touchX by remember{
        mutableStateOf(0f)
    }
    var touchY by remember{
        mutableStateOf(0f)
    }
    var centerX by remember{
        mutableStateOf(0f)
    }
    var centerY by remember{
        mutableStateOf(0f)
    }

    Image(
        painter = painterResource(id = R.drawable.tone_knob),
        contentDescription = "Volume/Gain Knob",
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned {
                val windowBounds = it.boundsInWindow()
                centerX = windowBounds.size.width / 2f
                centerY = windowBounds.size.height / 2f
            }
            .pointerInteropFilter { event -> //Similar to the onTouch event from xml, both provide a MotionEvent
                touchX = event.x
                touchY = event.y
                val angle = -Math.atan2(
                    centerX.toDouble() - touchX.toDouble(),
                    centerY.toDouble() - touchY.toDouble()
                ) * (180f / Math.PI).toFloat() //add the minus above in order to get the correct angle,
                // otherwise it rotates the wrong direction
                // Then we do the multiplication to
                //convert from radians to degrees
                when (event.action) {
                    MotionEvent.ACTION_DOWN,
                    MotionEvent.ACTION_MOVE -> {
                        if (angle !in -limitingAngle..limitingAngle) { //check if angle is not in the range of each limiting angle
                            val fixedAngle = if (angle in -180f..-limitingAngle) {
                                360f + angle
                            } else {
                                angle
                            }
                            rotation = fixedAngle.toFloat()

                            val percent =
                                (fixedAngle - limitingAngle) / (360f - (2 * limitingAngle))
                            onValueChange(percent.toFloat())
                            true
                        } else {
                            false
                        }
                    }

                    else -> false
                }
            }
            .rotate(rotation) //must be at the end or the touch co-ordiniates will also be rotated
    )
}

