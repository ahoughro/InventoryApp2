package com.example.android.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.view.View.OnClickListener;

import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * {@link InventoryCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of inventory data as its data source. This adapter knows
 * how to create list items for each row of inventory data in the {@link Cursor}.
 */
public class InventoryCursorAdapter extends CursorAdapter {


    /**
     * Constructs a new {@link InventoryCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the inventory data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current item can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);

        Button makeSale = (Button) view.findViewById(R.id.makeSale);

        // Find the columns of inventory attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_QUANTITY);


        // Read the inventory attributes from the Cursor for the current item
        String inventoryName = cursor.getString(nameColumnIndex);
        String inventoryPrice = cursor.getString(priceColumnIndex);
        String inventoryQuantity = cursor.getString(quantityColumnIndex);

        // If the inventory price is empty string or null, then use some default text
        // that says "Unknown price", so the TextView isn't blank.
        if (TextUtils.isEmpty(inventoryPrice)) {
            inventoryPrice = context.getString(R.string.unknown_price);
        }
        // Update the TextViews with the attributes for the current item
        nameTextView.setText(inventoryName);
        priceTextView.setText(inventoryPrice);
        quantityTextView.setText(inventoryQuantity);

// Check for the button click to reduce inventory
        makeSale.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // code here executes on main thread after button click

                String inventoryQuantity = cursor.getString(quantityColumnIndex);
                int quantity = Integer.parseInt(inventoryQuantity);

                if (quantity > 0) {
                    quantity = quantity - 1;

                    quantityTextView.setText(quantity);
                } else if (quantity <= 0) {
                    quantity = 0;
                    quantityTextView.setText(quantity);
                }
            }
        });
    }
}


