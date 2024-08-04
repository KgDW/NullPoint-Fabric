package me.nullpoint.mod.modules.impl.client;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.math.MathUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.render.ColorUtil;
import me.nullpoint.api.utils.render.TextUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class HUD extends Module {

    public static HUD INSTANCE = new HUD();

    //Elements
    private final EnumSetting<Page> page = add(new EnumSetting<>("Page", Page.GLOBAL));
    public final BooleanSetting armor = add(new BooleanSetting("Armor", true, v -> page.getValue() == Page.GLOBAL));
    public final SliderSetting lagTime = add(new SliderSetting("LagTime", 1000, 0, 2000, v -> page.getValue() == Page.GLOBAL));
    public final BooleanSetting lowerCase = add(new BooleanSetting("LowerCase", false, v -> page.getValue() == Page.GLOBAL));
    private final BooleanSetting grayColors = add(new BooleanSetting("Gray", true, v -> page.getValue() == Page.GLOBAL));
    private final BooleanSetting renderingUp = add(new BooleanSetting("RenderingUp", true, v -> page.getValue() == Page.GLOBAL));
    private final BooleanSetting watermark = add(new BooleanSetting("Watermark", true, v -> page.getValue() == Page.ELEMENTS).setParent());
    public final SliderSetting offset = add(new SliderSetting("Offset", 8, 0, 100, -1, v -> watermark.isOpen() && page.getValue() == Page.ELEMENTS));
    public final StringSetting watermarkString = add(new StringSetting("Text", "NullPoint", v -> watermark.isOpen() && page.getValue() == Page.ELEMENTS));
    private final BooleanSetting watermarkShort = add(new BooleanSetting("Shorten", false, v -> watermark.isOpen() && page.getValue() == Page.ELEMENTS));
    private final BooleanSetting watermarkVerColor = add(new BooleanSetting("VerColor", true, v -> watermark.isOpen() && page.getValue() == Page.ELEMENTS));
    private final SliderSetting waterMarkY = add(new SliderSetting("Height", 2, 2, 12, v -> page.getValue() == Page.ELEMENTS && watermark.isOpen()));
    private final BooleanSetting idWatermark = add(new BooleanSetting("IdWatermark", true, v -> page.getValue() == Page.ELEMENTS));
    private final BooleanSetting textRadar = add(new BooleanSetting("TextRadar", false, v -> page.getValue() == Page.ELEMENTS).setParent());
    private final SliderSetting updatedelay = add(new SliderSetting("UpdateDelay", 5, 0, 1000, v -> page.getValue() == Page.ELEMENTS && textRadar.isOpen()));
    private final BooleanSetting health = add(new BooleanSetting("Health", false, v -> page.getValue() == Page.ELEMENTS && textRadar.isOpen()));
    private final BooleanSetting coords = add(new BooleanSetting("Position(XYZ)", false, v -> page.getValue() == Page.ELEMENTS));
    private final BooleanSetting direction = add(new BooleanSetting("Direction", false, v -> page.getValue() == Page.ELEMENTS));
    private final BooleanSetting lag = add(new BooleanSetting("LagNotifier", false, v -> page.getValue() == Page.ELEMENTS));
    private final BooleanSetting greeter = add(new BooleanSetting("Welcomer", false, v -> page.getValue() == Page.ELEMENTS).setParent());
    private final EnumSetting<GreeterMode> greeterMode = add(new EnumSetting<>("Mode", GreeterMode.PLAYER, v -> page.getValue() == Page.ELEMENTS && greeter.isOpen()));
    private final BooleanSetting greeterNameColor = add(new BooleanSetting("NameColor", true, v -> greeter.isOpen() && greeterMode.getValue() == GreeterMode.PLAYER && page.getValue() == Page.ELEMENTS));
    private final StringSetting greeterText = add(new StringSetting("WelcomerText", "i sniff coke and smoke dope i got 2 habbits", v -> greeter.isOpen() && greeterMode.getValue() == GreeterMode.CUSTOM && page.getValue() == Page.ELEMENTS));

    private final BooleanSetting potions = add(new BooleanSetting("Potions", false, v -> page.getValue() == Page.ELEMENTS).setParent());
    private final BooleanSetting potionColor = add(new BooleanSetting("PotionColor", false, v -> page.getValue() == Page.ELEMENTS && potions.isOpen()));
    private final BooleanSetting pvphud = add(new BooleanSetting("PVPHud", false, v -> page.getValue() == Page.ELEMENTS).setParent());
    public final SliderSetting pvphudoffset = add(new SliderSetting("PVPHUDOffset", 8, 0, 100, -1, v -> page.getValue() == Page.ELEMENTS && pvphud.isOpen()));
    private final BooleanSetting totemtext = add(new BooleanSetting("TotemText", false, v -> page.getValue() == Page.ELEMENTS && pvphud.isOpen()));
    private final BooleanSetting potiontext = add(new BooleanSetting("PotionText", false, v -> page.getValue() == Page.ELEMENTS && pvphud.isOpen()));
    private final BooleanSetting ping = add(new BooleanSetting("Ping", false, v -> page.getValue() == Page.ELEMENTS));
    private final BooleanSetting speed = add(new BooleanSetting("Speed", false, v -> page.getValue() == Page.ELEMENTS));
    private final BooleanSetting tps = add(new BooleanSetting("TPS", false, v -> page.getValue() == Page.ELEMENTS));
    private final BooleanSetting fps = add(new BooleanSetting("FPS", false, v -> page.getValue() == Page.ELEMENTS));
    private final BooleanSetting time = add(new BooleanSetting("Time", false, v -> page.getValue() == Page.ELEMENTS));

    private final EnumSetting colorMode = add(new EnumSetting("ColorMode", ColorMode.Pulse, v -> page.getValue() == Page.Color));
    private final SliderSetting rainbowSpeed = add(new SliderSetting("RainbowSpeed", 200, 1, 400, v -> (colorMode.getValue() == ColorMode.Rainbow || colorMode.getValue() == ColorMode.PulseRainbow) && page.getValue() == Page.Color));
    private final SliderSetting saturation = add(new SliderSetting("Saturation", 130.0f, 1.0f, 255.0f, v -> (colorMode.getValue() == ColorMode.Rainbow || colorMode.getValue() == ColorMode.PulseRainbow) && page.getValue() == Page.Color));
    private final SliderSetting pulseSpeed = add(new SliderSetting("PulseSpeed", 100, 1, 400, v -> (colorMode.getValue() == ColorMode.Pulse || colorMode.getValue() == ColorMode.PulseRainbow) && page.getValue() == Page.Color));
    private final SliderSetting rainbowDelay = add(new SliderSetting("Delay", 350, 0, 600, v -> (colorMode.getValue() == ColorMode.Rainbow) && page.getValue() == Page.Color));
    private final ColorSetting color = add(new ColorSetting("Color", new Color(255, 255, 255, 255), v -> (colorMode.getValue() != ColorMode.Rainbow) && page.getValue() == Page.Color));
    private final BooleanSetting sync = add(new BooleanSetting("Sync", false, v -> page.getValue() == Page.Color));
    private final Timer timer = new Timer();
    private Map<String, Integer> players = new HashMap<>();
    private int counter = 20;

    public HUD() {
        super("HUD", "HUD elements drawn on your screen", Category.Client);
        INSTANCE = this;
    }

    @Override
    public void onUpdate() {
        if(timer.passed(updatedelay.getValue())){
            players = getTextRadarMap();
            timer.reset();
        }
        progress -= rainbowSpeed.getValueInt();
        pulseProgress -= pulseSpeed.getValueInt();
    }

    @Override
    public void onRender2D(DrawContext drawContext, float tickDelta) {
        if (nullCheck()) return;
        counter = 20;
        int width = mc.getWindow().getScaledWidth();
        int height = mc.getWindow().getScaledHeight();
        if (armor.getValue()) {
            Nullpoint.GUI.armorHud.draw(drawContext, tickDelta, null);
        }

        if(pvphud.getValue()){
            drawpvphud(drawContext, pvphudoffset.getValueInt());
        }

        if (textRadar.getValue()) drawTextRadar(drawContext, watermark.getValue() ? (int) (waterMarkY.getValue() + 2) : 2);

        if (watermark.getValue()) {
            String nameString = watermarkString.getValue() + " ";
            String verColor = watermarkVerColor.getValue() ? "\u00a7f" : "";
            String verString = verColor + (watermarkShort.getValue() ? "" : Nullpoint.VERSION);

            drawContext.drawTextWithShadow(mc.textRenderer, (lowerCase.getValue() ? nameString.toLowerCase() : nameString) + verString, offset.getValueInt(), 2 + offset.getValueInt(), getColor(counter));
            counter = counter + 1;
        }
        if (idWatermark.getValue()) {
            String nameString = Nullpoint.LOG_NAME + " ";
            String domainString = Nullpoint.VERSION;

            float offset = mc.getWindow().getScaledHeight() / 2.0f - 30.0f;

            drawContext.drawTextWithShadow(mc.textRenderer, nameString + domainString, (int) 2.0f, (int) offset, getColor(counter));
            counter = counter + 1;
        }
        String grayString = grayColors.getValue() ? "\u00a77" : "";
        int i = (mc.currentScreen instanceof ChatScreen && renderingUp.getValue()) ? 13 : (renderingUp.getValue() ? -2 : 0);

        if (renderingUp.getValue()) {
            if (potions.getValue()) {
                List<StatusEffectInstance> effects = new ArrayList<>((mc).player.getStatusEffects());
                for (StatusEffectInstance potionEffect : effects) {
                    String str = getColoredPotionString(potionEffect);
                    i += 10;

                    drawContext.drawTextWithShadow(mc.textRenderer, lowerCase.getValue() ? str.toLowerCase() : str, (width - (lowerCase.getValue() ? mc.textRenderer.getWidth(str.toLowerCase()) : mc.textRenderer.getWidth(str)) - 2), (height - 2 - i), potionColor.getValue() ? potionEffect.getEffectType().getColor() : getColor(counter));
                    counter = counter + 1;
                }
            }
            if (speed.getValue()) {
                String str = grayString + "Speed " + "\u00a7f" + Nullpoint.SPEED.getSpeedKpH() + " km/h";
                i += 10;

                drawContext.drawTextWithShadow(mc.textRenderer, lowerCase.getValue() ? str.toLowerCase() : str, (width - (lowerCase.getValue() ? mc.textRenderer.getWidth(str.toLowerCase()) : mc.textRenderer.getWidth(str)) - 2), (height - 2 - i), getColor(counter));
                counter = counter + 1;
            }
            if (time.getValue()) {
                String str = grayString + "Time " + "\u00a7f" + (new SimpleDateFormat("h:mm a")).format(new Date());
                i += 10;

                drawContext.drawTextWithShadow(mc.textRenderer, lowerCase.getValue() ? str.toLowerCase() : str, (width - (lowerCase.getValue() ? mc.textRenderer.getWidth(str.toLowerCase()) : mc.textRenderer.getWidth(str)) - 2), (height - 2 - i), getColor(counter));
                counter = counter + 1;
            }
            if (tps.getValue()) {
                String str = grayString + "TPS " + "\u00a7f" + Nullpoint.SERVER.getTPS();
                i += 10;

                drawContext.drawTextWithShadow(mc.textRenderer, lowerCase.getValue() ? str.toLowerCase() : str, (width - (lowerCase.getValue() ? mc.textRenderer.getWidth(str.toLowerCase()) : mc.textRenderer.getWidth(str)) - 2), (height - 2 - i), getColor(counter));
                counter = counter + 1;
            }

            String fpsText = grayString + "FPS " + "\u00a7f" + Nullpoint.FPS.getFps();
            String str1 = grayString + "Ping " + "\u00a7f" + Nullpoint.SERVER.getPing();

            if (mc.textRenderer.getWidth(str1) > mc.textRenderer.getWidth(fpsText)) {
                if (ping.getValue()) {
                    i += 10;

                    drawContext.drawTextWithShadow(mc.textRenderer, lowerCase.getValue() ? str1.toLowerCase() : str1, (width - (lowerCase.getValue() ? mc.textRenderer.getWidth(str1.toLowerCase()) : mc.textRenderer.getWidth(str1)) - 2), (height - 2 - i), getColor(counter));
                    counter = counter + 1;
                }
                if (fps.getValue()) {
                    i += 10;

                    drawContext.drawTextWithShadow(mc.textRenderer, lowerCase.getValue() ? fpsText.toLowerCase() : fpsText, (width - (lowerCase.getValue() ? mc.textRenderer.getWidth(fpsText.toLowerCase()) : mc.textRenderer.getWidth(fpsText)) - 2), (height - 2 - i), getColor(counter));
                }
            } else {
                if (fps.getValue()) {
                    i += 10;

                    drawContext.drawTextWithShadow(mc.textRenderer, lowerCase.getValue() ? fpsText.toLowerCase() : fpsText, (width - (lowerCase.getValue() ? mc.textRenderer.getWidth(fpsText.toLowerCase()) : mc.textRenderer.getWidth(fpsText)) - 2), (height - 2 - i), getColor(counter));
                    counter = counter + 1;
                }
                if (ping.getValue()) {
                    i += 10;

                    drawContext.drawTextWithShadow(mc.textRenderer, lowerCase.getValue() ? str1.toLowerCase() : str1, (width - (lowerCase.getValue() ? mc.textRenderer.getWidth(str1.toLowerCase()) : mc.textRenderer.getWidth(str1)) - 2), (height - 2 - i), getColor(counter));
                }
            }
        } else {
            if (potions.getValue()) {
                List<StatusEffectInstance> effects = new ArrayList<>((mc).player.getStatusEffects());
                for (StatusEffectInstance potionEffect : effects) {
                    String str = getColoredPotionString(potionEffect);

                    drawContext.drawTextWithShadow(mc.textRenderer, lowerCase.getValue() ? str.toLowerCase() : str, (width - (lowerCase.getValue() ?mc.textRenderer.getWidth(str.toLowerCase()) : mc.textRenderer.getWidth(str)) - 2), (2 + i++ * 10), potionColor.getValue() ? potionEffect.getEffectType().getColor() : getColor(counter));
                    counter = counter + 1;
                }
            }
            if (speed.getValue()) {
                String str = grayString + "Speed " + "\u00a7f" + Nullpoint.SPEED.getSpeedKpH() + " km/h";

                drawContext.drawTextWithShadow(mc.textRenderer, lowerCase.getValue() ? str.toLowerCase() : str, (width - (lowerCase.getValue() ? mc.textRenderer.getWidth(str.toLowerCase()) : mc.textRenderer.getWidth(str)) - 2), (2 + i++ * 10), getColor(counter));
                counter = counter + 1;
            }
            if (time.getValue()) {
                String str = grayString + "Time " + "\u00a7f" + (new SimpleDateFormat("h:mm a")).format(new Date());

                drawContext.drawTextWithShadow(mc.textRenderer, lowerCase.getValue() ? str.toLowerCase() : str, (width - (lowerCase.getValue() ? mc.textRenderer.getWidth(str.toLowerCase()) : mc.textRenderer.getWidth(str)) - 2), (2 + i++ * 10), getColor(counter));
                counter = counter + 1;
            }
            if (tps.getValue()) {
                String str = grayString + "TPS " + "\u00a7f" + Nullpoint.SERVER.getTPS();

                drawContext.drawTextWithShadow(mc.textRenderer, lowerCase.getValue() ? str.toLowerCase() : str, (width - (lowerCase.getValue() ? mc.textRenderer.getWidth(str.toLowerCase()) : mc.textRenderer.getWidth(str)) - 2), (2 + i++ * 10), getColor(counter));
                counter = counter + 1;
            }

            String fpsText = grayString + "FPS " + "\u00a7f" + Nullpoint.FPS.getFps();
            String str1 = grayString + "Ping " + "\u00a7f" + Nullpoint.SERVER.getPing();

            if (mc.textRenderer.getWidth(str1) > mc.textRenderer.getWidth(fpsText)) {
                if (ping.getValue()) {
                    drawContext.drawTextWithShadow(mc.textRenderer, lowerCase.getValue() ? str1.toLowerCase() : str1, (width - (lowerCase.getValue() ? mc.textRenderer.getWidth(str1.toLowerCase()) : mc.textRenderer.getWidth(str1)) - 2), (2 + i++ * 10), getColor(counter));
                    counter = counter + 1;
                }
                if (fps.getValue()) {
                    drawContext.drawTextWithShadow(mc.textRenderer, lowerCase.getValue() ? fpsText.toLowerCase() : fpsText, (width - (lowerCase.getValue() ? mc.textRenderer.getWidth(fpsText.toLowerCase()) : mc.textRenderer.getWidth(fpsText)) - 2), (2 + i++ * 10), getColor(counter));
                }
            } else {
                if (fps.getValue()) {
                    drawContext.drawTextWithShadow(mc.textRenderer, lowerCase.getValue() ? fpsText.toLowerCase() : fpsText, (width - (lowerCase.getValue() ? mc.textRenderer.getWidth(fpsText.toLowerCase()) : mc.textRenderer.getWidth(fpsText)) - 2), (2 + i++ * 10), getColor(counter));
                    counter = counter + 1;
                }
                if (ping.getValue()) {
                    drawContext.drawTextWithShadow(mc.textRenderer, lowerCase.getValue() ? str1.toLowerCase() : str1, (width - (lowerCase.getValue() ? mc.textRenderer.getWidth(str1.toLowerCase()) : mc.textRenderer.getWidth(str1)) - 2), (2 + i++ * 10), getColor(counter));
                }
            }
        }

        boolean inHell = mc.world.getRegistryKey().equals(World.NETHER);

        int posX = (int) mc.player.getX();
        int posY = (int) mc.player.getY();
        int posZ = (int) mc.player.getZ();

        float nether = !inHell ? 0.125F : 8.0F;

        int hposX = (int) (mc.player.getX() * nether);
        int hposZ = (int) (mc.player.getZ() * nether);
        int yawPitch = (int) MathHelper.wrapDegrees(mc.player.getYaw());
        int p = coords.getValue() ? 0 : 11;

        i = (mc.currentScreen instanceof ChatScreen) ? 14 : 0;

        String coordinates = (lowerCase.getValue() ? "XYZ: ".toLowerCase() : "XYZ: ") + "§f" + (inHell ? (posX + ", " + posY + ", " + posZ + "§7" + " [" + "§f" + hposX + ", " + hposZ + "§7" + "]" + "§f") : (posX + ", " + posY + ", " + posZ + "§7" + " [" + "§f" + hposX + ", " + hposZ + "§7" + "]"));
        String direction = this.direction.getValue() ? Nullpoint.ROTATE.getDirection4D(false) : "";
        String yaw = this.direction.getValue() ? (lowerCase.getValue() ? "Yaw: ".toLowerCase() : "Yaw: ") + "§f" + yawPitch : "";
        String coords = this.coords.getValue() ? coordinates : "";

        i += 10;

        if (mc.currentScreen instanceof ChatScreen && this.direction.getValue()) {
            yaw = "";
            direction = (lowerCase.getValue() ? "Yaw: ".toLowerCase() : "Yaw: ") + "§f" + yawPitch + "§7" + " " + getFacingDirectionShort();
        }
        drawContext.drawTextWithShadow(mc.textRenderer, direction, (int) 2.0F, (height - i - 11 + p), getColor(counter));
        counter = counter + 1;
        drawContext.drawTextWithShadow(mc.textRenderer, yaw, (int) 2.0F, (height - i - 22 + p), getColor(counter));
        counter = counter + 1;
        drawContext.drawTextWithShadow(mc.textRenderer, coords, (int) 2.0F, (height - i), getColor(counter));
        counter = counter + 1;


        if (greeter.getValue()) drawWelcomer(drawContext);

        if (lag.getValue()) drawLagOMeter(drawContext);
    }

    private void drawWelcomer(DrawContext drawContext) {
        int width = mc.getWindow().getScaledWidth();
        String nameColor = greeterNameColor.getValue() ? String.valueOf(Formatting.WHITE) : "";
        String text = (lowerCase.getValue() ? "Welcome, ".toLowerCase() : "Welcome, ");

        if (greeterMode.getValue() == GreeterMode.PLAYER) {
            if (greeter.getValue()) text = text + nameColor + mc.getSession().getUsername();

            drawContext.drawTextWithShadow(mc.textRenderer, text + "§0" + " :')", (int) (width / 2.0F - mc.textRenderer.getWidth(text) / 2.0F + 2.0F), (int) 2.0F, getColor(counter));
            counter = counter + 1;

        } else {
            String lel = greeterText.getValue();
            if (greeter.getValue()) lel = greeterText.getValue();
            drawContext.drawTextWithShadow(mc.textRenderer, lel, (int) (width / 2.0F - mc.textRenderer.getWidth(lel) / 2.0F + 2.0F), (int) 2.0F, getColor(counter));
            counter = counter + 1;
        }
    }

    private void drawpvphud(DrawContext drawContext, int yOffset) {
        double x = mc.getWindow().getWidth() / 4D;
        double y = mc.getWindow().getHeight() / 4D + yOffset;
        int textHeight = mc.textRenderer.fontHeight + 1;
        String t1 = "Totem " + Formatting.WHITE + InventoryUtil.getItemCount(Items.TOTEM_OF_UNDYING);
        String t2 = "Potion " + Formatting.WHITE + InventoryUtil.getPotCount(StatusEffects.RESISTANCE);
        List<StatusEffectInstance> effects = new ArrayList<>((mc).player.getStatusEffects());
        if(totemtext.getValue()){
            drawContext.drawTextWithShadow(mc.textRenderer, t1, (int) (x - mc.textRenderer.getWidth(t1) / 2), (int) y, getColor(counter));
            counter = counter + 1;
            y += textHeight;
        }
        if(potiontext.getValue()){
            drawContext.drawTextWithShadow(mc.textRenderer, t2, (int) (x - mc.textRenderer.getWidth(t2) / 2), (int) y, getColor(counter));
            counter = counter + 1;
            y += textHeight;
        }
        for (StatusEffectInstance potionEffect : effects) {
            if(potionEffect.getEffectType() == StatusEffects.RESISTANCE &&  (potionEffect.getAmplifier() + 1 ) > 1){
                String str = getColoredPotionTimeString(potionEffect);
                String t3 = "PotionTime " + Formatting.WHITE + str;
                if(potiontext.getValue()){
                    drawContext.drawTextWithShadow(mc.textRenderer, t3, (int) (x - mc.textRenderer.getWidth(t3) / 2), (int) y, getColor(counter));
                    counter = counter + 1;
                    y += textHeight;
                }
            }
        }
    }

    private void drawLagOMeter(DrawContext drawContext) {
        int width = mc.getWindow().getScaledWidth();
        if (Nullpoint.SERVER.isServerNotResponding()) {
            String text = "\u00a74" + (lowerCase.getValue() ? "Server is lagging for ".toLowerCase() : "Server is lagging for ") + MathUtil.round((float) Nullpoint.SERVER.serverRespondingTime() / 1000.0F, 1) + "s.";
            drawContext.drawTextWithShadow(mc.textRenderer, text, (int) (width / 2.0F - mc.textRenderer.getWidth(text) / 2.0F + 2.0F), (int) 20.0F, getColor(counter));
            counter = counter + 1;
        }
    }

    private void drawTextRadar(DrawContext drawContext, int yOffset) {

        if (!players.isEmpty()) {

            int y = mc.textRenderer.fontHeight + 7 + yOffset;

            for (Map.Entry<String, Integer> player : players.entrySet()) {

                String text = player.getKey() + " ";

                int textHeight = mc.textRenderer.fontHeight + 1;

                drawContext.drawTextWithShadow(mc.textRenderer, text, (int) 2.0F, y, getColor(counter));
                counter = counter + 1;
                y += textHeight;
            }
        }
    }

    private Map<String, Integer> getTextRadarMap() {
        Map<String, Integer> retval = new HashMap<>();

        DecimalFormat dfDistance = new DecimalFormat("#.#");
        dfDistance.setRoundingMode(RoundingMode.CEILING);
        StringBuilder distanceSB = new StringBuilder();

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player.isInvisible() || player.getName().equals(mc.player.getName())) continue;

            int distanceInt = (int) mc.player.distanceTo(player);
            String distance = dfDistance.format(distanceInt);

            if (distanceInt >= 25) {
                distanceSB.append(Formatting.GREEN);

            } else if (distanceInt > 10) {
                distanceSB.append(Formatting.YELLOW);

            } else {
                distanceSB.append(Formatting.RED);
            }
            distanceSB.append(distance);

            retval.put((health.getValue() ? (getHealthColor(player) + String.valueOf(round2(player.getAbsorptionAmount() + player.getHealth())) + " ") : "") + (Nullpoint.FRIEND.isFriend(player) ? Formatting.AQUA : Formatting.RESET) + player.getName().getString() + " " + Formatting.WHITE + "[" + Formatting.RESET + distanceSB + "m" + Formatting.WHITE + "] " + Formatting.GREEN, (int) mc.player.distanceTo(player));

            distanceSB.setLength(0);
        }

        if (!retval.isEmpty()) {
            retval = MathUtil.sortByValue(retval, false);
        }

        return retval;
    }

    public static float round2(double value) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(1, RoundingMode.HALF_UP);
        return bd.floatValue();
    }

    private Formatting getHealthColor(@NotNull PlayerEntity entity) {
        int health = (int) ((int) entity.getHealth() + entity.getAbsorptionAmount());
        if (health <= 15 && health > 7) return Formatting.YELLOW;
        if (health > 15) return Formatting.GREEN;
        return Formatting.RED;
    }

    private String getFacingDirectionShort() {
        int dirnumber = Nullpoint.ROTATE.getYaw4D();

        if (dirnumber == 0) {
            return "(+Z)";
        }
        if (dirnumber == 1) {
            return "(-X)";
        }
        if (dirnumber == 2) {
            return "(-Z)";
        }
        if (dirnumber == 3) {
            return "(+X)";
        }
        return "Loading...";
    }

    private String getColoredPotionString(StatusEffectInstance effect) {
        StatusEffect potion = effect.getEffectType();
        return potion.getName().getString() + " " + (effect.getAmplifier() + 1) + " " + "\u00a7f" + StatusEffectUtil.getDurationText(effect, 1, mc.world.getTickManager().getTickRate()).getString();
    }

    private String getColoredPotionTimeString(StatusEffectInstance effect) {
        return StatusEffectUtil.getDurationText(effect, 1, mc.world.getTickManager().getTickRate()).getString();
    }

    private int getColor(int counter) {
        if (colorMode.getValue() != ColorMode.Custom) {
            return rainbow(counter).getRGB();
        }
        if(sync.getValue()){
            return UIModule.INSTANCE.color.getValue().getRGB();
        }
        return color.getValue().getRGB();
    }

    int progress = 0;
    int pulseProgress = 0;

    private Color rainbow(int delay) {
        double rainbowState = Math.ceil((progress + delay * rainbowDelay.getValue()) / 20.0);
        if (colorMode.getValue() == ColorMode.Pulse) {
            if(sync.getValue()){
                return pulseColor(UIModule.INSTANCE.color.getValue(), delay);
            }
            return pulseColor(color.getValue(), delay);
        } else if (colorMode.getValue() == ColorMode.Rainbow) {
            return Color.getHSBColor((float) (rainbowState % 360.0 / 360), saturation.getValueFloat() / 255.0f, 1.0f);
        } else {
            return pulseColor(Color.getHSBColor((float) (rainbowState % 360.0 / 360), saturation.getValueFloat() / 255.0f, 1.0f), delay);
        }
    }

    private Color pulseColor(Color color, int index) {
        float[] hsb = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
        float brightness = Math.abs((pulseProgress % ((long) 1230675006 ^ 0x495A9BEEL) / Float.intBitsToFloat(Float.floatToIntBits(0.0013786979f) ^ 0x7ECEB56D) + index / (float) 14 * Float.intBitsToFloat(Float.floatToIntBits(0.09192204f) ^ 0x7DBC419F)) % Float.intBitsToFloat(Float.floatToIntBits(0.7858098f) ^ 0x7F492AD5) - Float.intBitsToFloat(Float.floatToIntBits(6.46708f) ^ 0x7F4EF252));
        brightness = Float.intBitsToFloat(Float.floatToIntBits(18.996923f) ^ 0x7E97F9B3) + Float.intBitsToFloat(Float.floatToIntBits(2.7958195f) ^ 0x7F32EEB5) * brightness;
        hsb[2] = brightness % Float.intBitsToFloat(Float.floatToIntBits(0.8992331f) ^ 0x7F663424);
        return ColorUtil.injectAlpha(new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2])), color.getAlpha());
    }

    private enum ColorMode {
        Custom,
        Pulse,
        Rainbow,
        PulseRainbow,
    }



    private enum GreeterMode {
        PLAYER, CUSTOM
    }

    private enum Page {
        ELEMENTS, GLOBAL, Color
    }
}
