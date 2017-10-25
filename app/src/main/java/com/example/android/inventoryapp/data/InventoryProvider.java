package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.ProductEntry;

public class InventoryProvider extends ContentProvider {

    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    private static final int PRODUCT = 100;
    private static final int PRODUCT_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY,
                InventoryContract.PATH_PRODUCTS, PRODUCT);

        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY,
                InventoryContract.PATH_PRODUCTS + "/#", PRODUCT_ID);
    }

    private InventoryDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor;
        switch (sUriMatcher.match(uri)){
            case PRODUCT:
                cursor = db.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCT:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues values) {
        try {

            String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Product requires a name");
            }

            Integer quantity = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            if (quantity == null || quantity < 0) {
                throw new IllegalArgumentException("Product requires a valid quantity");
            }

            Double price = values.getAsDouble(ProductEntry.COLUMN_PRODUCT_PRICE);
            if (price == null || price < 0) {
                throw new IllegalArgumentException("Product requires a valid price");
            }

            byte[] image = values.getAsByteArray(ProductEntry.COLUMN_PRODUCT_IMAGE);
            if (image == null) {
                throw new IllegalArgumentException("Product requires a valid image");
            }

            String provider = values.getAsString(ProductEntry.COLUMN_PRODUCT_PROVIDER);
            if (provider == null) {
                throw new IllegalArgumentException("Product requires a provider email");
            }
        } catch (IllegalArgumentException e){
            Log.e(LOG_TAG, e.getMessage());
            return null;
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        long id = db.insert(ProductEntry.TABLE_NAME, null, values);
        if (id == -1){
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int rowsDeleted = 0;
        switch (sUriMatcher.match(uri)){
            case PRODUCT:
                rowsDeleted = db.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = db.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCT:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        try {
            if (values.containsKey(ProductEntry.COLUMN_PRODUCT_NAME)) {
                String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
                if (name == null) {
                    throw new IllegalArgumentException("Product requires a name");
                }
            }

            if (values.containsKey(ProductEntry.COLUMN_PRODUCT_QUANTITY)) {
                Integer quantity = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_QUANTITY);
                if (quantity == null || quantity < 0) {
                    throw new IllegalArgumentException("Product requires a valid quantity");
                }
            }

            if (values.containsKey(ProductEntry.COLUMN_PRODUCT_PRICE)) {
                Double price = values.getAsDouble(ProductEntry.COLUMN_PRODUCT_PRICE);
                if (price == null || price < 0) {
                    throw new IllegalArgumentException("Product requires a valid price");
                }
            }

            if (values.containsKey(ProductEntry.COLUMN_PRODUCT_IMAGE)) {
                byte[] image = values.getAsByteArray(ProductEntry.COLUMN_PRODUCT_IMAGE);
                if (image == null) {
                    throw new IllegalArgumentException("Product requires a valid image");
                }
            }

            if (values.containsKey(ProductEntry.COLUMN_PRODUCT_PROVIDER)) {
                String provider = values.getAsString(ProductEntry.COLUMN_PRODUCT_PROVIDER);
                if (provider == null) {
                    throw new IllegalArgumentException("Product requires a provider email");
                }
            }
        } catch (IllegalArgumentException e){
            Log.e(LOG_TAG, e.getMessage());
            return 0;
        }

        if (values.size() == 0){
            return 0;
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int rowsUpdated = db.update(ProductEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case PRODUCT:
                return ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
