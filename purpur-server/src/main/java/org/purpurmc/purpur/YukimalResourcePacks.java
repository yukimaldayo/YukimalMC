package org.purpurmc.purpur;

import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.server.level.ServerPlayer;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class YukimalResourcePacks {
    public static void onJoin(ServerPlayer player) {
        if (!"url".equals(YukimalConfig.packMode)) return; // self-host方式は、あとで追加

        List<Map<?, ?>> packs = YukimalConfig.config.getMapList("resource-pack.packs");
        for (Map<?, ?> pack : packs) {
            Object urlObj = pack.get("url");
            if (urlObj == null) continue;
            String url = urlObj.toString();
            Object shaObj = pack.get("sha1");
            String sha1 = shaObj == null ? "" : shaObj.toString().toLowerCase();
            UUID id = UUID.nameUUIDFromBytes(url.getBytes(StandardCharsets.UTF_8));
            player.connection.send(new ClientboundResourcePackPushPacket(id, url, sha1, YukimalConfig.packRequired, Optional.empty()));
        }
    }
}