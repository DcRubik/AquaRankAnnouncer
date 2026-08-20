package com.dcrubik.announcer;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new RankListener(this), this);
        getLogger().info("¡Plugin de Anuncios de Rango iniciado correctamente!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin deshabilitado.");
    }
}