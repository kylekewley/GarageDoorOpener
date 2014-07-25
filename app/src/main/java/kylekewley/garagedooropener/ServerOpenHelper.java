package kylekewley.garagedooropener;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by kylekewley on 7/24/14.
 */
public class ServerOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = "server_opener_helper";

    private static final int DATABASE_VERSION = 2;
    private static final String SERVER_TABLE_NAME = "server";
    private static final String DOOR_TABLE_NAME = "door_name";

    private static enum ServerColumn {
        COLUMN_SERVER_ID("server_id", 0, "INTEGER PRIMARY KEY"),
        COLUMN_IP("ip_address", 1, "TEXT"),
        COLUMN_PORT("port", 2, "INTEGER"),
        COLUMN_SERVER_NAME("server_name", 4, "TEXT");

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

    private SQLiteDatabase database;

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
        Log.d(TAG, SERVER_TABLE_CREATE);
        Log.d(TAG, DOOR_TABLE_CREATE);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        database = db;
        db.execSQL(SERVER_TABLE_CREATE);
        db.execSQL(DOOR_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public List<Server> getSavedServers() {
        String serverQuery = "SELECT * FROM " + SERVER_TABLE_NAME;
        Cursor c = database.rawQuery(serverQuery, null);

        String doorQuery = "SELECT * FROM " + DOOR_TABLE_NAME + " WHERE "
                + DoorNameColumn.COLUMN_DOOR_SERVER.getName() + " EQUALS ?";

        ArrayList<Server> servers = new ArrayList<Server>();

        do {
            Server s = new Server();
            s.ipAddress = c.getString(ServerColumn.COLUMN_IP.getIndex());
            s.port = c.getInt(ServerColumn.COLUMN_PORT.getIndex());
            s.serverId = c.getInt(ServerColumn.COLUMN_SERVER_ID.getIndex());
            s.serverName = c.getString(ServerColumn.COLUMN_SERVER_NAME.getIndex());

            String args[] = {Long.toString(s.serverId)};
            Cursor doorNames = database.rawQuery(doorQuery, args);

            //Query for the door names
            do {
                s.doorNames.add(doorNames.getString(DoorNameColumn.COLUMN_DOOR_NAME.getIndex()));
            }while (doorNames.moveToNext());

            //Add to the array
            servers.add(s);

        } while(c.moveToNext());

        return servers;
    }

    public void updateServer(Server server) {

    }

    public void addNewServer(Server server) {
        String query = "INSERT INTO " + SERVER_TABLE_NAME + "("
                + ServerColumn.COLUMN_IP.getName() + ", "
                + ServerColumn.COLUMN_PORT.getName() + ", "
                + ServerColumn.COLUMN_SERVER_NAME.getName() + ") VALUES (*, *, *)";
        String args[] = {server.ipAddress, Long.toString(server.port), server.serverName};

        database.rawQuery(query, args);

        server.serverId = getLastInsertedId();
    }

    private long getLastInsertedId() {
        String query = "SELECT ROWID from MYTABLE order by ROWID DESC limit 1";
        Cursor c = database.rawQuery(query, null);
        if (c != null && c.moveToFirst()) {
            return c.getLong(0); //The 0 is the column index, we only have 1 column, so the index is 0
        }
        return -1;
    }

}
