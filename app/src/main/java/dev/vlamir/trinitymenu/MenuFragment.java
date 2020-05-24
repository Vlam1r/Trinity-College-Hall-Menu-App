package dev.vlamir.trinitymenu;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalTime;
import java.util.Objects;


public class MenuFragment extends Fragment {

    //private static final double HALL_LAT = WOLF_LAT;
    //private static final double HALL_LON = WOLF_LON;

    private Calendar calendar;
    private final Bundle lunch = new Bundle();
    private final Bundle dinner = new Bundle();
    private URL url;
    private PagerAdapter adapter;

    private ProgressBar pBar;
    private TabLayout tabLayout;

    private View v;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void initialize() {

        createNotificationChannel();

        Button selectDate = v.findViewById(R.id.dateselect);
        calendar = Calendar.getInstance();

        //PROGRESS BAR
        pBar = v.findViewById(R.id.progressBar);

        initTabs();

        //CONFIGURE DATE PICKER
        selectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(v.getContext(),
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

        v.findViewById(R.id.fAB).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar = Calendar.getInstance();
                updateMeals();
            }
        });

        updateMeals();

        // SELECT LUNCH OR DINNER BASED ON TIME OF DAY
        if (LocalTime.now().isAfter(Constants.LUNCH_END)) {
            Objects.requireNonNull(tabLayout.getTabAt(1)).select();
        }

    }

    private void initTabs() {

        tabLayout = v.findViewById(R.id.meals);
        final ViewPager viewPager = v.findViewById(R.id.view_pager);
        adapter = new PagerAdapter
                (getFragmentManager(), tabLayout.getTabCount(), lunch, dinner);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.fragment_menu, container, false);
        initialize();
        return v;
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
            NotificationManager notificationManager =
                    Objects.requireNonNull(getActivity()).getSystemService(NotificationManager.class);
            Objects.requireNonNull(notificationManager).createNotificationChannel(channel);
        }
    }

    private void updateMeals() {

        try {
            url = new URL("https://vlamir.dynu.net/response/hallmenu.php?date=" +
                    calendar.get(Calendar.YEAR) + "-" +
                    (calendar.get(Calendar.MONTH) + 1) + "-" +
                    calendar.get(Calendar.DAY_OF_MONTH));
        } catch (MalformedURLException e) {
            Toast.makeText(v.getContext(), "Error Invalid URL", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        lunch.putString("food", "");
        dinner.putString("food", "");
        pBar.setVisibility(View.VISIBLE);
        adapter.updateFragments();
        new HTTPGetJson(this).execute();

    }

    private static class HTTPGetJson extends dev.vlamir.trinitymenu.HTTPGetJson {


        final MenuFragment fragment;

        HTTPGetJson(MenuFragment context) {
            super(context);
            fragment = activityReference.get();
            url = fragment.url;
        }

        @Override
        protected void onPostExecute(String result) {
            {
                // get a reference to the activity if it is still there
                Activity activity = fragment.getActivity();

                if (activity == null || activity.isFinishing()) return;

                fragment.pBar.setVisibility(View.GONE);

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

                fragment.lunch.putString("food", meal.getLunch());
                fragment.dinner.putString("food", meal.getDinner());
                fragment.adapter.updateFragments();
            }
        }

    }
}