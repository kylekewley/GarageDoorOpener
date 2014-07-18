package kylekewley.garagedooropener;

public class DoorStatusChange {
    public long changeTime;
    public int doorId;
    public boolean didClose;

    public DoorStatusChange(long changeTime, int doorId, boolean didClose) {
        this.changeTime = changeTime;
        this.doorId = doorId;
        this.didClose = didClose;
    }
}
