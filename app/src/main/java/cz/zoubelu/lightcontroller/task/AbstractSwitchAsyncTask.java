package cz.zoubelu.lightcontroller.task;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import cz.zoubelu.lightcontroller.domain.Device;

public class AbstractSwitchAsyncTask extends AsyncTask<Void, Void, Void> {
    private Activity activity;

    private boolean switchOn;

    private Device device;

    private String urlContext;

    public AbstractSwitchAsyncTask(Activity activity, boolean switchOn, Device device, String urlContext) {
        this.activity = activity;
        this.switchOn = switchOn;
        this.device = device;
        this.urlContext = urlContext;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        String url = "http://" + device.getActual_ip() + urlContext + (switchOn ? "on" : "off");
        RequestQueue queue = Volley.newRequestQueue(activity);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Context context = activity.getApplicationContext();
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, response != null ? response : "Affirmative", duration);
                        toast.show();
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

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }
}
