package comfallblank.github.gankgirl;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

import comfallblank.github.gankgirl.model.Meizi;
import comfallblank.github.gankgirl.utils.BackgroundThread;
import comfallblank.github.gankgirl.utils.MyRecycleAdapter;

public class MainActivity extends AppCompatActivity {

    private BackgroundThread mBackgroundThread;

    private ArrayList<Meizi> mDates;

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BackgroundThread.MSG_SUCCESS:
                    mAdapter.notifyDataSetChanged();
                    mSwipeRefreshLayout.setRefreshing(false);
                    break;
                case BackgroundThread.MSG_FAILED:
                    Toast.makeText(getApplicationContext(), "刷新失败", Toast.LENGTH_SHORT).show();
                    mSwipeRefreshLayout.setRefreshing(false);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDates = new ArrayList<Meizi>();
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_reflesh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mBackgroundThread.requestGank();
            }
        });
        initBackgroundThread();
        mRecyclerView = (RecyclerView) findViewById(R.id.recycle_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MyRecycleAdapter(mDates, mBackgroundThread);
        mRecyclerView.setAdapter(mAdapter);
    }


    private void initBackgroundThread() {
        mBackgroundThread = new BackgroundThread(mHandler, mDates);
        mBackgroundThread.start();
        mBackgroundThread.getLooper();
        mBackgroundThread.setLinstener(new BackgroundThread.Linstener() {
            @Override
            public void loadfinished(ImageView imageView, Bitmap bitmap) {
                imageView.setImageBitmap(bitmap);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBackgroundThread.quit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
