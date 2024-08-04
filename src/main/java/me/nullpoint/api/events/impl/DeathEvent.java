package me.nullpoint.api.events.impl;

import me.nullpoint.api.events.Event;
import net.minecraft.entity.player.PlayerEntity;

public class DeathEvent extends Event {
    private final PlayerEntity player;

    public DeathEvent(PlayerEntity player) {
        super(Stage.Post);
        this.player = player;
    }

    public PlayerEntity getPlayer() {
        return this.player;
    }
}