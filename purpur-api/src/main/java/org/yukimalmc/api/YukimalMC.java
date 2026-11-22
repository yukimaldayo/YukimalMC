package org.yukimalmc.api;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.yukimalmc.api.resourcepack.ResourcePackManager;

public final class YukimalMC {
    private static ResourcePackManager resourcePacks;

    private YukimalMC() {
    }

    /** リソースパックの管理APIを取得します。 */
    public static @NotNull ResourcePackManager resourcePacks() {
        if (resourcePacks == null) {
            throw new IllegalStateException("YukimalMC API is not available yet");
        }
        return resourcePacks;
    }

    @ApiStatus.Internal
    public static void setResourcePacks(ResourcePackManager manager) {
        resourcePacks = manager;
    }
}