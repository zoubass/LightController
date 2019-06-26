package cz.zoubelu.lightcontroller;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import cz.zoubelu.lightcontroller.domain.Device;
import cz.zoubelu.lightcontroller.domain.LightingDay;
import cz.zoubelu.lightcontroller.domain.MotionDetected;
import cz.zoubelu.lightcontroller.service.BackgroundStatsLoaderService;
import cz.zoubelu.lightcontroller.service.DbInitializer;
import cz.zoubelu.lightcontroller.task.DiscoveryAsyncTask;
import cz.zoubelu.lightcontroller.task.SendDutyRequestAsyncTask;
import cz.zoubelu.lightcontroller.task.SendRequestSwitchAsyncTask;
import cz.zoubelu.lightcontroller.task.SendRequestSwitchAutoLightAsyncTask;
import cz.zoubelu.lightcontroller.task.SendRequestSwitchCalibrationAsyncTask;
import cz.zoubelu.lightcontroller.task.SendRequestSwitchDetectMotionAsyncTask;
import cz.zoubelu.lightcontroller.task.UpdateDeviceAsyncTask;

public class MainActivity extends AppCompatActivity {

    private Device actualDevice;

    private List<Device> savedDevices;
    private String deviceStringToEdit;
    private Switch switchButton;
    private Switch autolightSwitch;
    private Switch detectMotionSwitch;
    private Switch calibrateSwitch;
    private SeekBar seekBar;
    private Button colorBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DbInitializer.initDb(MainActivity.this);
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
                } else {
                    seekBar.setProgress(0);
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

        seekBar = findViewById(R.id.seekBar);
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
                switchButton.setChecked(true);
            }
        });

        colorBtn = findViewById(R.id.set_color_btn);
        colorBtn.setOnClickListener(new View.OnClickListener() {
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

        enableSwitches(false);
        if (actualDevice == null) {
            DiscoveryAsyncTask discoveryTask = new DiscoveryAsyncTask(MainActivity.this);
            discoveryTask.execute();
        }

        Intent statsLoadIntent = new Intent(this, BackgroundStatsLoaderService.class);
        startService(statsLoadIntent);

        final TextView discoveryInfoTextView = findViewById(R.id.discover_progress_text_view);
        discoveryInfoTextView.setText("Seeking stored devices...");
        discoveryInfoTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (discoveryInfoTextView.getText().equals(DiscoveryAsyncTask.SEARCHING) && actualDevice == null) {
                    discoveryInfoTextView.setText("Retry...");
                    new DiscoveryAsyncTask(MainActivity.this).execute();
                } else if (actualDevice != null) {
                    showDialog();
                }
            }
        });
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Device");

        final List<Device> devices = DbInitializer.getDb().deviceDao().findAll();

        final String[] stringArray = new String[devices.size()];

        for (int i = 0; i < devices.size(); i++) {
            stringArray[i] = devices.get(i).getName() + "\n" + devices.get(i).getActual_ip();
        }
        builder.setView(R.layout.array_list_layout);

        builder.setItems(stringArray, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                deviceStringToEdit = stringArray[item];
                showPopupMenu(findViewById(R.id.discover_progress_text_view));
            }
        });

        final Dialog dialog = builder.create();

        dialog.setContentView(R.layout.edit_item_layout);
        dialog.show();
    }


    private void showEditDialog(final String item) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        final LayoutInflater inflater = this.getLayoutInflater();

        final String deviceName = item.substring(0, item.indexOf("\n"));
        final String address = item.replace(deviceName, "").replace("\n", "");

        final View dialogView = inflater.inflate(R.layout.edit_item_layout, null);

        final EditText deviceText = dialogView.findViewById(R.id.deviceName);
        deviceText.setText(deviceName);

        final EditText addressText = dialogView.findViewById(R.id.address);
        addressText.setText(address);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(dialogView)
                // Add action buttons
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String newName = deviceText.getText().toString();
                        String newAddress = addressText.getText().toString();
                        System.out.println(newName);

                        if (newName==null || newName.isEmpty()) {
                            Toast.makeText(MainActivity.this, "Invalid name!", Toast.LENGTH_SHORT).show();
                        } else if (newAddress==null || newAddress.isEmpty()){
                            Toast.makeText(MainActivity.this, "Invalid address!", Toast.LENGTH_SHORT).show();
                        } else {
                            new UpdateDeviceAsyncTask(false, MainActivity.this, new Device(deviceName, address)).execute(new Device(newName, newAddress));
                        }

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        Dialog dialog = builder.create();
        dialog.show();
    }

    private void showPopupMenu(View view) {
        // inflate menu
        PopupMenu popup = new PopupMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_album, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener());
        popup.show();
    }

    /**
     * Click listener for popup menu items
     */
    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        public MyMenuItemClickListener() {
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.select_item:
                    Toast.makeText(MainActivity.this, "Selected", Toast.LENGTH_SHORT).show();


                    final String deviceName = deviceStringToEdit.substring(0, deviceStringToEdit.indexOf("\n"));
                    final String address = deviceStringToEdit.replace(deviceName, "").replace("\n", "");

                    Device actualDevice = DbInitializer.getDb().deviceDao().findByNameAndAddress(deviceName, address);
                    setActualDevice(actualDevice);
                    TextView discoveryText = findViewById(R.id.discover_progress_text_view);
                    discoveryText.setText(deviceStringToEdit.substring(0, deviceStringToEdit.indexOf("\n")));
                    return true;
                case R.id.edit_item:
                    showEditDialog(deviceStringToEdit);
                    return true;
                default:
            }
            return false;
        }
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
        seekBar.setEnabled(enabled);
        colorBtn.setEnabled(enabled);
    }
}
