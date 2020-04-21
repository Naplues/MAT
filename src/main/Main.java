package main;

import main.methods.NLP;
import main.methods.TM;

public class Main {

    public static String rootPath = Settings.rootPath;

    public static void main(String[] args) throws Exception {

        // 为各种方法准备数据
        //new Pattern().prepareData();
        //new TM().prepareData();
        new NLP().prepareData();

        // 使用各种方法进行预测
        //new Pattern().predict();
        //new TM().predict();
        //new NLP().predict();
        //new Mat().predict();
        //new TM().predictWithLimitedTrainingSet();

        //misClassification();

        /*
        Statistics.showResult("Pattern");
        Statistics.showResult("NLP");
        Statistics.showResult("TM");
        Statistics.showResult("Easy");
        Statistics.showResult("MAT"); //*/

        /*
        Statistics.getVennDiagram("1", "1"); // TP
        Statistics.getVennDiagram("0", "0"); // TN*/


    }
}
