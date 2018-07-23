package hu.bubbanet.popularmovies.Network_utils;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Anita Goldpergel (anita.goldpergel@gmail.com) on 2018.07.18.
 *
 * * * MOVIEENDPOINTS Class
 *
 */
public interface MovieEndpoints {

    @GET("movie/{preference}")
    Call<MovieResponse> getMoviesByChosenPreference(@Path("preference") String orderBy, @Query("api_key") String apiKey, @Query("page") int pageIndex, @Query("language") String usedLang);

    @GET("movie/{id}/videos")
    Call<TrailerResponse> getTrailerData(@Path("id") int movieId, @Query("api_key") String apiKey, @Query("language") String usedLan);

    @GET("movie/{id}/reviews")
    Call<ReviewResponse> getReviewData(@Path("id") int movieId, @Query("api_key") String apiKey, @Query("language") String usedLan);
}
