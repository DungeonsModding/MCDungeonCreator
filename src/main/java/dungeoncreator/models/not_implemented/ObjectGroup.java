package dungeoncreator.models.not_implemented;

import java.util.ArrayList;
import java.util.List;

public class ObjectGroup {
    List<ObjectTile> objects = null;

    public void addObjectTile(ObjectTile tile) {
        if(objects == null)
            objects = new ArrayList<>();

        objects.add(tile);
    }
}
