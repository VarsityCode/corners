package corners;


import java.util.ArrayList;
import java.util.List;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.world.blocks.Block;

import com.pragprog.ahmine.ez.EZPlugin;

public class CornerTracker extends EZPlugin {
    public List<Block> corners = new ArrayList<Block>();
    public Player player = null;
    public String errorMessage = null;

    CornerTracker(Player incomingPlayer) {
        player = incomingPlayer;
    }

    public boolean allCornersPlaced() {
        return corners.size() == 4;
    }


    public boolean tryToRegisterCorner(Block potentialCorner) {
        if (corners.isEmpty()) {
            corners.add(potentialCorner);
            player.chat("Corner has been set at " + potentialCorner.getLocation().toString());
            return true;
        } else {
            if (isValidCorner(potentialCorner)) {
                corners.add(potentialCorner);
                player.chat("Corner has been set at " + potentialCorner.getLocation().toString());
                return true;
            } else {
                player.chat(errorMessage);
                return false;
            }
        }
    }

    private boolean cornerTypeSameAsFirstCornerType(Block corner) {
        Block firstCorner = corners.get(0);

        if (firstCorner.getType() == corner.getType()) {
            return true;
        } else {
            errorMessage = "Your corner must be the same type as the first corner placed.";
            return false;
        }
    }

    private boolean cornerYSameAsFirstCornerY(Block corner) {
        Block firstCorner = corners.get(0);

        if (firstCorner.getY() == corner.getY()) {
            return true;
        } else {
            errorMessage = "Your Y cooridnate must be the same as the first corner placed";
            return false;
        }
    }

    private boolean cornerWouldBePartOfValidRectangle(Block potentialCorner) {
        boolean cornerWouldBePartOfValidRectangle = false;
        if (corners.size() < 2) {
            cornerWouldBePartOfValidRectangle = true;
        }

        if (corners.size() == 2) {
            if (twoXCoordinatesWouldBeTheSame(potentialCorner) || twoZCoordinatesWouldBeTheSame(potentialCorner)) {
                cornerWouldBePartOfValidRectangle = true;
            }
        } else if (corners.size() == 3) {
            if (twoXCoordinatesWouldBeTheSame(potentialCorner) && twoZCoordinatesWouldBeTheSame(potentialCorner)) {
                cornerWouldBePartOfValidRectangle = true;
            }
        }

        if (!cornerWouldBePartOfValidRectangle) {
            errorMessage = "Your corners must form a rectangle.";
        }

        return cornerWouldBePartOfValidRectangle;
    }

    private boolean twoXCoordinatesWouldBeTheSame(Block potentialCorner){
        boolean two_x_coordinates_the_same = false;
        for (Block b : corners) {
            if (b.getX() == potentialCorner.getX()) {
                two_x_coordinates_the_same = true;
            }
        }

        return two_x_coordinates_the_same;
    }
    private boolean twoZCoordinatesWouldBeTheSame(Block potentialCorner){
        boolean two_z_coordinates_the_same = false;
        for (Block b : corners) {
            if (b.getZ() == potentialCorner.getZ()) {
                two_z_coordinates_the_same = true;
            }
        }

        return two_z_coordinates_the_same;
    }

  private boolean isValidCorner(Block potentialCorner) {
    return cornerTypeSameAsFirstCornerType(potentialCorner) && cornerYSameAsFirstCornerY(potentialCorner) && cornerWouldBePartOfValidRectangle(potentialCorner);
  }
}
