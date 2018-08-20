package com.demo.ptop;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UDPServer {

    List<Address> addressList;

    public UDPServer(){
        addressList = new ArrayList<>();
        Gson gson = new GsonBuilder()
                //.setPrettyPrinting()
                .create();
        DatagramPacket receivePacket = null;
        try {
            MulticastSocket multicastSocket = new MulticastSocket(8334);
            while (true) {
                System.out.println("listenning...");

                byte[] receiveData = new byte[1024];
                byte[] sendData = new byte[1024];
                receivePacket = new DatagramPacket(receiveData, 1024);
                multicastSocket.receive(receivePacket);
                Address address = new Address(receivePacket.getAddress().getHostName(), receivePacket.getPort());
                if (!addressList.contains(address)) {
                    addressList.add(address);
                }
                String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                System.out.println(timeStamp + " server receive:" + address.toString());
                System.out.println(timeStamp + " server receive:" + new String(receivePacket.getData()));
                List<Address> backList = new ArrayList<>();
                for (Address addr : addressList) {
                    if (addr.equals(address)) {
                        continue;
                    }
                    backList.add(addr);
                }
                String backData = gson.toJson(backList);
                System.out.println(timeStamp + " server send:" + backData);
                sendData = backData.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, receivePacket.getAddress(), receivePacket.getPort());
                multicastSocket.send(sendPacket);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.toString());
        }

    }

    public static void main(String[] args) {
        UDPServer server = new UDPServer();
    }
}
