package org.purpurmc.purpur;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class YukimalConfig {
    private static final File CONFIG_FILE = new File("yukimal.yml");
    public static final File PACK_DIR = new File("Resourcepack");
    public static YamlConfiguration config;

    public static String packMode = "url";
    public static boolean packRequired = false;
    public static int selfHostPort = 8765;
    public static String selfHostAddress = "";

    public static void init() {
        YamlConfiguration loaded = new YamlConfiguration();
        try {
            loaded.load(CONFIG_FILE);
        } catch (IOException ignore) {
            // まだファイルがない場合
        } catch (InvalidConfigurationException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not load yukimal.yml, please correct your syntax errors", ex);
            return; // 壊れたファイルを上書きしないよう、保存せずに戻る
        }
        config = loaded;
        config.options().copyDefaults(true);

        packMode = getString("resource-pack.mode", "url").toLowerCase();
        packRequired = getBoolean("resource-pack.required", false);
        selfHostPort = getInt("resource-pack.self-host.port", 8765);
        selfHostAddress = getString("resource-pack.self-host.public-address", "");
        config.addDefault("resource-pack.packs", new java.util.ArrayList<String>());

        if (!PACK_DIR.exists() && !PACK_DIR.mkdirs()) {
            Bukkit.getLogger().log(Level.WARNING, "Could not create the Resourcepack folder");
        }

        try {
            config.save(CONFIG_FILE);
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not save yukimal.yml", ex);
        }
    }

    public static File[] listPacks() {
        File[] files = PACK_DIR.listFiles((dir, name) -> name.toLowerCase().endsWith(".zip"));
        return files == null ? new File[0] : files;
    }

    private static String getString(String path, String def) {
        config.addDefault(path, def);
        return config.getString(path, def);
    }

    private static boolean getBoolean(String path, boolean def) {
        config.addDefault(path, def);
        return config.getBoolean(path, def);
    }

    private static int getInt(String path, int def) {
        config.addDefault(path, def);
        return config.getInt(path, def);
    }
}