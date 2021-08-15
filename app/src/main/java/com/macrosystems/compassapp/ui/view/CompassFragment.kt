package com.macrosystems.compassapp.ui.view

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place

import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.macrosystems.compassapp.R

import com.macrosystems.compassapp.databinding.CompassFragmentBinding
import com.macrosystems.compassapp.ui.view.dialogs.ErrorDialog
import com.macrosystems.compassapp.ui.viewmodel.CompassFragmentViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@AndroidEntryPoint
class CompassFragment: Fragment(), SensorEventListener {

    private val viewModel: CompassFragmentViewModel by viewModels()

    private var _binding: CompassFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var destinationLatLng: LatLng
    private lateinit var actualLatLng: LatLng

    private var animationLogicCounter = 0

    //Sensor Variables
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private var haveSensorAccelerometer = false
    private var haveSensorMagnetometer = false
    private var rotationVector: Sensor? = null
    private var haveSensorRotationVector = false

    private val googlePlacesLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult->
        if (activityResult.resultCode == RESULT_OK) {
            val place = activityResult.data?.let { Autocomplete.getPlaceFromIntent(it) }
            binding.tvActualSelectedDestination.text = requireContext().getString(R.string.actual_destination, place?.address)
            destinationLatLng = place?.latLng!!
            viewModel.calculateDistance(destinationLatLng, true)
            animationLogicCounter = 0
        } else {
            Toast.makeText(
                requireContext(),
                "Oops, an error has occurred, please try again!",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    private val requestLocationPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permissions ->
        var permissionCount = 0
        permissions.entries.forEach{ singlePermission ->
            if (singlePermission.value){
                permissionCount++
            }
        }
        if (permissionCount == 2){
            viewModel.startLocationUpdates()
        } else {
            Toast.makeText(requireContext(), "Please go to settings and enable location permissions for this app.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = CompassFragmentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        enableSensorManager()
        enableLocation()
        initObservers()
        startCompass()

        binding.btnSetNewDestination.setOnClickListener {
            val fields = listOf(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME)
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(requireActivity())
            googlePlacesLauncher.launch(intent)
        }
    }

    private fun enableSensorManager() {
        sensorManager = context?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private fun initObservers() {
        with(viewModel){
            location.observe(viewLifecycleOwner, {
                actualLatLng = it
                if (::destinationLatLng.isInitialized) {
                    if (animationLogicCounter == 0){
                        YoYo.with(Techniques.SlideInDown).duration(1000).playOn(binding.tvDistanceFromDestination)
                        binding.tvDistanceFromDestination.isVisible = true
                        calculateDistance(destinationLatLng, false)
                        binding.tvDistanceFromDestination.text = requireContext().getString(R.string.distance_from_destination, distance.value.toString())
                        animationLogicCounter++
                    } else {
                        binding.tvDistanceFromDestination.isVisible = true
                        calculateDistance(destinationLatLng, false)
                        binding.tvDistanceFromDestination.text = requireContext().getString(R.string.distance_from_destination, distance.value.toString())
                    }

                } else {
                    YoYo.with(Techniques.FadeOut).duration(500).playOn(binding.tvDistanceFromDestination)
                    binding.tvDistanceFromDestination.isGone = true
                }
            })

            lifecycleScope.launchWhenStarted {
                viewState.collect{ viewState ->
                    updateUI(viewState)
                }
            }

        }
    }

    private fun updateUI(viewState: CompassViewState) {
        with(binding){
            pbProgressBar.isVisible = viewState.isLoading
            btnSetNewDestination.isVisible = viewState.onSuccess


            if (viewState.locationError) {
                btnSetNewDestination.isGone = true
                showLocationError(viewState.errorMessage)
            }

            if (viewState.googlePlacesError){
                showGooglePlacesError()
            }

            if (viewState.onFailure){
                showGenericError()
            }
        }
    }

    private fun showGenericError() {
        val genericErrorDialog = ErrorDialog(requireContext(), null){}
        genericErrorDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        genericErrorDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        genericErrorDialog.show()
    }

    private fun showGooglePlacesError() {
        val genericErrorDialog = ErrorDialog(requireContext(), null){
            viewModel.initializeGooglePlaces()
        }
        genericErrorDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        genericErrorDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        genericErrorDialog.show()
    }

    private fun showLocationError(errorMessage: String?) {
        val locationErrorDialog = ErrorDialog(requireContext(), errorMessage) {
            enableLocation()
        }
        locationErrorDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        locationErrorDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        locationErrorDialog.show()
    }

    //Permission Logics
    private fun isLocationPermissionGranted() = ContextCompat.checkSelfPermission((requireActivity()), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun enableLocation() {
        if (isLocationPermissionGranted()){
            viewModel.startLocationUpdates()
        } else {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)){
            Toast.makeText(context, "Please go to settings and enable location permissions.", Toast.LENGTH_LONG).show()
        } else {
            requestLocationPermissions.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    //Lifecycle Logics
    override fun onResume() {
        super.onResume()
        if (isLocationPermissionGranted()){
            viewModel.startLocationUpdates()
        }

        startCompass()
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopLocationUpdates()
        stopCompass()
    }
    //


    override fun onSensorChanged(event: SensorEvent) {
        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)
        var azimuth = 0
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

        if (::actualLatLng.isInitialized && ::destinationLatLng.isInitialized){
            binding.ivDestinationDirection.isVisible = true
            val getDestinationBearing = azimuth - bearing()
            binding.ivDestinationDirection.rotation = (getDestinationBearing.toFloat())
        }
    }

    private fun bearing(): Double{
        val latitude1 = Math.toRadians(actualLatLng.latitude)
        val latitude2 = Math.toRadians(destinationLatLng.latitude)
        val longDiff = Math.toRadians(destinationLatLng.longitude - actualLatLng.longitude)
        val y = sin(longDiff) * cos(latitude2)
        val x = cos(latitude1) * sin(latitude2) - sin(latitude1) * cos(latitude2) * cos(longDiff)

        return (Math.toDegrees(atan2(y, x)) + 360) % 360
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun startCompass() {
        sensorManager?.let { _sensorManager ->
            if (_sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) == null){
                if (_sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null || _sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) == null){
                    noSensorDetectedError()
                } else {
                    accelerometer = _sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                    magnetometer = _sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

                    haveSensorAccelerometer =
                        _sensorManager.registerListener(
                            this,
                            accelerometer,
                            SensorManager.SENSOR_DELAY_UI
                        )
                    haveSensorMagnetometer =
                        _sensorManager.registerListener(
                            this,
                            magnetometer,
                            SensorManager.SENSOR_DELAY_UI
                        )
                }
            } else {
                rotationVector = _sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
                haveSensorRotationVector = _sensorManager.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_UI)

            }
        } ?: run {
            Toast.makeText(
                requireContext(),
                "Oops, an error has occurred, please close the app and reopen it.",
                Toast.LENGTH_LONG
            ).show()
        }

    }

    private fun stopCompass(){
        sensorManager?.let { _sensorManager ->
            if (haveSensorRotationVector) _sensorManager.unregisterListener(this, rotationVector)
            if (haveSensorAccelerometer) _sensorManager.unregisterListener(this, accelerometer)
            if (haveSensorMagnetometer) _sensorManager.unregisterListener(this, magnetometer)
        }

    }

    private fun noSensorDetectedError() {
        val errorDialog = ErrorDialog(requireContext(), "Oops, seems like there is not a magnetic sensor in your phone, please try again!"){
            startCompass()
        }
        errorDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        errorDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        errorDialog.show()
    }

}