package com.example.proyecto_3;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;

public class UsbCommunication {

    private static final String ACTION_USB_PERMISSION = "com.example.proyecto_3.USB_PERMISSION";
    private final UsbManager usbManager;
    private UsbSerialPort port;
    private final Context context;
    private final UsbCommunicationListener listener;
    private final StringBuilder dataBuffer = new StringBuilder();

    public UsbCommunication(Context context, UsbCommunicationListener listener) {
        this.context = context;
        this.listener = listener;
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
    }

    public interface UsbCommunicationListener {
        void onUsbDataReceived(String data);
        void onUsbPermissionDenied();
        void onUsbConnected();
        void onUsbDeviceNotFound();
    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    Log.d("UsbCommunication", "Received intent for device: " + device);
                    if (device != null && intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        Log.d("UsbCommunication", "Permission granted for device " + device);
                        port = findDriver(device);
                        if (port != null) {
                            openSerialPort(device);
                        }
                    } else {
                        Log.d("UsbCommunication", "Permission denied for device " + device);
                        listener.onUsbPermissionDenied();
                    }
                }
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void initialize() {
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);

        if (!availableDrivers.isEmpty()) {
            UsbSerialDriver driver = availableDrivers.get(0);
            UsbDevice usbDevice = driver.getDevice();

            PendingIntent permissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION).putExtra(UsbManager.EXTRA_DEVICE, usbDevice), PendingIntent.FLAG_IMMUTABLE);
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            context.registerReceiver(usbReceiver, filter, Context.RECEIVER_NOT_EXPORTED);

            if (usbManager.hasPermission(usbDevice)) {
                port = driver.getPorts().get(0);
                openSerialPort(usbDevice);
            } else {
                Log.d("UsbCommunication", "Requesting permission for device " + usbDevice);
                usbManager.requestPermission(usbDevice, permissionIntent);
            }
        } else {
            Log.d("UsbCommunication", "No USB devices found");
            listener.onUsbDeviceNotFound();
        }
    }

    private UsbSerialPort findDriver(UsbDevice device) {
        List<UsbSerialDriver> drivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);
        for (UsbSerialDriver driver : drivers) {
            if (driver.getDevice().equals(device)) {
                return driver.getPorts().get(0);
            }
        }
        return null;
    }

    private void openSerialPort(UsbDevice device) {
        UsbDeviceConnection connection = usbManager.openDevice(device);
        if (connection == null) {
            Log.e("UsbCommunication", "Cannot open device connection");
            return;
        }

        try {
            port.open(connection);
            port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            SerialInputOutputManager usbIoManager = new SerialInputOutputManager(port, new SerialInputOutputManager.Listener() {
                @Override
                public void onNewData(byte[] data) {
                    accumulateData(new String(data));
                }

                @Override
                public void onRunError(Exception e) {
                    Log.e("UsbCommunication", "Error in USB Serial IO Manager", e);
                }
            });
            Executors.newSingleThreadExecutor().submit(usbIoManager);
            listener.onUsbConnected();
        } catch (IOException e) {
            Log.e("UsbCommunication", "Error opening serial port", e);
        }
    }

    private void accumulateData(String data) {
        Log.d("UsbCommunication", "Accumulating data: " + data);
        dataBuffer.append(data);
        int endIndex = dataBuffer.indexOf("\n");
        while (endIndex != -1) {
            String completeData = dataBuffer.substring(0, endIndex).trim();
            Log.d("UsbCommunication", "Complete data received: " + completeData);
            listener.onUsbDataReceived(completeData);
            dataBuffer.delete(0, endIndex + 1);
            endIndex = dataBuffer.indexOf("\n");
        }
    }

    public void close() {
        if (port != null) {
            try {
                port.close();
            } catch (IOException e) {
                Log.e("UsbCommunication", "Error closing serial port", e);
            }
        }
        try {
            context.unregisterReceiver(usbReceiver);
        } catch (IllegalArgumentException e) {
            Log.e("UsbCommunication", "Receiver not registered", e);
        }
    }
}
