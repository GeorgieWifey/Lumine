package luna.cats.lumine.client.peers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import luna.cats.lumine.client.Lumine;
import luna.cats.lumine.client.peers.auth.AuthClient;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;

public final class SubscriptionManager {
	private static SubscriptionManager instance;
	private final Set<UUID> fetchedSet = new HashSet<>();
	private WSClient wsClient;

	private SubscriptionManager() {
	}

	public static void init() {
		instance = new SubscriptionManager();
		ClientTickEvents.START_CLIENT_TICK.register(instance::onClientTick);
	}

	public static boolean isConnected() {
		return instance != null && instance.wsClient != null && instance.wsClient.isConnected();
	}

	/**
	 * Disconnects any existing WS and creates a fresh connection. Called on first
	 * auth and on account switch.
	 */
	public static void connect(String jwt) {
		if (instance == null)
			return;
		if (instance.wsClient != null) {
			instance.wsClient.disconnect();
		}
		instance.fetchedSet.clear();
		PeerRegistry.getInstance().clear();
		instance.wsClient = new WSClient();
		instance.wsClient.setOnReconnect(() -> {
			instance.fetchedSet.clear();
		});
		instance.wsClient.connect(() -> {
			String freshJwt = AuthClient.fetchJwt();
			Lumine.get().setAuthenticatorJwt(freshJwt,
					Minecraft.getInstance().getUser().getProfileId());
			return freshJwt;
		});
	}

	private void onClientTick(Minecraft client) {
		if (wsClient == null || !wsClient.isConnected())
			return;
		syncWithTabList(client);
	}

	private void syncWithTabList(Minecraft mc) {
		ClientPacketListener connection = mc.getConnection();
		if (connection == null || mc.player == null)
			return;
		UUID self = mc.player.getUUID();
		Set<UUID> current = new HashSet<>();
		for (PlayerInfo info : connection.getListedOnlinePlayers()) {
			UUID uuid = info.getProfile().id();
			if (!uuid.equals(self)) {
				current.add(uuid);
			}
		}
		List<UUID> toFetch = new ArrayList<>();
		for (UUID uuid : current) {
			if (!fetchedSet.contains(uuid)) {
				toFetch.add(uuid);
			}
		}
		if (!toFetch.isEmpty()) {
			fetchedSet.addAll(toFetch);
			wsClient.loadTabLogos(toFetch);
		}
		List<UUID> left = new ArrayList<>();
		for (UUID uuid : fetchedSet) {
			if (!current.contains(uuid)) {
				left.add(uuid);
			}
		}
		if (!left.isEmpty()) {
			left.forEach(fetchedSet::remove);
			left.forEach(u -> PeerRegistry.getInstance().remove(u));
		}
	}
}
