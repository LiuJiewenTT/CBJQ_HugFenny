package io.github.liujiewentt.hugfenny;


import static android.os.SystemClock.sleep;
import android.Manifest;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_OPEN_DOCUMENT_TREE = 1000;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 101;
    static Map<String, Integer> localizationValues;
    private RecyclerView recyclerView;
    private MyRecyclerViewAdapter adapter;
    private List<MyRecyclerViewItem> itemList;

    private static String dataDirectoryPath = null;
    private static String extSdRootPath = null;

    static public Map<String, String> remarkMap = Map.of(
            "com.dragonli.projectsnow.lhm", "官服",
            "com.dragonli.projectsnow.bilibili", "B服",
            "com.seasun.snowbreak.google", "国际服"
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String TAG = "onCreate";
        super.onCreate(savedInstanceState);
        // 设置布局文件
        setContentView(R.layout.activity_main);
        LinearLayout layout = findViewById(R.id.main_linearlayout);

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

        if (!checkPermission()) {
            requestStoragePermission();
        }
        while (!checkPermission()) {
//            debug sleep
            sleep(300);
        }
        boolean tempbool1 = false;
        tempbool1 = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        Log.d(TAG, "onCreate: READ_EXTERNAL_STORAGE == PERMISSION_GRANTED ? "+tempbool1);
        tempbool1 = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        Log.d(TAG, "onCreate: WRITE_EXTERNAL_STORAGE == PERMISSION_GRANTED ? "+tempbool1);
//        笔记：获取了WRITE_EXTERNAL_STORAGE后将会同时具有READ_EXTERNAL_STORAGE的权限。

//        ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
        if ( !(ContextCompat.checkSelfPermission(this,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) ) {
            // 请求 MANAGE_EXTERNAL_STORAGE 权限
//                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:$packageName"));
//            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:io.github.liujiewentt.hugfenny"));
            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            startActivityForResult(intent, REQUEST_CODE_STORAGE_PERMISSION);
        }
        while ( !(ContextCompat.checkSelfPermission(this,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) ) {
            Log.d(TAG, "onCreate: 等待授权MANAGE_EXTERNAL_STORAGE");
            sleep(300);
            // 笔记：这里是不会检测到的（如果请求用的也是和WRITE_EXTERNAL_STORAGE的一个方法的话），所以不知道，于是一直卡在这。
            break;  // debug临时退出
        }

        Main();
//        layout.addView(recyclerView);

    }

    protected void Main() {
        String TAG = "Main()";
        // 1. 获取 RecyclerView 实例
        recyclerView = findViewById(R.id.main_recyclerview);
        // 2. 设置 LayoutManager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        // 3. 准备数据源
        itemList = new ArrayList<MyRecyclerViewItem>();

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            // Android 11 及以上版本
//            if (!Environment.isExternalStorageManager()) {
//                // 请求 MANAGE_EXTERNAL_STORAGE 权限
//                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:$packageName"));
//                startActivityForResult(intent, REQUEST_CODE_STORAGE_PERMISSION);
//            }
//        }
//        requestAccessAndroidData(this);
        extSdRootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        dataDirectoryPath = String.join("/", extSdRootPath, "Android/data");
        Log.d(TAG, "Main: getExternalStorageDirectory(): "+extSdRootPath);
        Log.d(TAG, "Main: dataDirectoryPath = " + dataDirectoryPath);
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

        localizationValues = new HashMap<>();

        for (String dirName : projectDirs) {
            String dirPath = String.join("/", dataDirectoryPath, dirName);
            File localizationFile = new File(dirPath, "files/localization.txt");
            if (localizationFile.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(localizationFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("localization =")) {
                            String value = line.split("=")[1].trim();
                            localizationValues.put(dirName, Integer.parseInt(value));
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Main: 无法读取xvalue, 文件："+dirPath );
                    e.printStackTrace();
                }
            } else {
                Log.e(TAG, "Main: file not exist: " + localizationFile.getPath());
            }
        }

        if (localizationValues == null) {
            Log.e(TAG, "Main: localizationValues = null");
//            return;
        }

        PackageManager packageManager = getPackageManager();

        for (String dirName : projectDirs) {
            String dirPath = String.join(dataDirectoryPath, dirName);
            String remark = remarkMap.get(dirName);
            Drawable appIcon = null;
            Integer x_value = localizationValues.get(dirName);
            if (x_value == null) {
                x_value = -1;
            }

            try {
                ApplicationInfo appInfo = packageManager.getApplicationInfo(dirName, 0);
                appIcon = packageManager.getApplicationIcon(appInfo);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            itemList.add(new MyRecyclerViewItem(dirName, remark, appIcon, x_value));
        }




        // 假设你已经有了包名、图标和备注数据
        // itemList.add(new MyItem("com.example.app", "备注信息", someDrawable));

        // 4. 创建并设置适配器
        adapter = new MyRecyclerViewAdapter(itemList);
        recyclerView.setAdapter(adapter);
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        //        笔记：requestPermissions是异步的、非阻塞的。
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        String TAG = "onRequestPermissionsResult";
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onRequestPermissionsResult: PERMISSION_GRANTED");
            } else {
                Log.e(TAG, "onRequestPermissionsResult: NOT PERMISSION_GRANTED");
            }
        }
    }

    @TargetApi(26)
    private void requestAccessAndroidData(Activity activity){
        try {
            Uri uri = Uri.parse("content://com.android.externalstorage.documents/document/primary%3AAndroid%2Fdata");
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
            //flag看实际业务需要可再补充
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            activity.startActivityForResult(intent, 6666);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showDirectoryPrompt() {
        new AlertDialog.Builder(this)
                .setTitle("选择目录")
                .setMessage("请定位到 Android/data/ 目录并选择该目录。")
                .setPositiveButton("继续", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openDirectoryPicker();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void openDirectoryPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT_TREE);

        // 初始化 ActivityResultLauncher，仅在API 21及以上使用
//        ActivityResultLauncher<Intent> openDocumentTreeLauncher = null;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 初始化 ActivityResultLauncher
//            openDocumentTreeLauncher = registerForActivityResult(
//                    new ActivityResultContracts.StartActivityForResult(),
//                    new ActivityResultCallback<androidx.activity.result.ActivityResult>() {
//                        @Override
//                        public void onActivityResult(androidx.activity.result.ActivityResult result) {
//                            if (result.getResultCode() == RESULT_OK) {
//                                Intent data = result.getData();
//                                if (data != null) {
//                                    Uri uri = data.getData();
//                                    if (uri != null) {
//                                        // 处理返回的 URI（选中的目录）
//                                        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                                        Toast.makeText(MainActivity.this, "Directory selected: " + uri.getPath(), Toast.LENGTH_SHORT).show();
//                                    } else {
//                                        Toast.makeText(MainActivity.this, "No directory selected", Toast.LENGTH_SHORT).show();
//                                    }
//                                }
//                            }
//                        }
//                    });
//        }

        // 启动文件选择器
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
//            openDocumentTreeLauncher.launch(intent);
//        } else {
//            // 对于低于API 21的设备，使用传统的startActivityForResult
//            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
//            startActivityForResult(intent, 1000);  // 1000是旧版的请求代码
//        }
    }

    private static boolean updateLocalizationFile(String dirPath, int newValue) {
        String TAG = "updateLocalizationFile";
        File localizationFile = new File(dirPath, "files/localization.txt");
        Log.d(TAG, "updateLocalizationFile: localizationFile = "+localizationFile.getAbsolutePath());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(localizationFile))) {
            writer.write("localization = " + newValue);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 获取和保存 URI
    private void saveUri(Uri uri) {
        SharedPreferences sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("saved_uri", uri.toString());
        editor.apply();
    }

    private Uri getSavedUri() {
        SharedPreferences sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String uriString = sharedPreferences.getString("saved_uri", null);
        return uriString != null ? Uri.parse(uriString) : null;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        String TAG = "onActivityResult()";
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (Environment.isExternalStorageManager()) {
                // 用户已授予权限
                Toast.makeText(this, "已授权 MANAGE_EXTERNAL_STORAGE, Environment.isExternalStorageManager() = true", Toast.LENGTH_LONG).show();
            } else {
                // 用户拒绝权限
                Toast.makeText(this, "权限被拒绝，无法访问 Android/data 目录", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == 6666) {
            if (resultCode == Activity.RESULT_OK) {
                // 权限已获取，可以访问所有存储
                String dataDirectoryPath = null;
                File directory = null;
//        dataDirectoryPath = String.join(Environment.getExternalStorageDirectory().getAbsolutePath(), "Android/data");
//        directory = new File(dataDirectoryPath);
                directory = new File(Environment.getExternalStorageDirectory(), "Android/data");
                //persist uri
                getContentResolver().takePersistableUriPermission(data.getData(),
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                //now use DocumentFile to do some file op
                DocumentFile documentFile = DocumentFile
                        .fromTreeUri(this, data.getData());
                DocumentFile[] files = documentFile.listFiles();
                List<DocumentFile> subDirs = new ArrayList<>();
                for (DocumentFile file : files) {
                    if ( file.isDirectory() ) {
                        Log.d(TAG, "onActivityResult: add document: " + file.getName());
                        subDirs.add(file);
                    }
                }
//                File[] subDirs = directory.listFiles(File::isDirectory);

            }

        }
    }


    class MyRecyclerViewItem {
        private String packageName;
        private String remark;
        private Drawable icon;
        private Integer xValue;

        // 构造方法、getter、setter
        public MyRecyclerViewItem(String packageName, String remark, Drawable icon, Integer xValue) {
            this.packageName = packageName;
            this.remark = remark;
            this.icon = icon;
            this.xValue = xValue;
        }

        public String getPackageName() {
            return packageName;
        }

        public String getRemark() {
            return remark;
        }

        public Drawable getIcon() {
            return icon;
        }

        public Integer getXValue() {
            return xValue;
        }

        public void toggleXValue() {
            this.xValue = getOpposingXValue();
        }

        public Integer getOpposingXValue() {
            return (this.xValue == 0 ? 1 : 0);
        }
    }

    static class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.MyViewHolder> {
        private static final String TAG = "MyRecyclerViewAdapter: ";
        private List<MyRecyclerViewItem> itemList;

        public MyRecyclerViewAdapter(List<MyRecyclerViewItem> itemList) {
            this.itemList = itemList;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_app, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            MyRecyclerViewItem item = itemList.get(position);

            // 绑定数据
            holder.packageNameTextView.setText(item.getPackageName());
            holder.remarkTextView.setText(item.getRemark());
            holder.iconImageView.setImageDrawable(item.getIcon());
            holder.xValueTextView.setText(item.getXValue().toString());

            // 设置按钮点击事件，切换 X 值
            holder.xValueButton.setOnClickListener(v -> {
                Integer newXValue = item.getOpposingXValue();
                String dirPath = String.join("/", dataDirectoryPath, item.packageName);
                Log.d(TAG, "onBindViewHolder: dirPath = "+dirPath);
                if ( updateLocalizationFile(dirPath, newXValue) ) {
                    item.toggleXValue();
                    localizationValues.put(item.packageName, newXValue);
                    notifyItemChanged(position); // 通知数据已更新，刷新视图
                }
            });

//        String packageName = // 获取包名
//        int xValue = localizationValues.get(packageName); // 从存储中获取 x 值
//        holder.appIcon.setImageDrawable(appIcon); // 设置图标
//        holder.appRemark.setText(remarks.get(packageName)); // 设置备注
//        holder.xValue.setText(String.valueOf(xValue)); // 显示 x 值
//
//        holder.toggleButton.setOnClickListener(v -> {
//            // 切换 x 值
//            int newValue = xValue == 1 ? 0 : 1;
//            localizationValues.put(packageName, newValue); // 更新存储
//            holder.xValue.setText(String.valueOf(newValue)); // 更新显示
//            updateLocalizationFile(packageName, newValue); // 更新文件
//        });
        }

        @Override
        public int getItemCount() {
            return itemList.size();
        }

        public static class MyViewHolder extends RecyclerView.ViewHolder {
            TextView packageNameTextView;
            TextView remarkTextView;
            ImageView iconImageView;
            TextView xValueTextView;
            Button xValueButton;

            public MyViewHolder(View itemView) {
                super(itemView);
                packageNameTextView = itemView.findViewById(R.id.app_name);
                remarkTextView = itemView.findViewById(R.id.app_remark);
                iconImageView = itemView.findViewById(R.id.app_icon);
                xValueTextView = itemView.findViewById(R.id.x_value);
                xValueButton = itemView.findViewById(R.id.toggle_button);
            }
        }
    }


}



