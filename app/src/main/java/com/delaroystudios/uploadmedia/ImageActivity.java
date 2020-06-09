package com.delaroystudios.uploadmedia;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.delaroystudios.uploadmedia.networking.ApiConfig;
import com.delaroystudios.uploadmedia.networking.AppConfig;
import com.delaroystudios.uploadmedia.networking.ServerResponse;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImageActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TOKEN = "Bearer eyJhdWQiOiIyIiwianRpIjoiMmViNDUxNDM1ZDllNjhlYjI0MWIyZDY2ZDg0ZTFjY2JlY2I2NDdhNzU1MDExODU3N2NiMTYwNGU2OTQwOWE5ZThhNzk5Yjg2MDIzMmIzMWQiLCJpYXQiOjE1OTE3MzY5ODMsIm5iZiI6MTU5MTczNjk4MywiZXhwIjoxNjIzMjcyOTgzLCJzdWIiOiIyIiwic2NvcGVzIjpbXX0.ii9gfxE7qG8KdHuwaZu5ipeH-Z7UrchvAH_WAC_nUpgtz4oCd9ypWH3rVk2OGYs-j_4pVQjzTu7WigM16B7b51H-KsUzNCwGJe43y7Np9_yJrnHOplvJm_vnKM2ufRWhcRowfuWVATnTwra5TLcBibTogrm60n4YvIdkDOw_-Sug6kE2EWZC4MwaEdDjUKuCkZbo4k0VjkBsayt3VTa9wxQVKi4TJOShrkv1IDUzk0NW487tmo-qJ9id-xV4zGUKxB0_STj7kJDFLMuHj9T9DvvxI6VY-b2sFHmGYtcKIEajPnXyVAXtPesCGtaoGFOS9_kvVh-n2krJMJ13ap1Yye8SAC8tN3AYHiO9x7DDMjGsoBtyoGIfxsnmzw54hGF7pzUW9cdD1Y_qnWfgmEF_bawmWutwvf7jgnAHImGB1IaTF4dWCTq8tfNoqKrqOJRF5t-K31QvYbH0fmNF06yGCpu9nxkY8YYhbAwZnOS1z5yMPgpBm-4ePmY7wWzGboUKU0unmlfGrxwVnvn7Y58B5tn6P2Rtnd2lVgasa54TUogR054jRJSoc-7cJU-weYdvK41TMXH-FrlHJ6jyGgO2dSmSYjRByOuvyJ6-YyiywKaBtdQqpkwkNdsko5uW2_JQiDLr39-NZQhwpyV82ohXOOQhO4s3M594PD6Su7-mHTA";
    ImageView imageView;
    Button pickImage, upload;
    private static final int REQUEST_TAKE_PHOTO = 0;
    private static final int REQUEST_PICK_PHOTO = 2;
    private Uri mMediaUri;
    private static final int CAMERA_PIC_REQUEST = 1111;

    private static final String TAG = ImageActivity.class.getSimpleName();

    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;

    public static final int MEDIA_TYPE_IMAGE = 1;

    private Uri fileUri;

    private String mediaPath;

    private Button btnCapturePicture;

    private String mImageFileLocation = "";
    public static final String IMAGE_DIRECTORY_NAME = "Android File Upload";
    ProgressDialog pDialog;
    private String postPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_layout);

        imageView = (ImageView) findViewById(R.id.preview);
        pickImage = (Button) findViewById(R.id.pickImage);
        upload = (Button) findViewById(R.id.upload);

        pickImage.setOnClickListener(this);
        upload.setOnClickListener(this);
        initDialog();
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.pickImage:
                new MaterialDialog.Builder(this)
                        .title(R.string.uploadImages)
                        .items(R.array.uploadImages)
                        .itemsIds(R.array.itemIds)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                switch (which) {
                                    case 0:
                                        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                        startActivityForResult(galleryIntent, REQUEST_PICK_PHOTO);
                                        break;
                                    case 1:
                                        captureImage();

                                        break;
                                    case 2:
                                        imageView.setImageResource(R.drawable.ic_launcher_background);
                                        break;
                                }
                            }
                        })
                        .show();
                break;
            case R.id.upload:
                uploadFile();
                break;
        }
    }

    private boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO || requestCode == REQUEST_PICK_PHOTO) {
                if (data != null) {
                    // Get the Image from data
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    assert cursor != null;
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    mediaPath = cursor.getString(columnIndex);
                    // Set the Image in ImageView for Previewing the Media
                    imageView.setImageBitmap(BitmapFactory.decodeFile(mediaPath));
                    cursor.close();


                    postPath = mediaPath;
                }


            }else if (requestCode == CAMERA_PIC_REQUEST){
                if (Build.VERSION.SDK_INT > 21) {

                    Glide.with(this).load(mImageFileLocation).into(imageView);
                    postPath = mImageFileLocation;

                }else{
                    Glide.with(this).load(fileUri).into(imageView);
                    postPath = fileUri.getPath();

                }

            }

        }
        else if (resultCode != RESULT_CANCELED) {
            Toast.makeText(this, "Sorry, there was an error!", Toast.LENGTH_LONG).show();
        }
    }




    protected void initDialog() {

        pDialog = new ProgressDialog(this);
        pDialog.setMessage(getString(R.string.msg_loading));
        pDialog.setCancelable(true);
    }


    protected void showpDialog() {

        if (!pDialog.isShowing()) pDialog.show();
    }

    protected void hidepDialog() {

        if (pDialog.isShowing()) pDialog.dismiss();
    }


    /**
     * Launching camera app to capture image
     */
    private void captureImage() {
        if (Build.VERSION.SDK_INT > 21) { //use this if Lollipop_Mr1 (API 22) or above
            Intent callCameraApplicationIntent = new Intent();
            callCameraApplicationIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

            // We give some instruction to the intent to save the image
            File photoFile = null;

            try {
                // If the createImageFile will be successful, the photo file will have the address of the file
                photoFile = createImageFile();
                // Here we call the function that will try to catch the exception made by the throw function
            } catch (IOException e) {
                Logger.getAnonymousLogger().info("Exception error in generating the file");
                e.printStackTrace();
            }
            // Here we add an extra file to the intent to put the address on to. For this purpose we use the FileProvider, declared in the AndroidManifest.
            Uri outputUri = FileProvider.getUriForFile(
                    this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    photoFile);
            callCameraApplicationIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);

            // The following is a new line with a trying attempt
            callCameraApplicationIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Logger.getAnonymousLogger().info("Calling the camera App by intent");

            // The following strings calls the camera app and wait for his file in return.
            startActivityForResult(callCameraApplicationIntent, CAMERA_PIC_REQUEST);
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

            // start the image capture Intent
            startActivityForResult(intent, CAMERA_PIC_REQUEST);
        }


    }

    File createImageFile() throws IOException {
        Logger.getAnonymousLogger().info("Generating the image - method started");

        // Here we create a "non-collision file name", alternatively said, "an unique filename" using the "timeStamp" functionality
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmSS").format(new Date());
        String imageFileName = "IMAGE_" + timeStamp;
        // Here we specify the environment location and the exact path where we want to save the so-created file
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/photo_saving_app");
        Logger.getAnonymousLogger().info("Storage directory set");

        // Then we create the storage directory if does not exists
        if (!storageDirectory.exists()) storageDirectory.mkdir();

        // Here we create the file using a prefix, a suffix and a directory
        File image = new File(storageDirectory, imageFileName + ".jpg");
        // File image = File.createTempFile(imageFileName, ".jpg", storageDirectory);

        // Here the location is saved into the string mImageFileLocation
        Logger.getAnonymousLogger().info("File name and path set");

        mImageFileLocation = image.getAbsolutePath();
        // fileUri = Uri.parse(mImageFileLocation);
        // The file is returned to the previous intent across the camera application
        return image;
    }


    /**
     * Here we store the file url as it will be null after returning from camera
     * app
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }



    /**
     * Receiving activity result method will be called after closing the camera
     * */

    /**
     * ------------ Helper Methods ----------------------
     * */

    /**
     * Creating file uri to store image/video
     */
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * returning image / video
     */
    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + ".jpg");
        }  else {
            return null;
        }

        return mediaFile;
    }

    // Uploading Image/Video
    private void uploadFile() {
        if (postPath == null || postPath.equals("")) {
            Toast.makeText(this, "please select an image ", Toast.LENGTH_LONG).show();
            return;
        } else {
            showpDialog();

            // Map is used to multipart the file using okhttp3.RequestBody
            Map<String, RequestBody> map = new HashMap<>();
            File file = new File(postPath);

            MultipartBody.Builder builder = new MultipartBody.Builder();
            builder.setType(MultipartBody.FORM);

            RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);//RequestBody.create(MediaType.parse("*/*"), file);
            builder.addFormDataPart("title", "Multipart okhttp3");
            builder.addFormDataPart("subject", "Programming");
            builder.addFormDataPart("description", "How to upload Multipart okhttp3 from retrofit");
            builder.addFormDataPart("category", "other");
            builder.addFormDataPart("image", file.getName(), requestBody);

            // Map is used to multipart the file using okhttp3.RequestBody
            // Multiple Images
            //for (int i = 0; i < filePaths.size(); i++) {
            //    File file = new File(filePaths.get(i));
            //builder.addFormDataPart("file[]", file.getName(), RequestBody.create(MediaType.parse("multipart/form-data"), file));
            //}

            MultipartBody multipartBody = builder.build();

            ApiConfig getResponse = AppConfig.getRetrofit().create(ApiConfig.class);
            Call<ServerResponse> call = getResponse.upload(TOKEN, multipartBody);
            call.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                    Log.e("Response", ""+response);
                    Log.e("Response1", ""+response.code());
                    Log.e("Response2", response.message());
                    if (response.isSuccessful()){
                        if (response.body() != null){
                            hidepDialog();
                            ServerResponse serverResponse = response.body();
                            Toast.makeText(getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    }else {
                        hidepDialog();
                        Toast.makeText(getApplicationContext(), "problem uploading image", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    hidepDialog();
                    Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.v("Response gotten is", t.getMessage());
                }
            });
        }
    }


}
