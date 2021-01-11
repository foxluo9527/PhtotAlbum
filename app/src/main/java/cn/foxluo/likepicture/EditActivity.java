package cn.foxluo.likepicture;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
        choice = findViewById(R.id.choice);
        all_choice = findViewById(R.id.all_choice);
        gone = findViewById(R.id.gone);
        edit_photos = findViewById(R.id.edit_photos);
        editPhotos.addAll(getIntent().getParcelableArrayListExtra("editPhotos"));
        position = getIntent().getIntExtra("editPosition", 0);
        groupPosition = getIntent().getIntExtra("groupPosition", 0);
        photoPosition = getIntent().getIntExtra("photoPosition", 0);
        groupId = getIntent().getIntExtra("groupId", 0);
        if (groupId==1){
            onlineGroups=true;
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
                        dateGroupPhotos.add(new ArrayList<PhotoBean>());
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
        overridePendingTransition(R.anim.anim_out, R.anim.anim);
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
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = ((AppCompatActivity) context).getWindow().getAttributes();
                lp.alpha = 1.0f;
                ((AppCompatActivity) context).getWindow().setAttributes(lp);
            }
        });
        RecyclerView listView = popView.findViewById(R.id.groups);
        ChoiceGroupsAdapter adapter = new ChoiceGroupsAdapter(photoBeans, photoGroupBeans, new ChoiceGroupsAdapter.OnPhotoClickListener() {
            @Override
            public void itemClick(int photoPosition) {
                new AsyncTask<Void, Void, Void>() {
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
                    }
                }.execute();
            }
        }, context);
        listView.setLayoutManager(new GridLayoutManager(context, 1));
        listView.setAdapter(adapter);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PhotoGroupsActivity.createGroup(context, new Handler() {
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
                            }
                        }.execute();
                    }
                });
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
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
                overridePendingTransition(R.anim.anim_out, R.anim.anim);
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
            case R.id.add:
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
                if (onlineGroups) {
                    if (num > 0) {
                        AlertDialog dialog = new AlertDialog.Builder(this)
                                .setTitle("提示")
                                .setMessage("确认下载所选的" + num + "张图片?")
                                .setNegativeButton("取消", null)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }).create();
                        dialog.show();
                    }
                } else
                    new AsyncTask<Void, Void, ArrayList<PhotoGroupBean>>() {
                        @Override
                        protected ArrayList<PhotoGroupBean> doInBackground(Void... voids) {
                            ArrayList<PhotoGroupBean> photoGroupBeans = new ArrayList<>();
                            photoGroupBeans.addAll((ArrayList<PhotoGroupBean>) MyApplication.dao.getPhotoGroups());
                            groupFirstPhotos.clear();
                            for (int i = 0; i < photoGroupBeans.size(); i++) {
                                PhotoGroupBean photoGroupBean=photoGroupBeans.get(i);
                                if (photoGroupBean.getId() == 1||photoGroupBean.getId()==groupId){
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
                                addToGroups(EditActivity.this, photoGroupBeans, groupFirstPhotos, insertPhotos);
                            }
                        }
                    }.execute();
                break;
            case R.id.del:
                num = 0;
                ArrayList<Integer> deleteIds = new ArrayList<>();
                for (int j = 0; j < photosCheck.size(); j++) {
                    ArrayList<Boolean> checks = photosCheck.get(j);
                    ArrayList<PhotoBean> photoBeans = dateGroupPhotos.get(j);
                    for (int i = 0; i < checks.size(); i++) {
                        Boolean b = checks.get(i);
                        if (b) {
                            num++;
                            if (onlineGroups)
                                deleteIds.add(photoBeans.get(i).getUrl_p_id());
                            else
                                deleteIds.add(photoBeans.get(i).getId());
                        }
                    }
                }
                if (num > 0) {
                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setTitle("提示")
                            .setMessage("确认删除所选的" + num + "张图片?")
                            .setNegativeButton("取消", null)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteNum = 0;
                                    deleteSuccessNum = 0;
                                    if (onlineGroups) {
                                        for (int id : deleteIds) {
                                            if (id > 0) {
                                                StringRequest request = new StringRequest(
                                                        Request.Method.POST,
                                                        "http://www.foxluo.cn/alumni_club-1.0/sql/photo/delete",
                                                        new Response.Listener<String>() {
                                                            @Override
                                                            public void onResponse(String s) {
                                                                new Thread() {
                                                                    @Override
                                                                    public void run() {
                                                                        MyApplication.dao.deleteOnlinePhoto(id);
                                                                    }
                                                                }.start();
                                                                deleted = true;
                                                                setResult(10002);
                                                                for (int i = 0; i < editPhotos.size(); i++) {
                                                                    PhotoBean photoBean = editPhotos.get(i);
                                                                    if (photoBean.getUrl_p_id() == id) {
                                                                        editPhotos.remove(i);
                                                                        getData();
                                                                        break;
                                                                    }
                                                                }
                                                                deleteNum++;
                                                                deleteSuccessNum++;
                                                                if (deleteNum == num) {
                                                                    if (deleteSuccessNum == deleteNum) {
                                                                        Toast.makeText(EditActivity.this, deleteSuccessNum + "张图片删除成功", Toast.LENGTH_SHORT).show();
                                                                    } else
                                                                        Toast.makeText(EditActivity.this, deleteSuccessNum + "张图片删除成功，" + (num - deleteSuccessNum) + "张删除失败", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        },
                                                        new Response.ErrorListener() {
                                                            @Override
                                                            public void onErrorResponse(VolleyError volleyError) {
                                                                deleteNum++;
                                                                if (deleteNum == num) {
                                                                    Toast.makeText(EditActivity.this, deleteNum + "张图片删除失败", Toast.LENGTH_SHORT).show();
                                                                }
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
                                                        map.put("id", id + "");
                                                        return map;
                                                    }
                                                };
                                                RequestQueue mQueue = Volley.newRequestQueue(EditActivity.this);
                                                mQueue.add(request);
                                            }
                                        }
                                    }
                                }
                            }).create();
                    dialog.show();
                }
                break;
        }
    }
}