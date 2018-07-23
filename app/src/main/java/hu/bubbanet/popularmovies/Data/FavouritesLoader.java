package hu.bubbanet.popularmovies.Data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;

/**
 * Created by Anita Goldpergel (anita.goldpergel@gmail.com) on 2018.07.18.
 *
 * * * FAVOURITESLOADER Class
 *
 */

public class FavouritesLoader extends AsyncTaskLoader<Integer> {
    //Tag for log messages
    private static final String LOG_TAG = FavouritesLoader.class.getName();
    Cursor movieDataCursor = null;
    int movieId;

    //Query URL
    private Uri uri;

    public FavouritesLoader(Context context, Uri uri, int movieId) {
        super(context);
        this.uri = uri;
        this.movieId = movieId;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public Integer loadInBackground() {

        // Perform the db request, and extract a list of favourite movies
        String[] moviIdString = new String[]{String.valueOf(movieId)};
        movieDataCursor = getContext().getContentResolver().query(FavouritesContract.FavouritesEntry.CONTENT_URI,
                null,
                "movie_id = ?",
                moviIdString,
                null);
        return movieDataCursor.getCount();
    }
}
