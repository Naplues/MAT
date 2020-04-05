package main;

import dataset.ExtractComments;
import main.methods.*;
import others.FileHandle;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static String rootPath = Settings.rootPath;

    public static void main(String[] args) throws Exception {

        // 提取标注好的数据集
        //ExtractComments.rankingComments();
        //ExtractComments.exportDataset();

        // 为各种方法准备数据
        //new Pattern().prepareData();
        new TM().prepareData();
        //new NLP().prepareData();

        // 使用各种方法进行预测
        //new Pattern().predict();
        //new TM().predict();
        //new NLP().predict();
        new Mat().predict();

        for (String project : Settings.projectNames) {
            combineResult(project);
            //statistics(project);
            //misClassification(project);
        }
    }


    public static void getResult(String methodName, String projectName) {
        String PAT_Path = rootPath + methodName + "/result--" + projectName + ".txt";
    }

    /**
     * 将各个方法的结果整合到一起
     *
     * @param project 待处理项目
     */
    public static void combineResult(String project) {
        String commentPath = rootPath + "origin/data--" + project + ".txt";
        String labelPath = rootPath + "origin/label--" + project + ".txt";

        String PAT_Path = rootPath + "pattern/result--" + project + ".txt";
        String NLP_Path = rootPath + "nlp/result--" + project + ".txt";
        String T_M_Path = rootPath + "tm/result--" + project + ".txt";
        String MAT_Path = rootPath + "mat/result--" + project + ".txt";

        String resultPath = rootPath + "result/" + project + ".csv";

        List<String> commentLines = FileHandle.readFileToLines(commentPath);
        List<String> labelLines = FileHandle.readFileToLines(labelPath);

        List<String> PAT_Lines = FileHandle.readFileToLines(PAT_Path);
        List<String> NLP_Lines = FileHandle.readFileToLines(NLP_Path);
        List<String> T_M_Lines = FileHandle.readFileToLines(T_M_Path);
        List<String> MAT_Lines = FileHandle.readFileToLines(MAT_Path);

        // 组合结果
        List<String> matTMLines = new ArrayList<>();
        List<String> matNLPLines = new ArrayList<>();
        StringBuilder text = new StringBuilder("oracle, Pattern, NLP, TM, MAT, MAT_TM, MAT_NLP,Comments\n");

        for (int i = 0; i < commentLines.size(); i++) {
            if (labelLines.get(i).equals("positive")) text.append(1);
            else text.append(0);
            text.append(",");                                // Oracle
            text.append(PAT_Lines.get(i)).append(",");       // Pattern 结果
            text.append(NLP_Lines.get(i)).append(",");       // NLP     结果
            text.append(T_M_Lines.get(i)).append(",");       // TM      结果
            text.append(MAT_Lines.get(i)).append(",");       // MAT     结果

            // TM 和 MAT 组合结果
            if (MAT_Lines.get(i).equals("1") || T_M_Lines.get(i).equals("1")) matTMLines.add("1");
            else matTMLines.add("0");
            text.append(matTMLines.get(i)).append(",");

            // NLP 和 MAT 组合结果
            if (MAT_Lines.get(i).equals("1") || NLP_Lines.get(i).equals("1")) matNLPLines.add("1");
            else matNLPLines.add("0");
            text.append(matTMLines.get(i)).append(",");

            // 注释
            text.append(commentLines.get(i)).append("\n");  // 注释
        }
        FileHandle.writeStringToFile(resultPath, text.toString());
        //Method.evaluate(labelLines, PAT_Lines);
        //Method.evaluate(labelLines, NLP_Lines);
        //Method.evaluate(labelLines, T_M_Lines);
        Method.evaluate(labelLines, MAT_Lines);
        //Method.evaluate(labelLines, matTMLines);
        //Method.evaluate(labelLines, matNLPLines);

        System.out.println(project);

    }

    /**
     * 误分类情况统计
     *
     * @param projectName
     */
    public static void misClassification(String projectName) {
        System.out.println(projectName);
        String resultPath = rootPath+"/result/" + projectName + ".csv";
        List<String> lines = FileHandle.readFileToLines(resultPath);
        int count = 0;
        for (int i = 1; i < lines.size(); i++) {
            String[] splits = lines.get(i).split(",");
            String oracle = splits[0];
            String mat = splits[4];
            String comment = splits[7];
            //FN
            if (oracle.equals("1") && mat.equals("0")) {
                System.out.printf("%s, %s, %s\n", oracle, mat, comment);
                count++;
            }

        }
        System.out.println(count);
        //System.out.println(lines.size());
    }

    public static void statistics(String projectName) {
        String resultPath = rootPath + "/result/" + projectName + ".csv";
        List<String> lines = FileHandle.readFileToLines(resultPath);
        int count = 0;
        for (int i = 1; i < lines.size(); i++) {
            String[] splits = lines.get(i).split(",");
            String oracle = splits[0];  // oracle
            String pattern = splits[1]; // pattern
            String nlp = splits[2];     // nlp
            String tm = splits[3];      //tm
            String mat = splits[4];     //mat
            if (oracle.equals("1")) // tm.equals(mat) && tm.equals("1")
                if (tm.equals("1") && mat.equals("0")) count++;
        }
        System.out.println(count);
        //System.out.println(lines.size());
    }

}
