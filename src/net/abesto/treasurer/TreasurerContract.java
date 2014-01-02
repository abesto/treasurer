package net.abesto.treasurer;

import android.provider.BaseColumns;

public abstract class TreasurerContract {
    public static final String DATABASE_NAME = "treasurer.db";
    public static final int DATABASE_VERSION = 1;

    public interface Transaction extends BaseColumns {
        public static final String TABLE_NAME = "transactions";
        public static final String FULL_ID = TABLE_NAME + "." + _ID;
        public static final String DATE = "date";
        public static final String PAYEE = "payee";
        public static final String MEMO = "memo";
        public static final String OUTFLOW = "outflow";
        public static final String INFLOW = "inflow";
        public static final String COMPUTED_FLOW = "flow";
    }

    public interface StringSet extends BaseColumns {
        public static final String TABLE_NAME = "string_sets";
        public static final String SET_ID = "set_id";
        public static final String STRING = "string";

        public static final int FAILED_TO_PARSE_SMS_SET = 2;
    }
}
