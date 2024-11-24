package io.github.liujiewentt.hugfenny;

import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import rikka.shizuku.Shizuku;
import rikka.shizuku.ShizukuBinderWrapper;
import rikka.shizuku.ShizukuProvider;

public class Common {

    static String TAG = "Common";

    public static boolean flag_high_priviledge = false;

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

    public static String readFile(String path) throws Exception {
        return ShellUtils.readFile(path);
    }

    public static void writeFile(String path, String content) throws Exception {
        ShellUtils.writeFile(path, content);
    }

    public static boolean updateLocalizationFile(String dirPath, int newValue) {
//        String TAG = "updateLocalizationFile";
        String documentPath = String.join("/", dirPath, "files/localization.txt");
        Log.d(TAG, "documentPath: " + documentPath);
        try {
            writeFile(documentPath, "localization = " + newValue);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "无法写入xvalue, 文件：" + documentPath, e);
//            e.printStackTrace();
            return false;
        }
    }

    public static String readLocalizationFile(String dirPath) {
//        String TAG = "readLocalizationFile";
        String documentPath = String.join("/", dirPath, "files/localization.txt");
        Log.d(TAG, "documentPath: " + documentPath);
        try {
            return readFile(documentPath);
        } catch (Exception e) {
            Log.e(TAG, "无法读取xvalue, 文件：" + documentPath, e);
//            e.printStackTrace();
            return "";
        }
    }
}
