package com.mahmoudelshamy.gpstracking.vehicleapp;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.devspark.appmsg.AppMsg;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import datamodels.Cache;
import datamodels.Constants;
import datamodels.Vehicle;
import json.JsonReader;
import json.VehicleHandler;
import utils.GPSUtil;
import utils.InternetUtil;


public class MainActivity extends ActionBarActivity {
    private View viewStart;
    private TextView textStart;
    private View viewHelp;
    private TextView textHelp;
    private Animation startAnimation;
    private Animation helpAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initComponents();
    }

    /**
     * method, used to initialize components
     */
    private void initComponents() {
        viewStart = findViewById(R.id.view_start);
        textStart = (TextView) findViewById(R.id.text_start);
        viewHelp = findViewById(R.id.view_help);
        textHelp = (TextView) findViewById(R.id.text_help);
        startAnimation = AnimationUtils.loadAnimation(this, R.anim.rotation);
        helpAnimation = AnimationUtils.loadAnimation(this, R.anim.rotation);

        // customize fonts
        Typeface typeface = Typeface.createFromAsset(getAssets(), "roboto_l.ttf");
        textStart.setTypeface(typeface);
        textHelp.setTypeface(typeface);

        // set initials
        if (Cache.getServiceRunning(getApplicationContext())) {
            // service is running
            textStart.setText(R.string.stop);
        }

        // add listeners
        viewStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check if service running
                if (Cache.getServiceRunning(getApplicationContext())) {
                    // update cached flag
                    Cache.updateServiceRunning(getApplicationContext(), false);

                    // service is running >> stop it
                    AppController.stopLocationUpdater(getApplicationContext());

                    // change UI
                    textStart.setText(R.string.start);
                    startAnimation.setRepeatCount(0);
                    textStart.startAnimation(startAnimation);
                } else {
                    // not running >> validate settings
                    if (!InternetUtil.isConnected(getApplicationContext())) {
                        // show error
                        AppMsg.cancelAll();
                        AppMsg.makeText(MainActivity.this, R.string.enable_internet_and_try_again, AppMsg.STYLE_CONFIRM).show();

                        return;
                    }
                    if (!GPSUtil.isGPSEnabled(MainActivity.this)) {
                        // show error
                        AppMsg.cancelAll();
                        AppMsg.makeText(MainActivity.this, R.string.enable_gps_and_try_again, AppMsg.STYLE_CONFIRM).show();

                        return;
                    }

                    // update cached flag
                    Cache.updateServiceRunning(getApplicationContext(), true);

                    // settings is ok >> start it
                    AppController.startLocationUpdater(getApplicationContext(), System.currentTimeMillis());

                    // change UI
                    textStart.setText(R.string.stop);
                    startAnimation.setRepeatCount(0);
                    textStart.startAnimation(startAnimation);
                }
            }
        });
        viewHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new HelpTask().execute();
            }
        });

        // run login task to sync vehicle's data
        new ReLoginTask().execute();
    }

    /**
     * sub class, used to send help request to vehicle's owner
     */
    private class HelpTask extends AsyncTask<Void, Void, Void> {
        private Vehicle vehicle;
        private String response;

        public HelpTask() {
            vehicle = AppController.getInstance(getApplicationContext()).getActiveVehicle();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // check internet
            if (!InternetUtil.isConnected(getApplicationContext())) {
                // show error
                AppMsg.cancelAll();
                AppMsg.makeText(MainActivity.this, R.string.no_internet_connection, AppMsg.STYLE_CONFIRM).show();

                cancel(true);
                return;
            }

            // conditions is okay >> start progress
            helpAnimation.setRepeatCount(Animation.INFINITE);
            textHelp.startAnimation(helpAnimation);
            viewHelp.setEnabled(false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // create json reader
            String url = AppController.END_POINT + "/send_help.php?username=" + vehicle.getUsername() + "&vehicle_id=" + vehicle.getId();
            JsonReader jsonReader = new JsonReader(url);

            // execute request
            response = jsonReader.sendGetRequest();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // stop progress
            helpAnimation.setRepeatCount(0);
            viewHelp.setEnabled(true);

            // validate response
            if (response == null) {
                // show error
                AppMsg.cancelAll();
                AppMsg.makeText(MainActivity.this, R.string.connection_error_try_again, AppMsg.STYLE_CONFIRM).show();
                return;
            }

            // get success key from response

            boolean success;
            try {
                JSONObject jsonObject = new JSONObject(response);
                success = jsonObject.getInt("success") == 1 ? true : false;
            } catch (JSONException e) {
                success = false;
            }

            // check success
            if (success) {
                // show success msg
                Toast.makeText(getApplicationContext(), R.string.help_request_sent_successfully, Toast.LENGTH_LONG).show();
            } else {
                // show error
                AppMsg.cancelAll();
                AppMsg.makeText(MainActivity.this, R.string.unable_to_contact_vehicles_owner, AppMsg.STYLE_CONFIRM).show();
            }
        }
    }

    /**
     * sub class, used to send login request to sync vehicle's login info with server
     */
    private class ReLoginTask extends AsyncTask<Void, Void, Void> {
        private MainActivity activity;
        private Vehicle activeVehicle;
        private String response;

        private ReLoginTask() {
            activity = MainActivity.this;
            activeVehicle = AppController.getInstance(activity).getActiveVehicle();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // create json reader
            String url = AppController.END_POINT + "/vehicle_login.php";
            JsonReader jsonReader = new JsonReader(url);

            // prepare parameters
            List<NameValuePair> parameters = new ArrayList<>(2);
            parameters.add(new BasicNameValuePair("id", activeVehicle.getId()));
            parameters.add(new BasicNameValuePair("password", activeVehicle.getPassword()));

            // execute request
            response = jsonReader.sendGetRequest(parameters);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // ensure response is not null
            if (response == null) {
                return;
            }

            if (response.equals(Constants.JSON_MSG_ERROR)) {
                // update cache
                Cache.removeActiveVehicleResponse(activity);
                Cache.removeTrackingInterval(activity);
                Cache.removeRegId(activity);
                Cache.updateServiceRunning(activity, false);

                // make vehicle's object null at runtime
                AppController.getInstance(activity).setActiveVehicle(null);

                // invalid vehicle_id or password >> show msgs
                Toast.makeText(activity, R.string.vehicle_data_changed, Toast.LENGTH_LONG).show();

                // close MainActivity and open LoginActivity
                Intent intent = new Intent(activity, LoginActivity.class);
                activity.startActivity(intent);
                finish();

                return;
            }

            // --response is valid, handle it--
            VehicleHandler handler = new VehicleHandler(response);
            Vehicle vehicle = handler.handle();

            // check handling operation
            if (vehicle == null) {
                return;
            }

            // --vehicle object is valid--
            // save it & cache response
            AppController.getInstance(activity.getApplicationContext()).setActiveVehicle(vehicle);
            Cache.updateActiveVehicleResponse(activity.getApplicationContext(), response);
        }
    }

    /**
     * overriden method
     */
    @Override
    protected void onDestroy() {
        AppMsg.cancelAll(this);
        super.onDestroy();
    }
}
