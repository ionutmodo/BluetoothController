package com.home.bluetoothcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.widget.Toast;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Ionut-Vlad Modoranu on 10 Mai 2018.
 */

public class BluetoothManagement
{
    private static final String OTHER_DEVICE_NAME = "HC-05";
//    private static final String OTHER_DEVICE_NAME = "DESKTOP-E056U03";
//    private static final String OTHER_DEVICE_NAME = "iPhone";
    public static final UUID BLUETOOTH_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private MainActivity activity;
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothSocket bluetoothSocket = null;
    private BluetoothDevice bluetoothDevice = null;

    private InputStream bluetoothInputStream = null;
    private OutputStream bluetoothOutputStream = null;

    public BluetoothManagement(MainActivity activity)
    {
        this.activity = activity;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //setupWorker();
    }

    private void safeClose(Closeable stream)
    {
        if(stream != null)
        {
            try
            {
                stream.close();
            }
            catch(Exception e)
            {

            }
            stream = null;
        }
    }

    private void resetConnection()
    {
        if(bluetoothInputStream != null)
        {
            safeClose(bluetoothInputStream);
            safeClose(bluetoothOutputStream);
            safeClose(bluetoothSocket);
        }
    }

    public boolean connect()
    {
        resetConnection();
        if(!this.isAdapterEnabled())
        {
            enableBluetooth();
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices)
        {
            if (device.getName().trim().equals(OTHER_DEVICE_NAME))
            {
                if(bluetoothDevice == null)
                    bluetoothDevice = bluetoothAdapter.getRemoteDevice(device.getAddress());

                try
                {
                    //bluetoothSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(BLUETOOTH_UUID);
                    bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(BLUETOOTH_UUID);
                }
                catch(Exception e)
                {
                    Toast.makeText(activity.getApplicationContext(), "Failed to bind to RFCOMM by UUID", Toast.LENGTH_SHORT).show();
                    return false;
                }

                bluetoothAdapter.cancelDiscovery();

                try
                {
                    bluetoothSocket.connect();
                }
                catch(Exception e)
                {
                    Toast.makeText(activity.getApplicationContext(), "Failed to connect socket", Toast.LENGTH_SHORT).show();
                    return false;
                }

                try
                {
                    bluetoothInputStream = bluetoothSocket.getInputStream();
                    bluetoothOutputStream = bluetoothSocket.getOutputStream();
                }
                catch(Exception e)
                {
                    Toast.makeText(activity.getApplicationContext(), "Failed to attach I/O streams", Toast.LENGTH_SHORT).show();
                    return false;
                }
                Toast.makeText(activity, "Connected to " + OTHER_DEVICE_NAME, Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        Toast.makeText(activity, "Could not find device " + OTHER_DEVICE_NAME, Toast.LENGTH_SHORT).show();
        return false;
    }

    public void disconnect()
    {
        resetConnection();
        Toast.makeText(activity, "Disconnected from " + OTHER_DEVICE_NAME, Toast.LENGTH_SHORT).show();
    }

    private void setupWorker()
    {
        new Thread(new Runnable()
        {
            /*private boolean isValid(byte[] packet)
            {
                byte start = packet[ReceiveIndex.START.ordinal()];
                byte stop = packet[ReceiveIndex.STOP.ordinal()];
                return (start == START && stop == STOP);
            }*/

            @Override
            public void run()
            {
                while(true)
                {
                    try
                    {
                        if(bluetoothSocket != null && bluetoothSocket.isConnected() && bluetoothInputStream != null)
                        {
                            byte readByte = (byte) bluetoothInputStream.read();
                            // describe logic when a byte is received
                        }
                        Thread.sleep(100);
                    }
                    catch (InterruptedException | IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public boolean sendData(byte data)
    {
        try
        {
            if (bluetoothSocket != null)
            {
                bluetoothSocket.getOutputStream().write(data);
                return true;
            }
            return false;
        }
        catch (IOException ioe)
        {
            Toast.makeText(activity.getApplicationContext(),
                    "[BluetoothManager][sendData] Socket error on send",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public void enableBluetooth()
    {
        if(!bluetoothAdapter.isEnabled())
        {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(intent, 1);
            while (!bluetoothAdapter.isEnabled()) ;
            Toast.makeText(activity.getApplicationContext(), "Bluetooth turned on", Toast.LENGTH_SHORT).show();
        }
    }

    public void disableBluetooth()
    {
        if(this.isAdapterEnabled())
            this.bluetoothAdapter.disable();
        resetConnection();
    }

    public boolean isAdapterEnabled()
    {
        if(bluetoothAdapter == null)
            return false;
        return bluetoothAdapter.isEnabled();
    }

    public boolean isConnected()
    {
        if(bluetoothSocket == null)
            return false;
        return bluetoothSocket.isConnected();
    }
}
