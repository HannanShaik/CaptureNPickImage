package com.hannan.sample.pickncaptureimage;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.apache.commons.io.IOUtils;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_TAKE_PICTURE = 1001;
    private static final int REQUEST_CODE_PICK_GALLERY = 2001;
    private File mFileTemp;
    private ImageView choosenImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        choosenImage = (ImageView) findViewById(R.id.choosen_image);
        File path = new File(getCacheDir(), "images");
        if (!path.exists()) path.mkdirs();
        mFileTemp = new File(path, "sample_image.jpg");
    }

    public void takePicture(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            Uri imageUri = FileProvider.getUriForFile(this, "com.hannan.sample.pickncaptureimage", mFileTemp);
            takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageUri);
            takePictureIntent.putExtra("return-data", true);
            List<ResolveInfo> resolvedIntentActivities = this.getPackageManager().queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {
                String packageName = resolvedIntentInfo.activityInfo.packageName;
                this.grantUriPermission(packageName, imageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            startActivityForResult(takePictureIntent, REQUEST_CODE_TAKE_PICTURE);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void choosePicture(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT).setType("image/*");
        try {
            startActivityForResult(intent, REQUEST_CODE_PICK_GALLERY);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        super.onActivityResult(requestCode, resultCode, result);
        if (requestCode == REQUEST_CODE_TAKE_PICTURE) {
            if (resultCode == RESULT_OK) {
                mFileTemp = new File(mFileTemp.getPath());
                Bitmap myBitmap = BitmapFactory.decodeFile(mFileTemp.getAbsolutePath());
                choosenImage.setImageBitmap(myBitmap);
            } else {
                //failed or cancelled
                Log.e(MainActivity.class.getCanonicalName(), "User cancelled");
                return;
            }
        } else if (requestCode == REQUEST_CODE_PICK_GALLERY) {
            if (resultCode == RESULT_OK) {
                if (result == null || result.getData() == null) {
                    //error
                    Log.e(MainActivity.class.getCanonicalName(), "Result is null");
                    return;
                }
                try {
                    Uri URI = result.getData();
                    InputStream inputStream = getContentResolver().openInputStream(URI);
                    if (inputStream == null) {
                        Log.e(MainActivity.class.getCanonicalName(), "Error");
                        return;
                    }
                    FileOutputStream fileOutputStream = null;
                    fileOutputStream = new FileOutputStream(mFileTemp);
                    IOUtils.copy(inputStream, fileOutputStream);

                    mFileTemp = new File(mFileTemp.getPath());
                    Bitmap myBitmap = BitmapFactory.decodeFile(mFileTemp.getAbsolutePath());
                    choosenImage.setImageBitmap(myBitmap);
                    closeSilently(fileOutputStream);
                    closeSilently(inputStream);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                Log.e(MainActivity.class.getCanonicalName(), "User cancelled");
                return;
            }
        }
    }

    public static void closeSilently(Closeable c) {
        if (c == null) return;
        try {
            c.close();
        } catch (Throwable t) {
            // do nothing
        }
    }

}
