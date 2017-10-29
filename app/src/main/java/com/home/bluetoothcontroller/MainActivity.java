package com.home.bluetoothcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import static com.home.bluetoothcontroller.Settings.BACKWARD;
import static com.home.bluetoothcontroller.Settings.BACK_LEFT;
import static com.home.bluetoothcontroller.Settings.BACK_LIGHTS_OFF;
import static com.home.bluetoothcontroller.Settings.BACK_LIGHTS_ON;
import static com.home.bluetoothcontroller.Settings.BACK_RIGHT;
import static com.home.bluetoothcontroller.Settings.FORWARD;
import static com.home.bluetoothcontroller.Settings.FORWARD_LEFT;
import static com.home.bluetoothcontroller.Settings.FORWARD_RIGHT;
import static com.home.bluetoothcontroller.Settings.FRONT_LIGHTS_OFF;
import static com.home.bluetoothcontroller.Settings.FRONT_LIGHTS_ON;
import static com.home.bluetoothcontroller.Settings.HORN_OFF;
import static com.home.bluetoothcontroller.Settings.HORN_ON;
import static com.home.bluetoothcontroller.Settings.LEFT;
import static com.home.bluetoothcontroller.Settings.RIGHT;
import static com.home.bluetoothcontroller.Settings.SPEED;
import static com.home.bluetoothcontroller.Settings.STOP;
import static com.home.bluetoothcontroller.Settings.STOP_ALL;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, View.OnClickListener
{
    public static final String CAR_NAME = "HC-05";
    public static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private Button buttonLightsFront, buttonLightsBack, buttonHorn;
    private Button buttonLeft, buttonRight;
    private Button buttonForward, buttonBackward;
    private Button buttonConnect, buttonOpen;
    private SeekBar seekbarSpeed;
    private TextView textViewInfo;

    private boolean isLightsFront = false, isLightsBack = false, isHorn = false;
    private boolean isConnected = false;

    private int x = 1, y = 1;

    private BluetoothAdapter adapter = null;
    private BluetoothSocket socket = null;

    private final char[][] DIRECTION = new char[][]
            {
                    { FORWARD_LEFT, FORWARD, FORWARD_RIGHT },
                    { LEFT, STOP, RIGHT },
                    { BACK_LEFT, BACKWARD, BACK_RIGHT },
            };

    private final String[][] IMAGE_NAME =
            {
                    { "NV", "N", "NE" },
                    { "V", "", "E" },
                    { "SV", "S", "SE" }
            };

    private ImageView[][] IMAGE = null;

    private final int[][] IMAGE_BLUE = new int[][] {
            { R.drawable.blue_nv, R.drawable.blue_n, R.drawable.blue_ne },
            { R.drawable.blue_v, 0, R.drawable.blue_e },
            { R.drawable.blue_sv, R.drawable.blue_s, R.drawable.blue_se }
    };

    private final int[][] IMAGE_RED = new int[][] {
            { R.drawable.red_nv, R.drawable.red_n, R.drawable.red_ne },
            { R.drawable.red_v, 0, R.drawable.red_e },
            { R.drawable.red_sv, R.drawable.red_s, R.drawable.red_se }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createViews();

        adapter = BluetoothAdapter.getDefaultAdapter();
        buttonOpen.setText(adapter.isEnabled() ? "OFF" : "ON");
    }

    private void createViews()
    {
        buttonLightsFront = (Button) findViewById(R.id.button_lights_front);
        buttonLightsBack = (Button) findViewById(R.id.button_lights_back);
        buttonHorn = (Button) findViewById(R.id.button_horn);
        buttonLeft = (Button) findViewById(R.id.button_left);
        buttonRight = (Button) findViewById(R.id.button_right);
        buttonForward = (Button) findViewById(R.id.button_forward);
        buttonBackward = (Button) findViewById(R.id.button_backward);
        buttonConnect = (Button) findViewById(R.id.button_connect);
        buttonOpen = (Button) findViewById(R.id.button_open);
        textViewInfo = (TextView) findViewById(R.id.textview_info);
        seekbarSpeed = (SeekBar) findViewById(R.id.seekbar_speed);
        IMAGE = new ImageView[][] {
                {
                        (ImageView) findViewById(R.id.imageview_arrow_00),
                        (ImageView) findViewById(R.id.imageview_arrow_01),
                        (ImageView) findViewById(R.id.imageview_arrow_02)
                },
                {
                        (ImageView) findViewById(R.id.imageview_arrow_10),
                        (ImageView) findViewById(R.id.imageview_arrow_11),
                        (ImageView) findViewById(R.id.imageview_arrow_12)
                },
                {
                        (ImageView) findViewById(R.id.imageview_arrow_20),
                        (ImageView) findViewById(R.id.imageview_arrow_21),
                        (ImageView) findViewById(R.id.imageview_arrow_22)
                }
        };

        buttonLightsFront.setOnClickListener(this);
        buttonLightsBack.setOnClickListener(this);
        buttonHorn.setOnClickListener(this);
        buttonConnect.setOnClickListener(this);
        buttonOpen.setOnClickListener(this);


        buttonLeft.setOnTouchListener(this);
        buttonRight.setOnTouchListener(this);
        buttonForward.setOnTouchListener(this);
        buttonBackward.setOnTouchListener(this);

        seekbarSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                sendData(SPEED[progress / 10]);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
            }
        });
    }

    private void setRed()
    {
        for (int i = 0; i < 3; ++i)
            for (int j = 0; j < 3; ++j)
                if (i != 1 || j != 1)
                    IMAGE[i][j].setImageResource(IMAGE_BLUE[i][j]);
        if (x != 1 || y != 1)
            IMAGE[x][y].setImageResource(IMAGE_RED[x][y]);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        final int UP = MotionEvent.ACTION_UP;
        final int DOWN = MotionEvent.ACTION_DOWN;
        final int action = event.getAction();

        switch (v.getId())
        {
            case R.id.button_forward:
                if (action == DOWN)
                {
                    --x;
                    //setRed();
                }
                else if (action == UP)
                {
                    ++x;
                    //setBlue();
                }
                break;
            case R.id.button_backward:
                if (action == DOWN)
                {
                    ++x;
                    //setRed();
                }
                else if (action == UP)
                {
                    --x;
                    //setBlue();
                }
                break;
            case R.id.button_left:
                if (action == DOWN)
                {
                    --y;
                    //setRed();
                }
                else if (action == UP)
                {
                    ++y;
                    //setBlue();
                }
                break;
            case R.id.button_right:
                if (action == DOWN)
                {
                    ++y;
                    //setRed();
                }
                else if (action == UP)
                {
                    --y;
                    //setBlue();
                }
                break;
        }
        sendData(DIRECTION[x][y]);
        textViewInfo.setText(IMAGE_NAME[x][y]);
        setRed();
        return false;
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.button_lights_front:
                isLightsFront = !isLightsFront;
                buttonLightsFront.setText(!isLightsFront ? "FATA(OFF)" : "FATA(ON)");
                sendData(isLightsFront ? FRONT_LIGHTS_ON : FRONT_LIGHTS_OFF);
                break;
            case R.id.button_lights_back:
                isLightsBack = !isLightsBack;
                buttonLightsBack.setText(!isLightsBack ? "SPATE(OFF)" : "SPATE(ON)");
                sendData(isLightsFront ? BACK_LIGHTS_ON : BACK_LIGHTS_OFF);
                break;
            case R.id.button_horn:
                isHorn = !isHorn;
                buttonHorn.setText(!isHorn ? "CLAXON(OFF)" : "CLAXON(ON)");
                sendData(isHorn ? HORN_ON : HORN_OFF);
                break;
            case R.id.button_open:
                if (buttonOpen.getText().equals("OFF"))
                {
                    adapter.disable();
                    buttonOpen.setText("ON");
                    try
                    {
                        if (socket != null)
                            socket.close();
                    }
                    catch (IOException ioe)
                    {
                        Toast.makeText(getApplicationContext(), "Socket error on close", Toast.LENGTH_SHORT).show();
                    }
                }
                else if (buttonOpen.getText().equals("ON"))
                {
                    if (!adapter.isEnabled())
                    {
                        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(intent, 1);
                        while (!adapter.isEnabled()) ;
                        Toast.makeText(getApplicationContext(), "Bluetooth turned on", Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(getApplicationContext(), "Bluetooth already turned on", Toast.LENGTH_SHORT).show();
                    buttonOpen.setText("OFF");
                }
                break;
            case R.id.button_connect:
                if (!isConnected)
                {
                    Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
                    if (pairedDevices.size() > 0)
                    {
                        for (BluetoothDevice device : pairedDevices)
                        {
                            if (device.getName().equals(CAR_NAME))
                            {
                                try
                                {
                                    BluetoothDevice car = adapter.getRemoteDevice(device.getAddress());
                                    socket = car.createInsecureRfcommSocketToServiceRecord(myUUID);
                                    adapter.cancelDiscovery();
                                    socket.connect();
                                    sendData(STOP_ALL);
                                    Toast.makeText(getApplicationContext(), "Connected to HC-05", Toast.LENGTH_SHORT).show();
                                    break;
                                }
                                catch (IOException ioe)
                                {
                                    Toast.makeText(getApplicationContext(), "Socket error on connect", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                    isConnected = true;
                    buttonConnect.setText("DISCONNECT");
                }
                else // if(isConnected)
                {
                    if (socket != null)
                    {
                        try
                        {
                            socket.close();
                            isConnected = false;
                            buttonConnect.setText("CONNECT");
                        }
                        catch (IOException ioe)
                        {
                            Toast.makeText(getApplicationContext(), "Socket error on disconnect", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
        }
    }

    private void sendData(char data)
    {
        try
        {
            if (socket != null)
                socket.getOutputStream().write(String.valueOf(data).getBytes());
        }
        catch (IOException ioe)
        {
            Toast.makeText(getApplicationContext(), "Socket error on send", Toast.LENGTH_SHORT).show();
        }
    }
}
