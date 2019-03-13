package cz.zoubelu.lightcontroller.domain;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity()
public class LightingDay {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "day")
    private long day;

    @ColumnInfo(name = "hour")
    private int hour;

    @ColumnInfo(name = "date")
    private long date;

    private double value;

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }


    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public long getDay() {
        return day;
    }

    public void setDay(long day) {
        this.day = day;
    }
}
