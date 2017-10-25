package com.example.android.inventoryapp.adapters;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.R;
import com.example.android.inventoryapp.activities.EditorActivity;
import com.example.android.inventoryapp.data.InventoryContract.ProductEntry;
import com.example.android.inventoryapp.model.Product;

import java.text.NumberFormat;

public class ProductCursorAdapter extends CursorAdapter {

    private Context mContext;

    public ProductCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(mContext).inflate(R.layout.product_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        final Product currentProduct = Product.createProductFromCursor(cursor);

        final Integer id = cursor.getInt(cursor.getColumnIndex(ProductEntry._ID));
        final Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);

        TextView tvName = (TextView) view.findViewById(R.id.product_item_name);
        TextView tvQuantity = (TextView) view.findViewById(R.id.product_item_quantity);
        TextView tvPrice = (TextView) view.findViewById(R.id.product_item_price);
        View sellButton = view.findViewById(R.id.product_item_sell_button);

        tvName.setText(currentProduct.getName());
        tvQuantity.setText(mContext.getString(R.string.product_item_quantity_text,
                currentProduct.getQuantity()));
        tvPrice.setText(mContext.getString(R.string.product_item_price_text,
                configurePrice(currentProduct.getPrice())));

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, EditorActivity.class);
                intent.setData(currentProductUri);
                mContext.startActivity(intent);
            }
        });

        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sellProduct(currentProductUri, currentProduct.getQuantity());
            }
        });

    }

    private void sellProduct(Uri productUri, int quantity) {
        if (quantity <= 0) {
            Toast.makeText(mContext, mContext.getString(R.string.sell_product_error), Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, --quantity);

        mContext.getContentResolver().update(productUri, values, null, null);
    }

    private String configurePrice(double price) {
        NumberFormat format = NumberFormat.getCurrencyInstance();
        format.setMaximumFractionDigits(2);
        return format.format(price);
    }
}
