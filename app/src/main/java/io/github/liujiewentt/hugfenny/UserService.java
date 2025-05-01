package io.github.liujiewentt.hugfenny;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class UserService extends IUserService.Stub {

    public UserService() {
        Log.i("UserService", "constructor");
    }

    public UserService(Context context) {
        Log.i("UserService", "constructor with Context: context=" + context.toString());
    }

    @Override
    public void destroy() throws RemoteException {
        System.exit(0);
    }

    @Override
    public void exit() throws RemoteException {
        destroy();
    }

    @Override
    public String execLine(String command) throws RemoteException {
        try {
            // 执行shell命令
            Process process = Runtime.getRuntime().exec(command);
            // 读取执行结果
            return readResult(process);
        } catch (IOException | InterruptedException e) {
            throw new RemoteException();
        }
    }

    @Override
    public String execArr(String[] command) throws RemoteException {
        try {
            // 执行shell命令
            Process process = Runtime.getRuntime().exec(command);
            // 读取执行结果
            return readResult(process);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 读取执行结果，如果有异常会向上抛
     */
    public String readResult(Process process) throws IOException, InterruptedException {
        StringBuilder stringBuilder = new StringBuilder();
        // 读取执行结果
        InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        inputStreamReader.close();
        process.waitFor();
        return stringBuilder.toString();
    }

    @Override
    public String exec(String command) throws RemoteException {
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

            list.forEach(item -> Log.i("UserService", "exec: execArr item: " + item));

            // 这种方法可用于执行路径中带空格的命令，例如 ls /storage/0/emulated/temp dir/
            // 当然也可以执行不带空格的命令，实际上是要强于另一种执行方式的
            return execArr(list.toArray(new String[0]));
        } else {
            Log.i("UserService", "exec: execLine: " + command);
            // 这种方法仅用于执行路径中不包含空格的命令，例如 ls /storage/0/emulated/
            return execLine(command);
        }
    }
}
