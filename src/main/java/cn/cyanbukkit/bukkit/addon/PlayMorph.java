package cn.cyanbukkit.bukkit.addon;

import mchorse.metamorph.api.MorphAPI;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PlayMorph extends JavaPlugin {


    public static PlayMorph instance;
    public boolean isShow = false;
    public List<MorphData> list = new ArrayList<>();

    /**
     * Merge given args from given index
     *
     * Basically fold back string array argument back into string from given
     * index.
     */
    public static String mergeArgs(String[] args, int i)
    {
        String dataTag = "";

        for (; i < args.length; i++)
        {
            dataTag += args[i] + (i == args.length - 1 ? "" : " ");
        }

        return dataTag;
    }

    public void registerCommand(Command pluginCommand) throws NoSuchFieldException, IllegalAccessException {
        // 反射注册命令
        Class<?> cl = getServer().getPluginManager().getClass();
        Field df = cl.getDeclaredField("commandMap");
        df.setAccessible(true);
        SimpleCommandMap commandMap = (SimpleCommandMap) df.get(getServer().getPluginManager());
        commandMap.register(pluginCommand.getName(), pluginCommand);
    }

    @Override
    public void onEnable() {
        instance = this;
        try {
            registerCommand(new CM("playmorph"));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        getServer().getPluginManager().registerEvents(new PlayerListener(),this);
    }

    public void init(String[] args, Player p) {
        mchorse.metamorph.api.morphs.AbstractMorph morph;
        try {
            NBTTagCompound tag = JsonToNBT.getTagFromJson(mergeArgs(args, 2));
            tag.setString("Name", args[1]);
            morph = mchorse.metamorph.api.MorphManager.INSTANCE.morphFromNBT(tag);
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage("§c§l[变身]§r§c 未找到变身模型: " + args[1]);
            e.printStackTrace();
            return;
        }
        MorphData morphData = new MorphData(morph, p, (int) ((getLength(morph) / 20.0) * 1000L));
        if (isShow) {
            list.add(morphData);
        } else {
            Bukkit.getConsoleSender().sendMessage("§c§l[变身]§r§c 开始播放变身模型: " + morphData.getMorph().name + " 时长: " + getLength(morph) + "s");
            play(morphData);
        }
    }


    public void play(MorphData thisMorphData) {
        isShow = true;// Reflection to cast craftPlayer
        net.minecraft.entity.player.EntityPlayer entityPlayer = null;
        try {
            Method getHandle = thisMorphData.getPlayer().getClass().getMethod("getHandle");
            entityPlayer = (net.minecraft.entity.player.EntityPlayer) getHandle.invoke(thisMorphData.getPlayer());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 如果玩家处于变身的状态就关闭变身
        if (MorphAPI.demorph(entityPlayer)) {
            mchorse.metamorph.api.MorphAPI.morph(entityPlayer, thisMorphData.getMorph(), true);
        }
        // 调用 getHandle 方法获取 EntityPlayer 对象
        // Asynchronously find and play from the list
        net.minecraft.entity.player.EntityPlayer finalEntityPlayer = entityPlayer;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (!PlayMorph.instance.list.isEmpty()) {
                    play(PlayMorph.instance.list.get(0)); // Play the next one
                    PlayMorph.instance.list.remove(0);
                } else {
                    isShow = false;
                    // Close the morph
                    try {
                        mchorse.metamorph.api.MorphAPI.demorph(finalEntityPlayer);
                        Bukkit.getConsoleSender().sendMessage("§c§l[变身]§r§c 关闭变身模型: " + thisMorphData.getMorph().name);
                    } catch (Exception e) {
                        Bukkit.getConsoleSender().sendMessage("§c§l[变身]§r§c 未找到变身模型: " + thisMorphData.getMorph().name);
                        e.printStackTrace();
                        return;
                    }
                }
                cancel();
            }
        }, thisMorphData.getVideoLength());
    }


    private int getLength(mchorse.metamorph.api.morphs.AbstractMorph morph) {
        int ticks = 0;

        if (morph instanceof mchorse.metamorph.api.morphs.utils.IAnimationProvider) {
            mchorse.metamorph.api.morphs.utils.Animation animation = ((mchorse.metamorph.api.morphs.utils.IAnimationProvider) morph).getAnimation();

            if (animation.animates) {
                ticks = animation.duration;
            }
        } else if (morph instanceof mchorse.blockbuster_pack.morphs.SequencerMorph) {
            mchorse.blockbuster_pack.morphs.SequencerMorph sequencerMorph = (mchorse.blockbuster_pack.morphs.SequencerMorph) morph;
            ticks = (int) sequencerMorph.getDuration();
        }

        if (morph instanceof mchorse.metamorph.bodypart.IBodyPartProvider) {
            mchorse.metamorph.bodypart.BodyPartManager manager = ((mchorse.metamorph.bodypart.IBodyPartProvider) morph).getBodyPart();

            for (mchorse.metamorph.bodypart.BodyPart part : manager.parts) {
                if (!part.morph.isEmpty() && part.limb != null && !part.limb.isEmpty()) {
                    ticks = Math.max(ticks, this.getLength(part.morph.get()));
                }
            }
        }

        return ticks;
    }

}
