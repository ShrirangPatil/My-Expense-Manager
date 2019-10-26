package com.example.android.myexpensemanager;

import android.provider.BaseColumns;

/**
 * This acts as Contract class for ExpenseDbHelper class for
 * creating and maintaining database.
 */
public final class ExpenseContract {
    private ExpenseContract(){}

    public static class ExpenseEntry implements BaseColumns {
        public static final String TABLE_NAME = "mxm_manager";
        public static final String COLUMN_NAME_COST = "mxm_cost";
        public static final String COLUMN_NAME_DESCRIPTION = "mxm_description";
        public static final String COLUMN_NAME_DATE = "mxm_date";
    }
}
