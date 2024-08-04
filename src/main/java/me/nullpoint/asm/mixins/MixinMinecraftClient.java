package me.nullpoint.asm.mixins;

import me.nullpoint.Nullpoint;
import me.nullpoint.mod.gui.font.FontRenderers;
import me.nullpoint.mod.modules.impl.client.CombatSetting;
import me.nullpoint.mod.modules.impl.exploit.MineTweak;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient extends ReentrantThreadExecutor<Runnable> {
	@Inject(method = "<init>", at = @At("TAIL"))
	void postWindowInit(RunArgs args, CallbackInfo ci) {
		try {
			FontRenderers.Arial = FontRenderers.createArial(15f);
			FontRenderers.Calibri = FontRenderers.create("calibri", Font.BOLD, 11f);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Shadow
	private IntegratedServer server;

	@Shadow
	public ClientPlayNetworkHandler getNetworkHandler() {
		return null;
	}

	@Shadow
	public ServerInfo getCurrentServerEntry() {
		return null;
	}

	/**
	 * @author me
	 * @reason title
	 */
	@Overwrite
	private String getWindowTitle() {
		if (CombatSetting.INSTANCE == null) {
			return "FlawLess: Loading..";
		}
		StringBuilder stringBuilder = new StringBuilder("nullpoint.me");
		return stringBuilder.toString();
	}
	@Shadow
	public int attackCooldown;

	@Shadow
	public ClientPlayerEntity player;

	@Shadow
	public HitResult crosshairTarget;

	@Shadow
	public ClientPlayerInteractionManager interactionManager;

	@Final
	@Shadow
	public ParticleManager particleManager;

	@Inject(method = "handleBlockBreaking", at = @At("HEAD"), cancellable = true)
	private void handleBlockBreaking(boolean breaking, CallbackInfo ci) {
		if (this.attackCooldown <= 0 && this.player.isUsingItem() && MineTweak.INSTANCE.multiTask()) {
			if (breaking && this.crosshairTarget != null && this.crosshairTarget.getType() == HitResult.Type.BLOCK) {
				BlockHitResult blockHitResult = (BlockHitResult)this.crosshairTarget;
				BlockPos blockPos = blockHitResult.getBlockPos();
				if (!this.world.getBlockState(blockPos).isAir()) {
					Direction direction = blockHitResult.getSide();
					if (this.interactionManager.updateBlockBreakingProgress(blockPos, direction)) {
						this.particleManager.addBlockBreakingParticles(blockPos, direction);
						this.player.swingHand(Hand.MAIN_HAND);
					}
				}
			} else {
				this.interactionManager.cancelBlockBreaking();
			}
			ci.cancel();
		}
	}
	@Shadow
	public ClientWorld world;

	public MixinMinecraftClient(String string) {
		super(string);
	}

	@Inject(at = @At("TAIL"), method = "tick()V")
	public void tickTail(CallbackInfo info) {
		Nullpoint.SERVER.run();
		if (this.world != null) {
			Nullpoint.update();
		}
	}
/*	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;setIcon(Lnet/minecraft/resource/ResourcePack;Lnet/minecraft/client/util/Icons;)V"))
	private void onChangeIcon(Window instance, ResourcePack resourcePack, Icons icons) throws IOException {
		RenderSystem.assertInInitPhase();

		if (GLFW.glfwGetPlatform() == 393218) {
			MacWindowUtil.setApplicationIconImage(icons.getMacIcon(resourcePack));
			return;
		}
		//setIcon();
		setWindowIcon(nullpoint.class.getResourceAsStream("/icon.png"), Rebirth.class.getResourceAsStream("/icon.png"));
	}

	@Unique
	public void setWindowIcon(InputStream img16x16, InputStream img32x32) {
		try (MemoryStack memorystack = MemoryStack.stackPush()) {
			GLFWImage.Buffer buffer = GLFWImage.malloc(2, memorystack);
			List<InputStream> imgList = List.of(img16x16, img32x32);
			List<ByteBuffer> buffers = new ArrayList<>();

			for (int i = 0; i < imgList.size(); i++) {
				NativeImage nativeImage = NativeImage.read(imgList.get(i));
				ByteBuffer bytebuffer = MemoryUtil.memAlloc(nativeImage.getWidth() * nativeImage.getHeight() * 4);

				bytebuffer.asIntBuffer().put(nativeImage.copyPixelsRgba());
				buffer.position(i);
				buffer.width(nativeImage.getWidth());
				buffer.height(nativeImage.getHeight());
				buffer.pixels(bytebuffer);

				buffers.add(bytebuffer);
			}

			GLFW.glfwSetWindowIcon(mc.getWindow().getHandle(), buffer);
			buffers.forEach(MemoryUtil::memFree);
		} catch (IOException ignored) {
		}
	}*/

	/*@Overwrite
	private String getWindowTitle() {
		StringBuilder stringBuilder = new StringBuilder("Minecraft");

		stringBuilder.append(" ");
		stringBuilder.append(SharedConstants.getGameVersion().getName());

		ClientPlayNetworkHandler clientPlayNetworkHandler = this.getNetworkHandler();
		if (clientPlayNetworkHandler != null && clientPlayNetworkHandler.getConnection().isOpen()) {
			stringBuilder.append(" - ");
			ServerInfo serverInfo = this.getCurrentServerEntry();
			if (this.server != null && !this.server.isRemote()) {
				stringBuilder.append(I18n.translate("title.singleplayer"));
			} else if (serverInfo != null && serverInfo.isRealm()) {
				stringBuilder.append(I18n.translate("title.multiplayer.realms"));
			} else if (this.server == null && (serverInfo == null || !serverInfo.isLocal())) {
				stringBuilder.append(I18n.translate("title.multiplayer.other"));
			} else {
				stringBuilder.append(I18n.translate("title.multiplayer.lan"));
			}
		}

		return stringBuilder.toString();
	}

	@Shadow
	private IntegratedServer server;

	@Shadow
	public ClientPlayNetworkHandler getNetworkHandler() {
		return null;
	}

	@Shadow
	public ServerInfo getCurrentServerEntry() {
		return null;
	}*/
}
