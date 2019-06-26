package cz.zoubelu.lightcontroller.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import cz.zoubelu.lightcontroller.domain.Device;

@Dao
public interface DeviceDao {

    @Insert
    void save(Device device);

    @Query("SELECT * FROM  device")
    List<Device> findAll();

    @Query("DELETE FROM Device")
    void deleteAll();

    @Query("SELECT * from Device where name = :paramName")
    List<Device> findByName(String paramName);

    @Query("UPDATE Device set active = 0")
    void deactivateAll();

    @Query("SELECT * FROM Device where active=1")
    Device findActive();

    @Query("UPDATE Device set active = 1 where id = :id")
    void updateState(long id);

    @Query("Select * from Device where name=:name and actual_ip = :address")
    Device findByNameAndAddress(String name, String address);

    @Query("UPDATE Device set name=:name, actual_ip =:actual_ip where id =:id")
    void updateInfo(String name, String actual_ip, long id);
}
