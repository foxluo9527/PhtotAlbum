package cn.foxluo.likepicture;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "photo")
public class PhotoBean implements Serializable, Parcelable {
    @NonNull
    @PrimaryKey(autoGenerate = true)
    private int id;
    @NonNull
    @ColumnInfo()
    private int url_p_id;
    @NonNull
    @ColumnInfo
    private int g_id;
    @ColumnInfo
    private String hashCode;
    @ColumnInfo
    private String path;
    @ColumnInfo
    private long size;
    @ColumnInfo
    private int avg;
    @ColumnInfo
    private long time;
    @ColumnInfo
    private String desc;

    public PhotoBean() {
    }

    @Ignore
    public PhotoBean(int id, String hashCode, String path, long size) {
        this.id = id;
        this.hashCode = hashCode;
        this.path = path;
        this.size = size;
    }

    public int getUrl_p_id() {
        return url_p_id;
    }

    public void setUrl_p_id(int url_p_id) {
        this.url_p_id = url_p_id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getG_id() {
        return g_id;
    }

    public void setG_id(int g_id) {
        this.g_id = g_id;
    }

    public int getId() {
        return id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getAvg() {
        return avg;
    }

    public void setAvg(int avg) {
        this.avg = avg;
    }

    public String getHashCode() {
        return hashCode;
    }

    public void setHashCode(String hashCode) {
        this.hashCode = hashCode;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public static final Parcelable.Creator<PhotoBean> CREATOR = new Parcelable.Creator<PhotoBean>() {
        @Override
        public PhotoBean createFromParcel(Parcel source) {
            return new PhotoBean(source);
        }

        @Override
        public PhotoBean[] newArray(int size) {
            return new PhotoBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public PhotoBean(Parcel source) {
        this.hashCode = source.readString();
        this.path = source.readString();
        this.size = source.readLong();
        this.avg = source.readInt();
        this.time = source.readLong();
        this.desc = source.readString();
        this.url_p_id=source.readInt();
        this.g_id=source.readInt();
        this.id=source.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.hashCode);
        dest.writeString(this.path);
        dest.writeLong(this.size);
        dest.writeInt(this.avg);
        dest.writeLong(this.time);
        dest.writeString(this.desc);
        dest.writeInt(this.url_p_id);
        dest.writeInt(this.g_id);
        dest.writeInt(this.id);
    }
}
