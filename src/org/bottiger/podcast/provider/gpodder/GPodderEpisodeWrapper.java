package org.bottiger.podcast.provider.gpodder;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.bottiger.podcast.provider.FeedItem;
import org.bottiger.podcast.provider.Subscription;
import org.bottiger.podcast.utils.StrUtils;

import android.content.ContentResolver;

public class GPodderEpisodeWrapper {

	private String guid;
	private String title;
	private String subtitle;
	private String short_title;
	private String content;
	private int number;
	private int released;
	private String description;
	private String link;
	private String author;
	private int duration;
	private String language;
	private String license;
	private Collection<String> content_types;
	private Collection<JSONFile> files;

	public static class JSONFile {
		private int filesize;
		private String minetype;
		private Collection<String> urls;
	}

	/*
	 * public String fetch-feed; public String fetch-logo; public String
	 * hub-subscription;
	 */

	public FeedItem toFeedItem(ContentResolver contentResolver,
			Subscription subscription) {

		SimpleDateFormat dt = new SimpleDateFormat(FeedItem.default_format);

		FeedItem episode = FeedItem.getByURL(contentResolver, link);
		if (episode == null) {
			episode = new FeedItem();
		}

		Date time = new Date(released * 1000);

		episode.setTitle(title);
		episode.sub_title = subtitle;
		episode.content = content;
		episode.date = dt.format(time);
		episode.setEpisodeNumber(number);
		episode.author = author;
		
		episode.duration_ms = duration * 1000;
		episode.duration_string = StrUtils.formatTime(episode.duration_ms);
		
		episode.sub_id = subscription.getId();
		episode.image = subscription.getImageURL();

		if (files.size() > 0) {
			JSONFile file = files.iterator().next();
			episode.filesize = file.filesize;
			episode.url = file.urls.iterator().next();
		}
		
		episode.resource = episode.url;

		return episode;
	}
}