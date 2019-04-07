package config;

import java.util.ArrayList;
import java.util.List;

public class Transform {

    public static String startWithProject(String line) {
        for (String project : Settings.projectNames) {
            if (line.startsWith(project)) return project;
        }
        return null;
    }

    public static void main(String[] args) {
        List<String> lines = FileHandle.readFileToLines("data_new/technical_debt_dataset.csv");
        List<String> projects = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<String> comments = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String project = startWithProject(lines.get(i));
            if (project != null) {
                String label = lines.get(i).split(",")[1];
                String comment = "";
                String[] temp = lines.get(i).split(",");
                for (int k = 2; k < temp.length; k++) comment += temp[k];
                projects.add(project);
                labels.add(label);
                comments.add(comment);
            } else {
                String lastComment = comments.get(comments.size() - 1);
                lastComment += lines.get(i);
                comments.set(comments.size() - 1, lastComment);
            }
            System.out.println(i);
        }

        System.out.println(projects);
        System.out.println(projects.size());
        FileHandle.writeLinesToFile("data_new/origin/projects", projects);
        FileHandle.writeLinesToFile("data_new/origin/labels", labels);
        FileHandle.writeLinesToFile("data_new/origin/comments", comments);
        System.out.println("Finish!");
    }
}
