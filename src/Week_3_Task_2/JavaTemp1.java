package Week_3_Task_2;

import java.io.FileReader;
import java.io.IOException;

public class JavaTemp1 {
    public static void main(String[] args) {
        try (FileReader fr = new FileReader("E:\\AAA-YUNUS_BSc\\TY\\TY-SEM-2\\Data-Inn-Internship\\Java-Project-Container\\" +
                "DataInn_Internship_Tasks\\src\\Week_3_Task_2\\name.txt")) {
            int ch;
            while ((ch = fr.read()) != -1) {
                System.out.print(ch + " -> ");
                System.out.println((char) ch);
            }
        } catch (IOException err) {
            err.printStackTrace();
        }
    }
}
