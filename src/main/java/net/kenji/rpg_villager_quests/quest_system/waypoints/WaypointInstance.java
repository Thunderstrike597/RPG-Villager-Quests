package net.kenji.rpg_villager_quests.quest_system.waypoints;

import java.util.UUID;

public class WaypointInstance {
    public final int x;
    public final int y;
    public final int z;
    public final String waypointName;
    public final UUID waypointEntityUuid;

    public boolean hasCreatedCompatWaypoint;

    public  WaypointInstance(int x, int y, int z, String waypointName, UUID waypointEntity){
        this.x = x;
        this.y = y;
        this.z = z;
        this.waypointName = waypointName;
        this.waypointEntityUuid = waypointEntity;
    }


}
