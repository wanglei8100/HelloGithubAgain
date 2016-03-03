package com.caihongcity.com.common;

import com.caihongcity.com.R;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 
 * @author yuanjigong
 * 确认弹出框
 */
public class InputDialog extends Dialog{
	
	private Context context;
	private String showContent;
	private static InputDialog myDialog = null;
	private InputDialog(Context context,String showContent){
		super(context);
		this.context = context;
		this.showContent = showContent;
	}
	
	public static Dialog getDialog(Context context,String showContent){
		if(myDialog==null){
			myDialog = new InputDialog(context, showContent);
			myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			View view  = LayoutInflater.from(context).inflate(R.layout.confirminputdialog, null);
			myDialog.setContentView(view);
			TextView con= (TextView) myDialog.findViewById(R.id.confirm_prompt);
			LinearLayout confirm = (LinearLayout) myDialog.findViewById(R.id.confirm_layout);
			confirm.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(myDialog!=null){
					myDialog.dismiss();
					}
				}
			});
			con.setText(showContent);
		}
		
		return myDialog;
	}
	
}
