package vip.okfood.appshow;

import android.app.ProgressDialog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView listUI = findViewById(R.id.listUI);
        listUI.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(new ColorDrawable(Color.parseColor("#DDDDDD")));
        listUI.addItemDecoration(itemDecoration);
        listUI.setAdapter(mAdapter = new MyAdapter());
    }

    ProgressDialog mLoading;

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    private void refreshList() {
        if(mLoading == null) mLoading = ProgressDialog.show(this, null, "加载中");
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<AppInfo> datas    = new ArrayList<>();
                List<PackageInfo>   packages = getPackageManager().getInstalledPackages(0);
                for(int i = 0; i < packages.size(); i++) {
                    PackageInfo packageInfo = packages.get(i);
                    if(!showAll) {
                        if((packageInfo.applicationInfo.flags&ApplicationInfo.FLAG_SYSTEM) == 0) {
                            AppInfo appInfo = new AppInfo(
                                    packageInfo.applicationInfo.loadLabel(getPackageManager()).toString(),
                                    packageInfo.packageName,
                                    packageInfo.versionName,
                                    packageInfo.versionCode,
                                    packageInfo.applicationInfo.loadIcon(getPackageManager())
                            );
                            datas.add(appInfo);
                        }
                    } else {
                        AppInfo appInfo = new AppInfo(
                                packageInfo.applicationInfo.loadLabel(getPackageManager()).toString(),
                                packageInfo.packageName,
                                packageInfo.versionName,
                                packageInfo.versionCode,
                                packageInfo.applicationInfo.loadIcon(getPackageManager())
                        );
                        datas.add(appInfo);
                    }
                }
                getWindow().getDecorView().post(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.refreshDataList(datas);
                        if(mLoading != null) {
                            mLoading.dismiss(); mLoading.cancel();
                            mLoading = null;
                        }
                    }
                });
            }
        }).start();
    }

    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        private List<AppInfo> mDataList = new ArrayList<>();

        void refreshDataList(List<AppInfo> datas) {
            mDataList.clear();
            if(datas != null) mDataList.addAll(datas);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new MyViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_app, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder viewHolder, int position) {
            AppInfo appInfo = mDataList.get(position);
            viewHolder.icon.setImageDrawable(appInfo.appIcon);
            viewHolder.name.setText(String.format(Locale.getDefault(), "%s -%s(%d)", appInfo.appName, appInfo.versionName, appInfo.versionCode));
            viewHolder.packageName.setText(appInfo.packageName);
        }

        @Override
        public int getItemCount() {
            return mDataList.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView icon;
            TextView  name, packageName;

            MyViewHolder(@NonNull View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.icon);
                name = itemView.findViewById(R.id.name);
                packageName = itemView.findViewById(R.id.packageName);
            }
        }
    }

    public class AppInfo {
        String   appName;
        String   packageName;
        String   versionName;
        int      versionCode;
        Drawable appIcon;

        AppInfo(String appName, String packageName, String versionName, int versionCode, Drawable appIcon) {
            this.appName = appName;
            this.packageName = packageName;
            this.versionName = versionName;
            this.versionCode = versionCode;
            this.appIcon = appIcon;
        }
    }

    private boolean showAll;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.showAll) {
            showAll = !showAll;
            item.setTitle(showAll ? "显示非系统应用" : "显示系统应用");
            refreshList();
        }
        return false;
    }
}
