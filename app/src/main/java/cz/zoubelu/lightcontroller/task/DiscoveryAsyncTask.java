package cz.zoubelu.lightcontroller.task;


import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
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

    public static final String SEARCHING = "Still searching...";
    private MainActivity activity;

    boolean lightHasBeenFound = false;
    Device storedDevice = null;

    private static final int TIMEOUT_MS = 1000;

    private static final String BEACON_RESPONSE = "HELL0_THERE";

    private boolean fastSearch = true;

    private String hostIP;

    public DiscoveryAsyncTask(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    protected String doInBackground(Object... params) {

        WifiManager wm = (WifiManager) activity.getApplicationContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String myIP = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        String presentDevice = tryStoredDevicesFirst();

        if (presentDevice != null) {
            return presentDevice;
        }

        fastSearch = false;
        String subnet = myIP.substring(0, myIP.lastIndexOf(".") + 1);
        String server = discoverIps(generateCloseIps(myIP), subnet);

        publishProgress(0);
        return server == null ? discoverIps(generateSubAddresses(), subnet) : server;
    }

    private String tryStoredDevicesFirst() {

        if (DbInitializer.getDb() == null) {
            DbInitializer.initDb(activity);
        }
        DbInitializer.getDb().deviceDao().deactivateAll();

        List<Device> storedDevices = DbInitializer.getDb().deviceDao().findAll();

        if (!storedDevices.isEmpty()) {
            for (Device device : storedDevices) {
                findDevice(device.getActual_ip());

                if (hostIP == null) {
                    synchronized (this) {
                        try {
                            wait(5000);
                        } catch (InterruptedException e) {
                            Log.e("DiscoveryTask", "Failed to wait for response.");
                        }
                    }
                }
                if (hostIP != null) {
                    storedDevice = device;
                    return device.getActual_ip();
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String hostIp) {
        updateUi();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        TextView discoveryText = activity.findViewById(R.id.discover_progress_text_view);
        discoveryText.setText(String.valueOf(values[0]));
    }

    private String discoverIps(List<String> ips, String subnet) {
        int i = 1;
        while (hostIP == null && i < ips.size()) {
            for (String ipEnd : ips) {
                publishProgress(i * 100 / ips.size());
                final String hostIP = subnet + ipEnd;

                String server = findDevice(hostIP);
                if (server != null) {
                    return server;
                }
                i++;
            }
        }
        return hostIP;
    }

    private List<String> generateCloseIps(String myIP) {
        List<String> ips = new ArrayList<>();
        String rawValue = myIP.substring(myIP.lastIndexOf(".") + 1);
        Integer value = Integer.valueOf(rawValue);

        for (int i = value - 5; i < value + 6; i++) {
            ips.add(String.valueOf(i));
        }
        return ips;
    }

    private String findDevice(final String address) {
        String url = "http://" + address + "/beacon";
        RequestQueue queue = Volley.newRequestQueue(activity);
        // Request a string response from the provided URL.
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.contains(BEACON_RESPONSE)) {
                            setHostIP(address);
                            if (!fastSearch) {
                                updateUi();
                            }
                            lightHasBeenFound = true;
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("DiscoveryTask", error.getMessage() != null? error.getMessage() : "Probably error");
            }
        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
        if (lightHasBeenFound) {
            queue.stop();
        }
        return null;
    }

    private void updateUi() {
        TextView discoveryText = activity.findViewById(R.id.discover_progress_text_view);

        if (getHostIP() != null) {

            if (storedDevice != null) {
                storedDevice.setActive(true);
                new UpdateDeviceAsyncTask(true, activity, null).execute(storedDevice);
                discoveryText.setText("Connected to: " + storedDevice.getName());
                activity.setActualDevice(storedDevice);
            } else {
                Device device = new Device();
                device.setActual_ip(getHostIP());
                device.setName("Mr.Lighty");
                discoveryText.setText("Connected to: " + device.getName());
                new InsertDeviceIntoDbAsyncTask().execute(device);
                activity.setActualDevice(device);
            }
            activity.enableSwitches(true);

        } else {
            discoveryText.setText(SEARCHING);
        }
    }

    private List<String> generateSubAddresses() {
        List<String> ips = new ArrayList<>();
        for (int i = 0; i < 256; i++) {
            ips.add(String.valueOf(i));
        }
        return ips;
    }

    public String getHostIP() {
        return hostIP;
    }

    public void setHostIP(String hostIP) {
        this.hostIP = hostIP;
    }
}