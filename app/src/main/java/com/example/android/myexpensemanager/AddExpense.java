package com.example.android.myexpensemanager;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.Calendar;

import androidx.appcompat.app.AppCompatActivity;

/**
 * AddExpense activity adds the transactions to the database
 */
public class AddExpense extends AppCompatActivity {

    private EditText mCostET;
    private EditText mDescET;
    private EditText mDateET;
    private static String TAG = AddExpense.class.getName();
    private ExpenseDbHelper dbHelper = null;

    DatePickerDialog.OnDateSetListener mDateListerner = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            month += 1;
            String date = day+"/"+month+"/"+year;
            mDateET.setText(date);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mCostET = findViewById(R.id.mxm_cost);
        mDescET = findViewById(R.id.mxm_desc);
        mDateET = findViewById(R.id.mxm_date);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mDateET.setText(bundle.getString(VRExpense.DATE_KEY_CODE));
            mCostET.setText(Double.toString(bundle.getDouble(VRExpense.PRICE_KEY_CODE)));
        }
        ImageButton addButton = findViewById(R.id.mxm_addx);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Add the expense object
                if ((!mCostET.getText().toString().matches("")) && (!mDescET.getText().toString().matches(""))) {
                    double expenseRate = 1.0;
                    if (ExpenseCurrency.mCurrencyChoicePref != null) {
                        String key = ExpenseCurrency.mCurrencyChoicePref.getString(getString(R.string.preference_currency), null);
                        if (key != null) {
                            expenseRate = ExpenseCurrency.mCurrencyRatesHash.get(key);
                        }
                    }
                    double cost = Double.parseDouble(mCostET.getText().toString()) / expenseRate;
                    String desc = mDescET.getText().toString();
                    String date = mDateET.getText().toString();

                    if (AddExpense.checkDate(date)) {
                        insertData(new Expense(cost, desc, date));
                    } else {
                        Toast.makeText(AddExpense.this, "Invalid data!!!", Toast.LENGTH_SHORT).show();
                    }

                }
                else {
                    Toast.makeText(AddExpense.this, "Invalid data!!!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ImageButton clearButton = findViewById(R.id.mxm_clear);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Clear the edit texts
                mCostET.setText("");
                mDescET.setText("");
                mDateET.setText("");
            }
        });

        ImageButton calenderButton = findViewById(R.id.mxm_calender);
        calenderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(AddExpense.this, mDateListerner,
                        Calendar.getInstance().get(Calendar.YEAR),
                        Calendar.getInstance().get(Calendar.MONTH),
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    /**
     * adds the expense object into the database
     * @param expense consists of cost, date, description
     */
    public void insertData(final Expense expense) {


        //MyWorkerThread workerThread = new MyWorkerThread("addHandler");
        //workerThread.start();
        Handler handler = new Handler(MyWorkerThread.getWorkerThreadLooper());
        dbHelper = new ExpenseDbHelper(getApplicationContext());

        Runnable addRunnable = new Runnable() {
            @Override
            public void run() {

                SQLiteDatabase db = dbHelper.getWritableDatabase();

// Create a new map of values, where column names are the keys
                ContentValues values = new ContentValues();
                values.put(ExpenseContract.ExpenseEntry.COLUMN_NAME_COST, expense.getCost());
                values.put(ExpenseContract.ExpenseEntry.COLUMN_NAME_DESCRIPTION, expense.getDesc());
                values.put(ExpenseContract.ExpenseEntry.COLUMN_NAME_DATE, AddExpense.inverseDate(expense.getDate()));

// Insert the new row, returning the primary key value of the new row
                long newRowId = db.insert(ExpenseContract.ExpenseEntry.TABLE_NAME, null, values);
                Log.i(TAG, "Worker thread "+Thread.currentThread().getName() + " inserted row " + newRowId);

            }
        };
        //Log.i(TAG, "Worker thread id"+workerThread.mThread.getId()+" "+workerThread.getLooper());
        handler.post(addRunnable);
        Toast.makeText(AddExpense.this, "Successfully added!", Toast.LENGTH_SHORT).show();
        mCostET.setText("");
        mDateET.setText("");
        mDescET.setText("");
    }

    /**
     * Converts the date into the format which is saved as String in database
     * @param date transaction date
     * @return the formatted date
     */
    public static String inverseDate (String date) {
        String[] splitDate = date.split("/");
        //For Date
        if (splitDate[0].length() == 1) {
            splitDate[0] = "0" + splitDate[0];
        }
        //For Month
        if (splitDate[1].length() == 1) {
            splitDate[1] = "0" + splitDate[1];
        }
        return (splitDate[2] + "/" +splitDate[1]+ "/" +splitDate[0]);
    }

    /**
     * checks the validity of date (Min Year Limit is 2000 Max Year Limit is current year)
     * @param date date in the format dd/mm/yyyy
     * @return true if it is valid else false
     */
    public static boolean checkDate(String date) {
        try {
            String[] dateArray = date.split("/");
            int day = Integer.parseInt(dateArray[0]);
            int month = Integer.parseInt(dateArray[1]);
            int year = Integer.parseInt(dateArray[2]);
            int dayLimit = 0;

            if (month < 1 || month > 12) {
                Log.i(TAG,"Invalid month");
                return false;
            }
            // for 31 days months
            for (int i : new int[]{1, 3, 5, 7, 8, 10, 12}) {
                if (month == i) {
                    dayLimit = 31;
                    Log.i(TAG,"Day Limit set to 31");
                    break;
                }
            }
            // for 30 days months
            if (dayLimit == 0) {
                for (int i : new int[]{4, 6, 9, 11}) {
                    if (month == i) {
                        dayLimit = 30;
                        Log.i(TAG,"Day Limit set to 30");
                        break;
                    }
                }
                if (dayLimit == 0) {
                    if (year % 400 == 0) {
                        dayLimit = 29;
                    } else if (year % 100 == 0) {
                        dayLimit = 28;
                    } else if (year % 4 == 0) {
                        dayLimit = 29;
                    } else {
                        dayLimit = 28;
                    }
                    Log.i(TAG,"Day Limit set to "+dayLimit);
                }
            }
            if (day < 1 || day > dayLimit) {
                Log.i(TAG,"Invalid day");
                return false;
            }
            if (year < 2000 || year > Calendar.getInstance().get(Calendar.YEAR)) {
                Log.i(TAG,"Invalid year");
                return false;
            }
        }
        catch (Exception e) {
            Log.e(TAG, "Exception occurred" + e);
            return false;
        }
        return true;
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
