package io.github.liujiewentt.hugfenny;

import android.text.TextUtils;
import android.util.Log;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ShellUtils {

    static String TAG = "ShellUtils";

    /**
     * 读取文件内容
     *
     * @param filePath 文件路径
     * @return 文件内容
     * @throws Exception 如果 Shell 命令执行失败
     */
    public static String readFile(String filePath) throws RemoteException {
        String command = "cat " + filePath;
        Log.d(TAG, "readFile: command: " + command);
        String output;

//        output = Common.iUserService.exec(command);
        output = executeShellCommand(command);
        Log.d(TAG, "readFile: output: " + output);

        return output;
        // return executeShellCommand(command);
    }

    /**
     * 写入内容到文件
     *
     * @param filePath 文件路径
     * @param content  写入内容
     * @throws Exception 如果 Shell 命令执行失败
     */
    public static void writeFile(String filePath, String content) throws Exception {
        String command = "echo \"" + content + "\" > \"" + filePath + "\"";
        Log.d(TAG, "writeFile: command: " + command);
//        String output = Common.iUserService.exec(command);
        String output = executeShellCommand(command, Common.flag_high_priviledge);
        Log.d(TAG, "writeFile: output: " + output);
        // executeShellCommand(command);
    }

    public static void deleteFile(String filePath) throws Exception {
        String command = "rm \"" + filePath + "\"";
        Log.d(TAG, "deleteFile: command: " + command);
//        String output = Common.iUserService.exec(command);
        String output = executeShellCommand(command);
        Log.d(TAG, "deleteFile: output: " + output);
    }

    /**
     * 执行 Shell 命令
     *
     * @param command Shell 命令
     * @return Shell 命令的标准输出
     * @throws Exception 如果 Shell 命令执行失败
     */
    public static String executeShellCommand(String command) throws RemoteException {
        return executeShellCommand(command, Common.flag_high_priviledge);
    }

    /**
     * 执行 Shell 命令
     *
     * @param command Shell 命令
     * @return Shell 命令的标准输出
     * @throws Exception 如果 Shell 命令执行失败
     */
    public static String executeShellCommand(String command, boolean flag_su) throws RemoteException {
        String shell_executor = (flag_su ? "su" : "sh");
        command = shell_executor + " -c '" + command + "'";
        Log.d(TAG, "executeShellCommand: " + command);
        String output = Common.iUserService.exec(command);
        Log.d(TAG, "executeShellCommand output: " + output);
        if (output == null) {
            Log.i(TAG, "返回结果为null");
        } else if (TextUtils.isEmpty(output.trim())) {
            Log.i(TAG, "返回结果为空");
        }
        return output;
    }

    public static void resolveExecCommand(String command) {
        // 检查是否存在包含任意内容的单双引号
        Pattern pattern = Pattern.compile("'([^']*)'|\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(command);

        // 下面展示了两种不同的命令执行方法
        if (matcher.find()) {
            ArrayList<String> list = new ArrayList<>();
            Pattern pattern2 = Pattern.compile("'([^']*)'|\"([^\"]*)\"|(\\S+)");
            Matcher matcher2 = pattern2.matcher(command);

            while (matcher2.find()) {
                if (matcher2.group(1) != null) {
                    // 如果是单引号包裹的内容，取group(1)
                    list.add(matcher2.group(1));
                } else if (matcher2.group(2) != null) {
                    // 如果是双引号包裹的内容，取group(2)
                    list.add(matcher2.group(2));
                } else {
                    // 否则取group(3)，即普通的单词
                    list.add(matcher2.group(3));
                }
            }

            list.forEach(item -> Log.i(TAG, "exec: execArr item: [" + item + "]"));

            // 这种方法可用于执行路径中带空格的命令，例如 ls /storage/0/emulated/temp dir/
            // 当然也可以执行不带空格的命令，实际上是要强于另一种执行方式的
        } else {
            Log.i(TAG, "exec: execLine: " + command);
            // 这种方法仅用于执行路径中不包含空格的命令，例如 ls /storage/0/emulated/
        }
    }
}

