package arbitraryarithmetic;

import arbitraryarithmetic.AInteger;
import java.util.*;

@SuppressWarnings("unused") // Suppressing useless warnings
//
// I have written most of comments considering a different base
// So if i have written somewhere 'a digit' it highly points towards me using
// the base 10000
// therefore by a digit i mean -> 1234 is a digit, 123 is a digit
// 1 is a digit and 9999 is a digit too
// 10000 are two digits -> 1000 and 0
// I hope you get it :)
// For ease of implementation, we can just directly use AInteger
// class by scaling, and descaling the output :)
// Value = unscaled * 10^{-scale}
// Precision rule (project doc):
// – keep all mathematically exact digits during computation
// – when printed, truncate (NOT round) to max 30 fractional digits.

public class AFloat {

    private AInteger unscaled; // absolute value with no decimal point
    private int scale; // number of digits right of decimal point
    private boolean isNegative; // store the sign

    // 0.0 default
    public AFloat() {
        this.unscaled = new AInteger(0);
        this.scale = 0;
        this.isNegative = false;
    }

    // from string (examples: "-123.45", "9876", ".0012", "-.5")
    public AFloat(String s) {
        if (s == null || s.isEmpty()) // Empty string returns an exception
            throw new IllegalArgumentException("Empty string");

        // check for invalid characters
        if (!s.matches("^[+-]?\\d+(\\.\\d+)?$")) // regex to check for valid number
            throw new IllegalArgumentException("Invalid number format: " + s);

        // sign check
        isNegative = s.charAt(0) == '-';
        int start = (s.charAt(0) == '+' || s.charAt(0) == '-') ? 1 : 0;
        // Main implementation where string is parse after the sign
        // split integer / fractional parts
        int dot = s.indexOf('.', start);
        String intPart, fracPart;
        if (dot == -1) { // if no decimal, then fractional part doesn't exist
            intPart = s.substring(start);
            fracPart = "";
        } else { // if it does exist, then strat to dot is characterstic of the number and the
                 // remaining part is the mantissa
            intPart = s.substring(start, dot);
            fracPart = s.substring(dot + 1);
        }
        if (intPart.isEmpty())
            intPart = "0";
        if (fracPart.isEmpty())
            fracPart = "0";

        // remove leading zeros in int part, trailing zeros in frac part, using basic
        // string operations
        intPart = intPart.replaceFirst("^0+(?!$)", "");
        fracPart = fracPart.replaceFirst("0+$", "");
        scale = fracPart.length(); // scale of the number for example 0.001 has scale 3 since 0.001 = 1* 10^-3

        // concatenate to one big integer string
        String combined = intPart + fracPart;
        if (combined.isEmpty())
            combined = "0";

        unscaled = new AInteger(combined);
        if (unscaled.size() == 1 && unscaled.toString().equals("0"))
            isNegative = false; // -0 → +0
    }

    // Using AInteger -> makes life easy :)
    public AFloat(int n) {
        this.unscaled = new AInteger(Math.abs(n));
        this.scale = 0;
        this.isNegative = n < 0;
    }

    // Copy operator
    public AFloat(AFloat other) {
        this.unscaled = new AInteger(other.unscaled);
        this.scale = other.scale;
        this.isNegative = other.isNegative;
    }

    // parse function, everything is parsed in the constructor
    public static AFloat parse(String s) {
        return new AFloat(s);
    }

    // powerOfTen(n) --> 10^n as AInteger
    private static AInteger pow10(int n) {
        if (n <= 0)
            return new AInteger(1);
        // 10^n = (10^4)^{k} · 10^{r}
        int k = n / 4;
        int r = n % 4;
        AInteger ten4 = new AInteger(10000);
        AInteger res = new AInteger(1);
        for (int i = 0; i < k; i++)
            res = res.multiply(ten4);
        int small = (int) Math.pow(10, r);
        res = res.multiply(new AInteger(small));
        return res;
    }

    // Addition function for AFloat
    public AFloat add(AFloat other) {
        // Align the scales
        int common = Math.max(this.scale, other.scale);
        AInteger u1 = this.unscaled.multiply(pow10(common - this.scale)); // Converts it to AInteger by scaling it
        AInteger u2 = other.unscaled.multiply(pow10(common - other.scale)); // Converts it to AInteger by scaling it

        AInteger res; // Result
        boolean neg; // Neg or pos?
        if (this.isNegative == other.isNegative) { // same sign → add magnitudes
            res = u1.add(u2);
            neg = this.isNegative;
        } else { // different signs → subtract smaller from larger
            int cmp = AInteger.compareAbsolute(u1, u2);
            if (cmp >= 0) {
                res = u1.subtract(u2);
                neg = this.isNegative; // |u1| >= |u2|
            } else {
                res = u2.subtract(u1);
                neg = other.isNegative; // |u2| > |u1|
            }
        }
        AFloat out = new AFloat();
        out.unscaled = res;
        out.scale = common;
        out.isNegative = neg && !(res.size() == 1 && res.toString().equals("0"));
        out.stripZeros(); // Strip leading zeros
        return out;
    }

    // Subtract function, just use addition function by negating the second number
    public AFloat subtract(AFloat other) {
        // a - b == a + (-b)
        AFloat negOther = new AFloat(other);
        negOther.isNegative = !other.isNegative;
        return this.add(negOther);
    }

    // Multiply function
    public AFloat multiply(AFloat other) {
        AFloat out = new AFloat();
        out.unscaled = this.unscaled.multiply(other.unscaled); // Simply multiply the unscaled part
        out.scale = this.scale + other.scale; // Scale is added in multiplication x*10^a * y*10^b -> (x*y)* 10^(a+b),
                                              // the scales are a and b, and they are added in the product
        out.isNegative = (this.isNegative != other.isNegative); // IF same -> is not negative otherwise positive
        out.stripZeros();
        return out;
    }

    public AFloat divide(AFloat other) {
        if (other.unscaled.size() == 1 && other.unscaled.toString().equals("0"))
            throw new ArithmeticException("Division by zero");

        final int RESULT_SCALE = 30; // we need ≤30 fractional digits
        // Formula: (u1 / 10^s1) / (u2 / 10^s2)
        // = u1 * 10^{s2+RESULT_SCALE} / u2 * 10^{-(s1+RESULT_SCALE)}
        AInteger dividend = this.unscaled.multiply(pow10(other.scale + RESULT_SCALE));
        AInteger quotient = dividend.divide(other.unscaled);

        AFloat out = new AFloat();
        out.unscaled = quotient;
        out.scale = this.scale + RESULT_SCALE;
        out.isNegative = (this.isNegative != other.isNegative);// IF same -> is not negative otherwise positive
        out.stripZeros();
        return out;
    }

    // remove trailing zeros in unscaled and adjust scale; also normalise -0 → +0
    private void stripZeros() {
        // remove trailing decimal zeros (i.e., factors of 10) from unscaled
        while (scale > 0) {
            // quick check: last digit and possible power-of-10 in base10k
            if (unscaled.value.get(0) % 10 != 0)
                break;
            // divide by 10 by repeated subtraction of digits
            AInteger[] div10 = divMod10(unscaled);
            if (div10[1].size() == 1 && div10[1].toString().equals("0")) { // remainder 0
                unscaled = div10[0];
                scale--;
            } else
                break;
        }
        // if value is exactly 0 → sign = positive
        if (unscaled.size() == 1 && unscaled.toString().equals("0"))
            isNegative = false;
    }

    // divides an AInteger by 10, returns [quotient, remainder]
    private static AInteger[] divMod10(AInteger num) {
        // long division base-10 on base-10000 digits
        List<Integer> res = new ArrayList<>(Collections.nCopies(num.value.size(), 0));
        int rem = 0;
        for (int i = num.value.size() - 1; i >= 0; i--) {
            int cur = num.value.get(i) + rem * 10000;
            res.set(i, cur / 10);
            rem = cur % 10;
        }
        AInteger q = new AInteger();
        q.value = res;
        q.stripZeros();
        AInteger r = new AInteger(rem);
        return new AInteger[] { q, r };
    }

    @Override
    public String toString() {
        // Convert the absolute value to a decimal string
        String digits = unscaled.toString(); // unscaled value, no sign
        int len = digits.length();

        // Work out integer and fractional parts
        // Scale = how many digits belong after the decimal point
        int cut = Math.max(len - scale, 0); // index where fractional part starts
        String intPart = (cut == 0) ? "0" : digits.substring(0, cut);
        String fracPart = digits.substring(cut); // may be empty

        // Pad with leading zeros if we don’t have enough digits for the scale
        if (fracPart.length() < scale) {
            fracPart = "0".repeat(scale - fracPart.length()) + fracPart;
        }

        //
        // At this point fracPart has exactly `scale` digits.
        // Now force **exactly 30** digits after the decimal:
        // * pad with trailing zeros if scale < 30
        // * truncate if scale > 30 (no rounding)
        // * keep as-is if scale == 30
        //
        if (scale < 30) {
            fracPart = fracPart + "0".repeat(30 - scale);
        } else if (scale > 30) {
            fracPart = fracPart.substring(0, 30);
        }

        // Handle the special case “0.000…”
        // (unscaled could be zero even when scale > 0)
        boolean isZero = intPart.equals("0") && fracPart.chars().allMatch(ch -> ch == '0');

        // Assemble the final string
        StringBuilder sb = new StringBuilder(intPart.length() + 32);
        if (isNegative && !isZero)
            sb.append('-');
        sb.append(intPart).append('.').append(fracPart);
        return sb.toString();
    }

    // truncate / pad to ≤30 fractional digits, remove trailing zeros if needed
    private String trimFraction(String s) {
        int dot = s.indexOf('.');
        if (dot == -1)
            return s; // integer, nothing to trim
        String intP = s.substring(0, dot);
        String frac = s.substring(dot + 1);

        if (frac.length() > 30)
            frac = frac.substring(0, 30); // truncate
        frac = frac.replaceFirst("0+$", ""); // strip trailing zeros
        if (frac.isEmpty())
            return intP; // result is integer
        return intP + "." + frac;
    }
}
