package cz.zoubelu.lightcontroller.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.zoubelu.lightcontroller.domain.Device;
import cz.zoubelu.lightcontroller.domain.LightingDay;
import cz.zoubelu.lightcontroller.task.SaveLightingDayAsyncTask;

public class BackgroundStatsLoaderService extends IntentService {

    private AppDatabase db;

    private List<LightingDay> lightingDays;

    private Date lastDaySaved;

    public BackgroundStatsLoaderService() {
        super("StatsUpdateService");
        lightingDays = new ArrayList<>();
        db = DbInitializer.getDb();
    }

    public BackgroundStatsLoaderService(String name) {
        super(name);
        lightingDays = new ArrayList<>();
        db = DbInitializer.getDb();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        long lastDayInDb = db.lightingValuesDao().findLastDaySaved();
        lastDaySaved = new Date(lastDayInDb);


        if (mWifi.isConnected() && db != null) {
            Device device = db.deviceDao().findAll().get(0);
            sendRequestToGetStats(device.getActual_ip());
        }
    }


    private void sendRequestToGetStats(String hostIP) {
        String url = "http://" + hostIP + "/stats";
        RequestQueue queue = Volley.newRequestQueue(this);

        // Request a string response from the provided URL.
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null) {
                            parseResponse(response);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println();
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
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

                if (lastDaySaved.before(date)) {
                    lightingDay.setDay(date.getTime());
                    lightingDay.setHour(hour);
                    lightingDay.setValue(duty);

                    lightingDays.add(lightingDay);
                } else {
                    lastDaySaved = date;
                }
            }
            new SaveLightingDayAsyncTask().execute(lightingDays);

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
