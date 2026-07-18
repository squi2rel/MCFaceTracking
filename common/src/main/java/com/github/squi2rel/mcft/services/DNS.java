package com.github.squi2rel.mcft.services;

import com.github.squi2rel.mcft.MCFT;
import org.xbill.DNS.*;

import java.net.*;
import java.util.concurrent.TimeUnit;

public class DNS {
    private static final Object lock = new Object();
    private static volatile boolean running;
    private static Thread thread;
    public static final int port = 5353;
    public static void init() {
        synchronized (lock) {
            if (running) return;
            running = true;
            Thread candidate = new Thread(DNS::run);
            candidate.setName("MCFT DNS");
            candidate.setDaemon(true);
            thread = candidate;
            try {
                candidate.start();
            } catch (RuntimeException e) {
                thread = null;
                running = false;
                throw e;
            }
        }
        MCFT.LOGGER.info("DNS started on port {}", port);
    }

    public static void shutdown() {
        Thread current;
        synchronized (lock) {
            running = false;
            current = thread;
            thread = null;
        }
        if (current != null) current.interrupt();
    }

    private static void run() {
        Thread current = Thread.currentThread();
        boolean failureLogged = false;
        while (running && thread == current) {
            try {
                sendReply();
                if (failureLogged) MCFT.LOGGER.info("DNS replies resumed");
                failureLogged = false;
            } catch (Exception e) {
                if (!failureLogged) MCFT.LOGGER.error("DNS reply failed", e);
                failureLogged = true;
            }
            if (!running || thread != current) break;
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException ignored) {
                current.interrupt();
                break;
            }
        }
        synchronized (lock) {
            if (thread == current) {
                thread = null;
                running = false;
            }
        }
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
