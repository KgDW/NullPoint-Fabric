package me.nullpoint.api.managers;

import me.nullpoint.mod.modules.impl.miscellaneous.Timer;

public class TimerManager {

    public float timer = 1f;

    public void set(float factor) {
        if (factor < 0.1f) factor = 0.1f;
        timer = factor;
    }

    public float lastTime;
    public void reset() {
        timer = getDefault();
        lastTime = timer;
    }

    public void tryReset() {
        if (lastTime != getDefault()) {
            reset();
        }
    }

    public float get() {
        return timer;
    }

    public float getDefault() {
        return Timer.INSTANCE.isOn() ? Timer.INSTANCE.multiplier.getValueFloat() : 1f;
    }
}

