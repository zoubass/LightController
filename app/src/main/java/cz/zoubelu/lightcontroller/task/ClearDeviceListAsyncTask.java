package cz.zoubelu.lightcontroller.task;

import android.os.AsyncTask;

import cz.zoubelu.lightcontroller.service.DbInitializer;

public class ClearDeviceListAsyncTask extends AsyncTask<Void, Void, Void> {

    @Override
    protected Void doInBackground(Void... voids) {
        DbInitializer.getDb().deviceDao().deleteAll();
        return null;
    }
}
