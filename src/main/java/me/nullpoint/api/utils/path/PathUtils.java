package me.nullpoint.api.utils.path;

import me.nullpoint.api.utils.world.BlockPosX;
import me.nullpoint.api.utils.Wrapper;
import me.nullpoint.mod.modules.impl.client.CombatSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Iterator;

public class PathUtils implements Wrapper {
   private static boolean canPassThrough(BlockPos pos) {
      Block block = mc.world.getBlockState(new BlockPos(pos.getX(), pos.getY(), pos.getZ())).getBlock();
      return block == Blocks.AIR || block instanceof PlantBlock || block == Blocks.VINE || block == Blocks.LADDER || block == Blocks.WATER || block == Blocks.WATER_CAULDRON || block instanceof WallSignBlock;
   }

   public static ArrayList<Vec3> computePath(LivingEntity fromEntity, LivingEntity toEntity) {
      return computePath(new Vec3(fromEntity.getX(), fromEntity.getY(), fromEntity.getZ()), new Vec3(toEntity.getX(), toEntity.getY(), toEntity.getZ()));
   }

   public static ArrayList<Vec3> computePath(Vec3d vec3d) {
      return computePath(new Vec3(mc.player.getX(), mc.player.getY(), mc.player.getZ()), new Vec3(vec3d.x, vec3d.y, vec3d.z));
   }

   public static ArrayList<Vec3> computePath(Vec3 topFrom, Vec3 to) {
      if (!canPassThrough(new BlockPosX(topFrom.mc()))) {
         topFrom = topFrom.addVector(0.0, 1.0, 0.0);
      }

      AStarCustomPathFinder pathfinder = new AStarCustomPathFinder(topFrom, to);
      pathfinder.compute();
      int i = 0;
      Vec3 lastLoc = null;
      Vec3 lastDashLoc = null;
      ArrayList<Vec3> path = new ArrayList<>();
      ArrayList<Vec3> pathFinderPath = pathfinder.getPath();

      for(Iterator<Vec3> var8 = pathFinderPath.iterator(); var8.hasNext(); ++i) {
         Vec3 pathElm = var8.next();
         if (i != 0 && i != pathFinderPath.size() - 1) {
            boolean canContinue = true;
            if (pathElm.squareDistanceTo(lastDashLoc) > CombatSetting.INSTANCE.tp.getValue()) {
               canContinue = false;
            } else {
               double smallX = Math.min(lastDashLoc.getX(), pathElm.getX());
               double smallY = Math.min(lastDashLoc.getY(), pathElm.getY());
               double smallZ = Math.min(lastDashLoc.getZ(), pathElm.getZ());
               double bigX = Math.max(lastDashLoc.getX(), pathElm.getX());
               double bigY = Math.max(lastDashLoc.getY(), pathElm.getY());
               double bigZ = Math.max(lastDashLoc.getZ(), pathElm.getZ());

               label54:
               for(int x = (int)smallX; (double)x <= bigX; ++x) {
                  for(int y = (int)smallY; (double)y <= bigY; ++y) {
                     for(int z = (int)smallZ; (double)z <= bigZ; ++z) {
                        if (!AStarCustomPathFinder.checkPositionValidity(x, y, z, false)) {
                           canContinue = false;
                           break label54;
                        }
                     }
                  }
               }
            }

            if (!canContinue) {
               path.add(lastLoc.addVector(0.5, 0.0, 0.5));
               lastDashLoc = lastLoc;
            }
         } else {
            if (lastLoc != null) {
               path.add(lastLoc.addVector(0.5, 0.0, 0.5));
            }

            path.add(pathElm.addVector(0.5, 0.0, 0.5));
            lastDashLoc = pathElm;
         }

         lastLoc = pathElm;
      }

      return path;
   }
}