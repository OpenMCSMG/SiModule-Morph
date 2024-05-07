package cn.cyanbukkit.bukkit.addon;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CM extends Command {
    public CM(String name) {
        super(name);
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        // 玩家名字 模型名字 NBT
        if (strings.length < 2) {
            commandSender.sendMessage("§c/playmorph <player> <morph> [nbt]");
            return true;
        }
        PlayMorph.instance.init(strings, Bukkit.getPlayer(strings[0]) );
        return true;
    }
}
