package org.techtown.blackbox;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.kakao.usermgmt.response.model.User;
import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Settings;
import com.klinker.android.send_message.Transaction;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class VideoActivity extends AppCompatActivity implements SurfaceHolder.Callback, View.OnClickListener, SensorEventListener {

    private static final String TAG = "AccelerometerActivity";
    private SensorManager sensorManager;
    private Sensor senAccelerometer;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private int SHAKE_THRESHOLD = 7000;
    private int COLLISION_THRESHOLD = 30000;
    private boolean collision;

    private Camera camera;
    private MediaRecorder mediaRecorder;
    private Button btn_record, btn_upload;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private boolean recording = false;
    private String filename = null;
    private String dirPath;
    private Button btn_send;

    private String phoneNum = null;
    private ProgressDialog progressDialog;
    private GpsTracker gpsTracker;
    private String userId;
    private String parentNum;
    private long startTime;
    private long endTime;
    private ContentValues addRowValue;
    private static final String URL = "http://ssoong-ssoong.paas-ta.org/accidentGps";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int VIDEO_REQUEST_CODE = 1000;

    private FloatingActionButton fab_logout, fab_home, fab_gallery;
    private FloatingActionMenu fabMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        clearPref();

        //startService(new Intent(this, UnCatchTask.class));
        setContentView(R.layout.activity_video);
        allowPermission();  //Ted permission으로 권한 얻어오기
        makeDir();
        if(userId == null) {
            userId = getIntent().getStringExtra("UserId");
            parentNum = getIntent().getStringExtra("UserPPhone");

        }


        Log.e("userID: ", userId);
        Log.e("parentNum: ", parentNum);

        collision = false;
        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        senAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);


        btn_record = (Button)findViewById(R.id.btn_record);
        btn_upload = findViewById(R.id.btn_upload);
        btn_send = findViewById(R.id.btn_send);

        btn_record.setOnClickListener(this);
        btn_upload.setOnClickListener(this);
        btn_send.setOnClickListener(this);


        surfaceView = findViewById(R.id.surfaceView);
        fabMenu = findViewById(R.id.fab_menu);
        fab_gallery = findViewById(R.id.fab_gallery);
        fab_home = findViewById(R.id.fab_home);
        fab_logout = findViewById(R.id.fab_logout);
        fab_gallery.setOnClickListener(this);
        fab_logout.setOnClickListener(this);
        fab_home.setOnClickListener(this);
        fabMenu.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(boolean opened) {
                if(opened){
//                    Toast.makeText(VideoActivity.this, "MENU OPEN", Toast.LENGTH_SHORT).show();
                }
                else{
//                    Toast.makeText(VideoActivity.this, "MENU CLOSED", Toast.LENGTH_SHORT).show();

                }


            }
        });


        addRowValue = new ContentValues();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("업로드 중입니다.\n 잠시만 기다려주세요...");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Horizontal);

        // 갖고 있던 상태지우고 다시 쓰기

        saveState();



    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        if(mySensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    Log.d("x, y, z: ", x + ", " + y + ", " + z);
//                }
//            },500);

            long curTime = System.currentTimeMillis(); // 현재시간
            if((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;
                double collision_detect = Math.sqrt( Math.pow(z - last_z,2)*100 + Math.pow(x-last_x,2)*10+  Math.pow(y-last_y,2)*10)/ diffTime * 10000;
                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

//                Log.e("Speed: ", String.valueOf(speed));
//
//                Log.e("collision: ", String.valueOf(collision_detect));
                if (speed > SHAKE_THRESHOLD || collision_detect >COLLISION_THRESHOLD) {
                    //지정된 수치이상 흔들림이 있으면 실행
                    Log.e("Speed", "흔들림 감지");

                    if(recording){
                        collision = true;
                        mediaRecorder.stop();
                        mediaRecorder.release();
                        camera.lock();
                        mediaRecorder=null;
                        recording  = false;
                        btn_upload.callOnClick();
                        btn_send.callOnClick();

                        send_collision_info();




                    }

                } else if (speed < 10) {

                }

                //갱신
                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    @Override
    protected void onPause() {
        super.onPause();
//        saveState();
        sensorManager.unregisterListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreState();
        sensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void makeDir() {
        String str = Environment.getExternalStorageState();
        if ( str.equals(Environment.MEDIA_MOUNTED)) {

            dirPath = getApplicationContext().getFilesDir().getAbsolutePath() + "/blackbox";
//            dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/BlackBox";

            File file = new File(dirPath);
            if( !file.exists() )  // 원하는 경로에 폴더가 있는지 확인
            {
                Log.e("TAG : ", "디렉토리 생성");
                file.mkdirs();
            }
            else Log.e("TAG : ", "디렉토리 이미존재");

        }
        else
            Toast.makeText(this, "SD Card 인식 실패", Toast.LENGTH_SHORT).show();
    }

    private void allowPermission() {

        TedPermission.with(this)
                .setPermissionListener(permission)
                .setRationaleMessage("필요한 권한을 허용해주세요.")
                .setDeniedMessage("권한이 거부되었습니다.")
                .setPermissions(Manifest.permission.CAMERA,  Manifest.permission.RECORD_AUDIO, Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.READ_SMS )
                .check();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(recording){
            mediaRecorder.stop();
            mediaRecorder.release();
            camera.lock();
            recording  = false;

//            Toast.makeText(this, "업로드중입니다.", Toast.LENGTH_SHORT).show();

            btn_upload.callOnClick();
            btn_send.callOnClick();
        }
    }






    // permission
    PermissionListener permission = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            //Toast.makeText(MainActivity.this, "권한 허가", Toast.LENGTH_SHORT).show();
            Log.e("TAG", "권한 허가");
            camera = Camera.open();
            camera.setDisplayOrientation(90);
            surfaceHolder = surfaceView.getHolder();
            surfaceHolder.addCallback(VideoActivity.this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(VideoActivity.this, "권한 거부이 거부되었습니다. 설정 -> 권한 허용", Toast.LENGTH_SHORT).show();
        }
    };
    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        refreshCamera(camera);
    }

    private void refreshCamera(Camera camera) {
        if(surfaceHolder.getSurface() == null){
            return ;
        }
        try {
            camera.stopPreview();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        setCamera(camera);


    }

    private void setCamera(Camera cam) {
        camera = cam;

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){

            case R.id.btn_record:

                if(recording){
                    endTime = System.currentTimeMillis();

                    if(endTime-startTime > 1000) {
                        mediaRecorder.stop();
                        mediaRecorder.release();
                        mediaRecorder = null;
                        camera.lock();
                        recording = false;

                        btn_upload.callOnClick();
                        btn_send.callOnClick();
                    }

//                    Toast.makeText(VideoActivity.this, "업로드중입니다.", Toast.LENGTH_SHORT).show();




                }
                else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startTime = System.currentTimeMillis();
                            Log.e("userID: ", userId);
                            Log.e("parentNum: ", parentNum);
                            Log.e("parentNum: ", String.valueOf(parentNum.length()));
                            if(parentNum.length() == 11 || parentNum.length() == 0){
                                phoneNum = parentNum;
                                //과부화도 덜되고 동영상 처리는 여기서 하는게 좋다
                                Toast.makeText(VideoActivity.this, "녹화를 시작합니다!", Toast.LENGTH_SHORT).show();
                                try {
                                    Log.e("시작:", "1");

                                    mediaRecorder = new MediaRecorder();
                                    camera.unlock();
                                    mediaRecorder.setCamera(camera);
                                    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                                    mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                                    mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));
                                    mediaRecorder.setOrientationHint(90);

                                    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmm_ss");
                                    Date now = new Date();
                                    filename =  userId + "_"+ formatter.format(now) + ".mp4";

                                    Log.e("시작:", "2");
                                    Log.e("파일저장: ", dirPath +"/"+ filename);
                                    mediaRecorder.setOutputFile(dirPath +"/"+ filename);
                                    mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());

                                    try {
                                        mediaRecorder.prepare();
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                    mediaRecorder.start();
                                    recording = true;


                                    Log.e("시작:", "3");

                                    MediaScanner ms = MediaScanner.newInstance(VideoActivity.this);
                                    try {  ms.mediaScanning(dirPath + "/" + filename); }
                                    catch (Exception e) { e.printStackTrace(); Log.d("MediaScan", "ERROR" + e); }
                                    finally { }
//                                galleryAddPic();
                                    gpsTracker = new GpsTracker(VideoActivity.this);
                                    double latitude = gpsTracker.getLatitude();
                                    double longtitude = gpsTracker.getLongitude();
                                    String address = getCurrentAddress(latitude, longtitude);

                                    String start_msg = "[슝슝] 등록된 사용자가 서비스를 이용합니다.\n현재 사용자의 위치는 "+address.trim()+" 입니다.";
//
                                    if(phoneNum.length()== 11)
                                        sendMMS(phoneNum, start_msg);

                                    Log.e("시작:", "4");


                                }catch (Exception e){
                                    Log.e("error", e.toString());
                                    e.printStackTrace();
                                    mediaRecorder.release();
                                }}
                            else{


                                Toast.makeText(VideoActivity.this, "유효하지 않은 전화번호", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }


                break;
            case R.id.btn_upload:
                if(filename != null) {

                    try {
                        Log.e("파일명 확인: ", filename);

                        FirebaseStorage storage = FirebaseStorage.getInstance();

                        Uri file = Uri.fromFile(new File(dirPath +"/"+ filename));

                        StorageReference storageRef = storage.getReferenceFromUrl("gs://sodium-inverter-294315.appspot.com").child("Ssoong_Ssoong/" + filename);
                        //storage url 적는란


                        Log.e("URi 확인: ", String.valueOf(file));
                        storageRef.putFile(file)
                                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                                        double progress = (100.0 * taskSnapshot.getBytesTransferred());
                                        try {
                                            progressDialog.show();
                                            if(progress >= 100)
                                                progressDialog.dismiss();
                                        }
                                        catch (Exception e){
                                            e.printStackTrace();
                                        }

                                    }
                                })

                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                                        Toast.makeText(VideoActivity.this, "업로드 성", Toast.LENGTH_SHORT).show();
                                        filename = null;
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(VideoActivity.this, "Uploading fails", Toast.LENGTH_SHORT).show();

                                    }
                                });

                    }
                    catch (Exception e){
                        Toast.makeText(this, "connection to firebase fails", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }


                }
                else{
                    Toast.makeText(VideoActivity.this, "업로드할 파일이 없습니다.", Toast.LENGTH_SHORT).show();
                }
                break;


            case R.id.btn_send:
                gpsTracker = new GpsTracker(this);
                double latitude = gpsTracker.getLatitude();
                double longtitude = gpsTracker.getLongitude();
                String address = getCurrentAddress(latitude, longtitude);
//                String location = "\n위도: " + latitude + "\n경도: " + longtitude + "\n";
                String URL = "http://Ssoong-Ssoong.paas-ta.org/path?id=" + userId;
                String msg;
                if(collision){
                    msg = "[슝슝] 현재 사용자에게 사고가 발생하였습니다.\n" +
                            "현 사용자 위치는 "+address.trim()+" 입니다.\n" +
                            "링크를 통해 경로를 확인할 수 있습니다.\n" + URL  ;

                    collision= false;
                }
                else{
                    msg = "[슝슝] 사용자가 안전하게 서비스를 이용하였습니다.\n" +
                            "사용자의 위치는 "+address.trim()+" 입니다.\n" +
                            "링크를 통해 경로를 확인할 수 있습니다.\n" + URL  ;}



                if(phoneNum.length() == 11)
                    sendMMS(phoneNum, msg );

//                sendSms(phoneNum, msg + URL);
                phoneNum = null;
//                Toast.makeText(this, "메시지 자동 전송", Toast.LENGTH_SHORT).show();

                break;
            case R.id.fab_gallery:
                Intent intent = new Intent(getApplicationContext(), VideoGallery.class);

                startActivity(intent);
//                Intent intent = new Intent(Intent.ACTION_PICK);
//                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                intent.setType("video/*");
//                startActivityForResult(intent, VIDEO_REQUEST_CODE);

                break;

            case R.id.fab_logout:
                Intent exit_intent = new Intent(getApplicationContext(), LoginActivity.class);
                clearPref();
                exit_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(exit_intent);
                break;
            case R.id.fab_menu:
                if(fabMenu.isOpened()){
                    fabMenu.close(true);
                }
                break;
            case R.id.fab_home:
                Intent intent1 = new Intent(getApplicationContext(), HomeActivity.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent1);

        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("getUserID: ", userId);
        Log.d("getUserParentNumber: ", parentNum);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(recording){
            mediaRecorder.stop();
            mediaRecorder.release();
            camera.lock();
            recording  = false;

//            Toast.makeText(this, "업로드중입니다.", Toast.LENGTH_SHORT).show();

            btn_upload.callOnClick();
            btn_send.callOnClick();
        }


    }

    private void sendSms(String phoneNum, String msg) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNum, null, msg, null, null);


    }


    public String getCurrentAddress( double latitude, double longitude) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }



        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";

    }


    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(VideoActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:
                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
//                        checkRunTimePermission();
                        return;
                    }
                }

                break;
            case VIDEO_REQUEST_CODE:
                try {
                    Uri uri = intent.getData();
                    Intent video_intent = new Intent(getApplicationContext(), VideoGallery.class);
                    video_intent.putExtra("videoUri", uri);
                    startActivity(video_intent);
////
//                    Uri uri = intent.getData();
//                    Intent video_intent = new Intent(getApplicationContext(), VideoGallery.class);
//                    video_intent.putExtra("videoUri", uri);
//                    startActivity(video_intent);

                }
                catch (Exception e){
                    e.printStackTrace();

                }

                break;
        }
    }


    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }





    protected void saveState(){
        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("id", userId);
        editor.putString("num", parentNum);
        editor.commit();


    }
    protected void restoreState(){
        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        if((pref!=null) && (pref.contains("id"))){
            userId = pref.getString("id", "");
        }
        if((pref!=null) && (pref.contains("num"))){
            parentNum = pref.getString("num", "");
        }
    }
    protected void clearPref(){
        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        userId = null;
        parentNum = null;
        editor.commit();
    }

    public void sendMMS(String phone,String text) {

        Log.d(TAG, "sendMMS(Method) : " + "start");

//        String subject = "제목";


        // 예시 (절대경로) : String imagePath = "/storage/emulated/0/Pictures/Screenshots/Screenshot_20190312-181007.png";
//        String imagePath = "이미지 경로";

//        Log.d(TAG, "subject : " + subject);
        Log.d(TAG, "text : " + text);
//        Log.d(TAG, "imagePath : " + imagePath);

        Settings settings = new Settings();
        settings.setUseSystemSending(true);

        // TODO : 이 Transaction 클래스를 위에 링크에서 다운받아서 써야함
        Transaction transaction = new Transaction(this, settings);

        // 제목이 있을경우
//        Message message = new Message(text, phone, subject);

        // 제목이 없을경우
        Message message = new Message(text, phone);

        long id = android.os.Process.getThreadPriority(android.os.Process.myTid());

        transaction.sendNewMessage(message, id);

    }

    private void send_collision_info(){



        gpsTracker = new GpsTracker(this);
        String col_latitude = String.valueOf(gpsTracker.getLatitude());
        String col_longitude = String.valueOf(gpsTracker.getLongitude());



        addRowValue.put("userID", userId);
        addRowValue.put("collLongitude", col_longitude);
        addRowValue.put("collLatitude", col_latitude);
        NetworkTask networkTask = new NetworkTask(URL, addRowValue);
        networkTask.execute();

    }



    public class NetworkTask extends AsyncTask<Void, Void, String> {

        private String url;
        private ContentValues values;

        public NetworkTask(String url, ContentValues values) {

            this.url = url;
            this.values = values;
        }

        @Override
        protected String doInBackground(Void... params) {

            String result; // 요청 결과를 저장할 변수.
            RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
            result = requestHttpURLConnection.request(url, values); // 해당 URL로 부터 결과물을 얻어온다.

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            //doInBackground()로 부터 리턴된 값이 onPostExecute()의 매개변수로 넘어오므로 s를 출력한다.
        }
    }
}
