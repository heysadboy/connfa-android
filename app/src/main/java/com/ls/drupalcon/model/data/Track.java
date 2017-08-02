package com.ls.drupalcon.model.data;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.gson.annotations.SerializedName;
import com.ls.drupalcon.model.database.AbstractEntity;
import com.ls.utils.CursorParser;
public class Track extends AbstractEntity<Long> {

    @SerializedName("trackId")
    private long mId;

    @SerializedName("trackName")
    private String mName;

    @SerializedName("order")
    private double mOrder;

    @SerializedName("deleted")
    private boolean mDeleted;

    @Override
    public ContentValues getContentValues() {
        ContentValues result = new ContentValues();
        result.put("_id", mId);
        result.put("track_name", mName);
        result.put("_order", mOrder);
        result.put("_deleted", mDeleted);

        return result;
    }

    @Override
    public void initialize(Cursor theCursor) {
        CursorParser parser = new CursorParser(theCursor);

        mId = parser.readLong();
        mName = parser.readString();
        mOrder = parser.readDouble();
        mDeleted = parser.readBoolean();
    }

    @Override
    public Long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public boolean isDeleted() {
        return mDeleted;
    }

    public double getOrder() {
        return mOrder;
    }

}
