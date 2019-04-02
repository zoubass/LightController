package cz.zoubelu.lightcontroller.task;

import android.app.Activity;

import cz.zoubelu.lightcontroller.domain.Device;

public class SendRequestSwitchDetectMotionAsyncTask extends AbstractSwitchAsyncTask {
    private static final String URL_CONTEXT = "/detectMotion/";

    public SendRequestSwitchDetectMotionAsyncTask(Activity activity, boolean switchOn, Device actualDevice) {
        super(activity, switchOn, actualDevice, URL_CONTEXT);

    }
}
