package com.battlelancer.seriesguide.jobs.episodes;

import android.content.Context;
import android.net.Uri;
import com.battlelancer.seriesguide.appwidget.ListWidgetProvider;
import com.battlelancer.seriesguide.enums.EpisodeFlags;
import com.battlelancer.seriesguide.provider.SeriesGuideContract;
import com.uwetrottmann.seriesguide.backend.episodes.model.Episode;
import com.uwetrottmann.trakt5.entities.SyncSeason;
import java.util.List;

public class EpisodeWatchedPreviousJob extends BaseJob {

    private long episodeFirstAired;

    public EpisodeWatchedPreviousJob(int showTvdbId, long episodeFirstAired) {
        super(showTvdbId, EpisodeFlags.WATCHED, JobAction.EPISODE_WATCHED_PREVIOUS);
        this.episodeFirstAired = episodeFirstAired;
    }

    @Override
    public Uri getDatabaseUri() {
        return SeriesGuideContract.Episodes.buildEpisodesOfShowUri(
                String.valueOf(getShowTvdbId()));
    }

    @Override
    public String getDatabaseSelection() {
        // must
        // - be released before current episode,
        // - have a release date,
        // - be unwatched or skipped
        return SeriesGuideContract.Episodes.FIRSTAIREDMS + "<" + episodeFirstAired
                + " AND " + SeriesGuideContract.Episodes.SELECTION_HAS_RELEASE_DATE
                + " AND " + SeriesGuideContract.Episodes.SELECTION_UNWATCHED_OR_SKIPPED;
    }

    @Override
    public List<SyncSeason> getEpisodesForTrakt(Context context) {
        return buildTraktEpisodeList(context);
    }

    @Override
    protected void setHexagonFlag(Episode episode) {
        episode.setWatchedFlag(EpisodeFlags.WATCHED);
    }

    @Override
    protected String getDatabaseColumnToUpdate() {
        return SeriesGuideContract.Episodes.WATCHED;
    }

    @Override
    public boolean applyLocalChanges(Context context) {
        if (!super.applyLocalChanges(context)) {
            return false;
        }

        // we don't care about the last watched episode value
        // always update last watched time, this type only marks as watched
        updateLastWatched(context, -1, true);

        ListWidgetProvider.notifyAllAppWidgetsViewDataChanged(context);

        return true;
    }

    @Override
    public String getConfirmationText(Context context) {
        return null;
    }
}
