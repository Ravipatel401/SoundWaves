package org.bottiger.podcast.Player;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.bottiger.podcast.R;
import org.bottiger.podcast.provider.FeedItem;
import org.bottiger.podcast.receiver.HeadsetReceiver;
import org.bottiger.podcast.service.PlayerService;

/**
 * Created by apl on 11-02-2015.
 */
public class RemoteController {
    private LegacyRemoteControlClient remoteControlClient;
    private PlayerService mContext;
    private Bitmap mCurrentAlbumArt;


    public void register(PlayerService context)
    {
        mContext = context;

        if (remoteControlClient == null)
        {
            System.out.println("Trying to register it.");

            mCurrentAlbumArt = BitmapFactory.decodeResource(context.getResources(), R.drawable.generic_podcast);

            AudioManager audioManager = (AudioManager) context.getSystemService(context.AUDIO_SERVICE);

            ComponentName myEventReceiver = new ComponentName(context.getPackageName(), HeadsetReceiver.class.getName());
            audioManager.registerMediaButtonEventReceiver(myEventReceiver);

            // build the PendingIntent for the remote control client
            Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            mediaButtonIntent.setComponent(myEventReceiver);
            // create and register the remote control client
            PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(context, 0, mediaButtonIntent, 0);
            remoteControlClient = new LegacyRemoteControlClient(mediaPendingIntent);
            remoteControlClient.setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
                            | RemoteControlClient.FLAG_KEY_MEDIA_NEXT
                            | RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS
                            | RemoteControlClient.FLAG_KEY_MEDIA_PLAY
                            | RemoteControlClient.FLAG_KEY_MEDIA_PAUSE
            );
            audioManager.registerRemoteControlClient(remoteControlClient);


        }
    }

    /**
     * Update the state of the remote control.
     */
    public void updatePlayingState(boolean isPlaying)
    {
        if(remoteControlClient != null)
        {
            if (isPlaying)
            {
                remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
            }

            else
            {
                remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
            }
        }
    }

    /**
     * Updates the state of the remote control to "stopped".
     */
    public void stop()
    {
        if (remoteControlClient != null)
        {
            remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED);
        }
    }

    public void updateMetaData()
    {
        final FeedItem episode = mContext.getCurrentItem();

        if (remoteControlClient != null && episode != null)
        {
            Log.d("RemoteController", "Updating remote control");
            //int state = mContext.isPlaying() ? remoteControlClient.PLAYSTATE_PLAYING : remoteControlClient.PLAYSTATE_PAUSED;
            //remoteControlClient.setPlaybackState(state);
            updatePlayingState(mContext.isPlaying());

            RemoteControlClient.MetadataEditor editor = remoteControlClient.editMetadata(true);
            updateSimpleMetaData(editor, episode);

            Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    Log.d("RemoteController", "Updating remote control (with background)");
                    mCurrentAlbumArt = bitmap;
                    RemoteControlClient.MetadataEditor editor = remoteControlClient.editMetadata(true);
                    editor.putBitmap(android.media.RemoteController.MetadataEditor.BITMAP_KEY_ARTWORK, bitmap);
                    updateSimpleMetaData(editor, episode);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    Log.d("RemoteController", "BACKGROUND failed to load");
                    return;
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    return;
                }
            };

            episode.getArtworAsync(mContext, target);
        }
    }

    private void updateSimpleMetaData(RemoteControlClient.MetadataEditor editor, FeedItem episode) {
        editor.putLong(MediaMetadataRetriever.METADATA_KEY_DURATION, (long)1000);
        editor.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, episode.getAuthor());
        editor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, episode.getTitle());

        if (mCurrentAlbumArt != null) {
            editor.putBitmap(android.media.RemoteController.MetadataEditor.BITMAP_KEY_ARTWORK, mCurrentAlbumArt);
        }

        editor.apply();
    }

    /**
     * Release the remote control.
     */
    public void release() {
        remoteControlClient = null;
    }
}
