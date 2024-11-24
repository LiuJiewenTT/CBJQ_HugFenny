package io.github.liujiewentt.hugfenny;

import static android.os.SystemClock.sleep;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.liujiewentt.hugfenny.IUserService;
import rikka.shizuku.Shizuku;


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
                Shizuku.bindUserService(userServiceArgs, serviceConnection);
            }
        });

        requestShizukuPermission();
        // 绑定 Shizuku 服务
        Shizuku.addRequestPermissionResultListener(onRequestPermissionResultListener);
        // 添加权限申请监听
//        Common.flag_high_priviledge = true;

        // 检查 Shizuku 服务绑定
        if (Shizuku.getVersion() > 0) {
            IBinder iBinder = Shizuku.getBinder();
            if( iBinder != null && iBinder.pingBinder() ){
                System.out.println("Shizuku 服务已绑定且可用！");
            } else {
                System.out.println("Shizuku 服务未绑定！");
            }
        } else {
            System.out.println("Shizuku 服务未启动，请确保它正在运行！");
            Toast.makeText(MainActivity.this, "Shizuku 服务未启动，请确保它正在运行！", Toast.LENGTH_SHORT).show();
        }

//        Shizuku.addRequestPermissionResultListener(onRequestPermissionResultListener);

        // Shiziku服务启动时调用该监听
        Shizuku.addBinderReceivedListenerSticky(onBinderReceivedListener);

        // Shiziku服务终止时调用该监听
        Shizuku.addBinderDeadListener(onBinderDeadListener);

        Shizuku.bindUserService(userServiceArgs, serviceConnection);

        Main();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 移除权限申请监听
        Shizuku.removeRequestPermissionResultListener(onRequestPermissionResultListener);

        Shizuku.removeBinderReceivedListener(onBinderReceivedListener);

        Shizuku.removeBinderDeadListener(onBinderDeadListener);

        Shizuku.unbindUserService(userServiceArgs, serviceConnection, true);
    }

    private final Shizuku.OnRequestPermissionResultListener onRequestPermissionResultListener = new Shizuku.OnRequestPermissionResultListener() {
        @Override
        public void onRequestPermissionResult(int requestCode, int grantResult) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                System.out.println("Shizuku 权限授予成功！");
            } else {
                System.out.println("Shizuku 权限授予失败！");
            }
        }
    };


    private final Shizuku.OnBinderDeadListener onBinderDeadListener = new Shizuku.OnBinderDeadListener() {
        @Override
        public void onBinderDead() {
            Toast.makeText(MainActivity.this, "Shizuku服务终止，binder失效", Toast.LENGTH_SHORT).show();
        }
    };

    private final Shizuku.OnBinderReceivedListener onBinderReceivedListener = new Shizuku.OnBinderReceivedListener() {
        @Override
        public void onBinderReceived() {
            Toast.makeText(MainActivity.this, "Shizuku服务已启动，收到binder", Toast.LENGTH_SHORT).show();
            Common.shizuku_ibinder = Shizuku.getBinder();
            Common.iUserService = UserService.asInterface(Common.shizuku_ibinder);
        }
    };

    // 指定服务的各项参数
    private final Shizuku.UserServiceArgs userServiceArgs =
            new Shizuku.UserServiceArgs(new ComponentName(BuildConfig.APPLICATION_ID, UserService.class.getName()))
                    .daemon(false)
                    .processNameSuffix("adb_service")
                    .debuggable(BuildConfig.DEBUG)
                    .version(BuildConfig.VERSION_CODE);

    // 建立服务连接通道
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Toast.makeText(MainActivity.this, "onServiceConnected: 服务连接成功", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onServiceConnected: 服务连接成功");

            if (iBinder != null && iBinder.pingBinder()) {
                Log.d(TAG, "onServiceConnected: good ibinder.");
                Toast.makeText(MainActivity.this, "binder可用", Toast.LENGTH_SHORT).show();
                Common.shizuku_ibinder = iBinder;
                Common.iUserService = UserService.asInterface(iBinder);
//                Common.iUserService = IUserService.Stub.asInterface(iBinder);
            } else {
                Toast.makeText(MainActivity.this, "binder不可用", Toast.LENGTH_SHORT).show();
                throw new RuntimeException("no ibinder");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Toast.makeText(MainActivity.this, "onServiceDisconnected: 服务连接断开", Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * 判断是否拥有shizuku adb shell权限
     */
    private boolean checkPermission() {
        return Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
    }

    private void requestShizukuPermission() {
        boolean checked = checkPermission();
        if (checked) {
            Toast.makeText(this, "已拥有Shizuku权限", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Shizuku.isPreV11()) {
            Toast.makeText(this, "当前shizuku版本不支持动态申请", Toast.LENGTH_SHORT).show();
            return;
        }

        // 动态申请权限
//        Shizuku.requestPermission(MainActivity.PERMISSION_CODE);
//        Shizuku.requestPermission(MainActivity.RESULT_OK);
        Shizuku.requestPermission(10001);
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

        // 刚启动这边可能会读取错误。
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



