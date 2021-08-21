package com.macrosystems.compassapp.ui.view

import android.Manifest
import android.app.Activity.RESULT_OK

import android.content.pm.PackageManager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.macrosystems.compassapp.R
import com.macrosystems.compassapp.core.utils.DialogFragmentLauncher
import com.macrosystems.compassapp.core.ex.longToast
import com.macrosystems.compassapp.core.ex.shortToast
import com.macrosystems.compassapp.core.ex.showDialog
import com.macrosystems.compassapp.data.model.NavigationDetails
import com.macrosystems.compassapp.databinding.CompassFragmentBinding
import com.macrosystems.compassapp.ui.view.dialogs.ErrorDialog
import com.macrosystems.compassapp.ui.viewmodel.CompassFragmentViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@AndroidEntryPoint
class CompassFragment: Fragment(){

    private val viewModel: CompassFragmentViewModel by viewModels()

    private var _binding: CompassFragmentBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var dialogLauncher: DialogFragmentLauncher

    private lateinit var destinationLatLng: LatLng
    private lateinit var actualLatLng: LatLng
    private lateinit var destinationAddress: String
    private var getDestinationBearing: Double = 0.0

    private var animationLogicCounter = 0

    //Launchers
    private val googlePlacesLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult->
        if (activityResult.resultCode == RESULT_OK) {
            val place = activityResult.data?.let { Autocomplete.getPlaceFromIntent(it) }
            destinationAddress = place?.address.toString()
            binding.tvActualSelectedDestination.text = requireContext().getString(R.string.actual_destination, place?.address)
            destinationLatLng = place?.latLng!!
            viewModel.calculateDistance(destinationLatLng, true)
            animationLogicCounter = 0
            viewModel.deleteLastLocation()
        } else {
            requireActivity().shortToast("Setting destination cancelled")
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
            requireActivity().longToast("Please go to settings and enable location permissions for this app.")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = CompassFragmentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        enableLocation()
        initObservers()
        setUpListeners()

    }

    private fun setUpListeners() {
        binding.btnSetNewDestination.setOnClickListener {
            val fields = listOf(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME)
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(requireActivity())
            googlePlacesLauncher.launch(intent)
        }

        binding.tvDistanceFromDestination.setOnClickListener {
            if (::destinationLatLng.isInitialized && ::destinationAddress.isInitialized){
                val action = CompassFragmentDirections.actionCompassFragmentToMapFragment(destinationLatLng.latitude.toString(), destinationLatLng.longitude.toString(), destinationAddress)
                findNavController().navigate(action)
            }
        }
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
                        binding.tvDistanceFromDestination.text = requireContext().getString(R.string.distance_from_destination, distance.value ?: "loading...")
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


            savedNavigationDetails.observe(viewLifecycleOwner, {
                actualLatLng = it.actualLatLng
                destinationLatLng = it.destinationLatLng
                destinationAddress = it.destinationAddress
                binding.tvActualSelectedDestination.text = getString(R.string.actual_destination, it.destinationAddress)

            })

            compassData.observe(viewLifecycleOwner, {
                binding.tvDegrees.text = requireContext().getString(R.string.degrees_textview, it.azimuth.toString(), it.cardinalPoint)
                binding.ivCompass.rotation = (-it.azimuth!!).toFloat()

                if (::actualLatLng.isInitialized && ::destinationLatLng.isInitialized){
                    binding.ivDestinationDirection.isVisible = true
                    getDestinationBearing = it.azimuth - calculateBearing()
                    binding.ivDestinationDirection.rotation = (getDestinationBearing.toFloat())
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

            if (viewState.compassSensorError){
                compassSensorMissingError()
            }
        }
    }

    private fun showGenericError() {
        ErrorDialog.create(textMessage = "Sorry, an error has occurred, please try again.",
        positiveAction = ErrorDialog.Action("OK") {
            it.dismiss()
        }).showDialog(dialogLauncher, requireActivity())

    }

    private fun compassSensorMissingError(){
        ErrorDialog.create(textMessage = "Oops, your phone seems to not have a magnetic sensor, please close the app and open it again.", positiveAction = ErrorDialog.Action("GOT IT") {
            it.dismiss()
        }).showDialog(dialogLauncher, requireActivity())
    }

    private fun showGooglePlacesError() {
        ErrorDialog.create(textMessage = "Sorry, an error has occurred, please try again.",
            positiveAction = ErrorDialog.Action("OK") {
                viewModel.initializeGooglePlaces()
                it.dismiss()
            }).showDialog(dialogLauncher, requireActivity())
    }

    private fun showLocationError(errorMessage: String?) {
        ErrorDialog.create(textMessage = errorMessage ?: "Sorry, an error has occurred, please try again.",
            positiveAction = ErrorDialog.Action("OK") {
                enableLocation()
                it.dismiss()
            }).showDialog(dialogLauncher, requireActivity())
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
            requireActivity().longToast("Please go to settings and enable location permissions.")
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

        viewModel.startCompass()
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopLocationUpdates()
        viewModel.stopCompass()
        saveNavigationOnPersistence()
    }
    //

    private fun calculateBearing(): Double{
        val latitude1 = Math.toRadians(actualLatLng.latitude)
        val latitude2 = Math.toRadians(destinationLatLng.latitude)
        val longDiff = Math.toRadians(destinationLatLng.longitude - actualLatLng.longitude)
        val y = sin(longDiff) * cos(latitude2)
        val x = cos(latitude1) * sin(latitude2) - sin(latitude1) * cos(latitude2) * cos(longDiff)

        return (Math.toDegrees(atan2(y, x)) + 360) % 360
    }

    private fun saveNavigationOnPersistence(){
        if (::actualLatLng.isInitialized && ::destinationLatLng.isInitialized && ::destinationAddress.isInitialized){
            viewModel.saveNavigationDetailsToLocalDB(NavigationDetails(destinationAddress, actualLatLng, destinationLatLng))
        }
    }

}