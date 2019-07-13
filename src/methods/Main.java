package methods;

import config.FileHandle;
import config.Settings;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        //Mat.main(args);
        for (String project : Settings.projectNames) {
            //System.out.print(project + ":\t");
            combineResult(project);
            //statistics(project);
            //misClassification(project);
        }
    }

    public static void misClassification(String projectName) {
        System.out.println(projectName);
        String resultPath = "data/result/" + projectName + ".csv";
        List<String> lines = FileHandle.readFileToLines(resultPath);
        int count = 0;
        for (int i = 1; i < lines.size(); i++) {
            String[] splits = lines.get(i).split(",");
            String oracle = splits[0];
            String mat = splits[3];
            String comment = splits[6];
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
        String resultPath = "data/result/" + projectName + ".csv";
        List<String> lines = FileHandle.readFileToLines(resultPath);
        int count = 0;
        for (int i = 1; i < lines.size(); i++) {
            String[] splits = lines.get(i).split(",");
            String oracle = splits[0];
            String pattern = splits[1];
            String nlp = splits[2];
            String tm = splits[3];
            String mat = splits[4];
            if (oracle.equals("1")) // tm.equals(mat) && tm.equals("1")
                if (nlp.equals("1") && mat.equals("0")) count++;
        }
        System.out.println(count);
        //System.out.println(lines.size());
    }

    public static void combineResult(String project) {
        String commentPath = "data/pattern/data--" + project + ".txt";
        String labelPath = "data/pattern/label--" + project + ".txt";

        String patternPath = "data/pattern/result--" + project + ".txt";
        String nlpPath = "data/nlp_new/result--" + project + ".txt";
        String tmPath = "data/tm_new/result--" + project + ".txt";
        String matPath = "data/mat/result--" + project + ".txt";

        String resultPath = "data/result/" + project + ".csv";

        List<String> commentLines = FileHandle.readFileToLines(commentPath);
        List<String> labelLines = FileHandle.readFileToLines(labelPath);

        List<String> patternLines = FileHandle.readFileToLines(patternPath);
        List<String> nlpLines = FileHandle.readFileToLines(nlpPath);
        List<String> tmLines = FileHandle.readFileToLines(tmPath);
        List<String> matLines = FileHandle.readFileToLines(matPath);

        // 组合结果
        List<String> matTMLines = new ArrayList<>();
        List<String> matNLPLines = new ArrayList<>();
        StringBuilder text = new StringBuilder("oracle, Pattern, NLP, TM, MAT, MAT_TM, MAT_NLP,Comments\n");

        for (int i = 0; i < commentLines.size(); i++) {
            if (labelLines.get(i).equals("positive")) text.append(1);
            else text.append(0);
            text.append(",");                               // Oracle
            text.append(patternLines.get(i)).append(",");   // Pattern 结果
            text.append(nlpLines.get(i)).append(",");       // NLP     结果
            text.append(tmLines.get(i)).append(",");        // TM      结果
            text.append(matLines.get(i)).append(",");       // MAT     结果

            // TM 和 MAT 组合结果
            if (matLines.get(i).equals("1") || tmLines.get(i).equals("1")) matTMLines.add("1");
            else matTMLines.add("0");
            text.append(matTMLines.get(i)).append(",");
            // NLP 和 MAT 组合结果
            if (matLines.get(i).equals("1") || nlpLines.get(i).equals("1")) matNLPLines.add("1");
            else matNLPLines.add("0");
            text.append(matTMLines.get(i)).append(",");
            // 注释
            text.append(commentLines.get(i)).append("\n");  // 注释
        }
        FileHandle.writeStringToFile(resultPath, text.toString());
        // evaluate(labelLines, patternLines);
        evaluate(labelLines, nlpLines);
        //evaluate(labelLines, tmLines);
        // evaluate(labelLines, matLines);
        //evaluate(labelLines, matTMLines);
        //evaluate(labelLines, matNLPLines);
        System.out.println();
        //System.out.println("Output result file successfully! " + project);
    }


    public static void evaluate(List<String> oracle, List<String> predictions) {
        //计算混淆矩阵, precision, recall and F1-measure
        int TP = 0, FP = 0, FN = 0, TN = 0;
        for (int i = 0; i < oracle.size(); i++) {
            if (oracle.get(i).equals("positive") && predictions.get(i).equals("1")) TP++;
            if (oracle.get(i).equals("positive") && predictions.get(i).equals("0")) FN++;
            if (!oracle.get(i).equals("positive") && predictions.get(i).equals("1")) FP++;
            if (!oracle.get(i).equals("positive") && predictions.get(i).equals("0")) TN++;
        }
        double precision = (double) TP / (TP + FP);
        double recall = (double) TP / (TP + FN);
        double f1 = 2 * precision * recall / (precision + recall);
        System.out.printf("%.3f, %.3f, %.3f, ", precision, recall, f1);
    }
}
