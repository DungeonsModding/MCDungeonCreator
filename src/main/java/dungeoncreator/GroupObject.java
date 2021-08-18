package dungeoncreator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dungeoncreator.models.InGameTile;
import dungeoncreator.utils.TileUtils;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.event.ClickEvent;
import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class GroupObject {

    transient private static String fileName = "objectgroup.json";
    transient private static GroupObject groupObject = null;
    transient private static File saveDir = null;

    public ArrayList<InGameTile> objects = null;

    public GroupObject() {}

    public boolean addTile(InGameTile tileObject) throws IOException {
        System.out.println("Adding tiles");
        if(objects == null)
            objects = new ArrayList<>();

        if(TileUtils.checkOverLapping(objects, tileObject))
            return false;

        objects.add(tileObject);
        System.out.println("tiles added");
        save();
        return true;
    }

    private static GroupObject load() {
        File objectgroup = new File(saveDir.getAbsoluteFile() + "\\" + fileName);

        if(!objectgroup.exists()) {
            System.out.println("Object group file does not exist.");
            return new GroupObject();
        }
        System.out.println("Converting json to GroupObject.class");
        String json ;
        try {
            json = FileUtils.readFileToString(objectgroup, StandardCharsets.UTF_8);
            GroupObject obj = new Gson().fromJson(json, GroupObject.class);
            for(InGameTile t : obj.objects) {
                t.computeSizes();
                t.decompressEncodedRegionPlane();
            }
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
            return new GroupObject();
        }
    }

    public InGameTile getTileByName(String name) {
        for (InGameTile object : objects) {
            if (object.id.equals(name)) {
                return object;
            }
        }
        return null;
    }

    public IFormattableTextComponent deleteTile(String tileName) {
        InGameTile t = getTileByName(tileName);
        if(t == null)
            return new StringTextComponent("[Error] Tile with name \"" + tileName + "\" not found.").setStyle(Style.EMPTY.setColor(Color.fromHex("#FF0000")));

        objects.remove(t);
        try {
            save();
        } catch (IOException e) {
            e.printStackTrace();
            return new StringTextComponent("[Error] Problem while saving.");
        }
        return new StringTextComponent(tileName + " deleted.");
    }

    public TextComponent listAllTiles() {
        TextComponent t;
        if(objects == null || objects.size() == 0) {
            t = new StringTextComponent("No tiles in memory.");
            return t;
        }

        t = new StringTextComponent(objects.size() + " tile(s) in memory.");

        for(InGameTile o : objects) {
            t.append(new StringTextComponent("\n- " + o.id + " "));

            Style style = Style.EMPTY
                    .setItalic(true)
                    .setColor(Color.fromHex("#0000FF"))
                    .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/tp @p " + ((o.pos[0]+o.pos2[0])/2) + " " + ((o.pos[1]+o.pos2[1])/2) + " " + ((o.pos[2]+o.pos2[2])/2)));
            IFormattableTextComponent cmd = new StringTextComponent("(Teleport)").setStyle(style);
            t.append(cmd);
        }

        return t;
    }

    public void save() throws IOException {
        System.out.println("Saving");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(this);
        BufferedWriter writer = new BufferedWriter( new FileWriter(saveDir.getAbsoluteFile() + "\\" + fileName));
        writer.write(json);
        writer.close();
    }

    public static GroupObject getInstance() {
        if(saveDir == null) {
            return null;
        }
        return groupObject;
    }

    /**
     * Return the singleton GameObject instance for the world
     */
    public static GroupObject getInstance(File _saveDir) {
        if(saveDir == null || !saveDir.equals(_saveDir)) {
            saveDir = _saveDir;
            groupObject = load();
        }
        System.out.println("getInstance");
        return groupObject;
    }
}
