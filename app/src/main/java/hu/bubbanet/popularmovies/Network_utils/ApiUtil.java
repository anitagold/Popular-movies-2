package hu.bubbanet.popularmovies.Network_utils;

/**
 * Created by Anita Goldpergel (anita.goldpergel@gmail.com) on 2018.07.18.
 *
 * * * APIUTIL Class
 *
 */

public class ApiUtil {
    private static final String BASE_URL = "http://api.themoviedb.org/3/";

    public static MovieEndpoints getMovieEndpoints() {
        return RetrofitClient.getApiCall(BASE_URL).create(MovieEndpoints.class);
    }
}
