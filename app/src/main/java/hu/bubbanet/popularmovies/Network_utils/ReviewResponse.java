package hu.bubbanet.popularmovies.Network_utils;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Anita Goldpergel (anita.goldpergel@gmail.com) on 2018.07.18.
 *
 * * * REVIEWRESPONSE Class
 *
 */

public class ReviewResponse {
    @SerializedName("id")
    private int id;
    @SerializedName("results")
    private List<Review> results;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Review> getResults() {
        return results;
    }

    public void setResults(List<Review> results) {
        this.results = results;
    }
}
