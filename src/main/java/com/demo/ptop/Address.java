package com.demo.ptop;

public class Address {
    String ip;
    int port;

    public Address(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    @Override
    public String toString() {
        return String.format("%s:%d", ip, port);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Address) {
            return ip.equals(((Address) obj).ip) && port == ((Address) obj).port;
        }
        return super.equals(obj);
    }
}
