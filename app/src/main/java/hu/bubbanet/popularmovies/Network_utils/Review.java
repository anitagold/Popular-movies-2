package hu.bubbanet.popularmovies.Network_utils;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Anita Goldpergel (anita.goldpergel@gmail.com) on 2018.07.18.
 *
 * * * REVIEW Class
 *
 */

public class Review implements Parcelable {

    @SerializedName("id")
    private String id;
    @SerializedName("author")
    private String author;
    @SerializedName("content")
    private String content;

    public Review() {
    }

    public Review(String id, String author, String content) {
        this.id = id;
        this.author = author;
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(id);
        parcel.writeString(author);
        parcel.writeString(content);
    }

    public static final Creator<Review> CREATOR = new Creator<Review>() {
        public Review createFromParcel(Parcel pc) {
            return new Review(pc);
        }

        public Review[] newArray(int size) {
            return new Review[size];
        }
    };

    public Review(Parcel pc) {
        id = pc.readString();
        author = pc.readString();
        content = pc.readString();
    }
}
