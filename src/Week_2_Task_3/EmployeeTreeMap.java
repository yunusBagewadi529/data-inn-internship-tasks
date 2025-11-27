package Week_2_Task_3;

import java.util.Map;
import java.util.TreeMap;

public class EmployeeTreeMap {
    public static void main(String[] args) {
        TreeMap<Integer, String> map = new TreeMap<>(Map.of(
                101, "Aditya Kamble",
                104, "Om Gurav",
                102, "Sangram Rajput",
                105, "Arpit Dhoke",
                103, "Saaquib Khan"
        ));

        System.out.println("\nEmp ID : Emp Name");
        for (Map.Entry<Integer, String> mapObj : map.entrySet()) {
            System.out.println(mapObj.getKey() + " :\t" + mapObj.getValue());
        }
    }
}
