package luna.cats.lumine.client.version;

import com.google.gson.Gson;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LumineConfig {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Gson GSON = new Gson();
	private static final long STALE_MS = 24 * 60 * 60 * 1000L;
	public String lunarVersion;
	public String lunarGitCommit;
	public long fetchedAt;
	public Long modStatusSeed;

	private LumineConfig() {
	}

	public boolean isStale() {
		return lunarVersion == null || lunarGitCommit == null
				|| (System.currentTimeMillis() - fetchedAt) > STALE_MS;
	}

	private static File getCacheFile() {
		File dir = new File(Minecraft.getInstance().gameDirectory, "lumine");
		dir.mkdirs();
		return new File(dir, "version_cache.json");
	}

	public static LumineConfig load() {
		File file = getCacheFile();
		if (file.exists()) {
			try (Reader reader = new InputStreamReader(Files.newInputStream(file.toPath()),
					StandardCharsets.UTF_8)) {
				LumineConfig cfg = GSON.fromJson(reader, LumineConfig.class);
				if (cfg != null && cfg.lunarVersion != null && cfg.lunarGitCommit != null) {
					if (cfg.modStatusSeed == null) {
						cfg.modStatusSeed = UUID.randomUUID().getLeastSignificantBits();
						cfg.save();
					}
					return cfg;
				}
			} catch (Exception e) {
				LOGGER.warn("Failed to load version cache: {}", e.getMessage());
			}
		}
		LumineConfig cfg = new LumineConfig();
		cfg.modStatusSeed = UUID.randomUUID().getLeastSignificantBits();
		cfg.save();
		return cfg;
	}

	public void save() {
		try (Writer writer = new OutputStreamWriter(Files.newOutputStream(getCacheFile().toPath()),
				StandardCharsets.UTF_8)) {
			GSON.toJson(this, writer);
		} catch (Exception e) {
			LOGGER.warn("Failed to save version cache: {}", e.getMessage());
		}
	}
}
