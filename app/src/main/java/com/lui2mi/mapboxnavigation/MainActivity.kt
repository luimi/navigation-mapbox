package com.lui2mi.mapboxnavigation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions

import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import android.widget.Toast

import com.mapbox.mapboxsdk.geometry.LatLng

import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import com.mapbox.mapboxsdk.maps.MapboxMap.OnMapClickListener


class MainActivity : AppCompatActivity() {
    private var mapview: MapView? = null
    private var mapboxMap: MapboxMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_main)
        mapview = findViewById(R.id.mv_navigation)
        askPermissions()
        mapview?.getMapAsync {
            mapboxMap = it
            mapboxMap?.setStyle(Style.MAPBOX_STREETS) {
                val locationComponent = mapboxMap!!.locationComponent
                val customLocationComponentOptions = LocationComponentOptions.builder(this)
                    .pulseEnabled(true)
                    .build()
                locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this, it)
                        .locationComponentOptions(customLocationComponentOptions)
                        .build()
                )
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    askPermissions()
                    return@setStyle
                }
                locationComponent.setLocationComponentEnabled(true)
                locationComponent.setCameraMode(CameraMode.TRACKING)
            }
            mapboxMap!!.addOnMapClickListener { point ->
                val intent = Intent(this,Navigate::class.java)
                intent.putExtra("lat",point.latitude.toString())
                intent.putExtra("lng",point.longitude.toString())
                startActivity(intent)
                true
            }
        }
        /**/
    }

    fun askPermissions(){
        requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION),0)
    }
}