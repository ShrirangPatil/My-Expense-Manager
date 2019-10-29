package com.example.android.myexpensemanager;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

public class QueryExpense extends AppCompatActivity {

    private ExpenseDbHelper dbHelper = null;
    private ArrayList<Expense> expenseList = new ArrayList<>();
    private static String TAG = QueryExpense.class.getName();
    private double totalExpense = 0;
    private ExpenseAdapter expenseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_expense);

        final Button searchButton = findViewById(R.id.mxm_query_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText startCost = findViewById(R.id.mxm_start);
                EditText endCost = findViewById(R.id.mxm_end);
                EditText dateCost = findViewById(R.id.mxm_search_date);

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
                task.execute(new SearchObject(start_cost, end_cost, on_date));
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

    private class SearchObject {
        double start_cost;
        double end_cost;
        String date;
        SearchObject(double start_cost, double end_cost, String date) {
            this.start_cost = start_cost;
            this.end_cost = end_cost;
            this.date = date;
        }
    }

    private class SearchAsyncTask extends AsyncTask<SearchObject, Void, Void> {

        @Override
        protected void onPreExecute () {
            super.onPreExecute();
        }
        @Override
        protected Void doInBackground (SearchObject... searchObjects) {
            searchData(searchObjects[0].start_cost, searchObjects[0].end_cost, searchObjects[0].date);
            return null;
        }
        @Override
        protected void onPostExecute(Void v) {
            expenseAdapter.clear();
            expenseAdapter.addAll(expenseList);
            Toast.makeText(getApplicationContext(), "Total expense = "+totalExpense, Toast.LENGTH_SHORT).show();
        }
    }

    private void searchData(double startCostF, double endCostF, String dateF) {

        dbHelper = new ExpenseDbHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;
        String sortOrder =
                ExpenseContract.ExpenseEntry.COLUMN_NAME_DATE + " DESC";
        if( dateF.matches("")) {
            String selection = ExpenseContract.ExpenseEntry.COLUMN_NAME_COST + " BETWEEN ? AND ?";
            String[] selectionArgs = {Double.toString(startCostF), Double.toString(endCostF)};
             cursor = db.query(
                    ExpenseContract.ExpenseEntry.TABLE_NAME,   // The table to query
                    null,             // The array of columns to return (pass null to get all)
                    selection,              // The columns for the WHERE clause
                    selectionArgs,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            );
        }
        else {
            /*String selection = "(" + ExpenseContract.ExpenseEntry.COLUMN_NAME_COST + " BETWEEN ? AND ? ) AND " +
                    ExpenseContract.ExpenseEntry.COLUMN_NAME_DATE + " = ?";*/
            String selection = ExpenseContract.ExpenseEntry.COLUMN_NAME_COST + " >= ? AND "+
                    ExpenseContract.ExpenseEntry.COLUMN_NAME_COST + " <= ? AND "+
                    ExpenseContract.ExpenseEntry.COLUMN_NAME_DATE + " LIKE ?";
            String[] selectionArgs = {Double.toString(startCostF), Double.toString(endCostF), dateF};
            cursor = db.query(
                    ExpenseContract.ExpenseEntry.TABLE_NAME,   // The table to query
                    null,             // The array of columns to return (pass null to get all)
                    selection,              // The columns for the WHERE clause
                    selectionArgs,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            );
        }
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
}
