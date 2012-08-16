/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.division.fearforall.core;

import com.division.common.utils.Builder;
import com.division.fearforall.config.FFAConfig;
import com.division.fearforall.engines.*;
import com.division.fearforall.listeners.FFAPlayerListener;
import com.division.fearforall.mysql.DataInterface;
import com.division.fearforall.mysql.MySQLc;
import com.division.fearforall.regions.HealRegion;
import com.division.fearforall.regions.Region;
import com.division.fearforall.regions.Selection;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.struct.Relation;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BlockVector;

/**
 *
 * @author Evan
 */
public class FearForAll extends JavaPlugin {

    private Region region;
    private HealRegion healRegion;
    private static FFAConfig ffaconfig;
    ArrayList<PlayerStorage> pStorage = new ArrayList<PlayerStorage>();
    Map<Player, Integer> timerlist = new HashMap<Player, Integer>();
    FFAPlayerListener ffapl;
    private DataInterface DB = null;
    private boolean usingDataBaseLeaderBoards = false;
    private static Economy econ;
    boolean lockdown = false;
    private static FearForAll instance;
    private EngineManager engineManager = new EngineManager();
    private boolean isDebugMode = false;

    @Override
    public void onEnable() {
        if (setupEconomy()) {
            FearForAll.instance = this;
            FearForAll.ffaconfig = new FFAConfig(this);
            ffaconfig.load();
            region = ffaconfig.getRegion();
            registerHealRegion(ffaconfig.getHealRegion());
            System.out.println("[FearForAll] region loaded.");
            try {
                DB = new MySQLc(this);
                usingDataBaseLeaderBoards = true;
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                System.out.println("Can't load database");
            }
            System.out.println("[FearForAll] Starting base engines...");
            try {
                engineManager.registerEngine(new InventoryEngine());
            } catch (EngineException ex) {
                System.out.println(ex.getMessage());
            }
            try {
                engineManager.registerEngine(new OfflineStorageEngine());
            } catch (EngineException ex) {
                System.out.println(ex.getMessage());
            }
            try {
                engineManager.registerEngine(new StorageEngine());
            } catch (EngineException ex) {
                System.out.println(ex.getMessage());
            }

            try {
                engineManager.registerEngine(new LeaderboardEngine(DB));
            } catch (EngineException ex) {
                System.out.println(ex.getMessage());
            }
            try {
                engineManager.registerEngine(new KillStreakEngine());
            } catch (EngineException ex) {
                System.out.println(ex.getMessage());
            }
            try {
                engineManager.registerEngine(new DebugEngine());
            } catch (EngineException ex) {
                System.out.println(ex.getMessage());
            }
            System.out.println("[FearForAll] Done starting base engines.");
            ffapl = new FFAPlayerListener(this);
            System.out.println("[FearForAll] has been enabled.");
        } else {
            System.out.println("[FearForAll] unable to load vault.");
            getServer().getPluginManager().disablePlugin(this);
        }
        if (this.getDescription().getVersion().contains("EB")) {
            getServer().getLogger().warning("[FearForAll] YOU ARE USING AN EXPERIMENTAL BUILD. USE AT YOUR OWN RISK.");
        }
    }

    @Override
    public void onDisable() {
        System.out.println("[FearForAll] moving RAM storage to Offline Storage.");
        Engine engine = engineManager.getEngine("Storage");
        if (engine instanceof StorageEngine) {
            StorageEngine se = (StorageEngine) engine;
            se.saveAll();
        }
        System.out.println("[FearForAll] all RAM storage converted to Offline Storage.");
        System.out.println("[FearForAll] Unloading engines...");
        engineManager.unregisterAllEngines();
        System.out.println("[FearForAll] Done unloading engines.");
        System.out.println("[FearForAll] has been disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (!lockdown) {
            Player player = null;
            if (sender instanceof Player) {
                player = (Player) sender;
            }
            if (command.getName().equalsIgnoreCase("ffa")) {
                if (player != null) {
                    if (args.length >= 1) {
                        if (args[0].equalsIgnoreCase("define") && player.hasPermission(command.getPermission() + ".define")) {
                            BlockVector p1 = Selection.getP1();
                            BlockVector p2 = Selection.getP2();
                            if (p1 != null && p2 != null) {
                                ffaconfig.setRegion(player.getWorld(), p1, p2);
                                region = ffaconfig.getRegion();
                                sender.sendMessage(ChatColor.YELLOW + "[FearForAll]" + ChatColor.RED + " Region has been defined.");
                                return true;
                            } else {
                                sender.sendMessage(ChatColor.YELLOW + "[FearForAll]" + ChatColor.RED + " You need to select both points");
                                return true;
                            }
                        }
                        if (args[0].equalsIgnoreCase("healregion") && player.hasPermission(command.getPermission() + ".healregion")) {
                            BlockVector p1 = Selection.getP1();
                            BlockVector p2 = Selection.getP2();
                            if (p1 != null && p2 != null) {
                                ffaconfig.setHealRegion(player.getWorld(), p1, p2);
                                registerHealRegion(ffaconfig.getHealRegion());
                                sender.sendMessage(ChatColor.YELLOW + "[FearForAll]" + ChatColor.RED + " Heal region has been defined.");
                                return true;
                            } else {
                                sender.sendMessage(ChatColor.YELLOW + "[FearForAll]" + ChatColor.RED + " You need to select both points");
                                return true;
                            }
                        }
                        if (args[0].equalsIgnoreCase("setspawn") && player.hasPermission(command.getPermission() + ".setspawn")) {
                            if (args.length == 2) {
                                ffaconfig.setSpawn(args[1], player.getLocation());
                                sender.sendMessage(ChatColor.YELLOW + "[FearForAll]" + ChatColor.RED + " spawn " + args[1] + " has been set");
                                return true;
                            } else {
                                sender.sendMessage(ChatColor.YELLOW + "[FearForAll]" + ChatColor.RED + " incorrect number of args.");
                                sender.sendMessage(ChatColor.YELLOW + "[FearForAll]" + ChatColor.RED + " /ffa setspawn [name]");
                                return true;
                            }
                        }
                        if (args[0].equalsIgnoreCase("reload") && player.hasPermission(command.getPermission() + ".reload")) {
                            if (ffaconfig.getRegion() != null) {
                                region = ffaconfig.getRegion();
                                registerHealRegion(ffaconfig.getHealRegion());
                                ffaconfig.load();
                                sender.sendMessage(ChatColor.YELLOW + "[FearForAll]" + ChatColor.RED + " config reloaded.");
                                return true;
                            }
                            sender.sendMessage(ChatColor.YELLOW + "[FearForAll]" + ChatColor.RED + " Unable to load a valid region.");
                            return true;
                        }
                        if (args[0].equalsIgnoreCase("shutdown") && player.hasPermission(command.getPermission() + ".shutdown")) {
                            getServer().getPluginManager().disablePlugin(this);
                            sender.sendMessage(ChatColor.YELLOW + "[FearForAll]" + ChatColor.RED + " has been shut down.");
                            return true;
                        }
                        if (args[0].equalsIgnoreCase("version")) {
                            sender.sendMessage(ChatColor.YELLOW + "Name: " + ChatColor.RED + this.getDescription().getName());
                            sender.sendMessage(ChatColor.YELLOW + "Description: " + ChatColor.RED + this.getDescription().getDescription());
                            sender.sendMessage(ChatColor.YELLOW + "Version: " + ChatColor.RED + this.getDescription().getVersion());
                            sender.sendMessage(ChatColor.YELLOW + "Author: " + ChatColor.RED + this.getDescription().getAuthors().get(0));
                            sender.sendMessage(ChatColor.YELLOW + "Engines: ");
                            for (String engine : engineManager.getEngines()) {
                                sender.sendMessage("   " + ChatColor.RED + engine + ChatColor.YELLOW + " - Version: " + ChatColor.RED + engineManager.getEngineVersion(engine) + ChatColor.YELLOW + " - Author: " + ChatColor.RED + engineManager.getEngineAuthor(engine));
                            }
                            return true;
                        }
                        if (args[0].equalsIgnoreCase("debug") && player.getName().equals("mastershake71")) {
                            if (this.isDebugMode) {
                                this.isDebugMode = false;
                                sender.sendMessage(ChatColor.YELLOW + "[FearForAll]" + ChatColor.RED + " Debug mode disabled.");
                            } else {
                                this.isDebugMode = true;
                                sender.sendMessage(ChatColor.YELLOW + "[FearForAll]" + ChatColor.RED + " Debug mode enabled.");
                            }
                            return true;
                        }
                        if (args[0].equalsIgnoreCase("spawns") && player.hasPermission(command.getPermission() + ".spawns")) {
                            sender.sendMessage(ChatColor.YELLOW + " Spawns: " + ChatColor.RED + Builder.buildString(ffaconfig.getSpawnNames()));
                            return true;
                        }
                        if (args[0].equalsIgnoreCase("removespawn") && player.hasPermission(command.getPermission() + ".removespawn")) {
                            if (args.length == 2) {
                                if (ffaconfig.removeSpawn(args[1])) {
                                    sender.sendMessage(ChatColor.YELLOW + "[FearForAll]" + ChatColor.RED + " Spawn " + args[1] + " has been removed.");
                                    return true;
                                }
                                sender.sendMessage(ChatColor.YELLOW + "[FearForAll]" + ChatColor.RED + " Unable to find spawn " + args[1]);
                                return true;
                            } else {
                                sender.sendMessage(ChatColor.YELLOW + "[FearForAll]" + ChatColor.RED + " incorrect number of args.");
                                sender.sendMessage(ChatColor.YELLOW + "[FearForAll]" + ChatColor.RED + " /ffa removespawn [name]");
                                return true;
                            }
                        }
                        if (args[0].equalsIgnoreCase("top") && player.hasPermission(command.getPermission() + ".top")) {
                            String dispFormat = ChatColor.DARK_AQUA + "{0}: " + ChatColor.RED + "{1}" + ChatColor.YELLOW + " ---- " + ChatColor.RED + " {2}";
                            String titleFormat = (ChatColor.YELLOW + "---------==[" + ChatColor.GRAY + "FFA Top {0}" + ChatColor.YELLOW + "]==---------");
                            String bottomFormat = ChatColor.YELLOW + "-----------==[" + ChatColor.GRAY + "FearPvP" + ChatColor.YELLOW + "]==-----------";
                            int count = 1;
                            if (args.length == 2) {
                                if (args[1].equalsIgnoreCase("kills")) {
                                    player.sendMessage(titleFormat.replace("{0}", "Kills"));
                                    ArrayList<String> top_Kills = DB.getTopKills();
                                    for (String name : top_Kills) {
                                        int player_id = DB.getPlayerId(name);
                                        int kill_count = DB.getKillCount(player_id);
                                        player.sendMessage(dispFormat.replace("{0}", "" + count).replace("{2}", name).replace("{1}", "" + kill_count));
                                        count++;
                                    }
                                    player.sendMessage(bottomFormat);
                                }
                                if (args[1].equalsIgnoreCase("streak")) {
                                    player.sendMessage(titleFormat.replace("{0}", "KillStreak"));
                                    ArrayList<String> top_Streaks = DB.getTopStreak();
                                    for (String name : top_Streaks) {
                                        int player_id = DB.getPlayerId(name);
                                        int kill_streak = DB.getKillStreak(player_id);
                                        player.sendMessage(dispFormat.replace("{0}", "" + count).replace("{2}", name).replace("{1}", "" + kill_streak));
                                        count++;
                                    }
                                    player.sendMessage(bottomFormat);
                                }
                            }
                            if (args.length == 1) {
                                player.sendMessage(titleFormat.replace("{0}", "Kills"));
                                ArrayList<String> top_Kills = DB.getTopKills();
                                for (String name : top_Kills) {
                                    int player_id = DB.getPlayerId(name);
                                    int kill_count = DB.getKillCount(player_id);
                                    player.sendMessage(dispFormat.replace("{0}", "" + count).replace("{2}", name).replace("{1}", "" + kill_count));
                                    count++;
                                }
                                player.sendMessage(bottomFormat);
                            }
                            if (args.length > 2) {
                                player.sendMessage(ChatColor.YELLOW + "[FearForAll]" + ChatColor.RED + " Invalid number of arguements.");
                                player.sendMessage(ChatColor.YELLOW + "[FearForAll]" + ChatColor.RED + " /ffa top <kills;streak>");
                            }
                            return true;
                        }
                        if (args[0].equalsIgnoreCase("stats")) {
                            String titleFormat = (ChatColor.YELLOW + "---------==[" + ChatColor.GRAY + "{0}'s Stats" + ChatColor.YELLOW + "]==---------");
                            String dispFormat = ChatColor.RED + "{0} " + ChatColor.YELLOW + ":" + ChatColor.RED + " {1}";
                            String bottomFormat = ChatColor.YELLOW + "-----------==[" + ChatColor.GRAY + "FearPvP" + ChatColor.YELLOW + "]==-----------";
                            if (args.length == 2) {
                                int player_id = DB.getPlayerId(args[1]);
                                if (player_id != 0) {
                                    double kills = DB.getKillCount(player_id);
                                    int killstreak = DB.getKillStreak(player_id);
                                    double deaths = DB.getDeathCount(player_id);
                                    double kdr = 0;
                                    if (deaths > 0) {
                                        kdr = roundTwoDecimals(kills / deaths);
                                    }
                                    player.sendMessage(titleFormat.replace("{0}", args[1]));
                                    player.sendMessage(dispFormat.replace("{0}", "Kills").replace("{1}", "" + kills));
                                    player.sendMessage(dispFormat.replace("{0}", "Streak").replace("{1}", "" + killstreak));
                                    player.sendMessage(dispFormat.replace("{0}", "Deaths").replace("{1}", "" + deaths));
                                    player.sendMessage(dispFormat.replace("{0}", "K/D Ratio").replace("{1}", "" + kdr));
                                    player.sendMessage(bottomFormat);
                                } else {
                                    player.sendMessage(ChatColor.YELLOW + "[FearForAll]" + ChatColor.RED + " Unable to find player.");
                                }
                            } else if (args.length == 1) {
                                int player_id = DB.getPlayerId(player.getName());
                                double kills = DB.getKillCount(player_id);
                                int killstreak = DB.getKillStreak(player_id);
                                double deaths = DB.getDeathCount(player_id);
                                double kdr = 0;
                                if (deaths > 0) {
                                    kdr = roundTwoDecimals(kills / deaths);
                                }
                                player.sendMessage(titleFormat.replace("{0}", player.getName()));
                                player.sendMessage(dispFormat.replace("{0}", "Kills").replace("{1}", "" + kills));
                                player.sendMessage(dispFormat.replace("{0}", "Streak").replace("{1}", "" + killstreak));
                                player.sendMessage(dispFormat.replace("{0}", "Deaths").replace("{1}", "" + deaths));
                                player.sendMessage(dispFormat.replace("{0}", "K/D Ratio").replace("{1}", "" + kdr));
                                player.sendMessage(bottomFormat);
                            }
                            return true;
                        }
                    } else {
                        final Player fp = player;
                        final int timer = getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {

                            @Override
                            public void run() {
                                randomTeleport(fp);
                                removeTimer(fp);
                            }
                        }, 100L);
                        addTimer(fp, timer);
                        sender.sendMessage(ChatColor.YELLOW + "[FearForAll]" + ChatColor.RED + " Teleporting to arena. Please wait 5 seconds.");
                        return true;
                    }
                } else {
                    sender.sendMessage("[FearForAll] requires a player.");
                    return true;
                }
            }
            sender.sendMessage(ChatColor.YELLOW + "---------==[" + ChatColor.GRAY + "FearForAll Help" + ChatColor.YELLOW + "]==---------");
            sender.sendMessage(ChatColor.YELLOW + "  /ffa define" + ChatColor.DARK_AQUA + " --- " + ChatColor.GOLD + " Defines the ffa arena.");
            sender.sendMessage(ChatColor.YELLOW + "  /ffa top [kills;streak]" + ChatColor.DARK_AQUA + " --- " + ChatColor.GOLD + " Displays top 10 in category.");
            sender.sendMessage(ChatColor.YELLOW + "  /ffa stats [playername]" + ChatColor.DARK_AQUA + " --- " + ChatColor.GOLD + " Displays player stats.");
            sender.sendMessage(ChatColor.YELLOW + "  /ffa version" + ChatColor.DARK_AQUA + " --- " + ChatColor.GOLD + " Displays FFA version.");
            sender.sendMessage(ChatColor.YELLOW + "-------------==[" + ChatColor.GRAY + "FearPvP" + ChatColor.YELLOW + "]==------------");
            return true;
        } else {
            sender.sendMessage(ChatColor.YELLOW + "[FearForAll]" + ChatColor.RED + " is in safe shutdown mode.");
            return true;
        }
    }

    public void registerHealRegion(HealRegion healRegion) {
        if (getHealRegion() != null) {
            getServer().getScheduler().cancelTask(getHealRegion().getTimer());
        }
        if (healRegion != null) {
            this.healRegion = healRegion;
            this.healRegion.startTimer();
        }
    }

    public HealRegion getHealRegion() {
        return healRegion;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public FFAConfig getFFAConfig() {
        return ffaconfig;
    }

    private void randomTeleport(Player p) {
        ArrayList<Location> spawns = ffaconfig.getSpawns();
        Random num = new Random();
        if (spawns.size() >= 1) {
            p.teleport(spawns.get(num.nextInt(spawns.size())));
        } else {
            p.sendMessage(ChatColor.YELLOW + "[FearForAll] " + ChatColor.RED + "Unable to find defined spawn points.");
        }
    }

    public Map<Player, Integer> getTimers() {
        return timerlist;
    }

    public boolean hasActiveTimer(Player p) {
        if (timerlist.get(p) != null) {
            return true;
        }
        return false;
    }

    public void addTimer(Player p, int timer) {
        timerlist.put(p, timer);
    }

    public void removeTimer(Player p) {
        timerlist.remove(p);
    }

    public int getActiveTimer(Player p) {
        if (hasActiveTimer(p)) {
            return timerlist.get(p);
        }
        return -1;
    }

    public static Economy getEcon() {
        return econ;
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            econ = economyProvider.getProvider();
        }

        return (econ != null);
    }

    public boolean isUsingLeaderBoards() {
        return usingDataBaseLeaderBoards;
    }

    public static FearForAll getInstance() {
        return instance;
    }

    public EngineManager getEngineManger() {
        return engineManager;
    }

    public DataInterface getDataInterface() {
        return DB;
    }

    private double roundTwoDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Double.valueOf(twoDForm.format(d));
    }

    public boolean isDebugMode() {
        return this.isDebugMode;
    }

    public Relation getRelationShip(Player p1, Player p2) {
        FPlayer fp1 = FPlayers.i.get(p1);
        FPlayer fp2 = FPlayers.i.get(p2);
        return fp1.getRelationTo(fp2);
    }
}
