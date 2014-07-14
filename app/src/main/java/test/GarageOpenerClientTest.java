package test;

import com.kylekewley.piclient.PiClient;

import junit.framework.Assert;
import junit.framework.TestCase;

import kylekewley.garagedooropener.GarageOpenerClient;

public class GarageOpenerClientTest extends TestCase {
    private static final int NUMBER_OF_DOORS = 3;

    private GarageOpenerClient openerClient;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        //Create a new GarageOpenerClient object
        openerClient = new GarageOpenerClient(NUMBER_OF_DOORS);
    }

    public void testSetDoorStatusAtIndex() throws Exception {
        for (int i = 0; i < NUMBER_OF_DOORS; i++) {
            openerClient.setDoorStatusAtIndex(i, GarageOpenerClient.DoorStatus.DOOR_CLOSED);
            assertEquals(openerClient.getDoorStatusAtIndex(i), GarageOpenerClient.DoorStatus.DOOR_CLOSED);

            openerClient.setDoorStatusAtIndex(i, GarageOpenerClient.DoorStatus.DOOR_MOVING);
            assertEquals(openerClient.getDoorStatusAtIndex(i), GarageOpenerClient.DoorStatus.DOOR_MOVING);

            openerClient.setDoorStatusAtIndex(i, GarageOpenerClient.DoorStatus.DOOR_NOT_CLOSED);
            assertEquals(openerClient.getDoorStatusAtIndex(i), GarageOpenerClient.DoorStatus.DOOR_NOT_CLOSED);
        }
    }


    public void testSetLastStatusChangeAtIndex() throws Exception {
        for (int i = 0; i < NUMBER_OF_DOORS; i++) {
            long currentSeconds = System.currentTimeMillis()/1000;

            openerClient.setLastStatusChangeAtIndex(i, currentSeconds);
            assertEquals(openerClient.getLastStatusChangeAtIndex(i), currentSeconds);
        }

    }

    public void testIsGarageMoving() throws Exception {
        long currentSeconds = System.currentTimeMillis() / 1000;

        //Garage has been moving for 0 seconds
        assertTrue(openerClient.isGarageMoving(currentSeconds));

        //Garage has been moving for greater than DOOR_CLOSE_TIME seconds
        assertFalse(openerClient.isGarageMoving(currentSeconds - GarageOpenerClient.DOOR_CLOSE_TIME));

        //Garage has been moving for < 0 seconds
        assertTrue(openerClient.isGarageMoving(currentSeconds + 10));
    }
}