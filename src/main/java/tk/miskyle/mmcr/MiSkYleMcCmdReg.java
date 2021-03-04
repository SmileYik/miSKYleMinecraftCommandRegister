package tk.miskyle.mmcr;

import java.text.MessageFormat;
import java.util.HashMap;
import org.bukkit.Bukkit;

public class MiSkYleMcCmdReg {
  private static MiSkYleMcCmdReg mmcr;

  public final String msgNeedPlayer;
  public final String msgNoPermision;
  public final String msgWrongCommand;
  private final HashMap<String, String> cmdInfo;

  /**
   * 初始化.
   * @param msgNeedPlayer 当控制台使用玩家指令时所提示的信息.
   * @param msgNoPermision 当玩家无权限使用某一指令时提示的信息.
   * @param msgWrongCommand 当输入一个错误指令时所提示的信息.
   * @param cmdInfo 为args描述提供本地化.
   */
  public MiSkYleMcCmdReg(String msgNeedPlayer, String msgNoPermision,
                         String msgWrongCommand, HashMap<String, String> cmdInfo) {
    mmcr = this;
    this.msgNeedPlayer = msgNeedPlayer;
    this.msgNoPermision = msgNoPermision;
    this.msgWrongCommand = msgWrongCommand;
    this.cmdInfo = cmdInfo;
  }

  /**
   * 返回默认的MiSkYleMcCmdReg实例.
   * @return 若未通过构造器创建则返回null
   */
  public static MiSkYleMcCmdReg getDefault() {
    if (mmcr == null) {
      mmcr = new MiSkYleMcCmdReg(
          "You Not A Player!",
          "You Have Not Permission!",
          "Command is Wrong!",
          new HashMap<>()
      );
    }
    return mmcr;
  }

  public String tr(String key) {
    return cmdInfo.getOrDefault(key, key);
  }

  public String tr(String key, Object ... objs) {
    return MessageFormat.format(cmdInfo.getOrDefault(key, key), objs);
  }

  /**
   * 注册一个指令.
   * @param cmdClass 指令类
   * @param mainCmd 指令
   * @param aliases 指令别名(用逗号隔开, 包括主要指令
   * @throws InstantiationException 当不能构建指令类实例时抛出.
   * @throws IllegalAccessException 当不能构建指令类实例时抛出.
   */
  public static void registerCommands(
          Class<?> cmdClass, String mainCmd, String aliases)
          throws InstantiationException, IllegalAccessException {
    MiSkYleCommand cmd = new MiSkYleCommand(cmdClass, aliases);
    Bukkit.getPluginCommand(mainCmd).setExecutor(cmd);
    Bukkit.getPluginCommand(mainCmd).setTabCompleter(cmd);
  }
}
