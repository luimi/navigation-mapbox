package com.lui2mi.mapboxnavigation


import android.graphics.BitmapFactory
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.lui2mi.mapboxnavigation.navigateutils.NavigateCompanion
import com.lui2mi.mapboxnavigation.navigateutils.OSRM
import com.lui2mi.mapboxnavigation.navigateutils.RouteResponse
import com.mapbox.geojson.utils.PolylineUtils
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.create
import com.mapbox.mapboxsdk.style.layers.PropertyFactory

import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.style.layers.LineLayer
import android.graphics.Color

import LocationHelper
import android.content.Intent
import com.mapbox.geojson.*
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor


class Navigate : AppCompatActivity(){
    private var mapview: MapView? = null
    private var route: RouteResponse? = null
    private var mapboxMap: MapboxMap? = null
    private lateinit var locationHelper: LocationHelper
    private lateinit var style: Style
    private lateinit var car: GeoJsonSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_navigate)
        mapview = findViewById(R.id.mv_navigation)
        locationHelper = LocationHelper(this)
        mapview?.getMapAsync {
            mapboxMap = it
            mapboxMap?.setStyle(Style.MAPBOX_STREETS) {
                style = it
                it.addImage(("marker_icon"), BitmapFactory.decodeResource(getResources(), R.drawable.car))
                locationHelper.getCurrentLocation {
                    getRoute(it)
                    drawCar(it)
                    moveCamera(it,0.0)
                }
            }
        }
    }
    fun getRoute(current: Location) {
        val toLng = intent.getStringExtra("lng")
        val toLat = intent.getStringExtra("lat")
        NavigateCompanion.getClient().create<OSRM>().getRoute(current.longitude.toString(), current.latitude.toString(),toLng!!,toLat!!).enqueue(object : Callback<RouteResponse>{
            override fun onResponse(call: Call<RouteResponse>, response: Response<RouteResponse>) {
                if(response.isSuccessful && response.body()!!.isCorrect()){
                    route = response.body()
                    route!!.getSteps().forEachIndexed { index, step ->
                        step.initialize()
                        drawLine(step.path,"step-${index}")
                        //drawPolyline()
                    }

                    updateLocation()
                } else {
                    //TODO No se puede recorrer
                    this@Navigate.finish()
                }
            }

            override fun onFailure(call: Call<RouteResponse>, t: Throwable) {
                Log.e("onFailure", call.toString())
            }
        })
    }

    fun updateLocation(){
        locationHelper.startLocationUpdates { current ->
            if(route!!.isContainedInCurrentStep(current)){
                removeCurrentStepPath()
                if(route!!.isOnNextStep(current)){
                    route!!.nextStep()
                    removeCurrentStepPath()
                    //drawPolyline()
                }
                val id = "step-${route!!.currentStep}"
                var path = route!!.getCurrentStepPath(current)
                drawLine(path,id)
                val angle = route!!.getCurrentStepAngle(current,path)
                moveCamera(current, angle)
                //https://docs.mapbox.com/android/maps/examples/animate-marker-position/
                car.setGeoJson(NavigateCompanion.location2Point(current))
            } else {
                clearLines()
                locationHelper.stopLocationUpdates()
                getRoute(current)
            }
        }
    }
    fun manageStep(current: Location){

    }
    fun moveCamera(location: Location, angle: Double){
        val position = CameraPosition.Builder()
            .target(LatLng(location.latitude, location.longitude))
            .zoom(17.0)
            .tilt(60.0) //0 -> 60
            .bearing(angle)
            .build()
        mapboxMap?.animateCamera(CameraUpdateFactory.newCameraPosition(position), 300)
    }

    fun drawLine(path: List<Point>, id: String){
        style.removeSource(id)
        style.removeLayer(id)
        style.addSource(
            GeoJsonSource(
                id,
                FeatureCollection.fromFeatures(
                    arrayOf(
                        Feature.fromGeometry(
                            LineString.fromLngLats(path)
                        )
                    )
                )
            )
        )
        style.addLayer(
            LineLayer(id, id).withProperties(
                PropertyFactory.lineWidth(10f),
                PropertyFactory.lineColor(Color.parseColor("#FF0000"))
            )
        )
    }
    fun clearLines(){
        val steps = route!!.getSteps().size
        for(i in 0..steps - 1){
            val id = "step-${i}"
            style.removeSource(id)
            style.removeLayer(id)
        }
    }
    fun drawCar(location: Location){
        car = GeoJsonSource("car",
            Feature.fromGeometry(Point.fromLngLat(location.longitude,location.latitude)))
        style.addSource(car)
        style.addLayer(
            SymbolLayer("car", "car")
                .withProperties(
                    PropertyFactory.iconImage("marker_icon"),
                    PropertyFactory.iconIgnorePlacement(true),
                    PropertyFactory.iconAllowOverlap(true)
                )
        )
    }
    fun removeCurrentStepPath(){
        var id = "step-${route!!.currentStep}"
        style.removeLayer(id)
        style.removeSource(id)
    }
    // DrawPolyline
    fun drawPolyline(){
        val polygon = route!!.getCurrentStepPolygon()
        style.removeLayer("polygon")
        style.removeSource("polygon")
        style.addSource(
            GeoJsonSource(
                "polygon",
                Polygon.fromLngLats(polygon)
            )
        )
        style.addLayerBelow(
            FillLayer("polygon", "polygon").withProperties(
                fillColor(Color.parseColor("#3bb2d0"))
            ), "settlement-label"
        )
    }
}