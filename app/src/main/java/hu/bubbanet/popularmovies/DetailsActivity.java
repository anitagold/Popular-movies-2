package hu.bubbanet.popularmovies;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.util.Log;
import android.graphics.Color;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import hu.bubbanet.popularmovies.Data.FavouritesContract;
import hu.bubbanet.popularmovies.Data.FavouritesLoader;
import hu.bubbanet.popularmovies.Network_utils.ApiUtil;
import hu.bubbanet.popularmovies.Network_utils.Movie;
import hu.bubbanet.popularmovies.Network_utils.MovieEndpoints;
import hu.bubbanet.popularmovies.Network_utils.Review;
import hu.bubbanet.popularmovies.Network_utils.ReviewResponse;
import hu.bubbanet.popularmovies.Network_utils.Trailer;
import hu.bubbanet.popularmovies.Network_utils.TrailerResponse;
import hu.bubbanet.popularmovies.Utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Anita Goldpergel (anita.goldpergel@gmail.com) on 2018.07.18.
 *
 * * * DETAILSACTIVITY Class
 *
 */
public class DetailsActivity extends AppCompatActivity implements
        TrailerAdapter.OnItemClickListener, LoaderManager.LoaderCallbacks<Integer> {

    private static final String API_KEY = BuildConfig.API_KEY;
    private int MOVIE_ID;
    private static final int ID_MOVIE_LOADER = 2;
    static final String MOVIE_DETAILS = "movie_details";
    private TextView originalTitleView, titleView, releaseDateView, overviewView, emptyStateTextView, tv, emptyReview;
    private ImageView backdropView, posterView, favouriteButton;
    int pStatus = 0;
    private Handler handler = new Handler();
    private List<Trailer> trailerList = new ArrayList<>();
    private List<Review> reviewList = new ArrayList<>();
    private TrailerAdapter trailerAdapter;
    private ReviewAdapter reviewAdapter;
    private RecyclerView trailerRecyclerView, reviewRecyclerView;
    private boolean isFavourite;
    private MovieEndpoints videoEndpoints, reviewEndpoints;
    private Movie selectedMovie;

    @Override
    //oncreate
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Objects.requireNonNull(getSupportActionBar()).hide();

        selectedMovie = getIntent().getParcelableExtra(MOVIE_DETAILS);
        MOVIE_ID = selectedMovie.getId();

        findViews();
        setViews(selectedMovie);
        setVoteAverageProgressBar(selectedMovie);

        //trailers: horizontal, reviews: vertical
        LinearLayoutManager videoLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        trailerRecyclerView.setLayoutManager(videoLayoutManager);
        trailerAdapter = new TrailerAdapter(trailerList);
        trailerAdapter.setOnItemClickListener(this);
        trailerRecyclerView.setAdapter(trailerAdapter);

        LinearLayoutManager reviewLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        reviewRecyclerView.setLayoutManager(reviewLayoutManager);
        reviewAdapter = new ReviewAdapter(reviewList);
        reviewRecyclerView.setAdapter(reviewAdapter);

        //init Retrofit service
        videoEndpoints = ApiUtil.getMovieEndpoints();
        reviewEndpoints = ApiUtil.getMovieEndpoints();
        isConnectedToInternet();

        getSupportLoaderManager().initLoader(ID_MOVIE_LOADER, null, this);
    }

    @Override
    //onsaveinstancestate
    protected void onSaveInstanceState(Bundle favouriteBundle) {
        super.onSaveInstanceState(favouriteBundle);
        favouriteBundle.putBoolean("isFavourite", isFavourite);
    }

    protected void onRestoreInstanceState(Bundle favouriteBundle) {
        super.onRestoreInstanceState(favouriteBundle);
        isFavourite = favouriteBundle.getBoolean("isFavourite");
        setFavouriteButton();
    }

    private void isConnectedToInternet() {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get the networkinfo
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        // there is a network connection
        if (networkInfo != null && networkInfo.isConnected()) {
            loadVideos();
            loadReviews();
        } else {
            // Update empty state with no connection error message
            trailerAdapter.setTrailerList(null);
            reviewAdapter.setReviewList(null);
            emptyStateTextView.setVisibility(View.VISIBLE);
            emptyStateTextView.setTextColor(getResources().getColor(R.color.colorWhite));
            emptyStateTextView.setText(R.string.no_internet_connection);
        }
    }

    // findviewbyids
    private void findViews() {
        trailerRecyclerView = findViewById(R.id.rv_trailers);
        reviewRecyclerView = findViewById(R.id.rv_reviews);
        originalTitleView = findViewById(R.id.tv_original_title);
        titleView = findViewById(R.id.tv_details_title);
        releaseDateView = findViewById(R.id.tv_release_date);
        backdropView = findViewById(R.id.iv_detail_backdrop);
        overviewView = findViewById(R.id.tv_overview);
        posterView = findViewById(R.id.iv_detail_poster);
        emptyStateTextView = findViewById(R.id.tv_details_empty_state);
        favouriteButton = findViewById(R.id.iv_heart);
        emptyReview = findViewById(R.id.empty_review);
    }

    //setviews for selected movie
    private void setViews(Movie selectedMovie) {
        emptyStateTextView.setVisibility(View.GONE);
        String backdropURL = selectedMovie.getBackdropUriString();
        Picasso.with(this)
                .load(backdropURL)
                .into(backdropView);
        originalTitleView.setText(selectedMovie.getOriginalTitle());
        titleView.setText(selectedMovie.getTitle());
        releaseDateView.setText(selectedMovie.getReleaseDate());
        String overview = selectedMovie.getOverview();

        if (overview.length() == 0) {
            overviewView.setText(getResources().getText(R.string.not_available_in_your_language));
        } else {
            overviewView.setText(selectedMovie.getOverview());
        }

        String posterURL = selectedMovie.getImageUriString();
        Picasso.with(this)
                .load(posterURL)
                .into(posterView);
        setFavouriteButton();
    }

    private void setVoteAverageProgressBar(final Movie chosenMovie) {
        final ProgressBar mProgress = findViewById(R.id.circularProgressbar);
        mProgress.setProgress(0);   // Main Progress
        mProgress.setSecondaryProgress(100); // Secondary Progress
        mProgress.setMax(100); // Maximum Progress

        tv = findViewById(R.id.tv);
        new Thread(new Runnable() {

            @Override
            public void run() {
                int maxValue = (int) (chosenMovie.getVoteAverage() * 10);
                while (pStatus < maxValue) {
                    pStatus += 1;

                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            mProgress.setProgress(pStatus);
                            tv.setText(String.valueOf(chosenMovie.getVoteAverage()));
                        }
                    });
                    try {
                        // Sleep for 200 milliseconds.
                        // Just to display the progress slowly
                        Thread.sleep(16); //thread will take approx 3 seconds to finish
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    // onitemclick
    public void onItemClick(int position, Trailer trailer, ImageView imageView) {
        Uri uri = Uri.parse("https://www.youtube.com/watch?v=" + trailer.getVideoKey());
        Intent trailerIntent = new Intent(Intent.ACTION_VIEW);
        trailerIntent.setData(uri);
        startActivity(trailerIntent);
    }

    public void addToFavourites(View view) {
        if (!isFavourite) { //new favourite
            ContentValues contentValues = new ContentValues();
            contentValues.put(FavouritesContract.FavouritesEntry.COLUMN_FAV_MOVIE_ID, selectedMovie.getId());
            contentValues.put(FavouritesContract.FavouritesEntry.COLUMN_FAV_ORIGINAL_TITLE, selectedMovie.getOriginalTitle());
            contentValues.put(FavouritesContract.FavouritesEntry.COLUMN_FAV_TITLE, selectedMovie.getTitle());
            contentValues.put(FavouritesContract.FavouritesEntry.COLUMN_FAV_POSTER_PATH, selectedMovie.getPosterPath());
            contentValues.put(FavouritesContract.FavouritesEntry.COLUMN_FAV_BACKDROP_PATH, selectedMovie.getBackdropPath());
            contentValues.put(FavouritesContract.FavouritesEntry.COLUMN_FAV_RELEASE_DATE, selectedMovie.getReleaseDate());
            contentValues.put(FavouritesContract.FavouritesEntry.COLUMN_FAV_VOTE_AVERAGE, selectedMovie.getVoteAverage());
            contentValues.put(FavouritesContract.FavouritesEntry.COLUMN_FAV_OVERVIEW, selectedMovie.getOverview());

            Uri uri = getContentResolver().insert(FavouritesContract.FavouritesEntry.CONTENT_URI, contentValues);
            if (uri != null) {
                Toast toast = Toast.makeText(this, R.string.movie_added, Toast.LENGTH_SHORT);
                toast.getView().setBackgroundColor(Color.WHITE);
                toast.getView().setPadding(10, 10, 10, 10);
                TextView text = toast.getView().findViewById(android.R.id.message);
                text.setTextColor(Color.BLACK);
                toast.show();
                isFavourite = true;
                setFavouriteButton();
            }
        } else {
            String[] whereParam = new String[1];
            whereParam[0] = String.valueOf(MOVIE_ID);
            getContentResolver().delete(FavouritesContract.FavouritesEntry.CONTENT_URI,
                    FavouritesContract.FavouritesEntry.COLUMN_FAV_MOVIE_ID + "=?", whereParam);
            Toast toast = Toast.makeText(this, R.string.movie_removed, Toast.LENGTH_SHORT);
            toast.getView().setBackgroundColor(Color.WHITE);
            toast.getView().setPadding(10, 10, 10, 10);
            TextView text = toast.getView().findViewById(android.R.id.message);
            text.setTextColor(Color.BLACK);
            toast.show();
            Utils.deleteItem = true;
            isFavourite = false;
            setFavouriteButton();
        }
    }

    //select the language of the app
    public String selectAppLanguage() {

        String appLanguage = "en"; // default is english
        //Gets the used language of the phone
        String phoneLanguage = Locale.getDefault().getLanguage();

        if (phoneLanguage.equals("hu")) {
            appLanguage = "hu";
        }
        return appLanguage;
    }


    public void loadVideos() {
        String appLanguage = selectAppLanguage();

        videoEndpoints.getTrailerData(MOVIE_ID, API_KEY, appLanguage).enqueue(new Callback<TrailerResponse>() {
            @Override
            public void onResponse(Call<TrailerResponse> call, Response<TrailerResponse> response) {

                if (response.isSuccessful()) {
                    trailerAdapter.setTrailerList(response.body().getResults());
                    Log.d("DetailActivity", "videos loaded from API");
                } else {
                    int statusCode = response.code();
                    // handle request errors depending on status code
                }
            }

            @Override
            public void onFailure(Call<TrailerResponse> call, Throwable t) {
                emptyStateTextView.setVisibility(View.VISIBLE);
                emptyStateTextView.setText("Error loading from API");
                Log.d("DetailActivity", "error loading from API" + t);
            }
        });
    }

    public void loadReviews() {
        //reviews are available only in English
        String reviewLanguage;
        reviewLanguage = "en";

        reviewEndpoints.getReviewData(MOVIE_ID, API_KEY, reviewLanguage).enqueue(new Callback<ReviewResponse>() {
            @Override
            public void onResponse(Call<ReviewResponse> call, Response<ReviewResponse> response) {

                if (response.isSuccessful()) {
                    if (response.body().getResults().isEmpty()) {
                        emptyReview.setVisibility(View.VISIBLE);
                    } else {
                        emptyReview.setVisibility(View.GONE);
                        reviewAdapter.setReviewList(response.body().getResults());
                        Log.d("DetailActivity", "reviewList loaded from API");
                    }
                } else {
                    int statusCode = response.code();
                }
            }

            @Override
            public void onFailure(Call<ReviewResponse> call, Throwable t) {
                emptyStateTextView.setVisibility(View.VISIBLE);
                emptyStateTextView.setText("Error loading from API");
                Log.d("DetailActivity", "error loading from API" + t);
            }
        });
    }

    @Override
    public Loader<Integer> onCreateLoader(int id, Bundle args) {
        Uri uri = FavouritesContract.FavouritesEntry.CONTENT_URI;
        return new FavouritesLoader(this, uri, MOVIE_ID);
    }

    @Override
    public void onLoadFinished(Loader<Integer> loader, Integer data) {
        if (data > 0) {
            isFavourite = true;
            setFavouriteButton();
        } else {
            isFavourite = false;
            setFavouriteButton();
        }
    }

    @Override
    public void onLoaderReset(Loader<Integer> loader) {
    }

    public void setFavouriteButton() {
        if (isFavourite) {
            favouriteButton.setBackgroundResource(R.drawable.fav_button_add);
        } else favouriteButton.setBackgroundResource(R.drawable.fav_button_default);
    }
}
