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

    public class TheClient extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... Voids) {
            Socket socket;
            BufferedReader input;
            String messageToSend = Voids[0];
            String messageReceived = Voids[0];

            Log.w("inainte", "MModal");

            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                socket = new Socket(serverAddr, SERVERPORT);

                if (null != socket) {
                    PrintWriter out = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream())));
                    out.write(messageToSend);
                    out.flush();
                }


                String message1 = null;
                StringBuilder message = new StringBuilder();
                message.setLength(0);

                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));


                while ((message1 = input.readLine()) != null) {
                    message.append(message1 + "\n");

                    if (message1.equals(("Server: " + messageReceived))) {

                        break;
                    }else if (message1.equals(messageReceived)){
                        message.setLength(message.length()-3);
                        break;
                    }
                }



                socket.close();
                return message.toString();



            }catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            return null;
        }



        @Override
        protected void onPostExecute(String result) {
            msgList.addView(textView(result, clientTextColor));
        }
    }
}