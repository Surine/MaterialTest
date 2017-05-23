package com.example.materialtest.UI;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.Toast;

import com.example.materialtest.Adapter.PictureAdapter;
import com.example.materialtest.Bean.Picture;
import com.example.materialtest.Data.Urldate;
import com.example.materialtest.EventBus.SimpleEventBus;
import com.example.materialtest.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    int page = 1;
    private List<Picture> mPictureList = new ArrayList<>();
    private PictureAdapter adapter;

    private SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        EventBus.getDefault().register(this);

        //init the picture and we need a parameter named page
        initPicture(page);
    }

    private void initPicture(final int page) {
        //start a new thread for okhttp
       new Thread(new Runnable() {
           @Override
           public void run() {
               try {
                   OkHttpClient client = new OkHttpClient();
                   Request request = new Request.Builder()
                           .url(Urldate.gank_url+page)
                           .build();
                   Response response = client.newCall(request).execute();
                   String respondata = response.body().string();
                   //Resolve unparsed errors
                   respondata = respondata.substring(respondata.indexOf("["),respondata.indexOf("]")+1);
                   //Resolve the json
                   parseJson(respondata);
               } catch (IOException e) {
                   Snackbar.make(swipeRefresh, "加载出错，网络问题？", Snackbar.LENGTH_SHORT).show();
                   e.printStackTrace();
               }
           }
       }).start();
    }

    private void parseJson(String respondata) {
        Gson gson = new Gson();
        ArrayList<Picture> list = new ArrayList<Picture>();
        Type listType = new TypeToken<List<Picture>>() {}.getType();
        list = gson.fromJson(respondata, listType);
        for(Picture picture:list){
            Picture pic = new Picture();
            pic.setDesc(picture.getDesc());
            pic.setUrl(picture.getUrl());
            mPictureList.add(0,pic);
        }
        //send message
        mhandler.sendEmptyMessage(1);
    }

    private Handler mhandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==1) {
                //get message
                initView();
            }
        }
    };

    private void initView() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new PictureAdapter(mPictureList,this);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        AnimationSet set = new AnimationSet(false);
        WindowManager wm = getWindowManager();
        int height = wm.getDefaultDisplay().getHeight();
        Animation animation = new TranslateAnimation(0,0,height,0); //translateanimation
        animation.setDuration(1000);
        animation.setInterpolator(new AccelerateInterpolator(1.0F));
        set.addAnimation(animation);
        LayoutAnimationController controller = new LayoutAnimationController(set, 0);
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);//set order；
        controller.setDelay(0.2f);//set LayoutAnimationController；
        recyclerView.setLayoutAnimation(controller);   //set animation
        recyclerView.setAdapter(adapter);


        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFruits();
            }
        });
    }


    private void refreshFruits() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(800);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initPicture(++page);
                        adapter.notifyDataSetChanged();
                        swipeRefresh.setRefreshing(false);
                        EventBus.getDefault().post(
                                new SimpleEventBus(2,"end"));
                    }
                });
            }
        }).start();
    }



    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.about);
                builder.setMessage(R.string.about_message);
                builder.setPositiveButton(R.string.about_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            String pkName = getPackageName();
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse("market://details?id=" + pkName));
                            startActivity(i);
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, R.string.without_market, Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                });
                builder.show();
                break;
            case R.id.github:
                Uri uri = Uri.parse("http://github.com/surine/MaterialTest");
                Intent it = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(it);
                break;
            default:
        }
        return true;
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SimpleEventBus event) {
        if (event.getId() == 1) {
            Snackbar.make(swipeRefresh, R.string.reing, Snackbar.LENGTH_SHORT).show();
            refreshFruits();
        }
    }
}
