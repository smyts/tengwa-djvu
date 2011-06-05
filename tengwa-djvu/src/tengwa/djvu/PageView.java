package tengwa.djvu;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;

public class PageView extends ImageView {
    private double mZoomFactor = 2;         /* zoom coefficient for one click on zoom button */
    private Bitmap mOriginalPage;           /* original bitmap for current page */
    private int mLeft;                      /* leftmost displayed pixel in original bitmap */
    private int mTop;                       /* topmost displayed pixel in original bitmap */
    private int mOriginalWidth;             /* width of original bitmap */
    private int mOriginalHeight;            /* height of original bitmap */
    private int mCurrentWidth;              /* displayed width */
    private int mCurrentHeight;             /* displayed height */
    private int mScreenWidth = 0;           /* current width of view */
    private int mScreenHeight = 0;          /* current height of view */
    private double mCurrentZoom = 1;        /* zoom coefficient of displayed chunk of bitmap */


    public PageView(Context context) {
        super(context);
    }
    public PageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom){
        super.onLayout(changed, left, top, right, bottom);
        if (changed || mScreenWidth == 0) {
            if (mOriginalPage != null) {
                mScreenWidth = right - left;
                mScreenHeight = bottom - top;
                fitPage();
                updateImage();
            }
        }
    }

    public void loadPreferences(SharedPreferences sp){
        double zoom = Double.parseDouble(sp.getString("zoom_factor", "2"));
        if (zoom > 1) {
            mZoomFactor = zoom;
        } else {
            SharedPreferences.Editor e = sp.edit();
            e.putString("zoom_factor", Double.toString(mZoomFactor));
            e.commit();
        }
    }

    /*
     * Methods for work with bitmap
     */
    public void zoomIn() {
        if (mCurrentZoom > 1) {
            mCurrentWidth = mOriginalWidth;
            mCurrentHeight = mOriginalHeight;
            mLeft = mTop = 0;
            mCurrentZoom = 1;
        } else {
            int width = mCurrentWidth, height = mCurrentHeight;
            mCurrentZoom /= mZoomFactor;
            int xc = mLeft + width / 2, yc = mTop + height / 2;
            width = (int) (width / mZoomFactor);
            height = (int) (height / mZoomFactor);
            if (width == 0 || height == 0){
                return;
            }
            mLeft = xc - width / 2;
            mTop = yc - height / 2;
            mCurrentWidth = width;
            mCurrentHeight = height;
        }
        fitPage();
        updateImage();
    }

    public void zoomOut() {
        if (mCurrentZoom > 1) {
            return;
        }

        int xc = mLeft + mCurrentWidth / 2, yc = mTop + mCurrentHeight / 2;

        mCurrentZoom *= mZoomFactor;

        if (mCurrentZoom > 1) {
            //zoom > 1 means showing whole page without fitting to page
            mCurrentWidth = mOriginalWidth;
            mCurrentHeight = mOriginalHeight;
            mLeft = mTop = 0;
        } else {
            mCurrentWidth = (int) (mCurrentWidth * mZoomFactor);
            if (mCurrentWidth > mOriginalWidth) {
                mCurrentWidth = mOriginalWidth;
            }
            mCurrentHeight = (int) (mCurrentHeight * mZoomFactor);
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
        }

        fitPage();
        updateImage();
    }

    public void scroll(double distanceX, double distanceY) {
        int dx = (int) distanceX, dy = (int) distanceY;
        if (dx == 0 && dy == 0) {
            return;
        }
        if (mLeft + mCurrentWidth + dx > mOriginalWidth) {
            mLeft = mOriginalWidth - mCurrentWidth;
        } else if (mLeft + dx < 0) {
            mLeft = 0;
        } else {
            mLeft += dx;
        }
        if (mTop + mCurrentHeight + dy > mOriginalHeight) {
            mTop = mOriginalHeight - mCurrentHeight;
        } else if (mTop + dy < 0) {
            mTop = 0;
        } else {
            mTop += dy;
        }
        updateImage();
    }

    /*
     * This constant value is used to detect cases of significant difference between page and
     * screen width/heigth proportion
     */
    private static final int ALLOWABLE_ERROR = 300;

    public void fitPage(){
        //zoom > 1 means showing whole page without fitting to page
        if (mCurrentZoom <= 1 ) {
            if (Math.abs(mCurrentWidth * mScreenHeight - mCurrentHeight * mScreenWidth)
                    > ALLOWABLE_ERROR) {
                int width = (int) (mOriginalWidth * mCurrentZoom),
                        height = (int) (mOriginalHeight * mCurrentZoom);
                if (width * mScreenHeight < height * mScreenWidth) {
                    height = width * mScreenHeight / mScreenWidth;
                } else {
                    width = height * mScreenWidth / mScreenHeight;
                }
                mCurrentWidth = width;
                mCurrentHeight = height;
            }
        } else {
            mCurrentWidth = mOriginalWidth;
            mCurrentHeight = mOriginalHeight;
            mLeft = mTop = 0;
        }
    }

    /*
     * Loads page with same zoom factor as for previous page and shows leftmost topmost corner of it
     */
    public void setPage(Bitmap page) {
        mOriginalPage = page;
        mOriginalHeight = page.getHeight();
        mOriginalWidth = page.getWidth();
        mLeft = 0;
        mTop = 0;
        mCurrentWidth = (int) (mOriginalWidth * mCurrentZoom);
        mCurrentHeight = (int) (mOriginalHeight * mCurrentZoom);
        fitPage();
        updateImage();
    }

    /*
     * Shows needed chunk of mOriginalPage
     */
    private void updateImage() {
        int[] pixels = new int[mCurrentHeight * mCurrentWidth];
        mOriginalPage.getPixels(pixels, 0, mCurrentWidth, mLeft, mTop, mCurrentWidth,
                mCurrentHeight);
        Bitmap page = Bitmap.createBitmap(pixels, mCurrentWidth, mCurrentHeight,
                mOriginalPage.getConfig());
        setImageBitmap(page);
    }
}