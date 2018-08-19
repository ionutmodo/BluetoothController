package com.home.bluetoothcontroller;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

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
import static com.home.bluetoothcontroller.Settings.STOP;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, View.OnClickListener
{
    private BluetoothManagement btManager;

    private Button buttonLightsFront, buttonLightsBack, buttonHorn;
    private Button buttonLeft, buttonRight;
    private Button buttonForward, buttonBackward;
    private Button buttonConnect, buttonOpen;
    private SeekBar seekbarSpeed;
    private TextView textViewInfo;

    private boolean isLightsFront = false, isLightsBack = false, isHorn = false;

    private int x = 1, y = 1;
    private Byte lastSpeed = null, lastDirection = null;

    private final byte[][] DIRECTION = new byte[][]
            {
                    {FORWARD_LEFT, FORWARD, FORWARD_RIGHT},
                    {LEFT, STOP, RIGHT},
                    {BACK_LEFT, BACKWARD, BACK_RIGHT},
            };

    private final String[][] IMAGE_NAME =
            {
                    {"NV", "N", "NE"},
                    {"V", "o", "E"},
                    {"SV", "S", "SE"}
            };

    private ImageView[][] IMAGE = null;

    private final int[][] IMAGE_BLUE = new int[][]{
            {R.drawable.blue_nv, R.drawable.blue_n, R.drawable.blue_ne},
            {R.drawable.blue_v, 0, R.drawable.blue_e},
            {R.drawable.blue_sv, R.drawable.blue_s, R.drawable.blue_se}
    };

    private final int[][] IMAGE_RED = new int[][]{
            {R.drawable.red_nv, R.drawable.red_n, R.drawable.red_ne},
            {R.drawable.red_v, 0, R.drawable.red_e},
            {R.drawable.red_sv, R.drawable.red_s, R.drawable.red_se}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createViews();

        btManager = new BluetoothManagement(this);
        buttonOpen.setText(btManager.isAdapterEnabled() ? "OFF" : "ON");
    }

    private void createViews()
    {
        buttonLightsFront = findViewById(R.id.button_lights_front);
        buttonLightsBack = findViewById(R.id.button_lights_back);
        buttonHorn = findViewById(R.id.button_horn);
        buttonLeft = findViewById(R.id.button_left);
        buttonRight = findViewById(R.id.button_right);
        buttonForward = findViewById(R.id.button_forward);
        buttonBackward = findViewById(R.id.button_backward);
        buttonConnect = findViewById(R.id.button_connect);
        buttonOpen = findViewById(R.id.button_open);
        textViewInfo = findViewById(R.id.textview_info);
        seekbarSpeed = findViewById(R.id.seekbar_speed);
        IMAGE = new ImageView[][]{
                {
                        findViewById(R.id.imageview_arrow_00),
                        findViewById(R.id.imageview_arrow_01),
                        findViewById(R.id.imageview_arrow_02)
                },
                {
                        findViewById(R.id.imageview_arrow_10),
                        findViewById(R.id.imageview_arrow_11),
                        findViewById(R.id.imageview_arrow_12)
                },
                {
                        findViewById(R.id.imageview_arrow_20),
                        findViewById(R.id.imageview_arrow_21),
                        findViewById(R.id.imageview_arrow_22)
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
                byte speed = (byte) (progress / 10);
                if(lastSpeed == null)
                    lastSpeed = speed;
                else if(lastSpeed != speed)
                {
                    lastSpeed = speed;
                    btManager.sendData(lastSpeed);
                }

                MainActivity.this.setTitle(String.format("Speed = %d", (int) speed));
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
        if(lastDirection == null)
            lastDirection = DIRECTION[x][y];
        else if(lastDirection != DIRECTION[x][y])
        {
            lastDirection = DIRECTION[x][y];
            btManager.sendData(lastDirection);
        }
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
                buttonLightsFront.setText(!isLightsFront ? "FRONT(OFF)" : "FRONT(ON)");
                btManager.sendData(isLightsFront ? FRONT_LIGHTS_ON : FRONT_LIGHTS_OFF);
                break;
            case R.id.button_lights_back:
                isLightsBack = !isLightsBack;
                buttonLightsBack.setText(!isLightsBack ? "BACK(OFF)" : "BACK(ON)");
                btManager.sendData(isLightsBack ? BACK_LIGHTS_ON : BACK_LIGHTS_OFF);
                break;
            case R.id.button_horn:
                isHorn = !isHorn;
                buttonHorn.setText(!isHorn ? "HORN(OFF)" : "HORN(ON)");
                btManager.sendData(isHorn ? HORN_ON : HORN_OFF);
                break;
            case R.id.button_open:
                if (!btManager.isAdapterEnabled())
                {
                    btManager.enableBluetooth();
                    buttonOpen.setText("OFF");
                }
                else //adapter enabled
                {
                    btManager.disableBluetooth();
                    buttonOpen.setText("ON");
                }
                break;
            case R.id.button_connect:
                if (btManager.isConnected())
                {
                    btManager.disconnect();
                    buttonConnect.setText("CONNECT");
                }
                else
                {
                    btManager.connect();
                    buttonConnect.setText("DISCONNECT");
                }
                break;
        }
    }
}
