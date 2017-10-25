package com.example.android.inventoryapp.activities;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventoryapp.R;
import com.example.android.inventoryapp.data.InventoryContract.ProductEntry;
import com.example.android.inventoryapp.model.Product;
import com.example.android.inventoryapp.utils.DbBitmapUtility;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


public class EditorActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int GET_IMAGE_REQUEST_CODE = 10;
    private static final int EXISTING_PRODUCT_LOADER = 1;

    private EditText mNameEditText;
    private EditText mQuantityEditText;
    private EditText mPriceEditText;
    private EditText mProviderEditText;
    private ImageView mImageImageView;

    private Bitmap mProductImage;
    private Uri mCurrentProductUri;

    private boolean mProductHasChanged;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        if (mCurrentProductUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_product));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_product));
            getSupportLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        mNameEditText = (EditText) findViewById(R.id.product_name);
        mQuantityEditText = (EditText) findViewById(R.id.product_quantity);
        mPriceEditText = (EditText) findViewById(R.id.product_price);
        mProviderEditText = (EditText) findViewById(R.id.product_provider);
        mImageImageView = (ImageView) findViewById(R.id.product_image);

        View fabAddImage = findViewById(R.id.fab_add_image);
        fabAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getImage = new Intent();
                getImage.setType("image/*");
                getImage.setAction(Intent.ACTION_GET_CONTENT);
                getImage.addCategory(Intent.CATEGORY_OPENABLE);
                getImage.putExtra("crop", "true");
                getImage.putExtra("outputX", 400);
                getImage.putExtra("outputY", 400);
                getImage.putExtra("scale", true);

                startActivityForResult(getImage, GET_IMAGE_REQUEST_CODE);
            }
        });

        configureQuantityButtons();

        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mProviderEditText.setOnTouchListener(mTouchListener);
        fabAddImage.setOnTouchListener(mTouchListener);
    }

    private void configureQuantityButtons() {
        View increaseButton = findViewById(R.id.button_increase_quantity);
        increaseButton.setOnTouchListener(mTouchListener);
        increaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantityString = mQuantityEditText.getText().toString().trim();
                int quantity;
                if (!TextUtils.isEmpty(quantityString)) {
                    quantity = Integer.parseInt(quantityString);
                } else {
                    quantity = 0;
                }
                mQuantityEditText.setText(String.valueOf(++quantity));
            }
        });

        View decreaseButton = findViewById(R.id.button_decrease_quantity);
        decreaseButton.setOnTouchListener(mTouchListener);
        decreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantityString = mQuantityEditText.getText().toString().trim();
                int quantity;
                if (!TextUtils.isEmpty(quantityString)) {
                    quantity = Integer.parseInt(quantityString);
                } else {
                    quantity = 0;
                }

                if (quantity <= 0)
                    return;

                mQuantityEditText.setText(String.valueOf(--quantity));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentProductUri == null) {
            MenuItem menuItemDelete = menu.findItem(R.id.action_delete);
            menuItemDelete.setVisible(false);
            MenuItem menuItemBuy = menu.findItem(R.id.action_buy);
            menuItemBuy.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveProduct();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case R.id.action_buy:
                orderProducts();
                return true;
            case android.R.id.home:
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                showUnsavedChangesDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void orderProducts() {
        String productName = mNameEditText.getText().toString();
        String email = mProviderEditText.getText().toString();
        if (!isValidEmail(email)) {
            Toast.makeText(this, getString(R.string.invalid_email), Toast.LENGTH_SHORT).show();
            return;
        }

        Intent sendEmailIntent = new Intent(Intent.ACTION_SENDTO);
        sendEmailIntent.setData(Uri.parse("mailto:" + email));
        sendEmailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.product_order_subject,
                productName));
        if (sendEmailIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(sendEmailIntent);
        }
    }

    private void saveProduct() {
        if (!isDataValid()) {
            return;
        }

        if (!mProductHasChanged) {
            finish();
            return;
        }

        String name = mNameEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String providerEmail = mProviderEditText.getText().toString().trim();
        byte[] imageBytes = DbBitmapUtility.getBytes(mProductImage);

        if (!isValidEmail(providerEmail)) {
            return;
        }
        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        double price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Double.parseDouble(priceString);
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(ProductEntry.COLUMN_PRODUCT_NAME, name);
        contentValues.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        contentValues.put(ProductEntry.COLUMN_PRODUCT_PRICE, price);
        contentValues.put(ProductEntry.COLUMN_PRODUCT_PROVIDER, providerEmail);
        contentValues.put(ProductEntry.COLUMN_PRODUCT_IMAGE, imageBytes);

        if (mCurrentProductUri == null) {
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, contentValues);

            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Integer updatedRow = getContentResolver().update(mCurrentProductUri,
                    contentValues, null, null);

            if (updatedRow == 0) {
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }

    private boolean isDataValid() {
        boolean validData = true;

        TextInputLayout nameContainer =
                (TextInputLayout) findViewById(R.id.product_name_container);
        TextInputLayout quantityContainer =
                (TextInputLayout) findViewById(R.id.product_quantity_container);
        TextInputLayout priceContainer =
                (TextInputLayout) findViewById(R.id.product_price_container);
        TextInputLayout providerContainer =
                (TextInputLayout) findViewById(R.id.product_provider_container);

        nameContainer.setError(null);
        quantityContainer.setError(null);
        priceContainer.setError(null);
        providerContainer.setError(null);

        if (isEditTextEmpty(mNameEditText)) {
            nameContainer.setError(getString(R.string.product_name_error));
            validData = false;
        }

        if (isEditTextEmpty(mQuantityEditText)) {
            quantityContainer.setError(getString(R.string.product_quantity_error));
            validData = false;
        }

        if (isEditTextEmpty(mPriceEditText)) {
            priceContainer.setError(getString(R.string.product_price_error));
            validData = false;
        }

        if (isEditTextEmpty(mProviderEditText)) {
            providerContainer.setError(getString(R.string.product_provider_error));
            validData = false;
        } else if (!isValidEmail(mProviderEditText.getText().toString())) {
            providerContainer.setError(getString(R.string.invalid_email));
            validData = false;
        }

        if (mProductImage == null) {
            Toast.makeText(this, getString(R.string.product_image_error), Toast.LENGTH_SHORT).show();
            validData = false;
        }

        return validData;
    }

    private void deletePoduct() {
        if (mCurrentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GET_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            try {
                if (mProductImage != null) {
                    mProductImage.recycle();
                }
                InputStream stream = getContentResolver().openInputStream(data.getData());
                mProductImage = BitmapFactory.decodeStream(stream);
                stream.close();

                mImageImageView.setImageBitmap(mProductImage);
            } catch (FileNotFoundException e) {
                Toast.makeText(this, getString(R.string.image_not_found), Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(this, getString(R.string.get_image_error), Toast.LENGTH_SHORT).show();
            }

            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private boolean isEditTextEmpty(EditText editText) {
        return editText.getText().toString().trim().length() == 0;
    }

    private boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_PROVIDER,
                ProductEntry.COLUMN_PRODUCT_IMAGE};

        return new CursorLoader(this, mCurrentProductUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            Product product = Product.createProductFromCursor(cursor);

            mNameEditText.setText(product.getName());
            mQuantityEditText.setText(String.valueOf(product.getQuantity()));
            mPriceEditText.setText(String.valueOf(product.getPrice()));
            mProviderEditText.setText(product.getProviderEmail());

            mProductImage = product.getImage();
            mImageImageView.setImageBitmap(mProductImage);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mQuantityEditText.setText("");
        mPriceEditText.setText("");
        mProviderEditText.setText("");
        mImageImageView.setImageResource(R.drawable.ic_image_24dp);

        mProductImage = null;
    }

    private void showUnsavedChangesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                NavUtils.navigateUpFromSameTask(EditorActivity.this);
            }
        });
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deletePoduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
