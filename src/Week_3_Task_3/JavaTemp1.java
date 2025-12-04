package Week_3_Task_3;

import java.io.FileWriter;
import java.io.IOException;

public class JavaTemp1 {
    public static void main(String[] args) {

        try (FileWriter fw = new FileWriter("E:\\AAA-YUNUS_BSc\\TY\\TY-SEM-2\\Data-Inn-Internship\\" +
                "Java-Project-Container\\DataInn_Internship_Tasks\\src\\Week_3_Task_3\\log.txt", true)) {

            fw.write("Hello, my name is Yunus Bagewadi and I am learning Advance Java.\n");
            fw.write("Life is a journey of learning from out mistakes more, than our successes.\n");
//            fw.append("Life is a journey of learning from out mistakes more, than our successes.");

            System.out.println("File Writing Done!");

        } catch (IOException err) {
            err.printStackTrace();
        }

    }
}
