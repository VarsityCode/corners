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
      player.chat("Place 4 corners of the same block and the same height");
      player.chat("and a floor will be created to fill the corners.");
    }
  }

  private void clearCornerTrackerFor(Player player) {
    cornerHashMap.remove(player.getName());
  }

  private void buildFloorFromCorners(Player player) {
   List<Block> corners =  cornerHashMap.get(player.getName()).corners;
      Block firstCorner = corners.get(0);
      Block oppositeXCorner = oppositeXCorner(corners, firstCorner);
      Block oppositeZCorner = oppositeZCorner(corners, firstCorner);
      // unless firstCorner has the smallest Z value, switch firstCorner with oppositeCorner
      if (firstCorner.getZ() > oppositeXCorner.getZ()) {
          Block tempCorner = firstCorner;
          firstCorner = oppositeXCorner;
          oppositeXCorner = tempCorner;
      }

      for (int x = firstCorner.getX(); x == oppositeZCorner.getX(); x ++) {
          for (int z = firstCorner.getZ(); z == oppositeXCorner.getZ(); z++) {
              Block iteratorBlock = player.getWorld().getBlockAt(x, firstCorner.getY(), z);
              iteratorBlock.setType(firstCorner.getType());
          }
      }
  }

  private boolean playerPlacedAllCorners(Player player) {
    CornerTracker cornerTracker = cornerHashMap.get(player.getName());
    return cornerTracker.allCornersPlaced();
  }

    private Block oppositeXCorner(List<Block> corners, Block startingBlock) {
        Block oppositeCorner = null;

        for (Block corner: corners) {
            if(corner.getX() == startingBlock.getX()) {
                oppositeCorner = corner;
            }
        }
        return oppositeCorner;
    }

    private Block oppositeZCorner(List<Block> corners, Block startingBlock) {
        Block oppositeCorner = null;

        for (Block corner: corners) {
            if(corner.getZ() == startingBlock.getZ()) {
                oppositeCorner = corner;
            }
        }
        return oppositeCorner;
    }

  private void tryToRegisterCorner(BlockPlaceHook event, Player player) {
    Block blockPlaced = event.getBlockPlaced();
    CornerTracker cornerTracker = cornerHashMap.get(player.getName());
    if (!cornerTracker.tryToRegisterCorner(blockPlaced)) {
      event.setCanceled(); // do not allow block to be placed
    }
  }
}
