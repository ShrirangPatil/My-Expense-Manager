package com.example.android.myexpensemanager;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

public class GraphExpense extends AppCompatActivity {
    private static String TAG = GraphExpense.class.getName();
    private static String mRequestUrl = "http://192.168.43.199:8000/";
    private ImageView mImageView = null;
    private Bitmap mImageData = null;
    private ArrayList<Integer> mCostArrayList = new ArrayList<>();
    private ArrayList<String> mDateArrayList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_expense);
        //getDataPoints();
        mImageView = findViewById(R.id.mxm_graph_image);
        getDataPoints();
        setGraphFromCoordinates();
    }

    Runnable setImageViewWithBitMap = new Runnable() {
        @Override
        public void run() {
            mImageView.setImageBitmap(mImageData);
        }
    };

    private void makeHttpRequest(URL url) throws IOException {
        Log.i(TAG, "makeHttpRequest called");
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json; utf-8");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setDoOutput(true);
            JSONArray xData = new JSONArray();
            for (int i=0; i<mCostArrayList.size();i++) {
                xData.put(mCostArrayList.get(i));
            }
            JSONArray yData = new JSONArray();
            for (int i=0;i<mDateArrayList.size();i++) {
                yData.put(mDateArrayList.get(i));
            }
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("val_daily_expen", xData);
                jsonObject.put("key_daily_expen", yData);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            OutputStream outputStream = urlConnection.getOutputStream();
            byte[] sendData = jsonObject.toString().getBytes("utf-8");
            outputStream.write(sendData);
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Log.i(TAG, "makeHttpRequest success");
                try {
                    InputStream inputStream = urlConnection.getInputStream();
                    mImageData = BitmapFactory.decodeStream(inputStream);
                    Log.i(TAG, "image data "+mImageData.toString());
                    runOnUiThread(setImageViewWithBitMap);
                } catch (Exception e) {
                    Log.e(TAG,"Error in deocode input stream "+e.toString());
                    e.printStackTrace();
                }
            } else {
                Log.i(TAG, "makeHttpRequest failed with code "+urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private  void  setGraphFromCoordinates() {
        Log.i(TAG, "setGraphFromCoordinates called");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    makeHttpRequest(ExpenseCurrency.createUrl(mRequestUrl));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(runnable, "GraphHttpRequestThread");
        thread.start();
    }

    private void getDataPoints() {
        String sortOrder =
                ExpenseContract.ExpenseEntry.COLUMN_NAME_DATE + " ASC";
        ExpenseDbHelper dbHelper = new ExpenseDbHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                ExpenseContract.ExpenseEntry.TABLE_NAME,
                null,null,null,null,null, sortOrder);
        mCostArrayList.clear();
        mDateArrayList.clear();
        while(cursor.moveToNext()) {
            double cost = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(ExpenseContract.ExpenseEntry.COLUMN_NAME_COST));
            String date = cursor.getString(
                    cursor.getColumnIndexOrThrow(ExpenseContract.ExpenseEntry.COLUMN_NAME_DATE));
            date = AddExpense.inverseDate(date);
            mCostArrayList.add(new Integer((int)cost));
            mDateArrayList.add(date);
            Log.i(TAG, "Cost "+cost+" Date "+date);
        }
    }
}