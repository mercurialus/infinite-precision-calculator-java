package arbitraryarithmetic;

import java.util.*;

// I have written most of comments considering a different base
// So if i have written somewhere 'a digit' it highly points towards me using the base 10000
// therefore by a digit i mean -> 1234 is a digit, 123 is a digit
// 1 is a digit and 9999 is a digit too
// 10000 are two digits -> 1000 and 0
// I hope you get it :)
public class AInteger {
    // Storing the Integer in base10000, Least significant chunks first
    List<Integer> value = new ArrayList<>();
    // boolean for negative numbers, true -> Negative else positive
    boolean isNegative = false;

    // Default constructor, initialize to 0
    public AInteger() {
        value.add(0); // Add 0 to the list because the number is 0
    }

    // String -> AInteger Constructor
    public AInteger(String number) {
        if (number == null || number.isEmpty()) {
            throw new IllegalArgumentException("Invalid input string"); // If the input is empty or null, throw an error
        }

        isNegative = number.charAt(0) == '-'; // Check if the number starts with '-' to know if it's negative
        int start = isNegative ? 1 : 0; // If negative, start reading from the second character

        // Read the number backwards, in chunks of 4 digits
        for (int i = number.length(); i > start; i -= 4) {
            int end = i; // End of the chunk
            int begin = Math.max(start, i - 4); // Start of the chunk (or the beginning of the number)
            String segment = number.substring(begin, end); // Get the chunk
            value.add(Integer.parseInt(segment)); // Convert the chunk to an integer and add it to the list
        }

        stripZeros(); // Remove unnecessary leading zeros
    }

    // Int -> AInteger Constructor
    public AInteger(int number) {
        isNegative = number < 0; // If the number is less than 0, it's negative
        number = Math.abs(number); // Make the number positive for processing

        if (number == 0) {
            value.add(0); // If the number is 0, just add 0 to the list
        } else {
            while (number > 0) {
                value.add(number % 10000); // Add the last 4 digits of the number to the list
                number /= 10000; // Remove the last 4 digits from the number
            }
        }
    }

    // Copy constructor
    public AInteger(AInteger other) {
        this.isNegative = other.isNegative; // Copy the sign
        this.value = new ArrayList<>(other.value); // Copy the list of chunks
    }

    // parse method
    public static AInteger parse(String s) {
        return new AInteger(s); // Create a new AInteger from the string
    }

    // Strip starting zeros
    void stripZeros() {
        // Remove all leading zeros except the last one
        while (value.size() > 1 && value.get(value.size() - 1) == 0) {
            value.remove(value.size() - 1); // Remove the last element if it's 0
        }

        if (value.size() == 1 && value.get(0) == 0)
            isNegative = false; // If the number is 0, make sure it's not negative
    }

    // Absolute comparison
    static int compareAbsolute(AInteger a, AInteger b) {
        // Compare the size of the numbers first
        if (a.value.size() != b.value.size())
            return Integer.compare(a.value.size(), b.value.size());

        // If sizes are the same, compare each chunk from the most significant to the
        // least
        for (int i = a.value.size() - 1; i >= 0; i--) {
            int cmp = Integer.compare(a.value.get(i), b.value.get(i));
            if (cmp != 0)
                return cmp; // Return as soon as a difference is found
        }
        return 0; // If all chunks are the same, the numbers are equal
    }

    // Addition for two positive numbers
    private static AInteger addAbsolute(AInteger a, AInteger b) {
        AInteger result = new AInteger(); // Create a new AInteger to store the result
        result.value.clear(); // Clear the default 0 value

        int carry = 0; // Carry for addition
        for (int i = 0; i < Math.max(a.value.size(), b.value.size()) || carry != 0; i++) {
            int aVal = i < a.value.size() ? a.value.get(i) : 0; // Get the chunk from 'a' or 0 if out of bounds
            int bVal = i < b.value.size() ? b.value.get(i) : 0; // Get the chunk from 'b' or 0 if out of bounds

            int sum = aVal + bVal + carry; // Add the chunks and the carry
            result.value.add(sum % 10000); // Add the last 4 digits of the sum to the result
            carry = sum / 10000; // Update the carry
        }

        return result; // Return the result
    }

    // Subtract for two positive numbers
    private static AInteger subAbsolute(AInteger a, AInteger b) {
        AInteger result = new AInteger(); // Create a new AInteger to store the result
        result.value.clear(); // Clear the default 0 value

        int borrow = 0; // Borrow for subtraction
        for (int i = 0; i < a.value.size(); i++) {
            int aVal = a.value.get(i); // Get the chunk from 'a'
            int bVal = i < b.value.size() ? b.value.get(i) : 0; // Get the chunk from 'b' or 0 if out of bounds

            int diff = aVal - bVal - borrow; // Subtract the chunks and the borrow
            if (diff < 0) {
                diff += 10000; // If the result is negative, borrow from the next chunk
                borrow = 1; // Set the borrow flag
            } else {
                borrow = 0; // Reset the borrow flag
            }
            result.value.add(diff); // Add the result of the subtraction to the result
        }

        result.stripZeros(); // Remove unnecessary leading zeros
        return result; // Return the result
    }

    // Addition for general (either neg or pos) numbers
    public AInteger add(AInteger other) {
        // If both have same sign -> simply add both
        // If they have different sign -> Just subtract them, and sign will
        // remain same as sign of the larger number.
        if (isNegative == other.isNegative) {
            AInteger result = addAbsolute(this, other);
            result.isNegative = this.isNegative;
            return result;
        } else {
            if (compareAbsolute(this, other) >= 0) {
                AInteger result = subAbsolute(this, other);
                result.isNegative = this.isNegative;
                return result;
            } else {
                AInteger result = subAbsolute(other, this);
                result.isNegative = other.isNegative;
                return result;
            }
        }
    }

    // subtraction for general (either neg or pos) numbers
    public AInteger subtract(AInteger other) {
        // If both have same sign -> simply subtract both
        // If they have different sign -> Just add them,
        // and sign will change accordingly
        // Example : 5 - 10 -> simply subtract 10 from 5 -> -5
        // 5 - -10 -> add both -> 15, for sign if first positive, second negative ->
        // positive
        // -5 - -10 -> have to check sign in this case
        if (isNegative != other.isNegative) {
            AInteger result = addAbsolute(this, other);
            result.isNegative = this.isNegative;
            return result;
        } else {
            if (compareAbsolute(this, other) >= 0) {
                AInteger result = subAbsolute(this, other);
                result.isNegative = this.isNegative;
                return result;
            } else {
                AInteger result = subAbsolute(other, this);
                result.isNegative = !this.isNegative;
                return result;
            }
        }
    }

    // Multiplication for general (either neg or pos) numbers
    public AInteger multiply(AInteger other) {
        // Simple multiplication as taught in school
        AInteger result = new AInteger();
        result.value = new ArrayList<>(Collections.nCopies(this.value.size() + other.value.size(), 0));
        result.isNegative = this.isNegative != other.isNegative;

        for (int i = 0; i < this.value.size(); i++) {
            long carry = 0;
            for (int j = 0; j < other.value.size() || carry != 0; j++) {
                long curr = result.value.get(i + j) + carry;
                if (j < other.value.size())
                    curr += (long) this.value.get(i) * other.value.get(j);

                result.value.set(i + j, (int) (curr % 10000));
                carry = curr / 10000;
            }
        }

        result.stripZeros();
        return result;
    }

    // Integer division for general (either neg or pos) numbers
    // division: integer quotient only, no remainder, no floats
    public AInteger divide(AInteger other) {
        // don't allow division by zero
        if (other.value.size() == 1 && other.value.get(0) == 0) {
            throw new ArithmeticException("Division by zero");
        }

        // work with absolute values, fix sign at the end
        AInteger dividend = new AInteger(this);
        dividend.isNegative = false;
        AInteger divisor = new AInteger(other);
        divisor.isNegative = false;

        // if dividend < divisor, quotient is 0
        if (compareAbsolute(dividend, divisor) < 0) {
            return new AInteger(0);
        }

        List<Integer> quotMsf = new ArrayList<>(); // hold quotient digits MS-first
        AInteger current = new AInteger();
        current.value.clear(); // start with zero

        // long division: bring down chunks from highest to lowest
        for (int i = dividend.value.size() - 1; i >= 0; i--) {
            // shift current left by one chunk, then add next chunk
            current.value.add(0, dividend.value.get(i));
            current.stripZeros();

            // find the largest x in 0..9999 so that divisor * x <= current
            int low = 0, high = 9999, best = 0;
            while (low <= high) {
                int mid = (low + high) >>> 1;
                AInteger prod = divisor.multiply(new AInteger(mid));
                if (compareAbsolute(prod, current) <= 0) {
                    best = mid;
                    low = mid + 1;
                } else {
                    high = mid - 1;
                }
            }
            quotMsf.add(best);

            // subtract divisor * best from current
            current = current.subtract(divisor.multiply(new AInteger(best)));
        }

        // convert MS-first to our storage (LS-first)
        Collections.reverse(quotMsf);
        AInteger result = new AInteger();
        result.value = quotMsf;
        result.stripZeros();

        // sign of quotient is XOR of operand signs
        result.isNegative = this.isNegative ^ other.isNegative;
        return result;
    }

    // override toString() method to print the number properly
    @Override
    public String toString() {
        if (value.isEmpty())
            return "0"; // Empty number -> that means zero , just return zero

        StringBuilder sb = new StringBuilder();
        if (isNegative)
            sb.append('-'); // If negative, append '-' while printing

        sb.append(value.get(value.size() - 1)); // Most Significant digit
        for (int i = value.size() - 2; i >= 0; i--) {
            sb.append(String.format("%04d", value.get(i))); // Pad with leading zeros
        }

        return sb.toString();
    }

    // Returns number of digits (this is for base 10000)
    // which means 1234 has size 1
    // 12345678 and 1234567 has size 2
    public int size() {
        return value.size();
    }
}