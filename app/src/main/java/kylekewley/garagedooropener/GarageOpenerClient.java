package kylekewley.garagedooropener;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import kylekewley.garagedooropener.Constants.ServerParserId;
import kylekewley.garagedooropener.Constants.ClientParserId;

import com.kylekewley.piclient.CustomBufferParser;
import com.kylekewley.piclient.PiClient;
import com.kylekewley.piclient.PiMessage;
import com.kylekewley.piclient.PiMessageCallbacks;
import com.kylekewley.piclient.protocolbuffers.ParseError;
import com.squareup.wire.Message;


import java.util.List;

import kylekewley.garagedooropener.fragments.GarageOpenerFragment;
import kylekewley.garagedooropener.protocolbuffers.GarageStatus;

/**
 * Created by Kyle Kewley on 7/8/14.
 */
public class GarageOpenerClient {

    private static final String TAG = "garage_opener_client";

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
    public enum DoorPosition {
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

    private GarageOpenerView openerView;

    /**
     * Default parameters that are shared between all constructors.
     * This MUST be called by all constructors for the class to work properly.
     */
    private void sharedConstructor(PiClient client) {
        boolean registered = client.getPiParser().registerParserForId(new OpenerParser(),
                ClientParserId.DOOR_CHANGE_CLIENT_ID.getId());
        garageDoors = new GarageDoor[0];
        if (!registered) {
            throw new RuntimeException("The door updater parser was unable to be registered" +
                    "for parser id: " + ClientParserId.DOOR_CHANGE_CLIENT_ID.getId() +
                    " because the id is already used. The door status will not be updated. " +
                    "Please use a unique id for each parser.");
        }

    }
    
    /**
     * Create the class with an unknown number of doors.
     * The class will send a message to the PiServer asking for the number of 
     * doors and update their status.
     *
     * @param client The client that will be used for sending and receiving messages.
     */
    public GarageOpenerClient(PiClient client) {
        sharedConstructor(client);
        this.client = client;

        //Need to update garage door data
        requestGarageDoorStatus();
    }


    /**
     * Create an instance of the class with a known number of doors.
     *
     * @param numDoors  The number of doors to initialize. By default,
     *                  these doors will have IDs of 0...numDoors-1.
     * @param client    The client that will be used for sending and receiving messages.
     */
    public GarageOpenerClient(int numDoors, PiClient client) {
        sharedConstructor(client);
        this.client = client;
        initializeDoorArray(numDoors);
        requestGarageDoorStatus();
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
    public void setDoorStatusAtIndex(int index, DoorPosition newStatus) throws IndexOutOfBoundsException {
        checkDoorIndexBounds(index);

        garageDoors[index].doorPosition = newStatus;

    }

    /**
     * Gets the door status at the given index.
     *
     * @param index The index of the garage door in the array.
     */
    public DoorPosition getDoorStatusAtIndex(int index) throws IndexOutOfBoundsException{
        checkDoorIndexBounds(index);


        return garageDoors[index].doorPosition;
    }

    /**
     * Sets the time the garage status at the given index last changed.
     *
     * @param index The index of the garage door in the array.
     * @param lastStatusChange  The time given by the server that the garage
     *      status last changed.
     */
    public void setLastStatusChangeAtIndex(int index, long lastStatusChange)
            throws IndexOutOfBoundsException {
        checkDoorIndexBounds(index);

        garageDoors[index].lastStatusChange = lastStatusChange;
    }

    /**
     * @param index The index of the garage door in the array.
     *
     * @return  The time since the garage status last changed.
     */
    public long getLastStatusChangeAtIndex(int index) throws IndexOutOfBoundsException {
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

    public void setOpenerView(GarageOpenerView view) {
        this.openerView = view;

        openerView.setGarageOpenerClient(this);
        openerView.setGarageDoorCount(getNumberOfGarageDoors());
    }

    public int getDoorCount() {
        synchronized (garageDoors) {
            return garageDoors.length;
        }
    }

    public boolean triggerDoor(int doorIndex) {
        if (doorIndex < getDoorCount() && client != null) {
            //TODO: Code to send a trigger message.
            Log.d(TAG, "Trigger garage door: " + doorIndex);
            return true;
        }

        return false;
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
            garageDoors[i] = new GarageDoor(i, -1, DoorPosition.DOOR_NOT_CLOSED);
        }
    }

    private void requestGarageDoorStatus() {
        if (client != null) {
            PiMessage statusRequest = new PiMessage(ServerParserId.GARAGE_STATUS_ID.getId());

            statusRequest.setMessageCallbacks(new PiMessageCallbacks(GarageStatus.class) {
                @Override
                public void serverReturnedData(byte[] data, PiMessage message) {

                }

                @Override
                public void serverRepliedWithMessage(Message response, PiMessage sentMessage) {
                    GarageStatus status = (GarageStatus)response;
                    synchronized (garageDoors) {
                        parseDoorStatusList(status.doors);
                        updateInterfaceChanges();
                    }
                }
                @Override
                public void serverSuccessfullyParsedMessage(PiMessage message) {

                }

                @Override
                public void serverReturnedErrorForMessage(ParseError parseError, PiMessage message) {
                    Log.d(TAG, "Error parsing status request message.");
                }
            });

            client.sendMessage(statusRequest);
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
     * Parse the full list of DoorStatus objects and update the array.
     * Currently, the list must include every garage door.
     */
    private void parseDoorStatusList(List<GarageStatus.DoorStatus> doorStatusList) {
        initializeDoorArray(doorStatusList.size());
        for (GarageStatus.DoorStatus status : doorStatusList) {
            int index = status.garageId;
            boolean closed = status.isClosed;

            setLastStatusChangeAtIndex(index, status.timestamp);

            if (closed) {
                //Easy case
                setDoorStatusAtIndex(index, DoorPosition.DOOR_CLOSED);
            }else {
                //Check if it is still moving
                if (isGarageMoving(status.timestamp)) {
                    setDoorStatusAtIndex(index, DoorPosition.DOOR_MOVING);
                }else {
                    setDoorStatusAtIndex(index, DoorPosition.DOOR_NOT_CLOSED);
                }
            }
        }
    }

    private void updateInterfaceChanges() {
        openerView.setGarageDoorCount(getDoorCount());

        for (int i = 0; i < getDoorCount(); i++) {
            openerView.updateGarageView(i, garageDoors[i].doorPosition);
        }
    }




    /*
     * Plain data storage class
     */
    private class GarageDoor {
        /**
         * The current status of the door.
         */
        public DoorPosition doorPosition;

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
         * @param doorPosition        The status of the garage door.
         */
        public GarageDoor(int doorId, long lastStatusChange,
                DoorPosition doorPosition) {
            this.doorId = doorId;
            this.lastStatusChange = lastStatusChange;
            this.doorPosition = doorPosition;
        }
    }

    private class OpenerParser extends CustomBufferParser<GarageStatus> {

        @Override
        public void parse(GarageStatus message) {
            parseDoorStatusList(message.doors);
            updateInterfaceChanges();
        }
    }


}
