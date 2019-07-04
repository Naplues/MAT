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

    public static String[] projects = {"Ant", "ArgoUML", "Columba", "EMF", "Hibernate", "JEdit", "JFreeChart", "JMeter", "JRuby", "SQuirrel"};

    public static void main(String[] args) {
        String NLPPath = "data/nlp/";

        // 转换测试集格式
        for (int i = 0; i < projects.length; i++) {
            String testProject = NLPPath + "data--" + projects[i] + ".arff";
            StringBuilder text = new StringBuilder();
            List<String> lines = FileHandle.readFileToLines(testProject);
            for (String line : lines) {
                String[] splits = line.split(",");
                String comment = splits[0].replace("'", "");
                String label = "WITHOUT_CLASSIFICATION";
                if (splits[1].equals("positive")) label = "SATD";
                text.append(label).append("\t ").append(comment).append("\n");
            }
            FileHandle.writeStringToFile(testProject, text.toString());
        }

        // 构造训练集
        for (int i = 0; i < projects.length; i++) {

            String trainProjects = NLPPath + "train--" + projects[i] + ".arff";
            StringBuilder text = new StringBuilder();
            for (int j = 0; j < projects.length; j++) {
                if (i == j) continue;
                List<String> lines = FileHandle.readFileToLines(NLPPath + "data--" + projects[j] + ".arff");
                for (String line : lines) text.append(line).append("\n");
            }
            FileHandle.writeStringToFile(trainProjects, text.toString());
        }
    }
}
