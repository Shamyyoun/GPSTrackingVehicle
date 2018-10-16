package com.mahmoudelshamy.gpstracking.vehicleapp;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import datamodels.Cache;
import datamodels.Constants;
import datamodels.Vehicle;
import json.JsonReader;
import json.VehicleHandler;
import receivers.LocationUpdaterReceiver;

/**
 * Created by Shamyyoun on 3/15/2015.
 */
public class AppController extends Application {
    public static final String END_POINT = "http://gpstracking.mahmoudelshamy.com";
    public static final String PROJECT_NUMBER = "547910235601";
    public static final String KIT_MAC_ADDRESS = "20:15:03:27:14:19";

    private Vehicle activeVehicle;

    public AppController() {
        super();
    }

    /**
     * overriden method
     */
    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * method, used to get active vehicle from runtime or from SP
     */
    public Vehicle getActiveVehicle() {
        if (activeVehicle == null) {
            // get cached vehicle if exists
            String response = Cache.getActiveVehicleResponse(getApplicationContext());
            if (response != null) {
                VehicleHandler handler = new VehicleHandler(response);
                activeVehicle = handler.handle();

                // get cached tracking interval if exists
                int trackingInterval = Cache.getTrackingInterval(getApplicationContext());
                if (trackingInterval != -1) {
                    // exists
                    activeVehicle.setTrackingInterval(trackingInterval);
                }

                // get tracking_trip flag ftom SP
                boolean trackingTrip = Cache.getTrackingTrip(getApplicationContext());
                activeVehicle.setTrackingTrip(trackingTrip);
            }
        }

        return activeVehicle;
    }

    /**
     * method, used to set active user
     */
    public void setActiveVehicle(Vehicle vehicle) {
        this.activeVehicle = vehicle;
    }

    /**
     * method used to return current application instance
     */
    public static AppController getInstance(Context context) {
        return (AppController) context.getApplicationContext();
    }

    /**
     * method, used to register vehicle to GCM and send its reg_id to server
     */
    public static void registerToGCM(final Context context) {
        // execute operation in async task
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    // get reg_id
                    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
                    String regId = gcm.register(PROJECT_NUMBER);

                    // check reg_id
                    if (regId != null) {
                        // send reg_id to server
                        JsonReader jsonReader = new JsonReader(AppController.END_POINT + "/update_vehicle_regid.php");
                        List<NameValuePair> parameters = new ArrayList<>(2);
                        parameters.add(new BasicNameValuePair("vehicle_id", getInstance(context).getActiveVehicle().getId()));
                        parameters.add(new BasicNameValuePair("reg_id", regId));
                        jsonReader.sendAsyncPostRequest(parameters);

                        // cache it
                        Cache.updateRegId(context, regId);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }
        }.execute();
    }

    /**
     * method, used to start alarm manager to update location
     */
    public static void startLocationUpdater(Context context, long when) {
        Intent mIntent = new Intent(context, LocationUpdaterReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                Constants.RECEIVER_LOCATION_UPDATER, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, when, pendingIntent);
    }

    /**
     * method, used to cancel alarm manager before starts
     */
    public static void stopLocationUpdater(Context context) {
        Intent mIntent = new Intent(context, LocationUpdaterReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                Constants.RECEIVER_LOCATION_UPDATER, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}
