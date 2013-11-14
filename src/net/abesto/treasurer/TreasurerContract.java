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
        public static final String CATEGORY_ID = "category_id";
        public static final String MEMO = "memo";
        public static final String OUTFLOW = "outflow";
        public static final String INFLOW = "inflow";
        public static final String COMPUTED_FLOW = "flow";
    }

    public interface Category extends BaseColumns {
        public static final String TABLE_NAME = "categories";
        public static final String FULL_ID = TABLE_NAME + "." + _ID;
        public static final String NAME = "name";
    }

    public interface PayeeSubstringToCategory extends BaseColumns {
        public static final String TABLE_NAME = "payees_to_categories";
        public static final String CATEGORY_ID = "category_id";
        public static final String PAYEE_SUBSTRING = "payee_substring";
    }

    public interface StringSet extends BaseColumns {
        public static final String TABLE_NAME = "string_sets";
        public static final String SET_ID = "set_id";
        public static final String STRING = "string";

        public static final int UNKNOWN_PAYEE_SET = 1;
        public static final int FAILED_TO_PARSE_SMS_SET = 2;
    }
}
