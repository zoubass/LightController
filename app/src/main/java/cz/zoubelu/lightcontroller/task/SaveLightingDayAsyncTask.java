package cz.zoubelu.lightcontroller.task;

import android.os.AsyncTask;

import java.util.List;

import cz.zoubelu.lightcontroller.domain.LightingDay;
import cz.zoubelu.lightcontroller.service.DbInitializer;

public class SaveLightingDayAsyncTask extends AsyncTask<List<LightingDay>, Void, Void> {

    @Override
    protected Void doInBackground(List<LightingDay>... lists) {
        for (LightingDay lightingDay : lists[0]) {
            DbInitializer.getDb().lightingValuesDao().save(lightingDay);
        }
        return null;
    }
}
