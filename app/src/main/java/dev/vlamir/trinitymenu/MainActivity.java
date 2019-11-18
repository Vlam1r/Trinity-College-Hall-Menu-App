package dev.vlamir.trinitymenu;

import android.app.DatePickerDialog;
import android.icu.util.Calendar;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

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
import java.util.logging.Level;
import java.util.logging.Logger;


public class MainActivity extends AppCompatActivity {

    private Calendar calendar;
    private Bundle lunch = new Bundle();
    private Bundle dinner = new Bundle();
    private URL url;
    private PagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button selectDate = findViewById(R.id.dateselect);
        calendar = Calendar.getInstance();

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
    }

    private void updateMeals() {

        Toast.makeText(getApplicationContext(), "Fetching data", Toast.LENGTH_SHORT).show();
        try {
            url = new URL("https://vlamir.dynu.net/response/hallmenu.php?date=" +
                    calendar.get(Calendar.YEAR) + "-" +
                    (calendar.get(Calendar.MONTH) + 1) + "-" +
                    calendar.get(Calendar.DAY_OF_MONTH));
        } catch (MalformedURLException e) {
            Toast.makeText(getApplicationContext(), "Error Invalid URL", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

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
            return getJSON(activityReference.get().url, 5000);
        }

        @Override
        protected void onPostExecute(String result) {

            // get a reference to the activity if it is still there
            MainActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            if (result.contains("Meals for today")) {
                Toast.makeText(activityReference.get().getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                return;
            }


            JsonObject json = new JsonParser().parse(result).getAsJsonObject();

            Meal meal = new Gson().fromJson(json, Meal.class);
            activityReference.get().lunch.putString("food", meal.getLunch());
            activityReference.get().dinner.putString("food", meal.getDinner());

            activityReference.get().adapter.updateFragments();
        }

        private String getJSON(URL u, int timeout) {
            HttpURLConnection c = null;
            try {
                c = (HttpURLConnection) u.openConnection();
                c.setRequestMethod("GET");
                c.setRequestProperty("Content-length", "0");
                c.setUseCaches(false);
                c.setAllowUserInteraction(false);
                c.setConnectTimeout(timeout);
                c.setReadTimeout(timeout);
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