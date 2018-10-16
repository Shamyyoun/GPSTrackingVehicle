package com.mahmoudelshamy.gpstracking.vehicleapp;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import datamodels.Cache;
import datamodels.Constants;
import datamodels.Vehicle;
import json.JsonReader;
import json.VehicleHandler;
import utils.InternetUtil;
import utils.ViewUtil;


public class LoginActivity extends ActionBarActivity {
    private View layoutLogin;
    private EditText textId;
    private EditText textPassword;
    private Button buttonLogin;
    private ProgressBar progressBar;

    private List<AsyncTask> tasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initComponents();
    }

    /**
     * method, used to initialize components
     */
    private void initComponents() {
        layoutLogin = findViewById(R.id.layout_login);
        textId = (EditText) findViewById(R.id.text_id);
        textPassword = (EditText) findViewById(R.id.text_password);
        buttonLogin = (Button) findViewById(R.id.button_login);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        tasks = new ArrayList<AsyncTask>();

        // customize fonts
        Typeface typeface = Typeface.createFromAsset(getAssets(), "roboto_l.ttf");
        textId.setTypeface(typeface);
        textPassword.setTypeface(typeface);
        buttonLogin.setTypeface(typeface);

        // customize hints
        String color = String.format("#%06X", 0xFFFFFF & getResources().getColor(R.color.dark_white));
        textId.setHint(Html.fromHtml("<font color='" + color + "'>" + getString(R.string.vehicle_id) + "</font>"));
        textPassword.setHint(Html.fromHtml("<font color='" + color + "'>" + getString(R.string.password) + "</font>"));

        // add listeners
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = textId.getText().toString();
                String password = textPassword.getText().toString();
                new LoginTask(username, password).execute();
            }
        });
    }

    /**
     * overriden method
     */
    @Override
    protected void onDestroy() {
        // stop all running tasks
        for (AsyncTask task : tasks) {
            task.cancel(true);
        }

        super.onDestroy();
    }

    /**
     * sub class, used to send login request
     */
    private class LoginTask extends AsyncTask<Void, Void, Void> {
        private String id;
        private String password;

        private LoginActivity activity;
        private String response;

        private LoginTask(String id, String password) {
            this.id = id;
            this.password = password;

            activity = LoginActivity.this;

            tasks.add(this); // save reference to this task to destroy it if required
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // validate inputs
            id = id.trim();
            password = password.trim();
            if (id.isEmpty()) {
                textId.setText("");
                textId.setError(getString(R.string.vehicle_id_cant_be_empty));

                cancel(true);
                return;
            }
            if (password.isEmpty()) {
                textPassword.setText("");
                textPassword.setError(getString(R.string.password_cant_be_empty));

                cancel(true);
                return;
            }

            // check internet connection
            if (!InternetUtil.isConnected(activity)) {
                showError(R.string.no_internet_connection);

                cancel(true);
                return;
            }

            // all conditions is true >> show progress
            showProgress(true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // create json reader
            String url = AppController.END_POINT + "/vehicle_login.php";
            JsonReader jsonReader = new JsonReader(url);

            // prepare parameters
            List<NameValuePair> parameters = new ArrayList<>(2);
            parameters.add(new BasicNameValuePair("id", id));
            parameters.add(new BasicNameValuePair("password", password));

            // execute request
            response = jsonReader.sendGetRequest(parameters);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // hide progress
            showProgress(false);

            // ensure response is not null
            if (response == null) {
                showError(R.string.connection_error_try_again);

                return;
            }

            if (response.equals(Constants.JSON_MSG_ERROR)) {
                // invalid vehicle_id or password
                showError(R.string.invalid_vehicle_id_or_password);

                return;
            }

            // --response is valid, handle it--
            VehicleHandler handler = new VehicleHandler(response);
            Vehicle vehicle = handler.handle();

            // check handling operation
            if (vehicle == null) {
                showError(R.string.connection_error_try_again);

                return;
            }

            // --vehicle object is valid--
            // save it & cache response
            AppController.getInstance(activity.getApplicationContext()).setActiveVehicle(vehicle);
            Cache.updateActiveVehicleResponse(activity.getApplicationContext(), response);

            // register vehicle and send his reg_id to server
            AppController.registerToGCM(activity);

            // goto main activity
            Intent intent = new Intent(activity, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.main_enter, R.anim.login_exit);
        }

        private void showProgress(boolean show) {
            ViewUtil.showView(progressBar, show);
            ViewUtil.showView(layoutLogin, !show, View.INVISIBLE);
        }

        private void showError(int errorMsgRes) {
            Toast.makeText(activity, errorMsgRes, Toast.LENGTH_LONG).show();
        }
    }
}
