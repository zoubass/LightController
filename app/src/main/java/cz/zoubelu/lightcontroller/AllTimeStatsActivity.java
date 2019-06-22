package cz.zoubelu.lightcontroller;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.jjoe64.graphview.series.DataPoint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import cz.zoubelu.lightcontroller.domain.LightingDay;
import cz.zoubelu.lightcontroller.service.BackgroundStatsLoaderService;
import cz.zoubelu.lightcontroller.service.DbInitializer;
import cz.zoubelu.lightcontroller.task.LoadDataAndShowTotalAsyncTask;

public class AllTimeStatsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_all_time_stats);

        if (DbInitializer.getDb() == null) {
            DbInitializer.initDb(this);
        }

        new LoadDataAndShowTotalAsyncTask(this, R.id.graph_total).execute(Boolean.TRUE);
    }

    private List<DataPoint> generateDataPoints(List<LightingDay> lightingValues) {
        List<DataPoint> dataPoints = new ArrayList<>();

        for (LightingDay lightingDay : lightingValues) {
            dataPoints.add(new DataPoint(new Date(lightingDay.getDay()), lightingDay.getValue()));
        }
        return dataPoints;
    }

    private List<LightingDay> createTestData() {
        List<LightingDay> lightingValues = new ArrayList<>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            for (int day = 10; day < 14; day++) {

                for (int hour = 0; hour < 24; hour++) {
                    Date date = sdf.parse("2019-03-" + String.valueOf(day) + " " + String.valueOf(hour) + ":00:00");

                    LightingDay lightingDay = new LightingDay();
                    lightingDay.setDay(date.getTime());
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
}
