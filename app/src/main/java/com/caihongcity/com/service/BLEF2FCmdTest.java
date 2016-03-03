package com.caihongcity.com.service;


import android.content.Context;
import android.util.Log;

import com.itron.android.bluetooth.DeviceSearchListener;
import com.itron.android.ftf.Util;
import com.itron.android.lib.Logger;
import com.itron.protol.android.BLECommandController;
import com.itron.protol.android.CommandReturn;
import com.itron.protol.android.CommunicationListener;
import com.itron.protol.android.TransactionDateTime;
import com.itron.protol.android.TransactionInfo;
import com.itron.protol.android.TransationCurrencyCode;
import com.itron.protol.android.TransationTime;
import com.itron.protol.android.TransationType;

public class BLEF2FCmdTest {
	static Logger logger = Logger.getInstance(BLEF2FCmdTest.class);
	private BLECommandController itcommm;
	private  String  str;
	private int iii = 0;
	boolean instop=false;
//	byte[] check_key=new byte[]{(byte)0xD5,0x2A,0x09,0x2C,(byte)0xF0,0x12,(byte)0xDD,0x0A};
//	byte[] random=null;
	
	byte[] check_key=null;
//	byte[] random=null;//"123".getBytes();
	byte[] random="123".getBytes();
	byte[] cash="23347".getBytes();
	byte[] data={(byte)0x11,(byte)0x12,(byte)0x13,(byte)0x14,(byte)0x15,(byte)0xab,(byte)0xcd,(byte)0x11,(byte)0x12,(byte)0x13,(byte)0x14,(byte)0x15,(byte)0xab,(byte)0xcd,(byte)0x11,(byte)0x12,(byte)0x13,(byte)0x14,(byte)0x15,(byte)0xab,(byte)0xcd,(byte)0x11,(byte)0x12,(byte)0x13,(byte)0x14,(byte)0x15,(byte)0xab,(byte)0xcd,(byte)0x43,(byte)0x31,(byte)0x33,(byte)0x33,(byte)0x35,(byte)0x41,(byte)0x37,(byte)0x39};
	byte[] PAN={(byte)0x12, (byte)0x34, (byte)0x56, (byte)0x78, (byte)0x90, (byte)0x19};
	byte[] pinkey={(byte)0xaa, (byte)0xba, (byte)0xc7, (byte)0x80, (byte)0x2d, (byte)0xF4,(byte)0x9f,(byte)0x68,(byte)0x44,(byte)0xb4,(byte)0xd4,(byte)0x39};
	byte[] mackey={(byte)0x22, (byte)0x34, (byte)0x56, (byte)0x78, (byte)0x90, (byte)0x19};
	byte[] deskey={(byte)0x32, (byte)0x34, (byte)0x56, (byte)0x78, (byte)0x90, (byte)0x19};
	byte[] vendor="123456789012345".getBytes();
	byte[] terid="32145678".getBytes();
	String[] Pdata={
	         "11      商户名称：***\n商户存根            请妥善保管\n", //第一份的标题
	         "12    商户名称：***\n持卡人存根             请妥善保管\n", //第二份的标题
	         "00商户编号（MERCHAANT NO） 123456",
	         "00终端编号（TERMINAL NO） 123456",
	         "00发卡行：中国银行",
	         "00卡号（CARD NO）622209******6620",
	         "00交易类型（TRANS TYPE）",
	         "00    消费（SALE）",
	         "00交易时间（DATE/TIME）",
	         "00    2012/12/02   19:05:26",
	         "00参考号（REFERENCE NO）P000000000324",
	         "00交易金额（RMB）￥ 200.00",
	         "00交易金额：1000.00元",
	         "21持卡人签名\n\n\n\n\n\n\n本人确认以上交易",
	         "21持卡人签名\n\n\n\n\n\n\n本人确认以上交易，同意将其计入本卡账户\nI ACKNOWLEDGE SATISFACTORY RECEIPT OF RELATIVE GOODS/SERVICES",              //第一份的落款
	         "22",//第二份的落款

			};
	
	
	public BLEF2FCmdTest(Context ctx, CommunicationListener s){
		
		itcommm = BLECommandController.GetInstance(ctx,s);
	}
	
	
	

	
	/**
	 * 打开设备 如果是打开音频传任意值 如果是打开蓝牙 str 传蓝牙MAC地址
	 * @param str 
	 * @return
	 */
	public int openDevice(String str){
		//false为使用安全连接方式
		//itcommm.setConectStyle(true);
		return itcommm.openDevice(str);
	}
	
	

	
	public void setlistener(CommunicationListener s){
		itcommm.setlistener(s);
	}
	
	public void closeDevice(){
		itcommm.closeDevice();
	}
	/**
	 * 搜索蓝牙设备
	 * @param listener
	 */
	public void searchDevices(DeviceSearchListener listener){
		itcommm.searchDevices(listener);
	}
	
	public void stopSearchDevices(){
		itcommm.stopSearchDevices();
	}
	
	/**
	 * 获取磁道
	 * @return
	 */
	public boolean Get_CardTrack()
	{
		CommandReturn cmdret= new CommandReturn();
		cmdret=itcommm.Get_CardTrack(30);
		if(cmdret==null){
			return false;
		}
		String cardNo="";
		if(cmdret.Return_Track2!=null)
		{
			cardNo=Util.BinToHex(cmdret.Return_Track2, 0, cmdret.Return_Track2.length)+"FF";
		}
		if(cmdret.Return_Track3!=null)
		{
			cardNo+=Util.BinToHex(cmdret.Return_Track3, 0, cmdret.Return_Track3.length);
			cardNo+="FF";
		}
		if(cardNo.equals(""))
		{
			return false;
		}
//		itcommm.getlistener().onGetCardNo(cardNo);
		Log.d("itron", "PAN="+Util.BinToHex(cmdret.Return_PAN, 0, 8));
		return true;
	}
	
	/**
	 * 获取磁道密文
	 * @return
	 */
	public boolean Get_EncCardTrack()
	{
		CommandReturn cmdret= new CommandReturn();
		cmdret=itcommm.Get_EncTrack(0, 1,random , 50);
		if((cmdret!=null)&&(cmdret.Return_PSAMTrack!=null)){
			
			itcommm.getlistener().onShowMessage("磁道密文:\n"+Util.BinToHex(cmdret.Return_PSAMTrack,0, cmdret.Return_PSAMTrack.length));
			logger.error("cmdtest getksn="+Util.BinToHex(cmdret.Return_PSAMTrack,0, cmdret.Return_PSAMTrack.length));
			//return Util.BcdToString(cmdret.Return_PSAMNo);
			logger.setDebug(true);
			return true;
			
		}
		else
		{
			itcommm.getlistener().onShowMessage(null);
			//return Util.BcdToString(cmdret.Return_PSAMNo);
			logger.error("cmdtest getksn=null");
			return false;
			
		}
	}
	public void getDataEnc(byte[] data){
		CommandReturn cmdret= new CommandReturn();
		cmdret = itcommm.Get_DataEnc(0, 0, random, data);
		if((cmdret!=null)&&(cmdret.Return_Result==0x00)){
			logger.debug("Return_DataEnc:"+Util.BinToHex(cmdret.Return_DataEnc,0,cmdret.Return_DataEnc.length));
		}
	}
	
	/**
	 * 刷卡输入密码连续操作
	 * @return
	 */
	public boolean getcardpsw(String money){
		CommandReturn cmdret= new CommandReturn();
		if(money.equals("")||money==null)
			cmdret = itcommm.Get_ExtCtrlConOperator(1, 1, 1, 1, (byte)0x4f, random, null, null,	30);
		else
			cmdret = itcommm.Get_ExtCtrlConOperator(1, 1, 1, 1, (byte)0x4f, random, money.getBytes(), null,	30);
//		cmdret = cmdctrl.Get_ExtCtrlConOperator(4, 1, 1, 1, (byte)0x4e, random, null, null,	30);
		
		
		if((cmdret!=null)&&(cmdret.Return_Result==(byte)0x0a))
		{
			itcommm.getlistener().onShowMessage("用户取消了本次操作");
			return true;
		}
		if((cmdret!=null)&&(cmdret.Return_PSAMTrack!=null)){
			StringBuffer buf = new StringBuffer();
			if(cmdret.Return_PSAMMAC!=null)
				buf.append("PSMMAC:"+Util.BinToHex(cmdret.Return_PSAMMAC,0,cmdret.Return_PSAMMAC.length)+"\n");
			if(cmdret.Return_PSAMNo!=null)
				buf.append("PSAMNo:"+Util.BinToHex(cmdret.Return_PSAMNo,0,cmdret.Return_PSAMNo.length)+"\n");
			if(cmdret.Return_PSAMPIN!=null)
				buf.append("PSAMPIN:"+Util.BinToHex(cmdret.Return_PSAMPIN,0,cmdret.Return_PSAMPIN.length)+"\n");
			if(cmdret.Return_PSAMTrack!=null)
				buf.append("PSAMTrack:"+Util.BinToHex(cmdret.Return_PSAMTrack,0, cmdret.Return_PSAMTrack.length)+"\n");
			if(cmdret.Return_TerSerialNo !=null)
				buf.append("TerSerialNo:"+Util.BinToHex(cmdret.Return_TerSerialNo,0, cmdret.Return_TerSerialNo.length)+"\n");
			if(cmdret.Return_RecvData!=null)
				buf.append("Return_RecvData:"+Util.BinToHex(cmdret.Return_RecvData,0, cmdret.Return_RecvData.length)+"\n");
//			if(cmdret.Return_ResultData !=null)
//				buf.append("Return_ResultData:"+Util.BinToHex(cmdret.Return_ResultData,0, cmdret.Return_ResultData.length)+"\n");
			itcommm.getlistener().onShowMessage(buf.toString());
			logger.error(buf.toString());
			return true;
			
		}
		else
		{
			itcommm.getlistener().onShowMessage("Return_Result:"+Util.toHex(cmdret.Return_Result));
			//return Util.BcdToString(cmdret.Return_PSAMNo);
			logger.error("cmdtest getksn=null");
			return false;
			
		}
	}
	
	
	/**
	 * 发送退出指令
	 */
	public void stopCSwiper(){
		if(!instop)
		{
			instop=true;
			CommandReturn cmdret = itcommm.Get_CommExit();
			//return cmdret;
			instop=false;
			logger.error("  INSTOP IS "+instop);
		}
	}
	
	public void release(){
		itcommm.release();
	}
	
	public void reset(){
		itcommm.comm_reset();
	}
	/**
	 * 更新工作密钥
	 */
	public void RenewKey(){
		//<workkey>6D894CB8B15D89111F017515A7EC6AC39E689758FE635B90ACAEFDB16FB13B818330D3EB2C2507F3D72DC0CB9F75835F</workkey>
		//工作秘钥明文假定：02020202020202020202020202020202
		//工作秘钥密文：a317a150dfed260ba317a150dfed260b
		//校验位密文：c4af51bd
		//翔哥所给校验位：4eb13bf6
//		String pinstr = "a317a150dfed260ba317a150dfed260bc4af51bd";
//		//String pinstr = "920ACFACB1314ECE266712D915F92683486E46F2";
//        String macstr = "a317a150dfed260ba317a150dfed260bc4af51bd";
//        String desstr = "a317a150dfed260ba317a150dfed260bc4af51bd";
//		String str = "6D894CB8B15D89111F017515A7EC6AC39E689758FE635B90ACAEFDB16FB13B818330D3EB2C2507F3D72DC0CB9F75835F";
//        String pinstr = str.substring(0,40);
//        String macstr = str.substring(48, 88);//"ACAEFDB16FB13B818330D3EB2C2507F3D72DC0CB";
		String pinstr = "bc607fa9bc9756f7bc607fa9bc9756f7c4af51bd";
		
		byte[] PINkey = Util.HexToBin(pinstr);
		byte[] MACkey = Util.HexToBin(pinstr);
		byte[] DESkey = Util.HexToBin(pinstr);
		CommandReturn cmdret = new CommandReturn();
		cmdret = itcommm.Get_RenewKey(PINkey, MACkey, DESkey);
		if((cmdret!=null)&&((cmdret.Return_Result==0X00))){
			itcommm.getlistener().onShowMessage("密钥成功");
		}else{
			itcommm.getlistener().onShowMessage("密钥失败" + Util.toHex(cmdret.Return_Result));
		}
	}
	
	/**
	 * 更新待机界面
	 */
	public void updateHomeScreen(String str){
		CommandReturn cmdret = new CommandReturn();
		cmdret = itcommm.updateHomeScreen(str,"01");//欢迎使用U然派
		if((cmdret!=null)&&((cmdret.Return_Result==0X00))){
			itcommm.getlistener().onShowMessage("成功");
		}else{
			itcommm.getlistener().onShowMessage("失败");
		}
	}

    public void getTerminalTypeI21(){
         CommandReturn cmd = itcommm.getTerminalTypeReI21();
       // getTerminalTypeReI21();
        StringBuilder sb = new StringBuilder();

        if(cmd != null){
            sb.append("设备类型: "+cmd.deviceType+"\n");
            if(cmd.ksn != null){
                sb.append("ksn: "+ Util.BinToHex(cmd.ksn,0,cmd.ksn.length)+"\n");
            }
            if(cmd.Return_TerSerialNo != null){
                sb.append("终端版本号: "+ new String(cmd.Return_TerSerialNo)+"\n");
            }
            if(cmd.btName != null){
                sb.append("蓝牙名称: "+ new String(cmd.btName)+"\n");
            }
            if(cmd.btVersion != null){
                sb.append("蓝牙版本号: "+ new String(cmd.btVersion)+"\n");
            }
            if(cmd.btMac != null){
                sb.append("蓝牙MAC地址: "+ new String(cmd.btMac)+"\n");
            }
        }





        itcommm.getlistener().onShowMessage(sb.toString());
    }

	public void getTerminalType(){
		int i = itcommm.getTerminalType();
		itcommm.getlistener().onShowMessage("设备类型:" + i);
	}
	
	public void getTerminalTypeNew(){
		CommandReturn returnData = itcommm.getTerminalTypeReNew();
		StringBuilder sb = new StringBuilder();
		if(returnData != null){
			if(returnData.Return_Result == 0x00){
				sb.append("应答成功： " + returnData.Return_Result+"\n");
			}else{
				sb.append("应答失败： " + returnData.Return_Result+"\n");
			}
			sb.append("设备类型： " + returnData.deviceType+"\n");

			if(returnData.terminalNum != null){
				sb.append("终端号: "+Util.BinToHex(returnData.terminalNum, 0, returnData.terminalNum.length)+"\n");
			}
			if(returnData.versionNum != null){
				sb.append("版本号: "+new String(returnData.versionNum)+"\n");
			}
			if(returnData.btName != null){
				sb.append("蓝牙名称: "+new String(returnData.btName)+"\n");
			}
			if(returnData.btVersion != null){
				sb.append("蓝牙名版本: "+new String(returnData.btVersion)+"\n");
			}
			if(returnData.btMac != null){
				sb.append("mac地址: "+new String(returnData.btMac)+"\n");
			}
			if(returnData.versionDate != null){
				sb.append("版本日期: "+new String(returnData.versionDate)+"\n");
			}
			if(returnData.Return_PSAMNo != null){
				sb.append("psam卡卡号: "+ Util.BinToHex(returnData.Return_PSAMNo,0,returnData.Return_PSAMNo.length)+"\n");
			}
		}
		itcommm.getlistener().onShowMessage(sb.toString());
	}
	
	public void updateTerminalParameters(int flag, int packageNo,
			byte[] data){
		CommandReturn cmdret = new CommandReturn();
		cmdret = itcommm.updateTerminalParameters(flag, packageNo, data);
		if((cmdret!=null)&&((cmdret.Return_Result==0x00))){
			
			itcommm.getlistener().onShowMessage("第"+(packageNo+1)+"数据下载成功");
		}else{
			itcommm.getlistener().onShowMessage("失败 ,应答码:" + Util.toHex(cmdret.Return_Result));
		}
		
	}

	
	public void publickeyRepair(){
		CommandReturn cmdret = new CommandReturn();
		int i = itcommm.publickeyRepair();
		if(i == -1){
			itcommm.getlistener().onShowMessage("公钥修复失败");
		}else if(i == 0){
			itcommm.getlistener().onShowMessage("公钥修复成功");
		}else if(i ==1){
			itcommm.getlistener().onShowMessage("无需公钥修复");
		}
		
	}
    public void statEmvSwiper() {
        CommandReturn cmdret = new CommandReturn();

        TransactionInfo info = new TransactionInfo();
        // 设置交易日期 格式: YYMMDD
        TransactionDateTime dateTime = new TransactionDateTime();
        dateTime.setDateTime(Util.getCurrentDateYY());
        // 设置交易时间   hhssmm
        TransationTime time = new TransationTime();
        time.setTime(Util.getCurrentTime());
        // 设置货币代码
        TransationCurrencyCode currencyCode = new TransationCurrencyCode();
        currencyCode.setCode("0156");
        // 设置交易类型  00 消费  31余额查询
        TransationType type = new TransationType();
        type.setType("00");

        info.setDateTime(dateTime);
        info.setCurrencyCode(currencyCode);
        info.setTime(time);
        info.setType(type);

//		bit0＝0 保留
//	       Bit1＝0/1 表示终端需要不上送/上送mac。
//	       Bit2＝0  保留
//		   Bit3＝0  保留
//		   Bit4=0/1 磁道数据加密/不加密
//		   Bit5=0/1上送/不上送PAN码
//		   Bit6=0/1  55域不加密/加密
//	       Bit7 =0  保留
//	   字节2:
//	   Bit0 = 0/1 表示数据域+ 55域/只有数据域参与
//	   Bit1 = 0/1 全部ic卡域/标准55域
//	       Bit2 =0/1 密钥索引不启用/启用
//		   Bit3=0/1 刷卡的卡号密文/明文 不上送/上送
//		   Bit4=0/1 不上送/上送psam卡卡号
//	       Bit5=0/1 不上送/上送终端ID
//	       Bit6=0/1  手输入卡号加密/明文
//		   Bit7 =0/1  支持/不支持IC卡降级
//		字节3：
//	       bit0 = 0/1 刷卡的卡号不加密/加密
//	       bit1 = 0/1卡号加密时不压缩/压缩（当字节3的bit0=1时且字节2的bit3=1，或者当字节2的bit6=1时，该标志有作用）
//	       bit2 =0/1  不上送/上送卡有效期

//		String byte0 = "00100010";//"0010000";//"00000010";//"10101111";//"10100000";//"10100010";//"10001110"; 1
//		String byte1 = "00111011";//"0110011";//"01111001";
        //油付通
//		String byte0 = "01100000";
//		String byte1 = "01011011";
        //瑞银信
        String byte0 = "00000000";
        String byte1 = "01001110";
        int flag0 = Util.binaryStr2Byte(byte0);
        int flag1 = Util.binaryStr2Byte(byte1);
        cmdret = itcommm.statEmvSwiper(1, 1, 1, 1,new byte[]{(byte)flag0,(byte) flag1,0x04,0x00}, random, "100", null, 50, info);
        if((cmdret!=null)&&((cmdret.Return_Result==0X00))){
            StringBuffer buf = new StringBuffer();
            if(cmdret.Return_CardNo!=null)
                buf.append("Return_CardNo:"+Util.BinToHex(cmdret.Return_CardNo,0,cmdret.Return_CardNo.length)+"\n");

            if(cmdret.Return_PSAMMAC!=null)
                buf.append("PSMMAC:"+Util.BinToHex(cmdret.Return_PSAMMAC,0,cmdret.Return_PSAMMAC.length)+"\n");
            if(cmdret.Return_PSAMNo!=null)
                buf.append("PSAMNo:"+Util.BinToHex(cmdret.Return_PSAMNo,0,cmdret.Return_PSAMNo.length)+"\n");
            if(cmdret.Return_PSAMPIN!=null)
                buf.append("PSAMPIN:"+Util.BinToHex(cmdret.Return_PSAMPIN,0,cmdret.Return_PSAMPIN.length)+"\n");
            if(cmdret.Return_PSAMTrack!=null)
                buf.append("PSAMTrack:"+Util.BinToHex(cmdret.Return_PSAMTrack,0, cmdret.Return_PSAMTrack.length)+"\n");
            if(cmdret.Return_PAN != null)
                buf.append("Return_PAN:"+Util.BinToHex(cmdret.Return_PAN,0, cmdret.Return_PAN.length)+"\n");
            if(cmdret.Return_TerSerialNo !=null)
                buf.append("TerSerialNo:"+Util.BinToHex(cmdret.Return_TerSerialNo,0, cmdret.Return_TerSerialNo.length)+"\n");
            buf.append("CardType:"+cmdret.CardType+"\n");
            if(cmdret.CardSerial !=null)
                buf.append("CardSerial:"+Util.BinToHex(cmdret.CardSerial,0,+cmdret.CardSerial.length)+"\n");
            if(cmdret.CVM !=null)
                buf.append("CVM:"+Util.BinToHex(cmdret.CVM,0,3)+"\n");
            if(cmdret.emvDataInfo !=null)
                buf.append("emvDataInfo:"+Util.BinToHex(cmdret.emvDataInfo,0, cmdret.emvDataInfo.length)+"\n");

            itcommm.getlistener().onShowMessage(buf.toString());
            logger.error(buf.toString());
        }else{
            itcommm.getlistener().onShowMessage("失败,应答码:" + Util.toHex(cmdret.Return_Result));
        }
    }

    public void statEmvSwiperI21() {
		CommandReturn cmdret = new CommandReturn();

        TransactionInfo info = new TransactionInfo();
        // 设置交易日期 格式: YYMMDD
        TransactionDateTime dateTime = new TransactionDateTime();
        dateTime.setDateTime("140930");
        // 设置交易时间   hhssmm
        TransationTime time = new TransationTime();
        time.setTime("105130");
        // 设置货币代码
        TransationCurrencyCode currencyCode = new TransationCurrencyCode();

        currencyCode.setCode("0156");
        // 设置交易流水号
//			TransationNum num = new TransationNum();
//			num.setNum("00000001");
        // 设置交易类型  00 消费
        TransationType type = new TransationType();
        type.setType("00");

        info.setDateTime(dateTime);
        info.setCurrencyCode(currencyCode);
//			info.setNum(num);
        info.setTime(time);
        info.setType(type);
//			bit0＝0/1 表示随机有爱刷产生/由双方产生。
//		    Bit1＝0/1 表示终端需要不上送/上送mac。
//	    	Bit2＝0/1 表示终端上送的卡号不屏蔽/屏蔽
//		    Bit3＝0/1  不上送/上送各磁道的原始长度

//		    Bit4=0/1磁道数据加密/不加密
//		    Bit5=0/1上送/不上送PAN码
//		    Bit6=0/1 55域不加密/加密
//	    	Bit7 =0/1 不上送/上送各磁道密文的长度

        //bit7----bit0
        String byte0 = "10000011";//"10000011";//钱拓 83010000 ;//"10101111";//"10100000";//"10100010";//"10001110"; 1
        int flag0 = Util.binaryStr2Byte(byte0);
        cmdret = itcommm.statEmvSwiperI21((byte)0,
                new byte[]{(byte)flag0,0x03,0x00,0x00},
                "123".getBytes(), "100",
                null, 30, info);
        if((cmdret!=null)&&((cmdret.Return_Result==0X00))){
			StringBuffer buf = new StringBuffer();
			if(cmdret.Return_CardNo!=null)
				buf.append("Return_CardNo:"+new String(cmdret.Return_CardNo,0,cmdret.Return_CardNo.length)+"\n");
			
			if(cmdret.Return_PSAMMAC!=null)
				buf.append("PSMMAC:"+Util.BinToHex(cmdret.Return_PSAMMAC,0,cmdret.Return_PSAMMAC.length)+"\n");
			if(cmdret.Return_PSAMNo!=null)
				buf.append("ksn:"+Util.BinToHex(cmdret.Return_PSAMNo,0,cmdret.Return_PSAMNo.length)+"\n");
            if(cmdret.trackLengths !=null && cmdret.trackLengths.length == 3){
                buf.append("track1Length1:"+cmdret.trackLengths[0]+"\n");
                buf.append("track1Length2:"+cmdret.trackLengths[1]+"\n");
                buf.append("track1Length3:"+cmdret.trackLengths[2]+"\n");
            }


			if(cmdret.Return_PSAMTrack!=null)
				buf.append("PSAMTrack:"+Util.BinToHex(cmdret.Return_PSAMTrack,0, cmdret.Return_PSAMTrack.length)+"\n");

			if(cmdret.Return_TerSerialNo !=null)
				buf.append("TerSerialNo:"+Util.BinToHex(cmdret.Return_TerSerialNo,0, cmdret.Return_TerSerialNo.length)+"\n");
			buf.append("CardType:"+cmdret.CardType+"\n");
			if(cmdret.CardSerial !=null)
				buf.append("CardSerial:"+Util.BinToHex(cmdret.CardSerial,0,+cmdret.CardSerial.length)+"\n");
			if(cmdret.cardexpiryDate !=null)
				buf.append("cardexpiryDate:"+Util.BinToHex(cmdret.cardexpiryDate,0,cmdret.cardexpiryDate.length)+"\n");
			if(cmdret.emvDataInfo !=null)
				buf.append("emvDataInfo:"+Util.BinToHex(cmdret.emvDataInfo,0, cmdret.emvDataInfo.length)+"\n");
			
			itcommm.getlistener().onShowMessage(buf.toString());
			logger.error(buf.toString());
		}else{
			itcommm.getlistener().onShowMessage("失败,应答码:" + Util.toHex(cmdret.Return_Result));
		}
	}
	
	public void secondIssuance(){
		CommandReturn cmdret = new CommandReturn();
		cmdret = itcommm.secondIssuanceRe("00",null);
		if((cmdret!=null)&&((cmdret.Return_Result==0X00))){
			StringBuffer buf = new StringBuffer();
			buf.append("resultIC:"+ cmdret.resultIC+"\n");
			if(cmdret.resuiltScriptIC!=null)
				buf.append("resuiltScriptIC:"+Util.BinToHex(cmdret.resuiltScriptIC,0,cmdret.resuiltScriptIC.length)+"\n");
			if(cmdret.resuiltDataIC!=null)
				buf.append("resuiltDataIC:"+Util.BinToHex(cmdret.resuiltDataIC,0,cmdret.resuiltDataIC.length)+"\n");
				
			itcommm.getlistener().onShowMessage(buf.toString());
			logger.error(buf.toString());
		}else{
			itcommm.getlistener().onShowMessage("失败,应答码:" + Util.toHex(cmdret.Return_Result));
		}
	}
	
	public void renewVendorTerID(){
		
		CommandReturn cmdret = new CommandReturn();
		cmdret = itcommm.Get_RenewVendorTerID("885110159930210".getBytes(), "88500540".getBytes());
		if((cmdret!=null)&&((cmdret.Return_Result==0X00))){
			itcommm.getlistener().onShowMessage("更新成功");
		}else{
			itcommm.getlistener().onShowMessage("失败,应答码:" + Util.toHex(cmdret.Return_Result));
		}
		
	}
	
	public void getVendorTerID(){
		CommandReturn cmdret = new CommandReturn();
		cmdret = itcommm.Get_VendorTerID();
		if((cmdret!=null)&&((cmdret.Return_Result==0X00))){
			itcommm.getlistener().onShowMessage("成功");
			logger.debug("return vendor="
					+ Util.BinToHex(cmdret.Return_Vendor, 0,
							cmdret.Return_Vendor.length));
			logger.debug("ter id="
					+ Util.BinToHex(cmdret.Return_TerID, 0,
							cmdret.Return_TerID.length));
		}else{
			itcommm.getlistener().onShowMessage("失败");
		}
	}
	
	
	public boolean get_Pin(){
		CommandReturn cmdret= new CommandReturn();
		cmdret = itcommm.Get_PIN(0, 1, cash, random,null, 30);//(1, 1, 1, 1, (byte)0x1c, random, cash, null,	30);
		if((cmdret!=null)){
			if(cmdret.Return_Result == 0x00){
			StringBuffer buf = new StringBuffer();
			if(cmdret.Return_PSAMMAC!=null)
				buf.append("PSMMAC:"+Util.BinToHex(cmdret.Return_PSAMMAC,0,cmdret.Return_PSAMMAC.length)+"\n");
			if(cmdret.Return_PSAMNo!=null)
				buf.append("PSAMNo:"+Util.BinToHex(cmdret.Return_PSAMNo,0,cmdret.Return_PSAMNo.length)+"\n");
			if(cmdret.Return_PSAMPIN!=null)
				buf.append("PSAMPIN:"+Util.BinToHex(cmdret.Return_PSAMPIN,0,cmdret.Return_PSAMPIN.length)+"\n");
			if(cmdret.Return_PSAMTrack!=null)
				buf.append("PSAMTrack:"+Util.BinToHex(cmdret.Return_PSAMTrack,0, cmdret.Return_PSAMTrack.length)+"\n");
			if(cmdret.Return_TerSerialNo !=null)
				buf.append("TerSerialNo:"+Util.BinToHex(cmdret.Return_TerSerialNo,0, cmdret.Return_TerSerialNo.length)+"\n");
			
				itcommm.getlistener().onShowMessage(buf.toString());
				logger.error(buf.toString());
			}else{
				
				itcommm.getlistener().onShowMessage("Return_Result:" +Util.toHex(cmdret.Return_Result));
			}
				
				
			return true;
		}else
			return false;
	}
	
	/**
	 * 获取ksn或者psam号
	 */
	public void upadateHomeScreen(){
		logger.debug("upadateHomeScreen()");
		CommandReturn cmdret = new CommandReturn();
		cmdret = itcommm.updateHomeScreen(str,"01");//欢迎使用U然派
		if((cmdret!=null)&&((cmdret.Return_Result==0X00))){
			itcommm.getlistener().onShowMessage("成功");
		}else{
			itcommm.getlistener().onShowMessage("失败");
		}
	}
	
	/**
	 * 获取ksn或者psam号
	 */
	public void getKSN(){
		logger.debug("getKSN()");
		CommandReturn cmret = itcommm.Get_ExtPsamNo();
		if(cmret!=null){
			if(cmret.Return_Result ==0x00){
				StringBuffer buf = new StringBuffer();
				buf.append("Return_Result:" + cmret.Return_Result);
				if((cmret.Return_PSAMNo!=null)&&(cmret.Return_PSAMNo.length>0))
					buf.append("\nPSAM卡号:"+ Util.BinToHex(cmret.Return_PSAMNo, 0, cmret.Return_PSAMNo.length));
				if((cmret.Return_TerSerialNo!=null)&&(cmret.Return_TerSerialNo.length>0))
					buf.append("\n终端ID:" + Util.BinToHex(cmret.Return_TerSerialNo, 0, cmret.Return_TerSerialNo.length));
				if((cmret.Return_TerVersion!=null)&&(cmret.Return_TerVersion.length>0))
					buf.append("\n版本信息:" + Util.BytesToString(cmret.Return_TerVersion,0, cmret.Return_TerVersion.length));
//				updateUI.sendMessage(updateUI.obtainMessage(1, buf.toString()));
				
				itcommm.getlistener().onShowMessage(buf.toString());
			}else{
				StringBuffer buf = new StringBuffer();
				buf.append("Return_Result:" + cmret.Return_Result);
				if(cmret.Return_RecvData!=null)
					buf.append("\n错误码:"+ Util.BinToHex(cmret.Return_RecvData, 0, cmret.Return_RecvData.length));
//				updateUI.sendMessage(updateUI.obtainMessage(1, buf.toString()));
				itcommm.getlistener().onShowMessage(buf.toString());
			}
		}else{
//			updateUI.sendMessage(updateUI.obtainMessage(1, "通信失败"));
			itcommm.getlistener().onShowMessage("通信失败");
		}
		
	}
    /**
     * 获取ksn或者psam号
     */
    public void getKsnI21(){
        logger.debug("getKsnI21()");
        CommandReturn cmret = itcommm.getKsnI21();
        if(cmret!=null){
            if(cmret.Return_Result ==0x00){
                StringBuffer buf = new StringBuffer();
                buf.append("Return_Result:" + cmret.Return_Result);
                if((cmret.ksn!=null)&&(cmret.ksn.length>0))
                    buf.append("\nksn:"+ Util.BinToHex(cmret.ksn, 0, cmret.ksn.length));
                if((cmret.Return_TerSerialNo!=null)&&(cmret.Return_TerSerialNo.length>0))
                    buf.append("\n终端ID:" + Util.BinToHex(cmret.Return_TerSerialNo, 0, cmret.Return_TerSerialNo.length));
                if((cmret.Return_TerVersion!=null)&&(cmret.Return_TerVersion.length>0))
                    buf.append("\n版本信息:" + new String(cmret.Return_TerVersion,0, cmret.Return_TerVersion.length));
//				updateUI.sendMessage(updateUI.obtainMessage(1, buf.toString()));

                itcommm.getlistener().onShowMessage(buf.toString());
            }else{
                StringBuffer buf = new StringBuffer();
                buf.append("Return_Result:" + cmret.Return_Result);
                if(cmret.Return_RecvData!=null)
                    buf.append("\n错误码:"+ Util.BinToHex(cmret.Return_RecvData, 0, cmret.Return_RecvData.length));
//				updateUI.sendMessage(updateUI.obtainMessage(1, buf.toString()));
                itcommm.getlistener().onShowMessage(buf.toString());
            }
        }else{
//			updateUI.sendMessage(updateUI.obtainMessage(1, "通信失败"));
            itcommm.getlistener().onShowMessage("通信失败");
        }

    }
	public void get_CardNo(int timeout){
		
		CommandReturn cmdret=itcommm.Get_CardNo(30);
		if(cmdret!=null){
			if(cmdret.Return_Result==0x00){
				itcommm.getlistener().onShowMessage("Return_CardNo:"+Util.BinToHex(cmdret.Return_CardNo,0,cmdret.Return_CardNo.length));
				logger.error("Return_CardNo="+Util.BinToHex(cmdret.Return_CardNo,0,cmdret.Return_CardNo.length));
			}else{
				itcommm.getlistener().onShowMessage("Return_Result:"+Util.toHex(cmdret.Return_Result));
			}
		}else{
			itcommm.getlistener().onShowMessage("通信失败");
			
		}
	}
	public void get_PsamRandom(){
		
		CommandReturn cmdret=itcommm.Get_PsamRandom(8);
		if(cmdret!=null){
			if(cmdret.Return_Result==0x00){
				itcommm.getlistener().onShowMessage("Return_PSAMRandom:"+Util.BinToHex(cmdret.Return_PSAMRandom,0,cmdret.Return_PSAMRandom.length));
				logger.error("Return_PSAMRandom="+Util.BinToHex(cmdret.Return_PSAMRandom,0,cmdret.Return_PSAMRandom.length));
			}else{
				itcommm.getlistener().onShowMessage("Return_Result:"+cmdret.Return_Result);
			}
		}else{
			itcommm.getlistener().onShowMessage("通信失败");
			
		}
	}
	
	/**
	 * IC卡透传命令
	 * @param index 命令个数
	 * @param data 命令数据
	 */
	public void get_ThroughOrders(int index ,byte[] data){
		CommandReturn cmdret = new CommandReturn();
		cmdret = itcommm.Get_ThroughOrders(1, index, data, 30);
		if(cmdret!=null){
			if(cmdret.Return_Result==0x00){
				logger.debug("Return_ThroughDate:"+Util.BinToHex(cmdret.Return_ThroughDate,0,cmdret.Return_ThroughDate.length));
				itcommm.getlistener().onShowMessage(
					"Return_Order:"+cmdret.Return_Order + "\n"+
					"Return_ThroughDate:"+Util.BinToHex(cmdret.Return_ThroughDate,0,cmdret.Return_ThroughDate.length));
			}else if(cmdret.Return_Result==0x04){
				itcommm.getlistener().onShowMessage("错误码"+ Util.toHex(cmdret.Return_Result) +"\n SW:"+Util.BinToHex(cmdret.Return_RecvData,0,cmdret.Return_RecvData.length));
			}else{
				itcommm.getlistener().onShowMessage("错误码"+ Util.toHex(cmdret.Return_Result));
			}
		}
	}
	/**
	 * 计算MAC
	 */
	public void get_MAC(){
		logger.debug("getMAC()");
		CommandReturn cmdret= new CommandReturn();
//		byte[] data = {0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
		byte[] data = Util.HexToBin("1990050000000000100000011719241217313233343531323334353834");
		cmdret=itcommm.Get_MAC(0, 1, random, data);
		if((cmdret!=null)&&(cmdret.Return_PSAMMAC!=null)){
			itcommm.getlistener().onShowMessage("MAC:"+Util.BcdToString(cmdret.Return_PSAMMAC));
			logger.error("cmdtest getMAC="+Util.BcdToString(cmdret.Return_PSAMMAC));
			//return Util.BcdToString(cmdret.Return_PSAMNo);
		}else{
			itcommm.getlistener().onShowMessage(null);
			//return Util.BcdToString(cmdret.Return_PSAMNo);
			logger.error("cmdtest getMAC=null");
			
		}
	}
	
	/**
	 * 校验MAC
	 */
	public void checkMAC(){
		logger.debug("CheckMAC()");
		CommandReturn cmdret= new CommandReturn();
		byte[] data = {0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x6C,0x6B,(byte)0x90,0x5D,0x30,0x30,0x30,0x30};
		cmdret=itcommm.Get_CheckMAC(0, 1, random, data);
		if((cmdret!=null)&&(cmdret.Return_Result==0x00)){
			
			itcommm.getlistener().onShowMessage("MAC校驗成功");
			logger.error("cmdtest MAC校驗成功");
			//return Util.BcdToString(cmdret.Return_PSAMNo);
			
		}
		else
		{
			itcommm.getlistener().onShowMessage("MAC校驗失敗");
			//return Util.BcdToString(cmdret.Return_PSAMNo);
			logger.error("cmdtest MAC校驗失敗");
			
		}
	}
	
	/**
	 * 打印
	 */
	public void getPrint(){
		logger.debug("print test");
		String str[] = new String[] {
				"11      商户名称：盛付通\n商户存根            请妥善保管\n", // 第一份的标题
				"12    商户名称：盛付通切客\n持卡人存根             请妥善保管\n", // 第二份的标题
				"00商户编号（MERCHAANT NO） 123456",
				"00终端编号（TERMINAL NO） 123456",
				"00发卡行：中国银行",
				"00卡号（CARD NO）622209******6620",
				"00交易类型（TRANS TYPE）",
				"00    消费（SALE）",
				"00交易时间（DATE/TIME）",
				"00    2012/12/02   19:05:26",
				"00参考号（REFERENCE NO）P000000000324",
				"00交易金额（RMB）￥ 200.00",
				"00交易金额：1000.00元",
				"00备注（REFERENCE）：",
				"21持卡人签名\n\n\n\n\n\n\n本人确认以上交易，同意将其计入本卡账户\nI ACKNOWLEDGE SATISFACTORY RECEIPT OF RELATIVE GOODS/SERVICES", // 第一份的落款
				"22",// 第二份的落款
		};

		CommandReturn cmdret=itcommm.Set_PtrData(2,str,20);
		
		if(cmdret!=null){
			if(cmdret.Return_Result==0X00){
				itcommm.getlistener().onShowMessage("打印成功");
			}
			else if(cmdret.Return_Result==(byte)0X40){
				itcommm.getlistener().onShowMessage("打印机缺纸");
			}else{
				itcommm.getlistener().onShowMessage("打印失敗");
			}
		}
		else
		{
			itcommm.getlistener().onShowMessage("打印失败");
		}
	}
}
