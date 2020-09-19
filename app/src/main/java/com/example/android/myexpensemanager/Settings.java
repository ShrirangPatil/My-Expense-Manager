package com.example.android.myexpensemanager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;

public class Settings extends AppCompatActivity {

    private static String TAG = Settings.class.getName();
    private ExpenseDbHelper dbHelper = null;
    private EditText mNewCurrency;
    private ListView mCurrencyList;
    private Button mConvertButton;
    private Button mBaseButton;
    private TextView mOldCurrency;
    private ArrayAdapter<String> mCurrencyListAdapter;
    private ProgressDialog mProgressDialog;
    private boolean mBaseCurrencyChange = false;
    private double mOldCurrencyRate = 1.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Log.d(TAG, "onCreate Settings java file");
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        FetchAsyncTask task = new FetchAsyncTask();
        task.execute();
        ExpenseCurrency.mCurrencyChoicePref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        mNewCurrency = findViewById(R.id.mxm_new_currency);
        mOldCurrency = findViewById(R.id.mxm_old_currency);
        mCurrencyList = findViewById(R.id.mxm_currency_list);
        mCurrencyListAdapter = new ArrayAdapter<String>(this, R.layout.layout_currency_list_item,
                R.id.mxm_currency_list_item, new ArrayList<String>());
        mCurrencyList.setAdapter(mCurrencyListAdapter);
        mCurrencyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String currency = (String) mCurrencyList.getItemAtPosition(position);
                mNewCurrency.setText(currency);
                mCurrencyListAdapter.clear();
            }
        });
        mNewCurrency.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d(TAG, "CharSeq = "+charSequence.toString());
                mCurrencyListAdapter.clear();
                if (charSequence.length() != 0) {
                    mCurrencyListAdapter.addAll(ExpenseCurrency.getCurrencyMatch(charSequence));
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mConvertButton = findViewById(R.id.mxm_convert_currency);
        mConvertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = mNewCurrency.getText().toString();
                String key = ExpenseCurrency.mCurrencyNameHash.get(name);
                if (key != null) {
                    Double value = ExpenseCurrency.mCurrencyRatesHash.get(key);
                    DecimalFormat df = new DecimalFormat("#.####");
                    df.setRoundingMode(RoundingMode.CEILING);
                    String str = name+" "+df.format(value);
                    mOldCurrency.setText(str);
                    SharedPreferences.Editor editor = ExpenseCurrency.mCurrencyChoicePref.edit();
                    editor.putString(getString(R.string.preference_currency), key);
                    editor.commit();
                }
            }
        });
        mBaseButton = findViewById(R.id.mxm_base_currency);
        mBaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = mNewCurrency.getText().toString();
                String key = ExpenseCurrency.mCurrencyNameHash.get(name);
                if (key != null) {
                    mOldCurrencyRate = ExpenseCurrency.mCurrencyRatesHash.get(key);
                    ExpenseCurrency.setBaseCurrency(key);
                    SharedPreferences.Editor editor = ExpenseCurrency.mCurrencyChoicePref.edit();
                    editor.putString(getString(R.string.preference_currency), key);
                    editor.commit();
                    mBaseCurrencyChange = true;
                    FetchAsyncTask task = new FetchAsyncTask();
                    task.execute();
                }
            }
        });
    }

    private class FetchAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(Settings.this);
            mProgressDialog.setMessage("Loading Please wait...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ExpenseCurrency.extractCurrencyRatesFromJson();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mProgressDialog.dismiss();
            if (!ExpenseCurrency.mCurrencyRatesHash.isEmpty()) {
                String key = ExpenseCurrency.mCurrencyChoicePref.getString(getString(R.string.preference_currency), null);
                if (key == null) {
                    SharedPreferences.Editor editor = ExpenseCurrency.mCurrencyChoicePref.edit();
                    editor.putString(getString(R.string.preference_currency), "INR");
                    editor.commit();
                    key = "INR";
                }
                Double currencyRate = ExpenseCurrency.mCurrencyRatesHash.get(key);
                for (Map.Entry<String, String> entry : ExpenseCurrency.mCurrencyNameHash.entrySet()) {
                    if (entry.getValue().matches(key)) {
                        DecimalFormat df = new DecimalFormat("#.####");
                        df.setRoundingMode(RoundingMode.FLOOR);
                        String str = entry.getKey()+" "+df.format(currencyRate);
                        mOldCurrency.setText(str);
                    }
                }
                /**
                 * update all the values based on the base currency
                 */
                if (mBaseCurrencyChange) {
                    updateData();
                    mBaseCurrencyChange = false;
                }
            }
            super.onPostExecute(aVoid);
        }
    }
    private void updateData() {
        Handler handler = new Handler(MyWorkerThread.getWorkerThreadLooper());
        dbHelper = new ExpenseDbHelper(getApplicationContext());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                String query = "UPDATE "+ ExpenseContract.ExpenseEntry.TABLE_NAME +
                        " SET " + ExpenseContract.ExpenseEntry.COLUMN_NAME_COST + " = " +
                        " CAST( " + ExpenseContract.ExpenseEntry.COLUMN_NAME_COST + " as REAL) * "+mOldCurrencyRate;
                try {
                    db.execSQL(query);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(), "Refactored", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Thread in action "+Thread.currentThread().getName());
            }
        };
        handler.post(runnable);
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
