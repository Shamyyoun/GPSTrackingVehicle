package services;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.mahmoudelshamy.gpstracking.vehicleapp.AppController;
import com.mahmoudelshamy.gpstracking.vehicleapp.R;

import java.io.IOException;
import java.util.Set;

import datamodels.Cache;
import datamodels.Constants;
import json.JsonReader;
import receivers.GcmBroadcastReceiver;
import utils.InternetUtil;
import utils.NotificationUtil;

public class GcmMessageHandler extends IntentService {
    private Handler handler;
    private GoogleApiClient googleApiClient;

    public GcmMessageHandler() {
        super("GcmMessageHandler");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final Bundle extras = intent.getExtras();

        // get values
        final String key = extras.getString("key");
        final String message = extras.getString("message");

        handler.post(new Runnable() {
                         @Override
                         public void run() {
                             // check if there is an active vehicle
                             if (AppController.getInstance(getApplicationContext()).getActiveVehicle() != null) {
                                 // check message
                                 if (Constants.PUSHN_TURN.equals(key)) {
                                     // validate turn operation
                                     if ("A".equals(message) || "a".equals(message)) {
                                         new Thread(new Runnable() {
                                             @Override
                                             public void run() {
                                                 BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                                                 // check if bluetooth is supported
                                                 if (mBluetoothAdapter == null) {
                                                     // show notification
                                                     handler.post(new Runnable() {
                                                         @Override
                                                         public void run() {
                                                             Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                                             NotificationUtil.show(getApplicationContext(), Constants.NOTI_VEHICLE_ACTION, R.string.bluetooth_not_supported, soundUri);
                                                         }
                                                     });
                                                     return;
                                                 }

                                                 // check if bluetooth is not enabled
                                                 if (!mBluetoothAdapter.isEnabled()) {
                                                     // show notification
                                                     handler.post(new Runnable() {
                                                         @Override
                                                         public void run() {
                                                             Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                                             NotificationUtil.show(getApplicationContext(), Constants.NOTI_VEHICLE_ACTION, R.string.turn_on_bluetooth, soundUri);
                                                         }
                                                     });
                                                     return;
                                                 }

                                                 // get paired devices
                                                 Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

                                                 // get kit device from paired devices
                                                 boolean paired = false;
                                                 for (BluetoothDevice device : pairedDevices) {
                                                     if (device.getAddress().equals(AppController.KIT_MAC_ADDRESS)) {
                                                         // hold kit device
                                                         BluetoothSender bluetoothSender = new BluetoothSender(device, true, mBluetoothAdapter, message);
                                                         try {
                                                             bluetoothSender.send();

                                                             // show notification
                                                             handler.post(new Runnable() {
                                                                 @Override
                                                                 public void run() {
                                                                     Uri soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.horn);
                                                                     NotificationUtil.show(getApplicationContext(), Constants.NOTI_VEHICLE_ACTION,
                                                                             ("A".equals(message) ? R.string.vehicle_will_be_turn_on : R.string.vehicle_will_be_turn_off), soundUri);
                                                                 }
                                                             });
                                                         } catch (IOException e) {
                                                             e.printStackTrace();
                                                         }

                                                         paired = true;
                                                         break;
                                                     }
                                                 }

                                                 if (!paired) {
                                                     // show notification
                                                     handler.post(new Runnable() {
                                                         @Override
                                                         public void run() {
                                                             Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                                             NotificationUtil.show(getApplicationContext(), Constants.NOTI_VEHICLE_ACTION, R.string.turn_on_bluetooth, soundUri);
                                                         }
                                                     });
                                                 }
                                             }
                                         }).run();
                                     }
                                 } else if (Constants.PUSHN_UPDATE_TRACKING_INTERVAL.equals(key)) {
                                     // get new tracking time value
                                     try {
                                         // get new value
                                         int trackingValue = Integer.parseInt(message);
                                         // set new tracking interval to the active vehicle
                                         AppController.getInstance(getApplicationContext()).getActiveVehicle().setTrackingInterval(trackingValue);
                                         // cache new tracking interval value
                                         Cache.updateTrackingInterval(getApplicationContext(), trackingValue);
                                         // show notification
                                         String msg = getString(R.string.tracking_interval_updated_by_owner_to) + " " + trackingValue + " seconds";
                                         Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                         NotificationUtil.show(getApplicationContext(), Constants.NOTI_TRACKING_INTERVAL_UPDATED, msg, soundUri);
                                     } catch (Exception e) {
                                         e.printStackTrace();
                                     }
                                 } else if (Constants.PUSHN_TRIP.equals(key)) {
                                     // get time value
                                     final String time = extras.getString("time");

                                     // get last known location from Google Play Services
                                     googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                                             .addApi(LocationServices.API)
                                             .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                                                 @Override
                                                 public void onConnected(Bundle bundle) {
                                                     // get location
                                                     Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                                                     // send trip request to server
                                                     new TripTask(getApplicationContext(), message, time, location).execute();
                                                     // disconnect service
                                                     googleApiClient.disconnect();
                                                 }

                                                 @Override
                                                 public void onConnectionSuspended(int i) {
                                                 }
                                             })
                                             .build();

                                     // check trip msg
                                     if ("start".equals(message)) {
                                         // update vehicle tripTracking flag in runtime
                                         AppController.getInstance(getApplicationContext()).getActiveVehicle().setTrackingTrip(true);
                                         // update cached trackingTrip flag
                                         Cache.updateTrackingTrip(getApplicationContext(), true);
                                         // send google api client, then it will execute trip request when obtain location
                                         googleApiClient.connect();
                                     } else if ("end".equals(message)) {
                                         // update vehicle tripTracking flag in runtime
                                         AppController.getInstance(getApplicationContext()).getActiveVehicle().setTrackingTrip(false);
                                         // update cached trackingTrip flag
                                         Cache.updateTrackingTrip(getApplicationContext(), false);
                                         // send google api client, then it will execute trip request when obtain location
                                         googleApiClient.connect();
                                     }
                                 }
                             }
                         }
                     }

        );

        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    /**
     * sub class, used to send trip request to server
     */
    private class TripTask extends AsyncTask<Void, Void, Void> {
        private Context context;
        private String mode;
        private String time;
        private double lat;
        private double lng;

        private String response;

        public TripTask(Context context, String mode, String time, Location location) {
            this.context = context;
            this.mode = mode;
            this.time = time;
            lat = location.getLatitude();
            lng = location.getLongitude();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // validate values
            if (lat == 0.0 || lng == 0.0) {
                cancel(true);
                return;
            }

            // check internet
            if (!InternetUtil.isConnected(context)) {
                cancel(true);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            // create json parser
            String url = AppController.END_POINT + "/trip.php?time=" + time
                    + "&lat=" + lat + "&lng=" + lng + "&mode=" + mode
                    + "&vehicle_id=" + AppController.getInstance(context).getActiveVehicle().getId();
            JsonReader jsonReader = new JsonReader(url);

            // execute request
            response = jsonReader.sendGetRequest();

            System.out.println("RES: " + response);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            String msg; // msg to be send to user
            // check response
            if ("success".equals(response)) {
                msg = "success";

                // show notification
                int notificationMsgRes = "start".equals(mode) ? R.string.owner_has_started_trip : R.string.owner_has_ended_current_trip;
                Uri soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.horn);
                NotificationUtil.show(getApplicationContext(), Constants.NOTI_TRIP_ACTION, notificationMsgRes, soundUri);
            } else {
                msg = "error";
            }

            // send msg to user
            String url = AppController.END_POINT + "/send_trip_msg_to_user.php?username="
                    + AppController.getInstance(context).getActiveVehicle().getUsername()
                    + "&vehicle_id=" + AppController.getInstance(context).getActiveVehicle().getId()
                    + "&trip_msg=" + msg + "&mode=" + mode;
            JsonReader jsonReader = new JsonReader(url);
            jsonReader.sendAsyncGetRequest();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            // send error msg to user
            String url = AppController.END_POINT + "/send_trip_msg_to_user.php?username="
                    + AppController.getInstance(context).getActiveVehicle().getUsername()
                    + "&vehicle_id=" + AppController.getInstance(context).getActiveVehicle().getId()
                    + "&trip_msg=error";
            JsonReader jsonReader = new JsonReader(url);
            jsonReader.sendAsyncGetRequest();
        }
    }
}
