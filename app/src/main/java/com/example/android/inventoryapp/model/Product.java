package com.example.android.inventoryapp.model;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.android.inventoryapp.data.InventoryContract.ProductEntry;
import com.example.android.inventoryapp.utils.DbBitmapUtility;

import static android.R.attr.id;

public class Product {

    private String name;
    private int quantity;
    private double price;
    private String providerEmail;
    private Bitmap image;

    public Product(String name, int quantity, double price, String providerEmail, Bitmap image) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.providerEmail = providerEmail;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public String getProviderEmail() {
        return providerEmail;
    }

    public Bitmap getImage() {
        return image;
    }

    public static Product createProductFromCursor(Cursor cursor){
        if(cursor == null){
            return null;
        }

        String name = null;
        if (cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME) != -1){
            name = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME));
        }

        int quantity = 0;
        if (cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY) != -1){
            quantity = cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY));
        }

        double price = 0;
        if (cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE) != -1){
            price = cursor.getDouble(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE));
        }

        String providerEmail = null;
        if (cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PROVIDER) != -1){
            providerEmail = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PROVIDER));
        }

        Bitmap image = null;
        if (cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE) != -1){
            byte[] imageBytes = cursor.getBlob(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE));
            image = DbBitmapUtility.getImage(imageBytes);
        }

        return new Product(name, quantity, price, providerEmail, image);
    }
}
