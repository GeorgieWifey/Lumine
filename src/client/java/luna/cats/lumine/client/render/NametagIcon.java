package luna.cats.lumine.client.render;

import luna.cats.lumine.client.peers.PeerData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.Nullable;

public final class NametagIcon {
	static final FontDescription.Resource FONT = new FontDescription.Resource(
			Identifier.fromNamespaceAndPath("lumine", "lunar_logo"));

	private NametagIcon() {
	}

	public static Component decorate(Component name, @Nullable PeerData peer) {
		if (peer == null)
			return name;
		int color = ARGB.colorFromFloat(1.0F, peer.r, peer.g, peer.b);
		Component icon = Component.literal("\uE000\uE001")
				.withStyle(Style.EMPTY.withFont(FONT).withColor(color));
		return Component.empty().append(icon).append(name);
	}
}
