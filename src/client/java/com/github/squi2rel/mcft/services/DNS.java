package com.github.squi2rel.mcft.services;

import com.github.squi2rel.mcft.MCFT;
import org.xbill.DNS.*;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.TimeUnit;

public class DNS {
    public static final int port = 5353;
    public static void init() {
        Thread dns = new Thread(() -> {
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(10);
                    sendReply();
                } catch (InterruptedException ignored) {
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        dns.setDaemon(true);
        dns.start();
        MCFT.LOGGER.info("DNS started on port {}", port);
    }

    private static void sendReply() throws Exception {
        Message response = new Message();
        Header header = response.getHeader();
        header.setFlag(Flags.QR);
        header.setFlag(Flags.AA);
        header.setID(0);
        Name queryName = Name.fromString("_oscjson._tcp.local.");
        Name ptrData = Name.fromString("VRChat-Client-123456._oscjson._tcp.local.");
        Name aName = Name.fromString("VRChat-Client-123456.oscjson.local.");
        response.addRecord(new PTRRecord(queryName, DClass.IN, 4500, ptrData), Section.ANSWER);
        response.addRecord(new TXTRecord(ptrData, DClass.IN, 4500, "txtvers=1"), Section.ADDITIONAL);
        response.addRecord(new SRVRecord(ptrData, DClass.IN, 4500, 0, 0, HTTP.port, aName), Section.ADDITIONAL);
        response.addRecord(new ARecord(aName, DClass.IN, 120, InetAddress.getByName("127.0.0.1")), Section.ADDITIONAL);
        try (DatagramSocket socket = new DatagramSocket()) {
            byte[] data = response.toWire();
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName("224.0.0.251"), 5353);
            socket.send(packet);
        }
    }
}