package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;
import com.example.android.inventoryapp.data.InventoryDbHelper;
import com.example.android.inventoryapp.data.InventoryProvider;

import java.security.Provider;

/**
 * Displays list of inventory items that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = CatalogActivity.class.getSimpleName();

    private static final int INVENTORY_LOADER = 0;

    //Adapter for the ListView
    InventoryCursorAdapter mCursorAdapter;

    //Track the quantity number for decrement
    int quantityNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, com.example.android.inventoryapp.EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the inventory data
        ListView inventoryListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        inventoryListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of inventory data in the cursor
        // There is no inventory data yet (until the loader finishes) so pass in null for the cursor
        mCursorAdapter = new InventoryCursorAdapter(this, null);
        inventoryListView.setAdapter(mCursorAdapter);


        // Setup Item click listener to open the EditorActivity to edit inventory details
        inventoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@Link EditorActivity}
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                // Form the content URI that represents the specific item clicked on
                // by appending the "id" (passed as input to this method) onto the
                // {@link InventoryEntry#CONTENT_URI}.
                //For example the URI would be "content://com.example.android.inventoryapp/inventoryapp/2"
                //If the inventory with ID 2 was clicked on
                Uri currentInventoryUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

                //Set the uri on the data field of the intent
                intent.setData(currentInventoryUri);

                // Launch the {@link EditorActivity} to display the data for the current item
                startActivity(intent);
            }
        });
        // Kick off the loader
        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);


    }

    /**
     * Helper method to insert hardcoded inventory data into the database. For debugging purposes only.
     */
    private void insertInventory() {
        //Create a Content Values Object
        ContentValues values = new ContentValues();

        //Enter in some dummy inventory info to test that calling the DB.insert is working.
        values.put(InventoryEntry.COLUMN_INVENTORY_NAME, "Dog Collar");
        values.put(InventoryEntry.COLUMN_INVENTORY_PRICE, "$2.00");
        values.put(InventoryEntry.COLUMN_INVENTORY_QUANTITY, "3");
        values.put(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME, "Acme");
        values.put(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_PHONE, "978.758.1456");

        Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all inventory in the database.
     */
    private void deleteAllInventory() {
        int rowsDeleted = getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from inventory database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertInventory();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllInventory();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies the columns from the table we care about
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_INVENTORY_NAME,
                InventoryEntry.COLUMN_INVENTORY_PRICE,
                InventoryEntry.COLUMN_INVENTORY_QUANTITY};

        // This loader will execute the ContentProviders query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                InventoryEntry.CONTENT_URI,     // Provider content URI to query
                projection,                     // Columns to include in the resulting cursor
                null,                  // No selection clause
                null,               // No selection arguments
                null);                 // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@Link InventoryCursorAdapter} with this new cursor containing updated inventory data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }

    /**
     * This method decrements the inventory value by 1, and checks that we have no negative inventory
     *
     * NEXT STEP IS TO FIGURE OUT HOW TO UPDATE THE INVENTORY IN THE DATABASE
     *//*
   public void makeSale(View view){


       LinearLayout parentRow = (LinearLayout) view.getParent();
       TextView quantityView = (TextView) parentRow.findViewById(R.id.quantity);

       String quantityString = quantityView.getText().toString();
       quantityNumber = Integer.parseInt(quantityString);
       quantityNumber -= 1;

       if (quantityNumber < 0) {
           quantityNumber = 0;
           Toast.makeText(CatalogActivity.this, "Can not be less than 0 and rowID",
                   Toast.LENGTH_SHORT).show();}
       quantityView.setText(String.valueOf(quantityNumber));

    }*/

}