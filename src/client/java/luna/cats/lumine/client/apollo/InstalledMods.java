package luna.cats.lumine.client.apollo;

import com.lunarclient.apollo.modsetting.v1.Mod;
import com.lunarclient.apollo.modsetting.v1.ModGroup;
import com.lunarclient.apollo.player.v1.ModMessage;
import java.util.ArrayList;
import java.util.List;
import luna.cats.lumine.client.Lumine;

/**
 * The fabric mods Lunar Client ships internally (per its launcher artifact
 * list), reported as FABRIC_INTERNAL both in the Apollo handshake and in
 * InstalledModsResponse roundtrips.
 */
final class InstalledMods {
	private record Entry(String id, String displayName, String version) {
	}

	private static final List<Entry> MODS = List.of(new Entry("sodium", "Sodium", "0.6.13"),
			new Entry("modmenu", "Mod Menu", "13.0.3"),
			new Entry("noxesium", "Noxesium", "2.6.4"),
			new Entry("replaymod", "Replay Mod", "2.6.21"),
			new Entry("flashback", "Flashback", "0.35.2"),
			new Entry("ferrite-core", "FerriteCore", "7.1.2"));

	private InstalledMods() {
	}

	static List<ModMessage> handshakeMods() {
		List<ModMessage> mods = new ArrayList<>();
		String lunarVersion = Lumine.get().getLunarVersion();
		if (lunarVersion != null) {
			mods.add(ModMessage.newBuilder().setId("lunar").setName("Lunar Client")
					.setVersion(lunarVersion).setType(ModMessage.Type.TYPE_FABRIC_INTERNAL)
					.build());
		}
		for (Entry entry : MODS) {
			mods.add(ModMessage.newBuilder().setId(entry.id()).setName(entry.displayName())
					.setVersion(entry.version()).setType(ModMessage.Type.TYPE_FABRIC_INTERNAL)
					.build());
		}
		return mods;
	}

	static ModGroup modGroup() {
		ModGroup.Builder group = ModGroup.newBuilder()
				.setType(ModGroup.Type.TYPE_FABRIC_INTERNAL);
		String lunarVersion = Lumine.get().getLunarVersion();
		if (lunarVersion != null) {
			group.addMods(Mod.newBuilder().setId("lunar").setVersion(lunarVersion));
		}
		for (Entry entry : MODS) {
			group.addMods(Mod.newBuilder().setId(entry.id()).setVersion(entry.version()));
		}
		return group.build();
	}
}
