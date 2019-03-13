package cz.zoubelu.lightcontroller.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import cz.zoubelu.lightcontroller.domain.LightingDay;

@Dao
public interface LightingValuesDao {

    @Query("SELECT * FROM LightingDay order by day asc")
    List<LightingDay> findAll();

    @Query("SELECT * FROM LightingDay where day = (select max(day) from LightingDay)")
    List<LightingDay> findForLastDay();

    @Insert
    void save(LightingDay lightingDay);

    @Query("SELECT max(day) from LightingDay")
    long findLastDaySaved();
}
