package cz.zoubelu.lightcontroller.task;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import cz.zoubelu.lightcontroller.domain.Device;
import cz.zoubelu.lightcontroller.service.DbInitializer;

public class UpdateDeviceAsyncTask extends AsyncTask<Device, Void, Void> {

    private boolean updateStateOnly;
    private Activity activity;
    private Device oldDevice;

    public UpdateDeviceAsyncTask(boolean updateStateOnly, Activity activity, Device oldDevice) {
        this.updateStateOnly = updateStateOnly;
        this.activity = activity;
        this.oldDevice = oldDevice;
    }

    @Override
    protected Void doInBackground(Device... devices) {

        Device newDevice = devices[0];

        if (devices.length > 0 && newDevice != null) {
            if (updateStateOnly) {
                DbInitializer.getDb().deviceDao().updateState(devices[0].getId());
            } else {
                Device storedDevice = DbInitializer.getDb().deviceDao().findByNameAndAddress(oldDevice.getName(), oldDevice.getActual_ip());
                DbInitializer.getDb().deviceDao().updateInfo(newDevice.getName(), newDevice.getActual_ip(), storedDevice.getId());

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(activity, "Changes saved", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
        return null;
    }
}
