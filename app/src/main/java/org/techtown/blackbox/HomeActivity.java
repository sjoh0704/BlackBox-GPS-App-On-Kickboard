package org.techtown.blackbox;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HomeActivity extends AppCompatActivity {

    String userId, parentNum;
    TextView UserIdView;
    Button btn_gallery, btn_record, btn_end, btn_content;
    private long backKeyPressedTime = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        UserIdView = findViewById(R.id.user_id);

        //home에 데이터값 저장
        if(userId == null) {
            userId = getIntent().getStringExtra("UserId");
            parentNum = getIntent().getStringExtra("UserPPhone");
            UserIdView.setText(userId);
        }


        // 갤러리 버튼
        btn_gallery = findViewById(R.id.btn_gallery);
        btn_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), VideoGallery.class);
                intent.putExtra("UserPPhone", parentNum);
                intent.putExtra("UserId", userId);
                startActivity(intent);
            }
        });

        // 녹화버
        btn_record = findViewById(R.id.btn_record);
        btn_record.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                Log.e("UserID", userId);
                Log.e("parentNum", parentNum);

                intent.putExtra("UserPPhone", parentNum);
                intent.putExtra("UserId", userId);
                startActivity(intent);
            }
        });

        btn_end = findViewById(R.id.btn_end);
        btn_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent exit_intent = new Intent(getApplicationContext(), LoginActivity.class);
                exit_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                clearPref();
                startActivity(exit_intent);

            }
        });

        btn_content = findViewById(R.id.btn_content);
        btn_content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://map-path.paas-ta.org/about.jsp"));
                startActivity(intent);

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreState();
        UserIdView.setText(userId);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            clearPref();
            finish();
            moveTaskToBack(true);						// 태스크를 백그라운드로 이동
            finishAndRemoveTask();						// 액티비티 종료 + 태스크 리스트에서 지우기
            android.os.Process.killProcess(android.os.Process.myPid());	// 앱 프로세스 종료
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


}
