package arbitraryarithmetic;

import arbitraryarithmetic.AInteger;
import java.util.*;

//   Arbitrary-precision floating-point number that re-uses AInteger for the
//   un-scaled
//   mantissa and stores the scale (= number of decimal digits) separately.
//  
//   Value = unscaled × 10^{-scale}
//  
//   Base-10 000 representation is identical to AInteger’s implementation.
//   All public arithmetic methods return new AFloat objects and never mutate
//   the operands (functional style).
//  
//   Precision rule (project doc):
//   – keep all mathematically exact digits during computation
//   – when printed, truncate (NOT round) to max 30 fractional digits.

public class AFloat {

    private AInteger unscaled; // absolute value with no decimal point
    private int scale; // #digits right of decimal point
    private boolean isNegative; // sign bit

    /** 0.0 default */
    public AFloat() {
        this.unscaled = new AInteger(0);
        this.scale = 0;
        this.isNegative = false;
    }

    /** from string (examples: "-123.45", "9876", ".0012", "-.5") */
    public AFloat(String s) {
        if (s == null || s.isEmpty())
            throw new IllegalArgumentException("Empty string");

        // sign
        isNegative = s.charAt(0) == '-';
        int start = (s.charAt(0) == '+' || s.charAt(0) == '-') ? 1 : 0;

        // split integer / fractional parts
        int dot = s.indexOf('.', start);
        String intPart, fracPart;
        if (dot == -1) {
            intPart = s.substring(start);
            fracPart = "";
        } else {
            intPart = s.substring(start, dot);
            fracPart = s.substring(dot + 1);
        }
        if (intPart.isEmpty())
            intPart = "0";
        if (fracPart.isEmpty())
            fracPart = "0";

        // remove leading zeros in int part, trailing zeros in frac part
        intPart = intPart.replaceFirst("^0+(?!$)", "");
        fracPart = fracPart.replaceFirst("0+$", "");
        scale = fracPart.length();

        // concatenate to one big integer string
        String combined = intPart + fracPart;
        if (combined.isEmpty())
            combined = "0";

        unscaled = new AInteger(combined);
        if (unscaled.size() == 1 && unscaled.toString().equals("0"))
            isNegative = false; // -0 → +0
    }

    // from int (convenience)
    public AFloat(int n) {
        this.unscaled = new AInteger(Math.abs(n));
        this.scale = 0;
        this.isNegative = n < 0;
    }

    // copy
    public AFloat(AFloat other) {
        this.unscaled = new AInteger(other.unscaled);
        this.scale = other.scale;
        this.isNegative = other.isNegative;
    }

    // parse
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

    // returns a copy with its scale increased by `n` (multiplies unscaled by 10^n).
    private AFloat shiftScale(int n) {
        if (n == 0)
            return new AFloat(this);
        AFloat copy = new AFloat(this);
        copy.unscaled = copy.unscaled.multiply(pow10(n));
        copy.scale += n;
        return copy;
    }

    // compare absolute values ; reused in add/sub
    private static int compareAbs(AFloat a, AFloat b) {
        // align scales first
        int common = Math.max(a.scale, b.scale);
        AInteger ua = a.unscaled.multiply(pow10(common - a.scale));
        AInteger ub = b.unscaled.multiply(pow10(common - b.scale));
        return AInteger.compareAbsolute(ua, ub);
    }

    public AFloat add(AFloat other) {
        // Align the scales
        int common = Math.max(this.scale, other.scale);
        AInteger u1 = this.unscaled.multiply(pow10(common - this.scale));
        AInteger u2 = other.unscaled.multiply(pow10(common - other.scale));

        AInteger res;
        boolean neg;
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
        out.stripZeros();
        return out;
    }

    public AFloat subtract(AFloat other) {
        // a - b == a + (-b)
        AFloat negOther = new AFloat(other);
        negOther.isNegative = !other.isNegative;
        return this.add(negOther);
    }

    public AFloat multiply(AFloat other) {
        AFloat out = new AFloat();
        out.unscaled = this.unscaled.multiply(other.unscaled);
        out.scale = this.scale + other.scale;
        out.isNegative = this.isNegative ^ other.isNegative;
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
        out.isNegative = this.isNegative ^ other.isNegative;
        out.stripZeros();
        return out;
    }

    // remove trailing zeros in unscaled & adjust scale; also normalise -0 → +0
    private void stripZeros() {
        // remove trailing decimal zeros (i.e., factors of 10) from unscaled
        while (scale > 0) {
            // quick check: last chunk & possible power-of-10 in base10k
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
        // long division base-10 on base-10000 chunks
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
        String digits = unscaled.toString();
        // pad with leading zeros if digits shorter than scale
        if (scale >= digits.length()) {
            StringBuilder sb = new StringBuilder();
            if (isNegative)
                sb.append('-');
            sb.append("0.");
            for (int i = 0; i < scale - digits.length(); i++)
                sb.append('0');
            sb.append(digits);
            return trimFraction(sb.toString());
        } else if (scale == 0) {
            return (isNegative ? "-" : "") + digits;
        } else {
            int cut = digits.length() - scale;
            String intPart = digits.substring(0, cut);
            String fracPart = digits.substring(cut);
            String out = (isNegative ? "-" : "") + intPart + "." + fracPart;
            return trimFraction(out);
        }
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
