package com.richardchien.android.bluetoothcommunicator.sample;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.richardchien.android.bluetoothcommunicator.BluetoothServer;
import com.richardchien.android.bluetoothcommunicator.listener.OnListenListener;
import com.richardchien.android.bluetoothcommunicator.listener.OnLoseConnectionListener;
import com.richardchien.android.bluetoothcommunicator.listener.OnReceiveListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ServerActivity extends AppCompatActivity {
    @Bind(R.id.btn_start)
    Button mBtnStart;

    @Bind(R.id.btn_connected)
    Button mBtnConnected;

    @Bind(R.id.text_view)
    TextView mTextView;

    private BluetoothServer mServer;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        ButterKnife.bind(this);

        mServer = new BluetoothServer(mHandler, new OnReceiveListener() {
            @Override
            public void onReceiveLine(String line, BluetoothDevice device) {
                mTextView.append(line);
                mTextView.append("\n");
            }
        }, new OnLoseConnectionListener() {
            @Override
            public void onLoseConnection(BluetoothDevice device) {
                Toast.makeText(ServerActivity.this, "Lose connection with " + device.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start();
            }
        });

        mBtnConnected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConnected();
            }
        });
    }

    private void start() {
        if (mServer.isListening()) {
            mServer.stopListening();
            mBtnStart.setText("Listen");
        } else {
            mServer.startListening(MainActivity.NAME, MainActivity.MY_UUID, new OnListenListener() {
                @Override
                public void onAccept(BluetoothDevice device) {
                    Toast.makeText(ServerActivity.this, "Accept connection with " + device.getName(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFail() {
                    Toast.makeText(ServerActivity.this, "Listen failed", Toast.LENGTH_SHORT).show();
                }
            });
            mBtnStart.setText("Stop");
        }
    }

    private void showConnected() {
        final List<String> list = new ArrayList<>();
        Set<BluetoothDevice> connectedDevices = mServer.getConnectedDevices();
        if (connectedDevices.size() > 0) {
            for (BluetoothDevice device : connectedDevices) {
                list.add(device.getName() + ": " + device.getAddress());
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Device");
        builder.setItems(list.toArray(new String[list.size()]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
