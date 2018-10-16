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
public class InternetReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // check if service is running
        if (Cache.getServiceRunning(context)) {
            // check internet state
            if (InternetUtil.isConnected(context)) {
                // cancel enable internet notifications if exists
                NotificationUtil.cancel(context, Constants.NOTI_ENABLE_INTERNET);

                // check gps state
                if (GPSUtil.isGPSEnabled(context)) {
                    // start location updater right now
                    AppController.startLocationUpdater(context, System.currentTimeMillis());
                } else {
                    // show notification
                    Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    NotificationUtil.show(context, Constants.NOTI_ENABLE_GPS, R.string.enable_gps, soundUri);
                }
            } else {
                // show notification
                Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                NotificationUtil.show(context, Constants.NOTI_ENABLE_INTERNET, R.string.enable_internet, soundUri);

                // stop location updater
                AppController.stopLocationUpdater(context);
            }
        }
    }
}
