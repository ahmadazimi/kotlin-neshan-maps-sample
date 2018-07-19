package org.neshan.sample.kotlin.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Build
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.util.Log
import android.view.*
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*
import org.neshan.apksample.activity.utils.FontUtils
import org.neshan.core.Bounds
import org.neshan.core.LngLat
import org.neshan.core.LngLatVector
import org.neshan.core.Range
import org.neshan.geometry.LineGeom
import org.neshan.graphics.ARGB
import org.neshan.layers.VectorElementLayer
import org.neshan.sample.kotlin.R
import org.neshan.services.NeshanMapStyle
import org.neshan.services.NeshanServices
import org.neshan.styles.*
import org.neshan.ui.ClickData
import org.neshan.ui.MapEventListener
import org.neshan.utils.BitmapUtils
import org.neshan.vectorelements.Line
import org.neshan.vectorelements.Marker

/**
 * Created by alizeyn July 2018.
 */


const val REQUEST_PERMISSIONS_REQUEST_CODE = 1000
const val BASE_MAP_INDEX = 0
val TAG = MainActivity::class.java.simpleName.toString()

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    enum class Mode {
        NORMAL, LONG_PRESS
    }

    private var mode = Mode.NORMAL
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var markerLayer: VectorElementLayer
    private lateinit var lineLayer: VectorElementLayer
    private var marker: Marker? = null
    private lateinit var vNavHeader: View

    override fun init() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        initMap()
    }

    private fun initMap() {
        markerLayer = NeshanServices.createVectorElementLayer()
        lineLayer = NeshanServices.createVectorElementLayer()
        mapView.layers.add(markerLayer)
        mapView.layers.add(lineLayer)
        //set map focus position
        mapView.setFocalPointPosition(LngLat(53.529929, 35.164676), 0f)
        mapView.setZoom(4.5f, 0f)
        mapView.options.setZoomRange(Range(4.5f, 18f))

        /** layers.insert
        when you insert a layer in index i, index (i - 1) should exist
        keep base map layer at index 0
        ********
            layers.add
         suppose map has k layers right now, new layer adds in index (k + 1)
         */
        mapView.layers.insert(BASE_MAP_INDEX, NeshanServices.createBaseMap(NeshanMapStyle.STANDARD_DAY))
        setMapBounds()
    }

    override fun loadUi() {
        vNavHeader = vNavigation.getHeaderView(0)
        FontUtils.setViewsFont(this)
        FontUtils.setViewsFont(this, vNavHeader as ViewGroup)
        for (i in 1..vNavigation.menu.size()) {
            val item = vNavigation.menu.getItem(i - 1)
            FontUtils.applyFontToMenuItem(this, item)
        }
    }

    override fun listener() {
        vMenu.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START))
                drawerLayout.closeDrawers()
            else
                drawerLayout.openDrawer(GravityCompat.START)
        }

        vCurrentLocation.setOnClickListener { getLastLocation() }

        vNavigation.setNavigationItemSelectedListener(this)

        mapView.mapEventListener = object : MapEventListener() {
            override fun onMapClicked(mapClickInfo: ClickData?) {
                if (mode != Mode.LONG_PRESS) {
                    return
                }
                mode = Mode.LONG_PRESS
                mapClickInfo?.let {
                    it.clickPos.focus()
                    setMarker(it.clickPos)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        getLocationPermission(true)
    }


    private fun LngLat.focus() {
        mapView.setZoom(17f, 0.3f)
        mapView.setFocalPointPosition(
                this,
                0.6f)
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        mode = Mode.NORMAL

        when (menuItem.itemId) {

            R.id.drawLine -> {
                val lineGeom = drawLineGeom()
                mapView.setFocalPointPosition(
                        lineGeom.centerPos,
                        0.3f)
                mapView.setZoom(17f, 0.3f)
            }

            R.id.chooseLocation -> {
                lineLayer.clear()
                mode = Mode.LONG_PRESS
            }

            R.id.mapTilt -> {
                mapView.setTilt(30f, 0.4f)
            }

            R.id.northLock -> {
                mapView.setBearing(0f, 0.3f)
            }

            R.id.themeNeshan -> {
                mapView.layers.remove(mapView.layers.get(0))
                mapView.layers.insert(0, NeshanServices.createBaseMap(NeshanMapStyle.NESHAN))
            }

            R.id.themeDayStandard -> {
                mapView.layers.remove(mapView.layers.get(0))
                mapView.layers.insert(0, NeshanServices.createBaseMap(NeshanMapStyle.STANDARD_DAY))
            }

            R.id.themeNightStandard -> {
                mapView.layers.remove(mapView.layers.get(0))
                mapView.layers.insert(0, NeshanServices.createBaseMap(NeshanMapStyle.STANDARD_NIGHT))
            }

            R.id.info -> {
                intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://developer.neshan.org/"))
                startActivity(intent)
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun getLocationPermission(openPermissionDialog: Boolean): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (openPermissionDialog) {
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                            REQUEST_PERMISSIONS_REQUEST_CODE)
                }
                return false
            }
        }
        return true
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationClient.lastLocation
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful && task.result != null) {
                        onLocationChange(task.result)
                        Log.i(TAG, "lat ${task.result.latitude} lng ${task.result.longitude}")
                    } else {
                        Toast.makeText(this, "موقعیت یافت نشد.", Toast.LENGTH_SHORT).show()
                    }
                }
    }

    private fun onLocationChange(location: Location) {
        val loc = LngLat(location.longitude, location.latitude)
        loc.focus()
        setMarker(loc)
    }

    private fun setMarker(markerLocation: LngLat) {
        markerLayer.clear()

        val animSt = AnimationStyleBuilder()
                .let {
                    it.fadeAnimationType = AnimationType.ANIMATION_TYPE_SMOOTHSTEP
                    it.sizeAnimationType = AnimationType.ANIMATION_TYPE_SPRING
                    it.phaseInDuration = 0.5f
                    it.phaseOutDuration = 0.5f
                    it.buildStyle()
                }

        val st = MarkerStyleCreator()
                .let {
                    it.size = 48f
                    it.bitmap = BitmapUtils.createBitmapFromAndroidBitmap(
                            BitmapFactory.decodeResource(resources, R.drawable.ic_marker))
                    it.animationStyle = animSt
                    it.buildStyle()
                }

        marker = Marker(markerLocation, st)
        markerLayer.add(marker)
    }

    private fun setMapBounds() {
        val bounds = Bounds(LngLat(43.505859, 24.647017),
                LngLat(63.984375, 40.178873))
        mapView.options.setPanBounds(bounds)
    }

    private fun drawLineGeom(): LineGeom {
        val lngLatVector = LngLatVector()
        lngLatVector.add(LngLat(59.540182, 36.314163))
        lngLatVector.add(LngLat(59.539290, 36.310654))
        val lineGeom = LineGeom(lngLatVector)
        val line = Line(lineGeom, getLineStyle())
        lineLayer.clear()
        lineLayer.add(line)
        return lineGeom
    }

    private fun getLineStyle(): LineStyle {
        val builder = LineStyleCreator()
        builder.color = ARGB(2, 119, 189, 190)
        builder.width = 12f
        builder.stretchFactor = 0f
        return builder.buildStyle()
    }

}
