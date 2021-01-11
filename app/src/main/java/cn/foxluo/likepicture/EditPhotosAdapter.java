package cn.foxluo.likepicture;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class EditPhotosAdapter extends RecyclerView.Adapter {
    private ArrayList<ArrayList<PhotoBean>> groupPhotos;
    private ArrayList<ArrayList<Boolean>> photoChecks;
    private Context context;
    private OnGroupPhotosClickListener listener;

    public EditPhotosAdapter(ArrayList<ArrayList<PhotoBean>> groupPhotos, ArrayList<ArrayList<Boolean>> photoChecks, Context context, OnGroupPhotosClickListener listener) {
        this.groupPhotos = groupPhotos;
        this.photoChecks = photoChecks;
        this.context = context;
        this.listener = listener;
    }

    public interface OnGroupPhotosClickListener {
        void photoCheckChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GroupViewHolder(LayoutInflater.from(context).inflate(R.layout.edit_photo_group, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        GroupViewHolder viewHolder = (GroupViewHolder) holder;
        if (TimeUtil.isSameDay(new Date().getTime(), groupPhotos.get(position).get(0).getTime()))
            viewHolder.group.setText("今天");
        else if (TimeUtil.isSameDay(new Date().getTime() - 24 * 60 * 60 * 1000L, groupPhotos.get(position).get(0).getTime()))
            viewHolder.group.setText("昨天");
        else {
            Date date = new Date(groupPhotos.get(position).get(0).getTime());
            viewHolder.group.setText(new SimpleDateFormat("yyyy年MM月dd日").format(date) + " " + TimeUtil.getWeek(date));
        }
        setGroupCheck(viewHolder, position);
        viewHolder.photos.setVisibility(View.VISIBLE);
        viewHolder.adapter = new ListPhotosAdapter(groupPhotos.get(position), new OnPhotoClickListener() {
            @Override
            public void photoClick(int photoPosition) {
                photoChecks.get(position).set(photoPosition, !photoChecks.get(position).get(photoPosition));
                listener.photoCheckChanged();
                setGroupCheck(viewHolder, position);
                viewHolder.adapter.notifyItemChanged(photoPosition);
            }

            @Override
            public void photoChecked(boolean isChecked, int photoPosition) {
                photoChecks.get(position).set(photoPosition, isChecked);
                listener.photoCheckChanged();
                setGroupCheck(viewHolder, position);
            }

        }, context, position);
        viewHolder.size.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked=viewHolder.size.isChecked();
                for (int i = 0; i < photoChecks.get(position).size(); i++) {
                    photoChecks.get(position).set(i,isChecked);
                    listener.photoCheckChanged();
                }
                viewHolder.adapter.notifyDataSetChanged();
            }
        });
        GridLayoutManager layoutManager=new GridLayoutManager(context,3);
        viewHolder.photos.setLayoutManager(layoutManager);
        viewHolder.photos.setAdapter(viewHolder.adapter);
        viewHolder.photos.setHasFixedSize(true);
    }

    private void setGroupCheck(GroupViewHolder viewHolder, int position) {
        boolean groupCheck = true;
        if (photoChecks.size()==0){
            return;
        }
        for (Boolean check : photoChecks.get(position)) {
            if (!check) {
                groupCheck = false;
                break;
            }
        }
        viewHolder.size.setChecked(groupCheck);
    }

    class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView group;
        RecyclerView photos;
        CheckBox size;
        ListPhotosAdapter adapter;
        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            group = itemView.findViewById(R.id.group);
            size = itemView.findViewById(R.id.size);
            photos = itemView.findViewById(R.id.group_photos);
        }
    }
    interface OnPhotoClickListener {
        void photoClick(int photoPosition);

        void photoChecked(boolean isChecked, int photoPosition);
    }
    class ListPhotosAdapter extends RecyclerView.Adapter {
        private ArrayList<PhotoBean> photos;
        private OnPhotoClickListener listener;
        private Context context;
        private int groupPosition;

        public ListPhotosAdapter(ArrayList<PhotoBean> photos,OnPhotoClickListener listener, Context context, int groupPosition) {
            this.photos = photos;
            this.listener = listener;
            this.context = context;
            this.groupPosition = groupPosition;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ListPhotosAdapter.PhotoViewHolder(LayoutInflater.from(context).inflate(R.layout.edit_photo, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (photoChecks.size()==0||photoChecks.get(groupPosition).size()==0||photos.size()==0||photos.get(position)==null){
                return;
            }
            Glide.with(context)
                    .load(photos.get(position).getPath())
                    .centerCrop()
                    .placeholder(R.drawable.pic)
                    .error(R.drawable.pic_break)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(((ListPhotosAdapter.PhotoViewHolder) holder).photo);
            ListPhotosAdapter.PhotoViewHolder viewHolder = (ListPhotosAdapter.PhotoViewHolder) holder;
            viewHolder.check.setChecked(photoChecks.get(groupPosition).get(position));
            viewHolder.check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    listener.photoChecked(isChecked, position);
                }
            });
            viewHolder.photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.photoClick(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return photos.size();
        }

        class PhotoViewHolder extends RecyclerView.ViewHolder {
            ImageView photo;
            CheckBox check;
            public PhotoViewHolder(@NonNull View itemView) {
                super(itemView);
                photo = itemView.findViewById(R.id.img_photo);
                check = itemView.findViewById(R.id.check);
            }
        }
    }

    @Override
    public int getItemCount() {
        return groupPhotos.size();
    }
}
