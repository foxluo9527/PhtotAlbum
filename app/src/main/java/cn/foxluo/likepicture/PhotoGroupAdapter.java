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

import java.util.ArrayList;

public class PhotoGroupAdapter extends RecyclerView.Adapter {
    private ArrayList<PhotoBean> photos;
    private ArrayList<PhotoGroupBean> photoGroups;
    private ArrayList<Boolean> groupChecks;
    private OnPhotoClickListener listener;
    private Context context;
    boolean checkModel = false;

    public interface OnPhotoClickListener {
        void photoClick(int photoPosition);

        void photoLongClick(int photoPosition);

        void groupCheckChanged();
    }

    public PhotoGroupAdapter(ArrayList<PhotoBean> photos, ArrayList<PhotoGroupBean> photoGroups, ArrayList<Boolean> groupChecks, OnPhotoClickListener listener, Context context) {
        this.photos = photos;
        this.photoGroups = photoGroups;
        this.groupChecks = groupChecks;
        this.listener = listener;
        this.context = context;
    }

    public void setCheckModel(boolean checkModel) {
        this.checkModel = checkModel;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PhotoViewHolder(LayoutInflater.from(context).inflate(R.layout.photo_group_item, parent, false));
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
        viewHolder.num.setText(photoGroups.get(position).getNum() + "");
        viewHolder.check.setVisibility(View.GONE);
        viewHolder.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (photoGroups.get(position).getId() != 1 && checkModel) {
                    groupChecks.set(position, !groupChecks.get(position));
                    viewHolder.check.setChecked(groupChecks.get(position));
                    listener.groupCheckChanged();
                } else
                    listener.photoClick(position);
            }
        });
        viewHolder.img.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                listener.photoLongClick(position);
                return true;
            }
        });
        viewHolder.check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                groupChecks.set(position, !groupChecks.get(position));
                listener.groupCheckChanged();
            }
        });
        if (photoGroups.get(position).getId() != 1 && checkModel) {
            viewHolder.check.setVisibility(View.VISIBLE);
            if (groupChecks.get(position))
                viewHolder.check.setChecked(true);
            else
                viewHolder.check.setChecked(false);
        }
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView name;
        TextView num;
        CheckBox check;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img);
            name = itemView.findViewById(R.id.name);
            num = itemView.findViewById(R.id.num);
            check = itemView.findViewById(R.id.check);
        }
    }
}
