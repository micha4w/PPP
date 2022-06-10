package com.Practice.Items;

public enum ItemType {
    INVENTORY(1, new InventoryItem()),
    TELEPORT(2, new TeleportItem()),
    TELEPORTINV(3, new TeleportInvItem()),
    TELEPORTMLG(4, new MLGItem()),
    GUIITEM(5, new GUIItem()),
    CLOCKITEM(6, new GUIItem()),
    LEAVE(7, new GUIItem()),
    RACE(8, new GUIItem()),
    CHANGEHEIGHT(9, new GUIItem()),
    PARKOUR(10, new ParkourItem()),
    RESTART(11, new GUIItem());

    public final int id;
    public final DefaultPPPItem item;
    ItemType(int id, DefaultPPPItem item) {
        this.id = id;
        this.item = item;
    }

    public static ItemType fromID(int id) {
        for (ItemType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        return null;
    }
}
