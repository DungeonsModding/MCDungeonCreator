package dungeoncreator.utils;

import dungeoncreator.GroupObject;
import dungeoncreator.models.TileObject;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.Deflater;

import java.util.ArrayList;
import java.util.zip.Inflater;

public class TileUtils {

    public static boolean checkOverLapping(ArrayList<TileObject> tiles, TileObject tile) {
        for(TileObject t : tiles) {
            if(t.isOverlapping(tile))
                return true;
        }
        return false;
    }

    public static TileObject getTileWithPlayerInside(ArrayList<TileObject> tiles, int playerX, int playerY, int playerZ) {
        for(TileObject t : tiles) {
            if(t.sizeX == 0 || t.sizeY == 0 || t.sizeZ == 0)
                t.computeSizes();

            if(t.minX < playerX && t.sizeX + t.minX > playerX
            && t.minY < playerY && t.sizeY + t.minY > playerY
            && t.minZ < playerZ && t.sizeZ + t.minZ > playerZ)
                return t;
        }
        return null;
    }

    public static void computeHeightMap(TileObject t, World world) {
        if(t.sizeX == 0 || t.sizeY == 0 || t.sizeZ == 0)
            t.computeSizes();

        for(int x = 0 ; x < t.sizeX; x++) {
            for(int z = 0; z < t.sizeZ; z++) {

                short y = 255;
                while(!world.getBlockState(new BlockPos(x+t.minX, y, z+t.minZ)).isSolid() && y != 0) {
                    y--;
                }

                t.heightPlane[x][z] = y;
            }
        }

        return;
    }

    public static String setBlockWalkable(PlayerEntity playerIn, byte value, int range) {
        GroupObject groupObject = GroupObject.getInstance();
        if(groupObject != null) {
            // Fetching the player position
            BlockPos pos = playerIn.getPosition();

            // Getting the tile where the player is in
            TileObject tile = TileUtils.getTileWithPlayerInside(groupObject.objects,pos.getX(),pos.getY(),pos.getZ());

            if(tile == null)  {
                return "You are not currently in a tile.";
            }
            else
            {
                // Getting what the player is focusing in
                final RayTraceResult rayTraceResult = playerIn.pick(50.0D, 0.0F, false);
                if(rayTraceResult.getType() != RayTraceResult.Type.BLOCK) {
                    return "Touching nothing";
                }
                else
                {
                    Vector3d hit = rayTraceResult.getHitVec();
                    if(tile.minX<hit.x && tile.minX+tile.sizeX-1 > hit.x
                            && tile.minZ<hit.z && tile.minZ+tile.sizeZ-1 > hit.z) {


                        for(int i = 0 ; i < range ; i++) {
                            for(int j = 0 ; j < range ; j++) {
                                int x = (int) (hit.x-tile.minX) + i - range/2;
                                int z = (int) (hit.z-tile.minZ) + j - range/2;

                                if(x < tile.sizeX && z < tile.sizeZ  && x >=0 && z >=0)
                                    tile.regionPlane[x][z] = value;
                            }
                        }

                    }
                    else {
                        return "Target is outside the tile";
                    }
                }
            }
        }
        else
            return "Please load the GroupObject first /tiles load";


        return null;
    }

    public static void exportRegionPlane(TileObject tile) {

        byte[][] plane = tile.regionPlane;
        byte[] simpleArray = new byte[tile.sizeX*tile.sizeZ];

        for(int x = 0 ; x < tile.sizeX; x++) {
            for(int z = 0; z < tile.sizeZ ; z++) {
                simpleArray[x+z*tile.sizeX] = plane[x][z];
            }
        }

        try {
            System.out.println("[" + new String(Base64.getEncoder().encode(compress(simpleArray))) +"]");


        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    // Source: https://dzone.com/articles/how-compress-and-uncompress
    private static byte[] compress(byte[] data) throws IOException {
        Deflater deflater = new Deflater();
        deflater.setInput(data);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);

        deflater.finish();
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer); // returns the generated code... index
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();

        return outputStream.toByteArray();
    }
}
