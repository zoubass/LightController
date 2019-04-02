package cz.zoubelu.lightcontroller.task;

import android.app.Activity;

import cz.zoubelu.lightcontroller.domain.Device;

public class SendRequestSwitchAsyncTask extends AbstractSwitchAsyncTask {

    private static final String URL_CONTEXT = "/led/";

    public SendRequestSwitchAsyncTask(Activity activity, boolean switchOn, Device actualDevice) {
        super(activity, switchOn, actualDevice, URL_CONTEXT);

    }
}
