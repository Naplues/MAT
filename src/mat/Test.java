package mat;

import config.FileHandle;
import config.Settings;

import java.util.List;

public class Test {
    public static void main(String[] args) {
        for (String projectName : Settings.projectNames) {
            int count = 0;
            List<String> instances = FileHandle.readFileToLines(Main.rootPath + "data--" + projectName + ".txt");
            String[] labels = new String[instances.size()];
            int[] predicts = new int[instances.size()];

            String text = "@relation 'technicalDebt'\n" +
                    "\n" +
                    "@attribute Text string\n" +
                    "@attribute class-att {negative,positive}\n" +
                    "\n" +
                    "@data\n\n";
            //System.out.println(instances.size());
            for (int i = 0; i < instances.size(); i++) {
                labels[i] = instances.get(i).split(",")[1];
                predicts[i] = Main.classify(instances.get(i).split(",")[0], Main.keyWords, true);
                if (predicts[i] == 0) {
                    text += instances.get(i) + "\n";
                    count++;
                }
            }
            System.out.println(count);
            FileHandle.writeStringToFile("data/new/data--" + projectName + ".arff", text);
        }
    }
}
