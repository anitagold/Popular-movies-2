package hu.bubbanet.popularmovies.Utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import hu.bubbanet.popularmovies.MovieAdapter;

/**
 * Created by Anita Goldpergel (anita.goldpergel@gmail.com) on 2018.07.18.
 *
 * * * UTILS Class
 *
 */

public class Utils {

    static public boolean isConnectedToInternet(Activity activity) {
        boolean isConnectedToInternet = false;
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        assert connectivityManager != null; //line suggested by Lint
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        isConnectedToInternet = networkInfo != null && networkInfo.isConnected();
        return isConnectedToInternet;
    }

    static public boolean deleteItem;
    static public MovieAdapter movieAdapter;


}
