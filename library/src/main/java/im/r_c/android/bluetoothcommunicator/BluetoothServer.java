package im.r_c.android.bluetoothcommunicator;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.util.UUID;

import im.r_c.android.bluetoothcommunicator.listener.ListenStateListener;
import im.r_c.android.bluetoothcommunicator.listener.OnLoseConnectionListener;
import im.r_c.android.bluetoothcommunicator.listener.OnReceiveListener;

/**
 * BluetoothCommunicator
 * Created by richard on 16/1/11.
 * <p/>
 * Class used to communicate through Bluetooth as a server.
 */
public class BluetoothServer extends BluetoothCommunicator {
    private AcceptThread mAcceptThread;

    /**
     * BluetoothServer constructor without listeners.
     *
     * @param handler Handler on UI thread.
     */
    public BluetoothServer(Handler handler) {
        super(handler);
    }

    /**
     * BluetoothServer constructor with listeners.
     *
     * @param handler                  Handler on UI thread.
     * @param onReceiveListener        Listener for receiving message or null.
     * @param onLoseConnectionListener Listener for losing connection or null.
     */
    public BluetoothServer(Handler handler, OnReceiveListener onReceiveListener, OnLoseConnectionListener onLoseConnectionListener) {
        super(handler, onReceiveListener, onLoseConnectionListener);
    }

    /**
     * Check if the server is listening for connection request.
     *
     * @return Is listening or not.
     */
    public boolean isListening() {
        return mAcceptThread != null && mAcceptThread.isAlive();
    }

    /**
     * Start listening for connection request.
     *
     * @param serviceName Service name (Using app name is OK).
     * @param uuid        The app's UUID string, should be the same as the client side.
     * @param listener    Listener or null.
     */
    public void startListening(String serviceName, UUID uuid, ListenStateListener listener) {
        if (isListening()) {
            return;
        }

        if (mAcceptThread != null) {
            mAcceptThread.cancel();
        }
        mAcceptThread = new AcceptThread(serviceName, uuid, listener);
        mAcceptThread.start();
    }

    /**
     * Stop listening for connection request.
     */
    public void stopListening() {
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
        }
    }

    /**
     * Thread to accept connection request.
     */
    private class AcceptThread extends Thread {
        private BluetoothServerSocket mmServerSocket;
        private ListenStateListener mmListener;

        public AcceptThread(String name, UUID uuid, ListenStateListener listener) {
            mmListener = listener;

            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(name, uuid);
            } catch (Exception e) {
                fail();
            }
            mmServerSocket = tmp;
        }

        @Override
        public void run() {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            BluetoothSocket socket = null;

            // Keep listening
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    socket = mmServerSocket.accept();
                } catch (Exception ignored) {
                }

                // If a connection was accepted
                if (socket != null) {
                    startNewCommunicateThread(socket);
                    accept(socket.getRemoteDevice());
                    socket = null;
                }
            }
        }

        /**
         * Cancel the thread.
         */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (Exception ignored) {
            }
            interrupt();
        }

        /**
         * Call onAccept method on UI thread.
         *
         * @param device Device accepted.
         */
        private void accept(final BluetoothDevice device) {
            if (mmListener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mmListener.onAccept(device);
                    }
                });
            }
        }

        /**
         * Cancel the thread and call onFailed method on UI thread.
         */
        private void fail() {
            cancel();

            if (mmListener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mmListener.onFail();
                    }
                });
            }
        }
    }
}
