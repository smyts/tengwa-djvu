package tengwa.djvu;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.*;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class OpenFileActivity extends Activity {
    public static final int PICK_FILE = 0;
    private RecentDbAdapter mDbAdapter;
    private Cursor mRecentCursor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.open_file);
        Button btn = (Button) findViewById(R.id.browse_button);
        btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, PICK_FILE);
            }
        });

        final ListView lv = (ListView) findViewById(R.id.recent_list);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Cursor c  = (Cursor) lv.getAdapter().getItem(position);
                int rowid = c.getColumnIndexOrThrow(RecentDbAdapter.KEY_ROWID),
                    pathid = c.getColumnIndexOrThrow(RecentDbAdapter.KEY_PATH),
                    pageid = c.getColumnIndexOrThrow(RecentDbAdapter.KEY_PAGE);
                Intent intent = new Intent();
                intent.putExtra(RecentDbAdapter.KEY_PATH, c.getString(pathid));
                intent.putExtra(RecentDbAdapter.KEY_ROWID, c.getInt(rowid));
                intent.putExtra(RecentDbAdapter.KEY_PAGE, c.getInt(pageid));
                OpenFileActivity.this.setResult(RESULT_OK, intent);
                OpenFileActivity.this.finish();
                c.close();
                mRecentCursor.close();
            }
        });

        mDbAdapter = new RecentDbAdapter(getApplicationContext());
        mDbAdapter.open();
        checkRecent();
        fillRecent();
    }

    @Override
    public void onStop(){
        mRecentCursor.close();
        super.onStop();
    }

    @Override
    public void onActivityResult(int request, int result, Intent intent){
        switch (request){
        case PICK_FILE:
            if (result == RESULT_OK){
                String path = intent.getData().getPath();
                Intent i = new Intent();
                i.putExtra(RecentDbAdapter.KEY_PATH, path);
                setResult(RESULT_OK, i);
                finish();
                mRecentCursor.close();
            }
            break;
        }
    }

    /**
     * Checks files in "recent" database. If some files were moved or
     * removed then deletes they from database.
     */
    private void checkRecent(){
        mRecentCursor = mDbAdapter.fetchAll();
        List<Integer> toRemove = null;
        File file;
        int rowid = mRecentCursor.getColumnIndexOrThrow(RecentDbAdapter.KEY_ROWID),
            pathid = mRecentCursor.getColumnIndexOrThrow(RecentDbAdapter.KEY_PATH);

        if (mRecentCursor.moveToFirst()){
            do{
                file = new File(mRecentCursor.getString(pathid));
                if (!file.exists()){
                    if (toRemove == null){
                        toRemove = new LinkedList<Integer>();
                    }
                    toRemove.add(mRecentCursor.getInt(rowid));
                }
            }while (mRecentCursor.moveToNext());

            if (toRemove != null){
                for (Integer i: toRemove)
                    mDbAdapter.delete(i);
            }
        }
        mRecentCursor.close();
    }

    private void fillRecent(){
        ListView lv = (ListView) findViewById(R.id.recent_list);

        mRecentCursor = mDbAdapter.fetchAll();
        startManagingCursor(mRecentCursor);
        String[] from = new String[]{RecentDbAdapter.KEY_NAME, RecentDbAdapter.KEY_PATH};
        int[] to = new int[]{R.id.shortname, R.id.fullpath};
        SimpleCursorAdapter recent = new SimpleCursorAdapter(this,
                R.layout.recent_item, mRecentCursor, from, to);
        lv.setAdapter(recent);
    }
}