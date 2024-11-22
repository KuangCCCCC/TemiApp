package com.example.temiapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button navigateButton;
    private Button patrolButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    // 第一個按鈕點擊事件：跳轉到 FaceCaptureActivity
    public void onNavigateButtonClicked(View view) {
        Intent intent = new Intent(MainActivity.this, FaceCaptureActivity.class);
        startActivity(intent);
    }

    // 第二個按鈕點擊事件：跳轉到 PatrolActivity 並開始巡邏
    public void onPatrolButtonClicked(View view) {
        Intent intent = new Intent(MainActivity.this, PatrolActivity.class);
        startActivity(intent);

        // 開始巡邏（假設你有一個可以啟動巡邏的地方）
        // 這裡可以調用 PatrolHelper 的方法來啟動巡邏
        // 例如：patrolHelper.startPatrolling();
    }
}



