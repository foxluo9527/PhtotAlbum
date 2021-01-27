package cn.foxluo.likepicture;

import android.app.Application;
import android.os.Build;
import android.os.StrictMode;

import androidx.annotation.RequiresApi;
import androidx.room.Room;

import java.util.Date;

public class MyApplication extends Application {
    public static DatabaseHelper.Dao dao;
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onCreate() {
        super.onCreate();
        StrictMode.VmPolicy.Builder builder=new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
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
