package kylekewley.garagedooropener;

import java.text.SimpleDateFormat;

/**
 * Created by Kyle Kewley on 7/9/14.
 *
 * An interface to store all constant values used for the garage door opener.
 */
public final class Constants {

    public static final String GARAGE_GROUP_ID = "garage";

    public final static SimpleDateFormat epochDateTimeFormat = new SimpleDateFormat("hh:mm:ss a | EEEE MMMM dd, yyyy");
    public final static SimpleDateFormat epochDateFormat = new SimpleDateFormat("EEEE MMMM dd, yyyy");


    public enum ServerParserId {
        GARAGE_META_ID(1000),       //Used to get information about the system such as the number
                                    //of garage doors.
        GARAGE_OPENER_ID(1001),     //Used to send a door trigger message to the server.
        GARAGE_STATUS_ID(1002),     //Used to send a current garage status request to the server.
        GARAGE_HISTORY_ID(1003);    //Used to send a garage history request to the server.

        private final int id;

        private ServerParserId(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public enum ClientParserId {

        DOOR_CHANGE_CLIENT_ID(9999); //Used for updating the current garage status asynchronously.

        private final int id;

        private ClientParserId(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
}
