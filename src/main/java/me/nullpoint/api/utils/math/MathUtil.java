package me.nullpoint.api.utils.math;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class MathUtil {
    public static Direction getFacingOrder(float yaw, float pitch) {
        float f = pitch * 0.017453292F;
        float g = -yaw * 0.017453292F;
        float h = MathHelper.sin(f);
        float i = MathHelper.cos(f);
        float j = MathHelper.sin(g);
        float k = MathHelper.cos(g);
        boolean bl = j > 0.0F;
        boolean bl2 = h < 0.0F;
        boolean bl3 = k > 0.0F;
        float l = bl ? j : -j;
        float m = bl2 ? -h : h;
        float n = bl3 ? k : -k;
        float o = l * i;
        float p = n * i;
        Direction direction = bl ? Direction.EAST : Direction.WEST;
        Direction direction2 = bl2 ? Direction.UP : Direction.DOWN;
        Direction direction3 = bl3 ? Direction.SOUTH : Direction.NORTH;
        if (l > n) {
            if (m > o) {
                return direction2;
            } else {
                return direction;
            }
        } else if (m > p) {
            return direction2;
        } else {
            return direction3;
        }
    }

    public static float clamp(float num, float min, float max) {
        return num < min ? min : Math.min(num, max);
    }
    public static double clamp(double value, double min, double max) {
        if (value < min) return min;
        return Math.min(value, max);
    }
    public static double square(double input) {
        return input * input;
    }

    public static float random(float min, float max) {
        return (float) (Math.random() * (max - min) + min);
    }
    public static double random(double min, double max) {
        return (float) (Math.random() * (max - min) + min);
    }

    public static float rad(float angle) {
        return (float) (angle * Math.PI / 180);
    }

    public static double interpolate(double previous, double current, float delta) {
        return previous + (current - previous) * (double) delta;
    }
    public static float round(float value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.FLOOR);
        return bd.floatValue();
    }
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, boolean descending) {
        LinkedList<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        if (descending) {
            list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        } else {
            list.sort(Map.Entry.comparingByValue());
        }
        LinkedHashMap result = new LinkedHashMap();
        for (Map.Entry entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}

