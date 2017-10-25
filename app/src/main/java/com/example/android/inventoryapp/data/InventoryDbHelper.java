package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.inventoryapp.data.InventoryContract.ProductEntry;

public class InventoryDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = InventoryDbHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "inventory.db";
    private static final int DATABASE_VERSION = 1;

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlCreateProductTable = "CREATE TABLE " + ProductEntry.TABLE_NAME + "("
                + ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + ProductEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL, "
                + ProductEntry.COLUMN_PRODUCT_PRICE + " REAL NOT NULL, "
                + ProductEntry.COLUMN_PRODUCT_IMAGE + " BLOB NOT NULL, "
                + ProductEntry.COLUMN_PRODUCT_PROVIDER + " TEXT NOT NULL);";

        Log.v(LOG_TAG,sqlCreateProductTable);
        db.execSQL(sqlCreateProductTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ProductEntry.TABLE_NAME);
        onCreate(db);
    }
}
