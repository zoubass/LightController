package cz.zoubelu.lightcontroller.task;


import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.format.Formatter;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.List;

import cz.zoubelu.lightcontroller.MainActivity;
import cz.zoubelu.lightcontroller.R;
import cz.zoubelu.lightcontroller.domain.Device;
import cz.zoubelu.lightcontroller.service.DbInitializer;

public class DiscoveryAsyncTask extends AsyncTask<Object, Integer, String> {

    private MainActivity activity;

    private static final String BEACON_RESPONSE = "HELL0_THERE";

    private String response;

    public DiscoveryAsyncTask(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    protected String doInBackground(Object... params) {

        WifiManager wm = (WifiManager) activity.getApplicationContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String myIP = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        String subnet = myIP.substring(0, myIP.lastIndexOf(".") + 1);
        String server = discoverIps(generateCloseIps(myIP), subnet);


        return server == null ? discoverIps(generateSubAddresses(), subnet) : server;
    }

    @Override
    protected void onPostExecute(String hostIp) {
        TextView discoveryText = activity.findViewById(R.id.discover_progress_text_view);

        if (hostIp != null) {
            Device device = new Device();
            device.setActual_ip(hostIp);
            device.setName("Mr.Lighty");
            discoveryText.setText(device.getName());
            DbInitializer.getDb().deviceDao().insert(device);
            activity.setActualDevice(device);
        } else {
            discoveryText.setText("No device found!");
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        TextView discoveryText = activity.findViewById(R.id.discover_progress_text_view);
        discoveryText.setText(String.valueOf(values[0]));
    }

    private String discoverIps(List<String> ips, String subnet) {
        int i = 1;
        for (String ipEnd : ips) {
            publishProgress(i * 100 / ips.size());
            String server = findDevice(subnet, ipEnd);
            if (server != null) {
                return server;
            }
            i++;
        }
        return null;
    }

    private List<String> generateCloseIps(String myIP) {
        List<String> ips = new ArrayList<>();
        String rawValue = myIP.substring(myIP.lastIndexOf(".") + 1, myIP.length());
        Integer value = Integer.valueOf(rawValue);

        for (int i = value - 5; i < value + 6; i++) {
            ips.add(String.valueOf(i));
        }
        return ips;
    }

    private String findDevice(String subnet, String ipEnd) {
        String hostIP = subnet + ipEnd;

        String url = "http://" + hostIP + "/beacon";
        RequestQueue queue = Volley.newRequestQueue(activity);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        setResponse(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println();
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);

        return response != null ? response.equals(BEACON_RESPONSE) ? hostIP : null : null;
    }

    private List<String> generateSubAddresses() {
        List<String> ips = new ArrayList<>();
        for (int i = 0; i < 256; i++) {
            ips.add(String.valueOf(i));
        }
        return ips;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response.isEmpty() ? "" : response;
    }
}