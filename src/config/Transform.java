package config;

import java.util.*;

public class Transform {

    public static Map<String, String> projectNames = new HashMap<>();

    static {
        projectNames.put("apache-ant-1.7.0", "Ant");
        projectNames.put("apache-jmeter-2.10", "JMeter");
        projectNames.put("argouml", "ArgoUML");
        projectNames.put("columba-1.4-src", "Columba");
        projectNames.put("emf-2.4.1", "EMF");
        projectNames.put("hibernate-distribution-3.3.2.GA", "Hibernate");
        projectNames.put("jEdit-4.2", "JEdit");
        projectNames.put("jfreechart-1.0.19", "JFreeChart");
        projectNames.put("jruby-1.4.0", "JRuby");
        projectNames.put("sql12", "SQuirrel");
    }

    public static String startWithProject(String line) {
        for (String project : projectNames.keySet()) if (line.startsWith(project)) return project;
        return null;
    }

    public static void main(String[] args) {
        // 去除异常注释格式
        List<String> lines = FileHandle.readFileToLines("data_new/technical_debt_dataset.csv");
        StringBuilder text = new StringBuilder();
        for (String line : lines) {
            String project = startWithProject(line);
            if (project != null) text.append("\n");
            text.append(line);
        }
        // 替换项目名
        String result = text.toString();
        for (Map.Entry<String, String> entry : projectNames.entrySet())
            result = result.replace(entry.getKey(), entry.getValue());

        FileHandle.writeStringToFile("data_new/dataset.csv", result);


        lines = FileHandle.readFileToLines("data_new/dataset.csv");
        // 分离项目名、数据标记和注释
        List<String> projects = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<String> comments = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String[] split = lines.get(i).split(",");
            projects.add(split[0]);
            labels.add(split[1]);
            String temp = "";
            for (int j = 2; j < split.length; j++) temp += split[j] + " ";
            comments.add(temp);
        }

        System.out.println(projects.size());
        FileHandle.writeLinesToFile("data_new/origin/projects", projects);
        FileHandle.writeLinesToFile("data_new/origin/labels", labels);
        FileHandle.writeLinesToFile("data_new/origin/comments", comments);
        System.out.println("Finish!");
    }
}
