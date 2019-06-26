package cz.zoubelu.lightcontroller.service;

import android.app.Activity;
import android.arch.persistence.room.Room;

import cz.zoubelu.lightcontroller.MainActivity;

public class DbInitializer {
    private static AppDatabase db;


    public static AppDatabase initDb(Activity activity) {
        db = Room.databaseBuilder(activity, AppDatabase.class, "testik")
                .allowMainThreadQueries()
                .build();
        return db;
    }


    public static AppDatabase getDb() {
        return db;
    }

    public static void closeDb() {
        if (db != null) {
            db.close();
        }
    }
}
