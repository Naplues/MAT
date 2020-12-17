package main;

import main.methods.Mat;
import main.methods.NLP;
import main.methods.Pattern;
import main.methods.TM;

public class Main {

    public static void main(String[] args) throws Exception {
        // java -jar MAT.jar -p D:/exp_data/ -o D:/result/ -m Pattern -s MTO
        Config config = Config.parseArgs(args);
        Settings.rootPath = config.dataPath;
        Settings.resultPath = config.resultPath;

        if (config.scenario.equals("MTO")) {
            System.out.println("Running model " + config.model + " in " + config.scenario);
            switch (config.model) {
                case "Pattern":
                    new Pattern().predict();
                    break;
                case "NLP":
                    new NLP().predict();
                    break;
                case "TM":
                    new TM().predict();
                    break;
                case "MAT":
                    new Mat().predict();
                    break;
                default:
                    break;
            }
        }

        if (config.scenario.equals("OTO")) {
            switch (config.model) {
                case "Pattern":
                    new Pattern().predict();
                    break;
                case "NLP":
                    new NLP().predictWithLimitedTrainingSet();
                    break;
                case "TM":
                    new TM().predictWithLimitedTrainingSet();
                    break;
                case "MAT":
                    new Mat().predict();
                    break;
                default:
                    break;
            }
        }
    }
}


class Config {
    String dataPath = "";
    String resultPath = "";
    String model = "";
    String scenario = "";

    public static Config parseArgs(String[] args) {
        Config config = new Config();
        for (int i = 0; i < args.length - 1; i += 2) {
            if (args[i].equals("-p")) config.dataPath = args[i + 1];
            if (args[i].equals("-o")) config.resultPath = args[i + 1];
            if (args[i].equals("-m")) config.model = args[i + 1];
            if (args[i].equals("-s")) config.scenario = args[i + 1];
        }
        return config;
    }
}