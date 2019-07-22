package com.example.theclient;

import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;



public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnTaskCompleteListener {

    public static final int SERVERPORT = 8080;

    public static final String SERVER_IP = "172.18.81.40";
    private LinearLayout msgList;
//    private Handler handler;
    private int clientTextColor;
    private EditText edMessage;
    public String getInputValue = "";
//    private String getIpAddr = "";
    public String userName = "";
    public String password = "";
    private boolean okOrNot = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Client");
        clientTextColor = ContextCompat.getColor(this, R.color.colorPrimary);
//        handler = new Handler();
        msgList = findViewById(R.id.msgList);
        edMessage = findViewById(R.id.edMessage);
//        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
//        getIpAddr = Formatter.formatIpAddress(manager.getConnectionInfo().getIpAddress());


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
            String clientMessage = edMessage.getText().toString().trim();

            if (clientMessage.isEmpty()){
                Toast.makeText(this, "Wrong username or password!", Toast.LENGTH_LONG).show();
            }else {
                String[] partsOfMessageRecv = clientMessage.split(", ");
                if (partsOfMessageRecv.length != 2){
                    Toast.makeText(this, "Wrong username or password!", Toast.LENGTH_LONG).show();
                }else {
                    this.getInputValue = clientMessage;
                    final String timeToConnect = this.getInputValue + ", #$";
                    msgList.removeAllViews();

                    TheClient theClient = new TheClient(this);
                    theClient.executeOnExecutor(TheClient.SERIAL_EXECUTOR, timeToConnect);
                    edMessage.setHint("Write a messagel");
                }
            }

        }


        if (view.getId() == R.id.send_data) {

            String clientMessage = edMessage.getText().toString().trim();
            if (clientMessage.isEmpty()){
                Toast.makeText(this, "Empty message not accepted!", Toast.LENGTH_LONG).show();
            }else {

                String messageSent = this.userName + ", " + this.password + ", " + clientMessage;


                msgList.removeAllViews();

                TheClient theClient = new TheClient(this);
                theClient.executeOnExecutor(TheClient.SERIAL_EXECUTOR, messageSent);
            }
        }
    }

    @Override
    public void onTaskComplete(boolean value) {
            if(value == true){
                this.okOrNot = true;
                String clientMessage = (edMessage.getText().toString().trim()) + ", ";

                String[] partsOfMessageRecv = clientMessage.split(", ");
                this.userName = partsOfMessageRecv[0];
                this.password = partsOfMessageRecv[1];
            }
    }


    public class TheClient extends AsyncTask<String, String, String> {

        private OnTaskCompleteListener listener;
        private boolean loggedOrNot = false;

        public TheClient(OnTaskCompleteListener listener){
            this.listener = listener;
        }

        @Override
        protected String doInBackground(String... args) {
            Socket socket;
            BufferedReader input;
            String messageToSend = args[0];
            String messageReceived = args[0];

            String[] partsOfMessageRecv = messageReceived.split(", ");


            final String userConnectis = partsOfMessageRecv[0];
            final String messageIs = partsOfMessageRecv[2];



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

                    if (message1.equals(("Client-" + userConnectis + ": " + messageIs))) {
                        this.loggedOrNot = false;
                        break;
                    }else if (message1.equals(partsOfMessageRecv[0]) && !(message1.equals("NoAccount"))){
                        message.setLength(message.length()-5);
                        this.loggedOrNot = true;
                        break;
                    }else if (message1.equals("NoAccount")){
                        this.loggedOrNot = false;
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
            if(this.loggedOrNot == false){
                listener.onTaskComplete(false);
            }else{
                listener.onTaskComplete(true);
            }

        }
    }
}