package com.example.materialtest.UI;

import android.app.WallpaperManager;
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
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bm.library.PhotoView;
import com.bumptech.glide.Glide;
import com.example.materialtest.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class PictureActivity extends AppCompatActivity {
    private static final int _SUCCESSED = 1;
    private static final int _FAILED = 2;
    private OkHttpClient client;
    Intent intent;
    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);

        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setNavigationBarColor(Color.TRANSPARENT);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        intent = getIntent();
        setTitle(intent.getStringExtra("name"));

        PhotoView photoView = (PhotoView) findViewById(R.id.picture_big);
        photoView.enable();
        Glide.with(this).load(intent.getStringExtra("url")).into(photoView);



    }


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

    private void asyncDown() {
        Snackbar.make(toolbar, R.string.start_loading, Snackbar.LENGTH_SHORT).show();
        client = new OkHttpClient();
        final Request request = new Request.Builder().get()
                .url(intent.getStringExtra("url"))
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
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //notify the Photo album
                Intent intent =
                        new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri uri = Uri.fromFile(new File(filename));
                intent.setData(uri);
                sendBroadcast(intent);
                Snackbar.make(toolbar, "图片储存路径："+f.getAbsolutePath(), Snackbar.LENGTH_LONG).show();
            }
}
