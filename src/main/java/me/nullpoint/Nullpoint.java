package me.nullpoint;

import me.nullpoint.api.events.eventbus.EventBus;
import me.nullpoint.api.managers.*;
import me.nullpoint.mod.modules.impl.combat.AutoCrystal;
import me.nullpoint.socket.network.NpSocketManager;
import me.nullpoint.socket.network.SocketManager;
import net.fabricmc.api.ModInitializer;

import java.lang.invoke.MethodHandles;

public final class Nullpoint implements ModInitializer {


	@Override
	public void onInitialize()
	{
		load();
	}

	public static final String LOG_NAME = "NullPoint";
	public static final String VERSION = "v2.1.4";
	public static String PREFIX = ";";
	public static final EventBus EVENT_BUS = new EventBus();
	// Systems
	public static ModuleManager MODULE;
	public static CommandManager COMMAND;
	public static AltManager ALT;
	public static GuiManager GUI;
	public static ConfigManager CONFIG;
	public static RotateManager ROTATE;
	public static MineManager BREAK;
	public static PopManager POP;
	public static SpeedManager SPEED;
	public static FriendManager FRIEND;
	public static TimerManager TIMER;
	public static ShaderManager SHADER;
	public static FPSManager FPS;
	public static SocketManager IRC;
	public static NpSocketManager NPIRC;
	public static ServerManager SERVER;
	public static boolean loaded = false;
//推送的时候记得改成false，这个是决定是否启用私有irc频道的
	public static boolean isdev = true;

	public static void update() {
		MODULE.onUpdate();
		GUI.update();
		POP.update();
	}

	public static void load() {
		System.out.println("[" + LOG_NAME + "] Starting Client");
		System.out.println("[" + LOG_NAME + "] Register eventbus");
		EVENT_BUS.registerLambdaFactory("me.nullpoint", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
		System.out.println("[" + LOG_NAME + "] Reading Settings");
		CONFIG = new ConfigManager();
		//Set prefix
		PREFIX = Nullpoint.CONFIG.getString("prefix", ";");
		System.out.println("[" + LOG_NAME + "] Initializing Modules");
		MODULE = new ModuleManager();
		System.out.println("[" + LOG_NAME + "] Initializing Commands");
		COMMAND = new CommandManager();
		System.out.println("[" + LOG_NAME + "] Initializing GUI");
		GUI = new GuiManager();
		System.out.println("[" + LOG_NAME + "] Loading Alts");
		ALT = new AltManager();
		System.out.println("[" + LOG_NAME + "] Loading Friends");
		FRIEND = new FriendManager();
		System.out.println("[" + LOG_NAME + "] Loading RunManager");
		ROTATE = new RotateManager();
		System.out.println("[" + LOG_NAME + "] Loading BreakManager");
		BREAK = new MineManager();
		System.out.println("[" + LOG_NAME + "] Loading PopManager");
		POP = new PopManager();
		System.out.println("[" + LOG_NAME + "] Loading TimerManager");
		TIMER = new TimerManager();
		System.out.println("[" + LOG_NAME + "] Loading ShaderManager");
		SHADER = new ShaderManager();
		System.out.println("[" + LOG_NAME + "] Loading FPSManager");
		FPS = new FPSManager();
		System.out.println("[" + LOG_NAME + "] Loading ServerManager");
		SERVER = new ServerManager();
		System.out.println("[" + LOG_NAME + "] Loading SpeedManager");
		SPEED = new SpeedManager();
		System.out.println("[" + LOG_NAME + "] Loading IRC");
		IRC = new SocketManager();
		if (isdev) {
			System.out.println("[" + LOG_NAME + "] Loading NPIRC");
			NPIRC = new NpSocketManager();
		}
		System.out.println("[" + LOG_NAME + "] Loading Settings");
		CONFIG.loadSettings();
		System.out.println("[" + LOG_NAME + "] Initialized and ready to play!");

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (loaded) {
				save();
			}
		}));
		loaded = true;
	}

	public static void unload() {
		loaded = false;
		if (AutoCrystal.thread != null && AutoCrystal.thread.isAlive()) AutoCrystal.thread.stop();
		System.out.println("[" + LOG_NAME + "] Unloading..");
		EVENT_BUS.listenerMap.clear();
		ConfigManager.resetModule();
		CONFIG = null;
		MODULE = null;
		COMMAND = null;
		GUI = null;
		ALT = null;
		FRIEND = null;
		ROTATE = null;
		POP = null;
		TIMER = null;
		System.out.println("[" + LOG_NAME + "] Unloading success!");
	}
	public static void save() {
		System.out.println("[" + LOG_NAME + "] Saving...");
		CONFIG.saveSettings();
		FRIEND.saveFriends();
		ALT.saveAlts();
		System.out.println("[" + LOG_NAME + "] Saving success!");
	}
	public enum clientType {
		Dev,
		Beta,
		Release
	}
}
//                            _ooOoo_
//                           o8888888o
//                           88" . "88
//                           (| -_- |)
//                            O\ = /O
//                        ____/`---'\____
//                      .   ' \\| |// `.
//                       / \\||| : |||// \
//                     / _||||| -:- |||||- \
//                       | | \\\ - /// | |
//                     | \_| ''\---/'' | |
//                      \ .-\__ `-` ___/-. /
//                   ___`. .' /--.--\ `. . __
//                ."" '< `.___\_<|>_/___.' >'"".
//               | | : `- \`.;`\ _ /`;.`/ - ` : | |
//                 \ \ `-. \_ __\ /__ _/ .-` / /
//         ======`-.____`-.___\_____/___.-`____.-'======
//                            `=---='
//
//         .............................................
//                  佛祖保佑             永无BUG
//          佛曰:
//                  写字楼里写字间，写字间里程序员；
//                  程序人员写程序，又拿程序换酒钱。
//                  酒醒只在网上坐，酒醉还来网下眠；
//                  酒醉酒醒日复日，网上网下年复年。
//                  但愿老死电脑间，不愿鞠躬老板前；
//                  奔驰宝马贵者趣，公交自行程序员。
//                  别人笑我忒疯癫，我笑自己命太贱；
//                  不见满街漂亮妹，哪个归得程序员？

// 程序出Bug了？
// 　　　∩∩
// 　　（´･ω･）
// 　 ＿|　⊃／(＿＿_
// 　／ └-(＿＿＿／
// 　￣￣￣￣￣￣￣
// 算了反正不是我写的
// 　　 ⊂⌒／ヽ-、＿
// 　／⊂_/＿＿＿＿ ／
// 　￣￣￣￣￣￣￣
// 万一是我写的呢
// 　　　∩∩
// 　　（´･ω･）
// 　 ＿|　⊃／(＿＿_
// 　／ └-(＿＿＿／
// 　￣￣￣￣￣￣￣
// 算了反正改了一个又出三个
// 　　 ⊂⌒／ヽ-、＿
// 　／⊂_/＿＿＿＿ ／
// 　￣￣￣￣￣￣￣

/**
 *                      江城子 . 程序员之歌
 *
 *                  十年生死两茫茫，写程序，到天亮。
 *                      千行代码，Bug何处藏。
 *                  纵使上线又怎样，朝令改，夕断肠。
 *
 *                  领导每天新想法，天天改，日日忙。
 *                      相顾无言，惟有泪千行。
 *                  每晚灯火阑珊处，夜难寐，加班狂。
 */

