package com.example.android.myexpensemanager;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * ArrayAdapter class of type <Expense> used to get each
 * list item view and populate them with data
 * @param <E>
 */
public class ExpenseAdapter<E> extends ArrayAdapter<Expense> {
    final private String TAG = "ExpenseAdapter";
    private boolean mInitial = true;
    private int mCountExp = 0;
    private String mMonth = "";
    private String mYear = "";
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

//        TextView costView = listItemView.findViewById(R.id.mxm_display_cost);
//        DecimalFormat df = new DecimalFormat("#.####");
//        df.setRoundingMode(RoundingMode.CEILING);
//        costView.setText(""+df.format(expense.getCost()));

        TextView dateView = listItemView.findViewById(R.id.mxm_display_date);
        ImageView imageView = listItemView.findViewById(R.id.mxm_bullet_point);
        if (expense.getCost() == 0 && expense.getDesc() == "null" && expense.getDate() == "null") {
            dateView.setText("");
            imageView.setImageResource(0);
        } else {
            String date[] = expense.getDate().split("/");
            if (mInitial) {
                mMonth = date[1];
                mYear = date[2];
                mInitial = false;
            }
            if (mMonth.equals(date[1]) && mYear.equals(date[2])) {
                mCountExp += 1;
            } else {
                Log.d(TAG, ""+mInitial+mMonth+mYear);
                mMonth = date[1];
                mYear = date[2];
                mCountExp = 1;
            }
            dateView.setText("Expense " + mCountExp + " on " + getMonthString(mMonth) + " " + mYear);
        }
        return listItemView;
    }

    public static String getMonthString(String mon) {
        switch (mon) {
            case "01":
                return "Jan";
            case "02":
                return "Feb";
            case "03":
                return "Mar";
            case "04":
                return "Apr";
            case "05":
                return "May";
            case "06":
                return "Jun";
            case "07":
                return "Jul";
            case "08":
                return "Aug";
            case "09":
                return "Sep";
            case "10":
                return "Oct";
            case "11":
                return "Nov";
            case "12":
                return "Dec";
            default:
                return "";
        }
    }
}
