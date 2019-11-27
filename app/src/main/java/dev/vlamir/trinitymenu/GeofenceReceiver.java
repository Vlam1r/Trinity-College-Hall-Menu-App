package dev.vlamir.trinitymenu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.Calendar;
import java.util.Date;

import static dev.vlamir.trinitymenu.Constants.DEBUG;
import static dev.vlamir.trinitymenu.Constants.DINNER_BEGIN;
import static dev.vlamir.trinitymenu.Constants.DINNER_END;
import static dev.vlamir.trinitymenu.Constants.LUNCH_BEGIN;
import static dev.vlamir.trinitymenu.Constants.LUNCH_END;


public class GeofenceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.e("GEOFENCE", "GEOFENCE HAS ERROR");
            return;
        }

        Date now = Calendar.getInstance().getTime();
        if (!DEBUG && ((now.after(LUNCH_BEGIN) && now.before(LUNCH_END)) ||
                (now.after(DINNER_BEGIN) && now.after(DINNER_END)))) return;

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context.getApplicationContext(), "notif")
                        .setSmallIcon(R.drawable.googleg_disabled_color_18)
                        .setContentTitle("HALL")
                        .setContentText("YOU ARE IN THE HALL")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setOngoing(true);

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(context.getApplicationContext());

        // Test that the reported transition was of interest.
        if ((geofenceTransition & Geofence.GEOFENCE_TRANSITION_EXIT) != 0) {
            notificationManager.cancel(42);
            //TODO: Exited
        }
        if ((geofenceTransition & Geofence.GEOFENCE_TRANSITION_DWELL) == 0) return;

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(42, builder.build());
    }

}
