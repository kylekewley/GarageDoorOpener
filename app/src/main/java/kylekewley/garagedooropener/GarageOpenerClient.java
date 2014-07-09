package kylekewley.garagedooropener;

import com.kylekewley.piclient.CustomBufferParser;
import com.kylekewley.piclient.PiClient;
import kylekewley.garagedooropener.protocolbuffers.GarageStatus;

/**
 * Created by Kyle Kewley on 7/8/14.
 */
public class GarageOpenerClient {

    /**
     * The time it takes the door to go from totally closed to totally open in seconds.
     */
    private static final int DOOR_CLOSE_TIME = 14;

    /**
     * Enum for the different possible states of a garage door.
     *
     * DOOR_CLOSED: The door is stopped and closed.
     * DOOR_NOT_CLOSED: The door is not closed and is probably not moving.
     * DOOR_MOVING: The door is not closed and is probably moving.
     *
     * If the door went from closed to open in under DOOR_CLOSE_TIME seconds, 
     * it will be marked as DOOR_MOVING.
     *
     * If the door went from closed to open in over DOOR_CLOSE_TIME seconds,
     * it will be marked as DOOR_NOT_CLOSED.
     */
    public enum DoorStatus {
        DOOR_CLOSED,
        DOOR_NOT_CLOSED,
        DOOR_MOVING
    }


    /**
     * The client that will be used for sending and receiving messages
     */
    private PiClient client;


    /**
     * The array used to represent each GarageDoor object.
     * The size of the array is equal to the number of garage door views
     * displayed by the app.
     */
    private GarageDoor[] garageDoors;

    
    /**
     * Create the class with an unknown number of doors.
     * The class will send a message to the PiServer asking for the number of 
     * doors and update their status.
     */
    public GarageOpenerClient(PiClient server) {

    }


    /*
     * Getters and Setters
     */

    /**
     * Changes the door status at the given index.
     *
     * @param index The index of the garage door in the array.
     * @param newStatus The new status of the garage door based on the server
     *  data and the time since last change.
     */
    public void setDoorStatusAtIndex(int index, DoorStatus newStatus) {

    }

    /**
     * Gets the door status at the given index.
     *
     * @param index The index of the garage door in the array.
     */
    public DoorStatus getDoorStatusAtIndex(int index) {
        return DoorStatus.DOOR_CLOSED;
    }

    /**
     * Sets the time the garage status at the given index last changed.
     *
     * @param index The index of the garage door in the array.
     * @param lastStatusChange  The time given by the server that the garage
     *      status last changed.
     */
    public void setLastStatusChangeAtIndex(int index, long lastStatusChange) {

    }

    /**
     * @param index The index of the garage door in the array.
     *
     * @return  The time since the garage status last changed.
     */
    public long getLastStatusChangeAtIndex(int index) {
        return 0;
    }

    /**
     * Method to determine whether a door status should be set to
     * GARAGE_NOT_MOVING or GARAGE_OPEN.
     *
     * @param openTime  The time at which the door went from open to not open in
     *      seconds since Jan 1, 1970.
     *
     * @return  true if moving, false if stopped.
     */
    public boolean isGarageMoving(long openTime) {
        return (((System.currentTimeMillis()/1000) - openTime) > DOOR_CLOSE_TIME);
    }

    /*
     * Plain data storage class
     */
    private class GarageDoor {
        /**
         * The current status of the door.
         */
        public DoorStatus doorStatus;

        /**
         * The last time the garage status changed in seconds since Jan 1, 1970.
         */
       public long lastStatusChange;

       /**
        * The ID of the garage door.
        */
       public int garageId;


       /**
        * Create a GarageDoor with an ID and no status 
        *
        * @param garageId   The ID of the garage door.
        */
        public GarageDoor(int garageId) {
            this.garageId = garageId;
        }
        
        /**
         * Create a GarageDoor with all parameters set.
         *
         * @param garageId  The ID of the garage door.
         * @param lastStatusChange  The epoch time since the garage status
         *      last changed in seconds.
         * @param doorStatus        The status of the garage door.
         */
        public GarageDoor(int garageId, long lastStatusChange, 
                DoorStatus doorStatus) {
            this.garageId = garageId;
            this.lastStatusChange = lastStatusChange;
            this.doorStatus = doorStatus;
        }
    }

    private class OpenerParser extends CustomBufferParser<GarageStatus> {

        @Override
        public void parse(GarageStatus message) {
            int index = message.garageId;
            boolean closed = message.isClosed;

            setLastStatusChangeAtIndex(index, message.timestamp);

            if (closed) {
                //Easy case
                setDoorStatusAtIndex(index, DoorStatus.DOOR_CLOSED);
            }else {
                //Check if it is still moving
                if (isGarageMoving(message.timestamp)) {
                    setDoorStatusAtIndex(index, DoorStatus.DOOR_MOVING);
                }else {
                    setDoorStatusAtIndex(index, DoorStatus.DOOR_NOT_CLOSED);
                }
            }
        }
    }
}
