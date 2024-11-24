package io.github.liujiewentt.hugfenny;

import static android.os.SystemClock.sleep;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rikka.shizuku.Shizuku;
import rikka.shizuku.ShizukuBinderWrapper;
import rikka.shizuku.ShizukuProvider;

import io.github.liujiewentt.hugfenny.AppRecyclerViewAdapter;
import io.github.liujiewentt.hugfenny.AppRecyclerViewItem;



public class MainActivity extends AppCompatActivity {

    static String TAG = "MainActivity";

    private RecyclerView recyclerView;
    private AppRecyclerViewAdapter adapter;
    private List<AppRecyclerViewItem> itemList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置布局文件
        setContentView(R.layout.activity_main);
//        LinearLayout layout = findViewById(R.id.main_linearlayout);

        // 获取布局中的视图元素
        TextView helloWorldText = findViewById(R.id.hello_world);
        Button clickButton = findViewById(R.id.button);

        // 设置按钮点击事件
        clickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helloWorldText.setText("Button Clicked!");
            }
        });

        // 绑定 Shizuku 服务
        Shizuku.addRequestPermissionResultListener((requestCode, grantResult) -> {
            if (requestCode == 0 && grantResult == PackageManager.PERMISSION_GRANTED) {
                System.out.println("Shizuku 权限授予成功！");
            }
        });
        requestShizukuPermission();
        Common.flag_high_priviledge = true;

        // 检查 Shizuku 服务绑定
        if (Shizuku.getVersion() > 0) {
            System.out.println("Shizuku 服务已绑定！");
        } else {
            System.out.println("Shizuku 服务未启动，请确保它正在运行！");
        }

        Main();

    }

    private void requestShizukuPermission() {
        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_DENIED) {
            Shizuku.requestPermission(0);
            sleep(5000);
        } else {
            Log.d(TAG, "requestShizukuPermission: PERMISSION_GRANTED");
        }
    }

    protected void Main() {
        // 1. 获取 RecyclerView 实例
        recyclerView = findViewById(R.id.main_recyclerview);
        // 2. 设置 LayoutManager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        // 3. 准备数据源
        itemList = new ArrayList<AppRecyclerViewItem>();

        Common.extSdRootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        Common.dataDirectoryPath = String.join("/", Common.extSdRootPath, "Android/data");
        Log.d(TAG, "Main: getExternalStorageDirectory(): "+Common.extSdRootPath);
        Log.d(TAG, "Main: dataDirectoryPath = " + Common.dataDirectoryPath);
        List<String> projectDirs = new ArrayList<>();
        List<ApplicationInfo> allApps = null;
        allApps = getPackageManager().getInstalledApplications(0);
        while(allApps.size() <= 1){
            sleep(300);
            allApps = getPackageManager().getInstalledApplications(0);
        }

        for (ApplicationInfo ai : allApps) {
//            Log.d("packageName", ai.packageName);
            String subDir_name = ai.packageName;
//            Log.d(TAG, "onActivityResult: subdir, name: " + subDir_name);
//                添加国服
            if (subDir_name.startsWith("com.dragonli.projectsnow.")) {
                Log.d(TAG, "add projectDirs : "+subDir_name);
                projectDirs.add(subDir_name);
            }
//                添加国际服
            if (subDir_name.startsWith("com.seasun.snowbreak")) {
                Log.d(TAG, "add projectDirs : "+subDir_name);
                projectDirs.add(subDir_name);
            }
        }

        Common.localizationValues = new HashMap<>();
        Map<String, String> packageUriMap = new HashMap<>();

        for (String dirName : projectDirs) {
            String documentPath = String.join("/", Common.dataDirectoryPath, dirName);
            String x_value = Common.readLocalizationFile(documentPath);
            if (!x_value.isEmpty()) {
                Common.localizationValues.put(dirName, Integer.valueOf(x_value));
            }
        }

        if (Common.localizationValues == null) {
            Log.e(TAG, "localizationValues = null");
//            return;
        }

        PackageManager packageManager = getPackageManager();

        for (String dirName : projectDirs) {
            String dirPath = String.join(Common.dataDirectoryPath, dirName);
            String remark = Common.remarkMap.get(dirName);
            Drawable appIcon = null;
            Integer x_value = Common.localizationValues.get(dirName);
            if (x_value == null) {
                x_value = -1;
            }

            try {
                ApplicationInfo appInfo = packageManager.getApplicationInfo(dirName, 0);
                appIcon = packageManager.getApplicationIcon(appInfo);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            itemList.add(new AppRecyclerViewItem(dirName, remark, appIcon, x_value));
        }

        // 假设你已经有了包名、图标和备注数据
        // itemList.add(new MyItem("com.example.app", "备注信息", someDrawable));

        // 4. 创建并设置适配器
        adapter = new AppRecyclerViewAdapter(itemList);
        recyclerView.setAdapter(adapter);
    }

}



