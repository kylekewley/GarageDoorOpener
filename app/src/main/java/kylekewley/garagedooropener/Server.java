package kylekewley.garagedooropener;

import java.util.ArrayList;

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
}
