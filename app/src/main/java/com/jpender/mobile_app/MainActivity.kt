package com.jpender.mobile_app

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jpender.mobile_app.data.ble.TemperatureAndHumidityBLEReceiveManager
import com.jpender.mobile_app.presentation.Screen
import com.jpender.mobile_app.presentation.TemperatureHumidityScreen
import com.jpender.mobile_app.ui.theme.Pedal_connectTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


//class MainActivity: ComponentActivity() {
//    @OptIn(ExperimentalMaterial3Api::class)
//    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            Pedal_connectTheme {
////                // A surface container using the 'background' color from the theme
////                Surface(
////                    modifier = Modifier.fillMaxSize(),
////                    color = MaterialTheme.colorScheme.background
////                ) {
////                    Greeting("Android")
////                }
//                val navController = rememberNavController()
//                Scaffold(
//                    bottomBar = {
//                        BottomNavigationBar(items = listOf(
//                            BottomNavItem(
//                                name = "Home",
//                                route = "home",
//                                icon = Icons.Default.Home
//                            ),
//                            BottomNavItem(
//                                name = "Profile",
//                                route = "profile",
//                                icon = Icons.Default.Face
//                            ),
//                            BottomNavItem(
//                                name = "Settings",
//                                route = "settings",
//                                icon = Icons.Default.Settings
//                            )
//                        ),
//                        navController = navController,
//                        onItemClick = {
//                            navController.navigate(it.route) //it refers to the currently selected item
//                        })
//                    }
//                ) {
//                    Nav(navController = navController)
//                }
//            }
//        }
//
////        setContentView(R.layout.activity_main)
////
////        val viewModel: TempHumidityViewModel by viewModels()
////
////
////        val leftIcon = findViewById<ImageView>(R.id.left_icon) as ImageView
////        val rightIcon = findViewById<ImageView>(R.id.right_icon) as ImageView
////        val title = findViewById<TextView>(R.id.toolbar_title) as TextView
////
////        leftIcon.setOnClickListener(){
////            Toast.makeText(this@MainActivity,"Back Button", Toast.LENGTH_SHORT).show()
////        }
////        rightIcon.setOnClickListener{
////            Toast.makeText(this@MainActivity,"Menu Button", Toast.LENGTH_SHORT).show()
////            openBtMenu()
////        }
////
////        title.setOnClickListener(){
////            Toast.makeText(this@MainActivity,"Pedal Connect :)", Toast.LENGTH_SHORT).show()
////        }
//
//
//    }
//
//    fun openBtMenu(){
//        val intent = Intent(this@MainActivity, BtMenuActivity::class.java)
//        startActivity(intent)
//    }
//
//}


@AndroidEntryPoint
class MainActivity : ComponentActivity(){

    @Inject
    lateinit var bluetoothAdapter: BluetoothAdapter

    var gatt: BluetoothGatt? = null

    var presetList = arrayListOf<Preset>()


    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_bt_menu)
        setContent {
            Pedal_connectTheme {
                // A surface container using the 'background' color from the theme
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    Greeting("Android")
//                }

                val navController = rememberNavController()
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                             title = {
                                 Text(text = "Preset")
                             },
                            navigationIcon = {
                                IconButton(onClick = {
                                }){
//                                    Icon(
//                                        imageVector = , contentDescription =
//                                    )
                                }
                                Text(
                                    text = "Connect",
                                )
                            },
                            actions = {
                                IconButton(onClick = { /*TODO*/}){
//                                    Icon(
//                                        imageVector = ,
//                                        contentDescription =
//                                    )
                                    Text(text = "Get")
                                }
                                IconButton(onClick = { /*TODO*/}){
//                                    Icon(
//                                        imageVector = ,
//                                        contentDescription =
//                                    )
                                    Text(text = "Save")
                                }
                            }
                        )
                    },
                    bottomBar = {
                        BottomNavigationBar(items = listOf(
                            BottomNavItem(
                                name = "Home",
                                route = "home",
                                icon = Icons.Default.Home
                            ),
                            BottomNavItem(
                                name = "Profile",
                                route = "profile",
                                icon = Icons.Default.Face
                            ),
                            BottomNavItem(
                                name = "Settings",
                                route = "settings",
                                icon = Icons.Default.Settings
                            )
                        ),
                            navController = navController,
                            onItemClick = {
                                navController.navigate(it.route) //it refers to the currently selected item
                            })
                    }
                ) {
                    Nav(navController = navController)
//                    Navigation(
//                        onBluetoothStateChanged = {
//                            showBluetoothDialogue()
//                        }
//                    )
                }
            }
        }
    }



    override fun onStart() {
        super.onStart()
        showBluetoothDialogue()
    }

    private var isBluetoothDialogueAlreadyShown = false

    fun showBluetoothDialogue(){
        if(bluetoothAdapter.isEnabled == false){
            if (isBluetoothDialogueAlreadyShown) {
                val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
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
                Toast.makeText(this@MainActivity,"Bluetooth Permission Also Granted", Toast.LENGTH_SHORT).show()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.BLUETOOTH
            ) -> {
                requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH)
                Toast.makeText(this@MainActivity,"Bluetooth Permission Also Not Granted", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this@MainActivity,"result code: " + result.resultCode.toString(), Toast.LENGTH_SHORT).show()
                showBluetoothDialogue()
            }
        }
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){
                isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this@MainActivity,"Bluetooth Permission Granted", Toast.LENGTH_SHORT).show()
//                requestBluetoothPermission()

            }
            else {
//                showBluetoothDialogue()
//                Toast.makeText(this@BtMenuActivity,"Bluetooth Permission Not Granted", Toast.LENGTH_SHORT).show()
                requestBluetoothPermission()
            }
        }



    fun transmitVolume(value:Int){
//        gatt = tahbrm.gatt
        tahbrm.writeVolumeCharacteristic(volumeCharacteristic, value)
        Log.w(ContentValues.TAG, "Characteristic Value: " + volumeCharacteristic)
    }

    fun transmitGain(value:Int){
//        gatt = tahbrm.gatt
        tahbrm.writeGainCharacteristic(gainCharacteristic, value)
        Log.w(ContentValues.TAG, "Characteristic Value: " + gainCharacteristic)
    }

    fun transmitTone(value:Int){
//        gatt = tahbrm.gatt
        tahbrm.writeToneCharacteristic(toneCharacteristic, value)
        Log.w(ContentValues.TAG, "Characteristic Value: " + toneCharacteristic)
    }

    fun savePreset(name: String, volume: Int, gain: Int, tone: Int){

    }
}

val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
val main: MainActivity = MainActivity()
val tahbrm: TemperatureAndHumidityBLEReceiveManager = TemperatureAndHumidityBLEReceiveManager(
    bluetoothAdapter = bluetoothAdapter,
    context = main
)
//val characteristic: BluetoothGattCharacteristic = BluetoothGattCharacteristic(
//    UUID.fromString("00002a57-0000-1000-8000-00805f9b34fb"),
//    BluetoothGattCharacteristic.FORMAT_UINT8,
//    BluetoothGattCharacteristic.PERMISSION_WRITE
//)
val volumeCharacteristic  = "00002a57-0000-1000-8000-00805f9b34fb"
val gainCharacteristic = "00002a58-0000-1000-8000-00805f9b34fb"
val toneCharacteristic = "00002a59-0000-1000-8000-00805f9b34fb"
//val gatt = tahbrm.gatt


@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Composable
fun Nav (navController: NavHostController){
    NavHost(navController = navController, startDestination = "home"){
        composable("home"){
//            StartScreen(navController = navController)
            HomeScreen(onBluetoothStateChanged = { main.showBluetoothDialogue() })
        }
        composable("profile"){
            ProfileScreen()
        }
        composable("settings"){
            SettingsScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigationBar(
    items: List<BottomNavItem>,
    navController: NavController,
    modifier: Modifier = Modifier,
    onItemClick: (BottomNavItem) -> Unit
){
    val backStackEntry = navController.currentBackStackEntryAsState()
    NavigationBar(
        modifier = Modifier,
        containerColor = Color.DarkGray,
        tonalElevation = 5.dp
    ) {
        items.forEach {item ->
            val selected = item.route == backStackEntry.value?.destination?.route
            NavigationBarItem(
                selected = selected,
                onClick = { onItemClick(item) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Green,
                    unselectedIconColor = Color.Gray
                ),
                icon = {
                    Column(
                        horizontalAlignment = CenterHorizontally
                    ){
                        if (item.badgeCount > 0){
                            BadgedBox(
                                badge = {
                                    Badge { Text(item.badgeCount.toString()) }
                                }
                            ){
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.name
                                )
                            }
                        }
                        else{
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.name
                            )
                        }
                        if (selected){
                            Text(
                                text = item.name,
                                textAlign = TextAlign.Center,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            )
        }

    }
}


@Composable
fun HomeScreen(
    onBluetoothStateChanged:() -> Unit
){
//    Box(
//        modifier = Modifier.fillMaxSize(),
//        contentAlignment = Alignment.Center
//    ){
//        Text(
//            text = "Home Screen"
//        )
//    }

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.TemperatureHumidityScreen.route){
//        composable(Screen.StartScreen.route){//Need to look into what a composable is, as well as this route function
//            StartScreen(navController = navController )
//        }

        composable(Screen.TemperatureHumidityScreen.route){
            TemperatureHumidityScreen(
                onBluetoothStateChanged
            )
        }
    }
}

@Composable
fun ProfileScreen(){
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Text(
            text = "Profile Screen"
        )
    }
}

@Composable
fun SettingsScreen(){
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Text(
            text = "Settings Screen"
        )
    }
}


//@Composable
//fun volumeBar(
//    modifier: Modifier = Modifier,
//    activeBars: Int = 0,
//    barCount: Int = 10,
//){
//    BoxWithConstraints(
//        contentAlignment = Alignment.Center,
//        modifier = modifier
//    ) {
//        val barWidth = remember{
//            constraints.maxWidth / (2f * barCount)
//        }
//        Canvas(
//            modifier = modifier
//        ){
//            for (i in 0 until barCount){
//                drawRoundRect(
//                    color = if (i in 0..activeBars) Color.Green else Color.DarkGray,
//                    topLeft = Offset(i * barWidth * 2f + barWidth / 2f, 0f),
//                    size = Size(barWidth, constraints.maxWidth.toFloat()),
//                    cornerRadius = CornerRadius(0f)
//                )
//            }
//        }
//    }
//
//}
//
//@OptIn(ExperimentalComposeUiApi::class)
//@Composable
//fun RotaryKnob(
//    modifier: Modifier = Modifier,
//    limitingAngle: Float = 25f,
//    onValueChange: (Float) -> Unit, //return the percentage that the knob has been rotated
//){
//    var rotation by remember{ //this throws an error unless you import SetValue and GetValue
//        mutableStateOf(limitingAngle)
//    }
//    var touchX by remember{
//        mutableStateOf(0f)
//    }
//    var touchY by remember{
//        mutableStateOf(0f)
//    }
//    var centerX by remember{
//        mutableStateOf(0f)
//    }
//    var centerY by remember{
//        mutableStateOf(0f)
//    }
//
//    Image(
//        painter = painterResource(id = R.drawable.volume_gain_knob),
//        contentDescription = "Volume/Gain Knob",
//        modifier = modifier
//            .fillMaxSize()
//            .onGloballyPositioned {
//                val windowBounds = it.boundsInWindow()
//                centerX = windowBounds.size.width / 2f
//                centerY = windowBounds.size.height / 2f
//            }
//            .pointerInteropFilter { event -> //Similar to the onTouch event from xml, both provide a MotionEvent
//                touchX = event.x
//                touchY = event.y
//                val angle = -atan2(
//                    centerX.toDouble() - touchX.toDouble(),
//                    centerY.toDouble() - touchY.toDouble()
//                ) * (180f / PI).toFloat() //add the minus in order to get the correct angle,
//                // otherwise it rotates the wrong direction
//                // Then we do the multiplication to
//                //convert from radians to degrees
//                when (event.action) {
//                    MotionEvent.ACTION_DOWN,
//                    MotionEvent.ACTION_MOVE -> {
//                        if (angle !in -limitingAngle..limitingAngle) { //check if angle is not in the range of each limiting angle
//                            val fixedAngle = if (angle in -180f..-limitingAngle) {
//                                360f + angle
//                            } else {
//                                angle
//                            }
//                            rotation = fixedAngle.toFloat()
//
//                            val percent =
//                                (fixedAngle - limitingAngle) / (360f - (2 * limitingAngle))
//                            onValueChange(percent.toFloat())
//                            true
//                        } else {
//                            false
//                        }
//                    }
//
//                    else -> false
//                }
//            }
//            .rotate(rotation) //must be at the end or the touch co-ordiniates will also be rotated
//    )
//}

