package com.battlelancer.seriesguide.ui.search

import android.content.Context
import com.battlelancer.seriesguide.SgApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.regex.Pattern

class TvdbIdExtractor(val context: Context, val text: String) {

    /**
     * Returns the show TVDb ID or -1 if not found for slug. Runs in [Dispatchers.IO] context.
     */
    suspend fun tryToExtractTvdbId(): Int {
        return withContext(Dispatchers.IO) {
            lookUpShowTvdbId()
        }
    }

    private fun lookUpShowTvdbId(): Int {
        // match TVDB URLs like
        // https://www.thetvdb.com/series/lost
        // https://www.thetvdb.com/series/lost/seasons/1
        // https://www.thetvdb.com/series/lost/episodes/127131
        // https://www.thetvdb.com/series/341483
        val tvdbSeriesIdPattern = Pattern.compile("thetvdb\\.com/series/([^/\\n\\r]*)")
        val showSlug = matchShowSlug(tvdbSeriesIdPattern, text)
        if (showSlug.isNullOrBlank()) return -1

        val call = SgApp.getServicesComponent(context).tvdb()
            .search()
            .series(null, null, null, showSlug, null)
        try {
            val response = call.execute()
            if (response.isSuccessful && response.body() != null) {
                return response.body()!!.data[0].id
            }
        } catch (e: Exception) {
        }
        return -1
    }

    private fun matchShowSlug(pattern: Pattern, text: String): String? {
        val matcher = pattern.matcher(text)
        return if (matcher.find()) {
            matcher.group(1)
        } else {
            null
        }
    }

}