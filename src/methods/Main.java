package methods;

import config.FileHandle;
import config.Settings;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        for (String project : Settings.projectNames) combineResult(project);
    }

    public static void combineResult(String project) {
        String commentPath = "data/pattern/data--" + project + ".txt";
        String labelPath = "data/pattern/label--" + project + ".txt";

        String patternPath = "data/pattern/result--" + project + ".txt";
        String tmPath = "data/others.tm/result--" + project + ".txt";
        String matPath = "data/others/result--" + project + ".txt";

        String resultPath = "data/result/" + project + ".csv";

        List<String> commentLines = FileHandle.readFileToLines(commentPath);
        List<String> labelLines = FileHandle.readFileToLines(labelPath);

        List<String> patternLines = FileHandle.readFileToLines(patternPath);
        List<String> tmLines = FileHandle.readFileToLines(tmPath);
        List<String> matLines = FileHandle.readFileToLines(matPath);

        StringBuilder text = new StringBuilder("oracle,Pattern,TM,MAT,Comments\n");

        for (int i = 0; i < commentLines.size(); i++) {
            if (labelLines.get(i).equals("positive")) text.append(1);
            else text.append(0);
            text.append(",");                               // Oracle
            text.append(patternLines.get(i)).append(",");   // Pattern 结果
            text.append(tmLines.get(i)).append(",");        // TM      结果
            text.append(matLines.get(i)).append(",");       // MAT     结果
            text.append(commentLines.get(i)).append("\n");  // 注释
        }
        FileHandle.writeStringToFile(resultPath, text.toString());
        //evaluate(labelLines, patternLines);
        evaluate(labelLines, tmLines);
        //evaluate(labelLines, matLines);
        System.out.println("Output result file successfully! " + project);
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
        System.out.printf("%.3f, %.3f, %.3f ", precision, recall, f1);
    }
}
