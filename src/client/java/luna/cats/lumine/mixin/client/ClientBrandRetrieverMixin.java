package luna.cats.lumine.mixin.client;

import luna.cats.lumine.client.Lumine;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientBrandRetriever.class)
public class ClientBrandRetrieverMixin {
	@Inject(method = "getClientModName", at = @At("HEAD"), cancellable = true)
	private static void lumine$spoofBrand(CallbackInfoReturnable<String> cir) {
		String brand = Lumine.get().getClientBrand();
		if (!Minecraft.getInstance().hasSingleplayerServer() && brand != null) {
			cir.setReturnValue(brand);
		}
	}
}
