package com.example.android.myexpensemanager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

/**
 * The main activity of application
 */
public class MainActivity extends AppCompatActivity {

    static String TAG = MainActivity.class.getName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton addButton = findViewById(R.id.mxm_add);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent to Add Activity
                Intent addIntent = new Intent();
                addIntent.setClass(MainActivity.this, AddExpense.class);
                Log.i(TAG, "AddExpense Activity Launched");
                startActivity(addIntent);
            }
        });

        ImageButton searchButton = findViewById(R.id.mxm_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent to Query Activity
                Intent queryIntent = new Intent();
                queryIntent.setClass(MainActivity.this, QueryExpense.class);
                Log.i(TAG, "QueryExpense Activity Launched");
                startActivity(queryIntent);
            }
        });

        ImageButton deleteButton = findViewById(R.id.mxm_delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent to Delete Activity
                Intent deleteIntent = new Intent();
                deleteIntent.setClass(MainActivity.this, DeleteExpense.class);
                Log.i(TAG, "DeleteExpense Activity Launched");
                startActivity(deleteIntent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyWorkerThread.quitWorker();
    }
}
