package com.dglab.minecraft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

public class NetworkAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger("dglab-minecraft");
    
    private final Map<String, String> networkMap = new LinkedHashMap<>();
    private final List<String> networkNames = new ArrayList<>();
    private int currentIndex = 0;
    
    public NetworkAdapter() {
        refresh();
    }
    
    public void refresh() {
        networkMap.clear();
        networkNames.clear();
        
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            
            while (interfaces != null && interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                
                while (addresses.hasMoreElements()) {
                    InetAddress inetAddress = addresses.nextElement();
                    
                    if (inetAddress instanceof Inet4Address) {
                        String name = networkInterface.getDisplayName();
                        String ip = inetAddress.getHostAddress();
                        networkMap.put(name, ip);
                        networkNames.add(name);
                        LOGGER.debug("Found network adapter: {} -> {}", name, ip);
                    }
                }
            }
            
            if (networkMap.isEmpty()) {
                networkMap.put("localhost", "127.0.0.1");
                networkNames.add("localhost");
            }
            
        } catch (SocketException e) {
            LOGGER.error("Failed to get network adapters", e);
            networkMap.put("localhost", "127.0.0.1");
            networkNames.add("localhost");
        }
    }
    
    public Map<String, String> getNetworkMap() {
        return networkMap;
    }
    
    public List<String> getNetworkNames() {
        return networkNames;
    }
    
    public String getCurrentIpAddress() {
        if (networkNames.isEmpty()) {
            return "127.0.0.1";
        }
        return networkMap.get(networkNames.get(currentIndex));
    }
    
    public String getCurrentNetworkName() {
        if (networkNames.isEmpty()) {
            return "localhost";
        }
        return networkNames.get(currentIndex);
    }
    
    public String getNextIpAddress() {
        if (networkNames.isEmpty()) {
            return "127.0.0.1";
        }
        currentIndex = (currentIndex + 1) % networkNames.size();
        return networkMap.get(networkNames.get(currentIndex));
    }
    
    public void setCurrentByName(String name) {
        int index = networkNames.indexOf(name);
        if (index >= 0) {
            currentIndex = index;
        }
    }
    
    public int size() {
        return networkMap.size();
    }
}
