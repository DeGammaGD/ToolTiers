package elocindev.tierify.compat;

import elocindev.tierify.Tierify;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

/**
 * Compatibility boundary for networking registration.
 */
public final class NetworkingCompat {

    private NetworkingCompat() {
    }

    public static void registerPayloads() {
        PayloadTypeRegistry.clientboundPlay().register(Tierify.ATTRIBUTE_SYNC_PAYLOAD_ID, Tierify.ATTRIBUTE_SYNC_PAYLOAD_CODEC);
    }
}
