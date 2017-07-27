package com.example.deezy.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.deezy.inventoryapp.data.DbBitmapUtility;
import com.example.deezy.inventoryapp.data.ItemContract;

import java.io.IOException;

import static android.R.attr.data;
import static com.example.deezy.inventoryapp.R.id.price;
import static com.example.deezy.inventoryapp.R.id.quantity;

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_ITEM_LOADER = 0;
    private String nameString;
    private String priceString;
    private String quantityString;

    private Uri mCurrentItemUri;

    private EditText mNameEditText;

    private EditText mPriceEditText;

    private Bitmap imageBit = null;

    private ImageView imageView;

    private String name;

    private TextView quantityTextView;

    private TextView mImageName;

    private String imageName = null;

    private int quantity =0;

    private static final int SELECT_PICTURE = 1;

    private String selectedImagePath;
    private int PICK_IMAGE_REQUEST = 1;

    private boolean mItemHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        quantityTextView = (TextView) findViewById(R.id.quantityText);
        quantityTextView.setText(Integer.toString(quantity));

        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        if (mCurrentItemUri == null) {

            setTitle(getString(R.string.editor_activity_title_new_item));

            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_item));
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }

        mNameEditText = (EditText) findViewById(R.id.enterName);
        mPriceEditText = (EditText) findViewById(R.id.enterPrice);

        mImageName = (TextView) findViewById(R.id.imageName);


        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);


        Button plusButton = (Button) findViewById(R.id.plus);
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quantity = quantity + 1;
                quantityTextView.setText(Integer.toString(quantity));

            }
        });
        Button minusButton = (Button) findViewById(R.id.minus);
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(quantity > 0) {
                    quantity = quantity - 1;
                    quantityTextView.setText(Integer.toString(quantity));

                }
            }
        });


        Button imageButton = (Button) findViewById(R.id.select_button);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();

                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });


        Button orderButton = (Button) findViewById(R.id.order_more);
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/html");
                intent.putExtra(Intent.EXTRA_EMAIL, "orderexample@gmail.com");
                intent.putExtra(Intent.EXTRA_SUBJECT, name);
                intent.putExtra(Intent.EXTRA_TEXT, "I would like to order more of your product " + name + ".");
                startActivity(Intent.createChooser(intent, "Send Email"));
            }
        });

        imageView = (ImageView) findViewById(R.id.imgView);
        imageView.setImageBitmap(imageBit);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                imageBit = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                imageView = (ImageView) findViewById(R.id.imgView);
                imageView.setImageBitmap(imageBit);
                // Log.d(TAG, String.valueOf(bitmap));

            } catch (IOException e) {
                e.printStackTrace();
            }
            String[] projection = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            cursor.moveToFirst();

            Log.d("tag", DatabaseUtils.dumpCursorToString(cursor));

            int columnIndex = cursor.getColumnIndex(projection[0]);
            imageName = cursor.getString(columnIndex);
            if (imageName == null) {
                imageName = "No name available";
            }
            mImageName = (TextView) findViewById(R.id.imageName);
            mImageName.setText(imageName);
            cursor.close();
        }
    }

    private void saveItem() {

        nameString = mNameEditText.getText().toString().trim();
        priceString = mPriceEditText.getText().toString().trim();
        quantityString = Integer.toString(quantity);


        if (imageName == null) {
            imageName = "No name available";
        }

        if (mCurrentItemUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString) &&
                quantity <= 0 && imageBit == null) {
            Toast.makeText(this, getString(R.string.insert_info),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(nameString)){
            Toast.makeText(this, getString(R.string.insert_name),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(priceString)){
            Toast.makeText(this, getString(R.string.insert_price),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (imageBit == null) {
//            imageBit = BitmapFactory.decodeResource(getResources(), R.drawable.noimage);
            Toast.makeText(this, getString(R.string.select_image),
                    Toast.LENGTH_SHORT).show();
            return;

        }

        ContentValues values = new ContentValues();
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_NAME, nameString);
        byte[] imageBlob = DbBitmapUtility.getBytes(imageBit);
        values.put(ItemContract.ItemEntry.COLUMN_IMAGE, imageBlob);
        values.put(ItemContract.ItemEntry.COLUMN_IMAGE_NAME, imageName);


        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }else{
            Toast.makeText(this, getString(R.string.insert_quantity),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if(quantity <=0){
            Toast.makeText(this, getString(R.string.insert_quantity),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        values.put(ItemContract.ItemEntry.COLUMN_QUANTITY, quantity);
        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }
        values.put(ItemContract.ItemEntry.COLUMN_PRICE, price);

        if (mCurrentItemUri == null) {

            Uri newUri = getContentResolver().insert(ItemContract.ItemEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {

            int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);

            if (rowsAffected == 0) {

                Toast.makeText(this, getString(R.string.editor_update_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(this, getString(R.string.editor_update_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (mCurrentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_save:
                saveItem();
                if(!TextUtils.isEmpty(nameString) && !TextUtils.isEmpty(priceString) &&
                        !TextUtils.isEmpty(quantityString) && imageBit != null && quantity < 0) {
                    finish();
                }

                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ItemContract.ItemEntry._ID,
                ItemContract.ItemEntry.COLUMN_ITEM_NAME,
                ItemContract.ItemEntry.COLUMN_IMAGE,
                ItemContract.ItemEntry.COLUMN_QUANTITY,
                ItemContract.ItemEntry.COLUMN_PRICE,
                ItemContract.ItemEntry.COLUMN_IMAGE_NAME};

        return new CursorLoader(this,
                mCurrentItemUri,
                projection,
                null,
                null,
                null);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {

            int nameColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_QUANTITY);
            int imageNameColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_IMAGE_NAME);
            int imageColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_IMAGE);
            name = cursor.getString(nameColumnIndex);
            byte[] image = cursor.getBlob(imageColumnIndex);
            imageBit = DbBitmapUtility.getImage(image);


            int price = cursor.getInt(priceColumnIndex);
            quantity = cursor.getInt(quantityColumnIndex);
            String imageName = cursor.getString(imageNameColumnIndex);

            mNameEditText.setText(name);
            mPriceEditText.setText(Integer.toString(price));
            mImageName.setText(imageName);
            quantityTextView.setText(Integer.toString(quantity));
            imageView = (ImageView) findViewById(R.id.imgView);
            imageView.setImageBitmap(imageBit);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mNameEditText.setText("");
        quantityTextView.setText("");
        mPriceEditText.setText("");

    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
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
                deleteItem();
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

    private void deleteItem() {
        if (mCurrentItemUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }
}