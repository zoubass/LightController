package cz.zoubelu.lightcontroller.task;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.zoubelu.lightcontroller.domain.Device;
import cz.zoubelu.lightcontroller.domain.LightingDay;
import cz.zoubelu.lightcontroller.domain.MotionDetected;
import cz.zoubelu.lightcontroller.service.AppDatabase;
import cz.zoubelu.lightcontroller.service.DbInitializer;

public class SyncStatsAsyncTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "StatsLoader";
    private static int TIMEOUT_MS = 5000;

    private Activity activity;
    private static String LIGHT_STATS_ENDPOINT = "/stats";
    private static String MOTION_STATS_ENDPOINT = "/motion_stats";
    private static String HTTP_PREFIX = "http://";


    private AppDatabase db;

    private List<LightingDay> lightingDays;
    private List<MotionDetected> motionDetections;

    private Date lastDaySaved;
    private Date lastMotionDetected;
    private boolean isRequestCompleted;
    private boolean motionSynced = false;
    private boolean lightStatsSynced = false;

    public SyncStatsAsyncTask(Activity activity) {
        lightingDays = new ArrayList<>();
        motionDetections = new ArrayList<>();
        db = DbInitializer.getDb();
        this.activity = activity;
    }


    @Override
    protected Void doInBackground(Void... voids) {
        showMessage("Starting synchronization.");
        ConnectivityManager connManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connManager != null) {
            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            long lastDateInDb = db.lightingValuesDao().findLastDaySaved();
            lastDaySaved = new Date(lastDateInDb);

            long lastMotionInDb = db.motionDetectedDao().findLastMotionDetected();
            lastMotionDetected = new Date(lastMotionInDb);

            if (mWifi.isConnected() && db != null) {
                Device activeDevice = DbInitializer.getDb().deviceDao().findActive();
                while (activeDevice == null) {
                    try {
                        synchronized (this) {
                            wait(3000);
                            activeDevice = DbInitializer.getDb().deviceDao().findActive();
                        }
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Failed to wait on thread.");
                    }
                }
                sendRequestToGetStats(activeDevice.getActual_ip());
            }
        }

        createFinalMessage();
        return null;
    }

    private void sendRequestToGetStats(String hostIP) {
        RequestQueue queue = Volley.newRequestQueue(activity.getApplicationContext());

        final String lightStatsUrl = HTTP_PREFIX + hostIP + LIGHT_STATS_ENDPOINT;
        final String motionStatsUrl = HTTP_PREFIX + hostIP + MOTION_STATS_ENDPOINT;


        final StringRequest lightStatsRequest = createRequest(lightStatsUrl, false);
        queue.add(lightStatsRequest);

        while (!isRequestCompleted) {
            synchronized (this) {
                try {
                    wait(3000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Failed to wait for response.");
                }
            }
        }
        final StringRequest motionStatsRequest = createRequest(motionStatsUrl, true);
        queue.add(motionStatsRequest);
    }


    private StringRequest createRequest(final String url, final boolean isMotionStats) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null) {
                            parseResponse(response, isMotionStats);
                            isRequestCompleted = true;
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error sending request to url: " + url + " Message: " + error.getMessage());
            }
        });

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        return stringRequest;
    }

    private void parseResponse(String response, boolean isMotionStats) {
        if (isMotionStats) {
            parseMotionResponse(response);
        } else {
            parseResponse(response);
        }
    }

    private void parseMotionResponse(String response) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        try {
            JSONObject jObject = new JSONObject(response);
            JSONArray data = jObject.getJSONArray("time");

            for (int i = 0; i < data.length() - 1; i++) {
                if (data != null && data.get(i) != null) {
                    Date detectionDate = null;
                    try {
                        MotionDetected motionDetected = new MotionDetected();
                        detectionDate = sdf.parse((String) data.get(i));

                        if (lastMotionDetected.before(detectionDate)) {
                            motionDetected.setTime(detectionDate.getTime());
                            motionDetections.add(motionDetected);
                            lastMotionDetected = detectionDate;
                            motionSynced = true;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing date: " + detectionDate);
                    }
                }
            }
            new SaveMotionStatsAsyncTask().execute(motionDetections);

        } catch (Exception e) {
            Log.e(TAG, "Error parsing response: " + e.getMessage());
        }

    }

    private void parseResponse(String response) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            JSONObject jObject = new JSONObject(response);
            String dayTimeStamp = jObject.getString("day");
            String day = dayTimeStamp.substring(0, dayTimeStamp.indexOf("T"));
            JSONArray data = jObject.getJSONArray("data");

            for (int i = 0; i < data.length(); i++) {
                LightingDay lightingDay = new LightingDay();

                String dutyHour = (String) data.get(i);
                String[] dutyHourVals = dutyHour.split("-");

                int duty = Integer.valueOf(dutyHourVals[0]);
                int hour = Integer.valueOf(dutyHourVals[1]);

                Date date = sdf.parse(day + " " + String.valueOf(hour) + ":00:00");
                Date dayOnly = sdf.parse(day + " 00:00:00");

                if (lastDaySaved.before(date)) {
                    lightingDay.setDate(date.getTime());
                    lightingDay.setDay(dayOnly.getTime());
                    lightingDay.setHour(hour);
                    lightingDay.setValue(duty);

                    lightingDays.add(lightingDay);
                    lastDaySaved = date;
                    lightStatsSynced = true;
                }
            }

            new SaveLightingDayAsyncTask().execute(lightingDays);

        } catch (Exception e) {
            Log.e(TAG, "Error parsing response: " + e.getMessage());
        }
    }


    private void createFinalMessage() {
        final StringBuilder builder = new StringBuilder();

        builder.append("Sync completed!");
        builder.append(" ");
        if (motionSynced) {
            builder.append("|Motion|");
        }
        if (lightStatsSynced) {
            builder.append("|Lighting|");
        }
        if (!motionSynced && !lightStatsSynced) {
            builder.append("No changes");
        }

        showMessage(builder.toString());
    }

    private void showMessage(final String message) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(activity.getApplicationContext(), message,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
