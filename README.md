Bluetooth Communicator for Android
=========

Bluetooth Communicator is a framework that help you to establish Bluetooth connections between devices (several clients and one server). It hasn't been completely tested yet, and if anything strange occurs to your app, please send me an issue.

###Usage

####As a server

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
        // Do something after losing the connection with specific devcice
    }
});

// Start listening for Bluetooth connection requests
if (!mServer.isBluetoothSupported()) {
    return;
} else if (!mServer.isBluetoothEnabled()) {
    mServer.startActivityForEnablingBluetooth(this, 1);
} else {
    // NAME: Name of your service. It's OK to be app name
    // MY_UUID: A unique id used on both client side and server side, see "http://developer.android.com/intl/zh-cn/guide/topics/connectivity/bluetooth.html#ConnectingAsAServer"
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

####As a client

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
// Cancel discovery
mClient.cancelDiscovery(this);

// Choose a device (a server) and connect to it
mDevice = //...
mClient.connectToDevice(mDevice, MainActivity.MY_UUID, new ConnectResultListener() {
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

####Common things

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

// Disconnect to a specific device
mCommunicator.disconnectToDevice(someDevice);
```

Please refer to the source code for more information.

###Reference

[Bluetooth | Android Developers](http://developer.android.com/intl/zh-cn/guide/topics/connectivity/bluetooth.html)

###License

>The MIT License (MIT)
>
>Copyright (c) 2015 Richard Chien
>
>Permission is hereby granted, free of charge, to any person obtaining a copy
>of this software and associated documentation files (the "Software"), to deal
>in the Software without restriction, including without limitation the rights
>to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
>copies of the Software, and to permit persons to whom the Software is
>furnished to do so, subject to the following conditions:
>
>The above copyright notice and this permission notice shall be included in
>all copies or substantial portions of the Software.
>
>THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
>IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
>FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
>AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
>LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
>OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
>THE SOFTWARE.
