package io.github.liujiewentt.hugfenny;

import android.os.IBinder;
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

import io.github.liujiewentt.hugfenny.IUserService;
import rikka.shizuku.Shizuku;
import rikka.shizuku.ShizukuBinderWrapper;
import rikka.shizuku.ShizukuProvider;


public class Common {

    static String TAG = "Common";

    public static boolean flag_high_priviledge = false;
    public static IBinder shizuku_ibinder;
    public static IUserService iUserService;

    static String extSdRootPath;
    static String dataDirectoryPath;

    static Map<String, Integer> localizationValues;

    static public Map<String, String> remarkMap = Map.of(
            "com.dragonli.projectsnow.lhm", "官服",
            "com.dragonli.projectsnow.bilibili", "B服",
            "com.seasun.snowbreak.google", "国际服"
    );

    public static String readFile(String path) throws Exception {
        return ShellUtils.readFile(path);
    }

    public static void writeFile(String path, String content) throws Exception {
        ShellUtils.writeFile(path, content);
    }

    public static void deleteFile(String path) throws Exception {
        ShellUtils.deleteFile(path);
    }

    public static boolean updateLocalizationFile(String dirPath, int newValue) {
        // String TAG = "updateLocalizationFile";
        String documentPath = String.join("/", dirPath, "files/localization.txt");
        Log.d(TAG, "documentPath: " + documentPath);
        try {
            writeFile(documentPath, "localization=" + newValue);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "无法写入xvalue, 文件：" + documentPath, e);
            return false;
        }
    }

    public static String readLocalizationFile(String dirPath) {
        // String TAG = "readLocalizationFile";
        String documentPath = String.join("/", dirPath, "files/localization.txt");
        Log.d(TAG, "documentPath: " + documentPath);
        try {
            return readFile(documentPath);
        } catch (Exception e) {
            Log.e(TAG, "无法读取xvalue, 文件：" + documentPath, e);
            return "";
        }
    }

    public static String deleteLocalizationFile(String dirPath) {
        // String TAG = "deleteLocalizationFile";
        String documentPath = String.join("/", dirPath, "files/localization.txt");
        Log.d(TAG, "documentPath: " + documentPath);
        try {
            return readFile(documentPath);
        } catch (Exception e) {
            Log.e(TAG, "无法读取xvalue, 文件：" + documentPath, e);
            return "";
        }
    }

}
