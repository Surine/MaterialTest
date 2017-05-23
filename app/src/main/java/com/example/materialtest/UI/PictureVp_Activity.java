package com.example.materialtest.UI;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.materialtest.Bean.Picture;
import com.example.materialtest.EventBus.SimpleEventBus;
import com.example.materialtest.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by surine on 2017/5/23.
 */

public class PictureVp_Activity extends AppCompatActivity {
    private static final int _SUCCESSED = 1;
    private static final int _FAILED = 2;
    private OkHttpClient client;
    Intent intent;
    private ViewPager mViewPager;
    Toolbar toolbar;
    private static final String URL = "URL";
    private static final String TITLE = "TITLE";
    private static final String LIST = "LIST";
    String title;
    String picture_url;
    List<Picture> mlist = new ArrayList<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpager);

        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setNavigationBarColor(Color.TRANSPARENT);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        EventBus.getDefault().register(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //set viewpager
        mViewPager = (ViewPager) findViewById(R.id.vp_picture);
        intiData();


        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                if(position==0){
                    EventBus.getDefault().post(
                            new SimpleEventBus(1,"update"));
                }
                Picture picture = mlist.get(position);
                setTitle(picture.getDesc());
                return Picture_Fragment.getInstance(picture.getUrl());
            }

            @Override
            public int getCount() {
                return mlist.size();
            }
        });

        //Record last browse location
        for(int i=0;i<mlist.size();i++){
            if(mlist.get(i).getDesc().equals(title)){
                mViewPager.setCurrentItem(i);
                break;
            }
        }
    }

    private void intiData() {
        //get the value of the intent tag
        title = getIntent().getStringExtra(TITLE);
        picture_url = getIntent().getStringExtra(URL);
        mlist = (List<Picture>) getIntent().getSerializableExtra(LIST);
    }


    //menu
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar2, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();break;
            case R.id.setPaper:
                asyncDown();break;

        }
        return super.onOptionsItemSelected(item);
    }

    //download the bitmap
    private void asyncDown() {
        Snackbar.make(toolbar, R.string.start_loading, Snackbar.LENGTH_SHORT).show();
        client = new OkHttpClient();
        final Request request = new Request.Builder().get()
                .url(picture_url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                Message message = handler.obtainMessage();
                if (response.isSuccessful()) {
                    message.what =_SUCCESSED ;
                    message.obj = response.body().bytes();
                    handler.sendMessage(message);
                } else {
                    handler.sendEmptyMessage(_FAILED);
                }
            }
        });
    }

    //my handler
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case _SUCCESSED:
                    Snackbar.make(toolbar, R.string.success, Snackbar.LENGTH_SHORT).show();
                    byte[] bytes = (byte[]) msg.obj;
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    SetWallpaper(bitmap);
                    break;
                case _FAILED:
                    Snackbar.make(toolbar, R.string.fail, Snackbar.LENGTH_SHORT).show();
                    break;

            }
        }
    };

    //the method for set_wallpaper
    //we should give it a parameter:bitmap
    private void SetWallpaper(Bitmap bitmap) {
        WallpaperManager mWallManager = WallpaperManager.getInstance(this);
        try {
            mWallManager.setBitmap(bitmap);
            Snackbar.make(toolbar, R.string.set_success, Snackbar.LENGTH_SHORT).show();
            saveBitmap(bitmap);
        } catch (IOException e) {
            Snackbar.make(toolbar, R.string.set_fail, Snackbar.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    //save the picture
    private void saveBitmap(final Bitmap bitmap) {
        String filename = UUID.randomUUID() + ".png";
        FileOutputStream fOut = null;
        File f = new File(Environment.getExternalStorageDirectory(), filename);
        try {
            f.createNewFile();
            fOut = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
            //notify the Photo album
            Intent intent =
                    new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri uri = Uri.fromFile(new File(filename));
            intent.setData(uri);
            sendBroadcast(intent);
            Snackbar.make(toolbar, getString(R.string.url)+f.getAbsolutePath(), Snackbar.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Snackbar.make(toolbar, R.string.no_permi, Snackbar.LENGTH_SHORT).show();
        }
    }


    //config intent
    public static Intent newIntent(Context context, String url, String title, List<Picture> fruitList){
        Intent intent = new Intent(context,PictureVp_Activity.class);
        intent.putExtra(URL,url);
        intent.putExtra(TITLE,title);
        intent.putExtra(LIST, (Serializable) fruitList);
        return intent;
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        EventBus.getDefault().unregister(this);//反注册EventBus
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SimpleEventBus event) {
        if (event.getId() == 2) {
            Snackbar.make(toolbar, R.string.re_success, Snackbar.LENGTH_SHORT).show();
            intiData();
            mViewPager.getAdapter().notifyDataSetChanged();
        }
    }
}
