
package com.example.myloverspace;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.view.View.GONE;

//import androidx.core.view.LayoutInflaterCompat;

//import android.widget.Toolbar;
//import android.support.v7.widget.Toolbar;     报错 cannot resolve v7 更改下行代码问题解决

public class MainActivity extends BaseActivity implements OnItemClickListener,OnItemLongClickListener {

    private NoteDatabase dbHelper;
    //final String TAG= "main";
    FloatingActionButton fab;
    //TextView tv;
    private ListView lv;       //可滚动
    private LinearLayout lv_layout;

    private Context context = this;
    private NoteAdapter adapter;
    private List<Note> noteList = new ArrayList<>();
    private Toolbar myToolbar;


    //弹出菜单
    private PopupWindow popuWindow;
    private PopupWindow popupCover;
    private ViewGroup CustomView;
    private ViewGroup CoverView;
    private LayoutInflater layoutInflater;
    private WindowManager wm;
    private RelativeLayout main;
    private DisplayMetrics metrics;
    private TagAdapter tagAdapter;

    private TextView setting_text;
    private ImageView setting_image;
    private ListView lv_tag;
    private TextView add_tag;

    private SharedPreferences sharedPreferences;

    String[] list_String = {"before one month", "before three months", "before six months", "before one year"};

/*
    //tag标签
    private ArrayList<String> dataList =new ArrayList();
    private ArrayList<Integer> colorList = new ArrayList();
    private RecyclerView rv;
    private String[] arr= {"aaaaaaaa.jpg","巴拉巴.jpg","我的下朋友企业.jpg","新中.jpg","我的好朋友是不同的风格类型哦哦哦哦哦","开心","测试tag标签"};
*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();


        if (super.isNightMode())
            myToolbar.setNavigationIcon(getDrawable(R.drawable.ic_menu_white_24dp));
        else myToolbar.setNavigationIcon(getDrawable(R.drawable.ic_menu_black_24dp)); // 三道杠

        myToolbar.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopUpWindow();
            }
        });
    }


    public void showPopUpWindow(){
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        popupCover = new PopupWindow(CoverView, width, height, false);
        popuWindow = new PopupWindow(CustomView, (int)(width*0.7), height,true);
        if (isNightMode()) popuWindow.setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));
        popuWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));

        //用post推迟加载,在主界面加载成功后显示弹出
        findViewById(R.id.main_layout).post(new Runnable() {
            @Override
            public void run() {
                popupCover.showAtLocation(main, Gravity.NO_GRAVITY,0,0);
                popuWindow.showAtLocation(main, Gravity.NO_GRAVITY,0,0);

                setting_image = CustomView.findViewById(R.id.setting_settings_image);
                setting_text = CustomView.findViewById(R.id.setting_settings_text);

                lv_tag = CustomView.findViewById(R.id.lv_tag);
                add_tag = CustomView.findViewById(R.id.add_tag);

                add_tag.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (sharedPreferences.getString("tagListString","").split("_").length < 8) {
                            final EditText et = new EditText(context);
                            new AlertDialog.Builder(MainActivity.this)
                                    .setMessage("Enter the name of tag")
                                    .setView(et)
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            List<String> tagList = Arrays.asList(sharedPreferences.getString("tagListString", null).split("_")); //获取tags

                                            String name = et.getText().toString();
                                            if (!tagList.contains(name)) {
                                                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                                                String oldTagListString = sharedPreferences.getString("tagListString", null);
                                                String newTagListString = oldTagListString + "_" + name;
                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                editor.putString("tagListString", newTagListString);
                                                editor.commit();
                                                refreshTagList();
                                            }
                                            else Toast.makeText(context, "Repeated tag!", Toast.LENGTH_SHORT).show();
                                        }
                                    }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).create().show();
                        }
                        else{
                            Toast.makeText(context, "自定义的标签够多了！", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                List<String> tagList = Arrays.asList(sharedPreferences.getString("tagListString", null).split("_")); //获取tags
                tagAdapter = new TagAdapter(context, tagList, numOfTagNotes(tagList));
                lv_tag.setAdapter(tagAdapter);

                lv_tag.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        List<String> tagList = Arrays.asList(sharedPreferences.getString("tagListString", null).split("_")); //获取tags
                        int tag = position + 1;
                        List<Note> temp = new ArrayList<>();
                        for (int i = 0; i < noteList.size(); i++) {
                            if (noteList.get(i).getTag() == tag) {
                                Note note = noteList.get(i);
                                temp.add(note);
                            }
                        }
                        NoteAdapter tempAdapter = new NoteAdapter(context, temp);
                        lv.setAdapter(tempAdapter);
                        myToolbar.setTitle(tagList.get(position));
                        popuWindow.dismiss();
                        Log.d(TAG, position + "");
                    }
                });

                lv_tag.setOnItemLongClickListener(new OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                        if (position > 4) {
                            resetTagsX(parent);
                            float length = getResources().getDimensionPixelSize(R.dimen.distance);
                            TextView blank = view.findViewById(R.id.blank_tag);
                            blank.animate().translationX(length).setDuration(300).start();
                            TextView text = view.findViewById(R.id.text_tag);
                            text.animate().translationX(length).setDuration(300).start();
                            ImageView del = view.findViewById(R.id.delete_tag);
                            del.setVisibility(View.VISIBLE);
                            del.animate().translationX(length).setDuration(300).start();

                            del.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    new AlertDialog.Builder(MainActivity.this)
                                            .setMessage("All related notes will be tagged as \"no tag\" !")
                                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    int tag = position + 1;
                                                    for (int i = 0; i < noteList.size(); i++) {
                                                        //被删除tag的对应notes tag = 1
                                                        Note temp = noteList.get(i);
                                                        if (temp.getTag() == tag) {
                                                            temp.setTag(1);
                                                            CRUD op = new CRUD(context);
                                                            op.open();
                                                            op.updateNote(temp);
                                                            op.close();
                                                        }
                                                    }
                                                    List<String> tagList = Arrays.asList(sharedPreferences.getString("tagListString", null).split("_")); //获取tags
                                                    if(tag + 1 < tagList.size()) {
                                                        for (int j = tag + 1; j < tagList.size() + 1; j++) {
                                                            //大于被删除的tag的所有tag减一
                                                            for (int i = 0; i < noteList.size(); i++) {
                                                                Note temp = noteList.get(i);
                                                                if (temp.getTag() == j) {
                                                                    temp.setTag(j - 1);
                                                                    CRUD op = new CRUD(context);
                                                                    op.open();
                                                                    op.updateNote(temp);
                                                                    op.close();
                                                                }
                                                            }
                                                        }
                                                    }

                                                    //edit the preference
                                                    List<String> newTagList = new ArrayList<>();
                                                    newTagList.addAll(tagList);
                                                    newTagList.remove(position);
                                                    String newTagListString = TextUtils.join("_", newTagList);
                                                    Log.d(TAG, "onClick: " + newTagListString);
                                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                                    editor.putString("tagListString", newTagListString);
                                                    editor.commit();

                                                    refreshTagList();
                                                }
                                            }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).create().show();
                                }
                            });

                            return true;
                        }
                        return false;
                    }
                });


                //点击事件
                setting_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(MainActivity.this, UserSettingsActivity.class));
                    }
                });
                setting_text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(MainActivity.this, UserSettingsActivity.class));
                    }
                });
                //touch事件
                CoverView.setOnTouchListener(new View.OnTouchListener(){
                    @Override
                    public boolean onTouch(View v, MotionEvent event){
                        popuWindow.dismiss();;
                        return true;
                    }
                });

                popuWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        popupCover.dismiss();
                    }
                });
            }
        });
    }

    private void refreshTagList() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        List<String> tagList = Arrays.asList(sharedPreferences.getString("tagListString", null).split("_")); //获取tags
        tagAdapter = new TagAdapter(context, tagList, numOfTagNotes(tagList));
        lv_tag.setAdapter(tagAdapter);
        tagAdapter.notifyDataSetChanged();
    }

    private void resetTagsX(AdapterView<?> parent) {
        for (int i = 5; i < parent.getCount(); i++) {
            View view = parent.getChildAt(i);
            if (view.findViewById(R.id.delete_tag).getVisibility() == View.VISIBLE) {
                float length = 0;
                TextView blank = view.findViewById(R.id.blank_tag);
                blank.animate().translationX(length).setDuration(300).start();
                TextView text = view.findViewById(R.id.text_tag);
                text.animate().translationX(length).setDuration(300).start();
                ImageView del = view.findViewById(R.id.delete_tag);
                del.setVisibility(GONE);
                del.animate().translationX(length).setDuration(300).start();
            }
        }
    }

    @Override
    protected void needRefresh(){
        setNightMode();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("opMode", 10);
        startActivity(intent);
        if (popuWindow.isShowing()) popuWindow.dismiss();
        finish();
    }


    public void initView(){
        initPrefs();
        fab = findViewById(R.id.fab);
        lv = findViewById(R.id.lv);
        lv_layout = findViewById(R.id.lv_layout);
        myToolbar = findViewById(R.id.my_toolbar);

        adapter = new NoteAdapter(getApplicationContext(), noteList);
        refreshListView();
        lv.setAdapter(adapter);

        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra("mode", 4);     // MODE of 'new note'
                startActivityForResult(intent, 1);      //collect data from edit

            }
        });

        lv.setOnItemClickListener(this);
        lv.setOnItemLongClickListener(this);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //设置toolbar取代actionbar
        initPopupView();

    }

    public void initPopupView() {
        //instantiate the popup.xml layout file
        layoutInflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        CustomView = (ViewGroup) layoutInflater.inflate(R.layout.setting_layout, null);
        CoverView = (ViewGroup) layoutInflater.inflate(R.layout.setting_cover, null);

        main = findViewById(R.id.main_layout);
        //instantiate popup window
        wm = getWindowManager();
        metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);

    }

    private void initPrefs() {
        //initialize all useful SharedPreferences for the first time the app runs

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (!sharedPreferences.contains("nightMode")) {
            editor.putBoolean("nightMode", false);
            editor.commit();
        }


        if (!sharedPreferences.contains("tagListString")) {
            String s = "no tag_life_study_work_play";
            editor.putString("tagListString", s);
            editor.commit();
        }

        if(!sharedPreferences.contains("noteTitle")){
            editor.putBoolean("noteTitle", true);
            editor.commit();
        }


    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem mSearch = menu.findItem(R.id.action_search);
        SearchView mSearchView = (SearchView) mSearch.getActionView();

        mSearchView.setQueryHint("Search");
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });
        final int mode = 1;
        final String itemName = "notes" ;
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                final View view = findViewById(R.id.menu_clear);

                if (view != null) {
                    view.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("Delete all "+ itemName);
                            builder.setIcon(R.drawable.ic_error_outline_black_24dp);
                            builder.setItems(list_String, new DialogInterface.OnClickListener() {//列表对话框；
                                @Override
                                public void onClick(DialogInterface dialog, final int which) {//根据这里which值，即可以指定是点击哪一个Item；
                                    new AlertDialog.Builder(MainActivity.this)
                                            .setMessage("Do you want to delete all " + itemName + " " + list_String[which] + "? ")
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int a) {
                                                            Log.d(TAG, "onClick: " + which);
                                                            removeSelectItems(which, mode);
                                                            refreshListView();
                                                        }
                                                        //根据模式与时长删除对顶的计划s/笔记s
                                                        private void removeSelectItems(int which, int mode) {
                                                            int monthNum = 0;
                                                            switch (which){
                                                                case 0:
                                                                    monthNum = 1;
                                                                    break;
                                                                case 1:
                                                                    monthNum = 3;
                                                                    break;
                                                                case 2:
                                                                    monthNum = 6;
                                                                    break;
                                                                case 3:
                                                                    monthNum = 12;
                                                                    break;
                                                        }
                                                            Calendar rightNow = Calendar.getInstance();
                                                            rightNow.add(Calendar.MONTH,-monthNum);//日期加3个月
                                                            Date selectDate = rightNow.getTime();
                                                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                            String selectDateStr = simpleDateFormat.format(selectDate);
                                                            Log.d(TAG, "removeSelectItems: " + selectDateStr);
                                                            switch(mode){
                                                                case 1: //notes
                                                                    dbHelper = new NoteDatabase(context);
                                                                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                                                                    Cursor cursor = db.rawQuery("select * from notes" ,null);
                                                                    while(cursor.moveToNext()){
                                                                        if (cursor.getString(cursor.getColumnIndex(NoteDatabase.TIME)).compareTo(selectDateStr) < 0){
                                                                            db.delete("notes", NoteDatabase.ID + "=?", new String[]{Long.toString(cursor.getLong(cursor.getColumnIndex(NoteDatabase.ID)))});
                                                                        }
                                                                    }
                                                                    db.execSQL("update sqlite_sequence set seq=0 where name='notes'"); //reset id to 1
                                                                    refreshListView();
                                                                    break;
                                                            }
                                                        }
                                            }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).create().show();
                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            return true;
                        }
                    });
                }
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.menu_clear:
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("delete all?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dbHelper = new NoteDatabase(context);
                                SQLiteDatabase db = dbHelper.getWritableDatabase();
                                db.delete("notes", null, null);//delete data in table NOTES
                                db.execSQL("update sqlite_sequence set seq=0 where name='notes'"); //reset id to 1
                                refreshListView();
                            }
                        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                }).create().show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void refreshListView(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        //initialize CRUD
        CRUD op = new CRUD(context);
        op.open();
        // set adapter
        if(noteList.size() > 0) noteList.clear();
        noteList.addAll(op.getALLNotes());
        op.close();
        adapter.notifyDataSetChanged();

    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()){
            case R.id.lv:
                Note curNote = (Note) parent.getItemAtPosition(position);
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra("content", curNote.getContent());
                intent.putExtra("id", curNote.getId());
                intent.putExtra("time", curNote.getTime());
                intent.putExtra("mode",3);      //click to edit
                intent.putExtra("tag", curNote.getTag());
                startActivityForResult(intent, 1);
                break;
        }
    }

    //接受 startActivityForResult的结果  //checked
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        int returnMode;
        long note_id;
        returnMode = data.getExtras().getInt("mode",-1);
        note_id = data.getExtras().getLong("id",0);

        if(returnMode == 1){
            String content = data.getExtras().getString("content");
            String time = data.getExtras().getString("time");
            int tag = data.getExtras().getInt("tag",1);
            Note newNote = new Note(content, time, tag);
            newNote.setId(note_id);
            CRUD op = new CRUD(context);
            op.open();
            op.updateNote(newNote);
            op.close();
        }
        else if(returnMode == 0){ //cearte new note
            String content = data.getExtras().getString("content");
            String time = data.getExtras().getString("time");
            int tag = data.getExtras().getInt("tag",1);
            Note newNote = new Note(content, time, tag);
            CRUD op = new CRUD(context);
            op.open();
            op.addNote(newNote);
            op.close();
        }
        else if(returnMode == 2){ // delete
            Note curNote = new Note();
            curNote.setId(note_id);
            CRUD op = new CRUD(context);
            op.open();
            op.removeNote(curNote);
            op.close();
        }
        refreshListView();
        super.onActivityResult(requestCode, resultCode, data);  //有。问题
        //

        //Log.d(TAG,edit);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()){
            case R.id.lv:
                final Note note = noteList.get(position);
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("Do you want to delete this note ?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                CRUD op = new CRUD(context);
                                op.open();
                                op.removeNote(note);
                                op.close();
                                refreshListView();
                            }
                        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
                break;
        }
        return true;
    }


    //统计不同标签的笔记数
    public List<Integer> numOfTagNotes(List<String> noteStringList){
        Integer[] numbers = new Integer[noteStringList.size()];
        for(int i = 0; i < numbers.length; i++) numbers[i] = 0;
        for(int i = 0; i < noteList.size(); i++){
            numbers[noteList.get(i).getTag() - 1] ++;
        }
        return Arrays.asList(numbers);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}