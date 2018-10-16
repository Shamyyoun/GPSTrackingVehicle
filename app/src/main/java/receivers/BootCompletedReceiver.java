package receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;

import com.mahmoudelshamy.gpstracking.vehicleapp.AppController;
import com.mahmoudelshamy.gpstracking.vehicleapp.R;

import datamodels.Cache;
import datamodels.Constants;
import utils.GPSUtil;
import utils.InternetUtil;
import utils.NotificationUtil;

/**
 * Created by Shamyyoun on 4/28/2015.
 */
public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // check if service is running
        if (Cache.getServiceRunning(context)) {
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
                // start location updater right now
                AppController.startLocationUpdater(context, System.currentTimeMillis());
            }
        }
    }
}
