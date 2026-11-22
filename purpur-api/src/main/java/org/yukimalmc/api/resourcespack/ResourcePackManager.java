package org.yukimalmc.api.resourcepack;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ResourcePackManager {
    /** yukimal.yml に設定されている全パック */
    @NotNull List<ResourcePackInfo> getPacks();

    /** そのプレイヤーに、いま適用しているパックの名前 */
    @NotNull List<String> getAppliedPackNames(@NotNull Player player);

    /** そのプレイヤーのパックを、設定に合わせて更新します */
    void refresh(@NotNull Player player);

    /** 接続中の全員のパックを、設定に合わせて更新します */
    void refreshAll();
}