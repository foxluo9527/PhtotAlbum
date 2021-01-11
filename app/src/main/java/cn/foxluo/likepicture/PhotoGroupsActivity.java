package cn.foxluo.likepicture;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;

public class PhotoGroupsActivity extends AppCompatActivity {
    private ArrayList<PhotoBean> groupFirstPhotos = new ArrayList<>();
    private ArrayList<PhotoGroupBean> photoGroupBeans = new ArrayList<>();
    private ArrayList<Boolean> groupChecks = new ArrayList<>();
    View editBox;
    View gone;
    View toolBar, editBar;
    TextView choice;
    TextView all_choice;
    private PhotoGroupAdapter adapter;
    RecyclerView groups;
    boolean onEdit = false;
    boolean allChoice = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(getResources().getColor(R.color.white));
        }
        setContentView(R.layout.activity_photo_groups);
        editBox = findViewById(R.id.edit_box);
        gone = findViewById(R.id.gone);
        toolBar = findViewById(R.id.toolbar);
        editBar = findViewById(R.id.edit_bar);
        choice = findViewById(R.id.choice);
        all_choice = findViewById(R.id.all_choice);
        adapter = new PhotoGroupAdapter(groupFirstPhotos, photoGroupBeans, groupChecks, new PhotoGroupAdapter.OnPhotoClickListener() {
            @Override
            public void photoClick(int photoPosition) {
                if (!onEdit) {
                    Intent intent=new Intent(PhotoGroupsActivity.this, YunPhotosActivity.class);
                    intent.putExtra("groupId",photoGroupBeans.get(photoPosition).getId());
                    intent.putExtra("name",photoGroupBeans.get(photoPosition).getName());
                    startActivity(intent);
                }
            }

            @Override
            public void photoLongClick(int photoPosition) {
                if (!onEdit) {
                    Vibrator vibrator = (Vibrator) PhotoGroupsActivity.this.getSystemService(PhotoGroupsActivity.this.VIBRATOR_SERVICE);
                    vibrator.vibrate(100);
                    editBox.setVisibility(View.VISIBLE);
                    if (photoGroupBeans.size() > 1)
                        gone.setVisibility(View.GONE);
                    else
                        gone.setVisibility(View.VISIBLE);
                    editBar.setVisibility(View.VISIBLE);
                    toolBar.setVisibility(View.GONE);
                    choice.setText("选择项目");
                    all_choice.setText("全选");
                    allChoice = false;
                    groupChecks.clear();
                    for (int i = 0; i < photoGroupBeans.size(); i++) {
                        if (i != 0 && i == photoPosition) {
                            groupChecks.add(true);
                        } else {
                            groupChecks.add(false);
                        }
                    }
                    adapter.setCheckModel(true);
                    adapter.notifyDataSetChanged();
                    onEdit = true;
                    checkChanged();
                }
            }

            @Override
            public void groupCheckChanged() {
                checkChanged();
            }
        }, this);
        groups = findViewById(R.id.groups);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        groups.setLayoutManager(layoutManager);
        groups.setAdapter(adapter);
    }

    private void checkChanged() {
        int num = 0;
        boolean flag = false;
        for (int i = 0; i < groupChecks.size(); i++) {
            boolean b = groupChecks.get(i);
            if (b || photoGroupBeans.get(i).getId() == 1) {
                num++;
            } else {
                flag = true;
                allChoice = false;
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
        num--;
        groupChecks.set(0, false);
        if (num == 0) {
            choice.setText("选择项目");
            gone.setVisibility(View.VISIBLE);
        } else {
            choice.setText("已选择" + num + "项");
            gone.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.setCheckModel(false);
        onEdit = false;
        editBox.setVisibility(View.GONE);
        gone.setVisibility(View.GONE);
        editBar.setVisibility(View.GONE);
        toolBar.setVisibility(View.VISIBLE);
        adapter.notifyDataSetChanged();
        new AsyncTask<Void, Void, ArrayList<PhotoGroupBean>>() {
            @Override
            protected ArrayList<PhotoGroupBean> doInBackground(Void... voids) {
                photoGroupBeans.clear();
                groupFirstPhotos.clear();
                photoGroupBeans.addAll((ArrayList<PhotoGroupBean>) MyApplication.dao.getPhotoGroups());
                for (PhotoGroupBean photoGroupBean : photoGroupBeans) {
                    ArrayList<PhotoBean> photoBeans = (ArrayList<PhotoBean>) MyApplication.dao.getGroupPhotos(photoGroupBean.getId());
                    if (photoBeans.size() == 0) {
                        groupFirstPhotos.add(new PhotoBean(0, null, null, 0));
                        photoGroupBean.setNum(0);
                    } else {
                        groupFirstPhotos.add(photoBeans.get(0));
                        photoGroupBean.setNum(photoBeans.size());
                    }
                }
                return photoGroupBeans;
            }

            @Override
            protected void onPostExecute(ArrayList<PhotoGroupBean> photoGroupBeans) {
                ((TextView) findViewById(R.id.textView)).setText("共" + photoGroupBeans.size() + "组相册");
                adapter.notifyDataSetChanged();
            }
        }.execute();
    }

    @Override
    public void onBackPressed() {
        if (onEdit) {
            adapter.setCheckModel(false);
            onEdit = false;
            editBox.setVisibility(View.GONE);
            gone.setVisibility(View.GONE);
            editBar.setVisibility(View.GONE);
            toolBar.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        } else {
            super.onBackPressed();
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            onResume();
        }
    };
    Handler handler1 = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Toast.makeText(PhotoGroupsActivity.this, deleteNum + "个相册删除成功", Toast.LENGTH_SHORT).show();
            onResume();
        }
    };

    public static void createGroup(Context context, Handler handler) {
        EditText editText = new EditText(context);
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.setMargins(HomeActivity.dip2px(context, 20), 0, HomeActivity.dip2px(context, 20), 0);
        editText.setLayoutParams(layout);
        editText.setFocusable(true);
        new AlertDialog.Builder(context)
                .setTitle("新建相册")
                .setMessage("请输入相册名称")
                .setView(editText)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (editText.getText().toString().length() > 0)
                            new Thread() {
                                @Override
                                public void run() {
                                    super.run();
                                    String name=editText.getText().toString();
                                    MyApplication.dao.insertPhotoGroup(new PhotoGroupBean(0, name, null, new Date().getTime()));
                                    int id=MyApplication.dao.getLastInsertGroupId();
                                    Message message=new Message();
                                    message.arg1=id;
                                    message.obj=name;
                                    handler.sendMessage(message);
                                }
                            }.start();
                        else
                            Toast.makeText(context, "请输入名称", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    int deleteNum;

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.del:
                int num = 0;

                ArrayList<Integer> deleteIds = new ArrayList<>();
                for (int i = 0; i < groupChecks.size(); i++) {
                    Boolean b = groupChecks.get(i);
                    if (b) {
                        num++;
                        if (photoGroupBeans.get(i).getId() != 1) {
                            deleteIds.add(photoGroupBeans.get(i).getId());
                        }
                    }
                }
                if (num > 0) {
                    final int finalNum = num;
                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setTitle("提示")
                            .setMessage("确认删除所选的" + num + "个相册?")
                            .setNegativeButton("取消", null)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteNum = 0;
                                    for (int id : deleteIds) {
                                        new Thread() {
                                            @Override
                                            public void run() {
                                                MyApplication.dao.deletePhotoGroup(id);
                                                deleteNum++;
                                                if (deleteNum == finalNum) {
                                                    handler1.sendEmptyMessage(0);
                                                }
                                            }
                                        }.start();
                                    }
                                }
                            }).create();
                    dialog.show();
                }
                break;
            case R.id.add_g:
                createGroup(this,handler);
                break;
            case R.id.cancel:
                adapter.setCheckModel(false);
                adapter.notifyDataSetChanged();
                onEdit = false;
                editBox.setVisibility(View.GONE);
                gone.setVisibility(View.GONE);
                editBar.setVisibility(View.GONE);
                toolBar.setVisibility(View.VISIBLE);
                break;
            case R.id.all_choice:
                if (groupChecks.size() == 1) {
                    break;
                }
                for (int i = 0; i < groupChecks.size(); i++) {
                    if (i != 0)
                        groupChecks.set(i, !allChoice);
                    else
                        groupChecks.set(0, false);
                }
                checkChanged();
                adapter.notifyDataSetChanged();
                break;
        }
    }
}