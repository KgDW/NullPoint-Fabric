package me.nullpoint.api.managers;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.impl.Render3DEvent;
import me.nullpoint.api.utils.Wrapper;
import me.nullpoint.mod.Mod;
import me.nullpoint.mod.gui.clickgui.ClickGuiScreen;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.client.*;
import me.nullpoint.mod.modules.impl.combat.*;
import me.nullpoint.mod.modules.impl.exploit.*;
import me.nullpoint.mod.modules.impl.miscellaneous.*;
import me.nullpoint.mod.modules.impl.movement.*;
import me.nullpoint.mod.modules.impl.player.*;
import me.nullpoint.mod.modules.impl.player.freelook.FreeLook;
import me.nullpoint.mod.modules.impl.render.*;
import me.nullpoint.mod.modules.impl.render.skybox.Skybox;
import me.nullpoint.mod.modules.impl.vanilla.VAnchorAura;
import me.nullpoint.mod.modules.impl.vanilla.VAutoCity;
import me.nullpoint.mod.modules.impl.vanilla.VSpeedMine;
import me.nullpoint.mod.modules.impl.vanilla.VWebAura;
import me.nullpoint.mod.modules.settings.Setting;
import me.nullpoint.mod.modules.settings.impl.BindSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ModuleManager implements Wrapper {
	public final ArrayList<Module> modules = new ArrayList<>();
	public final HashMap<Module.Category, Integer> categoryModules = new HashMap<>();
	public static Mod lastLoadMod;

	public ModuleManager() {
		addModule(new Flight());
		addModule(new MineTweak());
		addModule(new AutoRespawn());
		addModule(new AutoAnchor());
		addModule(new AutoArmor());
		addModule(new HoleFiller());
		addModule(new FastWeb());
		addModule(new WebCleaner());
		addModule(new PacketElytra());
		addModule(new BugClip());
		addModule(new AutoPearl());
		addModule(new WebAura());
		addModule(new XCarry());
		addModule(new HoleESP());
		addModule(new ElytraFly());
		addModule(new PlaceRender());
		addModule(new BowBomb());
		addModule(new SilentDouble());
		addModule(new AntiWeakness());
		addModule(new AutoEZ());
		addModule(new AntiSpam());
		addModule(new ChatEncrypter());
		addModule(new ElytraFlyBypass());
		addModule(new ElytraFlyPlus());
		addModule(new PacketFly());
		addModule(new AutoSpam());
		addModule(new AutoCity());
		addModule(new EntityControl());
		addModule(new AutoDupe());
		addModule(new SilentDisconnect());
		addModule(new HoleSnap());
		addModule(new NoServerRP());
		addModule(new PacketEat());
		addModule(new Step());
		addModule(new Shader());
		addModule(new ServerLagger());
		addModule(new Strafe());
		addModule(new BurrowAssist());
		addModule(new Zoom());
		addModule(new Scaffold());
		addModule(new BedAura());
		addModule(new DesyncESP());
		addModule(new RaytraceBypass());
		addModule(new CrystalChams());
		addModule(new NoRender());
		addModule(new FastUse());
		addModule(new ModuleList());
		addModule(new Reach());
		addModule(new HotbarAnimation());
		addModule(new PopCounter());
		addModule(new SpeedMine());
		addModule(new UIModule());
		addModule(new CameraClip());
		addModule(new CustomFov());
		addModule(new ChatSuffix());
		addModule(new FastFall());
		addModule(new AutoTrap());
		addModule(new AspectRatio());
		addModule(new PacketControl());
		addModule(new BlockHighLight());
		addModule(new BlockerESP());
		addModule(new Blocker());
		addModule(new HUD());
		addModule(new IRC());
		addModule(new PopChams());
		addModule(new Burrow());
		addModule(new AutoCrystal());
		addModule(new Sprint());
		addModule(new TickShift());
		addModule(new AutoEXP());
		addModule(new AntiPiston());
		addModule(new AntiRegear());
		addModule(new Blink());
		addModule(new NameTags());
		addModule(new HoleKick());
		addModule(new NoSwap());
		addModule(new Velocity());
		addModule(new VAnchorAura());
		addModule(new VSpeedMine());
		addModule(new VAutoCity());
		addModule(new VWebAura());
		addModule(new Trajectories());
		addModule(new AntiHunger());
		addModule(new LogoutSpots());
		addModule(new PortalGui());
		addModule(new CombatSetting());
		addModule(new SelfFlatten());
		addModule(new NoInterp());
		addModule(new AntiVoid());
		addModule(new HitMarker());
		addModule(new TotemAnimation());
		addModule(new Crosshair());
		addModule(new Indicator());
		addModule(new Speed());
		addModule(new ExceptionPatcher());
		addModule(new ViewModel());
		addModule(new AnchorAssist());
		addModule(new SelfFill());
		addModule(new SwingModifer());
		addModule(new NoSoundLag());
		addModule(new CrystalPlaceESP());
		addModule(new InventoryMove());
		addModule(new FakePlayer());
		addModule(new FreeCam());
		addModule(new Ping());
		addModule(new AntiCheat());
		addModule(new FeetTrap());
		addModule(new Aura());
		addModule(new TPAura());
		addModule(new ESP());
		addModule(new Criticals());
		addModule(new ShulkerViewer());
		addModule(new AutoReconnect());
		addModule(new AutoTotem());
		addModule(new Quiver());
		addModule(new AutoWalk());
		addModule(new SpinBot());
		addModule(new AutoRegear());
		addModule(new PistonCrystal());
		addModule(new SafeWalk());
		addModule(new MCF());
		addModule(new Ambience());
		addModule(new AnchorAura());
		addModule(new Skybox());
		addModule(new NoSlow());
		addModule(new ForceSync());
		addModule(new NoFall());
		addModule(new NoRotateSet());
		addModule(new MCP());
		addModule(new ChatSetting());
		addModule(new BlockStrafe());
		addModule(new BedCrafter());
		addModule(new Timer());
		addModule(new BaseFinder());
		addModule(new TotemParticle());
		addModule(new AutoPot());
		addModule(new TwoDESP());
		addModule(new BreakESP());
		addModule(new FakeLag());
		addModule(new FreeLook());
		addModule(new HitboxDesync());
		addModule(new Replenish());
		addModule(new Notify());
		addModule(new MainMenu());
		if (Nullpoint.isdev){
			addModule(new OnlyNPIRC());
		}
		modules.sort(Comparator.comparing(Mod::getName));
	}

	public boolean setBind(int eventKey) {
		if (eventKey == -1 || eventKey == 0) {
			return false;
		}
		AtomicBoolean set = new AtomicBoolean(false);
		modules.forEach(module -> {
			for (Setting setting : module.getSettings()) {
				if (setting instanceof BindSetting bind) {
					if (bind.isListening()) {
						bind.setKey(eventKey);
						bind.setListening(false);
						if (bind.getBind().equals("DELETE")) {
							bind.setKey(-1);
						}
						set.set(true);
					}
				}
			}
		});
		return set.get();
	}

	public void onKeyReleased(int eventKey) {
		if (eventKey == -1 || eventKey == 0) {
			return;
		}
		modules.forEach(module -> {
			if (module.getBind().getKey() == eventKey && module.getBind().isHoldEnable() && module.getBind().hold) {
				module.toggle();
				module.getBind().hold = false;
			}
			module.getSettings().stream()
					.filter(setting -> setting instanceof BindSetting)
					.map(setting -> (BindSetting) setting)
					.filter(bindSetting -> bindSetting.getKey() == eventKey)
					.forEach(bindSetting -> bindSetting.setPressed(false));
		});
	}

	public void onKeyPressed(int eventKey) {
		if (eventKey == -1 || eventKey == 0 || mc.currentScreen instanceof ClickGuiScreen) {
			return;
		}
		modules.forEach(module -> {
			if (module.getBind().getKey() == eventKey && mc.currentScreen == null) {
				module.toggle();
				module.getBind().hold = true;
			}

			module.getSettings().stream()
					.filter(setting -> setting instanceof BindSetting)
					.map(setting -> (BindSetting) setting)
					.filter(bindSetting -> bindSetting.getKey() == eventKey)
					.forEach(bindSetting -> bindSetting.setPressed(true));
		});
	}

	public void onUpdate() {
		modules.stream().filter(Module::isOn).forEach(module -> {
			try {
				module.onUpdate();
			} catch (Exception e) {
				e.printStackTrace();
				CommandManager.sendChatMessage("§4[!] " + e.getMessage());
			}
		});
	}

	public void onLogin() {
		modules.stream().filter(Module::isOn).forEach(Module::onLogin);
	}

	public void onLogout() {
		modules.stream().filter(Module::isOn).forEach(Module::onLogout);
	}

	public void render2D(DrawContext drawContext) {
		modules.stream().filter(Module::isOn).forEach(module -> module.onRender2D(drawContext, MinecraftClient.getInstance().getTickDelta()));
	}
	public void render3D(MatrixStack matrixStack) {
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		matrixStack.push();
		modules.stream().filter(Module::isOn).forEach(module -> module.onRender3D(matrixStack, mc.getTickDelta()));
		Nullpoint.EVENT_BUS.post(new Render3DEvent(matrixStack, mc.getTickDelta()));
		matrixStack.pop();
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
	}

	public void addModule(Module module) {
		module.add(module.getBind());
		modules.add(module);
		categoryModules.put(module.getCategory(), categoryModules.getOrDefault(module.getCategory(), 0) + 1);
	}

	public void disableAll() {
		for (Module module : modules) {
			module.disable();
		}
	}

	public Module getModuleByName(String string) {
		for (Module module : modules) {
			if (module.getName().equalsIgnoreCase(string)) {
				return module;
			}
		}
		return null;
	}
}
