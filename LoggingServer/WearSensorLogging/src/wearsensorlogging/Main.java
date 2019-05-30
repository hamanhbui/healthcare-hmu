/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wearsensorlogging;

import java.awt.EventQueue;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import org.jfree.ui.RefineryUtilities;

/**
 *
 * @author habui
 */
public class Main { 
    private BufferedWriter outputWriterDevice1;
    private BufferedWriter outputWriterDevice2;
    private JFreeSensorData device1AcceData=new JFreeSensorData();
    private JFreeSensorData device1GyroData=new JFreeSensorData();
    private JFreeSensorData device2AcceData=new JFreeSensorData();
    private JFreeSensorData device2GyroData=new JFreeSensorData();
    private DatagramSocket device1DatagramSocket;
    private DatagramSocket device2DatagramSocket;
    public Main() {
        System.out.println("Waiting for the connectivity....");
        try {
            ServerSocket serverSocket=new ServerSocket(5550);
            while(true) {
                Socket socket=serverSocket.accept();
                ObjectInputStream ois=new ObjectInputStream(socket.getInputStream());
                String ping= (String) ois.readObject();
                if(ping.equals("Ping!")){
                    ObjectOutputStream oos=new ObjectOutputStream(socket.getOutputStream());
                    oos.writeObject("Accepted!");
                    oos.flush();
                    serverSocket.close();
                    break;
                }
            }
        }catch(Exception e){
            System.out.println(e.toString());
        }
        
        createFile();
        new Thread(new Runnable() {
            @Override
            public void run() {
                device1SocketStreaming();
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                device2SocketStreaming();
            }
        }).start();
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFreeSensorGraph device1AcceGraph = new JFreeSensorGraph("Device 1 Accelerometer",20,device1AcceData);
                device1AcceGraph.pack();
                RefineryUtilities.centerFrameOnScreen(device1AcceGraph);
                device1AcceGraph.setVisible(true);
                device1AcceGraph.start();
                
                JFreeSensorGraph device1GyroGraph = new JFreeSensorGraph("Device 1 Gyroscope",600,device1GyroData);
                device1GyroGraph.pack();
                RefineryUtilities.centerFrameOnScreen(device1GyroGraph);
                device1GyroGraph.setVisible(true);
                device1GyroGraph.start();
                
                JFreeSensorGraph device2AcceGraph = new JFreeSensorGraph("Device 2 Accelerometer",20,device2AcceData);
                device2AcceGraph.pack();
                RefineryUtilities.centerFrameOnScreen(device2AcceGraph);
                device2AcceGraph.setVisible(true);
                device2AcceGraph.start();
                
                JFreeSensorGraph device2GyroGraph = new JFreeSensorGraph("Device 2 Gyroscope",600,device2GyroData);
                device2GyroGraph.pack();
                RefineryUtilities.centerFrameOnScreen(device2GyroGraph);
                device2GyroGraph.setVisible(true);
                device2GyroGraph.start();
            }
        });
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run()
            {
                try{
                    outputWriterDevice1.close();
                    outputWriterDevice2.close();
                    device1DatagramSocket.close();
                    device2DatagramSocket.close();
                    System.out.println("Files and sockets have been closed!");
                }catch(Exception e){
//                    System.out.println(e.toString());
                }
            }
        });
    }
    
    public static void main(String[] args) {
        Main main=new Main();
    }
    
    private void createFile() {
        try {
            String folderName = "Recorder_" + new SimpleDateFormat("yyyy_MM_dd_HH_mm").format(new Date());
            File file = new File(folderName);
            if (!file.exists()) {
                file.mkdirs();
            }
            String fileNameDevice1 = file.getAbsolutePath() + "/" + "data_device_1.txt";
            outputWriterDevice1 = new BufferedWriter(new FileWriter(fileNameDevice1));
            String fileNameDevice2 = file.getAbsolutePath() + "/" + "data_device_2.txt";
            outputWriterDevice2 = new BufferedWriter(new FileWriter(fileNameDevice2));
        } catch (Exception e) {
//            System.out.println(e.toString());
        }
    }

    private void device1SocketStreaming() {
         try {
            device1DatagramSocket = new DatagramSocket(5556);
            byte[] buffer = new byte[10240];
            while(true){
                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                device1DatagramSocket.receive(receivePacket);
                ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
                ObjectInputStream ois = new ObjectInputStream(bais);
                ArrayList listData=(ArrayList) ois.readObject();
                
                for(int i=0;i<listData.size();++i){
                    String data=(String) listData.get(i);
                    String[] splited = data.split("\\s+");
                    device1AcceData.x=Float.parseFloat(splited[1]);
                    device1AcceData.y=Float.parseFloat(splited[2]);
                    device1AcceData.z=Float.parseFloat(splited[3]);
                    device1GyroData.x=Float.parseFloat(splited[4]);
                    device1GyroData.y=Float.parseFloat(splited[5]);
                    device1GyroData.z=Float.parseFloat(splited[6]);
                    outputWriterDevice1.write(data + "\r\n");
                    System.out.println("Socket device 1: "+data);
                    long startTime2 = System.currentTimeMillis();
                    long currentTime2 = startTime2;
                    while (currentTime2 < startTime2 + 10) {
                        currentTime2 = System.currentTimeMillis();
                    }
                }
            }
        }catch(Exception e){
//            System.out.println(e.toString());
        }     
    }
    private void device2SocketStreaming() {
        try {
            device2DatagramSocket = new DatagramSocket(5557);
            byte[] buffer = new byte[10240];
            while(true){
                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                device2DatagramSocket.receive(receivePacket);
                ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
                ObjectInputStream ois = new ObjectInputStream(bais);
                ArrayList listData=(ArrayList) ois.readObject();
                
                for(int i=0;i<listData.size();++i){
                    String data=(String) listData.get(i);
                    String[] splited = data.split("\\s+");
                    device2AcceData.x=Float.parseFloat(splited[1]);
                    device2AcceData.y=Float.parseFloat(splited[2]);
                    device2AcceData.z=Float.parseFloat(splited[3]);
                    device2GyroData.x=Float.parseFloat(splited[4]);
                    device2GyroData.y=Float.parseFloat(splited[5]);
                    device2GyroData.z=Float.parseFloat(splited[6]);
                    outputWriterDevice2.write(data + "\r\n");
                    System.out.println("Socket device 2: "+data);
                    long startTime2 = System.currentTimeMillis();
                    long currentTime2 = startTime2;
                    while (currentTime2 < startTime2 + 10) {
                        currentTime2 = System.currentTimeMillis();
                    }
                }
            }
        }catch(Exception e){
//            System.out.println(e.toString());
        }     
    }
}
