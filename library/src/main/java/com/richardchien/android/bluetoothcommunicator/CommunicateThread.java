package com.richardchien.android.bluetoothcommunicator;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * BluetoothCommunicator
 * Created by richard on 16/1/11.
 */
public class CommunicateThread extends Thread {
    private BluetoothSocket mSocket;
    private BluetoothDevice mDevice;
    private BluetoothCommunicator mCommunicator;
    private InputStream mInStream;
    private OutputStream mOutStream;

    public CommunicateThread(BluetoothCommunicator communicator, BluetoothSocket socket) {
        mCommunicator = communicator;
        mSocket = socket;
        mDevice = socket.getRemoteDevice();

        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (Exception e) {
            fail();
        }

        mInStream = tmpIn;
        mOutStream = tmpOut;
    }

    @Override
    public void run() {
        if (Thread.currentThread().isInterrupted()) {
            return;
        }

        InputStreamReader isr;
        BufferedReader br;
        try {
            isr = new InputStreamReader(mInStream);
            br = new BufferedReader(isr);
            String line;
            while (!Thread.currentThread().isInterrupted()) {
                line = br.readLine();
                if (line != null) {
                    mCommunicator.receiveLine(line, mDevice);
                }
            }
        } catch (Exception e) {
            fail();
        }
    }

    /**
     * Write a line to remote device
     *
     * @param string Line to write
     */
    public void writeLine(String string) {
        PrintWriter pw = new PrintWriter(mOutStream, true);
        pw.println(string);
    }

    /**
     * Cancel the thread
     */
    public void cancel() {
        try {
            mSocket.close();
        } catch (Exception ignored) {
        }
        try {
            mInStream.close();
        } catch (Exception ignored) {
        }
        try {
            mOutStream.close();
        } catch (Exception ignored) {
        }
        interrupt();
    }

    /**
     * Cancel the thread and notify the communicator
     */
    private void fail() {
        if (!Thread.currentThread().isInterrupted()) {
            mCommunicator.loseConnection(mDevice);
        }
        cancel();
    }
}
