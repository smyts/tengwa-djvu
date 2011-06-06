package tengwa.djvu;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class PageView extends View {
    private Bitmap mPage;                   /* original bitmap for current page */
    private int mOriginalWidth;             /* width of original bitmap */
    private int mOriginalHeight;            /* height of original bitmap */
    private int mScreenWidth = 0;           /* current width of view */
    private int mScreenHeight = 0;          /* current height of view */

    private double mCurrentScale = 0;       /* current scale */
    private double mStepScale = 0.2f;       /* step scale */
    private double mOffsetX, mOffsetY;      /* offsets for shifting page */


    public PageView(Context context) {
        super(context);
    }
    public PageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom){
        super.onLayout(changed, left, top, right, bottom);
        mScreenWidth = right - left;
        mScreenHeight = bottom - top;
    }

    public void loadPreferences(SharedPreferences sp){
        double scale = Double.parseDouble(sp.getString("step_scale", "0.2"));
        if (scale > 0) {
            mStepScale = scale;
        } else {
            SharedPreferences.Editor e = sp.edit();
            e.putString("step_scale", Double.toString(mStepScale));
            e.commit();
        }
    }

    /*
     * Methods for work with bitmap
     */
    public void zoomIn() {
        mCurrentScale += mStepScale;
        invalidate();
    }

    public void zoomOut() {
        if (mCurrentScale > mStepScale) {
            mCurrentScale -= mStepScale;
            invalidate();
        }
    }

    public void scroll(double dx, double dy) {
        mOffsetX -= dx;
        mOffsetY -= dy;

        if (mOriginalWidth * mCurrentScale <= mScreenWidth) {
            if (mOffsetX < 0) {
                mOffsetX = 0;
            } else if ((mOriginalWidth + mOffsetX) * mCurrentScale > mScreenWidth) {
                mOffsetX = mScreenWidth / mCurrentScale - mOriginalWidth;
            }
        } else {
            if (mOffsetX > 0) {
                mOffsetX = 0;
            } else if ((mOriginalWidth + mOffsetX) * mCurrentScale < mScreenWidth) {
                mOffsetX = mScreenWidth / mCurrentScale - mOriginalWidth;
            }
        }

        if (mOriginalHeight * mCurrentScale <= mScreenHeight) {
            if (mOffsetY < 0) {
                mOffsetY = 0;
            } else if ((mOriginalHeight + mOffsetY) * mCurrentScale > mScreenHeight) {
                mOffsetY = mScreenHeight /mCurrentScale - mOriginalHeight;
            }
        } else {
            if (mOffsetY > 0) {
                mOffsetY = 0;
            } else if ((mOriginalHeight + mOffsetY) * mCurrentScale < mScreenHeight) {
                mOffsetY = mScreenHeight /mCurrentScale - mOriginalHeight;
            }
        }
        invalidate();
    }

    /*
     * Loads page with same scale as for previous page and shows leftmost topmost corner of it
     */
    public void setPage(Bitmap page) {
        mPage = page;
        mOffsetX = mOffsetY = 0;
        mOriginalWidth = page.getWidth();
        mOriginalHeight = page.getHeight();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mPage != null) {
            if (mCurrentScale == 0) {
                double cx = ((double ) mScreenWidth ) / ((double ) mOriginalWidth ),
                       cy = ((double ) mScreenHeight ) / ( (double ) mOriginalHeight );
                mCurrentScale = Math.max(cx, cy);
            }
            canvas.scale((float) mCurrentScale, (float) mCurrentScale);
            canvas.translate((float) mOffsetX, (float ) mOffsetY);
            canvas.drawBitmap(mPage, 0, 0, null);
        }
    }
}