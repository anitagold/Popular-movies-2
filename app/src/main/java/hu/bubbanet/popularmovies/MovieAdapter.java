package hu.bubbanet.popularmovies;

import java.util.List;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import hu.bubbanet.popularmovies.Network_utils.Movie;

/**
 * Created by Anita Goldpergel (anita.goldpergel@gmail.com) on 2018.07.18.
 *
 * * * MOVIEADAPTER Class
 *
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    private Context context;
    private boolean isLoadingAdded = false;

    public interface OnItemClickListener  {
        void onItemClick(int position, Movie movie, ImageView imageView);
    }

    private List<Movie> movieList;
    public List<Movie> movies()//180316
    {
        return movieList;
    }
    private OnItemClickListener itemClickListener;

    public MovieAdapter(List<Movie> movieList) {
        this.movieList = movieList;
    }

    @Override
    public MovieAdapter.MovieAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        context = viewGroup.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_item, viewGroup, false);

        return new MovieAdapterViewHolder(view);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(final MovieAdapterViewHolder holder, int position) {
        Movie movie = movieList.get(position);

        String voteString = String.valueOf(movie.getVoteAverage());
        holder.ratingView.setText(context.getResources().getString(R.string.rating) + voteString + "/10");

        Picasso.with(holder.itemView.getContext())
                .load(movie.getImageUriString())
                .into(holder.moviePosterView);

        holder.moviePosterView.setTransitionName(
                getImageTransitionName(holder.moviePosterView.getContext(), position)
        );
    }

    public String getImageTransitionName(Context context, int position) {
        return context.getString(R.string.movie_transition_name) + position;
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        if (movieList == null) return 0;
        return movieList.size();
    }

    // Define viewholder
    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CardView cardView;
        ImageView moviePosterView;
        TextView ratingView;

        public MovieAdapterViewHolder(final View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cv_item);
            moviePosterView = itemView.findViewById(R.id.iv_thumbnail);
            ratingView = itemView.findViewById(R.id.tv_avg_votes);

            itemView.setOnClickListener(this);
        }

        @Override
        // onclick
        public void onClick(View v) {
            if (itemClickListener != null) {
                int pos = getAdapterPosition();
                Movie movie = movieList.get(pos);
                itemClickListener.onItemClick(pos, movie, moviePosterView);
            }
        }
    }

    public void setOnItemClickListener(final OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    // Helper method to set the actual movie list into the recyclerview on the activity
    public void setMovieList(List<Movie> movieList) {
        this.movieList = movieList;
        notifyDataSetChanged();
    }

    public void add(Movie r) {
        movieList.add(r);
        notifyItemInserted(movieList.size() - 1);
    }

    public void addAll(List<Movie> moveResults) {
        for (Movie result : moveResults) {
            add(result);
        }
    }

    public void remove(Movie r) {
        int position = movieList.indexOf(r);
        if (position > -1) {
            movieList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        isLoadingAdded = false;
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }


    public void addLoadingFooter() {
        isLoadingAdded = true;
        add(new Movie());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = movieList.size() - 1;
        Movie movie = getItem(position);

        if (movie != null) {
            movieList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public Movie getItem(int position) {
        return movieList.get(position);
    }
}
