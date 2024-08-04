package me.nullpoint.api.events.impl;

import me.nullpoint.api.events.Event;
import net.minecraft.client.sound.SoundInstance;

public class PlaySoundEvent extends Event {
    public final SoundInstance sound;

    public PlaySoundEvent(SoundInstance soundInstance) {
        super(Stage.Pre);
        sound = soundInstance;
    }
}
