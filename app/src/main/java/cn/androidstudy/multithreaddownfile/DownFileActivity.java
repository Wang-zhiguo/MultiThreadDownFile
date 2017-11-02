package cn.androidstudy.multithreaddownfile;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownFileActivity extends AppCompatActivity {
    private EditText et_path;
    private ProgressBar pb_jindu;
    private long totalSize = 0;
    private double downSize = 0.0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_down_file);

        et_path = (EditText)findViewById(R.id.editText);
        pb_jindu = (ProgressBar)findViewById(R.id.progressBar);
    }

    public void downFile(View view){
        final String path = et_path.getText().toString();
        // 1本地创建一个文件大小与服务器资源大小一样
        new Thread() {
            public void run() {
                try {
                    URL url = new URL(path);
                    HttpURLConnection conn = (HttpURLConnection) url
                            .openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(3000);
                    conn.setReadTimeout(2000);
                    // 获取服务器资源文件的大小
                    long contentLength = conn.getContentLength();
                    if (contentLength <= 0) {
                        return;
                    }
                    // 使用Handler发送消息更改界面
                    totalSize = contentLength;
                    downSize = 0;
                    pb_jindu.setMax(100);
                    pb_jindu.setProgress((int)(downSize/totalSize*100));
                    FileOutputStream fos = new FileOutputStream("/sdcard/temp.rar");
                    // 本地创建一个文件
                    //FileOutputStream fos = openFileOutput("/sdcard/temp.exe",MODE_PRIVATE);
                    InputStream is = conn.getInputStream();
                    // 将数据写到raf中
                    int len = 0;
                    byte[] buffer = new byte[1024];
                    while ((len = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                        downSize+=len;
                        System.out.println("----down----"+downSize+"---"+(int)(downSize/totalSize*100));
                        //Thread.sleep(100);

                        pb_jindu.setProgress((int)(downSize/totalSize*100));
                    }
                    is.close();
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
        }.start();
    }
}
