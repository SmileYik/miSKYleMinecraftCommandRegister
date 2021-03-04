package tk.miskyle.mmcr;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

public class MiSkYleCommand implements CommandExecutor, TabExecutor {
  // private RSCommand comm
  protected ArrayList<Method> methods;
  protected Object commands;
  private ArrayList<String[]> subCmds = new ArrayList<>();
  private ArrayList<ArrayList<String>> subCmdsNoTab = new ArrayList<>();
  private String aliases;

  public MiSkYleCommand(Class<?> clazz, String aliases)
          throws IllegalAccessException, InstantiationException {
    initialization(clazz.getDeclaredMethods(), clazz.newInstance(), aliases);
  }

  private void initialization(Method[] temps, Object commands, String aliases) {
    methods = new ArrayList<>();
    subCmds.clear();
    subCmdsNoTab.clear();
    this.aliases = aliases.toLowerCase();

    this.commands = commands;

    ArrayList<ArrayList<String>> temp = new ArrayList<>();
    for (Method method : temps) {
      if (!method.isAnnotationPresent(Cmd.class)) {
        continue;
      }
      methods.add(method);
      Cmd cmd = method.getAnnotation(Cmd.class);
      if (cmd.subCmd().length > 0) {
        int index = 0;
        for (String subCmd : cmd.subCmd()) {
          if (subCmd != null && !subCmd.isEmpty()) {
            if (temp.size() <= index) {
              ArrayList<String> temp2 = new ArrayList<>();
              temp2.add(subCmd);
              temp.add(index, temp2);
            } else {
              ArrayList<String> temp2 = temp.get(index);
              if (!temp2.contains(subCmd)) {
                temp2.add(subCmd);
              }
            }
          }
          if (subCmdsNoTab.size() <= index) {
            subCmdsNoTab.add(index, new ArrayList<>());
          }
          index++;
        }
        subCmdsNoTab.get(index - 1).add(cmd.subCmd()[index - 1].toLowerCase());
      }
    }
    for (int i = 0; i < temp.size(); i++) {
      ArrayList<String> temp2 = temp.get(i);
      int size = temp2.size();
      subCmds.add(i, temp2.toArray(new String[size]));
    }
  }

  @Override
  public boolean onCommand(
          CommandSender sender, Command command, String topCmd, String[] args) {
    if (!(aliases.contains(topCmd.toLowerCase()))) {
      return false;
    }
    if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
      getHelp(sender, topCmd);
      return true;
    }
    for (Method method : methods) {
      Cmd cmd = method.getAnnotation(Cmd.class);
      if (args.length < cmd.args().length
              || !compareSubCommand(args, cmd.subCmd())
              || !cmd.unlimitedLength() && args.length != cmd.args().length) {
        continue;
      }
      if (!cmd.permission().isEmpty() && !sender.hasPermission(cmd.permission())) {
        sender.sendMessage(MiSkYleMcCmdReg.getDefault().msgNoPermision);
        return true;
      }
      if (cmd.needPlayer() && (sender instanceof Player)) {
        try {
          method.invoke(commands, sender, args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
          e.printStackTrace();
        }
      } else if (cmd.needPlayer()) {
        sender.sendMessage(MiSkYleMcCmdReg.getDefault().msgNeedPlayer);
      } else {
        try {
          method.invoke(commands, sender, args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
          e.printStackTrace();
        }
      }
      return true;
    }
    sender.sendMessage(MiSkYleMcCmdReg.getDefault().msgWrongCommand);
    return true;
  }

  @Override
  public List<String> onTabComplete(
          CommandSender commandSender, Command command, String a, String[] args) {
    if (args.length > subCmds.size()) {
      return new ArrayList<>();
    } else if (args.length == 0) {
      return Arrays.asList(subCmds.get(0));
    } else if (args.length >= 2
            && subCmdsNoTab.get(args.length - 2).contains(args[args.length - 2].toLowerCase())) {
      return new ArrayList<>();
    } else {
      return Arrays.stream(subCmds.get(args.length - 1))
              .filter(s -> s.startsWith(args[args.length - 1]))
              .collect(Collectors.toList());
    }
  }

  private void getHelp(CommandSender sender, String top) {
    StringBuilder sb = new StringBuilder();
    sb.append("\n");
    sb.append("&b=============HELP=============\n");
    for (Method method : methods) {
      Cmd cmd = method.getAnnotation(Cmd.class);
      if (!cmd.permission().isEmpty() && !sender.hasPermission(cmd.permission())) {
        continue;
      }
      boolean canSee = cmd.permission().isEmpty()
              || (sender.isOp() || sender.hasPermission(cmd.permission()));
      if (canSee) {
        sb.append("&9/")
                .append(top)
                .append(" ");
        for (String subCmd : cmd.subCmd()) {
          sb.append("&3")
                  .append(subCmd)
                  .append(" ");
        }
        for (int i = cmd.subCmd().length; i < cmd.args().length; i++) {
          sb.append("&2[&a")
                  .append(MiSkYleMcCmdReg.getDefault().tr(cmd.args()[i]))
                  .append("&2] ");
        }
        sb.append("&7- ")
                .append("&b")
                .append(MiSkYleMcCmdReg.getDefault().tr(cmd.des()))
                .append("\n");
      }
    }
    sb.append("==============================");
    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', sb.toString()));
  }

  private static boolean compareSubCommand(String[] args, String[] subCmd) {
    for (int i = 0; i < subCmd.length; i++) {
      if (!subCmd[i].equalsIgnoreCase(args[i])) {
        return false;
      }
    }
    return true;
  }
}
