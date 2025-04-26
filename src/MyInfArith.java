import arbitraryarithmetic.AInteger;
import arbitraryarithmetic.AFloat;

public class MyInfArith {
    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Usage: java MyInfArith <int/float> <add/sub/mul/div> <operand1> <operand2>");
            return;
        }

        String type = args[0];
        String operation = args[1];
        String operand1 = args[2];
        String operand2 = args[3];

        try {
            if (type.equalsIgnoreCase("int")) {
                AInteger num1 = new AInteger(operand1);
                AInteger num2 = new AInteger(operand2);

                switch (operation.toLowerCase()) {
                    case "add":
                        System.out.println(num1.add(num2));
                        break;
                    case "sub":
                        System.out.println(num1.subtract(num2));
                        break;
                    case "mul":
                        System.out.println(num1.multiply(num2));
                        break;
                    case "div":
                        System.out.println(num1.divide(num2));
                        break;
                    default:
                        System.out.println("Invalid operation. Supported: add, sub, mul, div.");
                }

            } else if (type.equalsIgnoreCase("float")) {
                AFloat num1 = new AFloat(operand1);
                AFloat num2 = new AFloat(operand2);

                switch (operation.toLowerCase()) {
                    case "add":
                        System.out.println(num1.add(num2));
                        break;
                    case "sub":
                        System.out.println(num1.subtract(num2));
                        break;
                    case "mul":
                        System.out.println(num1.multiply(num2));
                        break;
                    case "div":
                        System.out.println(num1.divide(num2));
                        break;
                    default:
                        System.out.println("Invalid operation. Supported: add, sub, mul, div.");
                }
            } else {
                System.out.println("Invalid type. Supported types: int, float.");
            }
        } catch (ArithmeticException e) {
            System.out.println("Division by zero error");
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
        }
    }
}
