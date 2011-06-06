package tengwa.djvu;


import android.graphics.Bitmap;

public class DataCat extends DataCatBase {
    private final int[] mPages = {R.drawable.fight, R.drawable.lion, R.drawable.samurai,
            R.drawable.zzz};
    private boolean mFileLoaded = false;
    private String mFilepath;
    private int mPagesTotal;


    public int cacheSize = 5 * (1 << 20);

    public DataCat() {
        Djvulibre.contextCreate();
        Djvulibre.installCallback();
        this.updatePreferences();
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
            Djvulibre.getPage(page);
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
        if (docinfoDescription == Djvulibre.JOB_OK) {
            mFileLoaded = true;
            mPagesTotal = Djvulibre.getPagenum();
            FileInfo fileInfo = new FileInfo(mFilepath, mPagesTotal);
            mListener.takeFileInfo(fileInfo);
        } else if (docinfoDescription >= Djvulibre.JOB_FAILED) {
            mListener.takeError(DataCatListener.ERROR_FILE_NOT_OPENED);
        }
    }

    @Override
    public void signalPageinfo(int pageinfoDescription) {
        int[] img;
        if (pageinfoDescription == Djvulibre.JOB_OK) {
            img = Djvulibre.getPageImage(Djvulibre.sLastPageObj);
            int width, height;

            height = Djvulibre.getRenderHeight();
            width = Djvulibre.getRenderWidth();
            Bitmap bmp = Bitmap.createBitmap(img, width, height,
                    Bitmap.Config.RGB_565);


            mListener.takePage(bmp);
        } else if (pageinfoDescription >= Djvulibre.JOB_FAILED) {
            mListener.takeError(DataCatListener.ERROR_NO_SUCH_PAGE);
        }
    }

    @Override
    public void signalRedisplay(int redisplayDescription) {

    }

    @Override
    public void signalHandle() {

    }
}
