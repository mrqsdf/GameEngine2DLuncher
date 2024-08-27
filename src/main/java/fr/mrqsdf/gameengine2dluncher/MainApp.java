package fr.mrqsdf.gameengine2dluncher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.mrqsdf.gameengine2dluncher.res.LuncherData;
import fr.mrqsdf.gameengine2dluncher.res.ProjectData;
import fr.mrqsdf.gameengine2dluncher.res.ProjectDataRenderer;
import org.ietf.jgss.GSSContext;
import org.json.JSONWriter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class MainApp {

    public static MainApp mainApp;

    private JFrame mainFrame;
    public JList<ProjectData> projectList;
    public LuncherData luncherData;
    public DefaultListModel<ProjectData> listModel = new DefaultListModel<>();

    public MainApp() {
        loadExistingProjects();
        mainFrame = new JFrame("Project Manager");
        mainFrame.setSize(600, 400);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout());

        // Panel supérieur avec les boutons
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());

        JButton createButton = new JButton("Nouveau Projet");
        JButton openButton = new JButton("Ouvrir Projet");

        topPanel.add(createButton);
        topPanel.add(openButton);

        mainFrame.add(topPanel, BorderLayout.NORTH);

        // Liste des projets existants

        for (ProjectData project : luncherData.getProjects()) {
            listModel.addElement(project);
        }
        projectList = new JList<>(listModel);
        projectList.setCellRenderer(new ProjectDataRenderer()); // Custom renderer to display project information
        JScrollPane scrollPane = new JScrollPane(projectList);

        mainFrame.add(scrollPane, BorderLayout.CENTER);

        // Action pour créer un nouveau projet
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createNewProject();
            }
        });

        // Action pour ouvrir un projet sélectionné
        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openSelectedProject();
            }
        });

        // Rendre la fenêtre visible
        mainFrame.setVisible(true);
    }

    private void loadExistingProjects() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        File projectDataFile = new File("saveFile.json");
        if (!projectDataFile.exists()) {
            try {
                FileWriter writer = new FileWriter("saveFile.json");
                writer.write(gson.toJson(new LuncherData()));
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            luncherData = new LuncherData();
            return;
        }

        try {
            String json = Files.readString(projectDataFile.toPath());
            luncherData = gson.fromJson(json, LuncherData.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        /*for (ProjectData project : luncherData.getProjects()) {
            String savePath = project.getSavePath();
            File projectDir = new File(savePath, project.getName());
            File[] files = projectDir.listFiles();
            if (files == null || files.length == 0) {
                listModel.removeElement(project);
                luncherData.getProjects().remove(project);
            }
        }

        try {
            FileWriter writer = new FileWriter("saveFile.json");
            writer.write(gson.toJson(new LuncherData()));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

    }

    private void createNewProject() {
        JTextField projectNameField = new JTextField();
        JTextField groupIdField = new JTextField("com.example");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Choisissez l'emplacement de sauvegarde du projet");

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Nom du projet :"));
        panel.add(projectNameField);
        panel.add(new JLabel("Group ID (par défaut : com.example) :"));
        panel.add(groupIdField);

        int result = JOptionPane.showConfirmDialog(mainFrame, panel, "Créer un nouveau projet", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String projectName = projectNameField.getText().trim();
            String groupId = groupIdField.getText().trim();

            if (projectName.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "Le nom du projet ne peut pas être vide.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Demande de l'emplacement de sauvegarde du projet
            int returnValue = fileChooser.showSaveDialog(mainFrame);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File saveLocation = fileChooser.getSelectedFile();

                // Création du projet avec les informations fournies
                ProjectManager.createProject(projectName, groupId.isEmpty() ? "com.example" : groupId, saveLocation);

            } else {
                JOptionPane.showMessageDialog(mainFrame, "L'emplacement de sauvegarde n'a pas été sélectionné.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openSelectedProject() {
        ProjectData selectedProject = projectList.getSelectedValue();
        if (selectedProject != null) {
            ProjectManager.openProject(selectedProject.getName(), selectedProject.getSavePath(), selectedProject.getGroupId());
        } else {
            JOptionPane.showMessageDialog(mainFrame, "Veuillez sélectionner un projet à ouvrir.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        mainApp = new MainApp();
    }
}