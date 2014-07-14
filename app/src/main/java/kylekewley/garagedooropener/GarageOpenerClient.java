package kylekewley.garagedooropener;

import com.kylekewley.piclient.CustomBufferParser;
import com.kylekewley.piclient.PiClient;
import com.kylekewley.piclient.PiMessage;

import kylekewley.garagedooropener.protocolbuffers.GarageStatus;

/**
 * Created by Kyle Kewley on 7/8/14.
 */
public class GarageOpenerClient {

    /**
     * The time it takes the door to go from totally closed to totally open in seconds.
     */
    public static final int DOOR_CLOSE_TIME = 14;

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
     *
     * @param client The client that will be used for sending and receiving messages
     */
    public GarageOpenerClient(PiClient client) {

    }


    /**
     * Create an instance of the class with a known number of doors.
     *
     * @param numDoors  The number of doors to initialize. By default,
     *                  these doors will have IDs of 0...numDoors-1.
     */
    public GarageOpenerClient(int numDoors) {
        initializeDoorArray(numDoors);
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
    public void setDoorStatusAtIndex(int index, DoorStatus newStatus) throws IndexOutOfBoundsException {
        checkDoorIndexBounds(index);

        garageDoors[index].doorStatus = newStatus;

    }

    /**
     * Gets the door status at the given index.
     *
     * @param index The index of the garage door in the array.
     */
    public DoorStatus getDoorStatusAtIndex(int index) {
        checkDoorIndexBounds(index);


        return garageDoors[index].doorStatus;
    }

    /**
     * Sets the time the garage status at the given index last changed.
     *
     * @param index The index of the garage door in the array.
     * @param lastStatusChange  The time given by the server that the garage
     *      status last changed.
     */
    public void setLastStatusChangeAtIndex(int index, long lastStatusChange) {
        checkDoorIndexBounds(index);

        garageDoors[index].lastStatusChange = lastStatusChange;
    }

    /**
     * @param index The index of the garage door in the array.
     *
     * @return  The time since the garage status last changed.
     */
    public long getLastStatusChangeAtIndex(int index) {
        checkDoorIndexBounds(index);

        return garageDoors[index].lastStatusChange;
    }


    /**
     * @return  The number of garage doors tracked by the client.
     */
    public int getNumberOfGarageDoors() {
        if (garageDoors == null)
            return 0;
        return garageDoors.length;
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
        return (((System.currentTimeMillis()/1000) - openTime) < DOOR_CLOSE_TIME);
    }

    /*
     * Private Methods
     */

    /**
     * Creates a new array with the given number of doors set to the parameters:
     *      doorId = The index of the door.
     *      lastStatusChange = -1
     *      doorStatus = DOOR_NOT_CLOSED
     *
     * @param numDoors
     */
    private void initializeDoorArray(int numDoors) {
        garageDoors = new GarageDoor[numDoors];

        for (int i = 0; i < numDoors; i++) {
            garageDoors[i] = new GarageDoor(i, -1, DoorStatus.DOOR_NOT_CLOSED);
        }
    }

    /**
     * Throws an exception if the index is out of bounds for the garageDoors array.
     *
     * @param index The index to check.
     */
    private void checkDoorIndexBounds(int index) throws IndexOutOfBoundsException {
        if (index >= getNumberOfGarageDoors()) {
            throw new IndexOutOfBoundsException("The index " + index +
                    " was greater than the number of garage doors: " + getNumberOfGarageDoors());
        }
    }

    /**
     * Gets the number of garage doors by sending a request to the server.
     *
     * @param piClient  A piClient instance that the message will be sent through.
     */
    private void requestNumberOfDoors(PiClient piClient) {

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
       public int doorId;


       /**
        * Create a GarageDoor with an ID and no status 
        *
        * @param garageId   The ID of the garage door.
        */
        public GarageDoor(int garageId) {
            this.doorId = garageId;
        }
        
        /**
         * Create a GarageDoor with all parameters set.
         *
         * @param doorId  The ID of the garage door.
         * @param lastStatusChange  The epoch time since the garage status
         *      last changed in seconds.
         * @param doorStatus        The status of the garage door.
         */
        public GarageDoor(int doorId, long lastStatusChange,
                DoorStatus doorStatus) {
            this.doorId = doorId;
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
