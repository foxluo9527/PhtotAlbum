package cn.foxluo.likepicture;

import android.app.Application;

import androidx.room.Room;

import java.util.Date;

public class MyApplication extends Application {
    public static DatabaseHelper.Dao dao;
    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(){
            @Override
            public void run() {
                dao= Room.databaseBuilder(MyApplication.this, DatabaseHelper.class, "photo_groups").build().getDao();
                if (getSharedPreferences("FIRST_USE", MODE_PRIVATE).getBoolean("first_use", true)) {
                    dao.insertPhotoGroup(new PhotoGroupBean(1,"云相册","不再拘束于本地的云相册!",new Date().getTime()));
                }
            }
        }.start();
    }
}
