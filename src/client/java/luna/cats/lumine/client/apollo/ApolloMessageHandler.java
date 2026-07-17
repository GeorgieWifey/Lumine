package luna.cats.lumine.client.apollo;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lunarclient.apollo.configurable.v1.OverrideConfigurableSettingsMessage;
import com.lunarclient.apollo.modsetting.v1.InstalledModsRequest;
import com.lunarclient.apollo.modsetting.v1.InstalledModsResponse;
import com.lunarclient.apollo.transfer.v1.PingRequest;
import com.lunarclient.apollo.transfer.v1.PingResponse;
import com.lunarclient.apollo.transfer.v1.TransferRequest;
import com.lunarclient.apollo.transfer.v1.TransferResponse;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class ApolloMessageHandler {
	private static final Logger LOGGER = LogManager.getLogger();
	private static ApolloMessageHandler instance;

	public static ApolloMessageHandler getInstance() {
		if (instance == null) {
			instance = new ApolloMessageHandler();
		}
		return instance;
	}

	private ApolloMessageHandler() {
	}

	public void handleProtobufMessage(byte[] data) {
		try {
			Any any = Any.parseFrom(data);
			handleProtobufAny(any);
		} catch (InvalidProtocolBufferException e) {
			LOGGER.warn("Failed to parse Apollo protobuf message: {}", e.getMessage());
		}
	}

	public void handleJsonMessage(byte[] data) {
		try {
			String json = new String(data, StandardCharsets.UTF_8);
			JsonObject message = JsonParser.parseString(json).getAsJsonObject();
			handleJsonObject(message);
		} catch (Exception e) {
			LOGGER.warn("Failed to parse Apollo JSON message: {}", e.getMessage());
		}
	}

	private void handleProtobufAny(@NotNull Any any) throws InvalidProtocolBufferException {
		if (any.is(OverrideConfigurableSettingsMessage.class)) {
			handleOverrideConfigurableSettings(
					any.unpack(OverrideConfigurableSettingsMessage.class));
		} else if (any.is(TransferRequest.class)) {
			handleTransferRequest(any.unpack(TransferRequest.class));
		} else if (any.is(PingRequest.class)) {
			handlePingRequest(any.unpack(PingRequest.class));
		} else if (any.is(InstalledModsRequest.class)) {
			handleInstalledModsRequest(any.unpack(InstalledModsRequest.class));
		}
	}

	private void handleJsonObject(@NotNull JsonObject message) {
		String type = message.has("@type") ? message.get("@type").getAsString() : "unknown";
		if (type.contains("OverrideConfigurableSettingsMessage")) {
			LOGGER.info("Server sent Apollo module configuration");
		}
	}

	private void handleOverrideConfigurableSettings(
			@NotNull OverrideConfigurableSettingsMessage message) {
		LOGGER.info("Server configured {} Apollo module(s)", message.getConfigurableSettingsCount());
	}

	private void handleTransferRequest(@NotNull TransferRequest message) {
		String serverIp = message.getServerIp();
		UUID requestId = UUID.fromString(message.getRequestId().toStringUtf8());
		LOGGER.info("Server requested transfer to {} (request ID: {})", serverIp, requestId);
		sendTransferResponse(requestId, TransferResponse.Status.STATUS_REJECTED);
	}

	private void handlePingRequest(@NotNull PingRequest message) {
		UUID requestId = UUID.fromString(message.getRequestId().toStringUtf8());
		sendPingResponse(requestId);
	}

	private void handleInstalledModsRequest(@NotNull InstalledModsRequest message) {
		InstalledModsResponse response = InstalledModsResponse.newBuilder()
				.setRequestId(message.getRequestId()).setPage(0).setTotalPages(1)
				.addModGroups(InstalledMods.modGroup()).build();
		send(Any.pack(response));
	}

	private void send(@NotNull Any any) {
		if (Minecraft.getInstance().getConnection() == null) {
			return;
		}
		ClientPlayNetworking.send(new RawPayload(ApolloHandler.APOLLO_TYPE, any.toByteArray()));
	}

	private void sendTransferResponse(@NotNull UUID requestId,
			@NotNull TransferResponse.Status status) {
		TransferResponse response = TransferResponse.newBuilder()
				.setRequestId(ByteString.copyFromUtf8(requestId.toString())).setStatus(status)
				.build();
		send(Any.pack(response));
	}

	private void sendPingResponse(@NotNull UUID requestId) {
		PingResponse response = PingResponse.newBuilder()
				.setRequestId(ByteString.copyFromUtf8(requestId.toString())).build();
		send(Any.pack(response));
	}
}
