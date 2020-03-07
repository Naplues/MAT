package others;

import main.Settings;

import java.io.File;
import java.util.*;

/**
 * 对数据集进行采样研究
 */
public class Sampling {
    public static String sampleRootPath = Settings.rootPath + "samples/";
    public static String commentPath = Settings.rootPath + "tm/";
    public static String labelPath = Settings.rootPath + "origin/";

    public static Random random = new Random(1);

    public static void main(String[] args) {
        //samplingForReview(661);

        calcKappa();


        for (String project : Settings.projectNames) {
            //"data/pattern/comment--"
            //samplingForStudy(commentPath + "/data--", project, 0.1); //采样
        }

        File[] lines = new File("C:\\Users\\GZQ\\OneDrive\\缺陷定位\\").listFiles();
        for (File line : lines) {
            if (line.getName().startsWith("[")) {
                String[] t = line.getName().split("--");
                System.out.println(t[0] + " " + t[1] + " " + t[2]);
            }
        }

    }

    /**
     * 计算cohen kappa一致性
     */
    public static void calcKappa() {
        double p11 = .0, p12 = .0, p21 = .0, p22 = .0;
        List<String> lines = FileHandle.readFileToLines(Settings.rootPath + "reviewLabel.csv");
        for (String line : lines) {
            String[] temp = line.split(",");
            if (temp[0].equals("1") && temp[1].equals("1")) p11++;
            if (temp[0].equals("1") && temp[1].equals("0")) p12++;
            if (temp[0].equals("0") && temp[1].equals("1")) p21++;
            if (temp[0].equals("0") && temp[1].equals("0")) p22++;
        }
        double n = p11 + p12 + p21 + p22;
        double p0 = (p11 + p22) / n;
        double pe = ((p11 + p21) * (p11 + p12) + (p12 + p22) * (p21 + p22)) / (n * n);
        double kappa = (p0 - pe) / (1 - pe);
        System.out.println(p11 + ", " + p12);
        System.out.println(p21 + ", " + p22);
        System.out.printf("%.3f\n", kappa);
    }

    /**
     * 为检查标签 进行采样
     * 仅仅处理新收集的项目
     *
     * @param samplingNumber default = 661
     */
    public static void samplingForReview(int samplingNumber) {
        List<String> commentLines = FileHandle.readFileToLines(labelPath + "comments");
        List<String> labelLines = FileHandle.readFileToLines(labelPath + "labels");
        List<String> posInstances = new ArrayList<>();
        List<String> negInstances = new ArrayList<>();

        //正负例数据总数目
        double posNumber = 0, negNumber = 0;

        //填充正负例数据
        for (int i = 0; i < commentLines.size(); i++) {
            if (!labelLines.get(i).equals("WITHOUT_CLASSIFICATION")) {
                posInstances.add(commentLines.get(i));
                posNumber++;
            } else {
                negInstances.add(commentLines.get(i));
                negNumber++;
            }
        }

        int posSampleNumber = (int) (samplingNumber * posNumber / (posNumber + negNumber));
        int negSampleNumber = samplingNumber - posSampleNumber;

        //获取正负例采样内容
        List<String> posList = getSampleTextForReview(posInstances, posSampleNumber);
        List<String> negList = getSampleTextForReview(negInstances, negSampleNumber);
        List<String> combine = new ArrayList<>();
        for (String s : posList) combine.add(s);
        for (String s : negList) combine.add(s);

        // 一个索引数组 打乱顺序
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < samplingNumber; i++) list.add(i);

        Collections.shuffle(list, random);

        List<String> comments = new ArrayList<>();
        List<Integer> labels = new ArrayList<>();

        Iterator<Integer> it = list.iterator();
        while (it.hasNext()) {
            int index = it.next();
            comments.add(combine.get(index));
            if (index < posSampleNumber) labels.add(1);
            else labels.add(0);
        }


        StringBuilder commentText = new StringBuilder();
        StringBuilder labelText = new StringBuilder();
        for (int i = 0; i < samplingNumber; i++) {
            commentText.append(comments.get(i));
            labelText.append(labels.get(i)).append("\n");
        }

        //生成采样结果文件
        FileHandle.writeStringToFile(Settings.rootPath + "review.csv", commentText.toString());
        FileHandle.writeStringToFile(Settings.rootPath + "reviewLabe.csv", labelText.toString());

        System.out.println("Sampling " + posSampleNumber + " positive comments " + negSampleNumber + " negative comments.");
    }


    /**
     * 为实证研究 进行采样
     *
     * @param projectName 项目名
     * @param ratio       采样率
     */
    public static void samplingForStudy(String originPath, String projectName, double ratio) {
        List<String> lines = FileHandle.readFileToLines(originPath + projectName + ".txt");
        List<String> labelLines = FileHandle.readFileToLines(labelPath + "label--" + projectName + ".txt");
        List<String> posInstances = new ArrayList<>();
        List<String> negInstances = new ArrayList<>();

        //正负例数据总数目
        int posNumber = getInstanceNumber(labelLines, "positive");
        int negNumber = getInstanceNumber(labelLines, "negative");

        //填充正负例数据
        for (int i = 0; i < posNumber; i++) posInstances.add(lines.get(i));
        for (int i = posNumber; i < posNumber + negNumber; i++) negInstances.add(lines.get(i));

        //正负例采样数目
        int posSampleNumber = (int) (ratio * posInstances.size());
        int negSampleNumber = (int) (ratio * negInstances.size());

        //获取正负例采样内容
        String posSampleText = getSampleText(posInstances, posSampleNumber);
        String negSampleText = getSampleText(negInstances, negSampleNumber);

        //生成采样结果文件
        FileHandle.writeStringToFile(sampleRootPath + "positive/" + projectName + ".txt", posSampleText);
        FileHandle.writeStringToFile(sampleRootPath + "negative/" + projectName + ".txt", negSampleText);

        System.out.print("positive sample number: " + posSampleNumber + ",\t");
        System.out.print("negative sample number: " + negSampleNumber + ".\t");
        System.out.println("Sampling " + projectName + " has finished.");
    }

    /**
     * 获取某样例数目
     *
     * @param category 所属类别
     * @return
     */
    public static int getInstanceNumber(List<String> lines, String category) {
        int count = 0;
        for (String line : lines) if (line.equals(category)) count++;
        return count;
    }


    /**
     * 获取采样数据文本内容,采样不同的样例，如果遇到相同的样例则跳过重新进行采样
     *
     * @param instances
     * @param sampleNumber
     * @return
     */
    public static String getSampleText(List<String> instances, int sampleNumber) {
        StringBuilder text = new StringBuilder();
        Set<Integer> set = new HashSet<>();
        for (int i = 0; i < sampleNumber; ) {
            int randomNumber = (int) Math.round(random.nextDouble() * instances.size()); //产生一个随机数
            if (set.contains(randomNumber)) continue; //继续重新采样
            set.add(randomNumber);
            text.append(", ").append(instances.get(randomNumber)).append("\n");
            i++;
        }
        return text.toString();
    }

    /**
     * 获取采样数据文本内容,采样不同的样例，如果遇到相同的样例则跳过重新进行采样
     *
     * @param instances
     * @param sampleNumber
     * @return
     */
    public static List<String> getSampleTextForReview(List<String> instances, int sampleNumber) {
        List<String> list = new ArrayList<>();

        Set<Integer> set = new HashSet<>();
        for (int i = 0; i < sampleNumber; ) {
            int randomNumber = (int) Math.round(random.nextDouble() * instances.size()); //产生一个随机数
            if (set.contains(randomNumber)) continue; //继续重新采样
            set.add(randomNumber);
            StringBuilder text = new StringBuilder();
            text.append(", ").append(instances.get(randomNumber)).append("\n");
            list.add(text.toString());
            i++;
        }
        return list;
    }
}
