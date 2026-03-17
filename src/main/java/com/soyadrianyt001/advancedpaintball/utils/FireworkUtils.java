package com.soyadrianyt001.advancedpaintball.utils;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

public final class FireworkUtils {

    private FireworkUtils() {}

    public static void launch(Location loc, Color color1, Color color2) {
        Firework fw = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder()
            .with(FireworkEffect.Type.BALL_LARGE)
            .withColor(color1)
            .withFade(color2)
            .withFlicker()
            .withTrail()
            .build());
        meta.setPower(1);
        fw.setFireworkMeta(meta);
    }

    public static void launchTeam(Location loc, com.soyadrianyt001.advancedpaintball.models.Game.Team team) {
        Color c1, c2;
        switch (team) {
            case RED   -> { c1 = Color.RED;    c2 = Color.ORANGE; }
            case PINK  -> { c1 = Color.FUCHSIA; c2 = Color.WHITE; }
            case GREEN -> { c1 = Color.GREEN;  c2 = Color.LIME;   }
            case YELLOW-> { c1 = Color.YELLOW; c2 = Color.GOLD;   }
            default    -> { c1 = Color.WHITE;  c2 = Color.GRAY;   }
        }
        launch(loc, c1, c2);
    }
}
