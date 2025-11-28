// Student roll number & name lookup

package Week_2_Task_4;

import java.util.HashMap;
import java.util.Scanner;

public class StudentInfoLookup {
    public static void main(String[] args) {
        HashMap<Integer, String> studentRegister = new HashMap<>();
        Scanner sc = new Scanner(System.in);

        studentRegister.put(101, "Aditya Kamble");
        studentRegister.put(102, "Sangram Rajput");
        studentRegister.put(103, "Saaquib Khan");
        studentRegister.put(104, "Om Gurav");
        studentRegister.put(105, "Arpit Dhoke");

        System.out.println("\nStudent Roll No : Student Name");
        for (HashMap.Entry<Integer, String> studentObj : studentRegister.entrySet()) {
            System.out.println(studentObj.getKey() + " :\t" + studentObj.getValue());
        }

        System.out.print("\nEnter Roll No to search Student Name: ");
        int rollNo = sc.nextInt();

        if (studentRegister.containsKey(rollNo)) {
            System.out.println("Student Name: " + studentRegister.get(rollNo));
        } else {
            System.out.println("Roll No " + rollNo + " not found in the register.");
        }

        sc.close();
    }
}