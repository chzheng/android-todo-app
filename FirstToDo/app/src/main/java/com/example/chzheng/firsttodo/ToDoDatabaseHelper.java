package com.example.chzheng.firsttodo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chzheng on 11/9/2015.
 */

public class ToDoDatabaseHelper extends SQLiteOpenHelper {
    private static ToDoDatabaseHelper sInstance;

    public static synchronized ToDoDatabaseHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new ToDoDatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private static final String TAG = "ToDoDatabaseHelper";

    // Database Info
    private static final String DATABASE_NAME = "ToDoDatabase";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_TODOS = "todos";

    // ToDo Table Columns
    private static final String KEY_TODO_ID = "id";
    private static final String KEY_TODO_ITEM = "item";

    private ToDoDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Called when the database connection is being configured.
    // Configure database settings for things like foreign key support, write-ahead logging, etc.
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // Called when the database is created for the FIRST time.
    // If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TODOS_TABLE = "CREATE TABLE " + TABLE_TODOS +
                "(" +
                KEY_TODO_ID + " INTEGER PRIMARY KEY," + // Define a primary key
                KEY_TODO_ITEM + " TEXT" +
                ")";

        db.execSQL(CREATE_TODOS_TABLE);
    }

    // Called when the database needs to be upgraded.
    // This method will only be called if a database already exists on disk with the same DATABASE_NAME,
    // but the DATABASE_VERSION is different than the version of the database that exists on disk.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TODOS);
            onCreate(db);
        }
    }

    // Insert or update a todo item in the database
    // Since SQLite doesn't support "upsert" we need to fall back on an attempt to UPDATE (in case the
    // todo item already exists) optionally followed by an INSERT (in case the todo item does not already exist).
    // Unfortunately, there is a bug with the insertOnConflict method
    // (https://code.google.com/p/android/issues/detail?id=13045) so we need to fall back to the more
    // verbose option of querying for the todo item's primary key if we did an update.
    public void addorUpdateToDo(String item) {
        // The database connection is cached so it's not expensive to call getWriteableDatabase() multiple times.
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_TODO_ITEM, item);

            // First try to update the todo item in case the todo item already exists in the database
            // This assumes todo item are unique
            int rows = db.update(TABLE_TODOS, values, KEY_TODO_ITEM + "= ?", new String[]{item});

            // Check if update succeeded
            if (rows == 1) {
                // Get the primary key of the todo item we just updated
                String todoSelectQuery = String.format("SELECT %s FROM %s WHERE %s = ?",
                        KEY_TODO_ID, TABLE_TODOS, KEY_TODO_ITEM);
                Cursor cursor = db.rawQuery(todoSelectQuery, new String[]{String.valueOf(item)});
                try {
                    if (cursor.moveToFirst()) {
                        db.setTransactionSuccessful();
                    }
                } finally {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                }
            } else {
                // todo with this item did not already exist, so insert new todo item
                db.insertOrThrow(TABLE_TODOS, null, values);
                db.setTransactionSuccessful();
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add or update todo item");
        } finally {
            db.endTransaction();
        }
    }

    public List<String> getAllToDos() {
        List<String> toDos = new ArrayList<String>();

        // SELECT * FROM todos
        String TODOS_SELECT_QUERY =
                String.format("SELECT * FROM %s",
                        TABLE_TODOS);

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(TODOS_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    String item = cursor.getString(cursor.getColumnIndex(KEY_TODO_ITEM));
                    toDos.add(item);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get toDos from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return toDos;
    }

    public int deleteToDo(String item) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TODO_ITEM, item);

        return db.delete(TABLE_TODOS, KEY_TODO_ITEM + " = ?",
                new String[]{String.valueOf(item)});
    }
}