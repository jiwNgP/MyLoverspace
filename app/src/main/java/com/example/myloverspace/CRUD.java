package com.example.myloverspace;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class CRUD {
    SQLiteOpenHelper dbHandler;
    SQLiteDatabase db;

    private static final String[] columns = {
            NoteDatabase.ID,
            NoteDatabase.CONTENT,
            NoteDatabase.TIME,
            NoteDatabase.MODE
    };

    public CRUD(Context context){
        dbHandler = new NoteDatabase(context);  //init
    }

    public void open(){
        db = dbHandler.getWritableDatabase();
    }

    public void close(){
        dbHandler.close();
    }

    //当前note 插入database  增
    public Note addNote(Note note){
        ContentValues contentValues = new ContentValues();
        contentValues.put(NoteDatabase.CONTENT, note.getContent());
        contentValues.put(NoteDatabase.TIME, note.getTime());
        contentValues.put(NoteDatabase.MODE, note.getTag());
        long insertid = db.insert(NoteDatabase.TABLE_NAME, null, contentValues);
        note.setId(insertid);
        return note;
    }

    //查
    public Note getNote(long id){
        Cursor cursor = db.query(NoteDatabase.TABLE_NAME,columns,NoteDatabase.ID +"=?",
                new String[]{String.valueOf(id)}, null,null,null);

        if(cursor != null) cursor.moveToFirst();
        Note e = new Note(cursor.getString(1),cursor.getString(2),cursor.getInt(3));
        return e;
    }

    //顾名思义 得到全部数据
    public List<Note> getALLNotes(){
        Cursor cursor = db.query(NoteDatabase.TABLE_NAME, columns,null,null,null,null,null);

        List<Note> notes = new ArrayList<>();
        if(cursor.getCount() > 0){
            while(cursor.moveToNext()){
                Note note = new Note();
                note.setId(cursor.getLong(cursor.getColumnIndex(NoteDatabase.ID)));
                note.setContent(cursor.getString(cursor.getColumnIndex(NoteDatabase.CONTENT)));
                note.setTime((cursor.getString(cursor.getColumnIndex(NoteDatabase.TIME))));
                note.setTag(cursor.getInt(cursor.getColumnIndex(NoteDatabase.MODE)));
                notes.add(note);
            }
        }
        return notes;
    }

    //修改已存在数据库中的数据
    public int updateNote(Note note){
        ContentValues values = new ContentValues();
        values.put(NoteDatabase.CONTENT, note.getContent());
        values.put(NoteDatabase.TIME, note.getTime());
        values.put(NoteDatabase.MODE, note.getTag());

        return db.update(NoteDatabase.TABLE_NAME, values,
                NoteDatabase.ID + "=?", new String[]{String.valueOf(note.getId())});
    }
    //删
    public void removeNote(Note note){
        db.delete(NoteDatabase.TABLE_NAME, NoteDatabase.ID + "=" + note.getId(),null);
    }

}
