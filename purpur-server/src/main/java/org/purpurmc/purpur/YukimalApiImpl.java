package org.purpurmc.purpur;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.yukimalmc.api.YukimalMC;
import org.yukimalmc.api.resourcepack.ResourcePackInfo;
import org.yukimalmc.api.resourcepack.ResourcePackManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class YukimalApiImpl implements ResourcePackManager {
    public static void register() {
        YukimalMC.setResourcePacks(new YukimalApiImpl());
    }

    @Override
    public @NotNull List<ResourcePackInfo> getPacks() {
        List<ResourcePackInfo> list = new ArrayList<>();
        for (YukimalResourcePacks.Pack pack : YukimalResourcePacks.loadAll()) {
            list.add(new ResourcePackInfo(pack.name(), pack.id(), pack.url(), pack.sha1()));
        }
        return Collections.unmodifiableList(list);
    }

    @Override
    public @NotNull List<String> getAppliedPackNames(@NotNull Player player) {
        return YukimalResourcePacks.appliedNames(player.getUniqueId());
    }

    @Override
    public void refresh(@NotNull Player player) {
        YukimalResourcePacks.refresh(((CraftPlayer) player).getHandle());
    }

    @Override
    public void refreshAll() {
        YukimalResourcePacks.refreshAll();
    }
}