package io.github.liujiewentt.hugfenny;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ShellUtils {

    static String TAG = "ShellUtils";

    /**
     * 读取文件内容
     * @param filePath 文件路径
     * @return 文件内容
     * @throws Exception 如果 Shell 命令执行失败
     */
    public static String readFile(String filePath) throws Exception {
        String command = "cat " + filePath;
        Log.d(TAG, "readFile: command: " + command);
        return Common.iUserService.execCommand(command);
//        return executeShellCommand(command);
    }

    /**
     * 写入内容到文件
     * @param filePath 文件路径
     * @param content  写入内容
     * @throws Exception 如果 Shell 命令执行失败
     */
    public static void writeFile(String filePath, String content) throws Exception {
        String command = "echo \"" + escapeShellArgument(content) + "\" > " + filePath;
        Log.d(TAG, "writeFile: command: " + command);
        Common.iUserService.execCommand(command);
//        executeShellCommand(command);
    }

    /**
     * 执行 Shell 命令
     *
     * @param command Shell 命令
     * @return Shell 命令的标准输出
     * @throws Exception 如果 Shell 命令执行失败
     */
    private static String executeShellCommand(String command) throws Exception {
        return executeShellCommand(command, Common.flag_high_priviledge);
    }

    /**
     * 执行 Shell 命令
     * @param command Shell 命令
     * @return Shell 命令的标准输出
     * @throws Exception 如果 Shell 命令执行失败
     */
    private static String executeShellCommand(String command, boolean flag_su) throws Exception {
        String shell_executor = (flag_su? "su" : "sh");
        Process process = Runtime.getRuntime().exec(new String[]{shell_executor, "-c", command});
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Shell command failed with exit code " + exitCode);
        }

        return output.toString().trim();
    }

    /**
     * 转义 Shell 特殊字符
     * @param input 输入字符串
     * @return 转义后的字符串
     */
    private static String escapeShellArgument(String input) {
        return input.replace("\"", "\\\"").replace("$", "\\$");
    }
}

