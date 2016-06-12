package im.r_c.android.bluetoothcommunicator.listener;

import android.bluetooth.BluetoothDevice;

/**
 * BluetoothCommunicator
 * Created by richard on 16/1/11.
 */
public interface ListenStateListener {
    void onAccept(BluetoothDevice device);

    void onFail();
}
