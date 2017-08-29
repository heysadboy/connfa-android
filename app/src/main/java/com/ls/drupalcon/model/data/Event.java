package com.ls.drupalcon.model.data;

import com.google.gson.annotations.SerializedName;

import com.ls.drupalcon.model.PreferencesManager;
import com.ls.drupalcon.model.database.AbstractEntity;
import com.ls.drupalcon.model.database.AbstractEntityDAO;
import com.ls.utils.CursorStringParser;

import org.jetbrains.annotations.NotNull;

import android.content.ContentValues;
import android.database.Cursor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Event extends AbstractEntity<Long> implements Comparable<Event> {

    //TODO think about better event classes separation
    public static final int PROGRAM_CLASS = 1;
    public static final int BOFS_CLASS = 2;
    public static final int SOCIALS_CLASS = 3;

    @SerializedName("eventId")
    private long mId;

    @SerializedName("from")
    private String mFromTime;

    @SerializedName("to")
    private String mToTime;

    @SerializedName("type")
    private long mType;

    @SerializedName("name")
    private String mName;

    @SerializedName("speakers")
    private List<Long> mSpeakers = new ArrayList<>();

    @SerializedName("track")
    private long mTrack;

    @SerializedName("experienceLevel")
    private long mExperienceLevel;

    @SerializedName("place")
    private String mPlace;

    @SerializedName("text")
    private String mDescription;

    @SerializedName("link")
    private String mLink;

    @SerializedName("order")
    private double mOrder;

    @SerializedName("deleted")
    private boolean mDeleted;

    private Date mDate;
    private TimeRange mTimeRange;

    private long fromMillis;
    private long toMillis;

    private int mEventClass = -1;
    private boolean mIsFavorite = false;

    @Override
    public ContentValues getContentValues() {
        ContentValues result = new ContentValues();
        result.put("_id", mId);
        result.put("_date", mDate != null ? mDate.getTime() : 0);

        long timeFrom = convertTime(mFromTime);
        result.put("_from", timeFrom);

        long timeTo = convertTime(mToTime);
        result.put("_to", timeTo);

        result.put("_type", mType);
        result.put("_name", mName);
        result.put("_track", mTrack);
        result.put("_experience_level", mExperienceLevel);
        result.put("_place", mPlace);
        result.put("_description", mDescription);
        result.put("_event_class", mEventClass);
        result.put("_favorite", AbstractEntityDAO.getIntFromBool(mIsFavorite));
        result.put("_link", mLink);
        result.put("_order", mOrder);
        result.put("_deleted", mDeleted);

        return result;
    }

    @Override
    public void initialize(Cursor theCursor) {
        CursorStringParser parser = new CursorStringParser(theCursor);

        mId = parser.readLong("_id");
        initializeDateAndTime(parser);

        mType = parser.readLong("_type");
        mName = parser.readString("_name");
        mTrack = parser.readLong("_track");
        mExperienceLevel = parser.readLong("_experience_level");
        mPlace = parser.readString("_place");
        mDescription = parser.readString("_description");

        mEventClass = parser.readInt("_event_class");
        mIsFavorite = parser.readBoolean("_favorite");
        mLink = parser.readString("_link");

        mOrder = parser.readInt("_order");
        mDeleted = parser.readBoolean("_deleted");
    }

    public void initializePartly(CursorStringParser parser) {
        mId = parser.readLong("_id");
        mName = parser.readString("_name");
        initializeDateAndTime(parser);

        mType = parser.readLong("_type");
        mName = parser.readString("_name");
        mTrack = parser.readLong("_track");
        mPlace = parser.readString("_place");
        mExperienceLevel = parser.readLong("_experience_level");

        mEventClass = parser.readInt("_event_class");
        mIsFavorite = parser.readBoolean("_favorite");
        mOrder = parser.readDouble("_order");
    }

    private void initializeDateAndTime(CursorStringParser parser) {
        long date = parser.readLong("_date");
        mDate = date != 0 ? new Date(date) : null;

        fromMillis = parser.readLong("_from");
        toMillis = parser.readLong("_to");

        Calendar from = null;
        Calendar to = null;

        if (fromMillis != Long.MAX_VALUE && toMillis != Long.MAX_VALUE) {
            from = Calendar.getInstance(Locale.UK);
            from.setTimeInMillis(fromMillis);

            to = Calendar.getInstance(Locale.UK);
            to.setTimeInMillis(toMillis);

            mFromTime = convertTime(from);
            mToTime = convertTime(to);
        }

        mTimeRange = new TimeRange(date, from, to);
    }

    private static long convertTime(String time) {
        if (time == null) {
            return 0;
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.ENGLISH);
        try {
            Date date = format.parse(time);
            return date.getTime();

        } catch (ParseException e) {
            return 0;
        }
    }

    public static String convertTime(Calendar time) {
        if (time != null) {
            SimpleDateFormat format = new SimpleDateFormat("kk:mm", Locale.ENGLISH);
            format.setTimeZone(PreferencesManager.getInstance().getServerTimeZoneObject());
            return format.format(time.getTime());
        } else {
            return null;
        }
    }

    public void getHashtagTagText() {
//       if(mType)

    }

    @Override
    public Long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public String getFromTime() {
        return mFromTime;
    }

    public void setFromTime(String fromTime) {
        mFromTime = fromTime;
    }

    public String getToTime() {
        return mToTime;
    }

    public void setToTime(String toTime) {
        mToTime = toTime;
    }

    public long getType() {
        return mType;
    }

    public void setType(long type) {
        mType = type;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public List<Long> getSpeakers() {
        return mSpeakers;
    }

    public void setSpeakers(List<Long> speakers) {
        mSpeakers = speakers;
    }

    public long getTrack() {
        return mTrack;
    }

    public void setTrack(long track) {
        mTrack = track;
    }

    public long getExperienceLevel() {
        return mExperienceLevel;
    }

    public void setExperienceLevel(long experienceLevel) {
        mExperienceLevel = experienceLevel;
    }

    public String getPlace() {
        return mPlace;
    }

    public void setPlace(String place) {
        mPlace = place;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public TimeRange getTimeRange() {
        return mTimeRange;
    }

    public void setTimeRange(TimeRange timeRange) {
        mTimeRange = timeRange;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public int getEventClass() {
        return mEventClass;
    }

    public void setEventClass(int eventClass) {
        mEventClass = eventClass;
    }

    public boolean isFavorite() {
        return mIsFavorite;
    }

    public void setFavorite(boolean isFavorite) {
        mIsFavorite = isFavorite;
    }

    public double getOrder() {
        return mOrder;
    }

    public boolean isDeleted() {
        return mDeleted;
    }

    public String getLink() {
        return mLink;
    }

    public void setLink(String mLink) {
        this.mLink = mLink;
    }

    public long getFromMillis() {
        return fromMillis;
    }

    public long getToMillis() {
        return toMillis;
    }

    @Override
    public int compareTo(@NotNull Event event) {
        int result = mFromTime.compareTo(event.getFromTime());
        if (result == 0) {
            if (mOrder == event.mOrder) {
                result = 0;
            } else if (mOrder > event.mOrder) {
                result = 1;
            } else {
                result = -1;
            }
        }

        return result;
    }

    public static class Holder {

        @SerializedName("days")
        private List<Day> mDays = new ArrayList<Day>();

        public void setDays(List<Day> mDays) {
            this.mDays = mDays;
        }

        public List<Day> getDays() {
            return mDays;
        }
    }

    public static class Day {

        @SerializedName("date")
        private String date;

        @SerializedName("programEvents")
        private List<Event> mProgramEvents = new ArrayList<Event>();

        @SerializedName("bofsEvents")
        private List<Event> mBofsEvents = new ArrayList<Event>();

        @SerializedName("events") // perhaps this field will be renamed to "socialEvents" in future
        private List<Event> mSocialsEvents = new ArrayList<Event>();

        public void setDate(String date) {
            this.date = date;
        }

        public String getDate() {
            return date;
        }

        public void setEvents(List<Event> mProgramEvents) {
            this.mProgramEvents = mProgramEvents;
        }

        public List<Event> getEvents() {
            List<Event> result = new ArrayList<Event>();
            result.addAll(mProgramEvents);
            result.addAll(mBofsEvents);
            result.addAll(mSocialsEvents);
            return result;
        }
    }
}
