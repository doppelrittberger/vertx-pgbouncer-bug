package org.example;

import java.time.ZonedDateTime;
import java.util.Arrays;

public class Event {
    private String id;
    private String name;
    private String aggregateID;
    private ZonedDateTime stamp;
    private byte[] payload = new byte[0];

    public Event(String id, String name, String aggregateID, ZonedDateTime stamp, byte[] payload) {
        this.id = id;
        this.name = name;
        this.aggregateID = aggregateID;
        this.stamp = stamp;
        this.payload = payload;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAggregateID() {
        return aggregateID;
    }

    public void setAggregateID(String aggregateID) {
        this.aggregateID = aggregateID;
    }

    public ZonedDateTime getStamp() {
        return stamp;
    }

    public void setStamp(ZonedDateTime stamp) {
        this.stamp = stamp;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", aggregateID='" + aggregateID + '\'' +
                ", stamp=" + stamp +
                ", payload=" + Arrays.toString(payload) +
                '}';
    }
}
