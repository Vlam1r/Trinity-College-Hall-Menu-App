package dev.vlamir.trinitymenu;

import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static android.Manifest.permission.ACCESS_BACKGROUND_LOCATION;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;


public class MainActivity extends AppCompatActivity {

    private static final double HALL_LAT = 52.20712; //52.2068962;
    private static final double HALL_LON = 0.118585; //0.1160932;
    private static final float HALL_RAD = 30f;
    private static final int HALL_LOITER = 40 * 1000;

    private Calendar calendar;
    private Bundle lunch = new Bundle();
    private Bundle dinner = new Bundle();
    private URL url;
    private PagerAdapter adapter;

    private GeofencingClient geofencingClient;
    private Geofence hallLocation;
    private PendingIntent geofencingIntent;

    private ProgressBar pBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();

        Button selectDate = findViewById(R.id.dateselect);
        calendar = Calendar.getInstance();

        //PROGRESS BAR
        pBar = findViewById(R.id.progressBar);

        //CONFIGURE TABS
        TabLayout tabLayout = findViewById(R.id.meals);
        final ViewPager viewPager = findViewById(R.id.view_pager);
        adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount(), lunch, dinner);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        //CONFIGURE DATE PICKER
        selectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                calendar.set(year, month, day);
                                updateMeals();
                            }
                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });

        findViewById(R.id.fAB).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar = Calendar.getInstance();
                updateMeals();
            }
        });

        updateMeals();

        // SELECT LUNCH OR DINNER BASED ON TIME OF DAY
        if (Calendar.getInstance().getTime().after(Constants.LUNCH_END)) {
            Objects.requireNonNull(tabLayout.getTabAt(1)).select();
        }

        //CONFIGURE GEOFENCER
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION,
                ACCESS_COARSE_LOCATION,
                ACCESS_BACKGROUND_LOCATION}, 0);

        geofencingClient = LocationServices.getGeofencingClient(this);
        hallLocation = new Geofence.Builder()
                .setRequestId("hallmenu_geofence")
                .setCircularRegion(HALL_LAT, HALL_LON, HALL_RAD)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL |
                        Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .setLoiteringDelay(HALL_LOITER)
                .build();
        geofencingIntent = PendingIntent.getBroadcast(this, 0,
                new Intent(this, GeofenceReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        geofencingClient.addGeofences(getGeofencingRequest(), geofencingIntent)
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),
                                "Failed to make geofence", Toast.LENGTH_SHORT)
                                .show();
                    }
                });

    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "notif";
            String description = "hall channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("notif", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL |
                GeofencingRequest.INITIAL_TRIGGER_ENTER |
                GeofencingRequest.INITIAL_TRIGGER_EXIT);
        builder.addGeofence(hallLocation);
        return builder.build();
    }

    private void updateMeals() {

        try {
            url = new URL("https://vlamir.dynu.net/response/hallmenu.php?date=" +
                    calendar.get(Calendar.YEAR) + "-" +
                    (calendar.get(Calendar.MONTH) + 1) + "-" +
                    calendar.get(Calendar.DAY_OF_MONTH));
        } catch (MalformedURLException e) {
            Toast.makeText(getApplicationContext(), "Error Invalid URL", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        lunch.putString("food", "");
        dinner.putString("food", "");
        pBar.setVisibility(View.VISIBLE);
        adapter.updateFragments();
        new HTTPGetJson(this).execute();

    }

    private static class HTTPGetJson extends AsyncTask<Void, Void, String> {

        private WeakReference<MainActivity> activityReference;

        // only retain a weak reference to the activity
        HTTPGetJson(MainActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(Void... params) {
            return getJSON(activityReference.get().url);
        }

        @Override
        protected void onPostExecute(String result) {

            // get a reference to the activity if it is still there
            MainActivity activity = activityReference.get();

            if (activity == null || activity.isFinishing()) return;

            activity.pBar.setVisibility(View.GONE);

            if (result == null) {
                Toast.makeText(activity.getApplicationContext(),
                        "Error connecting to server", Toast.LENGTH_SHORT).show();
                return;
            }
            if (result.contains("missing from the database")) {
                Toast.makeText(activity.getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                return;
            }


            JsonObject json = new JsonParser().parse(result).getAsJsonObject();

            Meal meal = new Gson().fromJson(json, Meal.class);

            activity.lunch.putString("food", meal.getLunch());
            activity.dinner.putString("food", meal.getDinner());
            activity.adapter.updateFragments();
        }

        private String getJSON(URL u) {
            HttpURLConnection c = null;
            try {
                c = (HttpURLConnection) u.openConnection();
                c.setRequestMethod("GET");
                c.setRequestProperty("Content-length", "0");
                c.setUseCaches(false);
                c.setAllowUserInteraction(false);
                c.setConnectTimeout(5000);
                c.setReadTimeout(5000);
                c.connect();
                int status = c.getResponseCode();

                switch (status) {
                    case 200:
                    case 201:
                        BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line).append("\n");
                        }
                        br.close();
                        return sb.toString();
                }

            } catch (IOException ex) {
                Logger.getGlobal().log(Level.SEVERE, null, ex);
            } finally {
                if (c != null) {
                    try {
                        c.disconnect();
                    } catch (Exception ex) {
                        Logger.getGlobal().log(Level.SEVERE, null, ex);
                    }
                }
            }
            return null;
        }
    }
}