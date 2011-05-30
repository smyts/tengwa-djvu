package tengwa.djvu;

public abstract class DataCatBase{
    protected DataCatListener mListener;

    public void bind(DataCatListener listener){
        mListener = listener;
    }

    public abstract void updatePreferences();

    public abstract void loadFile(String file);

    public abstract void getPage(int page);
}
