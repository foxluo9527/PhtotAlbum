package cn.foxluo.likepicture;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

class ChoiceGroupsAdapter extends RecyclerView.Adapter {
    private ArrayList<PhotoBean> photos;
    private ArrayList<PhotoGroupBean> photoGroups;
    private OnPhotoClickListener listener;
    private Context context;

    public interface OnPhotoClickListener {
        void itemClick(int photoPosition);
    }

    public ChoiceGroupsAdapter(ArrayList<PhotoBean> photos, ArrayList<PhotoGroupBean> photoGroups, OnPhotoClickListener listener, Context context) {
        this.photos = photos;
        this.photoGroups = photoGroups;
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PhotoViewHolder(LayoutInflater.from(context).inflate(R.layout.group_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Glide.with(context)
                .load(photos.get(position).getPath())
                .centerCrop()
                .placeholder(R.drawable.pic)
                .error(R.drawable.pic)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(((PhotoViewHolder) holder).img);
        PhotoViewHolder viewHolder = (PhotoViewHolder) holder;
        viewHolder.name.setText(photoGroups.get(position).getName());
        viewHolder.num.setText(photoGroups.get(position).getNum() + "张 "+new SimpleDateFormat("yyyy年MM月dd日").format(photoGroups.get(position).getDate()));
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.itemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView name;
        TextView num;
        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img);
            name = itemView.findViewById(R.id.name);
            num = itemView.findViewById(R.id.desc);
        }
    }
}
