package kylekewley.garagedooropener;

import kylekewley.garagedooropener.GarageOpenerClient;
import kylekewley.garagedooropener.GarageOpenerClient.DoorStatus;

public interface GarageOpenerView {

    /**
     * Gives the GarageOpenerView a reference to the openerClient so the view
     * can request the garage status and trigger the garage doors
     *
     * @param openerClient A reference to a valid GarageDoorOpenerClient object.
     */
    public void setGarageOpenerClient(GarageOpenerClient openerClient);

    /**
     * Updates the view to reflect the new garage door status.
     *
     * @param index     The index of the garage door view to update.
     * @param status    The new garage status.
     */
    public void updateGarageView(int index, DoorStatus status);

    /**
     * Sets the number of doors and updates the view
     *
     * @param garageDoorCount  The number of garage doors to display.
     */
    public void setGarageDoorCount(int garageDoorCount);
}
