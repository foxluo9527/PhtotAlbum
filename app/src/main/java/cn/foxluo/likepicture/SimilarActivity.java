package cn.foxluo.likepicture;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.bingoogolapple.photopicker.activity.BGAPhotoPreviewActivity;

import static cn.foxluo.likepicture.HomeActivity.allPhotos;

public class
SimilarActivity extends AppCompatActivity {
    ArrayList<ArrayList<PhotoBean>> similarPhotoGroups = new ArrayList<>();
    ProgressBar progressBar;
    PhotosGroupAdapter adapter;
    RecyclerView similar_photos;
    CheckBox check;
    Button button;
    private int index = 0;
    private Date date;
    private long size;
    ExecutorService threadPool;
    ArrayList<ArrayList<Boolean>> photoChecks = new ArrayList<>();
    ArrayList<Boolean> opens = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Window window=getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(getResources().getColor(R.color.white));
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_similar);
        threadPool = Executors.newCachedThreadPool();
        progressBar = findViewById(R.id.progressBar);
        similar_photos = findViewById(R.id.similar_photos);
        button = findViewById(R.id.button);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        adapter = new PhotosGroupAdapter(similarPhotoGroups, photoChecks, opens, SimilarActivity.this, new PhotosGroupAdapter.OnGroupPhotosClickListener() {
            @Override
            public void photoClick(int groupIndex, int photoIndex) {
                BGAPhotoPreviewActivity.IntentBuilder photoPreviewIntentBuilder = new BGAPhotoPreviewActivity.IntentBuilder(SimilarActivity.this)
                        .saveImgDir(null);
                if (similarPhotoGroups.get(groupIndex).size() == 1) {
                    photoPreviewIntentBuilder.previewPhoto(similarPhotoGroups.get(groupIndex).get(0).getPath());
                } else if (similarPhotoGroups.get(groupIndex).size() > 1) {
                    ArrayList<String> showPhotoUrls = new ArrayList<>();
                    for (PhotoBean photoBean : similarPhotoGroups.get(groupIndex)) {
                        showPhotoUrls.add(photoBean.getPath());
                    }
                    photoPreviewIntentBuilder.previewPhotos(showPhotoUrls)
                            .currentPosition(photoIndex);
                }
                startActivityForResult(photoPreviewIntentBuilder.build(), -1);
            }

            @Override
            public void photoCheckChanged() {
                checkChanged();
            }

        });
        StaggeredGridLayoutManager staggeredGridLayoutManager1 = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        similar_photos.setLayoutManager(staggeredGridLayoutManager1);
        similar_photos.setAdapter(adapter);
        check = findViewById(R.id.check);
        check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for (int i = 0; i < photoChecks.size(); i++) {
                    for (int j = 0; j < photoChecks.get(i).size(); j++) {
                        photoChecks.get(i).set(j, isChecked);
                    }
                }
                adapter.notifyDataSetChanged();
                checkChanged();
            }
        });
        getData();
    }

    private void checkChanged() {
        long size = 0L;
        int num = 0;
        for (int i = 0; i < photoChecks.size(); i++) {
            for (int j = 0; j < photoChecks.get(i).size(); j++) {
                if (photoChecks.get(i).get(j)) {
                    size += similarPhotoGroups.get(i).get(j).getSize();
                    num++;
                }
            }
        }
        if (num == 0) {
            button.setEnabled(false);
            button.setText("删除");
        } else {
            button.setEnabled(true);
            button.setText("删除(" + num + "项," + format(size) + ")");
        }
    }

    private void getData() {
        ArrayList<ArrayList<PhotoBean>> dateGroupPhotos = new ArrayList<>();
        new Thread() {
            @Override
            public void run() {
                doneHandler.sendEmptyMessage(0);
                try {
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
                    for (int i = 0; i < dateGroupPhotos.size(); i++) {
                        ArrayList<PhotoBean> datePhotos = dateGroupPhotos.get(i);
                        if (datePhotos.size() == 1) {
                            dateGroupPhotos.remove(i);
                            i--;
                        }
                    }
                    index = 0;
                    size = 0L;
                    for (ArrayList<PhotoBean> datePhotos : dateGroupPhotos) {
                        threadPool.execute(() -> {
                            try {
                                int dateGroupSize = dateGroupPhotos.size() - 1;
                                for (PhotoBean photoBean : datePhotos) {
                                    if (TextUtils.isEmpty(photoBean.getHashCode()))
                                        photoBean.setHashCode(ImageHelper.getHashCode(photoBean));
                                }
                                for (int i = 0; i < datePhotos.size(); i++) {
                                    boolean flag = false;
                                    ArrayList<PhotoBean> photoBeans = new ArrayList<>();
                                    for (int j = i + 1; j < datePhotos.size(); j++) {
                                        try {
                                            if (distance(datePhotos.get(i).getHashCode(), datePhotos.get(j).getHashCode()) <= 16) {
                                                size += datePhotos.get(j).getSize();
                                                flag = true;
                                                photoBeans.add(datePhotos.remove(j));
                                                j--;
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if (flag) {
                                        photoBeans.add(datePhotos.get(i));
                                        size += datePhotos.get(i).getSize();
                                    }
                                    if (photoBeans.size() > 0) {
                                        similarPhotoGroups.add(photoBeans);
                                    }
                                }
                                if (index >= dateGroupSize)
                                    doneHandler.sendEmptyMessage(1);
                                index++;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        threadPool.shutdown();
    }

    @SuppressLint("HandlerLeak")
    Handler doneHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == 1) {
                photoChecks.clear();
                opens.clear();
                Collections.sort(similarPhotoGroups, (o1, o2) ->
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
                for (ArrayList<PhotoBean> photoBeans : similarPhotoGroups) {
                    ArrayList<Boolean> checks = new ArrayList<>();
                    for (PhotoBean photoBean : photoBeans) {
                        checks.add(false);
                    }
                    photoChecks.add(checks);
                    opens.add(true);
                }
                progressBar.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
                long time = System.currentTimeMillis() - date.getTime();
                Toast.makeText(SimilarActivity.this, "扫描完成,时间花费: " + (time / 1000) + "s", Toast.LENGTH_LONG).show();
                ((TextView) findViewById(R.id.textView)).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.textView)).setText("共" + similarPhotoGroups.size() + "组相似图片,占用空间" + format(size));
                check.setVisibility(View.VISIBLE);
            } else {
                date = new Date();
            }
        }
    };

    public static String format(long size) {
        String fileSize;
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        if (size < 1024l) {
            fileSize = decimalFormat.format(size) + " B";
        } else if (size < 1048576l) {
            fileSize = decimalFormat.format((double) size / 1024l) + "KB";
        } else if (size < 1073741824l) {
            fileSize = decimalFormat.format((double) size / (1048576l)) + "MB";
        } else {
            fileSize = decimalFormat.format((double) size / (1073741824l)) + "GB";
        }
        return String.format(Locale.ENGLISH, fileSize);
    }

    private int distance(String s1, String s2) {
        int counter = 0;
        if (s1==null||s2==null){
            return 100;
        }
        for (int k = 0; k < s1.length(); k++) {
            if (s1.charAt(k) != s2.charAt(k)) {
                counter++;
            }
        }
        return counter;
    }
}