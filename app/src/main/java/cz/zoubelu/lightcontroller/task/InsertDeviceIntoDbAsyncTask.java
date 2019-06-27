package cz.zoubelu.lightcontroller.task;

import android.os.AsyncTask;

import java.util.List;

import cz.zoubelu.lightcontroller.domain.Device;
import cz.zoubelu.lightcontroller.service.DbInitializer;

public class InsertDeviceIntoDbAsyncTask extends AsyncTask<Device, Void, Void> {
    @Override
    protected Void doInBackground(Device... devices) {
        Device newDevice = devices[0];
        List<Device> duplicates = DbInitializer.getDb().deviceDao().findByName(newDevice.getName());

        if (duplicates == null || duplicates.isEmpty()) {
            newDevice.setActive(true);
            DbInitializer.getDb().deviceDao().save(newDevice);
        } else if (isNotDuplicate(duplicates, newDevice)){
            newDevice.setActive(true);
            DbInitializer.getDb().deviceDao().save(newDevice);
        }
        return null;
    }

    private boolean isNotDuplicate(List<Device> duplicates, Device newDevice) {
        for (Device storedDevice: duplicates) {
            if (storedDevice.getActual_ip().equals(newDevice.getActual_ip()) && storedDevice.getName().equals(newDevice.getName())){
                return false;
            }
        }
        return true;
    }
}
