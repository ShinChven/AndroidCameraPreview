package net.atlassc.shinchven.camerapreview;

import android.Manifest;
import android.content.ContentValues;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import net.atlassc.shinchven.camerapreview.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {
    private static final String[] PERMISSION = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    private static final String TAG = "MainActivity";
    private ActivityMainBinding ui;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PermissionsUtil.checkPermissions(this, PERMISSION);
        ui = DataBindingUtil.setContentView(this, R.layout.activity_main);

        setupButtons();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        adjustPreviewRatio();
    }

    private void adjustPreviewRatio() {
        ui.previewScreen.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    ViewGroup.LayoutParams lp = ui.previewScreen.getLayoutParams();
                    int measuredWidth = ui.previewScreen.getMeasuredWidth();
                    lp.height = measuredWidth / 16 * 10;
                    ui.previewScreen.setLayoutParams(lp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 500);
    }

    private void setupButtons() {
        ui.capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Bitmap snapshot = ui.previewTexture.getBitmap();
                    Bitmap bitmap = Bitmap.createBitmap(snapshot, 0, 0, ui.previewScreen.getWidth(), ui.previewScreen.getHeight());
                    String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) +
                            "/CameraPreview/snapshot_" + System.currentTimeMillis() + ".jpg";
                    File file = new File(filePath);
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    FileOutputStream stream = new FileOutputStream(
                            file.toString());
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

                    ContentValues values = new ContentValues();

                    values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    values.put(MediaStore.MediaColumns.DATA, filePath);

                    MainActivity.this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                    Glide.with(MainActivity.this)
                            .load(filePath)
                            .into(ui.img);

                    Snackbar.make(ui.activityMain, filePath, Snackbar.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    // for layout editor preview
                    Snackbar.make(ui.activityMain, "saving error", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        ui.takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ui.previewTexture.startPreview();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ui.previewScreen.getMeasuredWidth() == 0) {
            ui.previewTexture.startPreview();
            adjustPreviewRatio();
        }

        ui.previewTexture.postDelayed(new Runnable() {
            @Override
            public void run() {
                ui.previewTexture.startPreview();
            }
        },1000);


    }

    @Override
    protected void onPause() {
        super.onPause();
        ui.previewTexture.releaseCameraAndPreview();
    }
}
