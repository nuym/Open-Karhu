package me.liwk.karhu.util.framework;

import org.bukkit.command.CommandException;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class BukkitCommand extends org.bukkit.command.Command {
   private final Plugin owningPlugin;
   private final CommandExecutor executor;

   protected BukkitCommand(String label, CommandExecutor executor, Plugin owner) {
      super(label);
      this.executor = executor;
      this.owningPlugin = owner;
      this.usageMessage = "";
   }

   public boolean execute(CommandSender sender, String commandLabel, String[] args) {
      boolean success = false;
      if (!this.owningPlugin.isEnabled()) {
         return false;
      } else if (!this.testPermission(sender)) {
         return true;
      } else {
         try {
            success = this.executor.onCommand(sender, this, commandLabel, args);
         } catch (Throwable var9) {
            Throwable ex = var9;
            throw new CommandException("Error while executing '" + commandLabel + "' in plugin " + this.owningPlugin.getDescription().getFullName(), ex);
         }

         if (!success && this.usageMessage.length() > 0) {
            String[] var10 = this.usageMessage.replace("<command>", commandLabel).split("\n");
            int var6 = var10.length;

            for(int var7 = 0; var7 < var6; ++var7) {
               String line = var10[var7];
               sender.sendMessage(line);
            }
         }

         return success;
      }
   }
}
