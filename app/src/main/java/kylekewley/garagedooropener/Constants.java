package kylekewley.garagedooropener;

/**
 * Created by Kyle Kewley on 7/9/14.
 *
 * An interface to store all constant values used for the garage door opener.
 */
public final class Constants {

    public enum ServerParserId {
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
