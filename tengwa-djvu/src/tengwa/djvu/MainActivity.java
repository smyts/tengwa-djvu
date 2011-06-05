package tengwa.djvu;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.*;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Formatter;

public class MainActivity extends Activity implements DataCatListener{
    public static final int GO_TO_DIALOG = 0;
    public static final int ABOUT_DIALOG = 1;
    public static final int SETTINGS_ACTIVITY = 2;
    public static final int OPEN_FILE_ACTIVITY = 3;

    private static final String FILE_PATH = "file path";
    private static final String CURRENT_PAGE = "current page";

    private RecentDbAdapter mDbAdapter;
    private DataCatBase mDataCat;
    private int mCurrentPage;
    private FileInfo mFileInfo;
    private Toast mPageToast;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setTitle(R.string.app_name);

        mPageToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        mPageToast.setGravity(Gravity.TOP | Gravity.RIGHT, 0, 0);
        mImage = (ImageView) findViewById(R.id.doc_view);

        loadPreferences();

        mDbAdapter = new RecentDbAdapter(getApplicationContext());
        mDbAdapter.open();

        Djvulibre.contextCreate();
        int cachesize = Djvulibre.cacheGetSize();
        Djvulibre.cacheSetSize(5 * (1 << 20));
        Djvulibre.cacheClear();

        //TODO: replace DataCatStub with real implementation
        mDataCat = new DataCatStub();
        mDataCat.bind(this);

        if (savedInstanceState == null){
            startActivityForResult(new Intent(this, OpenFileActivity.class), OPEN_FILE_ACTIVITY);
        } else {
            mDataCat.loadFile(savedInstanceState.getString(FILE_PATH));
            mCurrentPage = savedInstanceState.getInt(CURRENT_PAGE);
            //TODO: show "loading" until file loads
       }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        if (mFileInfo != null) {
            outState.putString(FILE_PATH, mFileInfo.filePath);
            outState.putInt(CURRENT_PAGE, mCurrentPage);
        }
        //TODO: update information in Recent database
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
    protected void onDestroy() {
        super.onDestroy();
        Djvulibre.documentRelease();
        Djvulibre.contextRelease();
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
            startActivityForResult(new Intent(this, OpenFileActivity.class), OPEN_FILE_ACTIVITY);
            break;
        case R.id.menu_item_goto:
            showDialog(GO_TO_DIALOG);
            break;
        case R.id.menu_item_settings:
            startActivityForResult(new Intent(this, SettingsActivity.class), SETTINGS_ACTIVITY);
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
            builder.setView(inflater.inflate(R.layout.about_dialog,
                    (ViewGroup) findViewById(R.layout.main)));
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                  }

            });
            return builder.create();
        case GO_TO_DIALOG:
            Formatter f = new Formatter();
            f.format(getString(R.string.go_to_header), mCurrentPage, mFileInfo.pageTotal);
            GoToDialog dialog = new GoToDialog(this, f.toString());
            dialog.setGoToPageAction(new GoToDialog.GoToPageAction(){
                public void goToPage(int page) {
                    if (page > 0 && page <= mFileInfo.pageTotal) {
                        mCurrentPage = page;
                        mDataCat.getPage(page);
                    }
                }
            });
            return dialog;
        }
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        switch (requestCode){
        case SETTINGS_ACTIVITY:
            loadPreferences();
            break;
        case OPEN_FILE_ACTIVITY:
            if (resultCode == RESULT_OK){
                Bundle data = intent.getExtras();
                String path = data.getString(RecentDbAdapter.KEY_PATH);
                String name = data.getString(RecentDbAdapter.KEY_NAME);
                mCurrentPage = data.getInt(RecentDbAdapter.KEY_PAGE, -1);
                {   //TODO: save path, name, page parameters and open file
                    // this is stub, remove it
                    if (name == null){
                        int first = path.lastIndexOf('/'), last = path.lastIndexOf('.');
                        name = path.substring(first + 1, last);
                    }
                    Toast.makeText(this, name, 300).show();
                    mDbAdapter.create(name, path, 1);
                }
                mDataCat.loadFile(path);
            }
            break;
        }
    }

    private void loadPreferences(){
        if (mDataCat != null)
            mDataCat.updatePreferences();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        View toolbar = findViewById(R.id.toolbar);
        if (sp.getBoolean("toolbar_on", true)){
            toolbar.setVisibility(View.VISIBLE);
        } else {
            toolbar.setVisibility(View.GONE);
        }

        double zoom = Double.parseDouble(sp.getString("zoom_coef", "0"));
        if (zoom > 0) {
            mZoomCoef = zoom;
        }

        mScreenHeight = mImage.getHeight();
        mScreenWidth = mImage.getWidth();
    }

    public void takePage(Bitmap page) {
        setPage(page);
        mPageToast.setText(mCurrentPage + "/" + mFileInfo.pageTotal);
        mPageToast.show();
    }

    public void takeFileInfo(FileInfo fileInfo) {
        mFileInfo = fileInfo;
        //TODO: show file header
        mDataCat.getPage(mCurrentPage);
    }

    public void takeError(int errorDescription) {
        //TODO: show error message and go to OpenFileActivity
    }

    public void onToolbarClick(View view){
        switch (view.getId()){
        case R.id.toolbar_zoom_down:
            zoomDown();
            break;
        case R.id.toolbar_zoom_up:
            zoomUp();
            break;
        case R.id.toolbar_prev:
            if (mCurrentPage > 1 && mDataCat != null) {
                mDataCat.getPage(mCurrentPage - 1);
                --mCurrentPage;
                //TODO: show "loading"
            }
            break;
        case R.id.toolbar_next:
            if (mCurrentPage < mFileInfo.pageTotal && mDataCat != null) {
                mDataCat.getPage(mCurrentPage + 1);
                ++mCurrentPage;
                //TODO: show "loading"
            }
            break;
        }
    }

    /*
     * Fields and methods for work with bitmap
     */
    private double mZoomCoef = 2;           /* zoom coefficient for one click on zoom button */
    private int mScreenWidth;               /* width of ImageView */
    private int mScreenHeight;              /* height of ImageView */
    private Bitmap mOriginalPage;           /* original bitmap for current page */
    private int mLeft;                      /* leftmost displayed pixel in original bitmap */
    private int mTop;                       /* topmost displayed pixel in original bitmap */
    private int mOriginalWidth;             /* width of original bitmap */
    private int mOriginalHeight;            /* height of original bitmap */
    private int mCurrentWidth;              /* displayed width */
    private int mCurrentHeight;             /* displayed height */
    private double mCurrentZoom = 1;        /* zoom coefficient of displayed chunk of bitmap */
    private ImageView mImage;

    private void zoomUp() {
        //TODO: соблюдение пропорций относительно размера ImageView
        int xc = mLeft + mCurrentWidth / 2, yc = mTop + mCurrentHeight / 2;
        mCurrentWidth = (int) (mCurrentWidth / mZoomCoef);
        mCurrentHeight = (int) (mCurrentHeight / mZoomCoef);
        mCurrentZoom /= mZoomCoef;
        mLeft = xc - mCurrentWidth / 2;
        mTop = yc - mCurrentHeight / 2;
        setBitmap();
    }

    private void zoomDown() {
        //TODO: соблюдение пропорций относительно размера ImageView
        int xc = mLeft + mCurrentWidth / 2, yc = mTop + mCurrentHeight / 2;

        mCurrentZoom *= mZoomCoef;
        if (mCurrentZoom > 1) {
            mCurrentZoom = 1;
        }
        mCurrentWidth = (int) (mCurrentWidth * mZoomCoef);
        if (mCurrentWidth > mOriginalWidth) {
            mCurrentWidth = mOriginalWidth;
        }
        mCurrentHeight = (int) (mCurrentHeight * mZoomCoef);
        if (mCurrentHeight > mOriginalHeight) {
            mCurrentHeight = mOriginalHeight;
        }

        if (xc - mCurrentWidth / 2 < 0) {
            mLeft = 0;
        } else if (xc + mCurrentWidth / 2 > mOriginalWidth) {
            mLeft = mOriginalWidth - mCurrentWidth;
        } else {
            mLeft = xc - mCurrentWidth / 2;
        }

        if (yc - mCurrentHeight / 2 < 0) {
            mTop = 0;
        } else if (yc + mCurrentHeight / 2 > mOriginalHeight) {
            mTop = mOriginalHeight - mCurrentHeight ;
        } else {
            mTop = yc - mCurrentHeight /2;
        }
        setBitmap();
    }

    /*
     * Грузит страницу в том же приближении, что и предыдущая, но позиционирует ее в точке (0, 0)
     */
    private void setPage(Bitmap page) {
        //TODO: соблюдение пропорций относительно размера ImageView
        mOriginalPage = page;
        mOriginalHeight = page.getHeight();
        mOriginalWidth = page.getWidth();
        mLeft = 0;
        mTop = 0;
        mCurrentWidth = (int) (mOriginalWidth * mCurrentZoom);
        mCurrentHeight = (int) (mOriginalHeight * mCurrentZoom);
        setBitmap();
    }

    /*
     * Вырезает нужный кусок из mOriginalPage
     */
    private void setBitmap() {
        int[] pixels = new int[mCurrentHeight * mCurrentWidth];
        mOriginalPage.getPixels(pixels, 0, mCurrentWidth, mLeft, mTop, mCurrentWidth,
                mCurrentHeight);
        Bitmap page = Bitmap.createBitmap(pixels, mCurrentWidth, mCurrentHeight,
                mOriginalPage.getConfig());
        mImage.setImageBitmap(page);
    }
}