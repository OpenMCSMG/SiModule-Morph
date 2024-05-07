package cn.cyanbukkit.bukkit.addon;

import mchorse.metamorph.api.morphs.AbstractMorph;
import org.bukkit.entity.Player;

public class MorphData {
    private final AbstractMorph morph;
    private final Player p;
    private final int videoLength;

    public MorphData(AbstractMorph morph, Player p, int videoLength) {
        this.morph = morph;
        this.p = p;
        this.videoLength = videoLength;
    }

    public AbstractMorph getMorph() {
        return morph;
    }

    public Player getPlayer() {
        return p;
    }

    public int getVideoLength() {
        return videoLength;
    }
}