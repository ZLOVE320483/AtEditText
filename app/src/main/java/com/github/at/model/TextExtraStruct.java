package com.github.at.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by zlove on 2018/3/19.
 */

public class TextExtraStruct implements Parcelable {

    public static final int TYPE_AT = 0;

    int start;
    int end;
    int userId;

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.start);
        dest.writeInt(this.end);
    }

    public TextExtraStruct() {
    }

    protected TextExtraStruct(Parcel in) {
        this.start = in.readInt();
        this.end = in.readInt();
    }

    public static final Creator<TextExtraStruct> CREATOR = new Creator<TextExtraStruct>() {
        @Override
        public TextExtraStruct createFromParcel(Parcel source) {
            return new TextExtraStruct(source);
        }

        @Override
        public TextExtraStruct[] newArray(int size) {
            return new TextExtraStruct[size];
        }
    };


}
