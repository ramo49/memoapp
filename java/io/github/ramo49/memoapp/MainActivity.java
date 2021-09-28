package io.github.ramo49.memoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    // ListView　用アダプタ
    SimpleAdapter mAdapter = null;
    // ListView に設定するデータ
    List<Map<String, String>> mList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ListView 用アダプタのリストを生成
        mList = new ArrayList<>();

        // ListView 用アダプタを生成
        mAdapter = new SimpleAdapter(
                this,
                mList,
                android.R.layout.simple_list_item_2,
                new String [] {"title", "content"},
                new int[] {android.R.id.text1, android.R.id.text2}
        );

        // ListView にアダプターをセット
        ListView list = (ListView)findViewById(R.id.listView);
        list.setAdapter(mAdapter);

        // ListView のアイテム選択イベント
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(
                    AdapterView<?> parent, View view, int pos, long id) {
                // 編集画面に渡すデータをセットし、表示
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra("NAME", mList.get(pos).get("filename"));
                intent.putExtra("TITLE", mList.get(pos).get("title"));
                intent.putExtra("CONTENT", mList.get(pos).get("content"));
                startActivity(intent);
            }
        });

        // ListView をコンテキストメニューに登録
        registerForContextMenu(list);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean returnVal = true;
        switch(item.getItemId()) {
            case R.id.action_add:
                //　[追加] 選択時の処理
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                startActivity(intent);
                break;
            default:
                returnVal = super.onOptionsItemSelected(item);
                break;
        }

        return returnVal;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ListView 用アダプタのデータをクリア
        mList.clear();

        // アプリの保存フォルダ内のファイル一覧を取得
        String savePath = this.getFilesDir().getPath().toString();
        File[] files = new File(savePath).listFiles();
        // ファイル名の降順でソート
        Arrays.sort (files, Collections.reverseOrder());
        // テキストファイル(*.txt)を取得し、ListView用アダプタのリストにセット
        for (int i=0; i<files.length; i++) {
            String fileName = files[i].getName();
            if (files[i].isFile() && fileName.endsWith(".txt")) {
                String title = null;
                String content = null;
                //　ファイルを読み込み
                try {
                    // ファイルオープン
                    InputStream in = this.openFileInput(fileName);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    char[] buf = new char[(int)files[i].length()];
                    // タイトル（1行目）を読み込み
                    title = reader.readLine();
                    // 内容（2行目以降）を読み込み
                    int num = reader.read(buf);
                    content = new String(buf, 0, num);
                    // ファイルクローズ
                    reader.close();
                    in.close();
                } catch (Exception e) {
                    Toast.makeText(this, "File read error!", Toast.LENGTH_LONG).show();
                }

                // ListView用のアダプタにデータをセット
                Map<String, String> map = new HashMap<>();
                map.put("filename", fileName);
                map.put("title", title);
                map.put("content", content);
                mList.add(map);
            }
        }
        // ListView のデータ変更を表示に反映
        mAdapter.notifyDataSetChanged();
    }

    // コンテキストメニュー作成処理
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo info) {
        super.onCreateContextMenu(menu, view, info);
        getMenuInflater().inflate(R.menu.main_context, menu);
    }
    // コンテキストメニュー選択処理
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        switch(item.getItemId()) {
            case R.id.context_del:
                // [削除] 選択時の処理
                // ファイル削除
                if (this.deleteFile(mList.get(info.position).get("filename"))) {
                    Toast.makeText(this, R.string.msg_del, Toast.LENGTH_SHORT).show();
                }
                // リストから削除
                mList.remove(info.position);
                // ListView のデータ変更を表示に反映
                mAdapter.notifyDataSetChanged();
                break;
            default:
                break;
        }

        return false;
    }
}