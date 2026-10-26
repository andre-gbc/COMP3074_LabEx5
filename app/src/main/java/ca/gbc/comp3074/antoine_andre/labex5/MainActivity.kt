package ca.gbc.comp3074.antoine_andre.labex5

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ca.gbc.comp3074.antoine_andre.labex5.ui.theme.LabEx5Theme
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LabEx5Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LocationScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@SuppressLint("MissingPermission") // check the permission before every call
@Composable
fun LocationScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val locationManager = remember {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    var latitude by remember { mutableStateOf("--") }
    var longitude by remember { mutableStateOf("--") }
    var status by remember { mutableStateOf("Press the button to get your location.") }

    val listener = remember {
        LocationListener { location: Location ->
            latitude = location.latitude.toString()
            longitude = location.longitude.toString()
            status = "Location updated."
        }
    }

    // start listening to the GPS (only called once permission is granted).
    fun startGps() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            status = "GPS is turned off. Please enable Location in settings."
            return
        }
        status = "Waiting for GPS fix..."
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            2000L, // minimum time between updates
            0f,    // minimum distance between updates
            listener
        )
    }

    // pop up that asks the user for permission then reports the answer
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startGps() else status = "Permission denied. Cannot read location."
    }

    // stop GPS updates when the screen goes away
    DisposableEffect(Unit) {
        onDispose { locationManager.removeUpdates(listener) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("My Location", style = MaterialTheme.typography.headlineMedium)
        Text("Latitude: $latitude", style = MaterialTheme.typography.bodyLarge)
        Text("Longitude: $longitude", style = MaterialTheme.typography.bodyLarge)
        Text(status)

        Button(onClick = {
            val hasPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) startGps()
            else permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }) {
            Text("Get Location")
        }
    }
}