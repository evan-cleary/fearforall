package com.division.fearforall.config;

import com.division.fearforall.core.FearForAll;
import com.division.fearforall.regions.HealRegion;
import com.division.fearforall.regions.Region;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.BlockVector;

/**
 *
 * @author Evan
 */
public class FFAConfig {

    YamlConfiguration ffaconfig = new YamlConfiguration();
    File configloc;
    FearForAll FFA;
    private static ArrayList<Location> spawns = new ArrayList<Location>();
    private static String mysqlhost;
    private static String mysqlport;
    private static String mysqluser;
    private static String mysqlpass;
    private static String mysqldata;
    private boolean changed = false;

    public FFAConfig(FearForAll instance) {
        this.FFA = instance;
        configloc = new File(instance.getDataFolder() + "/config.yml");
        if (!configloc.exists()) {
            try {
                configloc.createNewFile();
            } catch (IOException ex) {
            }
        }
    }

    public void load() {
        spawns.clear();
        System.out.println("[FearForAll] loading config...");
        try {
            ffaconfig.load(configloc);
        } catch (Exception ex) {
            System.out.println("[FearForAll] generation config...");
        }
        if (ffaconfig.contains("spawn")) {
            Set<String> spawnList;
            spawnList = ffaconfig.getConfigurationSection("spawn").getKeys(false);
            for (String s : spawnList) {
                World world = Bukkit.getServer().getWorld(ffaconfig.getString("spawn." + s + ".world", "world"));
                double x = ffaconfig.getDouble("spawn." + s + ".x");
                double y = ffaconfig.getDouble("spawn." + s + ".y");
                double z = ffaconfig.getDouble("spawn." + s + ".z");
                float yaw = (float) ffaconfig.getDouble("spawn." + s + ".yaw");
                float pitch = (float) ffaconfig.getDouble("spawn." + s + ".pitch");
                spawns.add(new Location(world, x, y, z, yaw, pitch));
            }
        }
        if (!ffaconfig.contains("mysql.host")) {
            ffaconfig.set("mysql.host", "localhost");
            changed = true;
        } else {
            mysqlhost = ffaconfig.getString("mysql.host");
        }
        if (!ffaconfig.contains("mysql.port")) {
            ffaconfig.set("mysql.port", "3306");
            changed = true;
        } else {
            mysqlport = ffaconfig.getString("mysql.port");
        }
        if (!ffaconfig.contains("mysql.username")) {
            ffaconfig.set("mysql.username", "root");
            changed = true;
        } else {
            mysqluser = ffaconfig.getString("mysql.username");
        }
        if (!ffaconfig.contains("mysql.password")) {
            ffaconfig.set("mysql.password", "password");
            changed = true;
        } else {
            mysqlpass = ffaconfig.getString("mysql.password");
        }
        if (!ffaconfig.contains("mysql.database")) {
            ffaconfig.set("mysql.database", "ffa");
            changed = true;
        } else {
            mysqldata = ffaconfig.getString("mysql.database");
        }
        if (changed) {
            try {
                ffaconfig.save(configloc);
                changed = false;
            } catch (IOException ex) {
            }
        } else {
            System.out.println("[FearForAll] Done loading config!");
        }
    }

    public void setSpawn(String rname, Location loc) {
        String name = rname.toLowerCase();
        ffaconfig.set("spawn." + name + ".world", loc.getWorld().getName());
        ffaconfig.set("spawn." + name + ".x", loc.getX());
        ffaconfig.set("spawn." + name + ".y", loc.getY());
        ffaconfig.set("spawn." + name + ".z", loc.getZ());
        ffaconfig.set("spawn." + name + ".yaw", loc.getYaw());
        ffaconfig.set("spawn." + name + ".pitch", loc.getPitch());
        try {
            ffaconfig.save(configloc);
        } catch (IOException ex) {
        }
        spawns.add(loc);
    }

    public void setRegion(World world, BlockVector p1, BlockVector p2) {
        ffaconfig.set("region.world", world.getName());
        ffaconfig.set("region.p1.x", p1.getX());
        ffaconfig.set("region.p1.y", 0);
        ffaconfig.set("region.p1.z", p1.getZ());
        ffaconfig.set("region.p2.x", p2.getX());
        ffaconfig.set("region.p2.y", 256);
        ffaconfig.set("region.p2.z", p2.getZ());
        try {
            ffaconfig.save(configloc);
        } catch (IOException ex) {
        }
    }

    public void setHealRegion(World world, BlockVector p1, BlockVector p2) {
        ffaconfig.set("healregion.world", world.getName());
        ffaconfig.set("healregion.p1.x", p1.getX());
        ffaconfig.set("healregion.p1.y", 0);
        ffaconfig.set("healregion.p1.z", p1.getZ());
        ffaconfig.set("healregion.p2.x", p2.getX());
        ffaconfig.set("healregion.p2.y", 256);
        ffaconfig.set("healregion.p2.z", p2.getZ());
        try {
            ffaconfig.save(configloc);
        } catch (IOException ex) {
        }
    }

    public HealRegion getHealRegion() {
        World world = Bukkit.getServer().getWorld(ffaconfig.getString("healregion.world", "world"));
        double p1x = ffaconfig.getDouble("healregion.p1.x");
        double p1y = ffaconfig.getDouble("healregion.p1.y");
        double p1z = ffaconfig.getDouble("healregion.p1.z");
        double p2x = ffaconfig.getDouble("healregion.p2.x");
        double p2y = ffaconfig.getDouble("healregion.p2.y");
        double p2z = ffaconfig.getDouble("healregion.p2.z");
        BlockVector p1 = new BlockVector(p1x, p1y, p1z);
        BlockVector p2 = new BlockVector(p2x, p2y, p2z);
        return new HealRegion(world, p1, p2, FFA);
    }

    public Region getRegion() {
        World world = Bukkit.getWorld(ffaconfig.getString("region.world", "world"));
        double p1x = ffaconfig.getDouble("region.p1.x");
        double p1y = ffaconfig.getDouble("region.p1.y");
        double p1z = ffaconfig.getDouble("region.p1.z");
        double p2x = ffaconfig.getDouble("region.p2.x");
        double p2y = ffaconfig.getDouble("region.p2.y");
        double p2z = ffaconfig.getDouble("region.p2.z");
        BlockVector p1 = new BlockVector(p1x, p1y, p1z);
        BlockVector p2 = new BlockVector(p2x, p2y, p2z);
        return new Region(world, p1, p2);
    }

    public ArrayList<Location> getSpawns() {
        return spawns;
    }

    public Set<String> getSpawnNames() {
        return ffaconfig.getConfigurationSection("spawn").getKeys(false);
    }

    public boolean removeSpawn(String name) {
        if (getSpawnNames().contains(name.toLowerCase())) {
            ffaconfig.set("spawn." + name.toLowerCase(), null);
            try {
                ffaconfig.save(configloc);
            } catch (IOException ex) {
            }
            return true;
        }
        return false;
    }

    public String getMySQLDatabase() {
        return mysqldata;
    }

    public String getMySQLPassword() {
        return mysqlpass;
    }

    public String getMySQLUsername() {
        return mysqluser;
    }

    public String getMySQLHost() {
        return mysqlhost;
    }

    public String getMySQLPort() {
        return mysqlport;
    }
}
