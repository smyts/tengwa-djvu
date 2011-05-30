package tengwa.djvu;

import android.graphics.Bitmap;

public interface DataCatListener {
    public static final int ERROR_FILE_NOT_FOUND = 1;
    public static final int ERROR_WRONG_FILE_FORMAT = 2;
    public static final int ERROR_NO_SUCH_PAGE = 3;
    public static final int ERROR_FILE_NOT_OPENED = 4;

    void takePage(Bitmap page);
    void takeFileInfo(FileInfo fileInfo);
    void takeError(int errorDescription);
}