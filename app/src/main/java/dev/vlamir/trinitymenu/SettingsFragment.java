package dev.vlamir.trinitymenu;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;

import java.util.Objects;

import static android.Manifest.permission.ACCESS_BACKGROUND_LOCATION;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static dev.vlamir.trinitymenu.Constants.HALL_LAT;
import static dev.vlamir.trinitymenu.Constants.HALL_LOITER;
import static dev.vlamir.trinitymenu.Constants.HALL_LON;
import static dev.vlamir.trinitymenu.Constants.HALL_RAD;
import static dev.vlamir.trinitymenu.Constants.REQUEST_PERMISSION_LOCATION;

public class SettingsFragment extends Fragment {

    GeofencingClient geofencingClient;
    private View v;
    private Switch geoEnabler;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_settings, container, false);

        v.findViewById(R.id.getLocationPerm).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                requestPermissions();
            }
        });

        geoEnabler = v.findViewById(R.id.geolocationEnable);
        geoEnabler.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    initGeofence();
                } else {
                    //TODO Stop Geofencing
                }
            }
        });
        permissionsGranted(((MainActivity) getActivity()).checkPermissions());
        return v;
    }

    private void requestPermissions() {

        assert (getActivity() != null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestPermissions(new String[]{ACCESS_FINE_LOCATION,
                    ACCESS_COARSE_LOCATION,
                    ACCESS_BACKGROUND_LOCATION}, REQUEST_PERMISSION_LOCATION);
        } else {

            requestPermissions(new String[]{ACCESS_FINE_LOCATION,
                    ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_LOCATION);

        }

    }


    private void initGeofence() {

        geofencingClient = LocationServices.getGeofencingClient(v.getContext());

        Geofence hallLocation = new Geofence.Builder()
                .setRequestId("hallmenu_geofence")
                .setCircularRegion(HALL_LAT, HALL_LON, HALL_RAD)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL |
                        Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .setLoiteringDelay(HALL_LOITER)
                .build();
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL |
                GeofencingRequest.INITIAL_TRIGGER_ENTER |
                GeofencingRequest.INITIAL_TRIGGER_EXIT);
        builder.addGeofence(hallLocation);

        PendingIntent geofencingIntent = PendingIntent.getBroadcast(v.getContext(), 0,
                new Intent(v.getContext(), GeofenceReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        geofencingClient.addGeofences(builder.build(), geofencingIntent)
                .addOnFailureListener(Objects.requireNonNull(getActivity()), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        geoEnabler.setChecked(false);
                    }
                });
    }
/*
    private void endGeofence() {

        geofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences removed
                        // ...
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to remove geofences
                        // ...
                    }
                });
    }
*/

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            permissionsGranted(grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED);

        }
    }

    private void permissionsGranted(boolean granted) {
        Button b = v.findViewById(R.id.getLocationPerm);
        if (granted) {
            b.setEnabled(false);
            b.setBackgroundColor(Color.argb(100, 30, 133, 30));
            geoEnabler.setEnabled(true);
        } else {
            b.setEnabled(true);
            b.setBackgroundColor(Color.LTGRAY);
            geoEnabler.setEnabled(false);
        }
    }
}
