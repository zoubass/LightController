package cz.zoubelu.lightcontroller.task;

import android.app.Activity;

import cz.zoubelu.lightcontroller.domain.Device;

public class SendRequestSwitchAutoLightAsyncTask extends AbstractSwitchAsyncTask {

    private static final String URL_CONTEXT = "/led/auto/";

    public SendRequestSwitchAutoLightAsyncTask(Activity activity, boolean switchOn, Device actualDevice) {
        super(activity, switchOn, actualDevice, URL_CONTEXT);
    }
}
