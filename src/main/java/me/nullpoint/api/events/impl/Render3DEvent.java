package me.nullpoint.api.events.impl;

import me.nullpoint.api.events.Event;
import net.minecraft.client.util.math.MatrixStack;

public class Render3DEvent extends Event {

    private final float partialTicks;
    private final MatrixStack matrixStack;

    public Render3DEvent(MatrixStack matrixStack, float partialTicks) {
        super(Stage.Pre);
        this.partialTicks = partialTicks;
        this.matrixStack = matrixStack;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public MatrixStack getMatrixStack() {
        return matrixStack;
    }
}

