package cz.zoubelu.lightcontroller.domain;

import java.util.List;

public class Statistics {
    private AppOperations appOps;
    private List<LightingDay> lightingValues;
    private List<MotionDetected> motionDetected;

    public AppOperations getAppOps() {
        return appOps;
    }

    public void setAppOps(AppOperations appOps) {
        this.appOps = appOps;
    }

    public List<LightingDay> getLightingValues() {
        return lightingValues;
    }

    public void setLightingValues(List<LightingDay> lightingValues) {
        this.lightingValues = lightingValues;
    }

    public List<MotionDetected> getMotionDetected() {
        return motionDetected;
    }

    public void setMotionDetected(List<MotionDetected> motionDetected) {
        this.motionDetected = motionDetected;
    }
}
