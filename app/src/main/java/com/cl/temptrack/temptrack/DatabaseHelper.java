package com.cl.temptrack.temptrack;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by jiezhao on 16/9/21.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private Context mContext;
    public static final String CREATE_TEMPDB = "create table TempDB (" +
            "id interger primary key autoincrement, " +
            "packagename text, " +
            "temp real)";

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TEMPDB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
