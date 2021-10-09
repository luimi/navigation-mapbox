package com.lui2mi.mapboxnavigation.navigateutils


import android.location.Location
import com.mapbox.geojson.Point
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NavigateCompanion {
    companion object {
        val url = "https://router.project-osrm.org/"
        fun getClient(): Retrofit{
            return Retrofit
                .Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        fun point2Location(point: Point): Location {
            val location = Location("path")
            location.latitude = point.latitude()
            location.longitude = point.longitude()
            return location
        }
        fun location2Point(location: Location): Point {
            return Point.fromLngLat(location.longitude, location.latitude)
        }
    }
}