package cn.foxluo.likepicture;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import cn.bingoogolapple.photopicker.activity.BGAPhotoPreviewActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class YunPhotosActivity extends AppCompatActivity {
    SwipeRefreshLayout mMainRefresh;
    private ArrayList<PhotoBean> allPhotos = new ArrayList<>();
    private boolean isRefresh = false;
    TextView name;
    private ArrayList<ArrayList<PhotoBean>> dateGroupPhotos = new ArrayList<>();
    private ArrayList<PhotoRequestBean.DataBean.ListBean> requestPhotos = new ArrayList<>();
    int size = 50;
    int scrollY = 0;
    int groupId;
    boolean onlineGroups;
    private RecyclerView recyclerView;
    private PhotoGroupsAdapter adapter;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;
    private ProgressBar progressBar;
    boolean onUpload = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(getResources().getColor(R.color.white));
        }
        setContentView(R.layout.activity_yun_photos);
        name = findViewById(R.id.textView);
        recyclerView = findViewById(R.id.groups);
        progressBar = findViewById(R.id.progressBar);
        groupId = getIntent().getIntExtra("groupId", 1);
        name.setText(getIntent().getStringExtra("name"));
        if (groupId == 1) {
            onlineGroups = true;
            findViewById(R.id.add).setVisibility(View.VISIBLE);
        } else {
            onlineGroups = false;
        }
        adapter = new PhotoGroupsAdapter(dateGroupPhotos, this, new PhotoGroupsAdapter.OnPhotoClickListener() {
            @Override
            public void onPhotoClick(int groupPosition, int photoPosition) {
                BGAPhotoPreviewActivity.IntentBuilder photoPreviewIntentBuilder = new BGAPhotoPreviewActivity.IntentBuilder(YunPhotosActivity.this)
                        .saveImgDir(null); // 保存图片的目录，如果传 null，则没有保存图片功能
                if (allPhotos.size() == 1) {
                    // 预览单张图片
                    photoPreviewIntentBuilder.previewPhoto(dateGroupPhotos.get(0).get(0).getPath());
                } else if (allPhotos.size() > 1) {
                    ArrayList<String> showPhotoUrls = new ArrayList<>();
                    for (int i = 0; i < dateGroupPhotos.size(); i++) {
                        ArrayList<PhotoBean> photoBeans = dateGroupPhotos.get(i);
                        for (PhotoBean photoBean : photoBeans) {
                            showPhotoUrls.add(photoBean.getPath());
                        }
                        if (i < groupPosition) {
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
                Vibrator vibrator = (Vibrator) YunPhotosActivity.this.getSystemService(YunPhotosActivity.this.VIBRATOR_SERVICE);
                vibrator.vibrate(100);
                Intent intent = new Intent(YunPhotosActivity.this, EditActivity.class);
                intent.putParcelableArrayListExtra("editPhotos", allPhotos);
                intent.putExtra("editPosition", (int) (scrollY * 1.01));
                intent.putExtra("groupId", groupId);
                intent.putExtra("groupPosition", groupPosition);
                intent.putExtra("photoPosition", photoPosition);
                startActivityForResult(intent, 0);
                overridePendingTransition(R.anim.anim, R.anim.anim_out);
            }
        });
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setNestedScrollingEnabled(true);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                scrollY -= dy;
            }
        });
        mMainRefresh = findViewById(R.id.fresh);
        mMainRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (onlineGroups) {
                    requestPhotos.clear();
                    request(0);
                } else {
                    getData();
                }
            }
        });
        mMainRefresh.setRefreshing(true);
        getData();
    }

    Handler handler1 = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            progressBar.setVisibility(View.GONE);
            onUpload = false;
            if (msg.what == 0) {
                Toast.makeText(YunPhotosActivity.this, "上传失败", Toast.LENGTH_SHORT).show();
            } else {
                mMainRefresh.setRefreshing(true);
                request(0);
                Toast.makeText(YunPhotosActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 10002) {
            mMainRefresh.setRefreshing(true);
            if (!onlineGroups)
                getData();
            else {
                requestPhotos.clear();
                request(0);
            }
        } else
            switch (requestCode) {
                case 10003:
                    if (data == null) {
                        return;
                    }
                    Uri uri = data.getData();
                    File file = new File(Environment.getExternalStorageDirectory() + "/Photos/uploadTemp/");
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    file = new File(Environment.getExternalStorageDirectory() + "/Photos/uploadTemp/temp.jpg");
                    if (file.exists()) {    //如果目标文件已经存在
                        file.delete();    //则删除旧文件
                    } else {
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    UCrop.of(uri, Uri.fromFile(file))
                            .start(this);
                    break;
                case UCrop.REQUEST_CROP:
                    if (resultCode == RESULT_OK) {
                        File tempFile = new File(Environment.getExternalStorageDirectory() + "/Photos/uploadTemp/temp.jpg");
                        if (!tempFile.exists()) {
                            Toast.makeText(this, "读取文件失败", Toast.LENGTH_SHORT).show();
                            handler1.sendEmptyMessage(0);
                            return;
                        }
                        progressBar.setVisibility(View.VISIBLE);
                        onUpload = true;
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... voids) {
                                RequestBody fileBody = RequestBody.create(MediaType.parse("*/*"), tempFile);
                                RequestBody requestBody = new MultipartBody.Builder()
                                        .setType(MultipartBody.FORM)
                                        .addFormDataPart("name", "&nbsp")
                                        .addFormDataPart("file", UUID.randomUUID().toString() + ".png", fileBody)
                                        .build();
                                okhttp3.Request request = new okhttp3.Request.Builder()
                                        .url("http://www.foxluo.cn/alumni_club-1.0/sql/photo/add")
                                        .post(requestBody)
                                        .build();
                                Call call = new OkHttpClient().newCall(request);
                                call.enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        Log.e("TAG", e.getMessage());
                                        handler1.sendEmptyMessage(0);
                                    }

                                    @Override
                                    public void onResponse(Call call, okhttp3.Response response) throws IOException {
                                        String body = response.body().string();
                                        Log.i("TAG", body);
                                        JSONObject result = JSON.parseObject(body);
                                        if (result.getInteger("code") == 200) {
                                            handler1.sendEmptyMessage(1);
                                        } else {
                                            handler1.sendEmptyMessage(0);
                                        }
                                    }
                                });
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                requestPhotos.clear();
                                request(0);
                            }
                        }.execute();
                    } else if (resultCode == UCrop.RESULT_ERROR) {
                        final Throwable cropError = UCrop.getError(data);
                        cropError.printStackTrace();
                        handler1.sendEmptyMessage(0);
                    }
                    break;
                default:
                    break;
            }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void getData() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                allPhotos.clear();
                dateGroupPhotos.clear();
                allPhotos.addAll(MyApplication.dao.getGroupPhotos(groupId));
                if (allPhotos.size() > 0) {
                    sort();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (allPhotos.size() > 0 || !onlineGroups) {
                    handler.sendEmptyMessage(0);
                } else if (onlineGroups) {
                    requestPhotos.clear();
                    request(0);
                }
            }
        }.execute();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            mMainRefresh.setRefreshing(false);
            adapter.notifyDataSetChanged();
        }
    };

    private void request(int page) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                "http://www.foxluo.cn/alumni_club-1.0/sql/photo/list",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        new Thread() {
                            @Override
                            public void run() {
                                PhotoRequestBean requestBean = JSONObject.parseObject(s, PhotoRequestBean.class);
                                if (requestBean.getCode() == 200) {
                                    requestPhotos.addAll(requestBean.getData().getList());
                                    if (!requestBean.getData().isHasNextPage()) {
                                        allPhotos.clear();
                                        dateGroupPhotos.clear();
                                        MyApplication.dao.cleanPhotoGroup(1);
                                        for (PhotoRequestBean.DataBean.ListBean listBean : requestBean.getData().getList()) {
                                            PhotoBean photoBean = new PhotoBean(0, null, listBean.getUrl(), 0);
                                            photoBean.setG_id(1);
                                            photoBean.setUrl_p_id(listBean.getId());
                                            photoBean.setDesc(listBean.getName());
                                            photoBean.setTime(listBean.getTime());
                                            allPhotos.add(photoBean);
                                            MyApplication.dao.insertPhoto(photoBean);
                                        }
                                        sort();
                                        handler.sendEmptyMessage(0);
                                    } else
                                        request(page + 1);
                                }
                            }
                        }.start();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        handler.sendEmptyMessage(0);
                        Toast.makeText(YunPhotosActivity.this, "获取云相册数据失败", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {  //设置头信息
                Map<String, String> map = new HashMap<String, String>();
                map.put("Content-Type", "application/x-www-form-urlencoded");
                return map;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {  //设置参数
                Map<String, String> map = new HashMap<String, String>();
                map.put("page", page + "");
                map.put("size", size + "");
                return map;
            }
        };
        RequestQueue mQueue = Volley.newRequestQueue(YunPhotosActivity.this);
        mQueue.add(request);
    }

    private void sort() {
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
    }

    public void onClick(View view) {
        if (view.getId() == R.id.back)
            finish();
        else {
            if (onUpload) {
                Toast.makeText(this, "正在上传!", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent();
            if (Build.VERSION.SDK_INT < 19) {//因为Android SDK在4.4版本后图片action变化了 所以在这里先判断一下
                intent.setAction(Intent.ACTION_GET_CONTENT);
            } else {
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            }
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, 10003);
        }
    }
}