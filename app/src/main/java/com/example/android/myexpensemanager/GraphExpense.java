package com.example.android.myexpensemanager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class GraphExpense extends AppCompatActivity {
    private static String TAG = GraphExpense.class.getName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_expense);
        Intent intent = getIntent();
        Bundle bundle = null;//intent.getExtras().getBundle("some_key");
//        ArrayList<DataPoint> arrayListPoints = new ArrayList<>();
        double maxCost = 0;
        if (bundle != null) {
            /* came from search activity */
        } else {
            ExpenseDbHelper dbHelper = new ExpenseDbHelper(getApplicationContext());
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query(
                    ExpenseContract.ExpenseEntry.TABLE_NAME,
                    null,null,null,null,null,null);
            int previousMonth = 0;
            double sumMonthCost = 0;
            boolean isAdd = false;
            int month = 0;
            int year = 0;
            while(cursor.moveToNext()) {
                double cost = cursor.getDouble(
                        cursor.getColumnIndexOrThrow(ExpenseContract.ExpenseEntry.COLUMN_NAME_COST));
                String date = cursor.getString(
                        cursor.getColumnIndexOrThrow(ExpenseContract.ExpenseEntry.COLUMN_NAME_DATE));
                Log.i(TAG, "cost = "+cost);
                date = AddExpense.inverseDate(date);
                String[] dates  = date.split("/");
                month = Integer.parseInt(dates[1]);
                year = Integer.parseInt(dates[2]);
                if (previousMonth == month) {
                    Log.i(TAG, "==");
                    sumMonthCost += cost;
                    isAdd = false;
                } else if (previousMonth != 0) {
                    Log.i(TAG, "elif");
                    Log.i(TAG, "x = " + month*10000+year + " y = " + sumMonthCost);
//                    arrayListPoints.add(new DataPoint(month*10000+year, sumMonthCost));
                    isAdd = true;
                    sumMonthCost = 0;
                    sumMonthCost += cost;
                    previousMonth = month;
                } else {
                    Log.i(TAG, "else");
                    sumMonthCost += cost;
                    previousMonth = month;
                    isAdd = false;
                }
                if (cost > maxCost) maxCost = cost;
            }
            if (!isAdd && month != 0 && year != 0) {
                Log.i(TAG, "x = " + (month*10000+year) + " y = " + sumMonthCost);
//                arrayListPoints.add(new DataPoint(month*10000+year, sumMonthCost));
            }
        }
    }
}