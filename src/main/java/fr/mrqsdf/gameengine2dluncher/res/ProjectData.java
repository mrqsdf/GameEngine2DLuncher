package fr.mrqsdf.gameengine2dluncher.res;

public class ProjectData {

    public final String name;
    public final String groupId;
    public final String savePath;

    public ProjectData(String name, String groupId, String savePath) {
        this.name = name;
        this.groupId = groupId;
        this.savePath = savePath;
    }

    public String getName() {
        return name;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getSavePath() {
        return savePath;
    }
}
