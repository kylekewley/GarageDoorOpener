package kylekewley.garagedooropener;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by kylekewley on 7/24/14.
 */
public class ServerFormActivity extends Activity {
    private static final String TAG = "server_form_activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ServerOpenHelper openHelper = new ServerOpenHelper(this);

        Server s = new Server();
        s.serverName = "Test";
        s.ipAddress = "192.168.1.100";
        s.port = 10102;

        openHelper.addNewServer(s);

        openHelper.addNewDoorName(s, "TestDoor: " + s.serverId);
        Log.d("Test", "Server id = " + s.serverId);
        openHelper.deleteServer(s);
        for (Server server : openHelper.getSavedServers()) {
            Log.d(TAG, server.toString());
        }
    }
}
