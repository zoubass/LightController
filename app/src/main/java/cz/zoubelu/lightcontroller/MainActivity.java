package cz.zoubelu.lightcontroller;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import cz.zoubelu.lightcontroller.domain.Device;
import cz.zoubelu.lightcontroller.domain.LightingDay;
import cz.zoubelu.lightcontroller.service.BackgroundStatsLoaderService;
import cz.zoubelu.lightcontroller.service.DbInitializer;
import cz.zoubelu.lightcontroller.task.ClearDeviceListAsyncTask;
import cz.zoubelu.lightcontroller.task.DiscoveryAsyncTask;
import cz.zoubelu.lightcontroller.task.SendDutyRequestAsyncTask;
import cz.zoubelu.lightcontroller.task.SendRequestSwitchAsyncTask;
import cz.zoubelu.lightcontroller.task.SendRequestSwitchAutoLightAsyncTask;
import cz.zoubelu.lightcontroller.task.SendRequestSwitchCalibrationAsyncTask;
import cz.zoubelu.lightcontroller.task.SendRequestSwitchDetectMotionAsyncTask;

public class MainActivity extends AppCompatActivity {

    private Device actualDevice;

    private List<Device> savedDevices;
    private Switch switchButton;
    private Switch autolightSwitch;
    private Switch detectMotionSwitch;
    private Switch calibrateSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DbInitializer.initDb(MainActivity.this);
        new ClearDeviceListAsyncTask().execute();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_main);

        switchButton = findViewById(R.id.switchId);
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    autolightSwitch.setChecked(false);
                }
                if (actualDevice != null) {
                    if (isChecked) {
                        new SendRequestSwitchAsyncTask(MainActivity.this, true, actualDevice).execute();
                    } else {
                        new SendRequestSwitchAsyncTask(MainActivity.this, false, actualDevice).execute();
                    }
                }
            }
        });

        SeekBar seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int value;
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                value = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (actualDevice != null) {
                    new SendDutyRequestAsyncTask(MainActivity.this, value, actualDevice).execute();
                }
            }
        });

        findViewById(R.id.set_color_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ColorActivity.class);
                intent.putExtra("deviceIp", actualDevice != null ? actualDevice.getActual_ip() : "");
                startActivity(intent);
            }
        });

        findViewById(R.id.statistics_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), StatsActivity.class);
                startActivity(intent);
            }
        });


        autolightSwitch = findViewById(R.id.auto_light);
        autolightSwitch.setChecked(true);
        autolightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    switchButton.setChecked(false);
                }
                if (actualDevice != null) {
                    if (isChecked) {
                        new SendRequestSwitchAutoLightAsyncTask(MainActivity.this, true, actualDevice).execute();
                    } else {
                        new SendRequestSwitchAutoLightAsyncTask(MainActivity.this, false, actualDevice).execute();
                    }

                }
            }
        });
        detectMotionSwitch = findViewById(R.id.detect_motion);
        detectMotionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (actualDevice != null) {
                    if (isChecked) {
                        new SendRequestSwitchDetectMotionAsyncTask(MainActivity.this, true, actualDevice).execute();
                    } else {
                        new SendRequestSwitchDetectMotionAsyncTask(MainActivity.this, false, actualDevice).execute();
                    }
                }
            }
        });
        calibrateSwitch = findViewById(R.id.calibrate_switch);
        calibrateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (actualDevice != null) {
                    if (isChecked) {
                        new SendRequestSwitchCalibrationAsyncTask(MainActivity.this, true, actualDevice).execute();
                    } else {
                        new SendRequestSwitchCalibrationAsyncTask(MainActivity.this, false, actualDevice).execute();
                    }
                }
            }
        });

//        new CheckDeviceAsyncTask(this).execute();

        enableSwitches(false);
        if (actualDevice == null) {
            DiscoveryAsyncTask discoveryTask = new DiscoveryAsyncTask(MainActivity.this);
            discoveryTask.execute();
        }

        Intent statsLoadIntent = new Intent(this, BackgroundStatsLoaderService.class);
        startService(statsLoadIntent);

        final TextView discoveryInfoTextView = findViewById(R.id.discover_progress_text_view);
        discoveryInfoTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (discoveryInfoTextView.getText().equals(DiscoveryAsyncTask.SEARCHING) && actualDevice == null) {
                    new DiscoveryAsyncTask(MainActivity.this).execute();
                }
            }
        });


//        new SaveLightingDayAsyncTask().execute(createTestData());
    }

    private List<LightingDay> createTestData() {
        List<LightingDay> lightingValues = new ArrayList<>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            for (int day = 10; day < 14; day++) {

                for (int hour = 0; hour < 24; hour++) {
                    Date date = sdf.parse("2019-03-" + String.valueOf(day) + " " + String.valueOf(hour) + ":00:00");
                    Date dayDate = sdf.parse("2019-03-" + String.valueOf(day) + " 00:00:00");

                    LightingDay lightingDay = new LightingDay();
                    lightingDay.setDay(dayDate.getTime());
                    lightingDay.setDate(date.getTime());
                    lightingDay.setHour(hour);
                    if (hour < 7 || hour > 22) {
                        lightingDay.setValue(0);
                    } else {
                        lightingDay.setValue(new Random().nextInt((247 - 124) + 1) + 124);
                    }
                    lightingValues.add(lightingDay);
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return lightingValues;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public Device getActualDevice() {
        return actualDevice;
    }

    public void setActualDevice(Device actualDevice) {
        this.actualDevice = actualDevice;
    }

    public void enableSwitches(boolean enabled) {
        switchButton.setEnabled(enabled);
        autolightSwitch.setEnabled(enabled);
        calibrateSwitch.setEnabled(enabled);
        detectMotionSwitch.setEnabled(enabled);
    }
}
