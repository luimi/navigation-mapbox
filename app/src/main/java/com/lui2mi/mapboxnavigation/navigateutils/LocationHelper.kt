
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat

class LocationHelper(val context:Context): LocationListener {
    private val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val minDistance:Float = 0f
    private val minTime: Long = 0
    private lateinit var locationManager: LocationManager
    private lateinit var callback: (Location) -> Unit
    private var stopOnFirstLocation = true

    fun getCurrentLocation(result: (Location) -> Unit){
        callback = result
        stopOnFirstLocation = true
        if (ActivityCompat.checkSelfPermission(context,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("LocationHelper","permission")
            //TODO agregar mensaje cuando no tenga permisos
            return
        }
        val isGPSProvider = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkProvider = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if(!isGPSProvider && !isNetworkProvider){
            Log.e("LocationHelper","providers not accepted")
            //TODO agregar mensaje que no esta habilitado ninguno de los 2
            return
        }

        val lastKnownGPS = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val lastKnownNetwork = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        if(validateLocation(lastKnownGPS)){
            result(lastKnownGPS!!)
            return
        }
        if(validateLocation(lastKnownNetwork)){
            result(lastKnownNetwork!!)
            return
        }
        if(isGPSProvider){
            forceLocation(LocationManager.GPS_PROVIDER)
        }
        if(isNetworkProvider){
            forceLocation(LocationManager.NETWORK_PROVIDER)
        }

    }

    fun startLocationUpdates(result: (Location) -> Unit){
        callback = result
        stopOnFirstLocation = false
        val isGPSProvider = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkProvider = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if(isGPSProvider){
            forceLocation(LocationManager.GPS_PROVIDER)
        }
        if(isNetworkProvider){
            forceLocation(LocationManager.NETWORK_PROVIDER)
        }
    }
    fun stopLocationUpdates(){
        lm.removeUpdates(this@LocationHelper)
    }
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

    }

    override fun onProviderEnabled(provider: String) {

    }

    override fun onProviderDisabled(provider: String) {

    }

    private fun validateLocation(location: Location?):Boolean {
        if(location!=null){
            return true
        } else {
            return false
        }
    }
    @SuppressLint("MissingPermission")
    private fun forceLocation(provider: String){
        lm.requestLocationUpdates(provider,minTime,minDistance,this)
    }

    override fun onLocationChanged(p0: Location) {
        if(this::callback.isInitialized && validateLocation(p0)){
            callback(p0)
        }
        if(stopOnFirstLocation){
            lm.removeUpdates(this@LocationHelper)
        }
    }
}