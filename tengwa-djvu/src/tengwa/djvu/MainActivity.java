package tengwa.djvu;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setTitle(R.string.app_name);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
        case R.id.menu_item_about:
            break;
        case R.id.menu_item_open:
            break;
        case R.id.menu_item_goto:
            break;
        case R.id.menu_item_settings:
            break;
        }
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){ }

    @Override
    protected void onPause() {}

    @Override
    protected void onResume() {}
}
