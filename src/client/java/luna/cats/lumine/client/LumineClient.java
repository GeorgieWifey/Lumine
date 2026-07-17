package luna.cats.lumine.client;

import java.util.UUID;
import luna.cats.lumine.client.apollo.ApolloHandler;
import luna.cats.lumine.client.peers.SubscriptionManager;
import luna.cats.lumine.client.peers.auth.AuthClient;
import luna.cats.lumine.client.version.LumineConfig;
import luna.cats.lumine.client.version.VersionFetcher;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LumineClient implements ClientModInitializer {
	private static final Logger LOGGER = LogManager.getLogger("lumine");

	@Override
	public void onInitializeClient() {
		LOGGER.info("Lumine {} initializing...", Lumine.get().getVersion());
		LumineConfig config = LumineConfig.load();
		Lumine.get().setModStatusSeed(config.modStatusSeed);
		if (config.lunarVersion != null && config.lunarGitCommit != null) {
			Lumine.get().setLunarVersion(config.lunarVersion, config.lunarGitCommit);
		}
		if (config.isStale()) {
			VersionFetcher fetcher = new VersionFetcher(config);
			if (Lumine.get().getLunarVersion() == null) {
				// First launch with no cache: block so the version is available
				// before any server connection.
				fetcher.fetch();
			} else {
				fetcher.fetchAsync();
			}
		}
		ApolloHandler.init();
		SubscriptionManager.init();
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> authenticate(client));
	}

	/**
	 * Authenticates with Lunar on every server connection. Re-authenticates if the
	 * account changed (e.g. via an account switcher mod).
	 */
	private static void authenticate(Minecraft client) {
		UUID currentUuid = client.getUser().getProfileId();
		if (currentUuid.equals(Lumine.get().getAuthenticatedUuid()) && SubscriptionManager.isConnected()) {
			return;
		}
		Lumine.get().clearAuth();
		AuthClient.fetchJwtAsync(jwt -> {
			Lumine.get().setAuthenticatorJwt(jwt, currentUuid);
			SubscriptionManager.connect(jwt);
		}, err -> LOGGER.warn("JWT fetch failed, peer icons disabled", err));
	}
}
