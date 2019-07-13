package methods;

import config.FileHandle;
import edu.stanford.nlp.classify.ColumnDataClassifier;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.objectbank.ObjectBank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NLP {

    private static String where = "data/nlp_new/";
    public static String[] projects = {"Ant", "ArgoUML", "Columba", "EMF", "Hibernate", "JEdit", "JFreeChart", "JMeter", "JRuby", "SQuirrel"};

    public static void main(String[] args) throws Exception {
        resultByMultiProjects();
        //resultBySingleProject();
    }

    public static void resultByMultiProjects() throws Exception {

        for (String project : projects) {
            String trainFile = where + "train--" + project + ".arff";
            String testFile = where + "data--" + project + ".arff";
            String resultFile = where + "result--" + project + ".txt";
            StringBuilder text = new StringBuilder();

            ColumnDataClassifier cdc = new ColumnDataClassifier(where + "cheese2007.prop");
            cdc.trainClassifier(trainFile);

            for (String line : ObjectBank.getLineIterator(testFile, "utf-8")) {
                Datum<String, String> d = cdc.makeDatumFromLine(line);
                System.out.printf("%s  ==>  %s (%.4f)%n", line, cdc.classOf(d), cdc.scoresOf(d).getCount(cdc.classOf(d)));
                if (cdc.classOf(d).equals("WITHOUT_CLASSIFICATION")) text.append("0").append("\n");
                else text.append("1").append("\n");
            }

            FileHandle.writeStringToFile(resultFile, text.toString());
        }
    }

    public static void resultBySingleProject() throws IOException {

        for (String testProject : projects) {
            StringBuilder allText = new StringBuilder();
            // 测试项目
            String testFile = where + "data--" + testProject + ".arff";
            for (String trainProject : projects) {
                if (trainProject.equals(testProject)) continue;
                String trainFile = where + "data--" + trainProject + ".arff";
                String resultFile = where + "result--" + testProject + ".txt";
                StringBuilder text = new StringBuilder();

                ColumnDataClassifier cdc = new ColumnDataClassifier(where + "cheese2007.prop");
                cdc.trainClassifier(trainFile);

                for (String line : ObjectBank.getLineIterator(testFile, "utf-8")) {
                    Datum<String, String> d = cdc.makeDatumFromLine(line);
                    if (cdc.classOf(d).equals("WITHOUT_CLASSIFICATION")) text.append("0").append("\n");
                    else text.append("1").append("\n");
                }
                FileHandle.writeStringToFile(resultFile, text.toString());

                List<String> resultFileLines = FileHandle.readFileToLines(resultFile);
                List<String> oracleFileLines = FileHandle.readFileToLines("data/result/" + testProject + ".csv");

                List<String> oracle = new ArrayList<>();
                for (int i = 1; i < oracleFileLines.size(); i++) {
                    oracle.add(oracleFileLines.get(i).split(",")[0]);
                }

                allText.append(evaluate(oracle, resultFileLines)).append("\n");
            }
            FileHandle.writeStringToFile(where + "result--" + testProject + ".csv", allText.toString(), true);
        }
    }

    public static String evaluate(List<String> oracle, List<String> predictions) {
        //计算混淆矩阵, precision, recall and F1-measure
        int TP = 0, FP = 0, FN = 0, TN = 0;
        for (int i = 0; i < oracle.size(); i++) {
            if (oracle.get(i).equals("1") && predictions.get(i).equals("1")) TP++;
            if (oracle.get(i).equals("1") && predictions.get(i).equals("0")) FN++;
            if (!oracle.get(i).equals("1") && predictions.get(i).equals("1")) FP++;
            if (!oracle.get(i).equals("1") && predictions.get(i).equals("0")) TN++;
        }
        double precision = (double) TP / (TP + FP);
        double recall = (double) TP / (TP + FN);
        double f1 = 2 * precision * recall / (precision + recall);
        System.out.printf("%.3f, %.3f, %.3f, ", precision, recall, f1);
        return precision + "," + recall + "," + f1;
    }
}