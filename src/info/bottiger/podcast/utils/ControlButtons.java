package info.bottiger.podcast.utils;

import info.bottiger.podcast.PodcastBaseFragment;
import info.bottiger.podcast.R;
import info.bottiger.podcast.RecentItemFragment;
import info.bottiger.podcast.SwipeActivity;
import info.bottiger.podcast.provider.FeedItem;
import info.bottiger.podcast.service.PodcastService;
import android.content.ContentResolver;
import android.os.SystemClock;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class ControlButtons {

	private static final int MAX_SEEKBAR_VALUE = 1000;
	private static long mLastSeekEventTime;
	private static boolean mFromTouch;

	private static TextView mCurrentTime;

	/*
	 * Deprecated. 24/12-2012 private static OnSeekBarChangeListener
	 * mSeekListener = new OnSeekBarChangeListener() { public void
	 * onStartTrackingTouch(SeekBar bar) { mLastSeekEventTime = 0; } public void
	 * onProgressChanged(SeekBar bar, int progress, boolean fromuser) { //if
	 * (!fromuser || (PodcastBaseFragment.mPlayerServiceBinder == null)) return;
	 * 
	 * long now = SystemClock.elapsedRealtime(); if ((now - mLastSeekEventTime)
	 * > 250) { mLastSeekEventTime = now; long timeMs =
	 * (PodcastBaseFragment.mPlayerServiceBinder.duration() * progress) / 1000;
	 * //if (mCurrentTime != null)
	 * mCurrentTime.setText(StrUtils.formatTime(timeMs)); }
	 * 
	 * }
	 * 
	 * public void onStopTrackingTouch(SeekBar bar) { //mPosOverride = -1;
	 * mFromTouch = false; long timeMs =
	 * fragment.mPlayerServiceBinder.duration() * bar.getProgress() / 1000; try
	 * { if(fragment.mPlayerServiceBinder.isInitialized())
	 * fragment.mPlayerServiceBinder.seek(timeMs); } catch (Exception ex) { }
	 * //log.debug("mFromTouch = false; ");
	 * 
	 * } };
	 */

	public static RecentItemFragment fragment = null;

	public static class Holder {
		public ImageButton playPauseButton;
		public ImageButton stopButton;
		public ImageButton infoButton;
		public ImageButton downloadButton;
		public ImageButton queueButton;
		public TextView currentTime;
		public TextView timeSlash;
		public TextView duration;
		public TextView filesize;
		public SeekBar seekbar;
	}

	public static void setCurrentTime(final Holder viewHolder, final long id) {

	}

	public static void setListener(
			final PodcastService podcastServiceConnection,
			final Holder viewHolder, final long id) {

		final ImageButton playPauseButton = viewHolder.playPauseButton;
		final ContentResolver resolver = fragment.getActivity()
				.getContentResolver();
		final FeedItem item = FeedItem.getById(resolver, id);

		playPauseButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (viewHolder.seekbar != null)
					fragment.setProgressBar(viewHolder.seekbar);
				if (viewHolder.currentTime != null)
					fragment.setCurrentTime(viewHolder.currentTime);

				if (viewHolder.duration != null)
					fragment.setDuration(viewHolder.duration);
				if (PodcastBaseFragment.mPlayerServiceBinder.isPlaying()) {
					playPauseButton.setContentDescription("Play");
					playPauseButton.setImageResource(R.drawable.av_play);
					PodcastBaseFragment.mPlayerServiceBinder.pause();
				} else {
					playPauseButton.setImageResource(R.drawable.av_pause);
					playPauseButton.setContentDescription("Pause");
					PodcastBaseFragment.mPlayerServiceBinder.play(id);
					fragment.getNotificationPlayer().show();
					PodcastBaseFragment.queueNextRefresh(1);
				}
			}
		});

		viewHolder.stopButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				playPauseButton.setContentDescription("Play");
				playPauseButton.setImageResource(R.drawable.av_play);
				PodcastBaseFragment.mPlayerServiceBinder.stop();
			}
		});

		viewHolder.infoButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});

		viewHolder.downloadButton
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {

						if (item.isDownloaded()) {
							// Delete file
							item.delFile(resolver);
							viewHolder.downloadButton
									.setImageResource(R.drawable.download);
							viewHolder.downloadButton
									.setContentDescription("Download");
						} else {
							// Download file
							FilesizeUpdater.put(fragment.getActivity(),
									item.id, viewHolder.filesize);
							podcastServiceConnection.downloadItem(fragment
									.getActivity().getContentResolver(), item);
							viewHolder.downloadButton
									.setImageResource(R.drawable.ic_action_delete);
							viewHolder.downloadButton
									.setContentDescription("Trash");
						}
					}
				});

		viewHolder.queueButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});

		ControlButtons.mCurrentTime = viewHolder.currentTime;
		OnPlayerSeekBarChangeListener listener = new OnPlayerSeekBarChangeListener(
				item, viewHolder.currentTime, viewHolder.duration);
		// viewHolder.seekbar.setOnSeekBarChangeListener(mSeekListener);
		viewHolder.seekbar.setOnSeekBarChangeListener(listener);
		viewHolder.seekbar.setMax(MAX_SEEKBAR_VALUE);
	}

	public static void setPlayerListeners(View listView, View playerView,
			long id) {

		ControlButtons.Holder viewHolder = new ControlButtons.Holder();
		viewHolder.currentTime = (TextView) playerView
				.findViewById(R.id.current_position);

		viewHolder.playPauseButton = (ImageButton) playerView
				.findViewById(R.id.play_toggle);
		viewHolder.stopButton = (ImageButton) playerView
				.findViewById(R.id.stop);
		viewHolder.infoButton = (ImageButton) playerView
				.findViewById(R.id.info);
		viewHolder.downloadButton = (ImageButton) playerView
				.findViewById(R.id.download);
		viewHolder.queueButton = (ImageButton) playerView
				.findViewById(R.id.queue);
		viewHolder.seekbar = (SeekBar) playerView.findViewById(R.id.progress);
		viewHolder.currentTime = (TextView) listView
				.findViewById(R.id.current_position);
		viewHolder.duration = (TextView) listView.findViewById(R.id.duration);
		viewHolder.filesize = (TextView) listView.findViewById(R.id.filesize);

		ControlButtons
				.setListener(SwipeActivity.mServiceBinder, viewHolder, id);
	}

	public static void setPlayerListeners(View playerView, long id) {
		// View view = getChildByID(id);

		ControlButtons.Holder viewHolder = new ControlButtons.Holder();
		viewHolder.currentTime = (TextView) playerView
				.findViewById(R.id.current_position);

		viewHolder.playPauseButton = (ImageButton) playerView
				.findViewById(R.id.play_toggle);
		viewHolder.stopButton = (ImageButton) playerView
				.findViewById(R.id.stop);
		viewHolder.infoButton = (ImageButton) playerView
				.findViewById(R.id.info);
		viewHolder.downloadButton = (ImageButton) playerView
				.findViewById(R.id.download);
		viewHolder.queueButton = (ImageButton) playerView
				.findViewById(R.id.queue);
		viewHolder.seekbar = (SeekBar) playerView.findViewById(R.id.progress);

		ControlButtons
				.setListener(SwipeActivity.mServiceBinder, viewHolder, id);

	}

	private static class OnPlayerSeekBarChangeListener implements
			OnSeekBarChangeListener {

		TextView currentTimeView;
		TextView durationView;
		FeedItem item;

		public OnPlayerSeekBarChangeListener(FeedItem item, TextView tv,
				TextView dv) {
			this.item = item;
			this.currentTimeView = tv;
			this.durationView = dv;
		}

		@Override
		public void onStartTrackingTouch(SeekBar bar) {
			mLastSeekEventTime = 0;
		}

		@Override
		public void onProgressChanged(SeekBar bar, int progress,
				boolean fromuser) {
			// if (!fromuser || (PodcastBaseFragment.mPlayerServiceBinder ==
			// null)) return;

			long now = SystemClock.elapsedRealtime();
			if ((now - mLastSeekEventTime) > 250 && durationView != null) {
				mLastSeekEventTime = now;

				float relativeProgress = progress / (float) 1000;
				String duration = durationView.getText().toString();
				// long timeMs = duration * progress / 1000;
				// if (mCurrentTime != null)
				// mCurrentTime.setText(StrUtils.formatTime(timeMs));
				if (currentTimeView != null)
					currentTimeView.setText(StrUtils.formatTime(
							relativeProgress, duration));
			}

		}

		@Override
		public void onStopTrackingTouch(SeekBar bar) {
			// mPosOverride = -1;
			mFromTouch = false;
			long timeMs = PodcastBaseFragment.mPlayerServiceBinder.duration()
					* bar.getProgress() / 1000;
			try {
				if (PodcastBaseFragment.mPlayerServiceBinder.isInitialized()
						&& PodcastBaseFragment.mPlayerServiceBinder
								.getCurrentItem().equals(item))
					PodcastBaseFragment.mPlayerServiceBinder.seek(timeMs);
			} catch (Exception ex) {
			}
			// log.debug("mFromTouch = false; ");

		}

	}

}
