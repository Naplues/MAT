package mat;

import config.Settings;

public class Discussion2 {

    public static String[][] keyWords = {
            {"todo"},//0
            {"hack"},//1
            {"fixme"},//2
            {"xxx"},//3
            {"todo", "hack"},//4
            {"todo", "fixme"},//5
            {"todo", "xxx"},//6
            {"hack", "fixme"},//7
            {"hack", "xxx"},//8
            {"fixme", "xxx"},//9
            {"todo", "hack", "fixme"},//10
            {"todo", "hack", "xxx"},//11
            {"todo", "fixme", "xxx"},//12
            {"hack", "fixme", "xxx"},//13
            {"todo", "hack", "fixme", "xxx"},//14
        };

    //25,
    public static void main(String[] args) {

        for (int i = 0; i < Settings.projectNames.length; i++) {
            double[] result = new double[3];
            int maxIndex = 0;
            for (int j = 0; j < keyWords.length; j++) {
                double[] temp = Main.predict(Settings.projectNames[i], keyWords[j], true, false);
                if (temp[2] > result[2]) {
                    maxIndex = j;
                    for (int k = 0; k < result.length; k++) result[k] = temp[k];
                }
            }
            System.out.println(maxIndex + "--" + "," + Settings.projectNames[i] + "," + result[0] + ", " + result[1] + ", " + result[2]);
        }
    }
}
