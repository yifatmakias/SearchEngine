package model;

public class Entity {
    private String entityName;
    private String entityRank;

    /**
     * Entity class to show in table view.
     */
    public Entity(String entityName, String entityRank) {
        this.entityName = entityName;
        this.entityRank = entityRank;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getEntityRank() {
        return entityRank;
    }
}
