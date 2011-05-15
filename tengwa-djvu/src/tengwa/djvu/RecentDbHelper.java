package tengwa.djvu;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class RecentDbHelper extends SQLiteOpenHelper{
    public static final String DATABASE_NAME = "tengwa_djvu";
    public static final String TABLE_NAME = "recent_files";
    public static final String KEY_ROWID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_PATH = "path";
    public static final String KEY_PAGE = "page";
    public static final String KEY_DATE = "date";

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE = "CREATE TABLE " + TABLE_NAME + "(" + KEY_ROWID
            + " integer primary key autoincrement, " + KEY_NAME + " text not null, " + KEY_PATH
            + " text not null, " + KEY_PAGE + " integer not null, " + KEY_DATE
            + " integer not null);";
    private static final String DATABASE_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public RecentDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(RecentDbHelper.class.getName(), "Upgrading database from " + "version " + oldVersion
                + " to " + newVersion + ", which will " + "destroy all old data");
        db.execSQL(DATABASE_DROP);
        onCreate(db);
    }
}
