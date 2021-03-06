package com.example.android.myexpensemanager;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity to delete the transaction from the database
 */
public class DeleteExpense extends AppCompatActivity {

    private ExpenseDbHelper dbHelper = null;
    private ArrayList<Expense> expenseList = new ArrayList<>();
    private static String TAG = DeleteExpense.class.getName();
    private ExpenseAdapter expenseAdapter = null;
    private EditText mDeleteDate;
    protected DatePickerDialog.OnDateSetListener mDateListerner = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            month += 1;
            String date = day+"/"+month+"/"+year;
            mDeleteDate.setText(date);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_expense);
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        ImageButton deleteButton = findViewById(R.id.mxm_delete_button);
        mDeleteDate = findViewById(R.id.mxm_delete_date);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckBox checkBoxBefore = findViewById(R.id.mxm_date_before);
                CheckBox checkBoxAfter = findViewById(R.id.mxm_date_after);
                if (expenseAdapter != null) {
                    expenseAdapter.clear();
                }
                if (AddExpense.checkDate(mDeleteDate.getText().toString())) {
                        deleteData(AddExpense.inverseDate(mDeleteDate.getText().toString()),
                                checkBoxBefore.isChecked(), checkBoxAfter.isChecked());
                }
                else {
                    Toast.makeText(getApplicationContext(), "Invalid Date!!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ImageButton searchButton = findViewById(R.id.mxm_search_delete);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText deleteDate = findViewById(R.id.mxm_delete_date);
                CheckBox checkBoxBefore = findViewById(R.id.mxm_date_before);
                CheckBox checkBoxAfter = findViewById(R.id.mxm_date_after);
                if (AddExpense.checkDate(deleteDate.getText().toString())) {
                    //searchData(AddExpense.inverseDate(deleteDate.getText().toString()));
                    expenseAdapter = new ExpenseAdapter(DeleteExpense.this, new ArrayList<Expense>());
                    SearchAsyncTask task = new SearchAsyncTask();
                    task.execute(new SearchObject(deleteDate.getText().toString(),
                            checkBoxBefore.isChecked(),
                            checkBoxAfter.isChecked()));

                    final ListView listView = findViewById(R.id.mxm_delete_list_view_expense);
                    listView.setAdapter(expenseAdapter);
                    listView.setClickable(false);
                }
                else {
                    Toast.makeText(getApplicationContext(), "Invalid Date!!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ImageButton calenderButton = findViewById(R.id.mxm_calender);
        calenderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(DeleteExpense.this, mDateListerner,
                        Calendar.getInstance().get(Calendar.YEAR),
                        Calendar.getInstance().get(Calendar.MONDAY),
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    /**
     * Custom class which is used to create objects that get searched in database
     */
    private class SearchObject {
        String date;
        boolean before;
        boolean after;
        SearchObject(String date, boolean before, boolean after) {
            this.date = date;
            this.before = before;
            this.after = after;
        }
    }

    /**
     * AsyncTask for searching data and populating the array adapter
     * using a separate thread
     */
    private class SearchAsyncTask extends AsyncTask<SearchObject, Void, Void> {

        @Override
        protected void onPreExecute () {
            super.onPreExecute();
        }
        @Override
        protected Void doInBackground (SearchObject... searchObjects) {
            searchData(AddExpense.inverseDate(searchObjects[0].date), searchObjects[0].before,
                    searchObjects[0].after);
            return null;
        }
        @Override
        protected void onPostExecute(Void v) {
            expenseAdapter.clear();
            expenseAdapter.addAll(expenseList);
        }
    }

    /**
     * deletes data from database using the WorkerThread
     * @param date transaction date
     * @param beforeF delete all transaction before date
     * @param afterF delete all transaction after date
     */
    private void deleteData(final String date, final boolean beforeF, final boolean afterF) {
        //MyWorkerThread workerThread = new MyWorkerThread("deleteHandler");
        //workerThread.start();
        Handler handler = new Handler(MyWorkerThread.getWorkerThreadLooper());
        dbHelper = new ExpenseDbHelper(getApplicationContext());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                String selection;
                if (beforeF && !afterF) {
                    selection = ExpenseContract.ExpenseEntry.COLUMN_NAME_DATE + " <= ?";
                }
                else if(!beforeF && afterF) {
                    selection = ExpenseContract.ExpenseEntry.COLUMN_NAME_DATE + " >= ?";
                }
                else {
                    selection = ExpenseContract.ExpenseEntry.COLUMN_NAME_DATE + " LIKE ?";
                }
                String[] selectionArgs = { date };
                int deletedRows = db.delete(ExpenseContract.ExpenseEntry.TABLE_NAME, selection, selectionArgs);
                Toast.makeText(getApplicationContext(), "Deleted items = "+deletedRows, Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Thread in action "+Thread.currentThread().getName());
            }
        };
        handler.post(runnable);
    }

    /**
     * Searches transactions in database using below parameters
     * @param dateF transaction date
     * @param beforeF delete all transaction before date
     * @param afterF delete all transaction after date
     */
    private void searchData (final String dateF, final boolean beforeF, final boolean afterF) {
        dbHelper = new ExpenseDbHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection;
        if (beforeF && !afterF) {
            selection = ExpenseContract.ExpenseEntry.COLUMN_NAME_DATE + " <= ?";
        }
        else if(!beforeF && afterF) {
            selection = ExpenseContract.ExpenseEntry.COLUMN_NAME_DATE + " >= ?";
        }
        else {
            selection = ExpenseContract.ExpenseEntry.COLUMN_NAME_DATE + " LIKE ?";
        }
        String[] selectionArgs = { dateF };
        String sortOrder =
                ExpenseContract.ExpenseEntry.COLUMN_NAME_DATE + " DESC";
        Cursor cursor = db.query(
                ExpenseContract.ExpenseEntry.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        expenseList.clear();
        while(cursor.moveToNext()) {
            double cost = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(ExpenseContract.ExpenseEntry.COLUMN_NAME_COST));
            String desc = cursor.getString(
                    cursor.getColumnIndexOrThrow(ExpenseContract.ExpenseEntry.COLUMN_NAME_DESCRIPTION));
            String date = cursor.getString(
                    cursor.getColumnIndexOrThrow(ExpenseContract.ExpenseEntry.COLUMN_NAME_DATE));
            expenseList.add(new Expense(cost, desc, AddExpense.inverseDate(date)));
        }
        cursor.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
