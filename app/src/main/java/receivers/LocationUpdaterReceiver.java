package receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.mahmoudelshamy.gpstracking.vehicleapp.AppController;
import com.mahmoudelshamy.gpstracking.vehicleapp.R;

import datamodels.Cache;
import datamodels.Constants;
import datamodels.Vehicle;
import json.JsonReader;
import json.VehicleHandler;
import utils.GPSUtil;
import utils.InternetUtil;
import utils.NotificationUtil;

public class LocationUpdaterReceiver extends BroadcastReceiver {
    private GoogleApiClient googleApiClient;
    private Vehicle vehicle;

    @Override
    public void onReceive(final Context context, Intent intent) {
        boolean internetEnabled;
        boolean gpsEnabled;

        // check internet state
        if (InternetUtil.isConnected(context)) {
            internetEnabled = true;
        } else {
            // show notification
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationUtil.show(context, Constants.NOTI_ENABLE_INTERNET, R.string.enable_internet, soundUri);
            internetEnabled = false;
        }

        // check gps state
        if (GPSUtil.isGPSEnabled(context)) {
            gpsEnabled = true;
        } else {
            // show notification
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationUtil.show(context, Constants.NOTI_ENABLE_GPS, R.string.enable_gps, soundUri);
            gpsEnabled = false;
        }

        // check conditions
        if (internetEnabled && gpsEnabled) {
            // get active vehicle
            try {
                // get from app controller if alive
                vehicle = AppController.getInstance(context).getActiveVehicle();
            } catch (Exception e) {
                // get cached vehicle if exists
                String response = Cache.getActiveVehicleResponse(context);
                if (response != null) {
                    VehicleHandler handler = new VehicleHandler(response);
                    vehicle = handler.handle();
                }
            }

            // check to ensure that vehicle is not null
            if (vehicle != null) {

                // get last known location from Google Play Services
                googleApiClient = new GoogleApiClient.Builder(context)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                            @Override
                            public void onConnected(Bundle bundle) {
                                // get location
                                Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

                                // send update location request to server
                                new UpdateLocationTask(context, location).execute();

                                // start location updater after tracking interval
                                long updateTime = System.currentTimeMillis() + (vehicle.getTrackingInterval() * 1000);
                                AppController.startLocationUpdater(context, updateTime);

                                // disconnect service
                                googleApiClient.disconnect();
                            }

                            @Override
                            public void onConnectionSuspended(int i) {
                            }
                        })
                        .build();
                googleApiClient.connect();
            }
        }
    }

    /*
     * async task, used to send location to server
     */
    private class UpdateLocationTask extends AsyncTask<Void, Void, Void> {
        private double lat = 0.0;
        private double lng = 0.0;
        private boolean trackingTrip;

        private UpdateLocationTask(Context context, Location location) {
            // get lat & lng
            if (location != null) {
                lat = location.getLatitude();
                lng = location.getLongitude();
            }

            // get tracking trip flag
            trackingTrip = AppController.getInstance(context).getActiveVehicle().getTrackingTrip();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //  validate lat & lng
            if (lat == 0.0 || lng == 0.0) {
                cancel(true);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            // create json parser
            String url = AppController.END_POINT + "/update_location.php"
                    + "?vehicle_id=" + vehicle.getId() + "&lat=" + lat + "&lng=" + lng;
            if (trackingTrip)
                url += "&track_trip=true";

            JsonReader jsonReader = new JsonReader(url);

            // execute request
            jsonReader.sendGetRequest();

            return null;
        }
    }
}
