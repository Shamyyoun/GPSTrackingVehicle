package utils;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Shamyyoun on 5/11/2015.
 */
public class GPSUtil {
    /**
     * method, used to check if gps is enabled
     */
    public static boolean isGPSEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * method, used to get current location
     */
    public static Location getLocation(Context context, GoogleApiClient.ConnectionCallbacks connectionCallbacks) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(connectionCallbacks)
                .build();
        googleApiClient.connect();
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        googleApiClient.disconnect();
        return location;
    }
}
