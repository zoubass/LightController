package cz.zoubelu.lightcontroller.task;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.format.DateFormat;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.zoubelu.lightcontroller.domain.LightingDay;
import cz.zoubelu.lightcontroller.service.DbInitializer;

public class LoadDataAndShowTotalAsyncTask extends AsyncTask<Boolean, Void, List<LightingDay>> {

    public static final int MAX_LIGHT_VALUE = 256;
    private Activity activity;
    private int graphId;
    private boolean isTotal;

    public LoadDataAndShowTotalAsyncTask(Activity activity, int graphId) {
        this.activity = activity;
        this.graphId = graphId;
    }

    @Override
    protected List<LightingDay> doInBackground(Boolean... isTotalGraph) {
        List<LightingDay> lightingValues;
        this.isTotal = isTotalGraph[0];

        if (DbInitializer.getDb() == null) {
            DbInitializer.initDb(activity);
        }

        if (isTotal) {
            lightingValues = DbInitializer.getDb().lightingValuesDao().findAll();
        } else {
            lightingValues = DbInitializer.getDb().lightingValuesDao().findForLastDay();
        }

        return lightingValues;
    }

    @Override
    protected void onPostExecute(List<LightingDay> lightingValues) {
        List<DataPoint> dataPoints = generateDataPoints(lightingValues);

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints.toArray(new DataPoint[0]));

        GraphView graph = activity.findViewById(graphId);

        graph.setTitle(isTotal ? "All time stats" : "Last day stats");
        graph.addSeries(series);

        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(100);

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setXAxisBoundsManual(true);

        if (isTotal) {
            graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(activity, DateFormat.getMediumDateFormat(activity)));
            graph.getGridLabelRenderer().setHorizontalAxisTitle("Days");
            graph.getGridLabelRenderer().setHorizontalAxisTitleColor(Color.WHITE);
            graph.getGridLabelRenderer().setHumanRounding(true);
            graph.getGridLabelRenderer().setLabelsSpace(50);
        }

        graph.getViewport().setMinX(dataPoints.size() > 0 ? dataPoints.get(0).getX() : 0);
        graph.getViewport().setMaxX(dataPoints.size() > 0 ? dataPoints.get(dataPoints.size() - 1).getX() : 1);
        graph.getViewport().setScrollable(true);
        graph.getViewport().setScrollableY(true);
        graph.getViewport().setScalable(true);
        graph.getViewport().setScalableY(true);

        graph.setBackgroundColor(Color.BLACK);
        graph.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
        graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
        graph.getGridLabelRenderer().reloadStyles();
    }

    private List<DataPoint> generateDataPoints(List<LightingDay> lightingValues) {
        List<DataPoint> dataPoints = new ArrayList<>();

        for (LightingDay lightingDay : lightingValues) {
            if (isTotal) {
                dataPoints.add(new DataPoint(new Date(lightingDay.getDate()), (lightingDay.getValue() * 100) / MAX_LIGHT_VALUE));
            } else {
                dataPoints.add(new DataPoint(lightingDay.getHour(), (lightingDay.getValue() * 100) / MAX_LIGHT_VALUE));
            }
        }
        return dataPoints;
    }

}
