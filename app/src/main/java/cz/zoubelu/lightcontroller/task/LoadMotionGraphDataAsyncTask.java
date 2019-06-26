package cz.zoubelu.lightcontroller.task;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.Toast;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.zoubelu.lightcontroller.domain.MotionDetected;
import cz.zoubelu.lightcontroller.service.DbInitializer;

public class LoadMotionGraphDataAsyncTask extends AsyncTask<Void, Void, List<MotionDetected>> {

    private Activity activity;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd.MM");

    private int graphId;

    public LoadMotionGraphDataAsyncTask(Activity activity, int graphId) {
        this.activity = activity;
        this.graphId = graphId;
    }

    @Override
    protected List<MotionDetected> doInBackground(Void... booleans) {
        List<MotionDetected> motionDetections;

        if (DbInitializer.getDb() == null) {
            DbInitializer.initDb(activity);
        }

        motionDetections = DbInitializer.getDb().motionDetectedDao().findAll();
        return motionDetections;
    }

    @Override
    protected void onPostExecute(List<MotionDetected> motionDetections) {
        GraphView graph = activity.findViewById(graphId);
        BarGraphSeries<DataPoint> series = new BarGraphSeries<>(generateDataPoints(motionDetections));
        graph.addSeries(series);

        series.setValueDependentColor(new ValueDependentColor<DataPoint>() {
            @Override
            public int get(DataPoint data) {
                return Color.rgb((int) data.getX()*255/4, (int) Math.abs(data.getY()*255/6), 100);
            }
        });

        series.setSpacing(10);

        series.setAnimated(true);

        graph.setTitle("Motion detection count");
        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    return sdf.format(new Date((long) value));
                } else {
                    return super.formatLabel(value, isValueX);
                }
            }
        });
        graph.getGridLabelRenderer().setLabelsSpace(3);
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Days");
        graph.getGridLabelRenderer().setHorizontalAxisTitleColor(Color.BLACK);
        graph.getGridLabelRenderer().setHumanRounding(true);

        graph.getViewport().setMinY(0);

        graph.getViewport().setMaxY(300);

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setXAxisBoundsManual(true);

        graph.getViewport().setScrollable(true);
        graph.getViewport().setScrollableY(true);
        graph.getViewport().setScalable(true);
        graph.getViewport().setScalableY(true);

        graph.setTitleColor(Color.WHITE);

        series.setDrawValuesOnTop(true);
        series.setValuesOnTopColor(Color.BLACK);

        series.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Date clickedDate = new Date(Math.round(dataPoint.getX()));
                String formattedDay = sdf.format(clickedDate);
                Toast.makeText(activity, "Day: " + formattedDay, Toast.LENGTH_SHORT).show();
            }
        });

        graph.getGridLabelRenderer().reloadStyles();

    }

    private DataPoint[] generateDataPoints(List<MotionDetected> motionDetections) {
        List<DataPoint> dataPoints = new ArrayList<>();

        Map<String, Long> detectionPerDay = new HashMap<>();

        int year = 0;

        for (MotionDetected motion: motionDetections) {
            Date actualDate = new Date(motion.getTime());
            Calendar cal = Calendar.getInstance();
            cal.setTime(actualDate);
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int month = cal.get(Calendar.MONTH);
            year = cal.get(Calendar.YEAR);

            String dayOfMonth = String.valueOf(day) + "." + String.valueOf(month) + ".";

            if (detectionPerDay.containsKey(dayOfMonth)) {
                detectionPerDay.put(dayOfMonth, detectionPerDay.get(dayOfMonth) + 1L);
            } else {
                detectionPerDay.put(dayOfMonth, 1L);
            }
        }

        for (Map.Entry<String, Long> detection: detectionPerDay.entrySet()) {
            String key = detection.getKey();

            int month = Integer.valueOf(key.substring(key.indexOf(".")+1, key.length()-1));
            int day = Integer.valueOf(key.substring(0, key.indexOf(".")));

            dataPoints.add(new DataPoint(new Date(year, month, day), detection.getValue()));
            Collections.sort(dataPoints, compareByDate);
        }
        return dataPoints.toArray(new DataPoint[0]);
    }

    /*
        Comparator to ensure the X axis are ordered
     */
    private Comparator<DataPoint> compareByDate = new Comparator<DataPoint>() {
        @Override
        public int compare(DataPoint d1, DataPoint d2) {
            return Double.valueOf(d1.getX()).compareTo(Double.valueOf(d2.getX()));
        }
    };
}
