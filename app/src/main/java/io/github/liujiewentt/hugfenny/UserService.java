package io.github.liujiewentt.hugfenny;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import io.github.liujiewentt.hugfenny.IUserService;

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
    public String execCommand(String command) throws RemoteException {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            // 执行shell命令
            Process process = Runtime.getRuntime().exec(command);
            // 读取执行结果
            // 读取标准输出流
            InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            bufferedReader.close();
            inputStreamReader.close();

            // 读取标准错误流
            InputStreamReader errorStreamReader = new InputStreamReader(process.getErrorStream());
            BufferedReader errorBufferedReader = new BufferedReader(errorStreamReader);
            String errorLine;
            while ((errorLine = errorBufferedReader.readLine()) != null) {
                // 将错误信息添加到输出中，或者你可以选择丢弃它
                stringBuilder.append("Error: ").append(errorLine).append("\n");
            }
            errorBufferedReader.close();
            errorStreamReader.close();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return stringBuilder.toString();
    }
}
