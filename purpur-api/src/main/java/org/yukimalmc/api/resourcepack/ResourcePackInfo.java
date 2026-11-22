package org.yukimalmc.api.resourcepack;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record ResourcePackInfo(@NotNull String name, @NotNull UUID id, @NotNull String url, @NotNull String sha1) {
}