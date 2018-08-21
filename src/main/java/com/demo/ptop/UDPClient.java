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
import java.util.regex.Pattern;

public class UDPClient {

    ScheduledThreadPoolExecutor threadPool;
    Gson gson;

    public UDPClient(int port,String serverIP,int serverPort) {
        threadPool = new ScheduledThreadPoolExecutor(2);
        gson = new GsonBuilder()
                //.setPrettyPrinting()
                .create();
        DatagramSocket datagramSocket = null;
        try {
            datagramSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        DatagramSocket finalDatagramSocket = datagramSocket;
        Runnable receiveTask = new Runnable() {
            @Override
            public void run() {
                startReceive(finalDatagramSocket);
            }
        };
        new Thread(receiveTask).start();
        Runnable connectTask = new Runnable() {
            public void run() {
                try {
                    byte[] sendDatas;
                    sendDatas = "hello".getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendDatas, sendDatas.length, InetAddress.getByName(serverIP), serverPort);
                    finalDatagramSocket.send(sendPacket);
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

    private void startReceive(DatagramSocket finalDatagramSocket) {
        try {
            while (true) {
                byte[] receiveDatas = new byte[0];
                DatagramPacket receivePacket = null;
                receiveDatas = new byte[1024];
                receivePacket = new DatagramPacket(receiveDatas,1024);
                finalDatagramSocket.receive(receivePacket);
                receiveDatas = receivePacket.getData();
                String receiveStr = new String(receiveDatas).trim();
                String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                Address fromAddr = new Address(receivePacket.getAddress().getHostName(), receivePacket.getPort());
                System.out.println(String.format("%s client receive from %s data:%s",timeStamp,fromAddr.toString(),receiveStr));
                Type type = new TypeToken<List<Address>>(){}.getType();
                List<Address> addressList = gson.fromJson(receiveStr.trim(),type);
                Address message = new Address("hello peer,i am peer.", 1);
                List<Address> messages = new ArrayList<>();
                messages.add(message);
                byte[] sendData = gson.toJson(messages).getBytes();
                for (Address address : addressList) {
                    if (isIP(address.ip)) {
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(address.ip), address.port);
                        finalDatagramSocket.send(sendPacket);
                        System.out.println("send data to " + address.toString());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isIP(String ip) {
        return Pattern.matches("[0-9]+(?:\\.[0-9]+){0,3}",ip);
    }

    public static void main(String[] args) {
        //System.out.println(isIP("111.22.2.3"));
        if (args!=null && args.length!=0) {
            int port = Integer.valueOf(args[0]);
            String serverIP = args[1];
            int serverPort = Integer.valueOf(args[2]);
            UDPClient client = new UDPClient(port,serverIP,serverPort);
        }else {
            UDPClient client = new UDPClient(8333, "127.0.0.1", 8334);
        }
    }
}
