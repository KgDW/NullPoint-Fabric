package me.nullpoint.api.managers;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.alts.Alt;
import me.nullpoint.api.utils.Wrapper;
import me.nullpoint.asm.accessors.IMinecraftClient;
import net.minecraft.client.session.Session;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AltManager implements Wrapper {
	private final ArrayList<Alt> alts = new ArrayList<>();

	public AltManager() {
		readAlts();
	}

	public void readAlts() {
		try {
			File altFile = new File(mc.runDirectory, "nullpoint_alts.txt");
			if (!altFile.exists())
				throw new IOException("File not found! Could not load alts...");
			List<String> list = IOUtils.readLines(new FileInputStream(altFile), StandardCharsets.UTF_8);

			for (String s : list) {
				alts.add(new Alt(s));
			}
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

	public void saveAlts() {
		PrintWriter printwriter = null;
		try {
			File altFile = new File(mc.runDirectory, "nullpoint_alts.txt");
			System.out.println("[" + Nullpoint.LOG_NAME + "] Saving Alts");
			printwriter = new PrintWriter(
					new OutputStreamWriter(new FileOutputStream(altFile), StandardCharsets.UTF_8));

			for (Alt alt : alts) {
				printwriter.println(alt.getEmail());
			}
		} catch (Exception exception) {
			System.out.println("[" + Nullpoint.LOG_NAME + "] Failed to save alts");
		}
		printwriter.close();
	}


	public void addAlt(Alt alt) {
		alts.add(alt);
	}

	public void removeAlt(Alt alt) {
		alts.remove(alt);
	}

	public ArrayList<Alt> getAlts() {
		return this.alts;
	}

	public void loginCracked(String alt) {
		try {
			((IMinecraftClient) this.mc).setSession(new Session(alt, UUID.fromString("66123666-1234-5432-6666-667563866600"), "", Optional.empty(), Optional.empty(), Session.AccountType.MOJANG));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loginToken(String name, String token, String uuid) {
		try {
			((IMinecraftClient) this.mc).setSession(new Session(name, UUID.fromString(uuid), token, Optional.empty(), Optional.empty(), Session.AccountType.MOJANG));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
