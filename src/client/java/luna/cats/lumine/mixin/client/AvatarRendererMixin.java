package luna.cats.lumine.mixin.client;

import luna.cats.lumine.client.peers.PeerRegistry;
import luna.cats.lumine.client.render.NametagIcon;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {
	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void lumine$addNametagIcon(Avatar entity, AvatarRenderState state, float partialTicks,
			CallbackInfo ci) {
		if (state.nameTag != null) {
			state.nameTag = NametagIcon.decorate(state.nameTag,
					PeerRegistry.getInstance().get(entity.getUUID()));
		}
	}
}
