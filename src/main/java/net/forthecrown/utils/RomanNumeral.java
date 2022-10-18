package net.forthecrown.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum RomanNumeral {
    I(1),    IV(4),  V(5),    IX(9),  X(10),
    XL(40),  L(50),  XC(90),  C(100),
    CD(400), D(500), CM(900),
    M(1000);

    public static final int MAX_NUMERAL_VALUE = 4000;
    private final int value;

    public static String arabicToRoman(long number) {
        if (number == 0) {
            return "0";
        }

        StringBuilder sb = new StringBuilder();

        // I know roman numerals can't be negative
        // but cmon, this is the land of make believe
        if (number < 0) {
            sb.append('-');
            number = -number;
        }

        // If bigger than the max number
        // decrease size until it fits
        if (number > MAX_NUMERAL_VALUE) {
            sb.append("MMMM".repeat((int) (number / MAX_NUMERAL_VALUE)));
            number = number % MAX_NUMERAL_VALUE;
        }

        List<RomanNumeral> romanNumerals = getReverseSortedValues();
        int i = 0;

        while ((number > 0) && (i < romanNumerals.size())) {
            RomanNumeral currentSymbol = romanNumerals.get(i);
            if (currentSymbol.getValue() <= number) {
                sb.append(currentSymbol.name());
                number -= currentSymbol.getValue();
            } else {
                i++;
            }
        }

        return sb.toString();
    }

    public static List<RomanNumeral> getReverseSortedValues() {
        return Arrays.stream(values())
                .sorted(Comparator.comparing((RomanNumeral e) -> e.value).reversed())
                .collect(Collectors.toList());
    }
}