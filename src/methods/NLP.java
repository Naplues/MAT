package methods;

import config.FileHandle;
import edu.stanford.nlp.classify.ColumnDataClassifier;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.objectbank.ObjectBank;

public class NLP {

    private static String where = "data/nlp/";
    public static String[] projects = { "ArgoUML", "Columba",  "Hibernate", "JEdit", "JFreeChart", "JMeter", "JRuby", "SQuirrel"};

    public static void main(String[] args) throws Exception {

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
/*
            System.out.println();
            System.out.println("Testing accuracy of ColumnDataClassifier");
            Pair<Double, Double> performance = cdc.testClassifier(testFile);
            System.out.printf("Accuracy: %.3f; macro-F1: %.3f%n", performance.first(), performance.second());*/
        }
    }
}
