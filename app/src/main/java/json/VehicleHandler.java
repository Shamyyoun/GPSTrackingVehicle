package json;

import org.json.JSONException;
import org.json.JSONObject;

import datamodels.Vehicle;

public class VehicleHandler {
    private String response;

    public VehicleHandler(String response) {
        this.response = response;
    }

    public Vehicle handle() {
        try {
            JSONObject jsonObject = new JSONObject(response);
            Vehicle vehicle = handleVehicle(jsonObject);
            return vehicle;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Vehicle handleVehicle(JSONObject jsonObject) {
        Vehicle vehicle;
        try {
            String id = jsonObject.getString("id");
            String password = jsonObject.getString("password");
            String name = jsonObject.getString("name");
            String purpose = jsonObject.getString("purpose");
            int licenceNumber = jsonObject.getInt("licence_number");
            int number = jsonObject.getInt("number");
            String color = jsonObject.getString("color");
            String model = jsonObject.getString("model");
            int year = jsonObject.getInt("year");
            String brand = jsonObject.getString("brand");
            int trackingInterval = jsonObject.getInt("tracking_interval");
            String username = jsonObject.getString("username");

            vehicle = new Vehicle(id, password, name, purpose, licenceNumber,
                    number, color, model, year, brand, trackingInterval, username);
        } catch (JSONException e) {
            vehicle = null;
            e.printStackTrace();
        }

        return vehicle;
    }
}
