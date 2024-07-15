package com.kss.first;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class AddActivity extends AppCompatActivity {   //음식의 데이터를 추가하기 위한 액티비티이다.

	private String dbName = "MYFREEZER.db";    //데이터베이스의 이름을 MYFREEZER.db라고 지정한다.

	private DBHelper dbHelper;                 //데이터베이스를 사용하기 위해 DBHelper를 사용한다.


	private Button save, limitDate;            //save는 데이터를 저장하기 위한 버튼, limitdate는 유통기한을 설정하기 위하 버튼이다.
	private EditText foodName, memo;           //foodName은 음식의 이름, memo는 음식의 메모를 나타내는 것이다.
	private Switch sw;                         //switch는 냉장인지 냉동인지를 나타내기 위해 사용한것이다.
	private String option;                     //option은 음식의 종류를 나타내기 위한 변수.
	private Spinner spinner;				   //spinner는 음식의 종류를 spinner로 나타내기 위한 것이다.



	private String deadDate, showDate;         //유통기한을 나타내기 위해서 사용한 것이다.

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add);

		dbHelper = new DBHelper(this, dbName);

		foodName = (EditText) findViewById(R.id.food_name);
		limitDate = (Button) findViewById(R.id.limitDate);
		memo = (EditText) findViewById(R.id.memo);
		sw = (Switch) findViewById(R.id.up_down);
		save = (Button) findViewById(R.id.add_save);


		final Calendar cal = Calendar.getInstance();  //날짜를 불러오기 위해서 나타낸 것


		showDate = "유통기한 : " + cal.get(Calendar.YEAR) + "/" + ((cal.get(Calendar.MONTH)) + 1) + "/" + cal.get(Calendar.DATE);
		deadDate = cal.get(Calendar.YEAR) + "/" + ((cal.get(Calendar.MONTH)) + 1) + "/" + cal.get(Calendar.DATE);

		limitDate.setText(showDate); //위에 지정한 것으로 유통기한의 날짜를 나타낸다.


		limitDate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { //유통기한을 누르면
				DatePickerDialog dateDialog = new DatePickerDialog(AddActivity.this, listener, cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH)), cal.get(Calendar.DATE));
				dateDialog.show();  //달력이 나와서 날짜를 설정할수 있게 하였다.
			}
		});

		spinner=(Spinner) findViewById(R.id.option_spinner);     //음식의 종류를 spinner로 설정할수 있게 하엿다.
		//리소스로부터 ArrayAdapter를 생성하는 메소드이다.
		ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(this,R.array.option_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); //사용자가 클릭하여 항목이 오픈될 때에 항목을 표시하는 뷰의 모양을 정의
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id){//스피너에서 선택된 경우
				option=adapterView.getItemAtPosition(pos).toString();                           //선택한 것을 String으로 바꾸어 option에 저장
				((TextView)adapterView.getChildAt(0)).setTextColor(Color.WHITE);
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {       //스피너에서 선택되지 않은 경우에 호출된다.
			}
		});

		save.setOnClickListener(new OnClickListener() {   //save(저장버튼을 클릭한 경우)
			@Override
			public void onClick(View v) {
				SQLiteDatabase db;

				db = dbHelper.getWritableDatabase();			//쓰기위해서 데이터 베이스를 불러온다.

				String name = "";							    //음식의 이름을 나타내기 위한 변수
				String mo = "";									//음식그이 메모를 나타내기 위한 변수
				String state = "";								//음식이 냉장인지 냉동인지를 나타내기 위한 변수


				name = foodName.getText().toString();			//음식이름에 입력한 것을 name에 저장한다.
				mo = memo.getText().toString();					//음식의 메모에 입력한 것을 mo에 저장한다.

				if (sw.isChecked()) {							//sw가 체크되어있을경우
					state = "냉장";								//state는 냉장이고
				} else {
					state = "냉동";								//아니면 state는 냉동으로 저장한다.
				}


				if (!name.equals("") ) {   //음식의 이름이 있을경우
					String sql = String.format("INSERT INTO MYFREEZER VALUES('%s', '%s', '%s','%s' ,'%s', '%s', null)", name, deadDate, option,
							state, mo, "false"); //이름, 유통기한, 음식종류, 상태, 메모를 삽입하기 위해서 sql에 저장한다.
					db.execSQL(sql);    //실제 데이터 베이스에 삽입하기 위해서 실행한다.

					Intent intent = new Intent(getApplication(), MainActivity.class);  //데이터 베이스에 보내고나면
					startActivity(intent);											   //메인 액티비티로 이동한다.
					finish();
				}else{  //음식의 이름이 없을 경우
					Toast.makeText(getApplicationContext(), "이름은 필수입니다.", Toast.LENGTH_LONG).show();
				}
			}
		});
	}


	OnDateSetListener listener = new OnDateSetListener() {
		public void onDateSet(android.widget.DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			showDate = "유통기한 : " + year + "/" + (monthOfYear + 1) + "/" + dayOfMonth; //날짜를 받아와서 나타낸다.
			deadDate = year + "/" + (monthOfYear + 1) + "/" + dayOfMonth;

			limitDate.setText(showDate);
		};
	};

	public class DBHelper extends SQLiteOpenHelper {

		public DBHelper(Context context, String name) {
			super(context, name, null,1);
		}

		@Override
		public void onCreate(SQLiteDatabase db) { // primary key (식별할수 있는)를 foodId로 지정해서
			db.execSQL(                           //autoincrement (불러올때마다 숫자를 증가하게 하였다) table을 만든다.
					"CREATE TABLE MYFREEZER (foodName TEXT, limitedDate TEXT, foodOption TEXT, up_down TEXT, memo TEXT, important Text, foodId integer primary key autoincrement);");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS MYFREEZER");
			onCreate(db);
		}
	}
}
