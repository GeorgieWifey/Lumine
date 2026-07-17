package luna.cats.lumine.client.apollo;

import com.google.protobuf.Any;
import com.lunarclient.apollo.common.v1.LunarClientVersion;
import com.lunarclient.apollo.common.v1.MinecraftVersion;
import com.lunarclient.apollo.player.v1.EmbeddedCheckoutSupport;
import com.lunarclient.apollo.player.v1.PlayerHandshakeMessage;
import luna.cats.lumine.client.Lumine;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.SharedConstants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ApolloHandler {
	// Every channel registered here (type + receiver) is advertised to the
	// server in the minecraft:register payload, matching what real Lunar
	// clients announce.
	public static final Identifier CHANNEL_APOLLO = Identifier.fromNamespaceAndPath("lunar",
			"apollo");
	public static final Identifier CHANNEL_APOLLO_JSON = Identifier.fromNamespaceAndPath("apollo",
			"json");
	public static final Identifier CHANNEL_PM = Identifier.fromNamespaceAndPath("lunarclient",
			"pm");
	public static final Identifier CHANNEL_TIMERS = Identifier.fromNamespaceAndPath("badlion",
			"timers");
	public static final Identifier CHANNEL_TRANSFER = Identifier.fromNamespaceAndPath("transfer",
			"channel");
	public static final CustomPacketPayload.Type<RawPayload> APOLLO_TYPE = new CustomPacketPayload.Type<>(
			CHANNEL_APOLLO);
	public static final CustomPacketPayload.Type<RawPayload> APOLLO_JSON_TYPE = new CustomPacketPayload.Type<>(
			CHANNEL_APOLLO_JSON);
	public static final CustomPacketPayload.Type<RawPayload> PM_TYPE = new CustomPacketPayload.Type<>(
			CHANNEL_PM);
	public static final CustomPacketPayload.Type<RawPayload> TIMERS_TYPE = new CustomPacketPayload.Type<>(
			CHANNEL_TIMERS);
	public static final CustomPacketPayload.Type<RawPayload> TRANSFER_TYPE = new CustomPacketPayload.Type<>(
			CHANNEL_TRANSFER);
	private static final Logger LOGGER = LogManager.getLogger();

	private ApolloHandler() {
	}

	public static void init() {
		register(APOLLO_TYPE, true);
		register(APOLLO_JSON_TYPE, false);
		registerLegacy(PM_TYPE);
		registerLegacy(TIMERS_TYPE);
		registerLegacy(TRANSFER_TYPE);
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			if (client.hasSingleplayerServer()) {
				return;
			}
			sendPlayerHandshake(client);
		});
		LOGGER.info("Apollo handler initialized");
	}

	private static void registerLegacy(CustomPacketPayload.Type<RawPayload> type) {
		StreamCodec<RegistryFriendlyByteBuf, RawPayload> codec = RawPayload.codec(type);
		PayloadTypeRegistry.clientboundPlay().register(type, codec);
		PayloadTypeRegistry.serverboundPlay().register(type, codec);
		ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
		});
	}

	private static void register(CustomPacketPayload.Type<RawPayload> type, boolean protobuf) {
		StreamCodec<RegistryFriendlyByteBuf, RawPayload> codec = RawPayload.codec(type);
		PayloadTypeRegistry.clientboundPlay().register(type, codec);
		PayloadTypeRegistry.serverboundPlay().register(type, codec);
		ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
			ApolloMessageHandler handler = ApolloMessageHandler.getInstance();
			if (protobuf) {
				handler.handleProtobufMessage(payload.data());
			} else {
				handler.handleJsonMessage(payload.data());
			}
		});
	}

	private static void sendPlayerHandshake(net.minecraft.client.Minecraft client) {
		String version = Lumine.get().getLunarVersion();
		String gitCommit = Lumine.get().getLunarGitCommit();
		if (version == null || gitCommit == null) {
			LOGGER.warn("Lunar version not yet available, skipping Apollo handshake");
			return;
		}
		MinecraftVersion minecraftVersion = MinecraftVersion.newBuilder()
				.setEnum(currentMinecraftVersion()).build();
		String semver = version.startsWith("v") ? version : "v" + version;
		LunarClientVersion lunarClientVersion = LunarClientVersion.newBuilder()
				.setGitBranch("master").setGitCommit(gitCommit).setSemver(semver).build();
		PlayerHandshakeMessage.Builder handshake = PlayerHandshakeMessage.newBuilder()
				.setMinecraftVersion(minecraftVersion).setLunarClientVersion(lunarClientVersion)
				.addAllInstalledMods(InstalledMods.handshakeMods())
				.setEmbeddedCheckoutSupport(
						EmbeddedCheckoutSupport.EMBEDDED_CHECKOUT_SUPPORT_WINDOW);
		ModStatusGenerator.apply(handshake, client.getUser().getProfileId());
		Any any = Any.pack(handshake.build());
		ClientPlayNetworking.send(new RawPayload(APOLLO_TYPE, any.toByteArray()));
		LOGGER.info("Sent Apollo player handshake");
	}

	private static String currentMinecraftVersion() {
		return "v" + SharedConstants.getCurrentVersion().id().replace('.', '_');
	}
}
