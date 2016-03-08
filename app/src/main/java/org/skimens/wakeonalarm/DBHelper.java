package org.skimens.wakeonalarm;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DBHelper extends SQLiteOpenHelper implements BaseColumns {

    private static final String DATABASE_NAME = "wakeonalarm.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_DEVICE = "device";
    public static final String DEVICE_NAME = "name";
    public static final String DEVICE_IP = "ip";
    public static final String DEVICE_MAC = "mac";

    private static final String CREATE_DEVICE_TABLE_SCRIPT = "create table "
            + TABLE_DEVICE + " (" + BaseColumns._ID
            + " integer primary key autoincrement, " + DEVICE_NAME
            + " text not null, " + DEVICE_IP + " text not null, " + DEVICE_MAC
            + " text not null);";

    public static final String TABLE_ALARM = "alarm";
    public static final String ALARM_DEVICE_ID = "device_id";
    public static final String ALARM_TIME = "time";
    public static final String ALARM_DAYS = "days";
    public static final String ALARM_REPEAT = "repeat";
    public static final String ALARM_ACTIVE = "active";

    private static final String CREATE_ALARM_TABLE_SCRIPT = "create table "
            + TABLE_ALARM + " (" + BaseColumns._ID
            + " integer primary key autoincrement, " + ALARM_DEVICE_ID
            + " integer not null, " + ALARM_TIME + " text not null, " + ALARM_DAYS
            + " text not null, " + ALARM_REPEAT + " integer not null, " + ALARM_ACTIVE + " integer not null);";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                          int version) {
        super(context, name, factory, version);
    }

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                          int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DEVICE_TABLE_SCRIPT);
        db.execSQL(CREATE_ALARM_TABLE_SCRIPT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
