package cn.foxluo.likepicture;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import cn.bingoogolapple.photopicker.activity.BGAPhotoPreviewActivity;

import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_SETTLING;

public class HomeActivity extends AppCompatActivity {
    public static ArrayList<PhotoBean> allPhotos = new ArrayList<>();
    private Thread hashThread;
    private RecyclerView photos;
    HomePhotoViewAdapter adapter;
    int scrollY = 0;
    boolean scrollState = false;
    StaggeredGridLayoutManager staggeredGridLayoutManager;
    private ArrayList<ArrayList<PhotoBean>> dateGroupPhotos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(getResources().getColor(R.color.white));
        }
        setContentView(R.layout.activity_home);
        photos = findViewById(R.id.home_photos);
        adapter = new HomePhotoViewAdapter(dateGroupPhotos, this, new HomePhotoViewAdapter.OnPhotoClickListener() {
            @Override
            public void onPhotoClick(int groupIndex, int photoPosition) {
                BGAPhotoPreviewActivity.IntentBuilder photoPreviewIntentBuilder = new BGAPhotoPreviewActivity.IntentBuilder(HomeActivity.this)
                        .saveImgDir(null); // 保存图片的目录，如果传 null，则没有保存图片功能
                if (dateGroupPhotos.size() == 1) {
                    // 预览单张图片
                    photoPreviewIntentBuilder.previewPhoto(dateGroupPhotos.get(0).get(0).getPath());
                } else if (dateGroupPhotos.size() > 1) {
                    ArrayList<String> showPhotoUrls = new ArrayList<>();
                    for (int i = 0; i < dateGroupPhotos.size(); i++) {
                        ArrayList<PhotoBean> photoBeans = dateGroupPhotos.get(i);
                        for (PhotoBean photoBean : photoBeans) {
                            showPhotoUrls.add(photoBean.getPath());
                        }
                        if (i < groupIndex) {
                            photoPosition += photoBeans.size();
                        }
                    }
                    // 预览多张图片
                    photoPreviewIntentBuilder.previewPhotos(showPhotoUrls)
                            .currentPosition(photoPosition); // 当前预览图片的索引
                }
                startActivityForResult(photoPreviewIntentBuilder.build(), -1);
            }

            @Override
            public void onPhotoLongClick(int groupPosition, int photoPosition) {
                Vibrator vibrator = (Vibrator) HomeActivity.this.getSystemService(HomeActivity.this.VIBRATOR_SERVICE);
                vibrator.vibrate(100);
                Intent intent = new Intent(HomeActivity.this, EditActivity.class);
                intent.putParcelableArrayListExtra("editPhotos", allPhotos);
                int headerHeight = dip2px(HomeActivity.this, 80);
                if (scrollY >= (-headerHeight)) {
                    intent.putExtra("editPosition", 0);
                } else {
                    intent.putExtra("editPosition", (int) ((scrollY + headerHeight) * 1.01));
                }
                intent.putExtra("groupPosition", groupPosition);
                intent.putExtra("photoPosition", photoPosition);
                startActivityForResult(intent,0);
                overridePendingTransition(0,0);
            }
        });
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        photos.setNestedScrollingEnabled(true);
        photos.setLayoutManager(staggeredGridLayoutManager);
        photos.setAdapter(adapter);
        photos.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                scrollY -= dy;
                if (scrollY == 0) {
                    findViewById(R.id.top).setVisibility(View.GONE);
                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == 0) {
                    scrollState = false;
                    new Handler() {
                        @Override
                        public void handleMessage(@NonNull Message msg) {
                            super.handleMessage(msg);
                        }
                    }.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!scrollState)
                                findViewById(R.id.top).setVisibility(View.GONE);
                        }
                    }, 1000);
                } else {
                    scrollState = true;
                    findViewById(R.id.top).setVisibility(View.VISIBLE);
                }
            }
        });
        Uri internalUri = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
        Uri externalUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        PhotoChangeObserver observer = new PhotoChangeObserver(new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (isPRM()) {
                    getAllPhotos();
                }
            }
        });
        getContentResolver().registerContentObserver(internalUri, false, observer);
        getContentResolver().registerContentObserver(externalUri, false, observer);
        if (getSharedPreferences("FIRST_USE", MODE_PRIVATE).getBoolean("first_use", true)) {
            if (isPRM()) {
                getAllPhotos();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
            getSharedPreferences("FIRST_USE", MODE_PRIVATE).edit().putBoolean("first_use", false).apply();
            loadCloudPhotos(0);
        } else if (allPhotos.size() == 0) {
            if (isPRM()) {
                getAllPhotos();
            } else {
                Toast.makeText(this, "由于您拒绝了应用权限，将无法正常使用软件功能", Toast.LENGTH_SHORT).show();
            }
        } else {
            sort();
        }
    }

    ArrayList<PhotoRequestBean.DataBean.ListBean> requestPhotos = new ArrayList<>();

    private void loadCloudPhotos(int page) {
        int size = 50;
        StringRequest request = new StringRequest(
                Request.Method.POST,
                "http://www.foxluo.cn/alumni_club-1.0/sql/photo/list",
                s -> new Thread() {
                    @Override
                    public void run() {
                        PhotoRequestBean requestBean = JSONObject.parseObject(s, PhotoRequestBean.class);
                        if (requestBean.getCode() == 200) {
                            requestPhotos.addAll(requestBean.getData().getList());
                            if (!requestBean.getData().isHasNextPage()) {
                                MyApplication.dao.cleanPhotoGroup(1);
                                for (PhotoRequestBean.DataBean.ListBean listBean : requestBean.getData().getList()) {
                                    PhotoBean photoBean = new PhotoBean(0, null, listBean.getUrl(), 0);
                                    photoBean.setG_id(1);
                                    photoBean.setUrl_p_id(listBean.getId());
                                    photoBean.setDesc(listBean.getName());
                                    photoBean.setTime(listBean.getTime());
                                    MyApplication.dao.insertPhoto(photoBean);
                                }
                            } else
                                loadCloudPhotos(page + 1);
                        }
                    }
                }.start(),
                volleyError -> Toast.makeText(HomeActivity.this, "获取云相册数据失败", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {  //设置头信息
                Map<String, String> map = new HashMap<String, String>();
                map.put("Content-Type", "application/x-www-form-urlencoded");
                return map;
            }

            @Override
            protected Map<String, String> getParams() {  //设置参数
                Map<String, String> map = new HashMap<String, String>();
                map.put("page", page + "");
                map.put("size", size + "");
                return map;
            }
        };
        RequestQueue mQueue = Volley.newRequestQueue(HomeActivity.this);
        mQueue.add(request);
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (isPRM()) {
            getAllPhotos();
        } else {
            Toast.makeText(this, "请打开文件读写权限以正常使用软件功能", Toast.LENGTH_SHORT).show();
            getPRM();
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            adapter.notifyDataSetChanged();
        }
    };

    private boolean isPRM() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void getPRM() {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", getPackageName());
        }
        startActivityForResult(localIntent, 10001);
    }

    private void sort() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                for (int i = 0; i < allPhotos.size(); i++) {
                    if (i == 0) {
                        dateGroupPhotos.add(new ArrayList<PhotoBean>());
                        dateGroupPhotos.get(i).add(allPhotos.get(i));
                    } else {
                        long date = allPhotos.get(i).getTime();
                        boolean flag = false;
                        for (int j = 0; j < dateGroupPhotos.size(); j++) {
                            ArrayList<PhotoBean> datePhotos = dateGroupPhotos.get(j);
                            if (TimeUtil.isSameDay(datePhotos.get(0).getTime(), date)) {
                                datePhotos.add(allPhotos.get(i));
                                flag = true;
                                break;
                            }
                        }
                        if (!flag) {
                            ArrayList<PhotoBean> photos = new ArrayList<>();
                            photos.add(allPhotos.get(i));
                            dateGroupPhotos.add(photos);
                        }
                    }
                }
                Collections.sort(dateGroupPhotos, (o1, o2) ->
                        {
                            if (o1.get(0) == null && o2.get(0) == null) {
                                return 0;
                            }
                            if (o1.get(0) == null) {
                                return -1;
                            }
                            if (o2.get(0) == null) {
                                return 1;
                            }
                            if (o1.get(0).getTime() > o2.get(0).getTime()) {
                                return -1;
                            }
                            if (o2.get(0).getTime() > o1.get(0).getTime()) {
                                return 1;
                            }
                            return 0;
                        }
                );
                for (ArrayList<PhotoBean> photos : dateGroupPhotos) {
                    Collections.sort(photos, (o1, o2) ->
                            {
                                if (o1 == null && o2 == null) {
                                    return 0;
                                }
                                if (o1 == null) {
                                    return -1;
                                }
                                if (o2 == null) {
                                    return 1;
                                }
                                if (o1.getTime() > o2.getTime()) {
                                    return -1;
                                }
                                if (o2.getTime() > o1.getTime()) {
                                    return 1;
                                }
                                return 0;
                            }
                    );
                }
                handler.sendEmptyMessage(0);
            }
        }.start();
    }

    private void getAllPhotos() {
        new Thread() {
            @Override
            public void run() {
                try {
                    Uri uri = MediaStore.Images.Media.getContentUri("external");
                    ContentResolver cr = getContentResolver();
                    if (cr != null) {
                        Cursor cursor = cr.query(uri, null, null, null, null);
                        if (null == cursor) {
                            return;
                        }
                        if (cursor.moveToFirst()) {
                            allPhotos.clear();
                            dateGroupPhotos.clear();
                            if (hashThread != null) {
                                try {
                                    hashThread.interrupt();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                hashThread = null;
                            }
                            do {
                                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                                long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.SIZE));
                                int id = 0;
                                PhotoBean photo = new PhotoBean(id, null, path, size);
                                File file = new File(photo.getPath());
                                long time = file.lastModified();
                                photo.setTime(time);
                                allPhotos.add(photo);
                            } while (cursor.moveToNext());
                            sort();
                            hashThread = getPhotosHash();
                            hashThread.start();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (data.getBooleanExtra("dataChanged", false)) {
                getAllPhotos();
            }
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.clean:
                if (isPRM()) {
                    startActivity(new Intent(this, RepeatActivity.class));
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                }
                break;
            case R.id.similar:
                if (isPRM()) {
                    startActivity(new Intent(this, SimilarActivity.class));
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                }
                break;
            case R.id.photos:
                if (isPRM()) {
                    startActivity(new Intent(this, PhotoGroupsActivity.class));
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                }
                break;
            case R.id.top:
                photos.stopScroll();
                scrollY = 0;
                staggeredGridLayoutManager.scrollToPosition(0);
                findViewById(R.id.top).setVisibility(View.GONE);
                break;
        }
    }

    private Thread getPhotosHash() {
        return new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    for (PhotoBean photoBean : allPhotos) {
                        if (TextUtils.isEmpty(photoBean.getHashCode()))
                            photoBean.setHashCode(ImageHelper.getHashCode(photoBean));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    class PhotoChangeObserver extends ContentObserver {
        Handler handler;

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public PhotoChangeObserver(Handler handler) {
            super(handler);
            this.handler = handler;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            handler.sendEmptyMessage(0);
        }
    }
}