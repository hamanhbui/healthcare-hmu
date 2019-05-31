package com.example.loggingphone;

import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class SocketService extends Service {

    private BufferedInputStream bisDevice1;
    private BufferedInputStream bisDevice2;
    private DatagramSocket socketDevice1DatagramSocket;
    private DatagramSocket socketDevice2DatagramSocket;
    private String ipServer;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ipServer=intent.getExtras().getString("IP");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    initBluetoothServerSocket();
                }catch (Exception e){
                    System.out.println(e.toString());
                }
            }
        }).start();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Text")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        Notification notification = builder.build();

        startForeground(1, notification);

        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public void onDestroy() {
        try {
            bisDevice2.close();
            bisDevice1.close();
            socketDevice1DatagramSocket.close();
            socketDevice2DatagramSocket.close();
            super.onDestroy();
        }catch (Exception e){}
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createSocket(){
        try {
            socketDevice1DatagramSocket=new DatagramSocket(5556);
            socketDevice2DatagramSocket=new DatagramSocket(5557);
        }catch (Exception e){}
    }

    private void initBluetoothServerSocket() throws IOException {
        //Create 2 sockets to 2 wears respectivelly.
        BluetoothAdapter blueAdapter = BluetoothAdapter.getDefaultAdapter();
        if (blueAdapter != null) {
            if (blueAdapter.isEnabled()) {
                Set<BluetoothDevice> bondedDevices = blueAdapter.getBondedDevices();

                if(bondedDevices.size() > 0) {
                    BluetoothServerSocket mmServerSocketDevice1=blueAdapter.listenUsingRfcommWithServiceRecord("DEVICE 1", UUID.fromString("00000000-0000-1000-8000-77f199fd0834"));
                    BluetoothServerSocket mmServerSocketDevice2=blueAdapter.listenUsingRfcommWithServiceRecord("DEVICE 2", UUID.fromString("00000000-0000-1000-8000-00805F9B34FB"));
                    BluetoothSocket device1Socket=null;
                    BluetoothSocket device2Socket=null;
                    while(true){
                        try {
                            if(device1Socket==null) {
                                //Waiting for the first device connection.
                                device1Socket=mmServerSocketDevice1.accept();
                                InputStream isDevice1 = device1Socket.getInputStream();
                                bisDevice1=new BufferedInputStream(isDevice1);
                                mmServerSocketDevice1.close();
                                MainActivity.instance.device1ConnectionStateTV.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.instance.device1ConnectionStateTV.setText("Device 1 connection state: Connected!");
                                    }
                                });
                            }
                            if(device2Socket==null) {
                                //Waiting for the second device connection.
                                device2Socket = mmServerSocketDevice2.accept();
                                InputStream isDevice2=device2Socket.getInputStream();
                                bisDevice2 = new BufferedInputStream(isDevice2);
                                mmServerSocketDevice2.close();
                                MainActivity.instance.device2ConnectionStateTV.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.instance.device2ConnectionStateTV.setText("Device 2 connection state: Connected!");
                                    }
                                });
                            }
                            if(device1Socket!=null&&device2Socket!=null){
                                //Once 2 devices connect successfully, create 2 UDP sockets to connect with the server. 
                                createSocket();
                                //Start 2 streamings by create 2 threads for 2 wearable devices.
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        runStreamingDevice1();
                                    }
                                }).start();
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        runStreamingDevice2();
                                    }
                                }).start();
                                MainActivity.instance.serverConnectionStateTV.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.instance.serverConnectionStateTV.setText("Server connection state: Start streaming!");
                                    }
                                });
                                MainActivity.instance.device1ConnectionStateTV.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.instance.device1ConnectionStateTV.setText("Device 1 connection state: Start streaming!");
                                    }
                                });
                                MainActivity.instance.device2ConnectionStateTV.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.instance.device2ConnectionStateTV.setText("Device 2 connection state: Start streaming!");
                                    }
                                });
                                break;
                            }
                        } catch (IOException e) {
                            break;
                        }
                    }
                }
                Log.e("error", "No appropriate paired devices.");
            } else {
                Log.e("error", "Bluetooth is disabled.");
            }
        }
    }
    public void runStreamingDevice1() {
        byte[] buff = new byte[128];
        int count=0;
        ArrayList listData=new ArrayList();
        while (true) {
            try {
                bisDevice1.read(buff,0,buff.length);
                String text = new String(buff, "UTF-8");
                char[] chars = text.toCharArray();
                int i;
                for (i = 0; i < chars.length; i++) {
                    if(chars[i]=='\0')
                        break;
                }
                String dataVal = String.valueOf(chars,0,i);
                System.out.println("Device 1:"+dataVal);

                listData.add(dataVal);
                if(count>100) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(baos);
                    oos.writeObject(listData);
                    oos.flush();
                    byte[] buffer = baos.toByteArray();
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ipServer), 5556);
                    socketDevice1DatagramSocket.send(packet);
                    count=0;
                    listData.clear();
                }
                count++;
            } catch (final Exception e) {
                System.out.println(e.toString());
            }
        }
    }

    public void runStreamingDevice2() {
        byte[] buff = new byte[128];
        int count =0;
        ArrayList listData=new ArrayList();
        while (true) {
            try {
                bisDevice2.read(buff,0,buff.length);
                String text = new String(buff, "UTF-8");
                char[] chars = text.toCharArray();
                int i;
                for (i = 0; i < chars.length; i++) {
                    if(chars[i]=='\0')
                        break;
                }
                String dataVal = String.valueOf(chars,0,i);
                System.out.println("Device 2:"+dataVal);

                listData.add(dataVal);
                if(count>100) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(baos);
                    oos.writeObject(listData);
                    oos.flush();
                    byte[] buffer = baos.toByteArray();
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ipServer), 5557);
                    socketDevice2DatagramSocket.send(packet);
                    count=0;
                    listData.clear();
                }
                count++;
            } catch (final Exception e) {
                System.out.println(e.toString());
            }
        }
    }

}
