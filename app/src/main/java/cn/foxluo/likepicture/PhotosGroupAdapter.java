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
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class PhotosGroupAdapter extends RecyclerView.Adapter {
    private ArrayList<ArrayList<PhotoBean>> groupPhotos;
    private ArrayList<ArrayList<Boolean>> photoChecks;
    private ArrayList<Boolean> opens;
    private Context context;
    private OnGroupPhotosClickListener listener;

    public PhotosGroupAdapter(ArrayList<ArrayList<PhotoBean>> groupPhotos, ArrayList<ArrayList<Boolean>> photoChecks, ArrayList<Boolean> opens, Context context, OnGroupPhotosClickListener listener) {
        this.groupPhotos = groupPhotos;
        this.photoChecks = photoChecks;
        this.context = context;
        this.opens = opens;
        this.listener = listener;
    }

    public interface OnGroupPhotosClickListener {
        void photoClick(int groupIndex, int photoIndex);

        void photoCheckChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GroupViewHolder(LayoutInflater.from(context).inflate(R.layout.sl_group, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        GroupViewHolder viewHolder = (GroupViewHolder) holder;
        ListPhotosAdapter adapter = new ListPhotosAdapter(groupPhotos.get(position), new OnPhotoClickListener() {
            @Override
            public void photoClick(int photoPosition) {
                listener.photoClick(position, photoPosition);
            }

            @Override
            public void photoChecked(boolean isChecked, int photoPosition) {
                photoChecks.get(position).set(photoPosition, isChecked);
                listener.photoCheckChanged();
                setGroupCheck(viewHolder, position);
            }

        }, context, position);
        viewHolder.photos.setLayoutManager(new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL));
        viewHolder.photos.setAdapter(adapter);
        viewHolder.photos.setHasFixedSize(true);
        viewHolder.group.setText(new SimpleDateFormat("yyyy年MM月dd日").format(groupPhotos.get(position).get(0).getTime()));
        long size = 0L;
        for (PhotoBean photoBean : groupPhotos.get(position)) {
            size += photoBean.getSize();
        }
        setGroupCheck(viewHolder, position);
        viewHolder.size.setText(SimilarActivity.format(size));
        setPhotos(viewHolder, opens.get(position), position, adapter);
        viewHolder.size.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isChecked=viewHolder.size.isChecked();
                    for (int i = 0; i < photoChecks.get(position).size(); i++) {
                        photoChecks.get(position).set(i,isChecked);
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        );
        viewHolder.group_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                opens.set(position, !opens.get(position));
                setPhotos(viewHolder, opens.get(position), position, adapter);
            }
        });
    }

    private void setPhotos(GroupViewHolder viewHolder, boolean open, int position, ListPhotosAdapter adapter) {
        if (open) {
            viewHolder.photos.setVisibility(View.VISIBLE);
            viewHolder.open.setImageDrawable(context.getResources().getDrawable(R.drawable.d));
        } else {
            viewHolder.photos.setVisibility(View.GONE);
            viewHolder.open.setImageDrawable(context.getResources().getDrawable(R.drawable.r));
        }
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

    @Override
    public int getItemCount() {
        return groupPhotos.size();
    }

    class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView group;
        RecyclerView photos;
        CheckBox size;
        ImageView open;
        View group_open;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            group = itemView.findViewById(R.id.group);
            size = itemView.findViewById(R.id.size);
            photos = itemView.findViewById(R.id.group_photos);
            open = itemView.findViewById(R.id.open);
            group_open = itemView.findViewById(R.id.group_open);
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

        public ListPhotosAdapter(ArrayList<PhotoBean> photos, OnPhotoClickListener listener, Context context, int groupPosition) {
            this.photos = photos;
            this.listener = listener;
            this.context = context;
            this.groupPosition = groupPosition;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PhotoViewHolder(LayoutInflater.from(context).inflate(R.layout.sll, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (photoChecks.size()==0||photoChecks.get(groupPosition).size()==0||photos.size()==0||photos.get(position)==null){
                return;
            }
            Glide.with(context).load(photos.get(position).getPath()).placeholder(R.drawable.pic).error(R.drawable.pic_break).into(((PhotoViewHolder) holder).photo);
            PhotoViewHolder viewHolder = (PhotoViewHolder) holder;
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
            viewHolder.photoSize.setText(SimilarActivity.format(photos.get(position).getSize()));
        }

        @Override
        public int getItemCount() {
            return photos.size();
        }

        class PhotoViewHolder extends RecyclerView.ViewHolder {
            ImageView photo;
            CheckBox check;
            TextView photoSize;

            public PhotoViewHolder(@NonNull View itemView) {
                super(itemView);
                photo = itemView.findViewById(R.id.photo);
                check = itemView.findViewById(R.id.check);
                photoSize = itemView.findViewById(R.id.photo_size);
            }
        }
    }
}
