package com.nettakrim.signed_paintings.util;

import org.apache.commons.lang3.ArrayUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SignByteMapper {

    public static String INITIALIZER_STRING = "i\u200C";
    public static String DELIMITER = "\u200C";

    // Sorted accordingly to textRenderer.getWidth (only first 256 used)
    private final static int[] MAP = {712, 781, 809, 1469, 1472, 1575, 1648, 1852, 1855, 1856, 1857, 1858, 1859, 1860,
            1994, 2029, 2034, 2078, 2079, 2080, 2118, 2139, 2170, 2221, 2282, 2285, 2362, 2404, 2551, 2878, 2930, 3193,
            3387, 3656, 3770, 3784, 3975, 4170, 4469, 5029, 5194, 6075, 6088, 6091, 6130, 6499, 6679, 6680, 6773, 6824,
            7223, 7227, 7294, 7388, 7389, 8286, 8402, 8403, 9134, 9144, 9145, 9168, 9478, 9482, 9550, 9589, 9591, 9615,
            9621, 10649, 11599, 11601, 11837, 12643, 12688, 12753, 42128, 42226, 42529, 42781, 42782, 42783, 42891,
            42892, 43056, 43126, 43214, 43302, 43307, 43311, 43335, 43336, 43337, 43339, 43346, 43596, 43613, 43760,
            43893, 65045, 65073, 65074, 65075, 65111, 65165, 65500, 65508, 65512, 65792, 65799, 66186, 66313, 66336,
            66413, 66662,66830, 67510, 67721, 67751, 67814, 67821, 67835, 67862, 67878, 68032, 68086, 68182, 68221,
            68253, 68389, 68440, 68472, 68670, 68773, 68837, 68858, 68913, 69384, 69392, 69573, 69609, 69703, 69930,
            69953, 70017, 70085, 70088, 70091, 70368, 71339, 71340, 71351, 72003, 72257, 72258, 72421, 73018, 73031,
            92761, 92912, 92914, 92976, 93992, 94031, 94073, 110581, 113666, 113671, 113676, 113800, 113816, 118581,
            118590, 119003, 119002, 119040, 119045, 119044, 119141, 119149, 119164, 119166, 119200, 119233, 119235,
            119234, 119237, 119236, 119657, 119671, 121393, 121463, 121465, 121464, 121467, 121466, 121469, 121468,
            121499, 121501, 121500, 121503, 121502, 123184, 124142, 124145, 124962, 124971, 125127, 125259, 125265,
            126209, 126464, 129905, 129904, 129907, 129906, 129909, 129908, 33, 39, 44, 46, 58, 59, 124, 161, 166, 183,
            184, 305, 448, 451, 697, 699, 700, 701, 716, 720, 721, 729, 731, 737, 753, 754, 760, 761, 762, 763, 764,
            775, 786, 787, 788, 789, 790, 791, 792, 793, 795, 796, 796, 797, 798, 799};

    public static String decode(String encoded) {
        List<Byte> rawBytes = new ArrayList<>();
        for (int i = 0; i < encoded.length(); i++) {
            int index = ArrayUtils.indexOf(MAP, encoded.codePointAt(i));
            if (index != -1) {
                rawBytes.add((byte) index);
            }
        }
        byte[] byteArray = ArrayUtils.toPrimitive(rawBytes.toArray(new Byte[0]));
        return new String(byteArray, StandardCharsets.UTF_8);
    }

    public static String encode(String decoded) {
        byte[] byteArray = decoded.getBytes(StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        for (byte b: byteArray) {
            int codePoint = MAP[b & 0xFF];
            sb.append(Character.toChars(codePoint));
        }
        return sb.toString();
    }
}
