package com.gorm.notes;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

public class DBHelper extends SQLiteOpenHelper{
    private Calendar c;

    public DBHelper(Context context) {
        super(context, "/sdcard/noteDB.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table NoteTable ("
                + "id integer primary key autoincrement,"
                + "name text,"
                + "note text,"
                + "time text,"
                + "data text" + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void createNote(String name) {
        c = Calendar.getInstance();
        ContentValues cv = new ContentValues();
        SQLiteDatabase db = this.getWritableDatabase();
        cv.put("name", name);
        cv.put("note", "");
        cv.put("time", c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND));
        cv.put("data", c.get(Calendar.YEAR) + " " + getM(c.get(Calendar.MONTH)) + " " + c.get(Calendar.DAY_OF_MONTH));
        db.insert("NoteTable", null, cv);
        this.close();
    }

    public void saveNote(String id, String note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("note", note);
        db.update("NoteTable", values, "id = ?", new String[]{id});
        this.close();
    }

    public void delNotes(ArrayList<String> ids) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (String i : ids) {
            db.delete("NoteTable", "id=?", new String[]{i});
        }
        this.close();
    }

    public void sendDB(MainActivity ma) {
        File file = new File("/storage/emulated/0/noteDB.db");
        Intent i = new Intent(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        i.setType("text/plain");
        ma.startActivity(Intent.createChooser(i, "Send Notes file via (full support not confirmed)"));
    }

    public String getNote(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM NoteTable WHERE id = '" + id + "'", null);
        c.moveToFirst();
        String r = c.getString(c.getColumnIndex("note"));
        this.close();
        c.close();
        return r;
    }

    private String getM(int i) {
        switch (i) {
            case 0:
                return "Jan";
            case 1:
                return "Feb";
            case 2:
                return "Mar";
            case 3:
                return "Apr";
            case 4:
                return "May";
            case 5:
                return "Jun";
            case 6:
                return "Jun";
            case 7:
                return "Aug";
            case 8:
                return "Sep";
            case 9:
                return "Oct";
            case 10:
                return "Nov";
            case 11:
                return "Dec";
        }
        return "ERR";
    }
}