package kylekewley.garagedooropener;

/**
 * Created by Kyle Kewley on 7/9/14.
 *
 * An interface to store all constant values used for the garage door opener.
 */
public interface Constants {
    enum ServerParserId {
        PING_ID(1),
        PARSE_ERROR_ID(2),
        GROUP_REGISTRATION_ID(3),

        GARAGE_OPENER_ID(1001),
        GARAGE_STATUS_ID(1002);

        private final int id;

        private ServerParserId(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
}
