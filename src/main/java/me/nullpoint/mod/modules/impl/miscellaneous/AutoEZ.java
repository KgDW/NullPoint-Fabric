package me.nullpoint.mod.modules.impl.miscellaneous;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.DeathEvent;
import me.nullpoint.api.events.impl.TotemEvent;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import me.nullpoint.mod.modules.settings.impl.StringSetting;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class AutoEZ extends Module {
	public AutoEZ() {
		super("AutoEZ", Category.Misc);
	}
	private final Random r = new Random();
	private static String[] kouzi = new String[0];
	private int lastNum;
	private final SliderSetting range = add(new SliderSetting("Range",10,1,30));
	private final BooleanSetting kill = add(new BooleanSetting("kill", true).setParent());
	private final EnumSetting<MessageMode> killMsgMode = add(new EnumSetting<>("msgMode", MessageMode.Rebirth, v -> kill.isOpen()));
	private final StringSetting custom = add(new StringSetting("Custom","killed %player%" , v -> killMsgMode.getValue() == MessageMode.Custom && kill.isOpen()));
	private final BooleanSetting pop = add(new BooleanSetting("pop", true).setParent());
	private final EnumSetting<MessageMode> popMsgMode = add(new EnumSetting<>("msgMode", MessageMode.Rebirth, v -> pop.isOpen()));
	private final StringSetting popcustom = add(new StringSetting("Custom","%player% pop %totem%" , v -> popMsgMode.getValue() == MessageMode.Custom && pop.isOpen()));

	static {
		BufferedReader buff = null;
		buff = new BufferedReader(new InputStreamReader(Objects.requireNonNull(Nullpoint.class.getClassLoader().getResourceAsStream("kouzi.txt")), StandardCharsets.UTF_8));
		List<String> dictionary = buff.lines().toList();
		kouzi = dictionary.toArray(new String[0]);
	}

	private final String[] reb = new String[]{
			"%player% Killed by Rebirth "+Nullpoint.VERSION,
			"Rebirth "+Nullpoint.VERSION+" Killed You %player%",
	};

	@Override
	public String getInfo() {
		return killMsgMode.getValue().name();
	}


	@Override
	public void onEnable() {
		lastNum = -1;
	}

	@EventHandler
	public void onDeath(DeathEvent event) {
		if(event.getPlayer().distanceTo(mc.player) > range.getValueInt() || event.getPlayer() == mc.player || Nullpoint.FRIEND.isFriend(event.getPlayer().getName().getString())) return;
		if(kill.getValue() && killMsgMode.getValue() == MessageMode.Rebirth){
			int num = r.nextInt(0, reb.length);
			if (num == lastNum) {
				num = num < reb.length - 1 ? num + 1 : 0;
			}
			lastNum = num;
			send(reb[num].replaceAll("%player%", event.getPlayer().getName().getString()));
		}
		if(kill.getValue() && killMsgMode.getValue() == MessageMode.NEW){
			int popCount = Nullpoint.POP.popContainer.getOrDefault(event.getPlayer().getName().getString(), 0);
			send("人生自古谁无死？遗憾的，%player%在pop %totem% 个图腾以后已无法与您互动，让我们一起悼念他".replaceAll("%player%", event.getPlayer().getName().getString()).replaceAll("%totem%", String.valueOf(popCount)));
		}
		if(kill.getValue() && killMsgMode.getValue() == MessageMode.Kouzi){
			int num = r.nextInt(0, kouzi.length);
			if (num == lastNum) {
				num = num < kouzi.length - 1 ? num + 1 : 0;
			}
			lastNum = num;
			send(kouzi[num].replaceAll("%player%", event.getPlayer().getName().getString()));
		}
		if(kill.getValue() && killMsgMode.getValue() == MessageMode.Custom){
			int popCount = Nullpoint.POP.popContainer.getOrDefault(event.getPlayer().getName().getString(), 0);
			send(custom.getValue().replaceAll("%player%", event.getPlayer().getName().getString()).replaceAll("%totem%", String.valueOf(popCount)));
		}
	}

	@EventHandler
	public void onTotem(TotemEvent event) {
		if(event.getPlayer().distanceTo(mc.player) > range.getValueInt() || event.getPlayer() == mc.player || Nullpoint.FRIEND.isFriend(event.getPlayer().getName().getString())) return;
		if(pop.getValue() && popMsgMode.getValue() == MessageMode.Rebirth){
			int num = r.nextInt(0, reb.length);
			if (num == lastNum) {
				num = num < reb.length - 1 ? num + 1 : 0;
			}
			lastNum = num;
			send(reb[num].replaceAll("%player%", event.getPlayer().getName().getString()));
		}
		if(pop.getValue() && popMsgMode.getValue() == MessageMode.NEW){
			int popCount = Nullpoint.POP.popContainer.getOrDefault(event.getPlayer().getName().getString(), 0);
			send("%player%竟然还活着！他pop了%totem%个图腾！".replaceAll("%player%", event.getPlayer().getName().getString()).replaceAll("%totem%", String.valueOf(popCount)));
		}
		if(pop.getValue() && popMsgMode.getValue() == MessageMode.Kouzi){
			int num = r.nextInt(0, kouzi.length);
			if (num == lastNum) {
				num = num < kouzi.length - 1 ? num + 1 : 0;
			}
			lastNum = num;
			send(kouzi[num].replaceAll("%player%", event.getPlayer().getName().getString()));
		}
		if(pop.getValue() && popMsgMode.getValue() == MessageMode.Custom){
			int popCount = Nullpoint.POP.popContainer.getOrDefault(event.getPlayer().getName().getString(), 0);
			send(popcustom.getValue().replaceAll("%player%", event.getPlayer().getName().getString()).replaceAll("%totem%", String.valueOf(popCount)));
		}
	}

	private void send(String s){
		mc.player.networkHandler.sendChatMessage(s);
	}

	public enum MessageMode {
		Kouzi,
		Rebirth,
		NEW,
		Custom

	}
}