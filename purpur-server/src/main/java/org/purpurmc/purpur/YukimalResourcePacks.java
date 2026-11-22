package org.purpurmc.purpur;

import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.server.level.ServerPlayer;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public class YukimalResourcePacks {
    public static void onJoin(ServerPlayer player) {
        try {
            if (!"url".equals(YukimalConfig.packMode)) return; // self-host方式は、あとで追加

            List<Map<?, ?>> packs = YukimalConfig.config.getMapList("resource-pack.packs");
            for (Map<?, ?> pack : packs) {
                Object urlObj = pack.get("url");
                if (urlObj == null) continue;
                String url = urlObj.toString();
                Object shaObj = pack.get("sha1");
                String sha1 = shaObj == null ? "" : shaObj.toString().toLowerCase();
                if (sha1.length() > 40) {
                    org.bukkit.Bukkit.getLogger().warning("[YukimalMC] sha1 is too long for pack " + url + " (skipped)");
                    continue;
                }
                UUID id = UUID.nameUUIDFromBytes(url.getBytes(StandardCharsets.UTF_8));
                player.connection.send(new ClientboundResourcePackPushPacket(id, url, sha1, YukimalConfig.packRequired, Optional.empty()));
            }
        } catch (Exception ex) {
            // ここでの失敗が、プレイヤーの切断につながらないようにする
            org.bukkit.Bukkit.getLogger().log(Level.WARNING, "[YukimalMC] Failed to send resource packs", ex);
        }
    }
}