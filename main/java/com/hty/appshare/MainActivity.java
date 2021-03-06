package com.hty.appshare;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends Activity {

    ListView listView;
    AppInfoAdapter adapter;
    EditText editTextSearch;
    ProgressDialog progressDialog;
    List<PackageInfo> packages;
    PackageManager PM;
    List<AppInfo> listAppInfo = null;
    int i = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextSearch = (EditText) findViewById(R.id.editTextSearch);
        listView = (ListView) findViewById(R.id.listView);
        listView.setTextFilterEnabled(true);    // 开启过滤

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("读取应用列表");
        progressDialog.setCancelable(false);
        progressDialog.show();

        PM = getPackageManager();
        packages = PM.getInstalledPackages(0);
        listAppInfo = new ArrayList<AppInfo>();
        progressDialog.setMax(packages.size());
        new Thread(t).start();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, final View view, int position, long id) {
                final String pkgName = ((TextView) view.findViewById(R.id.textViewPkgName)).getText().toString();
                final String appLabel = ((TextView) view.findViewById(R.id.textViewAppLabel)).getText().toString();
                final String sourceDir = ((TextView) view.findViewById(R.id.textViewSourceDir)).getText().toString();
                String pkgSize = ((TextView) view.findViewById(R.id.textViewPkgSize)).getText().toString();
                String lastUpdateTime = ((TextView) view.findViewById(R.id.textViewLastUpdateTime)).getText().toString();
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setIcon(((ImageView)view.findViewById(R.id.imageView)).getDrawable());
                builder.setTitle("详情");
                builder.setMessage("名称："+ appLabel + "\n包名：" + pkgName + "\n路径：" + sourceDir + "\n大小：" + pkgSize + "\n更新时间：" + lastUpdateTime);
                builder.setPositiveButton("启动", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = getPackageManager().getLaunchIntentForPackage(pkgName);
                        startActivity(intent);
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("关闭", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setNeutralButton("分享", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(sourceDir)));
                        intent.setType("*/*");
                        startActivity(Intent.createChooser(intent, "分享 " + appLabel));
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });

        listView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                listView.requestFocus();
                InputMethodManager IMM = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                IMM.hideSoftInputFromWindow(editTextSearch.getWindowToken(), 0);
                return false;
            }
        });

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (TextUtils.isEmpty(charSequence.toString().trim()))
                    listView.clearTextFilter(); //搜索文本为空时，清除ListView的过滤
                else
                    adapter.getFilter().filter(charSequence.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "退出");
        menu.add(0, 1, 1, "关于");
        menu.add(0, 2, 2, "更新日志");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int item_id = item.getItemId();
        switch (item_id) {
            case 0:
                finish();
                break;
            case 1:
                new AlertDialog.Builder(this).setIcon(R.mipmap.ic_launcher).setTitle("海天鹰应用分享 V1.4").setMessage("获取应用列表，分享发送安装包。\n作者：黄颖\nE-mail: sonichy@163.com\n源码：https://github.com/sonichy/AppShare\n\n参考：\n获取应用信息：https://www.cnblogs.com/jiuyi/p/5983304.html，http://blog.csdn.net/lishuangling21/article/details/50789715\nListView过滤：http://blog.csdn.net/zml_2015/article/details/52082174").setPositiveButton("确定", null).show();
                break;
            case 2:
                new AlertDialog.Builder(this).setIcon(R.mipmap.ic_launcher).setTitle("更新日志").setMessage("V1.4 (2019-02-14)\n增加应用更新日期，并按更新日期倒序排列。\n点击弹出详情，并可启动和分享。\n\nV1.3 (2018-10-01)\n过滤不区分大小写。\n在子线程中读取包列表，避免主线程阻塞，并增加进度条。\n\nV1.2 (2018-01-23)\n修复过滤后分享APP路径不对的问题。\n改动：点击启动应用，长按分享应用。\n\nV1.1 (2018-01-09)\n增加过滤功能。\n\nV1.0 (2018-01-01)\n获取应用列表，分享发送安装包。").setPositiveButton("确定", null).show();
                break;
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
            for (PackageInfo packageInfo : packages) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.setProgress(i);
                    }
                });
                Drawable icon = packageInfo.applicationInfo.loadIcon(PM);   // 获得应用的图标
                String appLabel = (String) packageInfo.applicationInfo.loadLabel(PM);   // 获得应用的Label
                String pkgName = packageInfo.packageName;   // 获得应用的包名
                String sourceDir = packageInfo.applicationInfo.sourceDir; // 获得应用的路径
                long lastUpdateTime = packageInfo.lastUpdateTime;   // 更新时间
                File file = new File(sourceDir);    //获取到应用安装包大小
                String pkgSize = Formatter.formatFileSize(MainActivity.this, file.length());
                AppInfo appInfo = new AppInfo();
                appInfo.setAppIcon(icon);
                appInfo.setAppLabel(appLabel);
                appInfo.setPkgName(pkgName);
                appInfo.setSourceDir(sourceDir);
                appInfo.setPkgSize(pkgSize);
                appInfo.setLastUpdateTime(lastUpdateTime);
                listAppInfo.add(appInfo);
                i++;
            }
            Collections.sort(listAppInfo, new lastUpdateTimeComparetor());
            adapter = new AppInfoAdapter(MainActivity.this, listAppInfo);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listView.setAdapter(adapter);
                    progressDialog.dismiss();
                }
            });
        }
    });

    class lastUpdateTimeComparetor implements Comparator<AppInfo> {
        @Override
        public int compare(AppInfo A1, AppInfo A2) {
            long d = A2.getLastUpdateTime() - A1.getLastUpdateTime();
            if(d > 0){
                return 1;
            }else if(d == 0){
                return 0;
            }else{
                return -1;
            }
        }
    }

}
