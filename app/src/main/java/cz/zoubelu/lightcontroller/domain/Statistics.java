package cz.zoubelu.lightcontroller.domain;

import java.util.List;

public class Statistics {
    private AppOperations appOps;
    private List<LightingValues> lightingValues;
    private List<MotionDetected> motionDetected;

    public AppOperations getAppOps() {
        return appOps;
    }

    public void setAppOps(AppOperations appOps) {
        this.appOps = appOps;
    }

    public List<LightingValues> getLightingValues() {
        return lightingValues;
    }

    public void setLightingValues(List<LightingValues> lightingValues) {
        this.lightingValues = lightingValues;
    }

    public List<MotionDetected> getMotionDetected() {
        return motionDetected;
    }

    public void setMotionDetected(List<MotionDetected> motionDetected) {
        this.motionDetected = motionDetected;
    }
}
