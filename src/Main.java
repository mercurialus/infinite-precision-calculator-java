import arbitraryarithmetic.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.print("Enter string s1: ");
            String s1 = sc.next();
            System.out.print("Enter string s2: ");
            String s2 = sc.next();

            AInteger i1 = new AInteger(s1);
            AInteger i2 = new AInteger(s2);

            System.out.println("Multiplication: " + i1.multiply(i2));
            System.out.println("Addition: " + i1.add(i2));
            System.out.println("Subtraction: " + i1.subtract(i2));
            System.out.println("Divsion: " + i1.divide(i2));
        }
    }
}
