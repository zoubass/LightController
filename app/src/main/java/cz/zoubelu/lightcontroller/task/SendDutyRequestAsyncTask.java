package cz.zoubelu.lightcontroller.task;

import android.app.Activity;
import android.os.AsyncTask;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import cz.zoubelu.lightcontroller.domain.Device;

public class SendDutyRequestAsyncTask extends AsyncTask<Void, Void, Void> {

    private Activity activity;

    private int duty;

    private Device device;

    public SendDutyRequestAsyncTask(Activity activity, int duty, Device actualDevice) {
        this.activity = activity;
        this.duty = duty;
        this.device = actualDevice;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        String url = "http://" + device.getActual_ip() + "/led?duty=" + duty;
        RequestQueue queue = Volley.newRequestQueue(activity);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        System.out.println();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println();
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
        return null;
    }
}
