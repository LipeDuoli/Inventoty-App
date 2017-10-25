package com.example.android.inventoryapp.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;

import com.example.android.inventoryapp.R;
import com.example.android.inventoryapp.adapters.ProductCursorAdapter;
import com.example.android.inventoryapp.data.InventoryContract.ProductEntry;

public class CatalogActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PRODUCT_LOADER_ID = 1;

    private ProductCursorAdapter mProductAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        configureListView();

        getSupportLoaderManager().initLoader(PRODUCT_LOADER_ID, null, this);

    }

    private void configureListView() {
        ListView productListView = (ListView) findViewById(R.id.products_list_view);

        View emptyView = findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyView);

        mProductAdapter = new ProductCursorAdapter(this, null);
        productListView.setAdapter(mProductAdapter);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_PRICE};

        return new CursorLoader(this, ProductEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mProductAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mProductAdapter.swapCursor(null);
    }
}
