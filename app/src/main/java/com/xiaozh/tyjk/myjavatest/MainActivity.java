package com.xiaozh.tyjk.myjavatest;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Socket连接的客户端
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText editText1, editText2, editText3;
    private TextView textView;
    private Button btn1, btn2;
    private static final String IP = "192.168.1.116";//服务端ip
    private static final int SOCKET_PORT = 1234;//端口号

    private Socket clientSocket;
    private boolean isReceivingMsgReady = false;
    private BufferedReader mReader;
    private BufferedWriter mWriter;
    private StringBuffer sb = new StringBuffer();

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    sb.append((String) msg.obj);
                    textView.setText(sb.toString());
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        editText1 = findViewById(R.id.edit_ip);
        editText2 = findViewById(R.id.edit_port);
        editText3 = findViewById(R.id.edit_message);
        btn1 = findViewById(R.id.btn_connect);
        btn2 = findViewById(R.id.btn_message);
        textView = findViewById(R.id.text_message);
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_connect:
                if (!isReceivingMsgReady) {
                    initSocket();
                }
                break;
            case R.id.btn_message:
                send();
                break;
            default:
                break;
        }
    }

    private void initSocket() {
        new Thread(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                String ip = editText1.getText().toString();
                int port = Integer.parseInt(editText2.getText().toString());
                try {
                    isReceivingMsgReady = true;
                    //在子线程中初始化Socket对象
                    clientSocket = new Socket(ip, port);
                    //根据clientSocket.getInputStream得到BufferedReader对象，从而从输入流中获取数据
                    mReader = new BufferedReader(new InputStreamReader(
                            clientSocket.getInputStream(), "UTF-8"));
                    //根据clientSocket.getOutputStream得到BufferedWriter对象，从而从输出流中获取数据
                    mWriter = new BufferedWriter(new OutputStreamWriter(
                            clientSocket.getOutputStream(), "UTF-8"));
                    while (isReceivingMsgReady) {
                        if (mReader.ready()) {
                            handler.obtainMessage(0, mReader.readLine()).sendToTarget();
                        }
                        Thread.sleep(200);
                    }
                    mWriter.close();
                    mReader.close();
                    clientSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    @SuppressLint("StaticFieldLeak")
    private void send() {
        new AsyncTask<String, Integer, String>() {

            @Override
            protected String doInBackground(String... strings) {
                sendMsg();
                return null;
            }
        }.execute();
    }

    /**
     * 向服务器发送消息
     */
    private void sendMsg() {
        try {
            String msg = editText3.getText().toString();
            //通过BufferedWriter对象向服务器写数据
            mWriter.write(msg + "\n");
            mWriter.flush();
            String str = "\n" + "客户端:" + msg + "  " + getTime(System.currentTimeMillis()) + "\n";
            handler.obtainMessage(0, str).sendToTarget();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @SuppressLint("SimpleDateFormat")
    private String getTime(long millTime) {
        String time;
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d = new Date(millTime);
        time = sdf.format(d);
        return time;
    }
}
