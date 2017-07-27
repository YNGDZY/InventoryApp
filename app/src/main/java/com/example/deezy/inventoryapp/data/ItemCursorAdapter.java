/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.deezy.inventoryapp.data;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.deezy.inventoryapp.R;

import java.sql.Blob;
import java.util.List;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static com.example.deezy.inventoryapp.R.id.imageName;
import static com.example.deezy.inventoryapp.R.id.price;
import static com.example.deezy.inventoryapp.R.id.quantity;
import static com.example.deezy.inventoryapp.data.DbBitmapUtility.getImage;

public class ItemCursorAdapter extends CursorAdapter {


    public int quantityInt;
    private Uri mCurrentItemUri;

    public ItemCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        String quantity;
        final TextView quantityTextView;

        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(price);
        quantityTextView = (TextView) view.findViewById(R.id.quantity);
        ImageView imageView = (ImageView) view.findViewById(R.id.image);
        Button sale = (Button) view.findViewById(R.id.order);

        int nameColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_QUANTITY);
        int imageColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_IMAGE);
        int idColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry._ID);
        int ident = cursor.getInt(idColumnIndex);
        final Uri currentItemUri = ContentUris.withAppendedId(ItemContract.ItemEntry.CONTENT_URI, ident);

        String itemName = cursor.getString(nameColumnIndex);
        String price = String.valueOf(cursor.getInt(priceColumnIndex));

        byte[] image = cursor.getBlob(imageColumnIndex);
        quantityInt = cursor.getInt(quantityColumnIndex);
        quantity = String.valueOf(quantityInt);
        nameTextView.setText(itemName);
        priceTextView.setText(price);
        quantityTextView.setText(quantity);

        sale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence quantityString = quantityTextView.getText();
                quantityInt = Integer.parseInt(quantityString.toString());
                quantityInt = clickMeth(quantityInt, quantityTextView, cursor);
                ContentValues values = new ContentValues();
                values.put(ItemContract.ItemEntry.COLUMN_QUANTITY, quantityInt);
                context.getContentResolver().update(currentItemUri, values, null, null);
            }
        });
        Bitmap decodedImage = DbBitmapUtility.getImage(image);
        imageView.setImageBitmap(decodedImage);

    }

    public int clickMeth(int quantityIntInt, TextView quantityTextView, Cursor cursor) {
        if (quantityIntInt >= 1) {
            quantityIntInt = quantityIntInt - 1;
            String quantity = String.valueOf(quantityIntInt);
            quantityTextView.setText(quantity);

            return quantityIntInt;
        }
        return quantityIntInt;

    }


}