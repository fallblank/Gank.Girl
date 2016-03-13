package comfallblank.github.gankgirl.utils;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

import comfallblank.github.gankgirl.R;
import comfallblank.github.gankgirl.model.Meizi;

/**
 * Created by fallb on 2016/3/12.
 */
public class MyRecycleAdapter extends RecyclerView.Adapter<MyRecycleAdapter.MyViewHolder> {
    private static final String TAG = "MyRecycleAdapter";
    private ArrayList<Meizi> mDatalist;
    private BackgroundThread mLoaderThread;


    public MyRecycleAdapter(ArrayList<Meizi> datalist, BackgroundThread loaderThread) {
        mDatalist = datalist;
        mLoaderThread = loaderThread;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        //实现异步加载图片
        ImageView imageView = holder.mImageView;
        mLoaderThread.requestBitmap(imageView, mDatalist.get(position));
    }

    @Override
    public int getItemCount() {
        return mDatalist.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView;
        public MyViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.item_image);
        }
    }
}