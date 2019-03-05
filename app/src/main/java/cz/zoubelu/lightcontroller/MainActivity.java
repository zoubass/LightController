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

import java.util.List;

import cz.zoubelu.lightcontroller.domain.Device;
import cz.zoubelu.lightcontroller.service.BackgroundStatsLoaderService;
import cz.zoubelu.lightcontroller.service.DbInitializer;
import cz.zoubelu.lightcontroller.task.CheckDeviceAsyncTask;
import cz.zoubelu.lightcontroller.task.DiscoveryAsyncTask;
import cz.zoubelu.lightcontroller.task.SendDutyRequestAsyncTask;
import cz.zoubelu.lightcontroller.task.SendRequestAsyncTask;

public class MainActivity extends AppCompatActivity {

    private Device actualDevice;

    private List<Device> savedDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DbInitializer.initDb(MainActivity.this);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_main);

        Switch switchButton = findViewById(R.id.switchId);

        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (actualDevice != null) {
                    if (isChecked) {
                        new SendRequestAsyncTask(MainActivity.this, true, actualDevice).execute();
                    } else {
                        new SendRequestAsyncTask(MainActivity.this, false, actualDevice).execute();
                    }
                }
            }
        });

        SeekBar seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (actualDevice != null) {
                    new SendDutyRequestAsyncTask(MainActivity.this, i, actualDevice).execute();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        findViewById(R.id.set_color_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ColorActivity.class);
                intent.putExtra("deviceIp", actualDevice != null? actualDevice.getActual_ip():"");
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

//        new CheckDeviceAsyncTask(this).execute();

        if (actualDevice == null) {
            new DiscoveryAsyncTask(MainActivity.this).execute();
        }

        Intent msgIntent = new Intent(this, BackgroundStatsLoaderService.class);

        startService(msgIntent);
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
}
