package com.example.android.myexpensemanager;

/**
 * This class is used for maintaining the Object of Expense for population the
 * ArrayList which will be in turn used for ArrayAdaptor for population ListView
 */
public class Expense {
    private double mCost;
    private String mDesc;
    private String mDate;
    public Expense(double cost, String desc, String date){
        mCost = cost;
        mDesc = desc;
        mDate = date;
    }

    public double getCost() {
        return mCost;
    }
    public String getDesc() {
        return mDesc;
    }
    public String getDate() {
        return mDate;
    }
}
