package com.battlelancer.seriesguide.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.provider.CalendarContract;
import android.text.format.DateUtils;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.app.ShareCompat;
import androidx.core.app.ShareCompat.IntentBuilder;
import com.battlelancer.seriesguide.R;
import com.battlelancer.seriesguide.thetvdbapi.TvdbLinks;

/**
 * Contains helpers to share a show, episode (share intent, calendar event) or movie.
 */
public class ShareUtils {

    protected static final String TAG = "ShareUtils";

    public static void shareEpisode(Activity activity, @Nullable String showTvdbSlug,
            int showTvdbId, int seasonTvdbId, int episodeTvdbId, int seasonNumber,
            int episodeNumber, String showTitle, String episodeTitle) {
        String message = showTitle + " - " + TextTools.getNextEpisodeString(activity, seasonNumber,
                episodeNumber, episodeTitle) + " "
                + TvdbLinks.episode(showTvdbSlug, showTvdbId, seasonTvdbId, episodeTvdbId);
        startShareIntentChooser(activity, message, R.string.share_episode);
    }

    public static void shareShow(Activity activity, @Nullable String showTvdbSlug, int showTvdbId,
            String showTitle) {
        String message = showTitle + " " + TvdbLinks.show(showTvdbSlug, showTvdbId);
        startShareIntentChooser(activity, message, R.string.share_show);
    }

    public static void shareMovie(Activity activity, int movieTmdbId, String movieTitle) {
        String message = movieTitle + " " + TmdbTools.buildMovieUrl(movieTmdbId);
        startShareIntentChooser(activity, message, R.string.share_movie);
    }

    /**
     * Share a text snippet. Displays a share intent chooser with the given title, share type is
     * text/plain.
     */
    public static void startShareIntentChooser(Activity activity, String message,
            @StringRes int titleResId) {
        IntentBuilder ib = ShareCompat.IntentBuilder.from(activity);
        ib.setText(message);
        ib.setChooserTitle(titleResId);
        ib.setType("text/plain");
        try {
            ib.startChooser();
        } catch (ActivityNotFoundException e) {
            // no activity available to handle the intent
            Toast.makeText(activity, R.string.app_not_available, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Launches a calendar insert intent for the given episode.
     */
    public static void suggestCalendarEvent(Context context, String showTitle, String episodeTitle,
            long episodeReleaseTime, int showRunTime) {
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, showTitle)
                .putExtra(CalendarContract.Events.DESCRIPTION, episodeTitle);

        long beginTime = TimeTools.applyUserOffset(context, episodeReleaseTime).getTime();
        long endTime = beginTime + showRunTime * DateUtils.MINUTE_IN_MILLIS;
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime);
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime);

        if (!Utils.tryStartActivity(context, intent, false)) {
            Toast.makeText(context, context.getString(R.string.addtocalendar_failed),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
