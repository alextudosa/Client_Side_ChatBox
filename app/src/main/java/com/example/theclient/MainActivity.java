package com.example.theclient;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Timer;
import java.util.TimerTask;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnTaskCompleteListener {

    public static final int SERVERPORT = 8080;

    public static final String SERVER_IP = "172.18.81.40";
    private LinearLayout msgList;
//    private Handler handler;
    private int clientTextColor;
    private static EditText edMessage;
    private ScrollView myScrollView;
    public String getInputValue = "";
//    private String getIpAddr = "";
    public String userName = "";
    public String password = "";
    private boolean issConnected;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Client");
        clientTextColor = ContextCompat.getColor(this, R.color.colorPrimary);
//        handler = new Handler();
        msgList = findViewById(R.id.msgList);
        edMessage = findViewById(R.id.edMessage);
        myScrollView = findViewById(R.id.myScrollView);
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
                if (this.userName != "" && this.password != ""){
                    msgList.removeAllViews();
                    String messageSent = this.userName + ", " + this.password + ", connect" + ", ";
                    TheClient theClient = new TheClient(this);
                    theClient.executeOnExecutor(TheClient.SERIAL_EXECUTOR, messageSent);
                    edMessage.setHint("Write a message");
                    callAsynchronousTask();
                    edMessage.getText().clear();
                    typingIndicator();
                }else {
                    File myWorkingDir = new File(Environment.getExternalStorageDirectory() + File.separator, "ChatRoom");
                    String userAllreadyLogged = "userLogged.txt";
                    File fileUserLogged = new File(myWorkingDir + File.separator, userAllreadyLogged);


                    try {
                        BufferedReader text = new BufferedReader(new FileReader(fileUserLogged));
                        String line;
                        while ((line = text.readLine()) != null) {

                            String[] credentials = line.split(", ");
                            this.userName = credentials[0];
                            this.password = credentials[1];
                        }
                        text.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String messageSent = this.userName + ", " + this.password + ", connect" + ", ";
                    TheClient theClient = new TheClient(this);
                    theClient.executeOnExecutor(TheClient.SERIAL_EXECUTOR, messageSent);
                    edMessage.setHint("Write a message");
                    callAsynchronousTask();
                    edMessage.getText().clear();
                    typingIndicator();
                }
            }else {
                String[] partsOfMessageRecv = clientMessage.split(", ");
                if (partsOfMessageRecv.length != 2){
                    Toast.makeText(this, "Wrong username or password!", Toast.LENGTH_LONG).show();
                }else {
                    this.getInputValue = clientMessage;
                    final String timeToConnect = this.getInputValue + ", connect" + ", ";
                    msgList.removeAllViews();

                    TheClient theClient = new TheClient(this);
                    theClient.executeOnExecutor(TheClient.SERIAL_EXECUTOR, timeToConnect);
                    edMessage.setHint("Write a message");
                    callAsynchronousTask();
                    typingIndicator();
                }

            }
        }


        if (view.getId() == R.id.send_data) {

            String clientMessage = edMessage.getText().toString().trim();
            if (clientMessage.isEmpty()){
                Toast.makeText(this, "Empty message not accepted!", Toast.LENGTH_LONG).show();
            }else {



                msgList.removeAllViews();
                String messageSent = this.userName + ", " + this.password + ", new message" + ", " + clientMessage;

                TheClient theClient = new TheClient(this);
                theClient.executeOnExecutor(TheClient.SERIAL_EXECUTOR, messageSent);
                edMessage.getText().clear();
                callAsynchronousTask();
                typingIndicator();
            }
        }
    }




    public void typingIndicator(){
        msgList.removeAllViews();
            edMessage.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {
                    final Handler handler = new Handler();
                    Timer timer = new Timer();
                    TimerTask doAsynchronousTask = new TimerTask() {
                        @Override
                        public void run() {
                            handler.post(new Runnable() {
                                public void run() {
                                    MainActivity mainActivity = new MainActivity();

                                    String userAllreadyLogged = "userLogged.txt";
                                    File myWorkingDir = new File(Environment.getExternalStorageDirectory() + File.separator, "ChatRoom");
                                    File fileUserLogged = new File(myWorkingDir + File.separator, userAllreadyLogged);
                                    BufferedReader text = null;
                                    String userName1 = "";
                                    String password1 = "";
                                    try {
                                        text = new BufferedReader(new FileReader(fileUserLogged));

                                        String line;
                                        while ((line = text.readLine()) != null) {

                                            String[] credentials = line.split(", ");
                                            userName1 = credentials[0];
                                            password1 = credentials[1];
                                        }

                                        String messageSent = userName1 + ", " + password1 + ", not typing" + ", ";
                                        TheClient theClient = new TheClient(mainActivity);
                                        theClient.executeOnExecutor(TheClient.SERIAL_EXECUTOR, messageSent);
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                        }
                    };
                    timer.schedule(doAsynchronousTask, 3000);
                }

                    @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {

                    MainActivity mainActivity = new MainActivity();

                    String userAllreadyLogged = "userLogged.txt";
                    File myWorkingDir = new File(Environment.getExternalStorageDirectory() + File.separator, "ChatRoom");
                    File fileUserLogged = new File(myWorkingDir + File.separator, userAllreadyLogged);
                    BufferedReader text = null;
                    String userName1 = "";
                    String password1 = "";
                    try {
                        text = new BufferedReader(new FileReader(fileUserLogged));

                        String line;
                        while((line = text.readLine()) != null) {

                            String[] credentials = line.split(", ");
                            userName1 = credentials[0];
                            password1 = credentials[1];
                        }
                    if(s.length() != 0 ){
                        Log.w("Sunt in", "TYPING INDICATOR");
                        String messageSent = userName1 + ", " + password1 + ", typing" + ", ";
                        TheClient theClient = new TheClient(mainActivity);
                        theClient.executeOnExecutor(TheClient.SERIAL_EXECUTOR, messageSent);
                    }

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

            });


    }


    public void callAsynchronousTask() {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            String userAllreadyLogged = "userLogged.txt";
                            File myWorkingDir = new File(Environment.getExternalStorageDirectory() + File.separator, "ChatRoom");
                            File fileUserLogged = new File(myWorkingDir + File.separator, userAllreadyLogged);
                            BufferedReader text = null;
                            String userName1 = "";
                            String password1 = "";
                            text = new BufferedReader(new FileReader(fileUserLogged));

                            String line;
                            while((line = text.readLine()) != null) {

                                String[] credentials = line.split(", ");
                                userName1 = credentials[0];
                                password1 = credentials[1];

                            }
                            msgList.removeAllViews();
                            MainActivity mainActivity = new MainActivity();
                            String messageSent = userName1 + ", " + password1 + ", refresh" + ", ";
                            TheClient theClient = new TheClient(mainActivity);
                            theClient.executeOnExecutor(TheClient.SERIAL_EXECUTOR, messageSent);


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 2000, 5000); //execute in every n ms
    }


    @Override
    public void onTaskComplete(int value) {
        try {
//            String createFileName = "credentials.txt";
            String userAllreadyLogged = "userLogged.txt";
            File myWorkingDir = new File(Environment.getExternalStorageDirectory() + File.separator, "ChatRoom");
//            File file = new File(myWorkingDir + File.separator, createFileName);
            File fileUserLogged = new File(myWorkingDir + File.separator, userAllreadyLogged);
        String savedUserName = "";
        String savedPassword = "";

        if (value == 0) {

            String clientMessage1 = (edMessage.getText().toString().trim()) + ", ";
            String[] partsOfMessageRecv1 = clientMessage1.split(", ");

            if (TextUtils.isEmpty(edMessage.getText().toString().trim()) && fileUserLogged.exists()){
                BufferedReader text = null;
                text = new BufferedReader(new FileReader(fileUserLogged));

                String line;
                while((line = text.readLine()) != null) {

                    String[] credentials = line.split(", ");
                    savedUserName = credentials[0];
                    savedPassword = credentials[1];

                }
            }else if(!(TextUtils.isEmpty(edMessage.getText().toString().trim())) && fileUserLogged.exists() && partsOfMessageRecv1.length != 2){
                BufferedReader text = null;
                text = new BufferedReader(new FileReader(fileUserLogged));

                String line;
                while((line = text.readLine()) != null) {

                    String[] credentials = line.split(", ");
                    savedUserName = credentials[0];
                    savedPassword = credentials[1];
                }
            }else {
                savedUserName = partsOfMessageRecv1[0];
                savedPassword = partsOfMessageRecv1[1].trim();

            }
        }else if(value == 1){
            BufferedReader text = null;
            text = new BufferedReader(new FileReader(fileUserLogged));

            String line;
            while((line = text.readLine()) != null) {

                String[] credentials = line.split(", ");
                savedUserName = credentials[0];
                savedPassword = credentials[1];

            }
        }


                boolean notFoundInDB = false;


                if (!myWorkingDir.exists()) {
                    fileUserLogged.getParentFile().mkdirs();
                }

                if (!fileUserLogged.exists()){
                    fileUserLogged.createNewFile();
                    FileOutputStream writeInFileUserLogged = new FileOutputStream(fileUserLogged);
                    writeInFileUserLogged.write((savedUserName + ", " + savedPassword.trim() + ", \n").getBytes());
                    this.userName = savedUserName;
                    this.password = savedPassword;
                    writeInFileUserLogged.close();

                }else {
                    BufferedReader text = new BufferedReader(new FileReader(fileUserLogged));
                    String line;
                    while((line = text.readLine()) != null) {

                        String[] credentials = line.split(", ");

                        if (credentials[0].equals(savedUserName) && credentials[1].trim().equals(savedPassword) && credentials.length == 2){
                            this.userName = savedUserName;
                            this.password = savedPassword;
                            text.close();
                            notFoundInDB = false;
                            break;

                        }else {
                            notFoundInDB = true;
                        }
                        if (notFoundInDB == true){
                            text.close();
                            FileOutputStream writeInFileUserLogged = new FileOutputStream(fileUserLogged);
                            writeInFileUserLogged.write((savedUserName + ", " + savedPassword + ", \n").getBytes());
                            this.userName = savedUserName;
                            this.password = savedPassword;
                            writeInFileUserLogged.close();
                            notFoundInDB = false;
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
    }


    public class TheClient extends AsyncTask<String, String, String> {

        private OnTaskCompleteListener listener;
        private int loggedOrNot;

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
            String[] partsOfMessageSent = messageToSend.split(", ");

            final String userConnectis = partsOfMessageSent[0];
            final String protocol = partsOfMessageSent[2];

            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

                InputStream is = getResources().openRawResource(R.raw.client);
                InputStream clientCert = new BufferedInputStream(is);

                InputStream serv = getResources().openRawResource(R.raw.server);
                InputStream serverCert = new BufferedInputStream(serv);

                KeyStore storeServer = KeyStore.getInstance("BKS");
                storeServer.load(serverCert, "123456".toCharArray());
                TrustManagerFactory tmf = TrustManagerFactory
                        .getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(storeServer);

                KeyStore store = KeyStore.getInstance("BKS");
                store.load(clientCert, "123456".toCharArray());
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
                kmf.init(store, null);

                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
                SSLSocketFactory sf = sslContext.getSocketFactory();
                SSLSocket sslSocket = (SSLSocket) sf.createSocket(serverAddr, SERVERPORT);
                sslSocket.setUseClientMode(true);
                if (null != sslSocket) {
                    PrintWriter out = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(sslSocket.getOutputStream())));
                    out.write(messageToSend);
                    out.flush();
                }


                String message1 = null;
                StringBuilder message = new StringBuilder();
                message.setLength(0);

                input = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));


                while ((message1 = input.readLine()) != null) {
                    message.append(message1 + "\n");
                    if (message1.equals((protocol)) && (protocol.equals("new message")) && !(message1.equals("NoAccount"))) {
                        message.setLength(message.length()-12);
                        this.loggedOrNot = 1;
                        break;
                    }else if (message1.equals(protocol) && !(message1.equals("NoAccount")) && (protocol.equals("connect"))){
                        message.setLength(message.length()-8);
                        this.loggedOrNot = 0;
                        break;
                    }else if(message1.equals(userConnectis + " is typing...") && protocol.equals("typing")) {
                        message.setLength(message.length()-22);
                        this.loggedOrNot = 1;
                        break;
                    }else if(message1.equals(protocol) && protocol.equals("typing")) {
                        message.setLength(message.length()-10);
                        this.loggedOrNot = 1;
                        break;
                    }else if(message1.equals(protocol) && protocol.equals("not typing")) {
                        Log.w("Am intrat aici", message1);
                        message.setLength(message.length()-11);
                        this.loggedOrNot = 1;
                        break;
                    }else if (message1.equals("NoAccount")){
                        this.loggedOrNot = 2;
                        break;
                    }else if (message1.equals(protocol) && protocol.equals("refresh")) {
                        message.setLength(message.length()-8);
                        this.loggedOrNot = 1;
                        break;
                    }

                    /*if (message1.equals(("Client-" + userConnectis + ": " + messageIs)) && !(message1.contains("@$DSC*&&6"))) {
                        this.loggedOrNot = 1;
                        break;
                    }else if (message1.equals(protocol) && !(message1.equals("NoAccount")) && !(message1.contains("@$DSC*&&6"))){
                        message.setLength(message.length()-5);
                        this.loggedOrNot = 0;
                        break;
                    }else if(message1.equals(partsOfMessageRecv[0] + " is typing...@$DSC*&&6")) {
                        message.setLength(message.length()-27);
                        this.loggedOrNot = 1;
                        break;
                    }else if(message1.contains("@$DSC*&&6")) {
                        message.setLength(message.length()-10);
                        this.loggedOrNot = 1;
                        break;
                    }else if(message1.contains("WV%^$0(*~")) {
                        message.setLength(message.length()-10);
                        this.loggedOrNot = 1;
                        break;
                    }else if (message1.equals("NoAccount")){
                        this.loggedOrNot = 2;
                        break;
                    }else if (message1.equals("refresh$$%#X")) {
                        message.setLength(message.length()-13);
                        this.loggedOrNot = 1;
                        break;
                }



                sslSocket.close();
                return message.toString();



            }catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (CertificateException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }  catch (GeneralSecurityException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            msgList.addView(textView(result, clientTextColor));
            myScrollView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    myScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
            },200);

            listener.onTaskComplete(this.loggedOrNot);

        }
    }
}