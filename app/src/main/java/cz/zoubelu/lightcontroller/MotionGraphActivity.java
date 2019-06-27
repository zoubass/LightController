package cz.zoubelu.lightcontroller;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import cz.zoubelu.lightcontroller.service.DbInitializer;
import cz.zoubelu.lightcontroller.task.LoadMotionGraphDataAsyncTask;

public class MotionGraphActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_motion_graph);

        if (DbInitializer.getDb() == null) {
            DbInitializer.initDb(this);
        }

        new LoadMotionGraphDataAsyncTask(this, R.id.graph_motion).execute();
    }

}
