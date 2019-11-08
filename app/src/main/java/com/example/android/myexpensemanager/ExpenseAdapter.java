package com.example.android.myexpensemanager;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * ArrayAdapter class of type <Expense> used to get each
 * list item view and populate them with data
 * @param <E>
 */
public class ExpenseAdapter<E> extends ArrayAdapter<Expense> {
    final private String TAG = "ExpenseAdapter";
    Context mAppContext;
    public ExpenseAdapter(Activity context, ArrayList<Expense> expenses) {
        super(context,0,expenses);
        mAppContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        final Expense expense = (Expense) getItem(position);

        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.layout_expense_list, parent, false);
        }

        TextView costView = listItemView.findViewById(R.id.mxm_display_cost);
        costView.setText(""+expense.getCost());

        TextView dateView = listItemView.findViewById(R.id.mxm_display_date);
        dateView.setText(""+expense.getDate());

        return listItemView;
    }
}
