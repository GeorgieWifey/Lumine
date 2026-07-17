package luna.cats.lumine.client.render;

import java.util.UUID;
import luna.cats.lumine.client.peers.PeerData;
import net.minecraft.network.chat.Component;

public final class NametagIconTest {
	private NametagIconTest() {
	}

	public static void main(String[] args) {
		Component name = Component.literal("Luna");
		assert NametagIcon.decorate(name, null) == name;

		PeerData peer = new PeerData(UUID.randomUUID(), 0.25F, 0.5F, 0.75F, 1.0F, true, 0);
		Component decorated = NametagIcon.decorate(name, peer);
		assert decorated.getString().equals("\uE000\uE001Luna");
		assert decorated.getSiblings().getFirst().getStyle().getFont().equals(NametagIcon.FONT);
		assert decorated.getSiblings().getFirst().getStyle().getColor().getValue() == 0x3F7FBF;
	}
}
