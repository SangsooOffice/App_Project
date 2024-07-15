package com.kss.first;

public class Foods { //음식의 데이터를 지정한 클래스이다
	public Foods(String foodName, String limitDate, String foodOption, String Up_Down, String memo, String important, int foodId) {
		this.foodName = foodName;  //음식의 이름을 나타내는 변수
		this.limitDate = limitDate;  //음식의 유통기한을 나타내는 변수
		this.foodOption=foodOption;  //음식의 종류를 나타내는 변수
		this.Up_Down = Up_Down; //음식이 냉동인지 냉장인지를 나타내는 변수
		this.memo = memo;   //음식의 메모를 나타내는 변수
		this.important=important;
		this.foodId=foodId; //음식의 고유 id를 나타내는 변수
	}
	String foodName;
	String limitDate;
	String foodOption;
	String Up_Down;
	String memo;
	String important;
	int foodId;
}
