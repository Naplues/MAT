package dataset;


import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.Comment;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractComments {

    // methodPath
    // ---- GitRepo
    // ---- ---- Proj1
    // ---- ---- Proj2
    // ---- ---- ....
    // ---- Project
    // ---- ---- Proj1
    // ---- ---- Proj2
    // ---- ---- ....
    // ---- Comment
    // ---- ---- Proj1.csv
    // ---- ---- Proj2.csv
    // Default rootPath C:\Users\GZQ\Desktop\SATD\Dataset\
    // The file path of the program
    public static String rootPath = "C:\\Users\\GZQ\\Desktop\\SATD\\Dataset\\";
    // This directory stores the git repository of all projects. There is a subdirectory for each project.
    public static String gitrepoFolder = rootPath + "GitRepo";
    // This directory stores the java files of all projects. There is a subdirectory for each project.
    public static String projectFolder = rootPath + "Project";
    // This directory stores the comments list of all projects. There is a ".csv" format file for each project.
    public static String commentFolder = rootPath + "Comment";

    // Three types of comments
    public static final String COMMENT_LINE = "Line";
    public static final String COMMENT_BLOCK = "Block";
    public static final String COMMENT_DOC = "JavaDoc";

    // The regular expression of matching Java source code in a comment
    private static final String SOURCE_CODE_REGEX =
            "else\\s*\\{|"
                    + "try\\s*\\{|"
                    + "do\\s*\\{|"
                    + "finally\\s*\\{|"
                    + "if\\s*\\(|"
                    + "for\\s*\\(|"
                    + "while\\s*\\(|"
                    + "switch\\s*\\(|"
                    + "Long\\s*\\(|"
                    + "Byte\\s*\\(|"
                    + "Double\\s*\\(|"
                    + "Float\\s*\\(|"
                    + "Integer\\s*\\(|"
                    + "Short\\s*\\(|"
                    + "BigDecimal\\s*\\(|"
                    + "BigInteger\\s*\\(|"
                    + "Character\\s*\\(|"
                    + "Boolean\\s*\\(|"
                    + "String\\s*\\(|"
                    + "assert\\s*\\(|"
                    + "System\\.out.|"
                    + "public\\s*void|"
                    + "private\\s*static\\*final|"
                    + "catch\\s*\\(";

    /**
     * The entry of this program.
     *
     * @param args args
     */
    public static void main(String[] args) {
        System.out.println("Starting: ");
        // Delete existing folders
        //preProcess(projectFolder);
        //preProcess(commentFolder);

        // 1. Extract Java files from Git repository
        //extractJavaFile();
        // 2. Extract Comments from Java Files   !!! Only run once
        //extractComments();

        // 3. Manually label the comments...

        // 4. Ranking the result according to Labels !!! Only run once
        rankingComments();

        // 5. Manually check the labels

        // 6. Output the dataset
        exportDataset();

    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////  1. Extract JavaFiles  ///////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 提取所有项目文件,把项目源码文件提取到一个统一的项目路径下
     */
    public static void extractJavaFile() {
        File projectRootFile = new File(gitrepoFolder);
        File[] projectArray = projectRootFile.listFiles();

        if (projectArray != null) {
            for (int i = 0; i < projectArray.length; i++) {
                //单个项目源码文件的路径
                System.out.println(projectArray[i].getName());
                String eachProjectDstPath = projectFolder + File.separator + projectArray[i].getName();
                preProcess(eachProjectDstPath);
                // extract java file
                extractSingleJavaFile(projectArray[i].getPath(), eachProjectDstPath);
                System.out.println((i + 1) + ": " + projectArray[i].getName());
            }
        }
        System.out.println("Java Files extract over");
    }

    /**
     * 提取单个项目中的文件并复制Java文件
     *
     * @param sourceDir source dir
     * @param destDir   destination dir
     */
    public static void extractSingleJavaFile(String sourceDir, String destDir) {
        List<String> fileList = new ArrayList<>();
        findJavaFile(sourceDir, fileList); // Find Java files
        // Process each file
        for (String file : fileList) {
            try {
                File cf = new File(file);
                copyFile(file, destDir + File.separator + cf.getName());
            } catch (Exception e) {
                System.out.println("Error when coping single file");
                e.printStackTrace();
            }
        }
    }

    /**
     * 递归寻找指定路径下的java文件
     *
     * @param sourceDir 指定的目录
     * @param result    结果
     */
    public static void findJavaFile(String sourceDir, List<String> result) {
        File[] array = new File(sourceDir).listFiles();
        if (array != null) {
            for (int i = 0; i < array.length; i++) {
                //元素类型是文件
                if (array[i].isFile()) {
                    String fileName = array[i].getName();
                    //后缀名为java则加入到list中
                    String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
                    if (suffix.equals("java")) result.add(array[i].getPath());

                } else if (array[i] instanceof File) {
                    //递归寻找
                    findJavaFile(array[i].getPath(), result);
                }
            }
        }
    }

    /**
     * 复制文件
     *
     * @param oldFilePath
     * @param newFilePath
     */
    public static void copyFile(String oldFilePath, String newFilePath) {
        try {
            File oldFile = new File(oldFilePath);
            File newFile = new File(newFilePath);
            BufferedWriter bw = new BufferedWriter(new FileWriter(newFile));
            BufferedReader br = new BufferedReader(new FileReader(oldFile));
            String line = br.readLine();
            while (line != null) {
                bw.write(line);
                bw.write("\n");
                line = br.readLine();
            }
            bw.flush();
            bw.close();
            br.close();
        } catch (Exception e) {
            System.out.println("Error when coping single file");
            e.printStackTrace();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////  2. Extract Comments  ////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Extracting and filtering comments according to five heuristics
     * Heuristics 1: License comments
     * Heuristics 2: Long comments
     * Heuristics 3: Commented source codes
     * Heuristics 4: Automatically generated comments
     * Heuristics 5: Java Doc Comments
     */
    public static void extractComments() {
        File[] projects = new File(projectFolder).listFiles();
        if (projects == null) return;
        int allNumber = 0;
        for (File project : projects) {
            File[] files = new File(project.getPath()).listFiles();
            if (files == null) return;
            int total = 0;
            Set<String> duplicatedComments = new HashSet<>();
            for (File file : files) {
                // Catch the exception of paring Java file
                try {
                    // Process the comments in each file
                    String lastLineComment = ""; // The content of last Line Comment (may be connected by multiple lines)
                    int lastLineNumber = -1;     // The line number of last Line Comment
                    int currLineNumber;
                    CompilationUnit compilationUnit = StaticJavaParser.parse(file);
                    List<Comment> comments = compilationUnit.getComments();

                    for (Comment comment : comments) {
                        // Heuristics 3: Commented source codes
                        Pattern pattern = Pattern.compile(SOURCE_CODE_REGEX);
                        Matcher matcher = pattern.matcher(comment.getContent());
                        if (matcher.find()) continue;

                        // Heuristics 4: Automatically generated comments
                        if (comment.getContent().contains("Auto-generated constructor stub")
                                || comment.getContent().contains("Auto-generated method stub")
                                || comment.getContent().contains("Auto-generated catch block")
                            // || comment.getContent().contains("(non-Javadoc)")
                        ) continue;

                        ///////////////////////////////////////////////// Processing Line Comment //////////////////////
                        if (comment.isLineComment()) {
                            currLineNumber = comment.getBegin().get().line; // The line number of current comment

                            // Heuristics 2: Long comments
                            if (currLineNumber != lastLineNumber + 1) {
                                // not a single line comment
                                // 存储上一个单行注释 并且将当前注释加入缓冲区
                                if (!lastLineComment.equals("")) duplicatedComments.add(lastLineComment);
                                lastLineComment = COMMENT_LINE + ", // " + comment.getContent() + " ";
                            } else {
                                // is a single line comment
                                // 将该注释续在上一注释的后面, 不存储当前注释
                                lastLineComment += " " + comment.getContent() + " ";
                            }
                            lastLineNumber = currLineNumber; // Using current line number to update last line number
                        }

                        /////////////////////////////////////////////// Processing Block Comment ///////////////////////
                        if (comment.isBlockComment()) {
                            // Heuristics 1: License comments
                            if (comment.getContent().contains("license")
                                    || comment.getContent().contains("License")
                                    || comment.getContent().contains("LICENSE")) continue;

                            duplicatedComments.add(COMMENT_BLOCK + ", /* " + comment.getContent().replace("\n", "") + " */");
                        }

                        /////////////////////////////////////////////// Processing Java Doc Comment ////////////////////
                        if (comment.isJavadocComment()) {
                            // Heuristics 5: Java Doc Comments
                            if (!comment.getContent().contains("TODO")
                                    && !comment.getContent().contains("FIXME")
                                    && !comment.getContent().contains("XXX")
                                    && !comment.getContent().contains("todo")
                                    && !comment.getContent().contains("fixme")
                                    && !comment.getContent().contains("xxx")) continue;
                            duplicatedComments.add(COMMENT_DOC + ", /* " + comment.getContent().replace("\n", "") + " */");
                        }

                    }// End for process each file
                    duplicatedComments.add(lastLineComment); // add last line comment

                    total += comments.size();
                } catch (Exception e) {
                }
            }

            // Output the comments to a file
            StringBuilder text = new StringBuilder("Label, Type, Content\n"); //"Label, Type, Content\n"
            int afterFiltering = 0;
            for (String line : duplicatedComments) {
                if (line.trim().equals(", Line, //") || line.trim().equals("")) continue;
                text.append(", ").append(line).append("\n");
                afterFiltering++;
            }
            allNumber += afterFiltering;
            writeStringToFile(commentFolder, project.getName() + ".csv", text.toString());
            System.out.println(project.getName() + "\t" + files.length + " files,\tTotal: " + total + ",\tAfter Filtering: " + afterFiltering);
        } // End for each project

        System.out.println("Extract Finish. Total " + allNumber);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////  2. Ranking Comments  ////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void rankingComments() {
        String labeledPath = "data/new/labeled/";
        String rankedPath = "data/new/ranked/";
        File[] projects = new File(labeledPath).listFiles();
        for (File project : projects) {
            StringBuilder comment_0 = new StringBuilder();
            StringBuilder comment_1 = new StringBuilder();
            StringBuilder comment_2 = new StringBuilder();

            List<String> lines = readFileToLines(project.getPath());
            for (int i = 1; i < lines.size(); i++) {
                String[] temp = lines.get(i).split(",");
                if (temp.length < 3) continue;
                if (temp[0].equals("0")) comment_0.append(lines.get(i)).append("\n");
                if (temp[0].equals("1")) comment_1.append(lines.get(i)).append("\n");
                if (temp[0].equals("2")) comment_2.append(lines.get(i)).append("\n");
            }
            writeStringToFile(rankedPath, project.getName(), comment_1.toString() + comment_2.toString() + comment_0.toString());
        }
        System.out.println("Ranked finish!");
    }

    /**
     * 将标好的数据导出
     * comments
     * projects
     * labels
     */
    public static void exportDataset() {
        String rankedPath = "data/new/ranked/";
        String originPath = "data/new/origin/";
        File[] projects = new File(rankedPath).listFiles();

        StringBuilder comments = new StringBuilder();
        StringBuilder projectNames = new StringBuilder();
        StringBuilder labels = new StringBuilder();

        for (File project : projects) {
            List<String> lines = readFileToLines(project.getPath());
            for (int i = 1; i < lines.size(); i++) {
                String[] temp = lines.get(i).split(",");
                if (temp.length < 3) continue;

                projectNames.append(project.getName().split("-")[0]).append("\n");

                comments.append(lines.get(i).replace(temp[0] + "," + temp[1] + ", ", "")).append("\n");

                if (temp[0].equals("1")) labels.append("SATD");
                else labels.append("WITHOUT_CLASSIFICATION");
                labels.append("\n");
            }
        }
        writeStringToFile(originPath, "projects", projectNames.toString());
        writeStringToFile(originPath, "comments", comments.toString());
        writeStringToFile(originPath, "labels", labels.toString());

        System.out.println("Export success!");
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////  File IO Process Methods  //////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 读取文件返回一个字符串列表
     * 列表元素为文件每一行的内容
     *
     * @param filePath
     * @return
     */
    public static List<String> readFileToLines(String filePath, boolean... args) {
        BufferedReader reader;
        if (args.length > 0 && args[0])
            reader = getExternalPath(filePath); // 读取文件系统路径
        else
            reader = getActualPath(filePath); // 默认读取实际路径
        List<String> lines = new ArrayList<>();
        try {
            String s;
            while ((s = reader.readLine()) != null) lines.add(s);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    /**
     * 将字符串写入文件，false表示覆盖
     *
     * @param folderPath
     * @param data
     */
    public static void writeStringToFile(String folderPath, String fileName, String data, boolean... a) {
        try {
            // true = append file
            File folder = new File(folderPath + File.separator + fileName);
            if (!folder.exists()) folder.createNewFile();

            boolean append = false;
            if (a.length == 1) append = a[0];
            FileWriter fileWriter = new FileWriter(folder, append);
            BufferedWriter bufferWriter = new BufferedWriter(fileWriter);
            bufferWriter.write(data);
            bufferWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取实际的文件路径
     *
     * @param path
     * @return
     */
    public static BufferedReader getActualPath(String path) {
        try {
            return new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取参数文件
     *
     * @param path
     * @return
     */
    public static BufferedReader getExternalPath(String path) {
        try {
            return new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 对目标文件夹进行预处理，如果存在文件夹，则删除该文件夹及其中的内容，然后重新创建该文件夹
     *
     * @param folder
     */
    public static void preProcess(String folder) {
        File dir = new File(folder);
        if (dir.exists()) deleteFolder(folder);
        dir.mkdirs();
    }

    /**
     * 删除文件夹目录下的所有文件和子文件夹
     *
     * @param folder
     */
    public static void deleteFolder(String folder) {
        File file = new File(folder);
        if (file.isFile()) file.delete();
        else {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) deleteFolder(files[i].getAbsolutePath());
            file.delete();
        }
    }
}
