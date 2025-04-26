package arbitraryarithmetic;

import java.util.*;

public class AInteger {
    private List<Integer> value = new ArrayList<>();
    private boolean isNegative = false;

    public AInteger() {
    }

    public AInteger(String number) {
        isNegative = !number.isEmpty() && number.charAt(0) == '-';
        int start = isNegative ? 1 : 0;

        for (int i = number.length(); i > start; i -= 4) {
            int end = i;
            int begin = Math.max(start, i - 4);
            String segment = number.substring(begin, end);
            value.add(Integer.parseInt(segment));
        }

        stripZeros();
    }

    public AInteger(int number) {
        isNegative = number < 0;
        number = Math.abs(number);

        while (number > 0) {
            value.add(number % 10000);
            number /= 10000;
        }

        if (value.isEmpty())
            value.add(0);
    }

    private void stripZeros() {
        while (value.size() > 1 && value.get(value.size() - 1) == 0) {
            value.remove(value.size() - 1);
        }

        if (value.size() == 1 && value.get(0) == 0)
            isNegative = false;
    }

    private static int compareAbsolute(AInteger a, AInteger b) {
        if (a.value.size() != b.value.size())
            return Integer.compare(a.value.size(), b.value.size());

        for (int i = a.value.size() - 1; i >= 0; i--) {
            int cmp = Integer.compare(a.value.get(i), b.value.get(i));
            if (cmp != 0)
                return cmp;
        }
        return 0;
    }

    private static AInteger addAbsolute(AInteger a, AInteger b) {
        AInteger result = new AInteger();
        result.value.clear();

        int carry = 0;
        for (int i = 0; i < Math.max(a.value.size(), b.value.size()) || carry != 0; i++) {
            int aVal = i < a.value.size() ? a.value.get(i) : 0;
            int bVal = i < b.value.size() ? b.value.get(i) : 0;

            int sum = aVal + bVal + carry;
            result.value.add(sum % 10000);
            carry = sum / 10000;
        }

        return result;
    }

    private static AInteger subAbsolute(AInteger a, AInteger b) {
        AInteger result = new AInteger();
        result.value.clear();

        int borrow = 0;
        for (int i = 0; i < a.value.size(); i++) {
            int aVal = a.value.get(i);
            int bVal = i < b.value.size() ? b.value.get(i) : 0;

            int diff = aVal - bVal - borrow;
            if (diff < 0) {
                diff += 10000;
                borrow = 1;
            } else {
                borrow = 0;
            }
            result.value.add(diff);
        }

        result.stripZeros();
        return result;
    }

    public AInteger add(AInteger other) {
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

    public AInteger subtract(AInteger other) {
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

    public AInteger multiply(AInteger other) {
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

    public String toString() {
        if (value.isEmpty())
            return "0";

        StringBuilder sb = new StringBuilder();
        if (isNegative)
            sb.append('-');

        sb.append(value.get(value.size() - 1));
        for (int i = value.size() - 2; i >= 0; i--) {
            sb.append(String.format("%04d", value.get(i)));
        }

        return sb.toString();
    }

    public int size() {
        return value.size();
    }
}
