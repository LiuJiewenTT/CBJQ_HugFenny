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
            InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return stringBuilder.toString();
    }
}
