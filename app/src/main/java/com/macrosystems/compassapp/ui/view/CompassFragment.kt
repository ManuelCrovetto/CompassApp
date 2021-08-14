package com.macrosystems.compassapp.ui.view

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place

import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.macrosystems.compassapp.R
import com.macrosystems.compassapp.data.model.Constants.Companion.GOOGLE_PLACES_REQUEST_CODE
import com.macrosystems.compassapp.data.model.Constants.Companion.REQUEST_LOCATION_CODE
import com.macrosystems.compassapp.databinding.CompassFragmentBinding
import com.macrosystems.compassapp.ui.core.interfaces.FragmentListener
import com.macrosystems.compassapp.ui.view.dialogs.ErrorDialog
import com.macrosystems.compassapp.ui.viewmodel.CompassFragmentViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs
import kotlin.math.roundToInt

@AndroidEntryPoint
class CompassFragment: Fragment(), FragmentListener, View.OnClickListener, SensorEventListener {

    private val viewModel: CompassFragmentViewModel by viewModels()

    private var _binding: CompassFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var googlePlacesSelectedLocationLatLng: LatLng
    private var animationLogicCounter = 0

    //Sensor Global Variables
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private var haveSensorAccelerometer = false
    private var haveSensorMagnetometer = false
    private var rotationVector: Sensor? = null
    private var haveSensorRotationVector = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = CompassFragmentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        sensorManager = context?.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        enableLocation()
        with(viewModel){
            listener = this@CompassFragment

            viewModel.initializeGooglePlaces()

            isLocationOnline.observe(viewLifecycleOwner, {
                if (it){
                    binding.pbProgressBar.isGone = true
                    binding.btnSetNewDestination.isVisible = true
                } else {
                    binding.pbProgressBar.isVisible = true
                    binding.btnSetNewDestination.isGone = true
                }
            })

            location.observe(viewLifecycleOwner, {
                if (::googlePlacesSelectedLocationLatLng.isInitialized) {
                    if (animationLogicCounter == 0){
                            YoYo.with(Techniques.SlideInDown).duration(1000).playOn(binding.tvDistanceFromDestination)
                            binding.tvDistanceFromDestination.isVisible = true
                            calculateDistance(googlePlacesSelectedLocationLatLng, false)
                            binding.tvDistanceFromDestination.text = context?.getString(R.string.distance_from_destination, distance.value.toString())
                            animationLogicCounter++
                    } else {
                            binding.tvDistanceFromDestination.isVisible = true
                            calculateDistance(googlePlacesSelectedLocationLatLng, false)
                            binding.tvDistanceFromDestination.text = context?.getString(R.string.distance_from_destination, distance.value.toString())
                    }

                } else {
                        YoYo.with(Techniques.FadeOut).duration(500).playOn(binding.tvDistanceFromDestination)
                        binding.tvDistanceFromDestination.isGone = true
                }
            })

        }

        startCompass()

        binding.btnSetNewDestination.setOnClickListener(this)
    }


    //Permission Logics
    private fun isLocationPermissionGranted() = ContextCompat.checkSelfPermission((requireActivity()), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun enableLocation() {
        if (isLocationPermissionGranted()){
            viewModel.startLocationUpdates()
        } else {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)){
            Toast.makeText(context, "Please go to settings and enable location permissions.", Toast.LENGTH_LONG).show()
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_LOCATION_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode){
            REQUEST_LOCATION_CODE->{
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    viewModel.startLocationUpdates()
                } else {
                    Toast.makeText(context, "To activate your location, please go to settings and enable location permissions.", Toast.LENGTH_LONG).show()
                }
            }
            else ->{
                Toast.makeText(
                    context, "An error has occurred while requesting location permissions, please try again.", Toast.LENGTH_LONG).show()
            }
        }
    }
    //

    //Lifecycle Logics
    override fun onResume() {
        super.onResume()
        viewModel.startLocationUpdates()
        startCompass()
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopLocationUpdates()
        stopCompass()
    }
    //

    //Fragment listener
    override fun onStarted() {
        binding.pbProgressBar.isVisible = true
        binding.btnSetNewDestination.isGone = true
    }

    override fun onSuccess(string: String?) {
        binding.pbProgressBar.isGone = true
        binding.btnSetNewDestination.isVisible = true
        if (!string.isNullOrEmpty())
            Toast.makeText(context, string, Toast.LENGTH_SHORT).show()
    }

    override fun onFailure(string: String?) {
        binding.pbProgressBar.isGone = true
        binding.btnSetNewDestination.isVisible = true
        Toast.makeText(context, string, Toast.LENGTH_SHORT).show()
    }
    //

    override fun onClick(v: View?) {
        when(v){
            binding.btnSetNewDestination->{
                val fields = listOf(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME)
                val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(requireActivity())
                startActivityForResult(intent, GOOGLE_PLACES_REQUEST_CODE)
            }
        }
    }

    private fun setCompass(latLng: LatLng) : String{
        val builder = StringBuilder()
        if (latLng.latitude < 0) builder.append("S ") else builder.append("N ")

        val latitudeDegrees = Location.convert(abs(latLng.latitude), Location.FORMAT_SECONDS)
        val latitudeSplit = latitudeDegrees.split((":").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        builder.append(latitudeSplit[0])
        builder.append("º")
        builder.append(latitudeSplit[1])
        builder.append("'")
        builder.append(latitudeSplit[2])
        builder.append("\"")
        builder.append("\n")

        if (latLng.longitude < 0 ) builder.append("W ") else builder.append("E ")
        val longitudeDegrees = Location.convert(abs(latLng.longitude), Location.FORMAT_SECONDS)
        val longitudeSplit = longitudeDegrees.split((":").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        builder.append(longitudeSplit[0])
        builder.append("º")
        builder.append(longitudeSplit[1])
        builder.append("'")
        builder.append(longitudeSplit[2])
        builder.append("\"")

        return builder.toString()
    }

    override fun onSensorChanged(event: SensorEvent) {
        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)
        var azimuth: Int = 0
        val lastAccelerometer = FloatArray(3)
        var lastAccelerometerSet = false

        val lastMagnetoMeter = FloatArray(3)
        var lastMagnetoMeterSet = false

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR){
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            azimuth = (Math.toDegrees(SensorManager.getOrientation(rotationMatrix, orientation)[0].toDouble()) + 360).toInt() % 360
        }

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER){
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
            lastAccelerometerSet = true

        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD){
            System.arraycopy(event.values, 0 , lastMagnetoMeter, 0, event.values.size)
            lastMagnetoMeterSet = true
        }

        if (lastAccelerometerSet && lastMagnetoMeterSet){
            SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetoMeter)
            SensorManager.getOrientation(rotationMatrix, orientation)
            azimuth = (Math.toDegrees(SensorManager.getOrientation(rotationMatrix, orientation)[0].toDouble()) + 360).toInt() % 360
        }

        azimuth = azimuth.toFloat().roundToInt()
        binding.ivCompass.rotation = (-azimuth).toFloat()

        val where = when (azimuth){
            in 281..349 -> "NW"
            in 261..280 -> "W"
            in 191..260 -> "SW"
            in 171..190 -> "S"
            in 101..170 -> "SE"
            in 81..100 -> "E"
            in 11..80 -> "NE"
            else -> "N"
        }
        binding.tvDegrees.text = context?.getString(R.string.degrees_textview, azimuth.toString(), where)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    private fun startCompass() {
        if (sensorManager!!.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) == null){
            if (sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null || sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) == null){
                noSensorDetected()
            } else {
                accelerometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                magnetometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

                haveSensorAccelerometer =
                    sensorManager!!.registerListener(
                        this,
                        accelerometer,
                        SensorManager.SENSOR_DELAY_UI
                    )
                haveSensorMagnetometer =
                    sensorManager!!.registerListener(
                        this,
                        magnetometer,
                        SensorManager.SENSOR_DELAY_UI
                    )
            }
        } else {
            rotationVector = sensorManager!!.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            haveSensorRotationVector = sensorManager!!.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_UI)

        }
    }

    private fun stopCompass(){
        if (haveSensorRotationVector) sensorManager!!.unregisterListener(this, rotationVector)
        if (haveSensorAccelerometer) sensorManager!!.unregisterListener(this, accelerometer)
        if (haveSensorMagnetometer) sensorManager!!.unregisterListener(this, magnetometer)
    }

    private fun noSensorDetected() {
        val errorDialog = ErrorDialog(requireContext(), "Oops, seems like there is not a magnetic sensor in your phone, please try again!"){
            startCompass()
        }
        errorDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        errorDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        errorDialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_PLACES_REQUEST_CODE && resultCode == RESULT_OK){
            val place = data?.let { Autocomplete.getPlaceFromIntent(it) }
            binding.tvActualSelectedDestination.text = context?.getString(R.string.actual_destination, place?.address)
            googlePlacesSelectedLocationLatLng = place?.latLng!!
            viewModel.calculateDistance(googlePlacesSelectedLocationLatLng, true)
            animationLogicCounter = 0
        } else {
            Toast.makeText(context, "Oops, an error has occurred, please try again!", Toast.LENGTH_LONG).show()
        }
    }


}