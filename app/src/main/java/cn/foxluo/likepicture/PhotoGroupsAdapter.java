package cn.foxluo.likepicture;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

class PhotoGroupsAdapter extends RecyclerView.Adapter{
    private ArrayList<ArrayList<PhotoBean>> dateGroupPhotos;
    private Context context;
    private PhotoGroupsAdapter.OnPhotoClickListener listener;
    private View headerView;
    public PhotoGroupsAdapter(ArrayList<ArrayList<PhotoBean>> dateGroupPhotos, Context context, PhotoGroupsAdapter.OnPhotoClickListener listener) {
        this.dateGroupPhotos= dateGroupPhotos;
        this.context = context;
        this.listener=listener;
        headerView= LayoutInflater.from(context).inflate(R.layout.home_header,null,false);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.home_pic_group, parent, false);
        return new PhotoGroupsAdapter.GroupViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        if (position==0){
            return 0;
        }else {
            return 1;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PhotoGroupsAdapter.GroupViewHolder viewHolder=(PhotoGroupsAdapter.GroupViewHolder)holder;
        if (TimeUtil.isSameDay(new Date().getTime(),dateGroupPhotos.get(position).get(0).getTime()))
            viewHolder.group.setText("今天");
        else if (TimeUtil.isSameDay(new Date().getTime()-24*60*60*1000L,dateGroupPhotos.get(position).get(0).getTime()))
            viewHolder.group.setText("昨天");
        else{
            Date date=new Date(dateGroupPhotos.get(position).get(0).getTime());
            viewHolder.group.setText(new SimpleDateFormat("yyyy年MM月dd日").format(date)+" "+TimeUtil.getWeek(date));
        }

        if (listener!=null){
            int finalPosition = position;
            PhotoGroupsAdapter.ListPhotosAdapter adapter=new PhotoGroupsAdapter.ListPhotosAdapter(dateGroupPhotos.get(position), new PhotoGroupsAdapter.ListPhotosAdapter.OnPhotoClickListener() {
                @Override
                public void photoClick(int photoPosition) {
                    listener.onPhotoClick(finalPosition,photoPosition);
                }

                @Override
                public void photoLongClick(int photoPosition) {
                    listener.onPhotoLongClick(finalPosition,photoPosition);
                }
            },context);
            GridLayoutManager layoutManager=new GridLayoutManager(context,3);
            viewHolder.photoView.setLayoutManager(layoutManager);
            viewHolder.photoView.setNestedScrollingEnabled(false);
            viewHolder.photoView.setAdapter(adapter);
        }
    }

    public class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView group;
        RecyclerView photoView;
        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            if (itemView==headerView){
                return;
            }
            photoView=itemView.findViewById(R.id.group_photos);
            group=itemView.findViewById(R.id.group);
        }
    }
    @Override
    public int getItemCount() {
        return dateGroupPhotos.size();
    }

    static class ListPhotosAdapter extends RecyclerView.Adapter {
        private ArrayList<PhotoBean> photos;
        private PhotoGroupsAdapter.ListPhotosAdapter.OnPhotoClickListener listener;
        private Context context;

        public ListPhotosAdapter(ArrayList<PhotoBean> photos, PhotoGroupsAdapter.ListPhotosAdapter.OnPhotoClickListener listener, Context context) {
            this.photos = photos;
            this.listener = listener;
            this.context = context;
        }

        interface OnPhotoClickListener {
            void photoClick(int photoPosition);
            void photoLongClick(int photoPosition);
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PhotoGroupsAdapter.ListPhotosAdapter.PhotoViewHolder(LayoutInflater.from(context).inflate(R.layout.home_pic_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Glide.with(context)
                    .load(photos.get(position).getPath())
                    .centerCrop()
                    .placeholder(R.drawable.pic)
                    .error(R.drawable.pic_break)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(((PhotoGroupsAdapter.ListPhotosAdapter.PhotoViewHolder) holder).img_photo);
            PhotoGroupsAdapter.ListPhotosAdapter.PhotoViewHolder viewHolder = (PhotoGroupsAdapter.ListPhotosAdapter.PhotoViewHolder) holder;
            viewHolder.img_photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.photoClick(position);
                }
            });
            viewHolder.img_photo.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    listener.photoLongClick(position);
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return photos.size();
        }

        class PhotoViewHolder extends RecyclerView.ViewHolder {
            ImageView img_photo;
            public PhotoViewHolder(@NonNull View itemView) {
                super(itemView);
                img_photo = itemView.findViewById(R.id.img_photo);
            }
        }
    }

    public interface OnPhotoClickListener {
        void onPhotoClick(int groupPosition, int photoPosition);
        void onPhotoLongClick(int groupPosition, int photoPosition);
    }
}
