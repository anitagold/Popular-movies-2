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

import hu.bubbanet.popularmovies.Network_utils.Trailer;

/**
 * Created by Anita Goldpergel (anita.goldpergel@gmail.com) on 2018.07.18.
 *
 * * * TRAILERADAPTER Class
 *
 */
public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerAdapterViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(int position, Trailer trailer, ImageView imageView);
    }

    private Context context;
    private List<Trailer> trailerList;
    private OnItemClickListener itemClickListener;

    public TrailerAdapter(List<Trailer> trailerList) {
        this.trailerList = trailerList;
    }

    @Override
    public TrailerAdapter.TrailerAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        final Context context = viewGroup.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.trailer_item, viewGroup, false);

        return new TrailerAdapterViewHolder(view);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(final TrailerAdapterViewHolder holder, int position) {
        Trailer trailer = trailerList.get(position);

        String nameString = String.valueOf(trailer.getVideoName());
        String thumbnailUrl = "http://img.youtube.com/vi/" + trailer.getVideoKey() + "/1.jpg";
        Picasso.with(holder.itemView.getContext())
                .load(thumbnailUrl)
                .placeholder(R.drawable.placeholder)
                .into(holder.trailerView);
        holder.trailerName.setText(nameString);
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        if (null == trailerList) return 0;
        return trailerList.size();
    }

    // Define viewholder
    public class TrailerAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CardView cardView;
        ImageView trailerView;
        TextView trailerName;

        public TrailerAdapterViewHolder(final View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cv_item);
            trailerView = itemView.findViewById(R.id.iv_trailer);
            trailerName = itemView.findViewById(R.id.tv_trailer_name);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (itemClickListener != null) {
                int pos = getAdapterPosition();
                Trailer trailer = trailerList.get(pos);
                itemClickListener.onItemClick(pos, trailer, trailerView);
            }
        }
    }

    public void setOnItemClickListener(final OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    // Helper method to set the actual trailer list into the recyclerview on the activity
    public void setTrailerList(List<Trailer> trailerList) {
        this.trailerList = trailerList;
        notifyDataSetChanged();
    }
}
