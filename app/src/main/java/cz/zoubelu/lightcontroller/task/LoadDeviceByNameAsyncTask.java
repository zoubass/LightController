package cz.zoubelu.lightcontroller.task;

import android.os.AsyncTask;

import java.util.List;

import cz.zoubelu.lightcontroller.domain.Device;
import cz.zoubelu.lightcontroller.service.DbInitializer;

public class LoadDeviceByNameAsyncTask extends AsyncTask<String, Void, List<Device>> {

    @Override
    protected List<Device> doInBackground(String... strings) {
        return DbInitializer.getDb().deviceDao().findByName(strings[0]);
    }
}
