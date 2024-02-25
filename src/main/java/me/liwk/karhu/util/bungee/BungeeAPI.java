

/**
 * @ Author: nuym
 * @ Date: 2024/2/25
 * @ Time: 13:56
 */
package me.liwk.karhu.util.bungee;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.util.Collection;
import me.liwk.karhu.Karhu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BungeeAPI {
    public BungeeAPI() {
    }

    public static void sendCommand(String args) {
        sendPluginMessage("karhu:bban", args);
    }

    public static void sendAlert(String msg) {
        sendPluginMessage("karhu:alert", msg);
    }

    public static void sendPluginMessage(String subChannel, String... args) {
        Player messenger = getRandomPlayer();
        if (messenger != null) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(subChannel);
            String[] var4 = args;
            int var5 = args.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                String arg = var4[var6];
                out.writeUTF(arg);
            }

            messenger.sendPluginMessage(Karhu.getInstance().getPlug(), Karhu.getInstance().getBungeeChannel(), out.toByteArray());
        } else {
            Karhu.getInstance().printCool("No player found for " + subChannel + "!");
        }

    }

    public static Player getRandomPlayer() {
        Collection<? extends Player> players = Bukkit.getServer().getOnlinePlayers();
        if (!players.isEmpty()) {
            int i = (int)((double)players.size() * Math.random());
            return ((Player[])players.toArray(new Player[0]))[i];
        } else {
            return null;
        }
    }
}
