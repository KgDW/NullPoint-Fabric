package me.nullpoint.api.events.impl;

import me.nullpoint.api.events.Event;

public class ReceiveMessageEvent extends Event {
    public String message;

    public ReceiveMessageEvent(String message) {
        super(Stage.Pre);
        this.message = message;
    }

    public String getString() {
        return this.message;
    }
}
