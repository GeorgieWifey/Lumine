package luna.cats.lumine.client;

import java.util.Objects;
import java.util.UUID;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Lumine {
	private static final @NotNull Logger LOGGER = LogManager.getLogger(Lumine.class);
	private static final @NotNull Lumine INSTANCE = new Lumine();
	private volatile @Nullable String lunarVersion;
	private volatile @Nullable String lunarGitCommit;
	private volatile @Nullable String clientBrand;
	private volatile @Nullable String authenticatorJwt;
	private volatile @Nullable UUID authenticatedUuid;
	private volatile long modStatusSeed;

	private Lumine() {
	}

	public static @NotNull Lumine get() {
		return Objects.requireNonNull(INSTANCE);
	}

	public @NotNull String getVersion() {
		return FabricLoader.getInstance().getModContainer("lumine")
				.map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("dev");
	}

	public @Nullable String getLunarVersion() {
		return lunarVersion;
	}

	public @Nullable String getLunarGitCommit() {
		return lunarGitCommit;
	}

	public @Nullable String getClientBrand() {
		return clientBrand;
	}

	public void setLunarVersion(@NotNull String version, @NotNull String gitCommit) {
		this.lunarVersion = version;
		this.lunarGitCommit = gitCommit;
		this.clientBrand = "lunarclient:" + version;
		LOGGER.info("Updated spoof target: Lunar Client {} ({})", version,
				gitCommit.substring(0, Math.min(7, gitCommit.length())));
	}

	public @Nullable String getAuthenticatorJwt() {
		return authenticatorJwt;
	}

	public @Nullable UUID getAuthenticatedUuid() {
		return authenticatedUuid;
	}

	public void setAuthenticatorJwt(@NotNull String jwt, @NotNull UUID uuid) {
		this.authenticatorJwt = jwt;
		this.authenticatedUuid = uuid;
	}

	public long getModStatusSeed() {
		return modStatusSeed;
	}

	public void setModStatusSeed(long seed) {
		this.modStatusSeed = seed;
	}

	public void clearAuth() {
		this.authenticatorJwt = null;
		this.authenticatedUuid = null;
	}
}
