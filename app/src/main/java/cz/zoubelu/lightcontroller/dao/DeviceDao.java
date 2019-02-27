package cz.zoubelu.lightcontroller.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import cz.zoubelu.lightcontroller.domain.Device;

@Dao
public interface DeviceDao {

    @Insert
    void insert(Device device);

    @Query("SELECT * FROM  device")
    List<Device> findAll();

}
