package com.example.networkingproject2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import yuku.ambilwarna.AmbilWarnaDialog;

public class IpAddress extends AppCompatActivity{

    EditText receivePortEditText, targetPortEditText, messageEditText, targetIPEditText;
    TextView chatText;
    String bg,hexColor;
    int initialColor,converted;
    ConstraintLayout mainLayout;
    boolean setColor;
    String colorCode;
    ServerClass serverClass;
    ClientClass clientClass;
    SendReceive sendReceive;
    String saveMsg;
    static final int MESSAGE_READ=1;
    static final String TAG = "yourTag";
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    Handler handler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what)
            {
                case MESSAGE_READ:
                    byte[] readBuff= (byte[]) msg.obj;
                    String tempMsg=new String(readBuff,0,msg.arg1);

                    chatText.setText(tempMsg);

                    String[] allMsg = tempMsg.split("@@@", 0);
                  /*  if(allMsg[0].equals("123")){
                        Log.d(TAG, "Message if found");
                        chatText.setText(allMsg[1]);
                        chatText.setText(tempMsg);
                        saveMsg+=chatText;
                    }*/
                       if(allMsg[0].equals("124")){
                        Toast.makeText(getApplicationContext(), "File saved in conversation location", Toast.LENGTH_SHORT).show();
                        String fileText = allMsg[1];
                        writeToFile("file", fileText, true);
                    }
                  /* else if(allMsg[0].equals("125")){
                           int converted=Integer.parseInt(allMsg[1]);
                        changeColor(converted);
                           Log.d(TAG, "Color found");
                    }*/
                    if(tempMsg.charAt(0) == '#'){
                        Log.d(TAG,"Color is: "+ tempMsg);
                        if(tempMsg.equals("#green")){
                            setColor = false;
                            Log.d(TAG, "SetColor is: "+setColor);
                            mainLayout.setBackgroundColor(Color.parseColor("#008577"));
                        }
                        else {
                            setColor = true;
                            Log.d(TAG, "SetColor is: "+setColor);
                            mainLayout.setBackgroundColor(Color.parseColor("#00574B"));

                        }

                    }

                    break;
            }
            return true;
        }
    });
    public void changeColor(final int color) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                mainLayout.setBackgroundColor(color);
            }
        });
        changeBackground(hexColor);

    }
    public void changeBackground(String code){

            AmbilWarnaDialog dialog = new AmbilWarnaDialog(this,initialColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                @Override
                public void onCancel(AmbilWarnaDialog dialog) {

                }

                @Override
                public void onOk(AmbilWarnaDialog dialog, int color) {
                    int initialColor = color;
                    mainLayout.setBackgroundColor(color);
                    hexColor = String.format("#%06X", (0xFFFFFF & color));

                    byte[] b = hexColor.getBytes();
                    sendReceive.write(b);
                    //Toast.makeText(getApplicationContext(),b,Toast.LENGTH_LONG).show();
                }
            });
            dialog.show();
        }

    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ip_adress);
        mainLayout = findViewById(R.id.mainLayout);
        receivePortEditText = findViewById(R.id.receiveEditText);
        targetPortEditText = findViewById(R.id.targetPortEditText);
        messageEditText = findViewById(R.id.messageEditText);
        targetIPEditText = findViewById(R.id.targetIPEditText);
        chatText = findViewById(R.id.chatText);
        verifyStoragePermissions();
        verifyDataFolder();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub

        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case 7:

                if (resultCode == RESULT_OK) {

                    Uri uri = data.getData();
                    String fileText = getTextFromUri(uri);

                    String msg = "124@@@"+fileText;
                    Log.d(TAG, msg);
                    sendReceive.write(msg.getBytes());
                }
                break;

        }
    }

    public String getTextFromUri(Uri uri){
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)));
            String line = "";

            while ((line = reader.readLine()) != null) {
                builder.append("\n"+line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return builder.toString();
    }

    public void verifyStoragePermissions() {
        // Check if we have write permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(
                        PERMISSIONS_STORAGE,
                        REQUEST_EXTERNAL_STORAGE
                );
            }
        }
    }
    private void verifyDataFolder() {
        File folder = new File(Environment.getExternalStorageDirectory() + "/Peer 2 Peer");
        File folder1 = new File(folder.getPath() + "/Conversations");
        File folder2 = new File(folder.getPath() + "/Saved txt files");
        if(!folder.exists() || !folder.isDirectory()) {
            folder.mkdir();
            folder1.mkdir();
            folder2.mkdir();
        }
        else if(!folder1.exists())
            folder1.mkdir();
        else if(!folder2.exists())
            folder2.mkdir();
    }
    private void writeToFile(String fileName, String data, boolean timeStamp) {

        Long time= System.currentTimeMillis();
        String timeMill = " "+time.toString();
        String path = Environment.getExternalStorageDirectory().toString();
        File file = null;
        if(timeStamp)
            file = new File(path+"/Peer 2 Peer/Conversations", fileName+timeMill+".txt");
        else
            file = new File(path+"/Peer 2 Peer/Saved txt files", fileName);
        FileOutputStream stream;
        try {
            stream = new FileOutputStream(file, false);
            stream.write(data.getBytes());
            stream.close();
           // showToast("file saved in: "+file.getPath());
            Toast.makeText(this, "Saving Your Conversation.....", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            Log.d(TAG, e.toString());
        } catch (IOException e) {
            Log.d(TAG, e.toString());
        }
    }




    public void onStartServerClicked(View v){
        String port = receivePortEditText.getText().toString();
        serverClass = new ServerClass(Integer.parseInt(port));
        serverClass.start();
    }

    public void onConnectClicked(View v){
        String port = targetPortEditText.getText().toString();
        clientClass = new ClientClass(targetIPEditText.getText().toString(), Integer.parseInt(port));
        clientClass.start();
    }
    
    public void onFileClicked(View v){
        generateFileManagerWindow();
    }
    public void onColorChange(View v){

        if(setColor == false){
            colorCode = "#dark";
            setColor = true;
            mainLayout.setBackgroundColor(Color.parseColor("#00574B"));

        }
        else {
            colorCode = "#green";
            setColor = false;
            mainLayout.setBackgroundColor(Color.parseColor("#008577"));
        }

        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    //String msg = "Hello Soumik";

                    sendReceive.write(colorCode.getBytes());
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    private void generateFileManagerWindow() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, 7);
    }

    public void onSendClicked(View v){
       String msg=messageEditText.getText().toString();
        saveMsg+=msg;
        msg = "121@@@"+msg;

        Log.d(TAG, "Message is sent");

        new Thread(new Runnable() {
            String msg=messageEditText.getText().toString();

            String m = "123@@@"+msg;


            @Override
            public void run() {
                sendReceive.write(msg.getBytes());
            }
        }).start();


    }

    public void onsaveConservation(View v){
           writeToFile(saveMsg,saveMsg,true);
    }

    public class ServerClass extends Thread{
        Socket socket;
        ServerSocket serverSocket;
        int port;

        public ServerClass(int port) {
            this.port = port;
        }

        @Override
        public void run() {
            try {
                serverSocket=new ServerSocket(port);
                Log.d(TAG, "Waiting for client...");
                socket=serverSocket.accept();
                Log.d(TAG, "Connection established from server");
                sendReceive=new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "ERROR/n"+e);
            }
        }
    }

    private class SendReceive extends Thread{
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public SendReceive(Socket skt)
        {
            socket=skt;
            try {
                inputStream=socket.getInputStream();
                outputStream=socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            byte[] buffer=new byte[1024];
            int bytes;

            while (socket!=null)
            {
                try {
                    bytes=inputStream.read(buffer);
                    if(bytes>0)
                    {
                        handler.obtainMessage(MESSAGE_READ,bytes,-1,buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes)
        {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class ClientClass extends Thread{
        Socket socket;
        String hostAdd;
        int port;

        public  ClientClass(String hostAddress, int port)
        {
            this.port = port;
            this.hostAdd = hostAddress;
        }

        @Override
        public void run() {
            try {

                socket=new Socket(hostAdd, port);
                Log.d(TAG, "Client is connected to server");
                sendReceive=new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Can't connect from client/n"+e);
            }
        }
    }

}
