package cn.androidstudy.multithreaddownfile;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void downFile(View view){
        Intent intent = new Intent(this,DownFileActivity.class);
        startActivity(intent);
    }

    public void multiThreadDownFile(View view){
        Intent intent = new Intent(this,MultiThreadDownActivity.class);
        startActivity(intent);
    }




}
