package cz.zoubelu.lightcontroller;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.flask.colorpicker.ColorPickerView;

import cz.zoubelu.lightcontroller.domain.Device;
import cz.zoubelu.lightcontroller.task.ColorChangeRequestAsyncTask;

public class ColorActivity extends AppCompatActivity {

    private Device device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        setContentView(R.layout.activity_color);
        final String deviceIp = getIntent().getStringExtra("deviceIp");

        final ColorPickerView colorPicker = findViewById(R.id.color_picker_view);

        Button setColorBtn = findViewById(R.id.change_color_btn);
        setColorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!(deviceIp == null || deviceIp.isEmpty())) {
                    int color = colorPicker.getSelectedColor();
                    int red = Color.red(color);
                    int green = Color.green(color);
                    int blue = Color.blue(color);
                    new ColorChangeRequestAsyncTask(ColorActivity.this, deviceIp, red, green, blue).execute();
                    System.out.println(color);
                }
            }
        });
    }

}
