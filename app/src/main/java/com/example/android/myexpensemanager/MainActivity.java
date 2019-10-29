package com.example.android.myexpensemanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String about = "All the data gets stored locally on the device. Happy Privacy!";
        TextView aboutTextView = findViewById(R.id.mxm_about);
        aboutTextView.setText(about);
        Button addButton = findViewById(R.id.mxm_add);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent to Add Activity
                Intent addIntent = new Intent();
                addIntent.setClass(MainActivity.this, AddExpense.class);
                Toast.makeText(MainActivity.this, "Add", Toast.LENGTH_SHORT).show();
                startActivity(addIntent);
            }
        });

        Button searchButton = findViewById(R.id.mxm_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent to Query Activity
                Intent queryIntent = new Intent();
                queryIntent.setClass(MainActivity.this, QueryExpense.class);
                Toast.makeText(MainActivity.this, "Search", Toast.LENGTH_SHORT).show();
                startActivity(queryIntent);
            }
        });

        final Button deleteButton = findViewById(R.id.mxm_delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent to Delete Activity
                Intent deleteIntent = new Intent();
                deleteIntent.setClass(MainActivity.this, DeleteExpense.class);
                Toast.makeText(MainActivity.this, "Delete", Toast.LENGTH_SHORT).show();
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
