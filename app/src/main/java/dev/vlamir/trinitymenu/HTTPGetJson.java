package dev.vlamir.trinitymenu;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

abstract class HTTPGetJson extends AsyncTask<Void, Void, String> {

    final WeakReference<MenuFragment> activityReference;
    URL url;

    HTTPGetJson(MenuFragment context) {
        activityReference = new WeakReference<>(context);
    }

    @Override
    protected String doInBackground(Void... params) {
        return getJSON();
    }

    @Override
    protected abstract void onPostExecute(String result);

    private String getJSON() {
        HttpURLConnection c = null;
        try {
            c = (HttpURLConnection) url.openConnection();
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
