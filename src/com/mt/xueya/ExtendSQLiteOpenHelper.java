package com.mt.xueya;



import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ExtendSQLiteOpenHelper extends SQLiteOpenHelper{

	private final static String SQLiteOpen="SQLiteOpen";
	private final static int VERSION=1;
	private String table_name="bodyindex";

	public interface body{
		static final String id="id";
		static final String name="name";
		static final String sex="sex";
		static final String age="age";
		static final String highb="highb";
		static final String lowb="lowb";
		static final String pulse="pulse";
		static final String date="date";
		static final String time="time";
		static final String week="week";
	}

	public ExtendSQLiteOpenHelper(Context context, String name,CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}
	public ExtendSQLiteOpenHelper(Context context,String name,int version){
		this(context, name, null, version);
	}
	public ExtendSQLiteOpenHelper (Context context,String name) {
		this(context, name, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String sqlCreate="create table "+
				table_name+
				"("+
				body.id+" integer  primary key autoincrement,"+
				body.name+" varchar(10),"+
				body.sex+" varchar(5),"+
				body.age+" int,"+
				body.highb+ " int,"+
				body.lowb+" int,"+ 
				body.pulse+" int,"+
				body.date+" int,"+
				body.time +" varchar(20),"+
				body.week+" varchat(8)"+
				");";
		/*特别注意：sql语句中字段名  与  类型之间必须有空格，否则编译出错  */
		db.execSQL(sqlCreate);
		Log.e(SQLiteOpen, "Create SQLite");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		Log.e(SQLiteOpen, "updade database");
	}

}
