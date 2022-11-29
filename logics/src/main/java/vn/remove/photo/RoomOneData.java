package vn.remove.photo;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import vn.remove.photo.logics.BoatModelRoom;

@Database(entities = {BoatModelRoom.class}, version = 1, exportSchema = false)
public abstract class RoomOneData extends RoomDatabase {

    private static final String DB_NAME = "fire.db";
    private static RoomOneData database;
    private static final Object LOCK = new Object();

    public static RoomOneData getInstance(Context context) {
        synchronized (LOCK) {
            if (database == null) {
                database = Room.databaseBuilder(
                        context, RoomOneData.class, DB_NAME
                ).allowMainThreadQueries().fallbackToDestructiveMigration().build();
            }
        }
        return database;
    }

    public abstract RoomDao roomDao();
}
