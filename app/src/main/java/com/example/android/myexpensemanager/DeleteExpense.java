package com.example.android.myexpensemanager;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

public class DeleteExpense extends AppCompatActivity {

    private ExpenseDbHelper dbHelper = null;
    private ArrayList<Expense> expenseList = new ArrayList<>();
    private static String TAG = DeleteExpense.class.getName();
    private ExpenseAdapter expenseAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_expense);

        final Button deleteButton = findViewById(R.id.mxm_delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText deleteDate = findViewById(R.id.mxm_delete_date);
                if (expenseAdapter != null) {
                    expenseAdapter.clear();
                }
                if (AddExpense.checkDate(deleteDate.getText().toString())) {
                        deleteData(AddExpense.inverseDate(deleteDate.getText().toString()));
                }
                else {
                    Toast.makeText(getApplicationContext(), "Invalid Date!!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button searchButton = findViewById(R.id.mxm_search_delete);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText deleteDate = findViewById(R.id.mxm_delete_date);
                if (AddExpense.checkDate(deleteDate.getText().toString())) {
                    //searchData(AddExpense.inverseDate(deleteDate.getText().toString()));
                    expenseAdapter = new ExpenseAdapter(DeleteExpense.this, new ArrayList<Expense>());
                    SearchAsyncTask task = new SearchAsyncTask();
                    task.execute(deleteDate.getText().toString());

                    final ListView listView = findViewById(R.id.mxm_delete_list_view_expense);
                    listView.setAdapter(expenseAdapter);
                    listView.setClickable(true);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                            Expense expense = (Expense) listView.getItemAtPosition(position);
                            Toast.makeText(DeleteExpense.this,expense.getDesc(),Toast.LENGTH_LONG).show();
                        }
                    });
                }
                else {
                    Toast.makeText(getApplicationContext(), "Invalid Date!!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private class SearchAsyncTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute () {
            super.onPreExecute();
        }
        @Override
        protected Void doInBackground (String... dates) {
            searchData(AddExpense.inverseDate(dates[0]));
            return null;
        }
        @Override
        protected void onPostExecute(Void v) {
            expenseAdapter.clear();
            expenseAdapter.addAll(expenseList);
        }
    }

    private void deleteData(final String date) {
        //MyWorkerThread workerThread = new MyWorkerThread("deleteHandler");
        //workerThread.start();
        Handler handler = new Handler(MyWorkerThread.getWorkerThreadLooper());
        dbHelper = new ExpenseDbHelper(getApplicationContext());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                String selection = ExpenseContract.ExpenseEntry.COLUMN_NAME_DATE + " <= ?";
                String[] selectionArgs = { date };
                int deletedRows = db.delete(ExpenseContract.ExpenseEntry.TABLE_NAME, selection, selectionArgs);
                Toast.makeText(getApplicationContext(), "Deleted items = "+deletedRows, Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Thread in action "+Thread.currentThread().getName());
            }
        };
        handler.post(runnable);
    }

    private void searchData (final String dateF) {
        dbHelper = new ExpenseDbHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = ExpenseContract.ExpenseEntry.COLUMN_NAME_DATE + " <= ?";
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
}
