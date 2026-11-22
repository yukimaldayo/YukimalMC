package org.purpurmc.purpur;

import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class YukimalResourcePacks {
    public record Pack(String name, UUID id, String url, String sha1) {}

    /** プレイヤーごとの、いま送っているパック(パックID -> sha1) */
    private static final Map<UUID, Map<UUID, String>> applied = new ConcurrentHashMap<>();

    /** 入室したとき */
    public static void onJoin(ServerPlayer player) {
        try {
            applied.put(player.getUUID(), new HashMap<>()); // 新しい接続には、まだ何も送っていない
            apply(player);
        } catch (Exception ex) {
            Bukkit.getLogger().log(Level.WARNING, "[YukimalMC] Failed to send resource packs", ex);
        }
    }

    /** ワールドを移ったとき */
    public static void onWorldChange(ServerPlayer player) {
        try {
            apply(player);
        } catch (Exception ex) {
            Bukkit.getLogger().log(Level.WARNING, "[YukimalMC] Failed to update resource packs", ex);
        }
    }

    /** 設定を読み直したあと、接続中の全員に反映する */
    public static void refreshAll() {
        try {
            Set<UUID> online = new HashSet<>();
            for (ServerPlayer player : new ArrayList<>(MinecraftServer.getServer().getPlayerList().getPlayers())) {
                online.add(player.getUUID());
                try {
                    apply(player);
                } catch (Exception ex) {
                    Bukkit.getLogger().log(Level.WARNING, "[YukimalMC] Failed to update resource packs for " + player.getScoreboardName(), ex);
                }
            }
            applied.keySet().retainAll(online); // 退出したプレイヤーの記録を消す
        } catch (Exception ex) {
            Bukkit.getLogger().log(Level.WARNING, "[YukimalMC] Failed to refresh resource packs", ex);
        }
    }

    /** 設定に書かれているパックの数 */
    public static int configuredPackCount() {
        return YukimalConfig.config.getMapList("resource-pack.packs").size();
    }

    /** 設定に書かれている全パック(API用) */
    public static List<Pack> loadAll() {
        List<Pack> all = new ArrayList<>();
        int index = 0;
        for (Map<?, ?> raw : YukimalConfig.config.getMapList("resource-pack.packs")) {
            Object urlObj = raw.get("url");
            if (urlObj == null) continue;
            String url = urlObj.toString();
            Object shaObj = raw.get("sha1");
            String sha1 = shaObj == null ? "" : shaObj.toString().toLowerCase();
            Object nameObj = raw.get("name");
            String name = nameObj == null ? "pack" + index : nameObj.toString();
            index++;
            all.add(new Pack(name, UUID.nameUUIDFromBytes(url.getBytes(StandardCharsets.UTF_8)), url, sha1));
        }
        return all;
    }

    public static List<String> appliedNames(UUID playerId) {
        Map<UUID, String> current = applied.get(playerId);
        List<String> names = new ArrayList<>();
        if (current == null) return names;
        for (Pack pack : loadAll()) {
            if (current.containsKey(pack.id())) names.add(pack.name());
        }
        return names;
    }

    public static void refresh(ServerPlayer player) {
        onWorldChange(player);
    }

    private static void apply(ServerPlayer player) {
        if (!"url".equals(YukimalConfig.packMode)) return; // self-host方式は、あとで追加

        Map<UUID, Pack> wanted = wantedFor(player);
        Map<UUID, String> current = applied.computeIfAbsent(player.getUUID(), k -> new HashMap<>());

        // 不要になったパック、またはsha1が変わったパックを外す
        for (UUID id : new ArrayList<>(current.keySet())) {
            Pack want = wanted.get(id);
            if (want == null || !want.sha1().equals(current.get(id))) {
                player.connection.send(new ClientboundResourcePackPopPacket(Optional.of(id)));
                current.remove(id);
            }
        }

        // 足りないパックを送る
        for (Pack pack : wanted.values()) {
            if (!current.containsKey(pack.id())) {
                player.connection.send(new ClientboundResourcePackPushPacket(pack.id(), pack.url(), pack.sha1(), YukimalConfig.packRequired, Optional.empty()));
                current.put(pack.id(), pack.sha1());
            }
        }
    }

    /** このプレイヤーに、いま送りたいパックの一覧 */
    private static Map<UUID, Pack> wantedFor(ServerPlayer player) {
        Map<String, Pack> byName = new LinkedHashMap<>();
        List<Pack> all = new ArrayList<>();
        for (Pack pack : loadAll()) {
            if (pack.sha1().length() > 40) {
                Bukkit.getLogger().warning("[YukimalMC] sha1 is too long for pack " + pack.url() + " (skipped)");
                continue;
            }
            all.add(pack);
            byName.put(pack.name(), pack);
        }

        Map<UUID, Pack> wanted = new LinkedHashMap<>();

        // global の指定がなければ、今までどおり、全部のパックを全員に送る
        if (!YukimalConfig.config.isSet("resource-pack.global")) {
            for (Pack pack : all) wanted.put(pack.id(), pack);
            return wanted;
        }

        List<String> names = new ArrayList<>(YukimalConfig.config.getStringList("resource-pack.global"));
        String worldName = player.getBukkitEntity().getWorld().getName();
        names.addAll(YukimalConfig.config.getStringList("resource-pack.worlds." + worldName));
        for (String name : names) {
            Pack pack = byName.get(name);
            if (pack == null) {
                Bukkit.getLogger().warning("[YukimalMC] Unknown resource pack name in yukimal.yml: " + name);
                continue;
            }
            wanted.put(pack.id(), pack);
        }
        return wanted;
    }
}