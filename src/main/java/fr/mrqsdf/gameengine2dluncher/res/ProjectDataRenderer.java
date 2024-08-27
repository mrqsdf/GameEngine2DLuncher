package fr.mrqsdf.gameengine2dluncher.res;

import javax.swing.*;
import java.awt.*;

public class ProjectDataRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value instanceof ProjectData) {
            ProjectData project = (ProjectData) value;
            setText("Name: " + project.getName() + " | GroupId: " + project.getGroupId() + " | Path: " + project.getSavePath());
        }

        return this;
    }
}
