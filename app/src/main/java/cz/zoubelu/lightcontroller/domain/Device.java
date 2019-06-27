package cz.zoubelu.lightcontroller.domain;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Device {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "actual_ip")
    private String actual_ip;

    @ColumnInfo(name = "active")
    private boolean isActive;

    public Device() {
    }

    public Device(String name, String actual_ip) {
        this.name = name;
        this.actual_ip = actual_ip;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getActual_ip() {
        return actual_ip;
    }

    public void setActual_ip(String actual_ip) {
        this.actual_ip = actual_ip;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
