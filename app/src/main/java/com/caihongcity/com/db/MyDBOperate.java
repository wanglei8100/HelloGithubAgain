package com.caihongcity.com.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.caihongcity.com.model.TerminalInfo;
import com.caihongcity.com.utils.LogUtil;

/**
 * 
 * @author yuanjigong
 * 数据库操作类
 */
public class MyDBOperate {

	private Context context;
	
	private SQLiteOpenHelper mySQLiteOpenHelper;

	public MyDBOperate(){
	}
	public MyDBOperate(Context context) {
		this.context = context;
		mySQLiteOpenHelper = new MySQLiteOpenHelper(context);
	}
	
	public void insert(TerminalInfo info){
		SQLiteDatabase teleportDB = mySQLiteOpenHelper.getWritableDatabase();
		teleportDB.beginTransaction();
		String sql_insert = "insert into terminalinfo (voucherNo,batchNo,terminalNo) values(?,?,?)";
		try{
			teleportDB.execSQL(sql_insert, new Object[]{info.getVoucherNo(),info.getBatchNo(),info.getTermianlNo()});
			teleportDB.setTransactionSuccessful();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			teleportDB.endTransaction();
			teleportDB.close();
		}
	}
	public void update(ContentValues values,String terminalNo){
		SQLiteDatabase teleportDB = mySQLiteOpenHelper.getWritableDatabase();
		teleportDB.beginTransaction();
//		String sql_insert = "insert into terminalinfo (voucherNo,batchNo,terminalNo) values(?,?,?)";
		try{
			teleportDB.update("terminalinfo", values, "terminalNo=?",new String[]{terminalNo});
//			teleportDB.execSQL(sql_insert, new Object[]{info.getVoucherNo(),info.getBatchNo(),info.getTermianlNo()});
			teleportDB.setTransactionSuccessful();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			teleportDB.endTransaction();
			teleportDB.close();
		}
	}
	
	public List<TerminalInfo> getTerminalInfo(String terminalNo){
		
		ArrayList<TerminalInfo> list = new ArrayList<TerminalInfo>();
		
		SQLiteDatabase teleportDB = mySQLiteOpenHelper.getReadableDatabase();
		teleportDB.beginTransaction();
		String sql_query = "select voucherNo,batchNo from terminalinfo where terminalNo=?" ;
		try{
			Cursor cur = teleportDB.rawQuery(sql_query,new String[]{terminalNo});
			int len = cur.getCount();
			LogUtil.syso("len=="+len);
			while(cur.moveToNext()){
				    TerminalInfo info = new TerminalInfo();
					String voucherNo = cur.getString(0);
					String batchNo = cur.getString(1);
					info.setVoucherNo(voucherNo);;
					info.setBatchNo(batchNo);;
					list.add(info);
			}
			teleportDB.setTransactionSuccessful();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			teleportDB.endTransaction();
			teleportDB.close();
		}
		LogUtil.syso("list.size=="+list.size());
		return list;
		
	}
	
	public List<TerminalInfo> getTerminalInfo(){
		
		ArrayList<TerminalInfo> list = new ArrayList<TerminalInfo>();
		
		SQLiteDatabase teleportDB = mySQLiteOpenHelper.getReadableDatabase();
		teleportDB.beginTransaction();
		String sql_query = "select voucherNo,batchNo from terminalinfo" ;
		try{
			Cursor cur = teleportDB.rawQuery(sql_query,null);
			int len = cur.getCount();
			LogUtil.syso("len=="+len);
			while(cur.moveToNext()){
				    TerminalInfo info = new TerminalInfo();
					String voucherNo = cur.getString(0);
					String batchNo = cur.getString(1);
					info.setVoucherNo(voucherNo);;
					info.setBatchNo(batchNo);;
					list.add(info);
			}
			teleportDB.setTransactionSuccessful();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			teleportDB.endTransaction();
			teleportDB.close();
		}
		LogUtil.syso("list.size=="+list.size());
		return list;
		
	}
	
}
