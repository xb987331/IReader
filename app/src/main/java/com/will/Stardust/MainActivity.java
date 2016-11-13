package com.will.Stardust;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.will.Stardust.adapter.BookListAdapter;
import com.will.Stardust.base.BaseActivity;
import com.will.Stardust.bean.Book;
import com.will.Stardust.common.SPHelper;
import com.will.Stardust.common.Util;
import com.will.Stardust.db.DBHelper;
import com.will.filesearcher.file_searcher.FileSearcherActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by will on 2016/10/29.
 */

public class MainActivity extends BaseActivity {
    private static final int GET_DATA_REQUEST = 888;
    private static final int RESTART_REQUEST = 123;
    private Toast mToast;
    private BookListAdapter mAdapter;

    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        setTheme(SPHelper.getInstance().isNightMode() ? R.style.AppNightTheme : R.style.AppDayTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new BookListAdapter(this);
        mAdapter.setOnClickCallback(new BookListAdapter.ClickCallback() {
            @Override
            public void onClick(Book book) {
                Intent intent = new Intent(MainActivity.this,ReadingActivity.class);
                intent.putExtra("book",book);
                startActivityForResult(intent,RESTART_REQUEST);
            }
            @Override
            public void onLongClick() {
                turnIntoMoveMode(true);
            }
        });
        recyclerView.setAdapter(mAdapter);
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_search:
                Intent intent = new Intent(this, FileSearcherActivity.class);
                intent.putExtra("keyword",".txt");
                startActivityForResult(intent, GET_DATA_REQUEST);
                break;
            case R.id.menu_delete:
                if(mAdapter.isAllowMove()){
                  turnIntoMoveMode(false);
                }else{
                   turnIntoMoveMode(true);
                }
                break;
            case R.id.menu_delete_all:
                showDeleteAllDialog();
        }

        return true;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == GET_DATA_REQUEST && resultCode == FileSearcherActivity.OK && data != null){
            ArrayList<File> fileList = (ArrayList<File>) data.getSerializableExtra("data");
            List<Book> dataList = new ArrayList<>();
            for(File file :fileList){
                dataList.add(new Book(file.getName(),file.getPath()));
            }
            mAdapter.addData(dataList);
        }
        if(requestCode == RESTART_REQUEST && resultCode == Activity.RESULT_OK){
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if(mAdapter.isAllowMove()){
           turnIntoMoveMode(false);
        }else{
            showExitToast();
        }

    }
    private void turnIntoMoveMode(boolean which){
       if(which){
           mAdapter.allowMove(true);
           toolbar.setTitle(getResources().getString(R.string.remove_to_delete));
           //Util.makeToast("进入管理，左右滑动书籍删除");
       }else{
           mAdapter.allowMove(false);
           toolbar.setTitle(getResources().getString(R.string.app_name));
           Util.makeToast("已退出管理模式");
       }
    }
    private void showExitToast(){
        if(mToast == null){
            mToast = Toast.makeText(this,"",Toast.LENGTH_SHORT);
        }
        if (mToast.getView().getParent() == null){
            mToast.setText("再次点击返回退出应用");
            mToast.show();
        }else{
            super.onBackPressed();
        }
    }
    private void showDeleteAllDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.AppDialogTheme);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mAdapter.clearData();
                DBHelper.getInstance().clearAllData();
                SPHelper.getInstance().clearAllBookMarkData();
                Util.makeToast("已删除");
            }
        });
        builder.setMessage("确认删除全部书籍？");
        builder.setNegativeButton("取消",null);
        builder.show();
    }
}
