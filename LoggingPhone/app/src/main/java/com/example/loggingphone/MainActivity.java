package com.example.loggingphone;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    static MainActivity instance;
    public EditText ipServerEditTxt;
    public TextView serverConnectionStateTV;
    public Button connectBtn;
    public TextView device1ConnectionStateTV;
    public TextView device2ConnectionStateTV;
    public Button closeFileBtn;
    public Button openSocketBtn;

    public boolean startStreaming;
    private PowerManager.WakeLock mWakeLock;
    private WifiManager.WifiLock mWifiLock;
    private String ipServer;
    private Intent socketServiceIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instance=this;
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Wake Lock");
        mWakeLock.acquire();

        WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mWifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "My Wifi Lock");
        mWifiLock.acquire();
        this.startStreaming=false;

        initViews();
    }
    public void startActivityFromMainThread(){

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                socketServiceIntent = new Intent (MainActivity.this, SocketService.class);
                socketServiceIntent.putExtra("IP",ipServer);
                //Start the foreground service for bluetooth and UDP socket connections.
                startService(socketServiceIntent);
            }
        });
    }
    private void initViews(){
        ipServerEditTxt=findViewById(R.id.editTextIPServer);
        connectBtn=findViewById(R.id.buttonConnect);
        serverConnectionStateTV=findViewById(R.id.textViewServerConnection);
        device1ConnectionStateTV=findViewById(R.id.textViewDevice1Connection);
        device2ConnectionStateTV=findViewById(R.id.textViewDevice2Connection);
        closeFileBtn=findViewById(R.id.buttonCloseSocket);
        openSocketBtn=findViewById(R.id.buttonOpenSocket);

        //This is a handle action for create an UDP socket to the server.
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ipServer=ipServerEditTxt.getText().toString();
                            Socket socket = new Socket(ipServer, 5550);
                            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                            oos.writeObject("Ping!");
                            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                            String result = (String) ois.readObject();
                            if (result.equals("Accepted!")) {
                                serverConnectionStateTV.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        serverConnectionStateTV.setText("Server connection state: Connected!");
                                    }
                                });
                                socket.close();
                                //If connect successfully, start streaming.
                                startActivityFromMainThread();
                            } else {
                                socket.close();
                                throw new Exception();
                            }
                        } catch (final Exception e) {}
                    }
                }).start();

            }
        });

        openSocketBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startStreaming=true;
                serverConnectionStateTV.setText("Server connection state: Start streaming!");
                device1ConnectionStateTV.setText("Device 1 connection state: Start streaming!");
                device2ConnectionStateTV.setText("Device 2 connection state: Start streaming!");
            }
        });

        closeFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWakeLock.release();
                mWifiLock.release();
                if(socketServiceIntent!=null)
                    stopService(socketServiceIntent);
                finish();
            }
        });

    }
}
