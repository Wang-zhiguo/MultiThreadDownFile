package cn.androidstudy.multithreaddownfile;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class MultiThreadDownActivity extends AppCompatActivity {
    // 服务器资源地址
    private static String path = "http://172.16.26.58:8080/EditPlus.exe";
    private TextView mFileTV; // 用于展示服务器资源文件的大小
    private TextView mThread1TV; // 用于显示thread需要下载的文件长度
    private TextView mThread3CompleteTV; // thread下载完成时显示
    protected static int threadCount; // 线程个数
    private EditText et_path;
    private ProgressBar pb_jindu;
    private ProgressBar pb_jindu1;
    private ProgressBar pb_jindu2;
    // 用于更新UI界面的Handler
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {

            switch (msg.what) {
                case 100: // 服务器资源文件的大小
                    mFileTV.setText("服务器资源文件大小为:" + (Long) msg.obj);
                    break;
                case 101: // 计算每个线程需要下载多少
                    String string = mThread1TV.getText().toString();
                    mThread1TV.setText(string + (String) msg.obj);
                    break;
                case 102:// 查看那个线程下载的最快
                    String string1 = mThread3CompleteTV.getText().toString();
                    mThread3CompleteTV.setText(string1 + (String) msg.obj);
                    break;
                case 300:
                    Toast.makeText(MultiThreadDownActivity.this, "获取不到服务器文件", Toast.LENGTH_SHORT).show();
                    break;
            }
        };
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_thread_down);

        mFileTV = (TextView)findViewById(R.id.textView);
        mThread1TV = (TextView)findViewById(R.id.textView2);
        mThread3CompleteTV = (TextView)findViewById(R.id.textView3);

        et_path = (EditText)findViewById(R.id.editText);
        pb_jindu = (ProgressBar)findViewById(R.id.progressBar);
        pb_jindu1 = (ProgressBar)findViewById(R.id.progressBar1);
        pb_jindu2 = (ProgressBar)findViewById(R.id.progressBar2);
        pb_jindu.setMax(100);
        pb_jindu.setProgress(0);
        pb_jindu1.setMax(100);
        pb_jindu1.setProgress(0);
        pb_jindu2.setMax(100);
        pb_jindu2.setProgress(0);
        path = et_path.getText().toString();
    }

    public void downFile(View view){
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
                        Message msg = new Message();
                        msg.what = 300;
                        handler.sendMessage(msg);
                        return;
                    }
                    // 使用Handler发送消息更改界面
                    Message msg = new Message();
                    msg.what = 100;
                    msg.obj = new Long(contentLength);
                    handler.sendMessage(msg);
                    // 本地创建一个随机文件并制定类型
                    RandomAccessFile raf = new RandomAccessFile(
                            "/sdcard/notepad.rar", "rwd");
                    // 设置本地文件的大小
                    raf.setLength(contentLength);
                    // 线程的数量
                    threadCount = 3;
                    // 每个线程下载的区块的大小
                    long blocksize = contentLength / threadCount;
                    // 计算出来每个线程 下载的开始和结束的位置.
                    for (int threadId = 1; threadId <= threadCount; threadId++) {
                        long startPos = (threadId - 1) * blocksize;
                        long endPos = threadId * blocksize - 1;
                        if (threadId == threadCount) {
                            // 最后一个线程
                            endPos = contentLength;
                        }
                        Message message = new Message();
                        message.what = 101;
                        message.obj = "线程" + threadId + "需下载" + startPos + "-"
                                + endPos + "\n";
                        handler.sendMessage(message);
                        // 开起线程开始下载文件
                        new DownLoadThread(startPos, endPos, threadId, path)
                                .start();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
        }.start();
    }


    // 自定一个线程用于下载文件
    class DownLoadThread extends Thread {
        private long startPos;
        private long endPos;
        private long threadId;
        private String path;
        private long totalSize;
        private double downSize;

        public DownLoadThread(long startPos, long endPos, long threadId,
                              String path) {
            super();
            this.startPos = startPos;
            this.endPos = endPos;
            this.threadId = threadId;
            this.path = path;
            totalSize = endPos-startPos+1;
            downSize = 0;
        }

        public void run() {
            try {
                URL url = new URL(path);
                HttpURLConnection conn = (HttpURLConnection) url
                        .openConnection();
                conn.setRequestMethod("GET"); // 设置请求方法
                conn.setConnectTimeout(5000); // 设置超时时间
                // 请求部分数据 请求成功返回206
                conn.setRequestProperty("Range", "bytes=" + startPos + "-"
                        + endPos);
                InputStream is = conn.getInputStream();
                RandomAccessFile raf = new RandomAccessFile("/sdcard/notepad.rar",
                        "rwd");
                // 重新指定某个线程保存文件的开始位置 需与服务器下载的位置一致
                raf.seek(startPos);
                // 将数据写到raf中
                int len = 0;
                byte[] buffer = new byte[1024];
                while ((len = is.read(buffer)) != -1) {
                    raf.write(buffer, 0, len);
                    downSize+=len;
                    if(threadId==1){
                        pb_jindu.setProgress((int)(downSize/totalSize*100));
                    }else if(threadId==2){
                        pb_jindu1.setProgress((int)(downSize/totalSize*100));
                    }else if(threadId==3){
                        pb_jindu2.setProgress((int)(downSize/totalSize*100));
                    }
                }
                is.close();
                raf.close();
                // 使用handler给主线程发送消息
                Message msg = new Message();
                msg.what = 102;
                msg.obj = new String("线程" + threadId + "下载完成" + "\n");
                handler.sendMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
