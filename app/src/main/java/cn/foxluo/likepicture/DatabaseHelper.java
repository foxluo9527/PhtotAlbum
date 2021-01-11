package cn.foxluo.likepicture;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.DatabaseConfiguration;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.InvalidationTracker;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import java.util.List;

@Database(entities = {PhotoGroupBean.class, PhotoBean.class}, version = 1, exportSchema = false)
public abstract class DatabaseHelper extends RoomDatabase {

    @NonNull
    @Override
    protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration config) {
        return null;
    }

    @NonNull
    @Override
    protected InvalidationTracker createInvalidationTracker() {
        return null;
    }

    @Override
    public void clearAllTables() {

    }

    public abstract Dao getDao();

    @androidx.room.Dao
    public interface Dao {
        @Query("select * from photo order by time desc")
        List<PhotoBean> getAllPhoto();
        @Query("select * from photo where g_id=:groupId order by time desc")
        List<PhotoBean> getGroupPhotos(int groupId);
        @Query("select * from photo_group order by date asc")
        List<PhotoGroupBean> getPhotoGroups();
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void insertPhotos(List<PhotoBean> photoBeans);
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void insertPhoto(PhotoBean photoBean);
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void insertPhotoGroup(PhotoGroupBean photoGroupBean);
        @Query("select max(id) from photo_group")
        int getLastInsertGroupId();
        @Delete
        void deletePhoto(PhotoBean photoBean);
        @Query("delete from photo where url_p_id=:urlPId")
        void deleteOnlinePhoto(int urlPId);
        @Delete
        void deletePhotoGroup(PhotoGroupBean photoGroupBean);
        @Query("delete from photo where g_id=:groupId")
        void cleanPhotoGroup(int groupId);
        @Query("delete from photo_group where id=:groupId")
        void deletePhotoGroup(int groupId);
    }

}
