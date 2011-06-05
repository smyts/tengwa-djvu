package tengwa.djvu;


import android.content.Context;
import android.os.AsyncTask;

public class DataCat extends DataCatBase {
    private boolean mFileLoaded = false;
    private String mFilepath;
    private int mPagesTotal;
    private HandleMessages bgHandler;

    public int cacheSize = 5 * (1 << 20);

    public DataCat() {
        Djvulibre.contextCreate();
        this.updatePreferences();
        bgHandler = new HandleMessages((Context)mListener);
        bgHandler.execute();
    }

    @Override
    public void updatePreferences() {
        Djvulibre.cacheSetSize(cacheSize);
    }

    @Override
    public void loadFile(String filepath) {
        mFilepath = filepath;
        Djvulibre.documentRelease();
        Djvulibre.documentCreate(mFilepath);
    }

    @Override
    public void getPage(int page) {
        if (!mFileLoaded) {
            mListener.takeError(DataCatListener.ERROR_FILE_NOT_OPENED);
        } else if (page > 0 && page <= mPagesTotal) {
            long pageobj = Djvulibre.pageCreateByPageno(page);
        } else {
            mListener.takeError(DataCatListener.ERROR_NO_SUCH_PAGE);
        }
    }

    @Override
    public void signalError(int errorDescription) {
        mListener.takeError(DataCatListener.ERROR_GENERAL);
    }

    @Override
    public void signalDocinfo(int docinfoDescription) {

    }

    @Override
    public void signalPageinfo(int pageinfoDescription) {

    }

    @Override
    public void signalRedisplay(int redisplayDescription) {

    }

    @Override
    public void signalHandle() {

    }

    private class HandleMessages extends AsyncTask<Void, Void, Void> {
        private Context mContext;

        public HandleMessages(Context context) {
            mContext = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Djvulibre.handleDjvuMessages(1);
            this.publishProgress();
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... voids) {
            Djvulibre.handleMessages();
        }

        @Override
        protected void onPostExecute(Void vd) {
            //do smth
        }
    }
}
