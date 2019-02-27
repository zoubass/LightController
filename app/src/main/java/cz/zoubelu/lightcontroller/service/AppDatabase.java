package cz.zoubelu.lightcontroller.service;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import cz.zoubelu.lightcontroller.dao.AppOperationsDao;
import cz.zoubelu.lightcontroller.dao.DeviceDao;
import cz.zoubelu.lightcontroller.dao.LightingValuesDao;
import cz.zoubelu.lightcontroller.dao.MotionDetectedDao;
import cz.zoubelu.lightcontroller.domain.AppOperations;
import cz.zoubelu.lightcontroller.domain.Device;
import cz.zoubelu.lightcontroller.domain.LightingValues;
import cz.zoubelu.lightcontroller.domain.MotionDetected;

@Database(entities = {AppOperations.class, LightingValues.class, MotionDetected.class, Device.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract DeviceDao deviceDao();

    public abstract AppOperationsDao appOperationsDao();

    public abstract LightingValuesDao lightingValuesDao();

    public abstract MotionDetectedDao motionDetectedDao();

}
