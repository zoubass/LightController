package cz.zoubelu.lightcontroller.task;

import android.os.AsyncTask;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.List;

import cz.zoubelu.lightcontroller.MainActivity;
import cz.zoubelu.lightcontroller.R;
import cz.zoubelu.lightcontroller.domain.Device;
import cz.zoubelu.lightcontroller.service.DbInitializer;

public class CheckDeviceAsyncTask extends AsyncTask<Object, Integer, String> {

    private MainActivity activity;

    private Device device;

    private static final String BEACON_RESPONSE = "HELL0_THERE";

    private String response;

    public CheckDeviceAsyncTask(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    protected String doInBackground(Object... objects) {
        List<Device> devices = DbInitializer.getDb().deviceDao().findAll();

        if (!devices.isEmpty()){
            device = devices.get(0);
        }

        if (device != null) {
            String hostIP = device.getActual_ip();

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
            return response != null ? response.equals(BEACON_RESPONSE) ? device.getName() : null : null;
        }
        return null;
    }


    @Override
    protected void onPostExecute(String result) {
        if (device != null) {
            TextView discoveryText = activity.findViewById(R.id.discover_progress_text_view);
            discoveryText.setText(result);
            activity.setActualDevice(device);
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        TextView discoveryText = activity.findViewById(R.id.discover_progress_text_view);
        discoveryText.setText("Checking saved device...");
    }


    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response.isEmpty() ? "" : response;
    }
}
