package main;

import main.methods.Method;
import others.FileHandle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Statistics {

    public static void evaluate(String methodName) {
        System.out.println("Method: " + methodName);
        StringBuilder text = new StringBuilder("TP, FN, FP, TN, P    , R    , F1   , ER   , RI\n");
        // 处理每个项目的结果
        for (String projectName : Settings.projectNames) {
            double tp = .0, fp = .0, tn = .0, fn = .0;
            String resultPath = Settings.rootPath + methodName + "/result--" + projectName + ".txt";
            String oraclePath = Settings.rootPath + "origin/label--" + projectName + ".txt";
            List<String> result = FileHandle.readFileToLines(resultPath);
            List<String> oracle = FileHandle.readFileToLines(oraclePath);
            for (int i = 1; i < result.size(); i++) {
                String label = oracle.get(i).trim(), prediction = result.get(i).trim();
                if (label.equals("positive") && prediction.equals("1")) tp++;
                if (label.equals("positive") && prediction.equals("0")) fn++;
                if (label.equals("negative") && prediction.equals("1")) fp++;
                if (label.equals("negative") && prediction.equals("0")) tn++;
            }
            // 准确度指标
            double precision = tp / (tp + fp);
            double recall = tp / (tp + fn);
            double f1 = 2 * precision * recall / (precision + recall);
            // 工作量感知指标
            double x = tp + fp, y = tp, n = tp + fn, N = tp + fp + fn + tn;
            double ER = (y * N - x * n) / (y * N);
            double RI = (y * N - x * n) / (x * n);

            text.append((int) tp).append(", ").append((int) fn).append(", ").append((int) fp).append(", ").append((int) tn).append(", ");

            text.append(String.format("%.3f", precision)).append(", ")
                    .append(String.format("%.3f", recall)).append(", ")
                    .append(String.format("%.3f", f1)).append(", ")
                    .append(String.format("%.3f", ER)).append(", ")
                    .append(String.format("%.3f", RI)).append("\n");
        }
        System.out.println(text.toString());
        FileHandle.writeStringToFile(Settings.rootPath + methodName + "/Evaluation_" + methodName + ".csv", text.toString());
    }


    public static void getVennDiagram(String label, String prediction) {

        int all_NLP = 0;
        int t = 0;
        for (String project : Settings.projectNames) {
            int NLP = 0, TM = 0, Easy = 0, MAT = 0;
            int NLP_TM = 0, NLP_Easy = 0, NLP_MAT = 0, TM_Easy = 0, TM_MAT = 0, Easy_MAT = 0;
            int NLP_TM_Easy = 0, NLP_TM_MAT = 0, NLP_Easy_MAT = 0, TM_Easy_MAT = 0;
            int NLP_TM_Easy_MAT = 0;
            int all_TP_TN = 0;


            //System.out.println("======================================================" + project);
            List<String> result = FileHandle.readFileToLines("result/predictions/" + project + ".csv");

            StringBuilder sNLP = new StringBuilder();
            StringBuilder sTM = new StringBuilder();
            StringBuilder sEasy = new StringBuilder();
            StringBuilder sMAT = new StringBuilder();
            for (int i = 1; i < result.size(); i++) {
                String[] temp = result.get(i).split(",");
                String vLabel = temp[0].trim();
                String vNLP = temp[2].trim();
                String vTM = temp[3].trim();
                String vEasy = temp[4].trim();
                String vMAT = temp[5].trim();
                // P N
                if (vLabel.equals(label)) {
                    //NLP预测成功的
                    if (vEasy.equals(prediction)) all_NLP++;


                    all_TP_TN++;
                    if (vNLP.equals(prediction)) sNLP.append(i).append("\n");
                    if (vTM.equals(prediction)) sTM.append(i).append("\n");
                    if (vEasy.equals(prediction)) sEasy.append(i).append("\n");
                    if (vMAT.equals(prediction)) sMAT.append(i).append("\n");


                    // 单个模型
                    if (vNLP.equals(prediction) && !vTM.equals(prediction) && !vEasy.equals(prediction) && !vMAT.equals(prediction))
                        NLP++;
                    if (!vNLP.equals(prediction) && vTM.equals(prediction) && !vEasy.equals(prediction) && !vMAT.equals(prediction))
                        TM++;
                    if (!vNLP.equals(prediction) && !vTM.equals(prediction) && vEasy.equals(prediction) && !vMAT.equals(prediction))
                        Easy++;
                    if (!vNLP.equals(prediction) && !vTM.equals(prediction) && !vEasy.equals(prediction) && vMAT.equals(prediction))
                        MAT++;

                    // 两个模型
                    if (vNLP.equals(prediction) && vTM.equals(prediction) && !vEasy.equals(prediction) && !vMAT.equals(prediction))
                        NLP_TM++;
                    if (vNLP.equals(prediction) && !vTM.equals(prediction) && vEasy.equals(prediction) && !vMAT.equals(prediction))
                        NLP_Easy++;
                    if (vNLP.equals(prediction) && !vTM.equals(prediction) && !vEasy.equals(prediction) && vMAT.equals(prediction))
                        NLP_MAT++;
                    if (!vNLP.equals(prediction) && vTM.equals(prediction) && vEasy.equals(prediction) && !vMAT.equals(prediction))
                        TM_Easy++;
                    if (!vNLP.equals(prediction) && vTM.equals(prediction) && !vEasy.equals(prediction) && vMAT.equals(prediction))
                        TM_MAT++;
                    if (!vNLP.equals(prediction) && !vTM.equals(prediction) && vEasy.equals(prediction) && vMAT.equals(prediction))
                        Easy_MAT++;

                    // 三个模型
                    if (vNLP.equals(prediction) && vTM.equals(prediction) && vEasy.equals(prediction) && !vMAT.equals(prediction))
                        NLP_TM_Easy++;
                    if (vNLP.equals(prediction) && vTM.equals(prediction) && !vEasy.equals(prediction) && vMAT.equals(prediction))
                        NLP_TM_MAT++;
                    if (vNLP.equals(prediction) && !vTM.equals(prediction) && vEasy.equals(prediction) && vMAT.equals(prediction))
                        NLP_Easy_MAT++;
                    if (!vNLP.equals(prediction) && vTM.equals(prediction) && vEasy.equals(prediction) && vMAT.equals(prediction))
                        TM_Easy_MAT++;

                    // 四个模型
                    if (vNLP.equals(prediction) && vTM.equals(prediction) && vEasy.equals(prediction) && vMAT.equals(prediction))
                        NLP_TM_Easy_MAT++;
                }
            }
            /*
            FileHandle.writeStringToFile("result/venn/" + project + "/" + label + "/NLP.csv", sNLP.toString());
            FileHandle.writeStringToFile("result/venn/" + project + "/" + label + "/TM.csv", sTM.toString());
            FileHandle.writeStringToFile("result/venn/" + project + "/" + label + "/Easy.csv", sEasy.toString());
            FileHandle.writeStringToFile("result/venn/" + project + "/" + label + "/MAT.csv", sMAT.toString());

            System.out.printf("Label: %s, Prediction: %s\n", label, prediction);
            System.out.println("NLP :    " + NLP);
            System.out.println("TM  :    " + TM);
            System.out.println("Easy:    " + Easy);
            System.out.println("MAT :    " + MAT);

            System.out.println("NLP , TM:   " + NLP_TM);
            System.out.println("NLP , Easy: " + NLP_Easy);
            System.out.println("NLP , MAT:  " + NLP_MAT);
            System.out.println("TM  , Easy: " + TM_Easy);
            System.out.println("TM  , MAT:  " + TM_MAT);
            System.out.println("Easy, MAT:  " + Easy_MAT);

            System.out.println("NLP, TM and Easy:   " + NLP_TM_Easy);
            System.out.println("NLP, TM and MAT:    " + NLP_TM_MAT);
            System.out.println("NLP, Easy and MAT:  " + NLP_Easy_MAT);
            System.out.println("TM , Easy and MAT:  " + TM_Easy_MAT);

            System.out.println("NLP, TM, Easy and MAT: " + NLP_TM_Easy_MAT);
            System.out.printf("all: %d\n\n", NLP + TM + Easy + MAT
                    + NLP_TM + NLP_Easy + NLP_MAT + +TM_Easy + TM_MAT + Easy_MAT
                    + NLP_TM_Easy + NLP_TM_MAT + NLP_Easy_MAT + TM_Easy_MAT
                    + NLP_TM_Easy_MAT);//*/
            double all = NLP + TM + Easy + MAT
                    + NLP_TM + NLP_Easy + NLP_MAT + +TM_Easy + TM_MAT + Easy_MAT
                    + NLP_TM_Easy + NLP_TM_MAT + NLP_Easy_MAT + TM_Easy_MAT
                    + NLP_TM_Easy_MAT;
            /*
            System.out.println(project + ", " + all + ", "
                    + NLP_TM_Easy_MAT + ", " + (NLP_TM_Easy_MAT / all) + ", "
                    + NLP + ", " + (NLP / all) + ", "
                    + TM + ", " + (TM / all) + ", "
                    + Easy + ", " + (Easy / all) + ", "
                    + MAT + ", " + (MAT / all));*/
            t += all;
        }
        System.out.println(t);
    }

    /**
     * Oracle : 0
     * Pattern: 1
     * NLP    : 2
     * TM     : 3
     * Easy   : 4
     * MAT    : 5
     * MAT_TM : 6
     * MAT_NLP: 7
     * Comments:8
     *
     * @param methodName 方法名
     */
    public static void showResult(String methodName) {
        String[] names = {"Oracle", "Pattern", "NLP", "TM", "Easy", "MAT", "MAT_TM", "MAT_NLP", "Comments"};
        System.out.println("Method: " + methodName);
        StringBuilder text = new StringBuilder("TP, FN, FP, TN, P    , R    , F1   , ER   , RI\n");
        // 处理每个项目的结果
        for (String projectName : Settings.projectNames) {
            double tp = .0, fp = .0, tn = .0, fn = .0;
            List<String> result = FileHandle.readFileToLines("result/predictions/" + projectName + ".csv");
            for (int i = 1; i < result.size(); i++) {
                String[] temp = result.get(i).split(",");
                String label = temp[0].trim(), prediction = temp[getIndex(names, methodName)].trim();
                if (label.equals("1") && prediction.equals("1")) tp++;
                if (label.equals("1") && prediction.equals("0")) fn++;
                if (label.equals("0") && prediction.equals("1")) fp++;
                if (label.equals("0") && prediction.equals("0")) tn++;
            }
            // 准确度指标
            double precision = tp / (tp + fp);
            double recall = tp / (tp + fn);
            double f1 = 2 * precision * recall / (precision + recall);
            // 工作量感知指标
            double x = tp + fp, y = tp, n = tp + fn, N = tp + fp + fn + tn;
            double ER = (y * N - x * n) / (y * N);
            double RI = (y * N - x * n) / (x * n);

            text.append((int) tp).append(", ").append((int) fn).append(", ").append((int) fp).append(", ").append((int) tn).append(", ");

            text.append(String.format("%.3f", precision)).append(", ")
                    .append(String.format("%.3f", recall)).append(", ")
                    .append(String.format("%.3f", f1)).append(", ")
                    .append(String.format("%.3f", ER)).append(", ")
                    .append(String.format("%.3f", RI)).append("\n");

        }
        System.out.println(text.toString());
        FileHandle.writeStringToFile("result/evaluation/" + methodName + ".csv", text.toString());
    }

    private static int getIndex(String[] names, String methodName) {
        int index = 0;
        for (; index < names.length; index++) if (names[index].equals(methodName)) return index;
        return 0;
    }

    /**
     * 将各个方法的结果整合到一起
     */
    public static void combineResult() {
        for (String project : Settings.projectNames) {
            System.out.println(project);
            // 标准结果
            List<String> commentLines = FileHandle.readFileToLines(Settings.rootPath + "origin/data--" + project + ".txt");
            List<String> labelLines = FileHandle.readFileToLines(Settings.rootPath + "origin/label--" + project + ".txt");
            // 单个结果
            List<String> PAT_Lines = FileHandle.readFileToLines(Settings.rootPath + "pattern/result--" + project + ".txt");
            List<String> NLP_Lines = FileHandle.readFileToLines(Settings.rootPath + "nlp/result--" + project + ".txt");
            List<String> T_M_Lines = FileHandle.readFileToLines(Settings.rootPath + "tm/result--" + project + ".txt");
            List<String> Esy_Lines = FileHandle.readFileToLines(Settings.rootPath + "easy/result--" + project + ".txt");
            List<String> MAT_Lines = FileHandle.readFileToLines(Settings.rootPath + "mat/result--" + project + ".txt");

            // 合并结果
            StringBuilder text = new StringBuilder("oracle, Pattern, NLP, TM, Easy, MAT, MAT_NLP, MAT_TM,Comments\n");
            for (int i = 0; i < commentLines.size(); i++) {
                text.append(labelLines.get(i).equals("positive") ? 1 : 0).append(",");    // Oracle
                text.append(PAT_Lines.get(i)).append(",");       // Pattern 结果
                text.append(NLP_Lines.get(i)).append(",");       // NLP     结果
                text.append(T_M_Lines.get(i)).append(",");       // TM      结果
                text.append(Esy_Lines.get(i)).append(",");       // Easy    结果
                text.append(MAT_Lines.get(i)).append(",");       // MAT     结果

                // NLP 和 MAT 组合结果
                text.append(MAT_Lines.get(i).equals("1") || NLP_Lines.get(i).equals("1") ? 1 : 0).append(",");
                // T M 和 MAT 组合结果
                text.append(MAT_Lines.get(i).equals("1") || T_M_Lines.get(i).equals("1") ? 1 : 0).append(",");

                // 注释
                text.append(commentLines.get(i)).append("\n");  // 注释
            }
            FileHandle.writeStringToFile("result/predictions/" + project + ".csv", text.toString());
        }
    }


    /**
     * 误分类情况统计
     */
    public static void misClassification() {
        for (String projectName : Settings.projectNames) {
            System.out.println(projectName);
            List<String> lines = FileHandle.readFileToLines("/result/" + projectName + ".csv");
            int count = 0;
            for (int i = 1; i < lines.size(); i++) {
                String[] splits = lines.get(i).split(",");
                String oracle = splits[0];
                String mat = splits[5];
                String comment = splits[7];
                //FN
                if (oracle.equals("1") && mat.equals("0")) {
                    System.out.printf("%s, %s, %s\n", oracle, mat, comment);
                    count++;
                }
            }
            System.out.println(count);
            //System.out.println(lines.size());
        }
    }


    /**
     * 计算MAT与每个baseline方法在每个项目上的列联矩阵
     *
     * @param methodName
     */
    public static void contingencyMatrix(String methodName, String labelString) {
        String[] names = {"Oracle", "Pattern", "NLP", "TM", "Easy", "MAT", "MAT_TM", "MAT_NLP", "Comments"};
        System.out.println("Method: " + methodName);
        StringBuilder text = new StringBuilder("library(exact2x2)\nvalue <- \"\"\n");
        // 处理每个项目的结果
        for (String projectName : Settings.projectNames) {

            int Ncc = 0, Ncw = 0, Nwc = 0, Nww = 0;
            List<String> result = FileHandle.readFileToLines("result/predictions/" + projectName + ".csv");
            for (int i = 1; i < result.size(); i++) {
                String[] temp = result.get(i).split(",");
                String label = temp[0].trim();
                String predictionOfMAT = temp[getIndex(names, "MAT")].trim();
                String predictionOfBaseline = temp[getIndex(names, methodName)].trim();
                if (!label.equals(labelString)) continue;
                if (predictionOfMAT.equals(label) && predictionOfBaseline.equals(label)) Ncc++;
                if (predictionOfMAT.equals(label) && !predictionOfBaseline.equals(label)) Ncw++;
                if (!predictionOfMAT.equals(label) && predictionOfBaseline.equals(label)) Nwc++;
                if (!predictionOfMAT.equals(label) && !predictionOfBaseline.equals(label)) Nww++;
            }
            text.append("d <- matrix( c(")
                    .append(Ncc).append(",")
                    .append(Nwc).append(",")
                    .append(Ncw).append(",")
                    .append(Nww).append("), 2, 2, dimnames=list(c(\"MAT\",\"")
                    .append(methodName).append("\"),c(\"True\",\"False\")))\n")
                    .append("r <- mcnemar.exact(d)\n")
                    .append("value <- paste(value, r[\"p.value\"], sep=\", \")\n");
            //System.out.println(Ncw + "," + Nwc + "," + (double) Ncw / Nwc);
            System.out.println(Ncc + ", " + Nwc + ", " + Ncw + ", " + Nww);
        }
        text.append("value\n");
        FileHandle.writeStringToFile("result/mcnemar/" + methodName + "_" + labelString + ".r", text.toString());
    }

    /**
     *
     */
    public static void conceptDrift_OTO(String methodName) {
        int[] total = {3052, 5426, 4090, 2585, 2492, 4644, 2494, 4148, 3652, 4473,
                1649, 3324, 4435, 29340, 1219, 15033, 7712, 3639, 12218, 2691};
        int[] P = {102, 969, 128, 74, 377, 195, 101, 282, 383, 201, 85,
                321, 249, 1046, 136, 618, 98, 92, 287, 63};

        int[] R0 = new int[Settings.projectNames.length];
        int[] N0 = new int[Settings.projectNames.length];


        String path = "result/oto/" + methodName + "/";
        //  添加表头
        StringBuilder text = new StringBuilder("Train project, ");
        for (int i = 0; i < 20; i++) text.append(Settings.projectNames[i]).append(" r0, n0, r1, n1, ");
        text.append("\n");

        // 提取r0 和 n0
        // 测试项目, 读取待测项目的数据，同一个测试项目的n1均一样
        for (int j = 0; j < 20; j++) {
            String testProject = Settings.projectNames[j];
            List<String> lines = FileHandle.readFileToLines(path + testProject + ".csv");
            // 添加内容 读取在不同的训练项目i下的结果 r1, n1
            for (int i = 1; i < lines.size(); i++) {
                String[] split = lines.get(i).split(", ");
                String trainProject = split[0].trim();
                if (!trainProject.equals(testProject)) continue;
                double precision = Double.parseDouble(split[1].trim());
                double recall = Double.parseDouble(split[2].trim());
                int tp = (int) (recall * P[j]);
                int fn = P[j] - tp;
                int fp = (int) (tp / precision) - tp;
                int tn = total[j] - P[j] - fp;
                int r0 = tp + tn;
                R0[j] = r0;
            }
        }
        for (int j = 0; j < 20; j++) N0[j] = total[j];


        // 提取r0, n0, r1 和 n1
        // 测试项目, 读取待测项目的数据，同一个测试项目的n1均一样
        for (int j = 0; j < 20; j++) {
            int n1 = total[j];
            String testProject = Settings.projectNames[j];
            text.append(testProject).append(", ");
            List<String> lines = FileHandle.readFileToLines(path + testProject + ".csv");

            // 添加内容 读取在不同的训练项目i下的结果 r1, n1
            for (int i = 1; i < lines.size(); i++) {
                String[] split = lines.get(i).split(", ");
                double precision = Double.parseDouble(split[1].trim());
                double recall = Double.parseDouble(split[2].trim());
                int tp = (int) (recall * P[j]);
                int fn = P[j] - tp;
                int fp = (int) (tp / precision) - tp;
                int tn = total[j] - P[j] - fp;
                int r1 = tp + tn;
                text.append(R0[i - 1]).append(", ");
                text.append(N0[i - 1]).append(", ");
                text.append(r1).append(", ");
                text.append(n1).append(", ");
            }
            text.append("\n");
        }

        FileHandle.writeStringToFile("result/oto/" + methodName + "/" + methodName + "_heatmap.csv", text.toString());
    }

    public static void conceptDrift_MTO(String methodName) {
        int[] total = {115264, 112890, 114226, 115731, 115824, 113672, 115822, 114168, 114664, 113843, 116667,
                114992, 113881, 88976, 117097, 103283, 110604, 114677, 106098, 115625,};
        int[] P = {5705, 4838, 5679, 5733, 5430, 5612, 5706, 5525, 5424, 5606, 5722,
                5486, 5558, 4761, 5671, 5189, 5709, 5715, 5520, 5744,};
        int[] R0 = new int[20];
        int[] R1 = new int[20];
        int[] N1 = {3052, 5426, 4090, 2585, 2492, 4644, 2494, 4148, 3652, 4473,
                1649, 3324, 4435, 29340, 1219, 15033, 7712, 3639, 12218, 2691};

        List<String> line0 = FileHandle.readFileToLines("result/oto/" + methodName + "/Self.csv");
        List<String> line1 = FileHandle.readFileToLines("result/evaluation/" + methodName + ".csv");
        for (int i = 1; i < line0.size(); i++) {
            String[] split = line0.get(i).split(",");
            double precision = Double.parseDouble(split[1].trim());
            double recall = Double.parseDouble(split[2].trim());
            int j = i - 1;
            int tp = (int) (recall * P[j]);
            int fn = P[j] - tp;
            int fp = (int) (tp / precision) - tp;
            int tn = total[j] - P[j] - fp;
            int r0 = tp + tn;
            R0[j] = r0;
        }

        for (int i = 1; i < line1.size(); i++) {
            String[] split = line1.get(i).split(", ");
            int tp = Integer.parseInt(split[0]);
            int tn = Integer.parseInt(split[3]);
            R1[i - 1] = tp + tn;
        }

        StringBuilder text = new StringBuilder("r0, n0, r1, n1\n");
        for (int i = 0; i < 20; i++) {
            text.append(R0[i]).append(", ");
            text.append(total[i]).append(", ");
            text.append(R1[i]).append(", ");
            text.append(N1[i]).append("\n");
        }
        System.out.println(text.toString());
    }


    public static void f1(int base) {
        List<String> lines = FileHandle.readFileToLines("data/cd.p.csv");

        List<Double> l1 = new ArrayList<>();
        for (int i = base; i < base + 20; i++) {
            String[] temp = lines.get(i).split(",");
            for (String t : temp) l1.add(Double.parseDouble(t));
        }

        int c_003 = 0;
        int c_05 = 0;
        int c_1 = 0;
        for (int i = 0; i < l1.size(); i++) {
            if (l1.get(i) < 0.003) c_003++;
            else if (l1.get(i) < 0.05) c_05++;
            else c_1++;
        }
        System.out.println(c_003 + ", " + c_05 + ", " + c_1);
    }


    public static void ratioOfMAT(String labelString) {
        String[] names = {"Oracle", "Pattern", "NLP", "TM", "Easy", "MAT", "MAT_TM", "MAT_NLP", "Comments"};
        StringBuilder text = new StringBuilder("Project, MAT v.s. NLP, , , MAT v.s. TM, , , MAT v.s. Easy, , ,\n");
        text.append(",V, Hit, Over, V, A, O, V, A, O \n");
        // 处理每个项目的结果
        for (String projectName : Settings.projectNames) {

            int NLP = 0, numOfNLP = 0, moreOfNLP = 0, TM = 0, numOfTM = 0, moreOfTM = 0, Easy = 0, numOfEasy = 0, moreOfEasy = 0;
            List<String> result = FileHandle.readFileToLines("result/predictions/" + projectName + ".csv");
            for (int i = 1; i < result.size(); i++) {
                String[] temp = result.get(i).split(",");
                String label = temp[0].trim();
                String predictionOfMAT = temp[getIndex(names, "MAT")].trim();
                String predictionOfNLP = temp[getIndex(names, "NLP")].trim();
                String predictionOfTM = temp[getIndex(names, "TM")].trim();
                String predictionOfEasy = temp[getIndex(names, "Easy")].trim();
                if (!label.equals(labelString)) continue;


                // MAT 与 baseline 的交集
                if (predictionOfMAT.equals(label) && predictionOfNLP.equals(label)) numOfNLP++;
                if (predictionOfMAT.equals(label) && predictionOfTM.equals(label)) numOfTM++;
                if (predictionOfMAT.equals(label) && predictionOfEasy.equals(label)) numOfEasy++;
                // baseline 的结果
                if (predictionOfNLP.equals(label)) NLP++;
                if (predictionOfTM.equals(label)) TM++;
                if (predictionOfEasy.equals(label)) Easy++;
                // MAT 超过Baseline的部分
                if (predictionOfMAT.equals(label) && !predictionOfNLP.equals(label)) moreOfNLP++;
                if (predictionOfMAT.equals(label) && !predictionOfTM.equals(label)) moreOfTM++;
                if (predictionOfMAT.equals(label) && !predictionOfEasy.equals(label)) moreOfEasy++;
            }
            text/*
                    .append(projectName).append(", ")
                    .append(NLP).append(", ")
                    .append(String.format("%.3f", numOfNLP / (double) NLP)).append(", ")
                    .append(String.format("%.3f", moreOfNLP / (double) NLP)).append(", ")
                    .append(TM).append(", ")
                    .append(String.format("%.3f", numOfTM / (double) TM)).append(", ")
                    .append(String.format("%.3f", moreOfTM / (double) TM)).append(", ")
                    .append(Easy).append(", ")
                    .append(String.format("%.3f", numOfEasy / (double) Easy)).append(", ")
                    .append(String.format("%.3f", moreOfEasy / (double) Easy)).append(", ")//*/


                    .append(NLP).append(", ")
                    .append(numOfNLP).append(", ")
                    .append(moreOfNLP).append(", ")
                    .append(TM).append(", ")
                    .append(numOfTM).append(", ")
                    .append(moreOfTM).append(", ")
                    .append(Easy).append(", ")
                    .append(numOfEasy).append(", ")
                    .append(moreOfEasy).append(", ")//*/
                    .append("\n");
        }
        System.out.println(text);
    }

    /**
     * 输出 P,R,
     * 输出Precision, Recall, F1, ER, RI
     */
    public static void transform(double[] P) {
        int tp = 0, tn = 0, fp = 0, fn = 0;


        // 准确度指标
        double precision = tp / (tp + fp);
        double recall = tp / (tp + fn);
        double f1 = 2 * precision * recall / (precision + recall);
        // 工作量感知指标
        double x = tp + fp, y = tp, n = tp + fn, N = tp + fp + fn + tn;
        double ER = (y * N - x * n) / (y * N);
        double RI = (y * N - x * n) / (x * n);

    }

    public static void main(String[] args) {
        /*
        f1(20 * 0);
        f1(20 * 1);
        f1(20 * 2);*/

        //ratioOfMAT("0");
        getVennDiagram("1", "1");

    }
}
