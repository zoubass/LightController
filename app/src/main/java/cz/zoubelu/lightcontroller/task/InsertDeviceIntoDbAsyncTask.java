package cz.zoubelu.lightcontroller.task;

import android.os.AsyncTask;

import cz.zoubelu.lightcontroller.domain.Device;
import cz.zoubelu.lightcontroller.service.DbInitializer;

public class InsertDeviceIntoDbAsyncTask extends AsyncTask<Device, Void, Void> {
    @Override
    protected Void doInBackground(Device... devices) {
        DbInitializer.getDb().deviceDao().save(devices[0]);
        return null;
    }
}
