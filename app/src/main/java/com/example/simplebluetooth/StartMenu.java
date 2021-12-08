package com.example.simplebluetooth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;




public class StartMenu extends Activity {
    Button startActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_menu);
        startActivity = findViewById(R.id.startBtn);
        startActivity.setOnClickListener(v -> openActivity());
    }

    public void openActivity(){
        Intent intent = new Intent(this, DeviceList.class);
        startActivity(intent);
    }
}
