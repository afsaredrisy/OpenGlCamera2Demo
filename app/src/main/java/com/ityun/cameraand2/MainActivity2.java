package com.ityun.cameraand2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.ityun.cameraand2.opengl.CameraGLSurfaceView;

public class MainActivity2 extends AppCompatActivity implements View.OnClickListener {

    private CameraGLSurfaceView cameraView;
    private Button blue,green,red;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        cameraView = findViewById(R.id.mview);
        blue=findViewById(R.id.blue);
        blue.setOnClickListener(this);
        red=findViewById(R.id.red);
        red.setOnClickListener(this);
        green=findViewById(R.id.green);
        green.setOnClickListener(this);
        //cameraView.setMultiplier(1.0f,1.0f,0.0f);

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.blue:
                cameraView.setMultiplier(1.0f,1.0f,0.0f);
                break;
            case  R.id.red:
                cameraView.setMultiplier(0.0f,1.0f,1.0f);
                break;
            case R.id.green:
                cameraView.setMultiplier(1.0f,0.0f,1.0f);
                break;

        }
    }
}
