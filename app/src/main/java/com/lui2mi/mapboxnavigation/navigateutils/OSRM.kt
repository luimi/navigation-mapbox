package com.lui2mi.mapboxnavigation.navigateutils

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

public interface OSRM {
    @GET("route/v1/driving/{fromLng},{fromLat};{toLng},{toLat}?overview=false&steps=true&overview=full")
    fun getRoute(@Path("fromLng") fromLng: String, @Path("fromLat") fromLat: String, @Path("toLng") toLng: String, @Path("toLat") toLat: String): Call<RouteResponse>
}