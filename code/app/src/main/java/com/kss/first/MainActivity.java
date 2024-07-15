package com.kss.first;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {


    private String dbName = "MYFREEZER.db";

    private DBHelper dbHelper;

    private ArrayList<Foods> freezeFood;     //저장한 음식을 나타내기 위한 arraylist이다.
    private MyListAdapter MyAdapter;         //listview를 나타내기 위해서 지정한것이다.

    final ArrayList<String> saveDate = new ArrayList<>();      //유통기한 하루전에 알림을 설정하기 위해서 나타내는 arraylist이다.

    private Button add, all, up, down, important, dummy;  //add는 추가, all은 모든 데이터, up은 냉동, down은 냉장, dummy는 유통기한이 지난것을 나타낸다.
    private TextView comment; //위에 버튼 누르면 바뀌는 것을 나타내기 위한 textview이다
    private ListView myList;  //음식의 정보를 list로 나타내기 위한 listview이다.

    private int deaded_food = 0;  //유통기한이 지난 음식을 나타낸다.

    private AlarmManager alarmManager;
    private GregorianCalendar mCalender;

    private NotificationManager notificationManager;
    NotificationCompat.Builder builder;

    private static String deadline(String days, int plus) {   //소비기한을 나타내기 위한 것이다.(또한, 특정알람을 설정할때도 사용)
        String eat_deadline = "";      //소비기한을 나타내는 변수
        try {                          //days는 유통기한, plus는 추가할 일수를 나타낸다.
            SimpleDateFormat transformat = new SimpleDateFormat("yyyy/MM/dd");
            Date date = transformat.parse(days);   //days를 데이터의 형식을 yyyy/MM/dd로 바꾼다.
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(date);                     // 시간 설정
            cal1.add(Calendar.DATE, plus);          //유통기한에서 plus일을 더한다.
            eat_deadline = new java.text.SimpleDateFormat("yyyy/MM/dd").format(cal1.getTime());

        } catch (ParseException e) {  //바꾸는 과저엥서 오류가 발생한 경우
            // TODO Auto-generated catch block
            e.printStackTrace();      //오류메시지를 나타낸다.
        }
        return eat_deadline;
    }

    private void setAlarm(String day) {   //알람을 설정하기 위한것이다. 여기서 day는 알람이 울리는 날짜이다.
        //AlarmReceiver에 값 전달
        Intent receiverIntent = new Intent(MainActivity.this, Alarm.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, receiverIntent, 0);
        String[] SP = day.split("/");   //SP는 날짜를 /을 기준으로 나눈것을 나타내는 것이다.
        day = String.join("-", SP);   //SP로 나눈 것을 다시 -로 조인해서 나타낸다.
        String from = day + " 09:00:00"; //해당 날짜의 9시에 울리게 한다,
        //날짜 포맷을 바꿔주는 것이다.
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date datetime = null;
        try {
            datetime = dateFormat.parse(from);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(datetime);
        alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent); //RTC는 UTC 표준시간을 기준으로 알람을 설정한다.
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        mCalender = new GregorianCalendar();

        dbHelper = new DBHelper(this, dbName);   //데이터베이스를 사용하기 위한것이다.
        freezeFood = new ArrayList<Foods>();            //음식의 정보를 나타내는 arraylist이다.


        dummy = (Button) findViewById(R.id.paste);       //dummy는 유통기한이 지난 음식을 표현
        comment = (TextView)findViewById(R.id.comment);  //comment는 dummy,all,up,down을 누를때마다 다른 말을 설정한 textview이다.
        myList = (ListView) findViewById(R.id.foodList); //음식 정보를 나타내는 listview이다.
        add = (Button) findViewById(R.id.add);           //음식을 추가하기 위한 버튼이다.
        all = (Button) findViewById(R.id.all);           //모든 음식을 나타내는 버튼
        up = (Button) findViewById(R.id.up);             //냉동 음식을 나타내는 버튼
        down = (Button) findViewById(R.id.down);         //냉장 음식을 나타내는 버튼
        important=(Button) findViewById(R.id.important); //즐겨찾기

        //여기서 incontext는 음식 정보를 편하게 보기위해서 나타낸것이다.
        MyAdapter = new MyListAdapter(this, R.layout.icontext, freezeFood);
        myList.setAdapter(MyAdapter);

        search_all_db();    //시작했을때 모든 음식 정보를 나타내기 위해서 모든 데이터를 부른다.
        if(saveDate.size()!=0){     //저장된 음식이 한개 이상일 경우
            for(int i = 0; i<saveDate.size(); i++){ //savedate에 크기만큼 반복
                SimpleDateFormat d_format = new SimpleDateFormat("yyyy/MM/dd");
                Calendar cal = Calendar.getInstance();
                String today = d_format.format(cal.getTime());
                if(today.equals(saveDate.get(i))){  //오늘날짜와 유통기한이 하루 남은 날짜가 같을 경우
                    setAlarm(saveDate.get(i));      //알람을 설정하여 울린다.
                }
            }
        }



        dummy.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                search_dummy_db();						// 유통기한이 지난 음식을 불러온다
                MyAdapter.notifyDataSetChanged();		// 리스트뷰 적용한다.
                comment.setText("유통기한 지난 음식");
            }
        });

        all.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                search_all_db();						// 모든음식의 정보를 불러온다
                MyAdapter.notifyDataSetChanged();		// 리스트뷰에 적용한다.
                comment.setText("보관된 음식 리스트");
            }
        });

        up.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                search_up_db();							// 냉동상태의 음식을 불러온다
                MyAdapter.notifyDataSetChanged();		// 리스트뷰에 적용한다.
                comment.setText("보관된 냉동 리스트");
            }
        });

        down.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                search_down_db();						// 냉장 상태의 음식을 불러온다
                MyAdapter.notifyDataSetChanged();		// 리스트뷰에 적용한다.
                comment.setText("보관된 냉장 리스트");
            }
        });

        important.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                search_important_db();							// 냉동상태의 음식을 불러온다
                MyAdapter.notifyDataSetChanged();		// 리스트뷰에 적용한다.
                comment.setText("보관된 즐겨찾기 리스트");
            }
        });


        add.setOnClickListener(new Button.OnClickListener() {  //추가 버튼을 클릭하면
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(getApplication(), AddActivity.class);  //addActivity로 이동한다.
                startActivity(intent);
            }
        });
        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                //음식의 정보를 LinearLayout형식으로 나타낸다.
                final LinearLayout linear = (LinearLayout) View.inflate(MainActivity.this, R.layout.info, null);

                TextView memo = (TextView) linear.findViewById(R.id.memo);
                memo.setText(freezeFood.get(position).memo);                     //음식의 메모를 받아와서 나타낸다.
                TextView eat_date=(TextView) linear.findViewById(R.id.eat_date); //음식의 소비기한을 나타내기 위한 것

                //음식종류가 특정 경우일때 소비기한을 표시한다.
                if((freezeFood.get(position).foodOption).equals("우유")){
                    eat_date.setText("소비기한: "+deadline(freezeFood.get(position).limitDate,50));
                }
                else if((freezeFood.get(position).foodOption).equals("치즈")){
                    eat_date.setText("소비기한: "+deadline(freezeFood.get(position).limitDate,70));
                }
                else if((freezeFood.get(position).foodOption).equals("요거트")){
                    eat_date.setText("소비기한: "+deadline(freezeFood.get(position).limitDate,10));
                }
                else if((freezeFood.get(position).foodOption).equals("달걀")){
                    eat_date.setText("소비기한: "+deadline(freezeFood.get(position).limitDate,25));
                }
                else if((freezeFood.get(position).foodOption).equals("식빵")){
                    eat_date.setText("소비기한: "+deadline(freezeFood.get(position).limitDate,20));
                }
                else if((freezeFood.get(position).foodOption).equals("두부")){
                    eat_date.setText("소비기한: "+deadline(freezeFood.get(position).limitDate,90));
                }
                else if((freezeFood.get(position).foodOption).equals("냉동만두")){
                    eat_date.setText("소비기한: "+deadline(freezeFood.get(position).limitDate,25));
                }
                else if((freezeFood.get(position).foodOption).equals("김치")){
                    eat_date.setText("소비기한: "+deadline(freezeFood.get(position).limitDate,180));
                }
                else if((freezeFood.get(position).foodOption).equals("케이크/크림빵")){
                    eat_date.setText("소비기한: "+deadline(freezeFood.get(position).limitDate,2));
                }
                else if((freezeFood.get(position).foodOption).equals("고추장")){
                    eat_date.setText("소비기한: "+deadline(freezeFood.get(position).limitDate,730));
                }
                else if((freezeFood.get(position).foodOption).equals("생면")){
                    eat_date.setText("소비기한: "+deadline(freezeFood.get(position).limitDate,9));
                }
                else{
                    eat_date.setText("소비기한: "+freezeFood.get(position).limitDate);
                }


                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("음식정보 (" + freezeFood.get(position).foodOption+ ")").setView(linear)
                        .setPositiveButton("확인", null).show();

            }
        });
        registerForContextMenu(myList);   //리스트에 대한 contextMenu를 설정한다.
    }
    public boolean onCreateOptionsMenu(Menu menu){  //옵션 메뉴를 실행했을 경우
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.optionmenu,menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.restart:  //갱신을 클릭하였을 경우
                try {
                    Intent intent = getIntent();
                    finish(); //현재 액티비티 종료 실시
                    overridePendingTransition(0, 0); //인텐트 애니메이션 없애기
                    startActivity(intent);                            //현재 액티비티 재실행 실시
                    overridePendingTransition(0, 0); //인텐트 애니메이션 없애기
                }
                catch (Exception e){        //에러 발생시
                    e.printStackTrace();    //에러 사항을 나타낸다.
                }
                return true;
            case R.id.allDelete:           // 전체삭제를 클릭하였을 경우
                delete_Allfood_db();   //모든데이터를 삭제한다.
                //다시 실행시켜서 리스트에 바로 적용시킨다.
                try {
                    Intent intent = getIntent();
                    finish(); //현재 액티비티 종료 실시
                    overridePendingTransition(0, 0); //인텐트 애니메이션 없애기
                    startActivity(intent);                            //현재 액티비티 재실행 실시
                    overridePendingTransition(0, 0); //인텐트 애니메이션 없애기
                }
                catch (Exception e){        //에러 발생시
                    e.printStackTrace();    //에러 사항을 나타낸다.
                }
                return true;
            case R.id.exit:
                moveTaskToBack(true);                               // 태스크를 백그라운드로 이동
                finishAndRemoveTask();                                      // 액티비티 종료 + 태스크 리스트에서 지우기
                android.os.Process.killProcess(android.os.Process.myPid()); // 앱 프로세스 종료
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu,v,menuInfo);
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo;
        int index; //index는 해당음식에 맞는 정보를 나타내는 숫자이다.
        switch(item.getItemId()){
            case R.id.enroll:
                menuInfo=(AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                index=menuInfo.position; //index에 메뉴의 순서를 저장한다.
                int Fid=freezeFood.get(index).foodId;
                update_food_db(Fid);     //즐겨찾기에 맞게 추가한다.
                search_important_db();   //즐겨찾기에 추가한 정보를 불러온다.
                return true;
            case R.id.delete:  //삭제를 클릭할 경우
                menuInfo=(AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                index=menuInfo.position;
                int id=freezeFood.get(index).foodId; //음식정보의 foodid를 가져와서
                delete_food_db(id);                  //foodid에 맞는 정보를 삭제한다.
                //다시 실행시켜준다.
                try {
                    Intent intent = getIntent();
                    finish(); //현재 액티비티 종료 실시
                    overridePendingTransition(0, 0); //인텐트 애니메이션 없애기
                    startActivity(intent);                            //현재 액티비티 재실행 실시
                    overridePendingTransition(0, 0); //인텐트 애니메이션 없애기
                }
                catch (Exception e){        //에러 발생시
                    e.printStackTrace();    //에러 사항을 나타낸다.
                }
                return true;
        }
        return super.onContextItemSelected(item);
    }

    class MyListAdapter extends BaseAdapter {   //리스트뷰에 데이터를 적용하기 위해서 어뎁트뷰를 사용한다.
        Context maincon;
        LayoutInflater Inflater;
        ArrayList<Foods> foodList;
        int layout;

        public MyListAdapter(Context _context, int _layout, ArrayList<Foods> _arSrc) {
            maincon = _context;
            Inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            foodList = _arSrc;
            layout = _layout;
        }

        @Override
        public int getCount() {
            return foodList.size();
        }  //음식리스트의 크기를 반환

        @Override
        public Object getItem(int position) {
            return foodList.get(position).foodName;
        }  //음식의 이름을 반환

        @Override
        public long getItemId(int position) {
            return position;
        }

        //리스트뷰를 그린다.
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = Inflater.inflate(layout, parent, false);
            }

            ImageView img = (ImageView) convertView.findViewById(R.id.img); //icontext.xml에서 냉장과 냉동일 경우 나타나는 이미지 이다.

            if ((foodList.get(position).Up_Down).equals("냉장")) { //냉장일때
                img.setImageResource(R.drawable.water);           //냉장 사진이 나타난다.
            } else {                                              //냉동일때
                img.setImageResource(R.drawable.ice);             //냉동에 맞는 사진이 나타난다.
            }

            TextView foodName = (TextView) convertView.findViewById(R.id.f_name);  //incontext.xml에서 윗부분에 음식의 이름을 나타낸다.
            foodName.setText(foodList.get(position).foodName);

            TextView limitData = (TextView) convertView.findViewById(R.id.l_date); //incontext.xml 아랫부분에서 유통기한을 나타낸다.
            limitData.setText(foodList.get(position).limitDate);

            return convertView;
        }
    }

    private void update_food_db(int id) {  //특정 음식의 정보를 즐겨찾기에 추가한다.
        SQLiteDatabase db;

        db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM MYFREEZER", null); //커서를 설정한다.
        int index=0;  //index는 saveDate의 순서를 표시하기 위한 것이다.
        if (cursor.getCount() > 0) {  //데이터가 있을 경우
            while (cursor.moveToNext()) {  //다음 커서에 데이터가 있을때까지 반복
                if(cursor.getInt(6)==id) {   //id와 커서 값이 같으면
                    String sql=String.format("UPDATE MYFREEZER SET foodName ='%s', limitedDate ='%s', foodOption='%s', up_down ='%s' , memo ='%s', important='%s', foodId=%s",cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3),
                            cursor.getString(4),"true",cursor.getString(6)+ " WHERE foodId =" + id);  //important값을 true로 바꿈
                    db.execSQL(sql);
                }

            }
        }
        cursor.close();
        dbHelper.close();

    }
    private void delete_food_db(int id) {  //음식의 정보를 삭제하기 위한 것이다.
        SQLiteDatabase db;

        db = dbHelper.getWritableDatabase();						// 데이터베이스를 읽기 위해서 불러온다.

        db.execSQL("DELETE FROM MYFREEZER WHERE foodId ="+id+";");  //음식정보의 id와 받은 id가 같은 것을 삭제한다.
        dbHelper.close();
    }
    private void delete_Allfood_db() {                              //모든 데이터를 삭제한다.
        SQLiteDatabase db;

        db = dbHelper.getWritableDatabase();						// 데이터베이스를 읽기 위해서 불러온다.

        db.execSQL("delete from MYFREEZER");                        //테이블의 모든데이터를 삭제한다.
        dbHelper.close();
    }

    private void search_all_db() {                                  //모든데이터를 불러오기위한 것이다.
        SQLiteDatabase db;

        freezeFood.clear();

        db = dbHelper.getReadableDatabase();

        deaded_food = 0;

        Cursor cursor = db.rawQuery("SELECT * FROM MYFREEZER", null); //커서를 설정한다.
        int index=0;  //index는 saveDate의 순서를 표시하기 위한 것이다.
        if (cursor.getCount() > 0) {  //데이터가 있을 경우
            while (cursor.moveToNext()) {  //다음 커서에 데이터가 있을때까지 반복
                Foods food = new Foods(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3),
                        cursor.getString(4), cursor.getString(5), cursor.getInt(6));  //음식의 정보를 받아온다.

                freezeFood.add(food); //음식정보를 freezeFood에 추가한다.

                if(saveDate.size()==0){ //데이터가 있고, 앱을 처음 실행했을 경우
                    saveDate.add(index,deadline(cursor.getString(1),-1)); //유통기한의 하루전 날짜를 추가한다.
                    index++;                                                     //index의 수를 증가한다.
                }
                //saveDate에 데이터와 유통기한 하루전의 날짜데이터가 같으면 (중복되면)
                else if(deadline(cursor.getString(1),-1).equals(saveDate.get(saveDate.size()-1))){
                    continue;
                }
                else{   //나머지 경우
                    saveDate.add(index,deadline(cursor.getString(1),-1));
                    index++;
                }

                boolean state = over_limit_date(cursor.getString(1));  //유통기한을 over_limit_date에 넣고 유통기한이 지났는지 확인한다.
                if (state) {                //유통기한이 지났으면
                    deaded_food++;          //유통기한이 지난 음식의 수를 증가.
                }
            }
        }
        else{
            Toast.makeText(getApplicationContext(),"음식정보를 입력하세요",Toast.LENGTH_SHORT).show();
        }

        dummy.setText(deaded_food + "/" + freezeFood.size());   //유통기한이 지났으면 유통기한이 지난 음식의 수를 나타낸다.

        cursor.close();
        dbHelper.close();

        deaded_food = 0;
    }

    private void search_dummy_db(){    //유통기한이 지난 음식을 불러오기 위한 것이다.
        SQLiteDatabase db;

        freezeFood.clear();

        db = dbHelper.getReadableDatabase();    //읽기위해서 데이터베이스를 불러온다.

        deaded_food = 0;

        Cursor cursor = db.rawQuery("SELECT * FROM MYFREEZER", null);
        //데이터가 있을 경우
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                boolean state = over_limit_date(cursor.getString(1)); //음식의 유통기한이 지났는지 확인

                if(state){ //유통기한이 지났으면
                    Foods food = new Foods(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3),
                            cursor.getString(4), cursor.getString(5), cursor.getInt(6));  //음식의 정보를 받아온다.
                    freezeFood.add(food); //유통기한이 지난 음식의 정보를 추가한다.
                }
            }
        }

        cursor.close();
        dbHelper.close();
    }


    private void search_up_db() {   //냉장일 경우의 데이터를 불러오기 위한것
        SQLiteDatabase db;

        freezeFood.clear();

        db = dbHelper.getReadableDatabase();           //읽기위해서 데이터를 불러온다.

        //냉장상태의 데이터를 불러온다.
        Cursor cursor = db.rawQuery("SELECT * FROM MYFREEZER where Up_Down = '냉동'", null);

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Foods food = new Foods(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3),
                        cursor.getString(4), cursor.getString(5), cursor.getInt(6));  //음식의 정보를 받아온다.
                freezeFood.add(food); //냉장 상태의 데이터를 불러와서 저장한다.
                boolean state = over_limit_date(cursor.getString(1));
                if (state) {
                    deaded_food++; //냉장상태의 유통기한이 지난 음식의 수를 표시한다.
                }
            }
        }

        dummy.setText(deaded_food + "/" + freezeFood.size());

        deaded_food = 0;

        cursor.close();
        dbHelper.close();
    }

    private void search_down_db() {  //냉장 상태의 데이터를 불러오기 위한것.
        SQLiteDatabase db;

        freezeFood.clear();

        db = dbHelper.getReadableDatabase();           //읽기위해서 데이터를 불러온다.

        //냉장상태의 데이터를 불러온다.
        Cursor cursor = db.rawQuery("SELECT * FROM MYFREEZER where Up_Down = '냉장'", null);

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Foods food = new Foods(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3),
                        cursor.getString(4), cursor.getString(5), cursor.getInt(6));  //음식의 정보를 받아온다.
                freezeFood.add(food); //냉장 상태의 데이터를 불러와서 저장한다.
                boolean state = over_limit_date(cursor.getString(1));
                if (state) {
                    deaded_food++; //냉장상태의 유통기한이 지난 음식의 수를 표시한다.
                }
            }
        }

        dummy.setText(deaded_food + "/" + freezeFood.size());

        deaded_food = 0;

        cursor.close();
        dbHelper.close();
    }

    private void search_important_db() {  //즐겨찾기에 추가한 정보를 불러온다.
        SQLiteDatabase db;

        freezeFood.clear();

        db = dbHelper.getReadableDatabase();           //읽기위해서 데이터를 불러온다.

        //즐겨찾기에 추가한 데이터를 불러온다.
        Cursor cursor = db.rawQuery("SELECT * FROM MYFREEZER where important = 'true'", null);

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Foods food = new Foods(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3),
                        cursor.getString(4), cursor.getString(5), cursor.getInt(6));  //음식의 정보를 받아온다.
                freezeFood.add(food); //즐겨찾기에 추가한데이터를 불러와서 저장한다.
                boolean state = over_limit_date(cursor.getString(1));
                if (state) {
                    deaded_food++; //냉장상태의 유통기한이 지난 음식의 수를 표시한다.
                }
            }
        }

        dummy.setText(deaded_food + "/" + freezeFood.size());

        deaded_food = 0;

        cursor.close();
        dbHelper.close();
    }

    private boolean over_limit_date(String f_date) {  //유통기한이 지났는지 확인
        try {
            // date format to compare
            SimpleDateFormat d_format = new SimpleDateFormat("yyyy/MM/dd");

            Calendar cal = Calendar.getInstance();

            String today = d_format.format(cal.getTime());

            Date date1 = d_format.parse(f_date);			// 유통기한 날짜를 형식에 맞추어 바꾼다.
            Date date2 = d_format.parse(today);				// 현제 날짜를 형식에 맞추어 바꾼다

            if (date1.before(date2) || date1.equals(date2)) {
                return true;                                //유통기한이 지났거나 유통기한이 오늘 날짜이면 true을 반환한다.
            }

        } catch (ParseException e) {                        //바꿀때 오류가 발생하면
            e.printStackTrace();                            //오류내용을 출력
        }

        return false;                                       //유통기한이 지나지 않았으면 false를 반환한다.
    }

    public class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name) {
            super(context, name, null,1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) { // primary key (식별할수 있는)를 foodId로 지정해서
            db.execSQL(                           //autoincrement (불러올때마다 숫자를 증가하게 하였다) table을 만든다.
                    "CREATE TABLE MYFREEZER (foodName TEXT, limitedDate TEXT, foodOption TEXT, up_down TEXT, memo TEXT, important TextfoodId, foodId integer primary key autoincrement);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS MYFREEZER");
            onCreate(db);
        }
    }
}
