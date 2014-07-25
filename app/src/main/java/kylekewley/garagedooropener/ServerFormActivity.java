package kylekewley.garagedooropener;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by kylekewley on 7/24/14.
 */
public class ServerFormActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ServerOpenHelper openHelper = new ServerOpenHelper(this);

        Server s = new Server();
        s.serverName = "Test";
        s.ipAddress = "192.168.1.100";
        s.port = 10102;

        openHelper.addNewServer(s);

        Log.d("Test", "Server id = " + s.serverId);
    }
}
