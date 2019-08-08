package com.example.theclient;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
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
                if (this.userName != "" && this.password != ""){
                    msgList.removeAllViews();
                    String messageSent = this.userName + ", " + this.password + ", #$";
                    TheClient theClient = new TheClient(this);
                    theClient.executeOnExecutor(TheClient.SERIAL_EXECUTOR, messageSent);
                    edMessage.setHint("Write a message");
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

                    String messageSent = this.userName + ", " + this.password + ", #$";
                    TheClient theClient = new TheClient(this);
                    theClient.executeOnExecutor(TheClient.SERIAL_EXECUTOR, messageSent);
                    edMessage.setHint("Write a message");
                }
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
                    edMessage.setHint("Write a message");
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
        if (value == true) {

            String checkClientMessage = edMessage.getText().toString().trim();
            String savedUserName = "";
            String savedPassword = "";
            if (checkClientMessage.isEmpty()){
                savedUserName = this.userName;
                savedPassword = this.password;
            }else {
                String clientMessage = (edMessage.getText().toString().trim()) + ", ";
                String[] partsOfMessageRecv = clientMessage.split(", ");
                savedUserName = partsOfMessageRecv[0];
                savedPassword = partsOfMessageRecv[1].trim();
            }


            String createFileName = "credentials.txt";
            String userAllreadyLogged = "userLogged.txt";


            File myWorkingDir = new File(Environment.getExternalStorageDirectory() + File.separator, "ChatRoom");
            File file = new File(myWorkingDir + File.separator, createFileName);
            File fileUserLogged = new File(myWorkingDir + File.separator, userAllreadyLogged);


            try {
                boolean notFoundInDB = false;


                Log.w("AICI AM AJUNS", String.valueOf(file));
                if (!myWorkingDir.exists()) {
                    file.getParentFile().mkdirs();
                }

                if (!fileUserLogged.exists()){
                    fileUserLogged.createNewFile();
                }

                if (!file.exists()) {
                    FileOutputStream writeInFile = new FileOutputStream(file);
                    file.createNewFile();
                    writeInFile.write((savedUserName + ", " + savedPassword + ", \n").getBytes());
                    this.userName = savedUserName;
                    this.password = savedPassword;
                    writeInFile.close();
                    FileOutputStream writeInFileUserLogged = new FileOutputStream(fileUserLogged);
                    writeInFileUserLogged.write((savedUserName + ", " + savedPassword.trim() + ", \n").getBytes());
                    writeInFileUserLogged.close();

                }else {
                    BufferedReader text = new BufferedReader(new FileReader(file));
                    String line;
                    while((line = text.readLine()) != null) {

                        String[] credentials = line.split(", ");
                        if (credentials[0].equals(savedUserName) && credentials[1].trim().equals(savedPassword)){
                            this.userName = savedUserName;
                            this.password = savedPassword;
                            text.close();
                            notFoundInDB = false;
                            FileOutputStream writeInFileUserLogged = new FileOutputStream(fileUserLogged);
                            writeInFileUserLogged.write((savedUserName + ", " + savedPassword + ", \n").getBytes());
                            writeInFileUserLogged.close();
                            break;

                        }else {
                            notFoundInDB = true;
                        }
                    }
                    if (notFoundInDB == true){
                        text.close();
                        FileOutputStream writeInFile = new FileOutputStream(file, true);
                        writeInFile.write(("\n" + savedUserName + ", " + savedPassword + ", \n").getBytes());
                        this.userName = savedUserName;
                        this.password = savedPassword;
                        writeInFile.close();
                        FileOutputStream writeInFileUserLogged = new FileOutputStream(fileUserLogged);
                        writeInFileUserLogged.write((savedUserName + ", " + savedPassword + ", \n").getBytes());
                        writeInFileUserLogged.close();
                        notFoundInDB = false;
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
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
            if(this.loggedOrNot == false){
                listener.onTaskComplete(false);
            }else{
                listener.onTaskComplete(true);
            }

        }
    }
}