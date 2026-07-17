package luna.cats.lumine.mixin.client;

import java.util.HashMap;
import java.util.Map;
import luna.cats.lumine.client.peers.PeerData;
import luna.cats.lumine.client.peers.PeerRegistry;
import luna.cats.lumine.client.render.PeerIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(PlayerTabOverlay.class)
public abstract class PlayerTabOverlayMixin {
	@Shadow
	@Final
	private Minecraft minecraft;
	@Shadow
	public abstract Component getNameForDisplay(PlayerInfo info);
	@Unique
	private static final int ICON_SIZE = 8;
	@Unique
	private static final int ICON_PAD = 2;
	@Unique
	private static final int ICON_SHIFT = ICON_SIZE + ICON_PAD;
	@Unique
	private Map<String, PlayerInfo> lumine$nameToInfo;

	@Inject(method = "extractRenderState", at = @At("HEAD"))
	private void lumine$buildNameMap(GuiGraphicsExtractor graphics, int screenWidth,
			Scoreboard scoreboard, Objective displayObjective, CallbackInfo ci) {
		lumine$nameToInfo = new HashMap<>();
		ClientPacketListener connection = minecraft.getConnection();
		if (connection == null)
			return;
		for (PlayerInfo info : connection.getListedOnlinePlayers()) {
			lumine$nameToInfo.put(getNameForDisplay(info).getString(), info);
		}
	}

	@ModifyArgs(method = "extractRenderState",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"))
	private void lumine$onDrawName(Args args, GuiGraphicsExtractor graphics, int screenWidth,
			Scoreboard scoreboard, Objective displayObjective) {
		if (lumine$nameToInfo == null)
			return;
		Component text = args.get(1);
		PlayerInfo info = lumine$nameToInfo.get(text.getString());
		if (info == null)
			return;
		PeerData data = PeerRegistry.getInstance().get(info.getProfile().id());
		if (data == null)
			return;
		int x = args.get(2);
		int y = args.get(3);
		PeerIcon.draw(graphics, x, y, ICON_SIZE, data.r, data.g, data.b, data.a);
		args.set(2, x + ICON_SHIFT);
	}

	@Redirect(method = "extractRenderState",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/client/gui/Font;width(Lnet/minecraft/network/chat/FormattedText;)I"))
	private int lumine$nameWidth(Font font, FormattedText text) {
		int width = font.width(text);
		if (lumine$nameToInfo == null || !(text instanceof Component component))
			return width;
		PlayerInfo info = lumine$nameToInfo.get(component.getString());
		if (info == null)
			return width;
		PeerData data = PeerRegistry.getInstance().get(info.getProfile().id());
		if (data == null)
			return width;
		return width + ICON_SHIFT;
	}
}
