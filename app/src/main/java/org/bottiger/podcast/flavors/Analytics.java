package org.bottiger.podcast.flavors;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import org.bottiger.podcast.BuildConfig;
import org.bottiger.podcast.SoundWaves;

import java.util.HashMap;

/**
 * Created by apl on 11-02-2015.
 *
 * Only used on the Google Play Store
 */
public class Analytics {

    private Context mContext;

    public Analytics(@NonNull Context argContext) {
        mContext = argContext;
    }

    /**
     * Enum used to identify the tracker that needs to be used for tracking.
     *
     * A single tracker is usually enough for most purposes. In case you do need multiple trackers,
     * storing them all in Application object helps ensure that they are created only once per
     * application instance.
     */
    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }

    private HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    public void startTracking() {
        Tracker tracker = getTracker(TrackerName.APP_TRACKER);
        tracker.enableExceptionReporting(true);
        tracker.enableAutoActivityTracking(true);
    }

    private synchronized Tracker getTracker(TrackerName trackerId) {
        if (trackerId != TrackerName.APP_TRACKER) {
            throw new IllegalStateException("TrackerName not suported");
        }

        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(mContext);
            Tracker t = analytics.newTracker(SoundWaves.ANALYTICS_ID);
            mTrackers.put(trackerId, t);

        }
        return mTrackers.get(trackerId);
    }
}