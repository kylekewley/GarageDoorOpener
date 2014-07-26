package kylekewley.garagedooropener;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ServerOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = "server_opener_helper";

    private static final int DATABASE_VERSION = 2;
    private static final String SERVER_TABLE_NAME = "server";
    private static final String DOOR_TABLE_NAME = "door_name";

    private static enum ServerColumn {
        COLUMN_SERVER_ID("server_id", 0, "INTEGER PRIMARY KEY"),
        COLUMN_IP("ip_address", 1, "TEXT"),
        COLUMN_PORT("port", 2, "INTEGER"),
        COLUMN_SERVER_NAME("server_name", 3, "TEXT");

        private final String name;
        private final int index;
        private final String type;

        ServerColumn(String name, int index, String type) {
            this.name = name;
            this.index = index;
            this.type = type;
        }

        public String getName() {
            return name;
        }
        public int getIndex() {
            return index;
        }
        public String getType() {
            return type;
        }
    }

    private static enum DoorNameColumn {
        COLUMN_DOOR_SERVER("door_server", 0, "INTEGER"),
        COLUMN_DOOR_NAME("door_name", 1, "TEXT");

        private final String name;
        private final int index;
        private final String type;

        DoorNameColumn(String name, int index, String type) {
            this.name = name;
            this.index = index;
            this.type = type;
        }

        public String getName() {
            return name;
        }
        public int getIndex() {
            return index;
        }
        public String getType() {
            return type;
        }
    }


    private static final String SERVER_TABLE_CREATE;
    private static final String DOOR_TABLE_CREATE;

    static {
        StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS " + SERVER_TABLE_NAME + "(");

        for (ServerColumn s : ServerColumn.values()) {
            sb.append(s.getName());
            sb.append(" ");
            sb.append(s.getType());
            sb.append(", ");
        }

        //Delete the last two characters ", "
        sb.delete(sb.length()-2, sb.length());
        sb.append(");");
        SERVER_TABLE_CREATE = sb.toString();
    }
    static {
        StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS " + DOOR_TABLE_NAME + "(");

        for (DoorNameColumn doorColumn : DoorNameColumn.values()) {
            sb.append(doorColumn.getName())
                    .append(" ")
                    .append(doorColumn.getType())
                    .append(", ");
        }

        sb.append("FOREIGN KEY(")
        .append(DoorNameColumn.COLUMN_DOOR_SERVER.getName())
        .append(") REFERENCES ")
        .append(SERVER_TABLE_NAME)
        .append("(")
        .append(ServerColumn.COLUMN_SERVER_ID.getName())
        .append("));");

        DOOR_TABLE_CREATE = sb.toString();
    }


    private static final String DATABASE_NAME = "server_data";

    ServerOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        getWritableDatabase();
        boolean result = verifyDatabase();
        if (result == false) {
            createNewTable();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createNewTable();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * Verifies that the two tables have the correct number of columns.
     * @return  True if the tables have the correct number of columns. False if
     * the number of columns differs from the column enums, or if an exception was raised.
     */
    private boolean verifyDatabase() {
        String serverQuery = "PRAGMA table_info(" + SERVER_TABLE_NAME + ");";
        String doorsQuery = "PRAGMA table_info(" + DOOR_TABLE_NAME + ");";

        try {

            Cursor c = getWritableDatabase().rawQuery(serverQuery, null);
            int count = c.getCount();
            int totalCount = ServerColumn.values().length;
            if (count != totalCount)
                return false;

            c = getWritableDatabase().rawQuery(doorsQuery, null);
            count = c.getCount();
            totalCount = DoorNameColumn.values().length;
            if (count != totalCount)
                return false;

        }catch (Exception e) {
            return false;
        }

        return true;
    }

    public List<Server> getSavedServers() {
        String serverQuery = "SELECT * FROM " + SERVER_TABLE_NAME;
        Cursor c = getWritableDatabase().rawQuery(serverQuery, null);

        String doorQuery = "SELECT * FROM " + DOOR_TABLE_NAME + " WHERE "
                + DoorNameColumn.COLUMN_DOOR_SERVER.getName() + " == ?";

        ArrayList<Server> servers = new ArrayList<Server>();

        while (c.moveToNext()) {
            Server s = new Server();
            s.ipAddress = c.getString(ServerColumn.COLUMN_IP.getIndex());
            s.port = c.getInt(ServerColumn.COLUMN_PORT.getIndex());
            s.serverId = c.getInt(ServerColumn.COLUMN_SERVER_ID.getIndex());
            s.serverName = c.getString(ServerColumn.COLUMN_SERVER_NAME.getIndex());

            String args[] = {Long.toString(s.serverId)};
            Cursor doorNames = getWritableDatabase().rawQuery(doorQuery, args);

            //Query for the door names
            while (doorNames.moveToNext()) {
                s.doorNames.add(doorNames.getString(DoorNameColumn.COLUMN_DOOR_NAME.getIndex()));
            }

            //Add to the array
            servers.add(s);

        }

        return servers;
    }

    public long updateServer(Server server) {
        if (getWritableDatabase() == null) return 0;

        ContentValues content = new ContentValues();
        content.put(ServerColumn.COLUMN_IP.getName(), server.ipAddress);
        content.put(ServerColumn.COLUMN_PORT.getName(), server.port);
        content.put(ServerColumn.COLUMN_SERVER_NAME.getName(), server.serverName);

        String where = ServerColumn.COLUMN_SERVER_ID.getName() + " == ?";
        String args[] = {Long.toString(server.serverId)};

        return (long) getWritableDatabase().update(SERVER_TABLE_NAME, content, where, args);
    }

    public boolean addNewServer(Server server) {
        if (getWritableDatabase() == null) return false;

        ContentValues content = new ContentValues();
        content.put(ServerColumn.COLUMN_IP.getName(), server.ipAddress);
        content.put(ServerColumn.COLUMN_PORT.getName(), server.port);
        content.put(ServerColumn.COLUMN_SERVER_NAME.getName(), server.serverName);

        long result = getWritableDatabase().insert(SERVER_TABLE_NAME, null, content);

        if (result == -1) return false;

        server.serverId = result;
        return true;
    }

    public boolean addNewDoorName(Server server, String doorName) {
        if (getWritableDatabase() == null) return false;

        ContentValues content = new ContentValues();
        content.put(DoorNameColumn.COLUMN_DOOR_SERVER.getName(), server.serverId);
        content.put(DoorNameColumn.COLUMN_DOOR_NAME.getName(), doorName);

        long result = getWritableDatabase().insert(DOOR_TABLE_NAME, null, content);

        return result != -1;
    }

    public boolean deleteServer(Server server) {
        if (getWritableDatabase() == null) return false;

        String where = ServerColumn.COLUMN_SERVER_ID.getName() + " == ?";
        String args[] = {Long.toString(server.serverId)};

        int numDeleted = getWritableDatabase().delete(SERVER_TABLE_NAME, where, args);
        deleteServerDoors(server);

        return numDeleted != 0;
    }

    private void deleteServerDoors(Server server) {
        if (getWritableDatabase() == null) return;

        String where = DoorNameColumn.COLUMN_DOOR_SERVER.getName() + " == ?";
        String args[] = {Long.toString(server.serverId)};

        getWritableDatabase().delete(DOOR_TABLE_NAME, where, args);

    }

    public int deleteDoorName(Server server, String doorName) {
        if (getWritableDatabase() == null) return 0;

        String where = DoorNameColumn.COLUMN_DOOR_SERVER.getName() + " == ? AND "
                + DoorNameColumn.COLUMN_DOOR_NAME.getName() + " == ?";
        String args[] = {Long.toString(server.serverId), doorName};

        return getWritableDatabase().delete(DOOR_TABLE_NAME, where, args);

    }

    private void createNewTable() {
        getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + DOOR_TABLE_NAME + ";");
        getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + SERVER_TABLE_NAME + ";");

        getWritableDatabase().execSQL(SERVER_TABLE_CREATE);
        getWritableDatabase().execSQL(DOOR_TABLE_CREATE);

    }

}
