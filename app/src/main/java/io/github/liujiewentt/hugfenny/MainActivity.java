package io.github.liujiewentt.hugfenny;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
    static Map<String, Integer> localizationValues;
    private RecyclerView recyclerView;
    private MyRecyclerViewAdapter adapter;
    private List<MyRecyclerViewItem> itemList;

    static public Map<String, String> remarkMap = Map.of(
            "com.dragonli.projectsnow.lhm", "官服",
            "com.dragonli.projectsnow.bilibili", "B服",
            "com.seasun.snowbreak.google", "国际服"
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        Main();
//        layout.addView(recyclerView);

    }

    protected void Main() {
        String TAG = "Main()";
        // 1. 获取 RecyclerView 实例
        recyclerView = findViewById(R.id.main_recyclerview);
        // 2. 设置 LayoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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
        requestAccessAndroidData(this);



        // 假设你已经有了包名、图标和备注数据
        // itemList.add(new MyItem("com.example.app", "备注信息", someDrawable));

        // 4. 创建并设置适配器
        adapter = new MyRecyclerViewAdapter(itemList);
        recyclerView.setAdapter(adapter);
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
        ActivityResultLauncher<Intent> openDocumentTreeLauncher = null;
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

    private static void updateLocalizationFile(String dirPath, int newValue) {
        File localizationFile = new File(dirPath, "files/localization.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(localizationFile))) {
            writer.write("localization = " + newValue);
        } catch (IOException e) {
            e.printStackTrace();
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
                Toast.makeText(this, "已授权", Toast.LENGTH_LONG).show();
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
                List<DocumentFile>  subDirs = new ArrayList<>();
                for (DocumentFile file : files) {
                    if ( file.isDirectory() ) {
                        Log.d(TAG, "onActivityResult: add document: " + file.getName());
                        subDirs.add(file);
                    }
                }
//                File[] subDirs = directory.listFiles(File::isDirectory);
                List<String> projectDirs = new ArrayList<>();

                if (!subDirs.isEmpty()) {
                    for (DocumentFile subDir : subDirs) {
                        String subDir_name = subDir.getName();
                        Log.d(TAG, "onActivityResult: subdir, name: " + subDir_name);
//                添加国服
                        if (subDir_name.startsWith("com.dragonli.projectsnow.")) {
                            projectDirs.add(subDir.getName());
                        }
//                添加国际服
                        if (subDir_name.startsWith("com.seasun.snowbreak")) {
                            projectDirs.add(subDir.getName());
                        }
                    }
                } else {
                    Log.e(TAG, "Main: subDir is null");
                    return;
                }

                localizationValues = new HashMap<>();

                for (String dirName : projectDirs) {
                    String dirPath = String.join(dataDirectoryPath, dirName);
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
                            e.printStackTrace();
                        }
                    }
                }

                PackageManager packageManager = getPackageManager();

                for (String dirName : projectDirs) {
                    String dirPath = String.join(dataDirectoryPath, dirName);
                    String remark = remarkMap.get(dirName);
                    Drawable appIcon = null;
                    Integer x_value = localizationValues.get(dirName);

                    try {
                        ApplicationInfo appInfo = packageManager.getApplicationInfo(dirName, 0);
                        appIcon = packageManager.getApplicationIcon(appInfo);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }

                    itemList.add(new MyRecyclerViewItem(dirName, remark, appIcon, x_value));
                }

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
            this.xValue = (this.xValue == 0 ? 1 : 0);
        }
    }

    static class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.MyViewHolder> {
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
            holder.xValueTextView.setText(item.getXValue() == 1 ? "X=1" : "X=0");

            // 设置按钮点击事件，切换 X 值
            holder.xValueButton.setOnClickListener(v -> {
                item.toggleXValue();
                localizationValues.put(item.packageName, item.xValue);
                updateLocalizationFile(item.packageName, item.xValue);
                notifyItemChanged(position); // 通知数据已更新，刷新视图
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



