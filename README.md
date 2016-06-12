# Bluetooth Communicator for Android

[![Release](https://jitpack.io/v/richardchien/bluetooth-communicator-android.svg)](https://jitpack.io/#richardchien/bluetooth-communicator-android)

Bluetooth Communicator is a framework that helps you to establish Bluetooth connections between devices (several clients and one server). It hasn't been completely tested yet, and if anything strange occurs to your app, please send me an issue.

## Usage

Add the following to your module's `build.gradle`:

```groovy
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}

dependencies {
    compile 'com.github.richardchien:bluetooth-communicator-android:v1.0.0'
}
```

### As a server

```java
// All two objects you need
private BluetoothServer mServer;
private Handler mHandler = new Handler();

// Initialize mServer
mServer = new BluetoothServer(mHandler, new OnReceiveListener() {
    @Override
    public void onReceiveLine(String line, BluetoothDevice device) {
        // Do something to handle with the line received from a remote device
    }
}, new OnLoseConnectionListener() {
    @Override
    public void onLoseConnection(BluetoothDevice device) {
        // Do something after losing the connection with specific device
    }
});

// Start listening for Bluetooth connection requests
if (!mServer.isBluetoothSupported()) {
    return;
} else if (!mServer.isBluetoothEnabled()) {
    mServer.startActivityForEnablingBluetooth(this, 1);
} else {
    // NAME: Name of your service. It's OK to be app name
    // MY_UUID: A unique id used on both client side and server side. See "http://developer.android.com/intl/zh-cn/guide/topics/connectivity/bluetooth.html#ConnectingAsAServer"
    mServer.startListening(NAME, MY_UUID, new ListenStateListener() {
        @Override
        public void onAccept(BluetoothDevice device) {
            // Do something after establishing connection with a new device
        }

        @Override
        public void onFail() {
            // Do something after listening task failed
        }
    });
}
```

### As a client

```java
// All three objects you need
private BluetoothClient mClient;
private BluetoothDevice mDevice;
private Handler mHandler = new Handler();

// Initialize mClient
mClient = new BluetoothClient(mHandler, new OnReceiveListener() {
    @Override
    public void onReceiveLine(String line, BluetoothDevice device) {
        // Do something to handle with the line received from a remote device
    }
}, new OnLoseConnectionListener() {
    @Override
    public void onLoseConnection(BluetoothDevice device) {
        // Do something after losing the connection with a specific devcice
    }
});

// Get paired devices
Set<BluetoothDevice> pairedDevices = mClient.getPairedDevices();
// Or start discovering new devices
mClient.startDiscovery(this, new OnNewDeviceFoundListener() {
    @Override
    public void onNewDeviceFound(BluetoothDevice device) {
        // Do something to handle with newly found device
    }
});
// Cancel discovery (MUST be called if startDiscovery was called, because it unregisters a broadcast receiver inside)
mClient.cancelDiscovery(this);

// Choose a device (a server) and connect to it
mDevice = //...
// MY_UUID: A unique id used on both client side and server side. See "http://developer.android.com/intl/zh-cn/guide/topics/connectivity/bluetooth.html#ConnectingAsAServer"
mClient.connectToDevice(mDevice, MY_UUID, new ConnectListener() {
    @Override
    public void onSucceed(BluetoothDevice device) {
        // Do something after establishing connection with a specific device
    }

    @Override
    public void onFail(BluetoothDevice device) {
        // Do something when failed to connect
    }
});
```

### Common things

```java
// mCommunicator can be either a BluetoothClient or a BluetoothServer

// Get all devices connected
Set<BluetoothDevice> connectedDevices = mCommunicator.getConnectedDevices();

// Check if a device is connected
boolean connected = mCommunicator.isConnectedToDevice(aDevice);

// Send a string line to a device
mCommunicator.sendLine("Hello, Bluetooth!", aDevice);

// Send a string line to all devices connected
mCommunicator.sendLineToAll("Hello, Bluetooth!");

// Disconnect to a device
mCommunicator.disconnectToDevice(aDevice);
```

Please refer to the source code for more information.

## Reference

- [Bluetooth | Android Developers](http://developer.android.com/intl/zh-cn/guide/topics/connectivity/bluetooth.html)
