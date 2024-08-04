package me.nullpoint.api.managers;

import com.google.gson.*;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.utils.Wrapper;
import net.minecraft.entity.player.PlayerEntity;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FriendManager implements Wrapper {
    public FriendManager() {
        readFriends();
    }

    public static final ArrayList<String> friendList = new ArrayList<>();

    public static boolean isFriend(String name) {
        return friendList.contains(name) ||
                "EntityEvent".equals(name) ||
                "dyzjct".equals(name) ||
                "0ay".equals(name) ||
                "eternity2333".equals(name) ||
                "Q6E".equals(name) ||
                "天才猫咪".equals(name) ||
                "5k1d".equals(name) ||
                "DreamLoveNing".equals(name) ||
                "Dream__Dev".equals(name) ||
                "8PE".equals(name) ||
                "Best_IQ".equals(name) ||
                "MBTPVP机器人_02".equals(name);
    }

    public static void removeFriend(String name) {
        friendList.remove(name);
    }

    public void addFriend(String name) {
        if (!friendList.contains(name)) {
            friendList.add(name);
        }
    }

    public void friend(String name) {
        if (friendList.contains(name)) {
            friendList.remove(name);
        } else {
            friendList.add(name);
        }
    }

    public void readFriends() {
        try {
            File friendFile = new File(mc.runDirectory, "nullpoint_friends.txt");
            if (!friendFile.exists())
                throw new IOException("File not found! Could not load friends...");
            List<String> list = IOUtils.readLines(new FileInputStream(friendFile), StandardCharsets.UTF_8);

            for (String s : list) {
                addFriend(s);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void saveFriends() {
        PrintWriter printwriter = null;
        try {
            File friendFile = new File(mc.runDirectory, "nullpoint_friends.txt");
            System.out.println("[" + Nullpoint.LOG_NAME + "] Saving Friends");
            printwriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(friendFile), StandardCharsets.UTF_8));
            for (String str : friendList) {
                printwriter.println(str);
            }
        } catch (Exception exception) {
            System.out.println("[nullpoint] Failed to save friends");
        }
        printwriter.close();
    }

    public void loadFriends() throws IOException {
        String modName = "nullpoint_friends.json";
        Path modPath = Paths.get(modName);

        if (!Files.exists(modPath)) return;

        loadPath(modPath);
    }

    private void loadPath(Path path) throws IOException {
        InputStream stream = Files.newInputStream(path);
        try {
            loadFile((new JsonParser()).parse(new InputStreamReader(stream)).getAsJsonObject());

        } catch (IllegalStateException e) {
            loadFile(new JsonObject());
        }
        stream.close();
    }

    private void loadFile(JsonObject input) {
        for (Map.Entry<String, JsonElement> entry : input.entrySet()) {
            JsonElement element = entry.getValue();
            try {
                addFriend(element.getAsString());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void saveFriendsOld() throws IOException {
        String modName = "nullpoint_friends.json";

        Path outputFile = Paths.get(modName);

        if (!Files.exists(outputFile)) Files.createFile(outputFile);

        Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
        String json = gson.toJson(writeFriends());

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(outputFile)));

        writer.write(json);
        writer.close();
    }

    public JsonObject writeFriends() {
        JsonObject object = new JsonObject();
        JsonParser jp = new JsonParser();

        for (String str : friendList) {
            try {
                object.add(str.replace(" ", "_"), jp.parse(str.replace(" ", "_")));
            } catch (Exception ignored) {

            }
        }
        return object;
    }

    public boolean isFriend(PlayerEntity entity) {
        return isFriend(entity.getName().getString());
    }
}
