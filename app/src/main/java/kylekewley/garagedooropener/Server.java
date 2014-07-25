package kylekewley.garagedooropener;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by kylekewley on 7/25/14.
 */
public class Server {
    public long serverId;
    public String ipAddress;
    public int port;
    public String serverName;

    public ArrayList<String> doorNames;

    public Server() {
        doorNames = new ArrayList<String>();
    }

    public Server(String ipAddress, int port, String serverName) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.serverName = serverName;
    }

    @Override
    public String toString() {
        return "Name: " + serverName
                + "\nPort: " + Integer.toString(port)
                + "\nIP Address: " + ipAddress
                + "\nServer ID: " + Long.toString(serverId)
                + "\nDoor Names: " + doorNames.toString();
    }
}
