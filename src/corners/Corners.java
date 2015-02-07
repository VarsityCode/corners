package corners;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import net.canarymod.plugin.Plugin;
import net.canarymod.logger.Logman;
import net.canarymod.Canary;
import net.canarymod.commandsys.*;
import net.canarymod.chat.MessageReceiver;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.world.position.Location;
import net.canarymod.api.world.blocks.Block;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.BlockIterator;
import net.canarymod.hook.HookHandler;
import net.canarymod.hook.player.TeleportHook;
import net.canarymod.hook.player.BlockPlaceHook;
import net.canarymod.hook.player.BlockDestroyHook;
import net.canarymod.plugin.PluginListener;
import com.pragprog.ahmine.ez.EZPlugin;


public class Corners extends EZPlugin implements PluginListener {
  private static HashMap<String, CornerTracker> cornerHashMap =
      new HashMap<String, CornerTracker>();

  @Override
  public boolean enable() {
    Canary.hooks().registerListener(this, this);
    return super.enable();
  }

  @HookHandler
  public void onBlockPlace(BlockPlaceHook event) {
    Player player = event.getPlayer();
    if (cornerHashMap.containsKey(player.getName())) {
        tryToRegisterCorner(event, player);

      if (playerPlacedAllCorners(player)) {
        buildFloorFromCorners(player);
        clearCornerTrackerFor(player);
      }
    }
  }

  @Command(aliases = { "corners" },
            description = "Quickly build a floor by placing 4 corners and have it fill the square of those corners automagically.",
            permissions = { "" },
            toolTip = "/corners")
  public void cornersCommand(MessageReceiver caller, String[] args) {
    if (caller instanceof Player) {
      Player player = (Player)caller;
      cornerHashMap.put(player.getName(), new CornerTracker(player));
      player.chat("Place 3 corners of the same block and the same height");
      player.chat("and a floor will be created to fill the corners.");
    }
  }

  private void clearCornerTrackerFor(Player player) {
    cornerHashMap.remove(player.getName());
  }

  private void buildFloorFromCorners(Player player) {
   List<Block> corners =  cornerHashMap.get(player.getName()).corners;
      Block firstCorner = getFirstCorner(corners);

      for (int x = firstCorner.getX(); x < largestXCoordinate(corners) + 1; x ++) {
          if (largestZCoordinate(corners) > firstCorner.getZ()){
              for (int z = firstCorner.getZ(); z != largestZCoordinate(corners) + 1; z++) {
                  Block iteratorBlock = player.getWorld().getBlockAt(x, firstCorner.getY(), z);
                  player.getWorld().setBlockAt(iteratorBlock.getLocation(), firstCorner.getType());
              }
          } else {
              for (int z = smallestZCoordinate(corners); z != largestZCoordinate(corners) + 1; z++) {
                  Block iteratorBlock = player.getWorld().getBlockAt(x, firstCorner.getY(), z);
                  player.getWorld().setBlockAt(iteratorBlock.getLocation(), firstCorner.getType());
              }
          }
      }
  }

    private int largestXCoordinate(List<Block> corners){
        Block largestCorner = corners.get(0);
        for (int i=0; i < 3; i++) {
            for (Block b : corners) {
                if (b.getX() > largestCorner.getX()) {
                    largestCorner = b;
                }
            }
        }

        return largestCorner.getX();
    }

    private int smallestZCoordinate(List<Block> corners){
        Block smallestCorner = corners.get(0);
        for (int i=0; i < 3; i++) {
            for (Block b : corners) {
                if (b.getZ() < smallestCorner.getZ()) {
                    smallestCorner = b;
                }
            }
        }

        return smallestCorner.getZ();
    }

    private int largestZCoordinate(List<Block> corners){
        Block largestCorner = corners.get(0);
        for (int i=0; i < 3; i++) {
            for (Block b : corners) {
                if (b.getZ() > largestCorner.getZ()) {
                    largestCorner = b;
                }
            }
        }

        return largestCorner.getZ();
    }

    // return the corner with the smallest X
    private Block getFirstCorner(List<Block> corners){
        Block firstCorner = corners.get(0);

        for (int i=0; i < 3; i++) {
            for (Block b : corners) {
                if (b.getX() < firstCorner.getX()) {
                    firstCorner = b;
                }
            }
        }
        return firstCorner;
    }

  private boolean playerPlacedAllCorners(Player player) {
    CornerTracker cornerTracker = cornerHashMap.get(player.getName());
    return cornerTracker.allCornersPlaced();
  }


  private void tryToRegisterCorner(BlockPlaceHook event, Player player) {
    Block blockPlaced = event.getBlockPlaced();
    CornerTracker cornerTracker = cornerHashMap.get(player.getName());
    if (!cornerTracker.tryToRegisterCorner(blockPlaced)) {
      event.setCanceled(); // do not allow block to be placed
    }
  }
}
