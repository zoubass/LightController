package cz.zoubelu.lightcontroller.task;

import android.os.AsyncTask;

import java.util.List;

import cz.zoubelu.lightcontroller.domain.MotionDetected;
import cz.zoubelu.lightcontroller.service.DbInitializer;

public class SaveMotionStatsAsyncTask extends AsyncTask<List<MotionDetected>, Void, Void> {
    @Override
    protected Void doInBackground(List<MotionDetected>... lists) {
        DbInitializer.getDb().motionDetectedDao().save(lists[0]);
        return null;
    }
}
