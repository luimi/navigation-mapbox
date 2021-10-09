package com.lui2mi.mapboxnavigation.navigateutils

import android.location.Location
import android.util.Log
import com.mapbox.geojson.Point
import com.mapbox.geojson.utils.PolylineUtils
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryFactory

class RouteResponse {
    val code: String = ""
    val routes: ArrayList<Route> = arrayListOf()
    var currentStep: Int = 0
    fun isCorrect(): Boolean{
        return code == "Ok"
    }
    fun getSteps(): ArrayList<Route.Leg.Step> {
        return routes[0].legs[0].steps
    }
    fun getCurrentStepPath(current: Location): List<Point> {
        val currentStep = getSteps()[currentStep]
        val currentStepPath = currentStep.path
        val pathCopy: ArrayList<Point> = currentStepPath.clone() as ArrayList<Point>
        currentStep.moveCheckPoint(current)
        val indexOnCurrentStep = currentStep.getCheckPointIndex()
        pathCopy.add(indexOnCurrentStep, Point.fromLngLat(current.longitude,current.latitude))
        if(indexOnCurrentStep > 0){
            for (i in indexOnCurrentStep-1 downTo 0 step 1){
                pathCopy.removeAt(i)
            }
        }
        return pathCopy
    }
    fun getCurrentStepAngle(current: Location, path: List<Point>): Double {
        val angle = current.bearingTo(NavigateCompanion.point2Location(path[1]))
        return angle.toDouble()
    }
    fun isContainedInCurrentStep(current: Location): Boolean{
        val currentStepPolygon = getSteps()[currentStep].polygon
        val point = GeometryFactory().createPoint(Coordinate(current.latitude, current.longitude))
        return currentStepPolygon.contains(point)
    }
    fun isOnNextStep(current: Location):Boolean {
        val cs = getSteps()[currentStep]
        if(currentStep < getSteps().size -1){
            val ns = getSteps()[currentStep+1]
            val currentStepPolygon = ns.polygon
            val point = GeometryFactory().createPoint(Coordinate(current.latitude, current.longitude))
            return currentStepPolygon.contains(point) && cs.isStepCompleted()
        }
        return false

    }
    fun nextStep(){
        val steps = getSteps()
        for (i in currentStep+1..steps.size-1){
            if(steps[i].path.size>1){
                currentStep = i
                Log.e("nextStep",": ${i}, ${steps[i].geometry}")
                break
            }
        }
    }
    fun getCurrentStepPolygon(): List<List<Point>>{
        val data: ArrayList<Point> = arrayListOf()
        val currentStepPolygon = getSteps()[currentStep].polygon
        currentStepPolygon.coordinates.forEach {
            data.add(Point.fromLngLat(it.y,it.x))
        }
        return listOf(data)
    }
    class Route {
        val legs: ArrayList<Leg> = arrayListOf()
        class Leg {
            val steps: ArrayList<Step> = arrayListOf()
            class Step {
                val name: String = ""
                val geometry: String = ""
                val maneuver: Maneuver = Maneuver()
                var path: ArrayList<Point> = arrayListOf()
                var polygon: Geometry = GeometryFactory().createPolygon()
                var checkpoints: ArrayList<CheckPoint> = arrayListOf()
                fun initialize() {
                    path = ArrayList(PolylineUtils.decode(geometry,5))
                    if(path.size>1){
                        val coordinates :ArrayList<Coordinate> = arrayListOf()
                        path.forEach {
                            coordinates.add(Coordinate(it.latitude(),it.longitude()))
                            checkpoints.add(CheckPoint(NavigateCompanion.point2Location(it)))
                        }
                        val geometry = GeometryFactory().createLineString(coordinates.toTypedArray())
                        polygon = geometry.buffer((20 * 0.0011) / 111.12)
                    }
                }
                class Maneuver {
                    val type: String = ""
                    val modifier: String = ""
                }
                class CheckPoint(var location: Location) {
                    var status: Boolean = false
                }
                fun isStepCompleted():Boolean {
                    var status = true
                    checkpoints.forEach {
                        if(!it.status) status = false
                    }
                    return status
                }
                fun getCheckPointIndex():Int {
                    for (i in 0..checkpoints.size-1) {
                        if(!checkpoints[i].status) return i
                    }
                    return checkpoints.size-1
                }
                fun moveCheckPoint(location: Location) {
                    for (i in 0..checkpoints.size-1) {
                        if(!checkpoints[i].status && checkpoints[i].location.distanceTo(location) <= 20){
                            checkpoints[i].status = true
                            break
                        }
                    }
                }
            }
        }
    }
}