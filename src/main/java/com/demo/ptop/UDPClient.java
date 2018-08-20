package com.demo.ptop;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class UDPClient {

    ScheduledThreadPoolExecutor threadPool;

    public UDPClient(int port,String serverIP,int serverPort) {
        threadPool = new ScheduledThreadPoolExecutor(2);
        final Gson gson = new GsonBuilder()
                //.setPrettyPrinting()
                .create();
        DatagramSocket datagramSocket = null;
        try {
            datagramSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        DatagramSocket finalDatagramSocket = datagramSocket;
        Runnable connectTask = new Runnable() {
            public void run() {
                byte[] receiveDatas = new byte[0];
                DatagramPacket receivePacket = null;
                try {
                    byte[] sendDatas;
                    receiveDatas = new byte[1024];
                    sendDatas = "hello".getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendDatas, sendDatas.length, InetAddress.getByName(serverIP), serverPort);
                    finalDatagramSocket.send(sendPacket);
                    receivePacket = new DatagramPacket(receiveDatas,1024);
                    finalDatagramSocket.receive(receivePacket);
                    receiveDatas = receivePacket.getData();
                    String receiveStr = new String(receiveDatas);
                    String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                    System.out.println(timeStamp + " client receive:" + receiveStr);
                    Type type = new TypeToken<List<Address>>(){}.getType();
                    List<Address> addressList = gson.fromJson(receiveStr.trim(),type);
                    System.out.println(timeStamp + " client receive:" + addressList);
                    for (Address address : addressList) {
                        System.out.println(address.toString());
                    }
                    Address
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println(e.toString());
                }
            }
        };
        threadPool.scheduleWithFixedDelay(connectTask, 0, 10, TimeUnit.SECONDS);
        //threadPool.execute(connectTask);
        //new Thread(connectTask).start();
    }

    public static void main(String[] args) {

        UDPClient client = new UDPClient(8333,"127.0.0.1",8334);
    }
}
