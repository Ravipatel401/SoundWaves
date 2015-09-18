package org.bottiger.podcast.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.squareup.otto.Subscribe;

import org.bottiger.podcast.R;
import org.bottiger.podcast.listeners.DownloadProgress;
import org.bottiger.podcast.provider.FeedItem;
import org.bottiger.podcast.provider.IEpisode;
import org.bottiger.podcast.service.Downloader.EpisodeDownloadManager;

/**
 * Created by apl on 02-09-2014.
 */
public class DownloadButtonView extends PlayerButtonView implements View.OnClickListener {

    private static final String TAG = "DownloadButtonView";

    private Context mContext;

    private Drawable mStaticBackground = null;
    private @DrawableRes int download_icon = R.drawable.ic_get_app_white;
    private @DrawableRes int qeueed_icon = R.drawable.ic_schedule_white;
    private @DrawableRes int delete_icon = R.drawable.ic_delete_white;

    private static final int BITMAP_OFFSET = 5;
    private static final float RECTANGLE_SCALING = 1F;

    private RectF buttonRectangle;
    private int mLastProgress = 0;

    public DownloadButtonView(Context context) {
        super(context);
        init(context, null);
    }

    public DownloadButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DownloadButtonView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context argContext, @Nullable AttributeSet attrs) {
        mContext = argContext;

        buttonRectangle = new RectF();

        setOnClickListener(this);

        // Detect of we have defined a background color for the view
        if (getId() == R.id.feedview_download_button) {
            if (attrs != null) {
                int[] attrsArray = new int[]{
                        android.R.attr.background, // 0
                };
                TypedArray ta = mContext.obtainStyledAttributes(attrs, attrsArray);
                mStaticBackground = ta.getDrawable(0);
                ta.recycle();

                if (mStaticBackground != getResources().getDrawable(R.color.colorPrimaryDark)) {
                    download_icon = R.drawable.ic_get_app_grey;
                    qeueed_icon = R.drawable.ic_schedule_grey;
                    delete_icon = R.drawable.ic_delete_grey;
                }
            }
        }

        if (getId() == R.id.expanded_download) {
            if (attrs != null) {
                int[] attrsArray = new int[]{
                        android.R.attr.background, // 0
                };
                TypedArray ta = mContext.obtainStyledAttributes(attrs, attrsArray);
                mStaticBackground = ta.getDrawable(0);
                ta.recycle();

                if (mStaticBackground != getResources().getDrawable(R.color.colorPrimaryDark)) {
                    download_icon = R.drawable.ic_get_app_black;
                    qeueed_icon = R.drawable.ic_schedule_black;
                    delete_icon = R.drawable.ic_delete_black;
                }
            }
        }


        if (!isInEditMode()) {
            //ThemeHelper themeHelper = new ThemeHelper(mActivity);
            //int downloadIcon = themeHelper.getAttr(download_icon); //R.drawable.av_download; // FIXME
            // http://stackoverflow.com/questions/7896615/android-how-to-get-value-of-an-attribute-in-code

            setImage(download_icon);
            addState(PlayerButtonView.STATE_DEFAULT, download_icon);
            addState(PlayerButtonView.STATE_DELETE, delete_icon);
            addState(PlayerButtonView.STATE_QUEUE, qeueed_icon);
        }

        addDownloadCompletedCallback(new PlayerButtonView.DownloadStatus() {
            @Override
            public void FileComplete() {
                setState(PlayerButtonView.STATE_DELETE);
                setProgressPercent(new DownloadProgress());
            }

            @Override
            public void FileDeleted() {
                setState(PlayerButtonView.STATE_DEFAULT);
                setProgressPercent(new DownloadProgress());
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float halfW = getWidth()*RECTANGLE_SCALING;

        int left = 0;
        int width = (int)(halfW);
        int top = 0;

        buttonRectangle.set(left+BITMAP_OFFSET, top+BITMAP_OFFSET, left + width-BITMAP_OFFSET, top + width-BITMAP_OFFSET);

        if(mProgress!=0 && mProgress < 100) {
            if (getState() != PlayerButtonView.STATE_DEFAULT) {
                setState(PlayerButtonView.STATE_DEFAULT);
            }
            canvas.drawArc(buttonRectangle, -90, Math.round(360 * mProgress / 100F), false, foregroundColorPaint);
        }

        mLastProgress = mProgress;
    }

    @Override
    public int ButtonColor(@NonNull Palette argPalette) {
        if (mStaticBackground != null)
            return Color.argb(0, 0, 0, 0); // transparent

        return super.ButtonColor(argPalette);
    }

    @Override
    public void setEpisode(@NonNull IEpisode argItem) {
        super.setEpisode(argItem);

        setState(calcState());
        setProgressPercent(new DownloadProgress());
    }

    @Subscribe
    public void setProgressPercent(@NonNull DownloadProgress argProgress) {
        if (!getEpisode().equals(argProgress.getEpisode()))
            return;

        mProgress = argProgress.getProgress();
        if (mProgress == 100) {
            if (mDownloadCompletedCallback != null) {
                mDownloadCompletedCallback.FileComplete();
            }
            setState(PlayerButtonView.STATE_DELETE);
        }

        if (argProgress.getStatus() == org.bottiger.podcast.service.DownloadStatus.DELETED) {
            setState(PlayerButtonView.STATE_DEFAULT);
        }

        this.invalidate();
    }

    @Override
    public void onClick(View view) {
        String viewStr = view == null ? "null" : view.toString();
        Log.d(TAG, "onCLick: view => " + viewStr);

        if (getEpisode() == null) {
            Log.e(TAG, "Episode is null");
            throw new IllegalStateException("Episode can not be null");
        }

        if (getState() == PlayerButtonView.STATE_DEFAULT) {
            Log.v(TAG, "Queue download");
            EpisodeDownloadManager.addItemAndStartDownload(getEpisode(), EpisodeDownloadManager.FIRST, mContext);
            setState(PlayerButtonView.STATE_QUEUE);
        } else if (getState() == PlayerButtonView.STATE_DELETE) {
            Log.v(TAG, "Delete file");
            IEpisode episode = getEpisode();
            if (episode instanceof FeedItem) {
                ((FeedItem)episode).delFile(mContext);
                setState(PlayerButtonView.STATE_DEFAULT);
            }
        }
    }

    private int calcState() {
        //mEpisode.isDownloaded() ? PlayerButtonView.STATE_DELETE : PlayerButtonView.STATE_DEFAULT;

        if (getEpisode().isDownloaded()) {
            return PlayerButtonView.STATE_DELETE;
        }

        org.bottiger.podcast.service.DownloadStatus status = EpisodeDownloadManager.getStatus(getEpisode());

        if (status == org.bottiger.podcast.service.DownloadStatus.DOWNLOADING) {
            return PlayerButtonView.STATE_DEFAULT;
        }

        if (status == org.bottiger.podcast.service.DownloadStatus.PENDING) {
            return PlayerButtonView.STATE_QUEUE;
        }

        return PlayerButtonView.STATE_DEFAULT;
    }
}
