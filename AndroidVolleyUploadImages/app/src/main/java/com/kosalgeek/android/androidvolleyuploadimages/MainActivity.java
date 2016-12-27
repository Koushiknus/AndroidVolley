package com.kosalgeek.android.androidvolleyuploadimages;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.kosalgeek.android.photoutil.GalleryPhoto;
import com.kosalgeek.android.photoutil.ImageBase64;
import com.kosalgeek.android.photoutil.PhotoLoader;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okio.BufferedSource;

import  com.kosalgeek.android.androidvolleyuploadimages.R;

import org.json.JSONArray;
import org.json.JSONObject;
//import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    ImageView ivGallery, ivUpload;

    GalleryPhoto galleryPhoto;

    final int GALLERY_REQUEST = 1200;

    final String TAG = this.getClass().getSimpleName();

    LinearLayout linearMain;

    public ArrayList<String> imageList = new ArrayList<>();

    ImageView resultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        linearMain = (LinearLayout) findViewById(R.id.linearMain);
        resultView = (ImageView) findViewById(R.id.resultView);

        galleryPhoto = new GalleryPhoto(getApplicationContext());

        ivGallery = (ImageView) findViewById(R.id.ivGallery);
        ivUpload = (ImageView) findViewById(R.id.ivUpload);

        ivGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = galleryPhoto.openGalleryIntent();
                startActivityForResult(in, GALLERY_REQUEST);
            }
        });


        ivUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new UploadImage().execute();
            }

        });
    }

        @Override
        protected void onActivityResult ( int requestCode, int resultCode, Intent data){
            if (resultCode == RESULT_OK) {
                if (requestCode == GALLERY_REQUEST) {
                    galleryPhoto.setPhotoUri(data.getData());
                    String photoPath = galleryPhoto.getPath();
                    imageList.add(photoPath);
                    Log.d(TAG, photoPath);
                    Log.v("number of photos", (imageList.size())+"");
                    try {
                        Bitmap bitmap = PhotoLoader.init().from(photoPath).requestSize(512, 512).getBitmap();

                        ImageView imageView = new ImageView(getApplicationContext());
                        LinearLayout.LayoutParams layoutParams =
                                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT);
                        imageView.setLayoutParams(layoutParams);
                        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        imageView.setPadding(0, 0, 0, 10);
                        imageView.setAdjustViewBounds(true);
                        imageView.setImageBitmap(bitmap);

                        linearMain.addView(imageView);

                    } catch (FileNotFoundException e) {
                        Toast.makeText(getApplicationContext(), "Error while loading image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }


    private class UploadImage extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            for (String imagePath : imageList) {
                try {
                    final MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpg");
                    File file = new File(imagePath);
                    String filename = imagePath.substring(imagePath.lastIndexOf("/"));

                    //String contentType = file.toURL().openConnection().getContentType();
                    //Log.v("ContentType",contentType);
                    RequestBody fileBody = RequestBody.create(MEDIA_TYPE_JPG, file);
                    //final String filename = "file_" + System .currentTimeMillis() / 1000L;
                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("user_id", "1")
                            .addFormDataPart("userfile", filename , fileBody)
                            .build();

                    okhttp3.Request request = new okhttp3.Request.Builder()
                            .url("http://192.168.0.104:8086/dcdrp/api/imageProcessing/uploadSan")
                            .post(requestBody)
                            .build();
                    OkHttpClient okHttpClient = new OkHttpClient();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, final IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "nah1", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onResponse(Call call, final okhttp3.Response response) throws IOException {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Log.v("Response", response.toString());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }

                    });

                    //    myCommand.add(stringRequest);

                } catch (Exception e) {
                    e.printStackTrace();
                }


                // myCommand.execute();
            }

            try
            {
                Thread.sleep(5000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
//            final Handler handler = new Handler();
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    //Do something after 100ms
                    startListening();
//                }
//            }, 5000);
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new  DownloadImage().execute();
        }

        private void startListening(){
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url("http://192.168.0.104:8086/dcdrp/api/imageProcessing/result")
                    .get()
                    .build();
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .build();
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "nah2", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, final okhttp3.Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Log.v("Response_listening", response.toString());
                                okhttp3.ResponseBody in = response.body();
                                String jsonData = in.string();
                                JSONObject Jobject = new JSONObject(jsonData);
                                Log.v("json", jsonData);
                                getImages(Jobject);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

            });



        }

        private void getImages(JSONObject Jobject) {

            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url("http://192.168.0.104:8086/dcdrp/api/imageProcessing/report/result.jpg")
                    .get()
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .build();

            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "nah3", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, final okhttp3.Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                int count;
                                Log.v("Response_imgggggg", response.toString());
                                okhttp3.ResponseBody in = response.body();
                                System.out.println("Response body = " + in);
                                InputStream is = in.byteStream();
                                System.out.print("byte is " + is.available());


                                File file = new File(Environment.getExternalStorageDirectory() + File.separator + "test.jpg");
                                Log.v("pathhhh", Environment.getExternalStorageDirectory() + File.separator + "test.jpg");
                                OutputStream fOut = null;
                                Integer counter = 0;
                                //File file = new File("FitnessGirl"+counter+".jpg"); // the File to save , append increasing numeric counter to prevent files from getting overwritten.
                                //file.createNewFile();
                                fOut = new FileOutputStream(file);

                                Bitmap bitmap =BitmapFactory.decodeStream(is); // obtaining the Bitmap
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
                                fOut.flush(); // Not really required
                                fOut.close(); // do not forget to close the stream

                               //MediaStore.Images.Media.insertImage(getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());


                               // System.out.println("bytes lenght " + (in.bytes()).length);
                                //BufferedInputStream input = new BufferedInputStream(is);
                                //OutputStream output = new FileOutputStream(new File("/storage/emulated/0/DCIM/Camera/result.jpg"));

                                //byte[] data = new byte[in.bytes().length];


                                //long total = 0;


                             /*   while ((count = input.read(data)) != -1) {
                                    total += count;
                                    output.write(data, 0, count);
                                }*/
                               /* output.flush();
                                output.close();
                                input.close();
*/
                                System.out.println("Content type " + in.contentType());




//                                Bitmap bitmap2 = BitmapFactory.decodeByteArray(array, 0, array.length);

//                                System.out.println("Bitmap image = " + bitmap2);
                               // updateImage(bitmap);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

            });
        }

        /*private void updateImage(final Bitmap bitmap){
            Log.i(TAG,"updating image");
            System.out.println("bitmap = " + bitmap);

            resultView.setImageBitmap(bitmap);
//            resultView.post(new Runnable() {
//                @Override
//                public void run() {
//                    if(bitmap!=null){
//                        resultView.setImageBitmap(bitmap);
//
//                    }else{
//                        //  resultView.setImageDrawable(getResources().getDrawable(R.drawable.));
//                    }
//                }
//            });
        }*/
    }
    private class DownloadImage extends AsyncTask<Void, Void, Bitmap> {


        @Override
        protected Bitmap doInBackground(Void... voids) {
            Bitmap bitmap = null;
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url("http://api.androidhive.info/images/sample.jpg")
                    .get()
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .addInterceptor(new Interceptor() {
                        @Override
                        public okhttp3.Response intercept(Chain chain) throws IOException {
                            okhttp3.Request request = chain.request();
                            Log.v("chain req", String.valueOf(request.url()));
                            okhttp3.Response response = null;
                            boolean responseOK = false;
                            int tryCount = 0;
                            //response = chain.proceed(request);

                            while (!responseOK) {
                                try {
                                    response = chain.proceed(request);
                                    //Log.v("response while", response.message());
                                    responseOK = response.code()==200;
                                    Log.v("responseook", String.valueOf(responseOK));
                                    Log.v("responseook222", String.valueOf(response.code()));
                                    Thread.sleep(1000);
                                }catch (Exception e){
                                    e.printStackTrace();
                                    Log.d("intercept", "Request is not successful - " + tryCount);
                                }finally{
                                    tryCount++;
                                }
                            }



                            // otherwise just pass the original response on
                            return response;
                        }
                    })
                    .build();

            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "nah3", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, final okhttp3.Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                int count;
                                Log.v("Response_imgggggg", response.toString());
                                okhttp3.ResponseBody in = response.body();
                                System.out.println("Response body = " + in);
                                InputStream is = in.byteStream();
                                System.out.print("byte is " + is.available());


                                File file = new File(Environment.getExternalStorageDirectory() + File.separator + "test.jpg");
                                Log.v("pathhhh", Environment.getExternalStorageDirectory() + File.separator + "test.jpg");
                                OutputStream fOut = null;
                                Integer counter = 0;
                                //File file = new File("FitnessGirl"+counter+".jpg"); // the File to save , append increasing numeric counter to prevent files from getting overwritten.
                                //file.createNewFile();
                                fOut = new FileOutputStream(file);

                                Bitmap bitmap =BitmapFactory.decodeStream(is);
                                // obtaining the Bitmap
                                Log.v("BitMapp",bitmap.toString());
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
                                fOut.flush(); // Not really required
                                fOut.close(); // do not forget to close the stream
                                Log.v("BitMapp2",bitmap.toString());
                                //MediaStore.Images.Media.insertImage(getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());
                                // System.out.println("bytes lenght " + (in.bytes()).length);
                                //BufferedInputStream input = new BufferedInputStream(is);
                                //OutputStream output = new FileOutputStream(new File("/storage/emulated/0/DCIM/Camera/result.jpg"));

                                //byte[] data = new byte[in.bytes().length];


                                //long total = 0;


                             /*   while ((count = input.read(data)) != -1) {
                                    total += count;
                                    output.write(data, 0, count);
                                }*/
                               /* output.flush();
                                output.close();
                                input.close();
*/
                                System.out.println("Content type " + in.contentType());

                                if(bitmap!=null)
                                    resultView.setImageBitmap(bitmap);

//                                Bitmap bitmap2 = BitmapFactory.decodeByteArray(array, 0, array.length);

//                                System.out.println("Bitmap image = " + bitmap2);
                                // updateImage(bitmap);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

            });
            return bitmap;
        }


    }
    }

