/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visualrecording;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import org.jfree.ui.RefineryUtilities;
import visualrecording.JFreeSensorData;

/**
 *
 * @author habui
 */
public class VisualRecording {
    private BufferedReader inputReadDevice1;
    private BufferedReader inputReadDevice2;
    private JFreeSensorData device1AcceData=new JFreeSensorData();
    private JFreeSensorData device1GyroData=new JFreeSensorData();
    private JFreeSensorData device2AcceData=new JFreeSensorData();
    private JFreeSensorData device2GyroData=new JFreeSensorData();
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd;HH:mm:ss.SSS");
    private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd;HH:mm:ss.SSS");
    public VisualRecording() throws IOException {
        Runtime.getRuntime().exec(new String[] { "/usr/bin/vlc", "Recorder/video.mp4" });
        new Thread(new Runnable() {
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
        }).start();
        getFile();
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
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run()
            {
                try{
                    inputReadDevice1.close();
                    inputReadDevice2.close();
                    System.out.println("Files and sockets have been closed!");
                }catch(Exception e){
//                    System.out.println(e.toString());
                }
            }
        });
    }
    
    public static void main(String[] args) throws IOException {
        VisualRecording main=new VisualRecording();
    }
    
    private void getFile() {
        try {
            String fileNameDevice1 ="Recorder/data_device_1.txt";
            inputReadDevice1 = new BufferedReader(new FileReader(fileNameDevice1));
            String fileNameDevice2 ="Recorder/data_device_2.txt";
            inputReadDevice2 = new BufferedReader(new FileReader(fileNameDevice2));
        } catch (Exception e) {
//            System.out.println(e.toString());
        }
    }

    private void device1SocketStreaming() {
         try {
            Scanner scanner=new Scanner(inputReadDevice1);
            String data="";
            String nextData="";
            while(scanner.hasNextLine()){
                if(nextData.equals(""))
                    data=(String) scanner.nextLine();
                if(!data.equals("")){
                    String[] splited = data.split("\\s+");
                    device1AcceData.x=Float.parseFloat(splited[1]);
                    device1AcceData.y=Float.parseFloat(splited[2]);
                    device1AcceData.z=Float.parseFloat(splited[3]);
                    device1GyroData.x=Float.parseFloat(splited[4]);
                    device1GyroData.y=Float.parseFloat(splited[5]);
                    device1GyroData.z=Float.parseFloat(splited[6]);
                    System.out.println("Socket device 1: "+data);
                    if(scanner.hasNextLine()){
                        nextData=scanner.nextLine();
                        String[] splited2 = nextData.split("\\s+");
                        long waittingTime=sdf.parse(splited2[0]).getTime()-sdf.parse(splited[0]).getTime();
                        long startTime2 = System.currentTimeMillis();
                        long currentTime2 = startTime2;
                        while (currentTime2 < startTime2 + waittingTime) {
                            currentTime2 = System.currentTimeMillis();
                        }
                        data=nextData;
                    }
                }
            }
        }catch(Exception e){
//            System.out.println(e.toString());
        }     
    }
    private void device2SocketStreaming() {
        try {
            Scanner scanner=new Scanner(inputReadDevice2);
            String data="";
            String nextData="";
            while(scanner.hasNextLine()){
                if(nextData.equals(""))
                    data=(String) scanner.nextLine();
                if(!data.equals("")){
                    String[] splited = data.split("\\s+");
                    device2AcceData.x=Float.parseFloat(splited[1]);
                    device2AcceData.y=Float.parseFloat(splited[2]);
                    device2AcceData.z=Float.parseFloat(splited[3]);
                    device2GyroData.x=Float.parseFloat(splited[4]);
                    device2GyroData.y=Float.parseFloat(splited[5]);
                    device2GyroData.z=Float.parseFloat(splited[6]);
                    System.out.println("Socket device 2: "+data);
                    if(scanner.hasNextLine()){
                        nextData=scanner.nextLine();
                        String[] splited2 = nextData.split("\\s+");
                        long waittingTime=sdf2.parse(splited2[0]).getTime()-sdf2.parse(splited[0]).getTime();
                        long startTime2 = System.currentTimeMillis();
                        long currentTime2 = startTime2;
                        while (currentTime2 < startTime2 + waittingTime) {
                            currentTime2 = System.currentTimeMillis();
                        }
                        data=nextData;
                    }
                }
            }
        }catch(Exception e){
//            System.out.println(e.toString());
        }     
    }
}
