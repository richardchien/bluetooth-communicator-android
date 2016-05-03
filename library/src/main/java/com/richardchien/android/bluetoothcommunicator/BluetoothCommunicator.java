package com.richardchien.android.bluetoothcommunicator;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;

import com.richardchien.android.bluetoothcommunicator.listener.OnLoseConnectionListener;
import com.richardchien.android.bluetoothcommunicator.listener.OnReceiveListener;
import com.richardchien.android.bluetoothcommunicator.listener.SendListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * BluetoothCommunicator
 * Created by richard on 16/1/11.
 * <p/>
 * Base class of client and server classes, implements some common communicate methods.
 */
public abstract class BluetoothCommunicator {
    protected BluetoothAdapter mBluetoothAdapter;
    protected Handler mHandler;
    protected Map<BluetoothDevice, CommunicateThread> mConnections = new HashMap<>();
    private OnReceiveListener mOnReceiveListener;
    private OnLoseConnectionListener mOnLoseConnectionListener;

    /**
     * BluetoothCommunicator constructor without listeners.
     *
     * @param handler Handler on UI thread.
     */
    public BluetoothCommunicator(Handler handler) {
        mHandler = handler;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * BluetoothCommunicator constructor with listeners.
     *
     * @param handler                  Handler on UI thread.
     * @param onReceiveListener        Listener for receiving message or null.
     * @param onLoseConnectionListener Listener for losing connection or null.
     */
    public BluetoothCommunicator(Handler handler, OnReceiveListener onReceiveListener, OnLoseConnectionListener onLoseConnectionListener) {
        mHandler = handler;
        mOnReceiveListener = onReceiveListener;
        mOnLoseConnectionListener = onLoseConnectionListener;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * Setter of mOnReceiveListener.
     *
     * @param listener Listener for receiving message.
     */
    public void setOnReceiveListener(OnReceiveListener listener) {
        mOnReceiveListener = listener;
    }

    /**
     * Setter of mOnLoseConnectionListener.
     *
     * @param listener Listener for losing connection.
     */
    public void setOnLoseConnectionListener(OnLoseConnectionListener listener) {
        mOnLoseConnectionListener = listener;
    }

    /**
     * Check if the device support Bluetooth.
     *
     * @return Support or not.
     */
    public boolean isBluetoothSupported() {
        return mBluetoothAdapter != null;
    }

    /**
     * Check if Bluetooth is enabled.
     *
     * @return Enabled or not.
     */
    public boolean isBluetoothEnabled() {
        return mBluetoothAdapter.isEnabled();
    }

    /**
     * Start an activity to ask the user to enable Bluetooth.
     * You should check the result in onActivityResult method.
     *
     * @param activity    Context activity.
     * @param requestCode Request code.
     */
    public void startActivityForEnablingBluetooth(Activity activity, int requestCode) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * Get set of connected devices.
     *
     * @return Set of connected devices.
     */
    public Set<BluetoothDevice> getConnectedDevices() {
        return mConnections.keySet();
    }

    /**
     * Check if remote device is connected.
     *
     * @param device Remote device to check.
     * @return Connected or not.
     */
    public boolean isConnectedToDevice(BluetoothDevice device) {
        return mConnections.containsKey(device);
    }

    /**
     * Send a string line to all devices connected without listener.
     *
     * @param line Line to send.
     */
    public void sendLineToAll(String line) {
        sendLineToAll(line, null);
    }

    /**
     * Send a string line to all devices connected with listener.
     *
     * @param line     Line to send.
     * @param listener Listener.
     */
    public void sendLineToAll(String line, final SendListener listener) {
        for (final BluetoothDevice device : mConnections.keySet()) {
            mConnections.get(device).writeLine(line);

            if (listener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onSucceed(device);
                    }
                });
            }
        }
    }

    /**
     * Send a string line to specific device without listener.
     *
     * @param line   Line to send.
     * @param device Device to send.
     */
    public void sendLine(String line, final BluetoothDevice device) {
        sendLine(line, device, null);
    }

    /**
     * Send a string line to specific device with listener.
     *
     * @param line     Line to send.
     * @param device   Device to send.
     * @param listener Listener.
     */
    public void sendLine(String line, final BluetoothDevice device, final SendListener listener) {
        CommunicateThread thread = mConnections.get(device);

        if (thread == null) {
            if (listener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onFail(device);
                    }
                });
            }
            return;
        }

        thread.writeLine(line);

        if (listener != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onSucceed(device);
                }
            });
        }
    }

    /**
     * Call onReceiveLine method on UI thread.
     * Automatically called by CommunicateThread.
     *
     * @param line   String line received.
     * @param device Device where string received from.
     */
    public void receiveLine(final String line, final BluetoothDevice device) {
        if (mOnReceiveListener != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mOnReceiveListener.onReceiveLine(line, device);
                }
            });
        }
    }

    /**
     * Disconnect to device.
     *
     * @param device Device to disconnect.
     */
    public void disconnectToDevice(BluetoothDevice device) {
        if (mConnections.containsKey(device)) {
            mConnections.get(device).cancel();
            mConnections.remove(device);
        }
    }

    /**
     * Call onLoseConnection method on UI thread.
     * Automatically called by CommunicateThread.
     *
     * @param device Device which lose connection to.
     */
    public void loseConnection(final BluetoothDevice device) {
        mConnections.remove(device);

        if (mOnLoseConnectionListener != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mOnLoseConnectionListener.onLoseConnection(device);
                }
            });
        }
    }

    /**
     * Start a new communicate thread.
     *
     * @param socket Socket to communicate through.
     */
    protected void startNewCommunicateThread(BluetoothSocket socket) {
        CommunicateThread thread = new CommunicateThread(this, socket);
        mConnections.put(socket.getRemoteDevice(), thread);
        thread.start();
    }
}
