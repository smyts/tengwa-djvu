package tengwa.djvu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

public class DataCatStub extends DataCatBase{
    private final int[] mPages = {R.drawable.fight, R.drawable.lion, R.drawable.samurai,
            R.drawable.zzz};
    private final int pageTotal = 4;

    private boolean mFileLoaded = false;
    private String mFileName;
    private int mPage = 0;


    @Override
    public void updatePreferences() { }

    @Override
    public void loadFile(String file) {
        mFileName = file;
        mFileLoaded = true;
        if (mListener != null) {
            mListener.takeFileInfo(new FileInfo(file, pageTotal));
        }
        Djvulibre.documentRelease();
        Djvulibre.documentCreate(file);
        Djvulibre.handleDjvuMessages();
    }

    @Override
    public void getPage(int page) {
        if (!mFileLoaded){
            mListener.takeError(DataCatListener.ERROR_FILE_NOT_OPENED);
        } else if (page > 0 && page <= pageTotal){
            mPage = page;
            //this is a bad code, but it is a stub and i'm sure that mListener will be MainActivity
            new LoadPageTask((Context) mListener).execute(page); //loading page in another thread
        } else {
            mListener.takeError(DataCatListener.ERROR_NO_SUCH_PAGE);
        }
    }

    private class LoadPageTask extends AsyncTask<Integer, Void, Bitmap> {
        private Context mContext;

        public LoadPageTask(Context context){
            mContext = context;
        }

        @Override
        protected Bitmap doInBackground(Integer... integers) {
            return BitmapFactory.decodeResource(mContext.getResources(),
                    mPages[integers[0] - 1]);
        }

        @Override
        protected void onPostExecute(Bitmap result){
            mListener.takePage(result);
        }
    }

}

