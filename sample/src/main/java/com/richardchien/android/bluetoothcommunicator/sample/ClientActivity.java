package com.richardchien.android.bluetoothcommunicator.sample;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.richardchien.android.bluetoothcommunicator.BluetoothClient;
import com.richardchien.android.bluetoothcommunicator.listener.ConnectListener;
import com.richardchien.android.bluetoothcommunicator.listener.OnLoseConnectionListener;
import com.richardchien.android.bluetoothcommunicator.listener.OnReceiveListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ClientActivity extends AppCompatActivity {
    @Bind(R.id.btn_choose_device)
    Button mBtnChoose;

    @Bind(R.id.btn_connect)
    Button mBtnConnect;

    @Bind(R.id.btn_send)
    Button mBtnSend;

    @Bind(R.id.edit_text)
    EditText mEditText;

    private BluetoothClient mClient;
    private BluetoothDevice mDevice;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        ButterKnife.bind(this);

        mClient = new BluetoothClient(mHandler, new OnReceiveListener() {
            @Override
            public void onReceiveLine(String line, BluetoothDevice device) {
                mBtnSend.setText(line);
            }
        }, new OnLoseConnectionListener() {
            @Override
            public void onLoseConnection(BluetoothDevice device) {
                Toast.makeText(ClientActivity.this, "Lose connection with " + device.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        mBtnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseDevice();
            }
        });

        mBtnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect();
            }
        });

        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send();
            }
        });

        if (!mClient.isBluetoothSupported()) {
            this.finish();
        } else if (!mClient.isBluetoothEnabled()) {
            mClient.startActivityForEnablingBluetooth(this, 1);
        }
    }

    private void chooseDevice() {
        final List<BluetoothDevice> deviceList = new ArrayList<>();
        final List<String> list = new ArrayList<>();
        Set<BluetoothDevice> pairedDevices = mClient.getPairedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                deviceList.add(device);
                list.add(device.getName() + ": " + device.getAddress());
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Device");
        int index = -1;
        if (mDevice != null) {
            index = deviceList.indexOf(mDevice);
        }
        builder.setSingleChoiceItems(list.toArray(new String[list.size()]), index, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (which >= 0 && which < list.size()) {
                    mDevice = deviceList.get(which);
                    mBtnChoose.setText(mDevice.getName());
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void connect() {
        if (mDevice == null) {
            Toast.makeText(this, "Choose device first", Toast.LENGTH_SHORT).show();
        } else if (!mClient.isConnectedToDevice(mDevice)) {
            mClient.connectToDevice(mDevice, MainActivity.MY_UUID, new ConnectListener() {
                @Override
                public void onSucceed(BluetoothDevice device) {
                    Toast.makeText(ClientActivity.this, "Connection succeeded", Toast.LENGTH_SHORT).show();
                    mBtnConnect.setText("Disconnect");
                }

                @Override
                public void onFail(BluetoothDevice device) {
                    Toast.makeText(ClientActivity.this, "Connection failed", Toast.LENGTH_SHORT).show();
                }
            });
            Toast.makeText(this, "Start connection", Toast.LENGTH_SHORT).show();
        } else {
            mClient.disconnectToDevice(mDevice);
            mBtnConnect.setText("Connect");
        }
    }

    private void send() {
        mClient.sendLine(mEditText.getText().toString(), mDevice);
    }
}
