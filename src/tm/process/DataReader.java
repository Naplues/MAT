package tm.process;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tm.util.FileUtil;
import tm.domain.Document;

public class DataReader {

    private static List<Document> commentList; //注释对象列表

    /**
     * 根据原始注释 生成 arff 数据文件
     *
     * @param comments
     * @param outputFilePath
     */
    public static void outputArffData(List<Document> comments, String outputFilePath) {
        // notice: here we assume positive class is SATD
        // arff declare info
        List<String> lines = new ArrayList<>();
        lines.add("@relation 'technicalDebt'");
        lines.add("");
        lines.add("@attribute Text string");
        lines.add("@attribute class-att {negative,positive}");
        lines.add("");
        lines.add("@data");
        lines.add("");

        for (Document doc : comments) {
            String tmp = "'";
            for (String word : doc.getWords()) tmp = tmp + word + " ";
            if (doc.getLabel().equals("WITHOUT_CLASSIFICATION")) tmp = tmp + "',negative";// negative comments
            else tmp = tmp + "',positive";
            lines.add(tmp);
        }
        FileUtil.writeLinesToFile(lines, outputFilePath);
    }

    /**
     * 选择项目
     *
     * @param projectName 项目列表
     * @return
     */
    public static List<Document> selectProject(String... projectName) {
        List<Document> res = new ArrayList<>();
        for (Document doc : commentList) {
            for (String project : projectName) if (project.equals(doc.getProject())) res.add(doc);
        }
        return res;
    }

    /**
     * 读取注释数据, 将注释存储到List<Document> 中
     *
     * @param path
     * @return
     */
    public static void readComments(String path) {
        List<Document> comments = new ArrayList<>();
        // read comments' content first
        List<String> lines = FileUtil.readLinesFromFile(path + "comments");

        // 读取注释内容
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (!line.contains("\"/*")) {
                comments.add(new Document(line));
            } else {
                String tmp = "";
                for (int j = i; j < lines.size(); j++) {
                    tmp = tmp + lines.get(j);
                    if (lines.get(j).contains("*/\"")) {
                        i = j;
                        break;
                    }
                }
                comments.add(new Document(tmp));
            }
        }

        // 读取标签
        lines = FileUtil.readLinesFromFile(path + "labels");
        for (int i = 0; i < lines.size(); i++) comments.get(i).setLabel(lines.get(i));

        // 读取项目名称
        lines = FileUtil.readLinesFromFile(path + "projects");
        for (int i = 0; i < lines.size(); i++) comments.get(i).setProject(lines.get(i));

        // 移除重复和空的注释
        commentList = new ArrayList<>();
        Set<String> content = new HashSet<>();
        for (Document doc : comments) {
            if (doc.getWords().isEmpty() || content.contains(doc.getContent())) continue;
            content.add(doc.getContent());
            commentList.add(doc);
        }
    }
}
