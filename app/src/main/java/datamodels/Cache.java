package datamodels;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Shamyyoun on 3/29/2015.
 */
public class Cache {
    /**
     * method used to update active vehicle response in SP
     */
    public static void updateActiveVehicleResponse(Context context, String value) {
        updateCachedString(context, Constants.SP_RESPONSES, Constants.SP_KEY_ACTIVE_VEHICLE_RESPONSE, value);
    }

    /**
     * method used to get active vehicle response from SP
     */
    public static String getActiveVehicleResponse(Context context) {
        return getCachedString(context, Constants.SP_RESPONSES, Constants.SP_KEY_ACTIVE_VEHICLE_RESPONSE);
    }

    /**
     * method, used to remove active vehicle rsponse from SP
     */
    public static void removeActiveVehicleResponse(Context context) {
        removeCachedValue(context, Constants.SP_RESPONSES, Constants.SP_KEY_ACTIVE_VEHICLE_RESPONSE);
    }

    /**
     * method used to update tracking interval in SP
     */
    public static void updateTrackingInterval(Context context, int value) {
        updateCachedInt(context, Constants.SP_VEHICLE_DATA, Constants.SP_KEY_TRACKING_INTERVAL, value);
    }

    /**
     * method used to get tracking interval from SP
     */
    public static int getTrackingInterval(Context context) {
        return getCachedInt(context, Constants.SP_VEHICLE_DATA, Constants.SP_KEY_TRACKING_INTERVAL);
    }

    /**
     * method, used to remove tracking interval from SP
     */
    public static void removeTrackingInterval(Context context) {
        removeCachedValue(context, Constants.SP_VEHICLE_DATA, Constants.SP_KEY_TRACKING_INTERVAL);
    }

    /**
     * method used to update tracking_trip flag in SP
     */
    public static void updateTrackingTrip(Context context, boolean value) {
        updateCachedBoolean(context, Constants.SP_VEHICLE_DATA, Constants.SP_KEY_TRACKING_TRIP, value);
    }

    /**
     * method used to get tracking_trip flag from SP
     */
    public static boolean getTrackingTrip(Context context) {
        return getCachedBoolean(context, Constants.SP_VEHICLE_DATA, Constants.SP_KEY_TRACKING_TRIP);
    }

    /**
     * method, used to remove tracking_trip flag from SP
     */
    public static void removeTrackingTrip(Context context) {
        removeCachedValue(context, Constants.SP_VEHICLE_DATA, Constants.SP_KEY_TRACKING_TRIP);
    }

    /**
     * method used to update active user reg_id in SP
     */
    public static void updateRegId(Context context, String value) {
        updateCachedString(context, Constants.SP_CONFIG, Constants.SP_KEY_REG_ID, value);
    }

    /**
     * method used to get active user reg_id from SP
     */
    public static String getRegId(Context context) {
        return getCachedString(context, Constants.SP_CONFIG, Constants.SP_KEY_REG_ID);
    }

    /**
     * method, used to remove reg_id from SP
     */
    public static void removeRegId(Context context) {
        removeCachedValue(context, Constants.SP_CONFIG, Constants.SP_KEY_REG_ID);
    }

    /**
     * method used to update service running flag in SP
     */
    public static void updateServiceRunning(Context context, boolean value) {
        updateCachedBoolean(context, Constants.SP_CONFIG, Constants.SP_KEY_SERVICE_RUNNING, value);
    }

    /**
     * method used to get service running flag from SP
     */
    public static boolean getServiceRunning(Context context) {
        return getCachedBoolean(context, Constants.SP_CONFIG, Constants.SP_KEY_SERVICE_RUNNING);
    }

    /**
     * method used to update string in SP
     */
    private static void updateCachedString(Context context, String spName, String key, String value) {
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.commit();
    }

    /**
     * method used to get cached string from SP
     */
    private static String getCachedString(Context context, String spName, String key) {
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        String value = sp.getString(key, null);

        return value;
    }


    /**
     * method used to update boolean in SP
     */
    private static void updateCachedBoolean(Context context, String spName, String key, boolean value) {
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    /**
     * method used to get cached boolean from SP
     */
    private static boolean getCachedBoolean(Context context, String spName, String key) {
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        boolean value = sp.getBoolean(key, false);

        return value;
    }

    /**
     * method used to update int in SP
     */
    private static void updateCachedInt(Context context, String spName, String key, int value) {
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    /*
     * method, used to get cached int from SP
     */
    private static int getCachedInt(Context context, String spName, String valueName) {
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        int value = sp.getInt(valueName, -1);

        return value;
    }

    /**
     * method used to remove value from SP
     */
    private static void removeCachedValue(Context context, String spName, String valueName) {
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(valueName);
        editor.commit();
    }
}
