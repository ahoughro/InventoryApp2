package com.example.android.inventoryapp.data;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

import com.example.android.inventoryapp.data.InventoryDbHelper;
import java.security.Provider;

public class InventoryProvider extends ContentProvider {

    /** Tag for the log messages */
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    /**
     * Initialize the provider and the database helper object.
     */
    private InventoryDbHelper mDbHelper;

    @Override
    public boolean onCreate(){
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    /** URI matcher code for the content URI for the inventory table */
    private static final int INVENTORY = 100;

    /** URI matcher code for the content URI for a single item in the inventory table */
    private static final int ITEM_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, INVENTORY);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY + "/#", ITEM_ID);
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch(match){
            case INVENTORY:
                // Query the inventory table directly with the given projection, selection, and selection args
                // and sort Order.
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case ITEM_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{ String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannont query unknown URI" + uri);
        }
        // Set URI notification on the cursor
        // so we know what content URI the cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);

        switch (match){
            case INVENTORY:
                return insertInventory(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }

    }

    private Uri insertInventory(Uri uri, ContentValues values){
        //Sanity Check begin:

        // Check that the name is not null
        String name = values.getAsString(InventoryEntry.COLUMN_INVENTORY_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Inventory Item requires a name");
        }

        // Check that the price is not null
        String price = values.getAsString(InventoryEntry.COLUMN_INVENTORY_PRICE);
        if (price == null) {
            throw new IllegalArgumentException("Inventory Price requires a value");
        }

        // If the quantity is provided, check that it's greater than or equal to 0
        Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_INVENTORY_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Quantity requires valid value");
        }

        // Check the Supplier Name is not null
        String supplierName = values.getAsString(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME);
        if (supplierName == null) {
            throw new IllegalArgumentException("Supplier Name requires a name");
        }

        // Check the supplier has a phone number
        String supplierPhone = values.getAsString(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_PHONE);
        if (supplierPhone == null) {
            throw new IllegalArgumentException("Supplier Phone requires a phone number");
        }
// SANITY CHECK END

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new pet with the given values
        long id = database.insert(InventoryEntry.TABLE_NAME, null, values);

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        //Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ITEM_ID:
                // Delete a single row given by the ID in the URI
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows deleted
        return rowsDeleted;
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return updateInventory(uri, contentValues, selection, selectionArgs);
            case ITEM_ID:
                // For the ITEM_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateInventory(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update inventory in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more inventory items).
     * Return the number of rows that were successfully updated.
     */
    private int updateInventory(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link InventoryEntry#COLUMN_INVENTORY_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(InventoryEntry.COLUMN_INVENTORY_NAME)) {
            String name = values.getAsString(InventoryEntry.COLUMN_INVENTORY_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Inventory requires a name");
            }
        }

        // If the {@link InventoryEntry#COLUMN_INVENTORY_PRICE} key is present,
        // check that the price value is not null.
        if (values.containsKey(InventoryEntry.COLUMN_INVENTORY_PRICE)) {
            String price = values.getAsString(InventoryEntry.COLUMN_INVENTORY_PRICE);
            if (price == null) {
                throw new IllegalArgumentException("Inventory requires a price");
            }
        }

        // If the {@link InventoryEntry#COLUMN_INVENTORY_QUANTITY} key is present,
        // check that the quantity value is valid.
        if (values.containsKey(InventoryEntry.COLUMN_INVENTORY_QUANTITY)) {
            // Check that the quantity is greater than or equal to 0
            Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_INVENTORY_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Inventory requires valid quantity");
            }
        }

        // If the {@link InventoryEntry#COLUMN_SUPPLIER_NAME} key is present,
        // check that the supplierName value is not null.
        if (values.containsKey(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME)) {
            String supplierName = values.getAsString(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME);
            if (supplierName == null) {
                throw new IllegalArgumentException("Supplier Name requires a name");
            }
        }
        // If the {@link InventoryEntry#COLUMN_SUPPLIER_PHONE} key is present,
        // check that the supplierPhone value is not null.
        if (values.containsKey(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_PHONE)) {
            String supplierPhone = values.getAsString(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_PHONE);
            if (supplierPhone == null) {
                throw new IllegalArgumentException("Supplier Phone requires a valid number");
            }
        }
        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }
        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows updated
        return rowsUpdated;
    }
}
