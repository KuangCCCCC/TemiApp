package com.example.temiapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;

import com.example.cameraxlib.CameraXHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FaceCaptureActivity extends AppCompatActivity {

    private CameraXHelper cameraXHelper;
    private PreviewView previewView;
    private boolean hasProceeded = false; // 確保只處理一次
    private ExecutorService executorService; // 用來執行 Firebase 監聽

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_capture);

        previewView = findViewById(R.id.previewView); // 對應 XML 中的 PreviewView

        // 初始化 CameraXHelper
        cameraXHelper = new CameraXHelper(this);
        cameraXHelper.setPreviewView(previewView);

        // 初始化 ExecutorService
        executorService = Executors.newSingleThreadExecutor();

        // 啟動相機
        cameraXHelper.startCamera(new CameraXHelper.CameraStartListener() {
            @Override
            public void onCameraStarted() {
                Log.d("FaceCaptureActivity", "相機啟動成功");
                // 延遲 2 秒後自動拍照
                previewView.postDelayed(() -> cameraXHelper.capturePhoto(), 2000);
            }

            @Override
            public void onCameraError(Exception e) {
                Log.e("FaceCaptureActivity", "相機啟動失敗: " + e.getMessage());
            }
        });
        // 在背景線程中監聽 Firebase 資料
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                monitorIdentifyValue(); // 呼叫 Firebase 監聽
            }
        });
    }
    private void monitorIdentifyValue() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("12"); // 假設你的資料在 "12" 下
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String identifyValue = dataSnapshot.child("identify").getValue(String.class);
                Log.d("FaceCaptureActivity", "Identify value: " + identifyValue);

                if (identifyValue != null && !hasProceeded) {
                    if ("success".equals(identifyValue)) {
                        // 顯示提示
                        Toast.makeText(FaceCaptureActivity.this, "辨識成功！", Toast.LENGTH_SHORT).show();

                        // 根據 "success" 進行後續操作（例如跳轉頁面或播放影片）
                        navigateToNextPage();

                        // 停止監聽
                        hasProceeded = true;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FaceCaptureActivity", "Failed to read value.", databaseError.toException());
            }
        });
    }
    private void navigateToNextPage() {
        // 根據需要跳轉到下個頁面，這裡假設是 VideoPlaybackActivity
        Intent intent = new Intent(FaceCaptureActivity.this, VideoPlaybackActivity.class);
        startActivity(intent);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 釋放 CameraX 資源
        if (cameraXHelper != null) {
            cameraXHelper.releaseResources();
        }
        // 關閉 ExecutorService
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}

