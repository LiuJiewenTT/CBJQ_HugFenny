package io.github.liujiewentt.hugfenny;

interface IUserService {
    void destroy() = 16777114; // Destroy method defined by Shizuku server

    void exit() = 1; // Exit method defined by user

    /**
     * 执行命令，不支持含有空格的命令。
     */
    String execLine(String command) = 2;

    /**
     * 执行数组中分离的命令
     */
    String execArr(in String[] command) = 3;

    /**
     * 自动选择execArr/execLine。
     */
    String exec(String command) = 4;
}
