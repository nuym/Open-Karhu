package me.liwk.karhu.util.framework;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class CommandManager1_19 {
   private static final Field knownCommandsField;
   private static final CommandMap bukkitCommandMap;
   private static Method syncCommandsMethod;
   protected final JavaPlugin plugin;
   private final Map registered;

   public CommandManager1_19(@NotNull JavaPlugin plugin) {
      if (plugin == null) {
         $$$reportNull$$$0(0);
      }

      super();
      this.registered = new HashMap();
      this.plugin = plugin;
   }

   public static void syncCommand() {
      if (syncCommandsMethod != null) {
         try {
            syncCommandsMethod.invoke(Bukkit.getServer());
         } catch (InvocationTargetException | IllegalAccessException var1) {
            ReflectiveOperationException e = var1;
            Bukkit.getLogger().log(Level.WARNING, "Error when syncing commands", e);
         }

      }
   }

   public static void unregisterFromKnownCommands(@NotNull org.bukkit.command.@NotNull Command command) throws IllegalAccessException {
      if (command == null) {
         $$$reportNull$$$0(1);
      }

      Map knownCommands = (Map)knownCommandsField.get(bukkitCommandMap);
      knownCommands.values().removeIf(command::equals);
      command.unregister(bukkitCommandMap);
   }

   public static void registerCommandToCommandMap(@NotNull String label, @NotNull org.bukkit.command.@NotNull Command command) {
      if (label == null) {
         $$$reportNull$$$0(2);
      }

      if (command == null) {
         $$$reportNull$$$0(3);
      }

      bukkitCommandMap.register(label, command);
   }

   public final void register(@NotNull org.bukkit.command.@NotNull Command command) {
      if (command == null) {
         $$$reportNull$$$0(4);
      }

      String name = command.getLabel();
      if (this.registered.containsKey(name)) {
         this.plugin.getLogger().log(Level.WARNING, "Duplicated \"{0}\" command ! Ignored", name);
      } else {
         registerCommandToCommandMap(this.plugin.getName(), command);
         this.registered.put(name, command);
      }
   }

   public final void unregister(@NotNull org.bukkit.command.@NotNull Command command) {
      if (command == null) {
         $$$reportNull$$$0(5);
      }

      try {
         unregisterFromKnownCommands(command);
         this.registered.remove(command.getLabel());
      } catch (ReflectiveOperationException var3) {
         ReflectiveOperationException e = var3;
         this.plugin.getLogger().log(Level.WARNING, "Something wrong when unregister the command", e);
      }

   }

   public final void unregister(@NotNull String command) {
      if (command == null) {
         $$$reportNull$$$0(6);
      }

      if (this.registered.containsKey(command)) {
         this.unregister((org.bukkit.command.Command)this.registered.remove(command));
      }

   }

   public final void unregisterAll() {
      this.registered.values().forEach((command) -> {
         try {
            unregisterFromKnownCommands(command);
         } catch (ReflectiveOperationException var3) {
            ReflectiveOperationException e = var3;
            this.plugin.getLogger().log(Level.WARNING, "Something wrong when unregister the command", e);
         }

      });
      this.registered.clear();
   }

   public final @NotNull Map getRegistered() {
      Map var10000 = Collections.unmodifiableMap(this.registered);
      if (var10000 == null) {
         $$$reportNull$$$0(7);
      }

      return var10000;
   }

   static {
      try {
         Method commandMapMethod = Bukkit.getServer().getClass().getMethod("getCommandMap");
         bukkitCommandMap = (CommandMap)commandMapMethod.invoke(Bukkit.getServer());
         knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
         knownCommandsField.setAccessible(true);
      } catch (ReflectiveOperationException var6) {
         ReflectiveOperationException e = var6;
         throw new ExceptionInInitializerError(e);
      }

      try {
         Class craftServer = Bukkit.getServer().getClass();
         syncCommandsMethod = craftServer.getDeclaredMethod("syncCommands");
      } catch (Exception var5) {
      } finally {
         if (syncCommandsMethod != null) {
            syncCommandsMethod.setAccessible(true);
         }

      }

   }

   // $FF: synthetic method
   private static void $$$reportNull$$$0(int var0) {
      String var10000;
      switch (var0) {
         case 0:
         case 1:
         case 2:
         case 3:
         case 4:
         case 5:
         case 6:
         default:
            var10000 = "Argument for @NotNull parameter '%s' of %s.%s must not be null";
            break;
         case 7:
            var10000 = "@NotNull method %s.%s must not return null";
      }

      byte var10001;
      switch (var0) {
         case 0:
         case 1:
         case 2:
         case 3:
         case 4:
         case 5:
         case 6:
         default:
            var10001 = 3;
            break;
         case 7:
            var10001 = 2;
      }

      Object[] var2 = new Object[var10001];
      switch (var0) {
         case 0:
         default:
            var2[0] = "plugin";
            break;
         case 1:
         case 3:
         case 4:
         case 5:
         case 6:
            var2[0] = "command";
            break;
         case 2:
            var2[0] = "label";
            break;
         case 7:
            var2[0] = "me/liwk/karhu/util/framework/CommandManager1_19";
      }

      switch (var0) {
         case 0:
         case 1:
         case 2:
         case 3:
         case 4:
         case 5:
         case 6:
         default:
            var2[1] = "me/liwk/karhu/util/framework/CommandManager1_19";
            break;
         case 7:
            var2[1] = "getRegistered";
      }

      switch (var0) {
         case 0:
         default:
            var2[2] = "<init>";
            break;
         case 1:
            var2[2] = "unregisterFromKnownCommands";
            break;
         case 2:
         case 3:
            var2[2] = "registerCommandToCommandMap";
            break;
         case 4:
            var2[2] = "register";
            break;
         case 5:
         case 6:
            var2[2] = "unregister";
         case 7:
      }

      var10000 = String.format(var10000, var2);
      Object var1;
      String var4;
      switch (var0) {
         case 0:
         case 1:
         case 2:
         case 3:
         case 4:
         case 5:
         case 6:
         default:
            IllegalArgumentException var3 = new IllegalArgumentException;
            var4 = var10000;
            var1 = var3;
            var3.<init>(var4);
            break;
         case 7:
            IllegalStateException var10002 = new IllegalStateException;
            var4 = var10000;
            var1 = var10002;
            var10002.<init>(var4);
      }

      throw var1;
   }
}
