package com.richardchien.android.bluetoothcommunicator.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public static final UUID MY_UUID = UUID.fromString("99E4C09C-67C1-40E0-A11B-B1C888AE4B65");
    public static final String NAME = "BluetoothSample";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startClient(View view) {
        startActivity(new Intent(this, ClientActivity.class));
    }

    public void startServer(View view) {
        startActivity(new Intent(this, ServerActivity.class));
    }
}
