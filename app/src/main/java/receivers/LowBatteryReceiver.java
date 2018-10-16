package receivers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;

import com.mahmoudelshamy.gpstracking.vehicleapp.AppController;
import com.mahmoudelshamy.gpstracking.vehicleapp.R;

import java.io.IOException;
import java.util.Set;

import datamodels.Constants;
import services.BluetoothSender;
import utils.NotificationUtil;

/**
 * Created by Shamyyoun on 6/23/2015.
 */
public class LowBatteryReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        final Handler handler = new Handler();
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
                            NotificationUtil.show(context, Constants.NOTI_BATTERY_ACTION, R.string.bluetooth_not_supported, soundUri);
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
                            NotificationUtil.show(context, Constants.NOTI_BATTERY_ACTION, R.string.turn_on_bluetooth, soundUri);
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
                        BluetoothSender bluetoothSender = new BluetoothSender(device, true, mBluetoothAdapter, "B");
                        try {
                            bluetoothSender.send();
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
                            NotificationUtil.show(context, Constants.NOTI_BATTERY_ACTION, R.string.turn_on_bluetooth, soundUri);
                        }
                    });
                }
            }
        }).run();
    }
}
