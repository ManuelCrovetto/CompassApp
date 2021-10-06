package com.macrosystems.compassapp.ui.view;

import static com.macrosystems.compassapp.data.model.Constants.MAP_CAMERA_ZOOM;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.macrosystems.compassapp.R;
import com.macrosystems.compassapp.databinding.MapFragmentBinding;


public class MapFragment extends Fragment implements OnMapReadyCallback {
    private MapFragmentBinding binding;
    private GoogleMap map;
    private LatLng destination;
    private String destinationAddress;
    private NavController navController;

    public MapFragment(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = MapFragmentBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        checkAndSetArguments();
        setSupportMapFragment();
        setListeners();
        setNavController();
    }

    private void setNavController() {
        navController = Navigation.findNavController(requireView());
    }

    private void setListeners() {
        binding.btnBackButtonFromMap.setOnClickListener(v -> navController.popBackStack());
    }

    private void setSupportMapFragment() {
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(binding.mapView.getId());
        assert supportMapFragment != null;
        supportMapFragment.getMapAsync(this);
    }

    private void checkAndSetArguments() {
        if (getArguments() != null){
            MapFragmentArgs args = MapFragmentArgs.fromBundle(getArguments());
            destination = new LatLng(Double.parseDouble(args.getLatitue()), Double.parseDouble(args.getLongitude()));
            destinationAddress = args.getDestinationName();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        if (destination != null){
            setUpMap(destination);
        }
    }

    //Once the user is at this point, the user have had to give permissions.
    @SuppressLint("MissingPermission")
    private void setUpMap(LatLng destination) {
        map.addMarker(new MarkerOptions().position(destination).title(destinationAddress).snippet(getString(R.string.destination_default_placeholder)));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(destination, MAP_CAMERA_ZOOM));
        map.getUiSettings().setMapToolbarEnabled(true);

        map.setOnMyLocationClickListener(location -> {
            LatLng actualLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(actualLatLng, MAP_CAMERA_ZOOM));
        });

        map.setMyLocationEnabled(true);
    }
}
