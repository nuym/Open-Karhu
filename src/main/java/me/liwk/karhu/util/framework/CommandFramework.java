package me.liwk.karhu.util.framework;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import me.kassq.client.ClientPlugin;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.manager.alert.AlertsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.help.GenericCommandHelpTopic;
import org.bukkit.help.HelpTopicComparator;
import org.bukkit.help.IndexHelpTopic;
import org.bukkit.plugin.SimplePluginManager;
import org.spigotmc.SpigotConfig;

public class CommandFramework implements CommandExecutor {
   private Map commandMap = new HashMap();
   private CommandMap map;
   private String newAliases;
   private ClientPlugin plugin;
   private List files = new ArrayList();

   public CommandFramework(ClientPlugin plugin) {
      this.plugin = plugin;
      if (plugin.getServer().getPluginManager() instanceof SimplePluginManager) {
         SimplePluginManager manager = (SimplePluginManager)plugin.getServer().getPluginManager();

         try {
            Field field = SimplePluginManager.class.getDeclaredField("commandMap");
            field.setAccessible(true);
            this.map = (CommandMap)field.get(manager);
         } catch (SecurityException | NoSuchFieldException | IllegalAccessException | IllegalArgumentException var4) {
            Karhu.getInstance().printCool("&b> &cCommandMap not found, couldn't add commands");
         }
      }

   }

   public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
      return this.handleCommand(sender, cmd, label, args);
   }

   public boolean handleCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
      for(int i = args.length; i >= 0; --i) {
         StringBuilder buffer = new StringBuilder();
         buffer.append(label.toLowerCase());

         for(int x = 0; x < i; ++x) {
            buffer.append(".").append(args[x].toLowerCase());
         }

         String cmdLabel = buffer.toString();
         if (this.commandMap.containsKey(cmdLabel)) {
            Method method = (Method)((Map.Entry)this.commandMap.get(cmdLabel)).getKey();
            Object methodObject = ((Map.Entry)this.commandMap.get(cmdLabel)).getValue();
            Command command = (Command)method.getAnnotation(Command.class);
            if (sender instanceof Player) {
               Player player = (Player)sender;
               if (!player.hasPermission("karhu.staff") && !player.isOp() && !AlertsManager.ADMINS.contains(player.getUniqueId())) {
                  if (cmdLabel.equalsIgnoreCase("karhu")) {
                     sender.sendMessage(SpigotConfig.unknownCommandMessage);
                  } else {
                     sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Karhu.getInstance().getConfigManager().getNoPermission()));
                  }

                  return true;
               }
            }

            try {
               method.invoke(methodObject, new CommandArgs(sender, cmd, label, args, cmdLabel.split("\\.").length - 1));
            } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException var12) {
            }

            return true;
         }
      }

      this.defaultCommand(new CommandArgs(sender, cmd, label, args, 0));
      return true;
   }

   public void registerCommands(Object obj) {
      Method[] var2 = obj.getClass().getMethods();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Method m = var2[var4];
         if (m.getAnnotation(Command.class) != null) {
            Command command = (Command)m.getAnnotation(Command.class);
            if (m.getParameterTypes().length <= 1 && m.getParameterTypes()[0] == CommandArgs.class) {
               String commandName = command.name();
               if (commandName.equalsIgnoreCase("karhu") && Karhu.getInstance().getConfigManager().getName().equalsIgnoreCase("vengeance")) {
                  commandName = "vengeance";
               }

               this.registerCommand(command, commandName, m, obj);
               if (Karhu.getInstance().getConfigManager().getName().equalsIgnoreCase("karhu") || !command.name().equalsIgnoreCase("karhu")) {
                  return;
               }

               if (command.aliases().length < 1) {
                  this.newAliases = Karhu.getInstance().getConfigManager().getName().toLowerCase();
               }

               this.registerCommand(command, this.newAliases, m, obj);
            }
         }
      }

   }

   public void registerHelp() {
      Set help = new TreeSet(HelpTopicComparator.helpTopicComparatorInstance());
      this.commandMap.keySet().stream().filter((s) -> {
         return !s.contains(".");
      }).map((s) -> {
         return this.map.getCommand(s);
      }).map((cmd) -> {
         return new GenericCommandHelpTopic(cmd);
      }).forEachOrdered((topicx) -> {
         help.add(topicx);
      });
      IndexHelpTopic topic = new IndexHelpTopic(this.plugin.getName(), "All commands for " + this.plugin.getName(), (String)null, help, "Below is a list of all " + this.plugin.getName() + " commands:");
      Bukkit.getServer().getHelpMap().addTopic(topic);
   }

   public void unregisterCommands(Object obj) {
      Method[] var2 = obj.getClass().getMethods();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Method m = var2[var4];
         if (m.getAnnotation(Command.class) != null) {
            Command command = (Command)m.getAnnotation(Command.class);
            this.commandMap.remove(command.name().toLowerCase());
            this.commandMap.remove(this.plugin.getName() + ":" + command.name().toLowerCase());
            this.map.getCommand(command.name().toLowerCase()).unregister(this.map);
         }
      }

   }

   public void registerCommand(Command command, String label, Method m, Object obj) {
      this.commandMap.put(label.toLowerCase(), new AbstractMap.SimpleEntry(m, obj));
      this.commandMap.put(this.plugin.getName() + ':' + label.toLowerCase(), new AbstractMap.SimpleEntry(m, obj));
      String cmdLabel = label.replace(".", ",").split(",")[0].toLowerCase();
      if (this.map.getCommand(cmdLabel) == null) {
         org.bukkit.command.Command cmd = new BukkitCommand(cmdLabel, this, this.plugin);
         this.map.register(this.plugin.getName(), cmd);
      }

      if (!command.description().isEmpty() && cmdLabel.equals(label)) {
         this.map.getCommand(cmdLabel).setDescription(command.description());
      }

      if (!command.usage().isEmpty() && cmdLabel.equals(label)) {
         this.map.getCommand(cmdLabel).setUsage(command.usage());
      }

      CommandManager1_19.syncCommand();
   }

   private void defaultCommand(CommandArgs args) {
      args.getSender().sendMessage("Unknown Command.");
   }

   boolean deleteDirectory(File directoryToBeDeleted) {
      File[] allContents = directoryToBeDeleted.listFiles();
      if (allContents != null) {
         File[] var3 = allContents;
         int var4 = allContents.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            File file = var3[var5];
            this.deleteDirectory(file);
         }
      }

      return directoryToBeDeleted.delete();
   }
}
