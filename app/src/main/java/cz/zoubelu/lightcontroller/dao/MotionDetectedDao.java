package cz.zoubelu.lightcontroller.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import cz.zoubelu.lightcontroller.domain.MotionDetected;

@Dao
public interface MotionDetectedDao {

    @Query("SELECT max(time) from MotionDetected")
    long findLastMotionDetected();

    @Query("SELECT * from MotionDetected")
    List<MotionDetected> findAll();

    @Insert
    void save(MotionDetected motion);

    @Insert
    void save(List<MotionDetected> motions);
}
