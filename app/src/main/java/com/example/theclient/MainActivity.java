package com.example.theclient;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;



public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int SERVERPORT = 8080;

    public static final String SERVER_IP = "172.18.81.40";
    private LinearLayout msgList;
    private Handler handler;
    private int clientTextColor;
    private EditText edMessage;
    public String getInputValue = "";





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Client");
        clientTextColor = ContextCompat.getColor(this, R.color.colorPrimary);
        handler = new Handler();
        msgList = findViewById(R.id.msgList);
        edMessage = findViewById(R.id.edMessage);

    }

    public TextView textView(String message, int color) {
        if (null == message || message.trim().isEmpty()) {
            message = "<Empty Message>";
        }
        TextView tv = new TextView(this);
        tv.setTextColor(color);
        tv.setText(message);
        tv.setTextSize(20);
        tv.setPadding(0, 5, 0, 0);
        return tv;
    }


    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.connect_server) {
            final String timeToConnect = "#$";
            msgList.removeAllViews();

            TheClient theClient = new TheClient();
            theClient.executeOnExecutor(TheClient.SERIAL_EXECUTOR, timeToConnect);


        }



        if (view.getId() == R.id.send_data) {

            String clientMessage = edMessage.getText().toString().trim();
            getInputValue = clientMessage;
            msgList.removeAllViews();
            Log.w("The Message IS: ", getInputValue);
            TheClient theClient = new TheClient();
            theClient.executeOnExecutor(TheClient.SERIAL_EXECUTOR, getInputValue);

        }
    }

}