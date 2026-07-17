package luna.cats.lumine.client.apollo;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** A raw byte-array payload for Lunar's plugin channels. */
public record RawPayload(CustomPacketPayload.Type<RawPayload> type, byte[] data)
		implements CustomPacketPayload {

	static StreamCodec<RegistryFriendlyByteBuf, RawPayload> codec(
			CustomPacketPayload.Type<RawPayload> type) {
		return StreamCodec.of((buf, payload) -> buf.writeBytes(payload.data()), buf -> {
			byte[] data = new byte[buf.readableBytes()];
			buf.readBytes(data);
			return new RawPayload(type, data);
		});
	}
}
