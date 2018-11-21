package com.example.xyzreader.data;

import android.os.Parcel;
import android.os.Parcelable;

public class ArticleDetails implements Parcelable {

    /** Type: INTEGER PRIMARY KEY AUTOINCREMENT */
    public long id;
    /** Type: TEXT */
    public String serverId;
    /** Type: TEXT NOT NULL */
    public String title;
    /** Type: TEXT NOT NULL */
    public String author;
    /** Type: TEXT NOT NULL */
    public String body;
    /** Type: TEXT NOT NULL */
    public String thumbUrl;
    /** Type: TEXT NOT NULL */
    public String photoUrl;
    /** Type: REAL NOT NULL DEFAULT 1.5 */
    public long aspectRatio;
    /** Type: INTEGER NOT NULL DEFAULT 0 */
    public String publishedDate;

    public ArticleDetails() {

    }

    private ArticleDetails(Parcel in) {
        id = in.readLong();
        serverId = in.readString();
        title = in.readString();
        author = in.readString();
        body = in.readString();
        thumbUrl = in.readString();
        photoUrl = in.readString();
        aspectRatio = in.readLong();
        publishedDate = in.readString();
    }

    public static final Parcelable.Creator<ArticleDetails> CREATOR = new Parcelable.Creator<ArticleDetails>() {
        @Override
        public ArticleDetails createFromParcel(Parcel in) {
            return new ArticleDetails(in);
        }

        @Override
        public ArticleDetails[] newArray(int size) {
            return new ArticleDetails[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(serverId);
        dest.writeString(title);
        dest.writeString(author);
        dest.writeString(body);
        dest.writeString(thumbUrl);
        dest.writeString(photoUrl);
        dest.writeLong(aspectRatio);
        dest.writeString(publishedDate);
    }
}
