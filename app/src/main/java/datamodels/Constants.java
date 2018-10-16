package datamodels;

/**
 * Created by Shamyyoun on 2/8/2015.
 */
public class Constants {
    // json response constants
    public static final String JSON_MSG_ERROR = "error";
    public static final String JSON_MSG_SUCCESS = "success";

    // sp constants
    public static final String SP_RESPONSES = "responses";
    public static final String SP_KEY_ACTIVE_VEHICLE_RESPONSE = "active_vehicle_response";
    public static final String SP_VEHICLE_DATA = "vehicle_data";
    public static final String SP_KEY_TRACKING_INTERVAL = "tracking_interval";
    public static final String SP_KEY_TRACKING_TRIP = "tracking_trip";
    public static final String SP_CONFIG = "config";
    public static final String SP_KEY_SERVICE_RUNNING = "service_running";
    public static final String SP_KEY_REG_ID = "reg_id";

    // notifications keys
    public static final int NOTI_ENABLE_GPS = 1;
    public static final int NOTI_ENABLE_INTERNET = 2;
    public static final int NOTI_VEHICLE_ACTION = 3;
    public static final int NOTI_TRACKING_INTERVAL_UPDATED = 4;
    public static final int NOTI_TRIP_ACTION = 5;
    public static final int NOTI_BATTERY_ACTION = 6;

    // receivers request codes
    public static final int RECEIVER_LOCATION_UPDATER = 1;

    // push notifications keys
    public static final String PUSHN_TURN = "turn";
    public static final String PUSHN_UPDATE_TRACKING_INTERVAL = "tracking_interval_updated";
    public static final String PUSHN_TRIP = "trip";
}