package cn.foxluo.likepicture;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class EditActivity extends AppCompatActivity {
    private ArrayList<ArrayList<PhotoBean>> dateGroupPhotos = new ArrayList<>();
    private ArrayList<PhotoBean> editPhotos = new ArrayList<>();
    private ArrayList<ArrayList<Boolean>> photosCheck = new ArrayList<>();
    private TextView choice;
    private boolean allChoice;
    private TextView all_choice;
    private EditPhotosAdapter adapter;
    private View gone;
    private RecyclerView edit_photos;
    boolean onlineGroups;
    int groupId;
    int position;
    int groupPosition;
    int photoPosition;
    boolean deleted = false;
    private static Context context;
    private boolean onLoading = false;
    private boolean loadingEnable = false;
    private ProgressDialog loadingDialog;
    AsyncTask task = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(getResources().getColor(R.color.white));
        }
        setContentView(R.layout.activity_edit);
        context = this;
        choice = findViewById(R.id.choice);
        all_choice = findViewById(R.id.all_choice);
        gone = findViewById(R.id.gone);
        edit_photos = findViewById(R.id.edit_photos);
        editPhotos.addAll(getIntent().getParcelableArrayListExtra("editPhotos"));
        position = getIntent().getIntExtra("editPosition", 0);
        groupPosition = getIntent().getIntExtra("groupPosition", 0);
        photoPosition = getIntent().getIntExtra("photoPosition", 0);
        groupId = getIntent().getIntExtra("groupId", 0);
        if (groupId == 1) {
            onlineGroups = true;
        }
        adapter = new EditPhotosAdapter(dateGroupPhotos, photosCheck, EditActivity.this, new EditPhotosAdapter.OnGroupPhotosClickListener() {
            @Override
            public void photoCheckChanged() {
                checkChanged();
            }
        });
        edit_photos.setLayoutManager(new LinearLayoutManager(EditActivity.this));
        edit_photos.setAdapter(adapter);
        getData();
        loadingDialog = new ProgressDialog(this);
        loadingDialog.setCancelable(false);
    }
    public void showLoading(boolean enable, String message) {
        if (!onLoading) {
            this.loadingEnable = enable;
            loadingDialog.setCanceledOnTouchOutside(loadingEnable);
            loadingDialog.show();
            loadingDialog.setMessage(message);
            onLoading = true;
        }
    }

    public void dismissLoading() {
        loadingDialog.dismiss();
        onLoading = false;
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            adapter.notifyDataSetChanged();
            LinearLayoutManager mLayoutManager =
                    (LinearLayoutManager) edit_photos.getLayoutManager();
            mLayoutManager.scrollToPositionWithOffset(0, position);
            checkChanged();
        }
    };

    private void getData() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                dateGroupPhotos.clear();
                photosCheck.clear();
                for (int i = 0; i < editPhotos.size(); i++) {
                    if (i == 0) {
                        dateGroupPhotos.add(new ArrayList<>());
                        dateGroupPhotos.get(i).add(editPhotos.get(i));
                    } else {
                        long date = editPhotos.get(i).getTime();
                        boolean flag = false;
                        for (int j = 0; j < dateGroupPhotos.size(); j++) {
                            ArrayList<PhotoBean> datePhotos = dateGroupPhotos.get(j);
                            if (TimeUtil.isSameDay(datePhotos.get(0).getTime(), date)) {
                                datePhotos.add(editPhotos.get(i));
                                flag = true;
                                break;
                            }
                        }
                        if (!flag) {
                            ArrayList<PhotoBean> photos = new ArrayList<>();
                            photos.add(editPhotos.get(i));
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
                for (int i = 0; i < dateGroupPhotos.size(); i++) {
                    ArrayList<PhotoBean> photoBeans = dateGroupPhotos.get(i);
                    ArrayList<Boolean> checks = new ArrayList<>();
                    for (int j = 0; j < photoBeans.size(); j++) {
                        if (groupPosition == i && photoPosition == j && !deleted)
                            checks.add(true);
                        else
                            checks.add(false);
                    }
                    photosCheck.add(checks);
                }
                handler.sendEmptyMessage(0);
            }
        }.start();
    }

    private void checkChanged() {
        int num = 0;
        boolean flag = false;
        for (ArrayList<Boolean> checks : photosCheck) {
            for (Boolean b : checks) {
                if (b) {
                    num++;
                } else {
                    flag = true;
                    if (allChoice) {
                        allChoice = false;
                    }
                }
            }
        }
        if (!flag) {
            allChoice = true;
        }
        if (allChoice) {
            all_choice.setText("全不选");
        } else {
            all_choice.setText("全选");
        }
        if (num > 0) {
            choice.setText("已选择" + num + "项");
            gone.setVisibility(View.GONE);
        } else {
            choice.setText("选择项目");
            gone.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0,0);
    }

    public static void addToGroups(Context context, final ArrayList<PhotoGroupBean> photoGroupBeans, final ArrayList<PhotoBean> photoBeans, ArrayList<PhotoBean> insertPhotos) {
        final View popView = View.inflate(context, R.layout.add_groups_view, null);
        View create = popView.findViewById(R.id.create);
        View cancel = popView.findViewById(R.id.cancel);
        View upload = popView.findViewById(R.id.upload_cloud);
        //获取屏幕宽高
        int weight = context.getResources().getDisplayMetrics().widthPixels * 9 / 10;
        int height = context.getResources().getDisplayMetrics().heightPixels / 2;
        PopupWindow popupWindow = new PopupWindow(popView, weight, height);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setFocusable(true);
        //点击外部popueWindow消失
        popupWindow.setOutsideTouchable(false);
        popupWindow.update();
        //popupWindow消失屏幕变为不透明
        popupWindow.setOnDismissListener(() -> {
            WindowManager.LayoutParams lp = ((AppCompatActivity) context).getWindow().getAttributes();
            lp.alpha = 1.0f;
            ((AppCompatActivity) context).getWindow().setAttributes(lp);
        });
        RecyclerView listView = popView.findViewById(R.id.groups);
        ChoiceGroupsAdapter adapter = new ChoiceGroupsAdapter(photoBeans, photoGroupBeans, photoPosition -> new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                for (PhotoBean photoBean : insertPhotos) {
                    photoBean.setG_id(photoGroupBeans.get(photoPosition).getId());
                    MyApplication.dao.insertPhoto(photoBean);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                Toast.makeText(context, "加入相册" + photoGroupBeans.get(photoPosition).getName() + "成功!"
                        , Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
                ((AppCompatActivity) context).finish();
            }
        }.execute(), context);
        listView.setLayoutManager(new GridLayoutManager(context, 1));
        listView.setAdapter(adapter);
        create.setOnClickListener(view -> PhotoGroupsActivity.createGroup(context, new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                int groupId = msg.arg1;
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        for (PhotoBean photoBean : insertPhotos) {
                            photoBean.setG_id(groupId);
                            MyApplication.dao.insertPhoto(photoBean);
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        Toast.makeText(context, "加入相册成功!", Toast.LENGTH_SHORT).show();
                        popupWindow.dismiss();
                        ((AppCompatActivity) context).finish();
                    }
                }.execute();
            }
        }));
        cancel.setOnClickListener(view -> popupWindow.dismiss());
        //popupWindow出现屏幕变为半透明
        WindowManager.LayoutParams lp = ((AppCompatActivity) context).getWindow().getAttributes();
        lp.alpha = 0.5f;
        ((AppCompatActivity) context).getWindow().setAttributes(lp);
        popupWindow.showAtLocation(popView, Gravity.BOTTOM, 0, (context.getResources().getDisplayMetrics().heightPixels - height) / 2);
    }

    int deleteNum = 0;
    int deleteSuccessNum = 0;
    int num = 0;
    ArrayList<PhotoBean> groupFirstPhotos = new ArrayList<>();

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cancel:
                finish();
                overridePendingTransition(0,0);
                break;
            case R.id.all_choice:
                allChoice = !allChoice;
                for (ArrayList<Boolean> checks : photosCheck) {
                    for (int i = 0; i < checks.size(); i++) {
                        checks.set(i, allChoice);
                    }
                }
                checkChanged();
                adapter.notifyDataSetChanged();
                break;
            case R.id.share:
                ArrayList<PhotoBean> sharePhotos = new ArrayList<>();
                for (int j = 0; j < photosCheck.size(); j++) {
                    ArrayList<Boolean> checks = photosCheck.get(j);
                    ArrayList<PhotoBean> photoBeans = dateGroupPhotos.get(j);
                    for (int i = 0; i < checks.size(); i++) {
                        Boolean b = checks.get(i);
                        if (b) {
                            num++;
                            photoBeans.get(i).setTime(new Date().getTime());
                            sharePhotos.add(photoBeans.get(i));
                        }
                    }
                }
                if (num > 0) {
                    Intent share = new Intent();
                    String cachePath = context.getExternalCacheDir().getAbsolutePath();
                    if (!onlineGroups){
                        ArrayList<Uri> uris = new ArrayList<>();
                        for (PhotoBean photoBean : sharePhotos) {
                            @SuppressLint("SimpleDateFormat") File file = new File(cachePath + File.separator + "cache" + new SimpleDateFormat("yyyyMMddHHmm").format(System.currentTimeMillis()) + UUID.randomUUID().toString().substring(0, 6) + ".jpeg");
                            if (!file.exists()) {
                                try {
                                    File dir = new File(file.getParent());
                                    dir.mkdir();
                                    file.createNewFile();
                                    OutputStream ops = new FileOutputStream(file);
                                    Bitmap bitmap = BitmapFactory.decodeFile(photoBean.getPath());
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ops);
                                    ops.close();
                                    bitmap.recycle();
                                    uris.add(Uri.fromFile(file));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        share.setAction(Intent.ACTION_SEND_MULTIPLE);
                        share.setType("image/*");
                        share.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                        context.startActivity(Intent.createChooser(share, sharePhotos.size() + "张图片"));
                    }else {
                        showLoading(false,"获取网络图片中");
                        try {
                            task=new AsyncTask<Void,Void,ArrayList<Uri>>(){
                                @Override
                                protected ArrayList<Uri> doInBackground(Void... voids) {
                                    ArrayList<Uri> uris=new ArrayList<>();
                                    for (PhotoBean photoBean : sharePhotos) {
                                        try {
                                            Bitmap bitmap=Glide.with(EditActivity.context).asBitmap().load(photoBean.getPath()).submit().get();
                                            @SuppressLint("SimpleDateFormat") File file = new File(cachePath + File.separator + "cache" + new SimpleDateFormat("yyyyMMddHHmm").format(System.currentTimeMillis()) + UUID.randomUUID().toString().substring(0, 6) + ".jpeg");
                                            if (!file.exists()) {
                                                try {
                                                    File dir = new File(file.getParent());
                                                    dir.mkdir();
                                                    file.createNewFile();
                                                    OutputStream ops = new FileOutputStream(file);
                                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ops);
                                                    ops.close();
                                                    uris.add(Uri.fromFile(file));
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                    task.cancel(true);
                                                }
                                            }
                                        } catch (ExecutionException e) {
                                            e.printStackTrace();
                                            task.cancel(true);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                            task.cancel(true);
                                        }
                                    }
                                    return uris;
                                }
                                @Override
                                protected void onPostExecute(ArrayList<Uri> uris) {
                                    dismissLoading();
                                    share.setAction(Intent.ACTION_SEND_MULTIPLE);
                                    share.setType("image/*");
                                    share.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                                    context.startActivity(Intent.createChooser(share, sharePhotos.size() + "张图片"));
                                }
                            }.execute();
                        }catch (Exception e){
                            dismissLoading();
                            task.cancel(true);
                        }
                    }
                }
                break;
            case R.id.add:
                num = 0;
                ArrayList<PhotoBean> insertPhotos = new ArrayList<>();
                for (int j = 0; j < photosCheck.size(); j++) {
                    ArrayList<Boolean> checks = photosCheck.get(j);
                    ArrayList<PhotoBean> photoBeans = dateGroupPhotos.get(j);
                    for (int i = 0; i < checks.size(); i++) {
                        Boolean b = checks.get(i);
                        if (b) {
                            num++;
                            photoBeans.get(i).setTime(new Date().getTime());
                            insertPhotos.add(photoBeans.get(i));
                        }
                    }
                }
                if (num > 0) {
                    if (onlineGroups) {
                        AlertDialog dialog = new AlertDialog.Builder(this)
                                .setTitle("提示")
                                .setMessage("确认下载所选的" + num + "张图片?")
                                .setNegativeButton("取消", null)
                                .setPositiveButton("确定", (dialog12, which) -> {

                                }).create();
                        dialog.show();
                    } else
                        new AsyncTask<Void, Void, ArrayList<PhotoGroupBean>>() {
                            @Override
                            protected ArrayList<PhotoGroupBean> doInBackground(Void... voids) {
                                ArrayList<PhotoGroupBean> photoGroupBeans = new ArrayList<>();
                                photoGroupBeans.addAll(MyApplication.dao.getPhotoGroups());
                                groupFirstPhotos.clear();
                                for (int i = 0; i < photoGroupBeans.size(); i++) {
                                    PhotoGroupBean photoGroupBean = photoGroupBeans.get(i);
                                    if (photoGroupBean.getId() == 1 || photoGroupBean.getId() == groupId) {
                                        photoGroupBeans.remove(i);
                                        i--;
                                        continue;
                                    }
                                    ArrayList<PhotoBean> photoBeans = (ArrayList<PhotoBean>) MyApplication.dao.getGroupPhotos(photoGroupBean.getId());
                                    if (photoBeans.size() > 0)
                                        groupFirstPhotos.add(photoBeans.get(0));
                                    else
                                        groupFirstPhotos.add(new PhotoBean(0, null, null, 0));
                                    photoGroupBean.setNum(photoBeans.size());
                                }
                                return photoGroupBeans;
                            }

                            @Override
                            protected void onPostExecute(ArrayList<PhotoGroupBean> photoGroupBeans) {
                                if (num > 0) {
                                    setResult(10002);
                                    addToGroups(EditActivity.this, photoGroupBeans, groupFirstPhotos, insertPhotos);
                                }
                            }
                        }.execute();
                }
                break;
            case R.id.del:
                num = 0;
                ArrayList<PhotoBean> deletePhotos = new ArrayList<>();
                for (int j = 0; j < photosCheck.size(); j++) {
                    ArrayList<Boolean> checks = photosCheck.get(j);
                    ArrayList<PhotoBean> photoBeans = dateGroupPhotos.get(j);
                    for (int i = 0; i < checks.size(); i++) {
                        Boolean b = checks.get(i);
                        if (b) {
                            num++;
                            deletePhotos.add(photoBeans.get(i));
                        }
                    }
                }
                if (num <= 0) {
                    break;
                }
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("提示")
                        .setMessage("确认删除所选的" + num + "张图片?")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确定", (dialog1, which) -> {
                            deleteNum = 0;
                            deleteSuccessNum = 0;
                            for (PhotoBean photoBean : deletePhotos) {
                                if (onlineGroups) {
                                    int id = photoBean.getUrl_p_id();
                                    if (id > 0) {
                                        StringRequest request = new StringRequest(
                                                Request.Method.POST,
                                                "http://www.foxluo.cn/alumni_club-1.0/sql/photo/delete",
                                                s -> {
                                                    new Thread() {
                                                        @Override
                                                        public void run() {
                                                            MyApplication.dao.deleteOnlinePhoto(id);
                                                        }
                                                    }.start();
                                                    deleted = true;
                                                    setResult(10002);
                                                    for (int i = 0; i < editPhotos.size(); i++) {
                                                        PhotoBean photoBean1 = editPhotos.get(i);
                                                        if (photoBean1.getUrl_p_id() == id) {
                                                            editPhotos.remove(i);
                                                            getData();
                                                            break;
                                                        }
                                                    }
                                                    deleteNum++;
                                                    deleteSuccessNum++;
                                                    Intent intent = new Intent();
                                                    intent.putExtra("dataChanged", true);
                                                    setResult(10002, intent);
                                                    if (deleteNum == num) {
                                                        if (deleteSuccessNum == deleteNum) {
                                                            Toast.makeText(EditActivity.this, deleteSuccessNum + "张图片删除成功", Toast.LENGTH_SHORT).show();
                                                        } else
                                                            Toast.makeText(EditActivity.this, deleteSuccessNum + "张图片删除成功，" + (num - deleteSuccessNum) + "张删除失败", Toast.LENGTH_SHORT).show();
                                                    }
                                                },
                                                volleyError -> {
                                                    deleteNum++;
                                                    if (deleteNum == num) {
                                                        Toast.makeText(EditActivity.this, deleteNum + "张图片删除失败", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                        ) {
                                            @Override
                                            public Map<String, String> getHeaders() {  //设置头信息
                                                Map<String, String> map = new HashMap<>();
                                                map.put("Content-Type", "application/x-www-form-urlencoded");
                                                return map;
                                            }

                                            @Override
                                            protected Map<String, String> getParams() {  //设置参数
                                                Map<String, String> map = new HashMap<>();
                                                map.put("id", id + "");
                                                return map;
                                            }
                                        };
                                        RequestQueue mQueue = Volley.newRequestQueue(EditActivity.this);
                                        mQueue.add(request);
                                    }
                                } else if (groupId==0){
                                    try {
                                        deleteNum++;
                                        File file = new File(photoBean.getPath());
                                        file.delete();
                                        deleted = true;
                                        deleteSuccessNum++;
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    } finally {
                                        if (deleteNum == num) {
                                            if (deleteSuccessNum == deleteNum) {
                                                Toast.makeText(EditActivity.this, deleteSuccessNum + "张图片删除成功", Toast.LENGTH_SHORT).show();
                                            } else
                                                Toast.makeText(EditActivity.this, deleteSuccessNum + "张图片删除成功，" + (num - deleteSuccessNum) + "张删除失败", Toast.LENGTH_SHORT).show();
                                            editPhotos.remove(photoBean);
                                            getData();
                                            Intent intent = new Intent();
                                            intent.putExtra("dataChanged", true);
                                            setResult(10002, intent);
                                        }
                                    }
                                }else {
                                    new AsyncTask<Void,Void,Void>(){
                                        @Override
                                        protected Void doInBackground(Void... voids) {
                                            MyApplication.dao.deletePhoto(photoBean);
                                            return null;
                                        }

                                        @Override
                                        protected void onPostExecute(Void aVoid) {
                                            editPhotos.remove(photoBean);
                                            getData();
                                            Intent intent = new Intent();
                                            intent.putExtra("dataChanged", true);
                                            setResult(10002, intent);
                                        }
                                    }.execute();
                                }
                            }
                        }).create();
                dialog.show();
                break;
        }
    }
}