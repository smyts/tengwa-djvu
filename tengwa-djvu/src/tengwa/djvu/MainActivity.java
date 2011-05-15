package tengwa.djvu;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.*;

public class MainActivity extends Activity {
    public static final int GO_TO_DIALOG = 0;
    public static final int ABOUT_DIALOG = 1;

    @Override
    public void onCreate(Bundle savedInstanceState){
        /*TODO: if savedInstanceState == null then go to OpenFileActivity*/
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setTitle(R.string.app_name);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
            showDialog(ABOUT_DIALOG);
            break;
        case R.id.menu_item_open:
            break;
        case R.id.menu_item_goto:
            showDialog(GO_TO_DIALOG);
            break;
        case R.id.menu_item_settings:
            break;
        }
        return true;
    }

    @Override
    protected Dialog onCreateDialog(int id){
        AlertDialog.Builder builder;
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        switch (id){
        case ABOUT_DIALOG:
            builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.about);
            builder.setView(inflater.inflate(R.layout.about_dialog, (ViewGroup) findViewById(R.layout.main)));
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialogInterface, int id) {
                    dialogInterface.cancel();
                }
            });
            return builder.create();
        case GO_TO_DIALOG:
            GoToDialog dialog = new GoToDialog(this);
            dialog.setGoToPageAction(new GoToDialog.GoToPageAction(){
                public void goToPage(int page) {
                    //TODO: implement this method
                }
            });
            return dialog;
        }
        return null;
    }
}
