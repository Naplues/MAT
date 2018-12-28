package nju.gzq.simple;

public class Discussion2 {

    public static String[] projectNames = {"argouml", "columba-1.4-src", "hibernate-distribution-3.3.2.GA", "jEdit-4.2",
            "jfreechart-1.0.19", "apache-jmeter-2.10", "jruby-1.4.0", "sql12", "apache-ant-1.7.0", "emf-2.4.1"};

    public static String[][] keyWords = {
            {"todo"},//0
            {"hack"},//1
            {"workaround"},//2
            {"fixme"},//3
            {"xxx"},//4
            {"todo", "hack"},//5
            {"todo", "workaround"},//6
            {"todo", "fixme"},//7
            {"todo", "xxx"},//8
            {"hack", "workaround"},//9
            {"hack", "fixme"},//10
            {"hack", "xxx"},//11
            {"workaround", "fixme"},//12
            {"workaround", "xxx"},//13
            {"fixme", "xxx"},//14
            {"todo", "hack", "workaround"},//15
            {"todo", "hack", "fixme"},//16
            {"todo", "hack", "xxx"},//17
            {"todo", "workaround", "fixme"},//18
            {"todo", "workaround", "xxx"},//19
            {"todo", "fixme", "xxx"},//20
            {"hack", "workaround", "fixme"},//21
            {"hack", "workaround", "xxx"},//22
            {"hack", "fixme", "xxx"},//23
            {"workaround", "fixme", "xxx"},//24
            {"todo", "hack", "workaround", "fixme"},//25
            {"todo", "hack", "workaround", "xxx"},//26
            {"todo", "hack", "fixme", "xxx"},//27
            {"todo", "workaround", "fixme", "xxx"},//28
            {"hack", "workaround", "fixme", "xxx"},//29
            {"todo", "hack", "workaround", "fixme", "xxx"}}; //30 "todo", "workaround", "fixme", "xxx"

    //25,
    public static void main(String[] args) {

        for (int i = 0; i < projectNames.length; i++) {
            double[] result = new double[3];
            int maxIndex = 0;
            for (int j = 0; j < keyWords.length; j++) {
                double[] temp = Main.readData(projectNames[i], keyWords[j], true, false);
                if (temp[2] > result[2]) {
                    maxIndex = j;
                    for (int k = 0; k < result.length; k++) result[k] = temp[k];
                }
            }
            System.out.println(maxIndex + "--" + "," + projectNames[i] + "," + result[0] + ", " + result[1] + ", " + result[2]);
        }
    }
}
