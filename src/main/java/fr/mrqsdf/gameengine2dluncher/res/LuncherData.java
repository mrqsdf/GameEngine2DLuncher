package fr.mrqsdf.gameengine2dluncher.res;

import java.util.ArrayList;
import java.util.List;

public class LuncherData {

    private List<ProjectData> projects = new ArrayList<>();

    public void addProject(ProjectData project){
        projects.add(project);
    }

    public List<ProjectData> getProjects(){
        return projects;
    }

}
