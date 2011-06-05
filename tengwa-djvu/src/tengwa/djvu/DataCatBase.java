package tengwa.djvu;

public abstract class DataCatBase implements DjvulibreErrorCallback, DjvulibreDocinfoCallback,
        DjvulibrePageinfoCallback, DjvulibreRedisplayCallback {
    protected DataCatListener mListener;

    public void bind(DataCatListener listener){
        mListener = listener;
    }

    public abstract void updatePreferences();

    public abstract void loadFile(String file);

    public abstract void getPage(int page);

    public abstract void signalError(int errorDescription);
    public abstract void signalDocinfo(int docinfoDescription);
    public abstract void signalPageinfo(int pageinfoDescription);
    public abstract void signalRedisplay(int redisplayDescription);
}
