package hu.bubbanet.popularmovies;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import hu.bubbanet.popularmovies.Network_utils.Review;

/**
 * Created by Anita Goldpergel (anita.goldpergel@gmail.com) on 2018.07.18.
 *
 * * * REVIEWADAPTER Class
 *
 */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewAdapterViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(int position, Review review, CardView textView);
    }

    private Context context;
    private List<Review> reviewList;
    private OnItemClickListener itemClickListener;

    public ReviewAdapter(List<Review> reviewList) {
        this.reviewList = reviewList;
    }

    @Override
    public ReviewAdapter.ReviewAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        final Context context = viewGroup.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.review_item, viewGroup, false);

        return new ReviewAdapterViewHolder(view);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(final ReviewAdapterViewHolder holder, int position) {
        Review review = reviewList.get(position);

        holder.authorView.setText(review.getAuthor());
        holder.contentView.setText(review.getContent());
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        if (null == reviewList) return 0;
        return reviewList.size();
    }

    // Define viewholder
    public class ReviewAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CardView cardView;
        TextView authorView;
        TextView contentView;

        public ReviewAdapterViewHolder(final View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cv_review_item);
            authorView = itemView.findViewById(R.id.tv_review_author);
            contentView = itemView.findViewById(R.id.tv_review_content);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (itemClickListener != null) {
                int pos = getAdapterPosition();
                Review review = reviewList.get(pos);
                itemClickListener.onItemClick(pos, review, cardView);
            }
        }
    }

    public void setOnItemClickListener(final OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    // Helper method to set the actual review list into the recyclerview on the activity
    public void setReviewList(List<Review> reviewList) {
        this.reviewList = reviewList;
        notifyDataSetChanged();
    }
}
