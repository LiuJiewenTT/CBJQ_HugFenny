package io.github.liujiewentt.hugfenny;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

public class Common {

    static String TAG = "Common";

    static String extSdRootPath;
    static String dataDirectoryPath;

    static Map<String, Integer> localizationValues;

    static public Map<String, String> remarkMap = Map.of(
            "com.dragonli.projectsnow.lhm", "官服",
            "com.dragonli.projectsnow.bilibili", "B服",
            "com.seasun.snowbreak.google", "国际服"
    );

//    static {
//
//    }

    public static boolean updateLocalizationFile(String dirPath, int newValue) {
        String TAG = "updateLocalizationFile";
        String documentPath = String.join("/", dirPath, "files/Flocalization.txt");
        Log.d(TAG, "Main: documentPath: " + documentPath);
        try {
            OutputStream outputStream = getContentResolver().openOutputStream(fileUri);
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
                writer.write("localization = " + newValue);
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Main: 无法写入xvalue, 文件：" + documentId, e );
                e.printStackTrace();
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Main: ", e);
//            throw new RuntimeException(e);
        }
        return false;
    }
}
