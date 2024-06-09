package cn.cyanbukkit.bukkit.addon;

import mchorse.metamorph.api.MorphAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.lang.reflect.Method;

public class PlayerListener implements Listener {

    @EventHandler( priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerDeathEvent e) {
        Player p = e.getEntity();
        net.minecraft.entity.player.EntityPlayer entityPlayer = null;
        try {
            Method getHandle = p.getClass().getMethod("getHandle");
            entityPlayer = (net.minecraft.entity.player.EntityPlayer) getHandle.invoke(p);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // 如果玩家处于变身的状态就关闭变身
        MorphAPI.demorph(entityPlayer);
        p.spigot().respawn();
        p.setHealth(p.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getBaseValue());
        p.teleport(p.getWorld().getSpawnLocation());
    }

}
