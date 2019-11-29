package dev.vlamir.trinitymenu;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import static android.Manifest.permission.ACCESS_BACKGROUND_LOCATION;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class SettingsFragment extends Fragment {


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
        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        v.findViewById(R.id.getLocationPerm).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                requestPermissions();
            }
        });

        return v;
    }

    private void requestPermissions() {

        assert (getActivity() != null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{ACCESS_FINE_LOCATION,
                    ACCESS_COARSE_LOCATION,
                    ACCESS_BACKGROUND_LOCATION}, 0);
        } else {

            ActivityCompat.requestPermissions(getActivity(), new String[]{ACCESS_FINE_LOCATION,
                    ACCESS_COARSE_LOCATION}, 0);

        }

    }
}
