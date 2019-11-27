package dev.vlamir.trinitymenu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;


public class GeofenceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.e("GEOFENCE", "GEOFENCE HAS ERROR");
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if ((geofenceTransition & Geofence.GEOFENCE_TRANSITION_DWELL) == 0) return;

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context.getApplicationContext(), "notif")
                        .setSmallIcon(R.drawable.googleg_disabled_color_18)
                        .setContentTitle("HALL")
                        .setContentText("YOU ARE IN THE HALL")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(context.getApplicationContext());

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(42, builder.build());

        Toast.makeText(context.getApplicationContext(), "YOU ARE IN THE HALL", Toast.LENGTH_LONG).show();
    }

}
