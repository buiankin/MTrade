package ru.code22.mtrade;

import java.util.Arrays;

public class NativeCallsClass
{
    // Used to load the 'native-lib' library on application startup.
    static {
        // Так было бы, если бы файл назывался libnativelib.so
        //System.loadLibrary("native-lib");
        // соответствующий файл библиотеки называется libmain.so
        System.loadLibrary("main");
    }

    //native public static int inputInt();
    //native public static void outputInt(int v);

	native public static int convertJpegFile(String srcFilename, String destFilename, byte[] textData, int textLeft, int textTop, int textWidth, int textHeight, int quality, int scaleText);
}