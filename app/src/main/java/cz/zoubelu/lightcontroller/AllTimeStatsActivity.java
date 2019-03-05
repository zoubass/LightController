package cz.zoubelu.lightcontroller;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import cz.zoubelu.lightcontroller.domain.LightingDay;
import cz.zoubelu.lightcontroller.service.DbInitializer;

public class AllTimeStatsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_all_time_stats);

        List<LightingDay> lightingValues = DbInitializer.getDb().lightingValuesDao().findAll();

        List<DataPoint> dataPoints = generateDataPoints(lightingValues);

        GraphView graph = findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints.toArray(new DataPoint[0]));
        graph.setTitle("Total light brightness");
        graph.addSeries(series);

        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(256);

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this));
        graph.getGridLabelRenderer().setNumHorizontalLabels(3);

        graph.getViewport().setMinX(dataPoints.get(0).getX());
        graph.getViewport().setMaxX(dataPoints.get(dataPoints.size() - 1).getX());
        graph.getViewport().setXAxisBoundsManual(true);

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
