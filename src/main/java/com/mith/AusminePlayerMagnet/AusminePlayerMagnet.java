package com.mith.AusminePlayerMagnet;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class AusminePlayerMagnet extends JavaPlugin implements Listener
{
  private double maxDistance;
  private List<Player> magnetPlayers;
  
  public AusminePlayerMagnet()
  {
    this.maxDistance = 8.0D;
    
    this.magnetPlayers = new CopyOnWriteArrayList();
  }
  
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
  {
    if (cmd.getName().equalsIgnoreCase("magnet"))
    {
      if (!(sender instanceof Player))
      {
        sender.sendMessage("Only players can use this command.");
      }
      else
      {
        Player player = (Player)sender;
        if (args.length == 1)
        {
          String arg = args[0].toLowerCase();
          if (arg.equals("on"))
          {
            if (!this.magnetPlayers.contains(player))
            {
              this.magnetPlayers.add(player);
              player.sendMessage("You turned on your magnet.");
            }
            else
            {
              player.sendMessage("Your magnet is already on!");
            }
          }
          else if (arg.equals("off")) {
            if (this.magnetPlayers.contains(player))
            {
              this.magnetPlayers.remove(player);
              player.sendMessage("You turned off your magnet.");
            }
            else
            {
              player.sendMessage("Your magnet is already off!");
            }
          }
        }
        else
        {
          sender.sendMessage("Usage: /magnet <on|off>");
        }
      }
      return true;
    }
    if (cmd.getName().equalsIgnoreCase("demagnet")){
        if (!(sender instanceof Player))
        {
            sender.sendMessage("Only players can use this command.");
        }
        else{

            magnetPlayers.clear();

            return true;
        }

    }
    return false;
  }
  
  public void onEnable()
  {
    saveDefaultConfig();
    
    this.maxDistance = getConfig().getInt("max-distance");
    
    ItemSearch itemSearch = new ItemSearch(null);
    getServer().getScheduler().scheduleSyncRepeatingTask(this, itemSearch, 5L, 5L);
  }

    private class ItemSearch
            implements Runnable
    {
        private ItemSearch() {}

        public ItemSearch(Object o) {
        }

        public void run()
        {
            for (Iterator i$ = AusminePlayerMagnet.this.getServer().getWorlds().iterator(); i$.hasNext();)
            {
                World world = (World)i$.next();
                for (Entity entity : world.getEntities()) {
                    if ((entity instanceof Item))
                    {
                        Item item = (Item)entity;
                        ItemStack stack = item.getItemStack();
                        Location location = item.getLocation();
                        if ((stack.getAmount() > 0) && (!item.isDead()) && (item.getPickupDelay() <= item.getTicksLived()))
                        {
                            Player closestPlayer = null;
                            double distanceSmall = AusminePlayerMagnet.this.maxDistance;
                            for (final Entity player : item.getNearbyEntities(maxDistance, maxDistance, maxDistance)) {
                                if(!(player instanceof Player)){
                                    continue;
                                }
                                if (player != null && player.getWorld().equals(world) && ((Player) player).isOnline() && magnetPlayers.contains(player)) {
                                    final double playerDistance = player.getLocation().distance(location);
                                    if (playerDistance >= distanceSmall) {
                                        continue;
                                    }
                                    distanceSmall = playerDistance;
                                    closestPlayer = (Player)player;
                                }
                            }
                            if (closestPlayer != null) {
                                item.setVelocity(closestPlayer.getLocation().toVector().subtract(item.getLocation().toVector()).normalize());
                            }
                        }
                    }
                }
            }

            World world;
        }
  }


    @EventHandler
    public void onLeave(PlayerQuitEvent e){
      if (magnetPlayers.contains(e.getPlayer())){

          magnetPlayers.remove(e.getPlayer());
      }
    }

}
