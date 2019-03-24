package mat;

import config.FileHandle;
import config.Settings;

import java.util.*;

/**
 * 对数据集进行采样研究
 */
public class Sampling {
    public static String sampleRootPath = "data/samples/";

    public static void main(String[] args) {

        for (int i = 0; i < Settings.projectNames.length; i++) {
            //"data/pattern/comment--"
            doSampling("data/mat/data--", Settings.projectNames[i], 0.1); //采样
        }
    }


    /**
     * 对项目注释进行采样
     *
     * @param projectName 项目名
     * @param ratio       采样率
     */
    public static void doSampling(String originPath, String projectName, double ratio) {
        List<String> lines = FileHandle.readFileToLines(originPath + projectName + ".txt");
        List<String> posInstances = new ArrayList<>();
        List<String> negInstances = new ArrayList<>();

        //正负例数据总数目
        int posNumber = getInstanceNumber(projectName, "positive");
        int negNumber = getInstanceNumber(projectName, "negative");

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
     * @param projectName 项目名称
     * @param category    所属类别
     * @return
     */
    public static int getInstanceNumber(String projectName, String category) {
        List<String> lines = FileHandle.readFileToLines("data/pattern/label--" + projectName + ".txt");
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
        String text = "";
        Random random = new Random(1);
        Set<Integer> set = new HashSet<>();
        for (int i = 0; i < sampleNumber; ) {
            int randomNumber = (int) Math.round(random.nextDouble() * instances.size()); //产生一个随机数
            if (set.contains(randomNumber)) continue; //继续重新采样
            set.add(randomNumber);
            text += randomNumber + ": " + instances.get(randomNumber) + "\n";
            i++;
        }
        return text;
    }
}
