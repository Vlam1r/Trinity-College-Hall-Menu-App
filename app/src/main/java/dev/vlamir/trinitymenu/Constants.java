package dev.vlamir.trinitymenu;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.LocalTime;

@RequiresApi(api = Build.VERSION_CODES.O)
public class Constants {

    public static final double HALL_LAT = 0;
    public static final double HALL_LON = 0;
    public static final int HALL_LOITER = 0;
    public static final float HALL_RAD = 20;
    public static final int REQUEST_PERMISSION_LOCATION = 0;
    public static final boolean DEBUG = true;
    public static final LocalTime LUNCH_BEGIN = LocalTime.parse("11:30");
    public static final LocalTime LUNCH_END = LocalTime.parse("14:00");
    public static final LocalTime DINNER_BEGIN = LocalTime.parse("17:30");
    public static final LocalTime DINNER_END = LocalTime.parse("19:00");

    private Constants() {
    }
}
