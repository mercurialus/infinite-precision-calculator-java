package arbitraryarithmetic;

import java.util.*;

public class AInteger{
    private List<Integer> digits;
    private boolean isNegative;

    public AInteger() {
        this.digits = new ArrayList<>();
        this.digits.add(0);
        this.isNegative= false;
    }

    public AInteger(String s){
        parseFromString(s);
    }

    public AInteger(AInteger other){
        this.digits = new ArrayList<>(other.digits);
        this.isNegative=other.isNegative;
    }

    public static AInteger parse(String s){
        return new AInteger(s);
    }

    private void parseFromString(String s){
        digits = new ArrayList<>();
        isNegative = false;

        if(s==null || s.isEmpty())
        {
            digits.add(0);
            return ;
        }

        if (s.charAt(0)=='-'){
            isNegative = true;
            s=s.substring(1);
        }

        for(int i = s.length()-1;i>=0;i--)
        {
            char ch = s.charAt(i);
            if(Character.isDigit(ch)) digits.add(ch-'0');
            else throw new IllegalArgumentException("Invalid digit in number: " + ch);
        }
        stripZeros();
    } 

    private void stripZeros() {
        while (digits.size() > 1 && digits.get(digits.size() - 1) == 0) {
            digits.remove(digits.size() - 1);
        }

        if (digits.size() == 1 && digits.get(0) == 0) {
            isNegative = false;
        }
    }

}