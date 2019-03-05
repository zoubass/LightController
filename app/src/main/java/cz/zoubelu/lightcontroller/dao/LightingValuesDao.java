package cz.zoubelu.lightcontroller.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import cz.zoubelu.lightcontroller.domain.LightingDay;

@Dao
public interface LightingValuesDao {

    @Query("SELECT * FROM LightingDay")
    List<LightingDay> findAll();

    @Insert
    void save(LightingDay lightingDay);

    @Query("SELECT max(day) from LightingDay")
    long findLastDaySaved();
}
