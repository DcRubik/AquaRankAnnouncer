package com.dcrubik.announcer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class RankListener implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, String> estadoAnterior = new HashMap<>();
    private final Random random = new Random();

    public RankListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.iniciarRastreadorDeRangos();
    }

    private void iniciarRastreadorDeRangos() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID uuid = player.getUniqueId();

                    String rangoActual = "NINGUNO";
                    if (player.hasPermission("group.yt") || player.hasPermission("aquacore.rank.yt")) {
                        rangoActual = "YT";
                    } else if (player.hasPermission("group.streamer") || player.hasPermission("aquacore.rank.streamer")) {
                        rangoActual = "STREAMER";
                    }

                    if (!estadoAnterior.containsKey(uuid)) {
                        estadoAnterior.put(uuid, rangoActual);
                        continue;
                    }

                    String rangoPasado = estadoAnterior.get(uuid);

                    if (!rangoActual.equals(rangoPasado)) {
                        if (rangoActual.equals("YT")) {
                            ejecutarEfectos(player, "YT", "&c");
                        } else if (rangoActual.equals("STREAMER")) {
                            ejecutarEfectos(player, "Streamer", "&5");
                        }
                        estadoAnterior.put(uuid, rangoActual);
                    }
                }
            }
        }.runTaskTimer(plugin, 40L, 20L);
    }

    private void ejecutarEfectos(Player player, String rango, String colorRango) {
        String mensaje = ChatColor.translateAlternateColorCodes('&',
                "&c&k||&a&k||&b&k||&6 &6¡El usuario &b" + player.getName() + " &6ha recibido el rango &cYT&6/&5Streamer &6! &b&k||&a&k||&c&k||&r");

        Bukkit.broadcastMessage(mensaje);

        enviarTituloAnimado(player);
        iniciarRafagaFuegos(player);
    }

    private void enviarTituloAnimado(Player player) {
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                // Modificado: Ahora se repite 20 veces (Aprox 5 segundos totales de animación)
                if (!player.isOnline() || ticks >= 20) {
                    this.cancel();
                    return;
                }

                String titleText = (ticks % 2 == 0) ? "&c<o/" : "&6\\o>";
                String finalTitle = ChatColor.translateAlternateColorCodes('&', titleText);
                String subtitle = ChatColor.translateAlternateColorCodes('&', "&a¡Felicidades!");

                try {
                    // Tiempos ajustados: 0 de entrada, 30 ticks de duración por frame, 20 de salida al final
                    player.sendTitle(finalTitle, subtitle, 0, 30, 20);
                } catch (LinkageError | Exception e) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            "title " + player.getName() + " title {\"text\":\"" + finalTitle + "\"}");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            "title " + player.getName() + " subtitle {\"text\":\"" + subtitle + "\"}");
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 5L); // Cambia el frame cada 5 ticks
    }

    // NUEVO MÉTODO: Lanza una ráfaga masiva mientras el título esté activo
    private void iniciarRafagaFuegos(Player player) {
        new BukkitRunnable() {
            int cantidad = 0;

            @Override
            public void run() {
                // Lanza 10 oleadas de fuegos artificiales (durante 5 segundos)
                if (!player.isOnline() || cantidad >= 10) {
                    this.cancel();
                    return;
                }

                Location loc = player.getLocation();
                // Genera un ligero offset aleatorio para que no exploten todos exactamente en el mismo bloque
                double offsetX = (random.nextDouble() - 0.5) * 3;
                double offsetZ = (random.nextDouble() - 0.5) * 3;
                Location fuegoLoc = loc.clone().add(offsetX, 0.5, offsetZ);

                lanzarFuegoArtificial(fuegoLoc);
                cantidad++;
            }
        }.runTaskTimer(plugin, 0L, 10L); // Lanza un fuego artificial cada 10 ticks (medio segundo)
    }

    private void lanzarFuegoArtificial(Location loc) {
        if (loc.getWorld() == null) return;

        Firework fw = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta fm = fw.getFireworkMeta();

        // Agregamos múltiples efectos y combinaciones de colores para que sea hiper vistoso
        FireworkEffect effect1 = FireworkEffect.builder()
                .flicker(true)
                .withColor(Color.RED, Color.ORANGE, Color.YELLOW)
                .withFade(Color.PURPLE, Color.FUCHSIA)
                .with(FireworkEffect.Type.BALL_LARGE)
                .trail(true)
                .build();

        FireworkEffect effect2 = FireworkEffect.builder()
                .flicker(true)
                .withColor(Color.LIME, Color.GREEN, Color.AQUA)
                .with(FireworkEffect.Type.STAR)
                .trail(false)
                .build();

        fm.addEffect(effect1);
        fm.addEffect(effect2);
        fm.setPower(1); // Altura baja para que exploten cerca de su cabeza
        fw.setFireworkMeta(fm);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        estadoAnterior.remove(event.getPlayer().getUniqueId());
    }
}