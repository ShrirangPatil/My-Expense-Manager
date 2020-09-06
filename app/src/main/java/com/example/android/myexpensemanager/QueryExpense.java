package com.example.android.myexpensemanager;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
 * Search activity used to search the transactions in the database
 */
public class QueryExpense extends AppCompatActivity {

    private ExpenseDbHelper dbHelper = null;
    private ArrayList<Expense> expenseList = new ArrayList<>();
    private static String TAG = QueryExpense.class.getName();
    private double totalExpense = 0;
    private ExpenseAdapter expenseAdapter;
    private EditText dateCost;
    protected DatePickerDialog.OnDateSetListener mDateListerner = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            String date = day+"/"+month+"/"+year;
            dateCost.setText(date);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_expense);
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        dateCost = findViewById(R.id.mxm_search_date);
        ImageButton searchButton = findViewById(R.id.mxm_query_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText startCost = findViewById(R.id.mxm_start);
                EditText endCost = findViewById(R.id.mxm_end);
                CheckBox checkBoxBefore = findViewById(R.id.mxm_date_before);
                CheckBox checkBoxAfter = findViewById(R.id.mxm_date_after);

                double start_cost = 0;
                double end_cost = Double.MAX_VALUE;
                String on_date = "";
                if ((!startCost.getText().toString().matches("")) && (!endCost.getText().toString().matches(""))
                        && AddExpense.checkDate(dateCost.getText().toString()) ) {
                    start_cost = Double.parseDouble(startCost.getText().toString());
                    end_cost = Double.parseDouble(endCost.getText().toString());
                    on_date = AddExpense.inverseDate(dateCost.getText().toString());
                    // start, end cost and date are entered
                    Log.i(TAG, "1. start, end cost and date are entered");
                }
                else if ((startCost.getText().toString().matches("")) && (endCost.getText().toString().matches(""))
                        && AddExpense.checkDate(dateCost.getText().toString())){
                    // start and end cost not entered
                    on_date = AddExpense.inverseDate(dateCost.getText().toString());
                    Log.i(TAG, "2. start and end cost not entered");
                }
                else if (!(startCost.getText().toString().matches("")) && !(endCost.getText().toString().matches(""))
                        && !AddExpense.checkDate(dateCost.getText().toString())){
                    start_cost = Double.parseDouble(startCost.getText().toString());
                    end_cost = Double.parseDouble(endCost.getText().toString());
                    dateCost.setText("");
                    // date not entered
                    Log.i(TAG,"3. date not entered");
                }
                else {
                    dateCost.setText("");
                    // none are entered
                    Log.i(TAG,"4. none are entered");
                }
                SearchAsyncTask task = new SearchAsyncTask();
                task.execute(new SearchObject(start_cost, end_cost, on_date,
                        checkBoxBefore.isChecked(), checkBoxAfter.isChecked()));
            }
        });

        ImageButton calenderButton = findViewById(R.id.mxm_calender);
        calenderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(QueryExpense.this, mDateListerner,
                        Calendar.getInstance().get(Calendar.YEAR),
                        Calendar.getInstance().get(Calendar.MONDAY),
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        expenseAdapter = new ExpenseAdapter(QueryExpense.this, new ArrayList<Expense>());
        final ListView listView = findViewById(R.id.mxm_list_view_expense);
        listView.setAdapter(expenseAdapter);
        listView.setClickable(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Expense expense = (Expense) listView.getItemAtPosition(position);
                Toast.makeText(QueryExpense.this,expense.getDesc(),Toast.LENGTH_LONG).show();
            }
        });

    }

    /**
     * Custom class which is used to create objects that get searched in database
     */
    private class SearchObject {
        double start_cost;
        double end_cost;
        String date;
        boolean before;
        boolean after;
        SearchObject(double start_cost, double end_cost, String date, boolean before, boolean after) {
            this.start_cost = start_cost;
            this.end_cost = end_cost;
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
            searchData(searchObjects[0].start_cost, searchObjects[0].end_cost, searchObjects[0].date,
                    searchObjects[0].before, searchObjects[0].after);
            return null;
        }
        @Override
        protected void onPostExecute(Void v) {
            expenseAdapter.clear();
            expenseAdapter.addAll(expenseList);
            Toast.makeText(getApplicationContext(), "Total expense = "+totalExpense, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Searches transactions in database using below parameters
     * @param startCostF starting cost range
     * @param endCostF ending cost range
     * @param dateF transaction date
     * @param beforeF delete all transaction before date
     * @param afterF delete all transaction after date
     * @param afterF
     */
    private void searchData(double startCostF, double endCostF, String dateF, boolean beforeF, boolean afterF) {

        dbHelper = new ExpenseDbHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;
        String sortOrder =
                ExpenseContract.ExpenseEntry.COLUMN_NAME_DATE + " DESC";
        String selection;
        String[] selectionArgs;// = {Double.toString(startCostF), Double.toString(endCostF)};
        if( dateF.matches("")) {
            selection = ExpenseContract.ExpenseEntry.COLUMN_NAME_COST + " BETWEEN ? AND ?";
            selectionArgs = new String[2];
            selectionArgs[0] = Double.toString(startCostF);
            selectionArgs[1] = Double.toString(endCostF);
        }
        else if (beforeF && !afterF) {
            selection = ExpenseContract.ExpenseEntry.COLUMN_NAME_COST + " >= ? AND "+
                    ExpenseContract.ExpenseEntry.COLUMN_NAME_COST + " <= ? AND "+
                    ExpenseContract.ExpenseEntry.COLUMN_NAME_DATE + " <= ?";
            selectionArgs = new String[3];
            selectionArgs[0] = Double.toString(startCostF);
            selectionArgs[1] = Double.toString(endCostF);
            selectionArgs[2] = dateF;
        }
        else if (!beforeF && afterF) {
            selection = ExpenseContract.ExpenseEntry.COLUMN_NAME_COST + " >= ? AND "+
                    ExpenseContract.ExpenseEntry.COLUMN_NAME_COST + " <= ? AND "+
                    ExpenseContract.ExpenseEntry.COLUMN_NAME_DATE + " >= ?";
            selectionArgs = new String[3];
            selectionArgs[0] = Double.toString(startCostF);
            selectionArgs[1] = Double.toString(endCostF);
            selectionArgs[2] = dateF;
        }
        else {
            /*String selection = "(" + ExpenseContract.ExpenseEntry.COLUMN_NAME_COST + " BETWEEN ? AND ? ) AND " +
                    ExpenseContract.ExpenseEntry.COLUMN_NAME_DATE + " = ?";*/
            selection = ExpenseContract.ExpenseEntry.COLUMN_NAME_COST + " >= ? AND "+
                    ExpenseContract.ExpenseEntry.COLUMN_NAME_COST + " <= ? AND "+
                    ExpenseContract.ExpenseEntry.COLUMN_NAME_DATE + " LIKE ?";
            selectionArgs = new String[3];
            selectionArgs[0] = Double.toString(startCostF);
            selectionArgs[1] = Double.toString(endCostF);
            selectionArgs[2] = dateF;
        }
        cursor = db.query(
                ExpenseContract.ExpenseEntry.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        expenseList.clear();
        totalExpense = 0;
        while(cursor.moveToNext()) {
            double cost = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(ExpenseContract.ExpenseEntry.COLUMN_NAME_COST));
            String desc = cursor.getString(
                    cursor.getColumnIndexOrThrow(ExpenseContract.ExpenseEntry.COLUMN_NAME_DESCRIPTION));
            String date = cursor.getString(
                    cursor.getColumnIndexOrThrow(ExpenseContract.ExpenseEntry.COLUMN_NAME_DATE));
            totalExpense += cost;
            expenseList.add(new Expense(cost, desc, AddExpense.inverseDate(date)));
        }
        cursor.close();
        Log.i(TAG, "length of ArrayList " + expenseList.size());
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
