package hu.bubbanet.popularmovies;

import android.support.v4.app.LoaderManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.net.Uri;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ProgressBar;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import hu.bubbanet.popularmovies.Utils.Utils;
import hu.bubbanet.popularmovies.Network_utils.ApiUtil;
import hu.bubbanet.popularmovies.Network_utils.Movie;
import hu.bubbanet.popularmovies.Network_utils.MovieResponse;
import hu.bubbanet.popularmovies.Network_utils.MovieEndpoints;
import hu.bubbanet.popularmovies.Data.FavouritesContract;
import hu.bubbanet.popularmovies.Data.FavouritesListLoader;

/**
 * Created by Anita Goldpergel (anita.goldpergel@gmail.com) on 2018.07.18.
 *
 * * * MAINACTIVITY Class
 *
 */
public class MainActivity extends AppCompatActivity implements
        MovieAdapter.OnItemClickListener, LoaderManager.LoaderCallbacks<List<Movie>> {

    private static final String API_KEY = BuildConfig.API_KEY;
    private static final String TAG = "MainActivity";
    private static final int ID_MOVIE_LIST_LOADER = 1;
    private final String ORDER_BY_POPULAR = "popular";
    private final String ORDER_BY_TOP_RATED = "top_rated";
    private final String ORDER_BY_NOW_PLAYING = "now_playing";
    private final String ORDER_BY_FAVOURITES = "favourites";
    static final String MOVIE_DETAILS = "movie_details";
    private static final int PAGE_START = 1;
    private int TOTAL_PAGES;
    private String order = ORDER_BY_POPULAR;
    //At portrait mode there will be 2 columns in the grid, in landscape mode there will be 3.
    private final int COLUMNSNUMBER_PORTRAIT = 2;
    private final int COLUMNSNUMBER_LANDSCAPE = 3;

    private EndlessRecyclerViewScrollListener endlessRecyclerView;
    GridLayoutManager layoutManager;
    private ProgressBar progressBar;
    private List<Movie> moviesList = new ArrayList<>();
    private RecyclerView recyclerView;
    private TextView emptyTextView;
    private String subtitle;

    private MovieEndpoints movieEndpoints;
    private MovieAdapter movieAdapter;
    private int currentPage = PAGE_START;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    int lastVisible;
    int selectedItem = 0; //default: popular

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        subtitle = getResources().getString(R.string.most_popular);
        getSupportActionBar().setSubtitle(subtitle);

        // findviewbyids
        recyclerView = findViewById(R.id.rv_movies);
        progressBar = findViewById(R.id.progress_bar);
        emptyTextView = findViewById(R.id.tv_empty_state);
        progressBar.setVisibility(View.VISIBLE);

        int columnsNumber;

        int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            columnsNumber = COLUMNSNUMBER_PORTRAIT;
        } else {
            columnsNumber = COLUMNSNUMBER_LANDSCAPE;
        }
        layoutManager = new GridLayoutManager(this, columnsNumber);
        recyclerView.setLayoutManager(layoutManager);

        boolean thereAreMovies = false;
        if (Utils.movieAdapter != null && Utils.movieAdapter.movies().size() > 0) {
            movieAdapter = Utils.movieAdapter;
            thereAreMovies = true;
            isFirstMovieList = true;
            progressBar.setVisibility(View.GONE);
        } else {
            movieAdapter = new MovieAdapter(moviesList);
        }

        if (Utils.deleteItem) {
            getSupportLoaderManager().restartLoader(ID_MOVIE_LIST_LOADER, null, this);
            Utils.deleteItem = false;
        }

        movieAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(movieAdapter);

        movieEndpoints = ApiUtil.getMovieEndpoints();

        endlessRecyclerView = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                isLoading = true;
                currentPage = page + 1;
                loadData();
            }
        };

        recyclerView.addOnScrollListener(endlessRecyclerView);

        CheckInternet();
        if (!thereAreMovies && Utils.isConnectedToInternet(this)) {
            loadData();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("order", order);
        outState.putString("subtitle", subtitle);
        outState.putInt("selectedItem", selectedItem);

        // Save list scrolled state
        lastVisible = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
    }

    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        order = state.getString("order");
        subtitle = state.getString("subtitle");
        getSupportActionBar().setSubtitle(subtitle);

        selectedItem = state.getInt("selectedItem");
        OptionsItemLoad(selectedItem);

        if (Utils.deleteItem) {
            getSupportLoaderManager().destroyLoader(ID_MOVIE_LIST_LOADER);
        }
    }

    boolean isFirstMovieList = false; //checks if this is the first set of moviesList (page 1)
    private void loadData() {
        if (!isFirstMovieList) {
            loadFirstMoviesByChosenPreference();
            isFirstMovieList = true;
        } else {
            loadNextDataFromApi(currentPage);
        }
        Utils.movieAdapter = movieAdapter;
    }

    private void CheckInternet() {
        //init Retrofit service and load data if there is internet
        if (!Utils.isConnectedToInternet(this)) {
            View progressBar = findViewById(R.id.progress_bar);
            progressBar.setVisibility(View.GONE);

            // Update empty state with no connection error message
            movieAdapter.setMovieList(null);
            emptyTextView.setVisibility(View.VISIBLE);
            emptyTextView.setTextColor(getResources().getColor(R.color.colorWhite));
            emptyTextView.setText(R.string.no_internet_connection);
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

    public void loadFirstMoviesByChosenPreference() {

        String appLanguage = selectAppLanguage();
        endlessRecyclerView.resetState();
        currentPage = 1;
        isLastPage = false;
        movieAdapter.clear();

        movieEndpoints.getMoviesByChosenPreference(order, API_KEY, currentPage, appLanguage).enqueue(
                new Callback<MovieResponse>() {
                    @Override
                    public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                        Callback_onResponse(response, true);
                    }

                    @Override
                    public void onFailure(Call<MovieResponse> call, Throwable t) {
                        emptyTextView.setText("Error loading from API");
                        Log.d("MainActivity", "error loading from API");
                    }
                }
        );
    }

    public void loadNextDataFromApi(final int offset) {
        Log.d(TAG, "loadNextPage: " + currentPage);
        String appLanguage = selectAppLanguage();

        movieEndpoints.getMoviesByChosenPreference(order, API_KEY, offset, appLanguage).enqueue(
                new Callback<MovieResponse>() {
                    @Override
                    public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                        Callback_onResponse(response, false);
                    }

                    @Override
                    public void onFailure(Call<MovieResponse> call, Throwable t) {
                        emptyTextView.setText("Error loading from API");
                        Log.d("MainActivity", "error loading from API");
                    }
                }
        );
    }

    private void Callback_onResponse(Response<MovieResponse> response, boolean isFirst) {
        if (response.isSuccessful()) {
            //first time
            if (isFirst) {
                TOTAL_PAGES = response.body().getTotalPages();
                List<Movie> movieList = (response.body().getResults());
                movieAdapter.setMovieList(movieList);
                progressBar.setVisibility(View.GONE);
                emptyTextView.setVisibility(View.GONE);
                if (currentPage <= TOTAL_PAGES) {
                    movieAdapter.addLoadingFooter();
                } else {
                    isLastPage = true;
                }
            }

            if (!isFirst) {
                movieAdapter.removeLoadingFooter();
                isLoading = false;
                List<Movie> newMovies = response.body().getResults();
                movieAdapter.addAll(newMovies);
                progressBar.setVisibility(View.GONE);
                emptyTextView.setVisibility(View.GONE);
                if (currentPage != TOTAL_PAGES) {
                    movieAdapter.addLoadingFooter();
                } else {
                    isLastPage = true;
                }
            }
        } else {
            int statusCode = response.code();
        }
        Log.d("MainActivity", "load from API");
    }

    //Create menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.movies, menu);
        return true;
    }

    //Options item selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        selectedItem = item.getItemId();
        OptionsItemLoad(selectedItem);
        return super.onOptionsItemSelected(item);
    }

    private void OptionsItemLoad(int index) {

        if (selectedItem == R.id.most_popular) {
            if (!order.equals(ORDER_BY_POPULAR)) {
                orderMoviesBy(ORDER_BY_POPULAR);
            }
        }
        if (selectedItem == R.id.highest_rated) {
            if (!order.equals(ORDER_BY_TOP_RATED)) {
                lastVisible = 0;
                orderMoviesBy(ORDER_BY_TOP_RATED);
            }
        }
        if (selectedItem == R.id.now_playing) {
            if (!order.equals(ORDER_BY_NOW_PLAYING)) {
                orderMoviesBy(ORDER_BY_NOW_PLAYING);
            }
        }
        if (selectedItem == R.id.favourite) {
            if (!order.equals(ORDER_BY_FAVOURITES)) {
                movieAdapter.setMovieList(null);
                orderMoviesBy(ORDER_BY_FAVOURITES);
            }
        }
    }

    //AsyncTask Loader to load favourites data from SQLite db
    //onCreateLoader
    @Override
    public Loader<List<Movie>> onCreateLoader(int id, Bundle args) {
        Uri uri = FavouritesContract.FavouritesEntry.CONTENT_URI;
        return new FavouritesListLoader(this, uri);
    }

    @Override
    //onLoadFinished
    public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> movieList) {
        //hide the progress bar
        progressBar.setVisibility(View.GONE);

        // Clear the adapter
        movieAdapter.setMovieList(null);

        // If there is a valid movieList, add them to the adapter
        if (movieList != null && !movieList.isEmpty()) {
            emptyTextView.setVisibility(View.GONE);
            movieAdapter.setMovieList(movieList);
            movieAdapter.notifyDataSetChanged();

        } else {
            emptyTextView.setText(R.string.no_movies);
            emptyTextView.setTextColor(getResources().getColor(R.color.colorWhite));
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Movie>> loader) {
        movieAdapter.clear();
    }

    //movie was chosen
    //onItemClick with transition
    @Override
    public void onItemClick(int position, Movie movie, ImageView imageView) {
        Intent intent = new Intent(MainActivity.this, DetailsActivity.class);

        //Passes the chosen Movie to Details Activity
        intent.putExtra(MOVIE_DETAILS, movie);

        //Passes the shared element info for transition
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, imageView, "transition");
        ActivityCompat.startActivity(MainActivity.this, intent, options.toBundle());
    }

    public void orderMoviesBy(String sortOrder) {
        switch (sortOrder) {
            case ORDER_BY_FAVOURITES:

                lastVisible = 0;
                order = ORDER_BY_FAVOURITES;
                subtitle = getResources().getString(R.string.favourite);
                getSupportActionBar().setSubtitle(subtitle);
                getSupportLoaderManager().initLoader(ID_MOVIE_LIST_LOADER, null, this);
                break;

            default:
                lastVisible = 0;
                orderBy(sortOrder);
                getSupportActionBar().setSubtitle(subtitle);
                getSupportLoaderManager().destroyLoader(ID_MOVIE_LIST_LOADER);
                CheckInternet();
                if (Utils.isConnectedToInternet(this)) {
                    isFirstMovieList = false;
                    loadData();
                }
        }
    }

    public String orderBy(String param) {
        switch (param) {
            case ORDER_BY_TOP_RATED:
                order = ORDER_BY_TOP_RATED;
                subtitle = getResources().getString(R.string.highest_rated);
                break;
            case ORDER_BY_NOW_PLAYING:
                order = ORDER_BY_NOW_PLAYING;
                subtitle = getResources().getString(R.string.now_playing);
                break;
            default:
                order = ORDER_BY_POPULAR;
                subtitle = getResources().getString(R.string.most_popular);
        } return order;
    }
}

