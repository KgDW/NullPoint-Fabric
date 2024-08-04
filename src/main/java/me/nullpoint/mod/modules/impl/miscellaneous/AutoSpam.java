package me.nullpoint.mod.modules.impl.miscellaneous;


import me.nullpoint.Nullpoint;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import me.nullpoint.mod.modules.settings.impl.StringSetting;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class AutoSpam extends Module {
    public AutoSpam() {
        super("AutoSpam", Category.Misc);
    }
    private final Random r = new Random();
    private static String[] kouzi = new String[0];
    private int lastNum;

    private final StringSetting custom = add(new StringSetting("Name","campaunlas" ));
    private final SliderSetting delay =
            add(new SliderSetting("Delay", 1500, 0, 10000));
    private final BooleanSetting heavy =
            add(new BooleanSetting("Heavy", false));
    private final BooleanSetting tell =
            add(new BooleanSetting("Tell", true));

    me.nullpoint.api.utils.math.Timer timer= new me.nullpoint.api.utils.math.Timer();
    @Override
    public String getInfo() {
        return custom.getValue();
    }
    @Override
    public void onEnable() {

        timer.reset();
    }




    @Override
    public void onUpdate(){
        if(nullCheck()){
            return;
        }
        BufferedReader buff = null;
        buff = heavy.getValue()? new BufferedReader(new InputStreamReader(Objects.requireNonNull(Nullpoint.class.getClassLoader().getResourceAsStream("kouzi2.txt")), StandardCharsets.UTF_8)) :  new BufferedReader(new InputStreamReader(Objects.requireNonNull(Nullpoint.class.getClassLoader().getResourceAsStream("kouzi.txt")), StandardCharsets.UTF_8));
        List<String> dictionary = buff.lines().toList();
        kouzi = dictionary.toArray(new String[0]);
        if(timer.passedMs(delay.getValue())){
            timer.reset();
            int num = r.nextInt(0, kouzi.length);
            if (num == lastNum) {
                num = num < kouzi.length - 1 ? num + 1 : 0;
            }
            lastNum = num;
            send((tell.getValue() ? (custom.getValue() + " ") : "") + kouzi[num] + (tell.getValue() ? "" : custom.getValue()));
        }
    }
    private void send(String s){
        if(tell.getValue()){
            mc.player.networkHandler.sendChatCommand("tell " + s);
        } else {
            mc.player.networkHandler.sendChatMessage(s);
        }
    }
}
