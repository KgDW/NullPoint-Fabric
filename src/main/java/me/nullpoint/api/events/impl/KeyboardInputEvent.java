package me.nullpoint.api.events.impl;

import me.nullpoint.api.events.Event;

public class KeyboardInputEvent extends Event {
    public KeyboardInputEvent() {
        super(Stage.Post);
    }
}
