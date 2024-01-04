package com.suhailapps.radiationmeasurement;

public class FBStringEncryption {
    public static String Decode(String encodeStr){
        encodeStr = encodeStr.replace("<dot>", ".");
        encodeStr = encodeStr.replace("<hash>", "#");
        encodeStr = encodeStr.replace("<dol>", "$");
        encodeStr = encodeStr.replace("<sqb1>", "[");
        encodeStr = encodeStr.replace("<sqb2>", "]");

        return encodeStr;
    }

    public static String Encode(String decodeStr){
        decodeStr = decodeStr.replace(".", "<dot>");
        decodeStr = decodeStr.replace("#", "<hash>");
        decodeStr = decodeStr.replace("$", "<dol>");
        decodeStr = decodeStr.replace("[", "<sqb1>");
        decodeStr = decodeStr.replace("]", "<sqb2>");

        return decodeStr;
    }
}
