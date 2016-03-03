package com.caihongcity.com.activity;

import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.caihongcity.com.R;
import com.caihongcity.com.utils.StorageAppInfoUtil;
import com.caihongcity.com.utils.ViewUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_select_terminal)
public class SelectTerminalActivity extends BaseActivity {

    @ViewById
    TextView tv_title_des;
    @ViewById
    RadioGroup rg_select_terminal_type;
    @ViewById
    RadioButton rb_dianfubao;
    @ViewById
    RadioButton rb_shuakatou;
    @ViewById
    RadioButton rb_huixing;
    @ViewById
    RadioButton rb_mofang;
    @ViewById
    RadioButton rb_zhongci;
    @ViewById
    RadioButton rb_mofangblue;
    @ViewById
    RadioButton rb_yifeng;
    @ViewById
    RadioButton rb_xinnuo;
    @ViewById
    RadioButton rb_m368;
    @ViewById
    RadioButton rb_bbpos;
    @ViewById
    RadioButton rb_xinlianda;


    String terminal_type;//终端类型 0代表 艾创刷卡头 1代表 艾创点付宝 2 汇兴刷卡头 3 魔方音频刷卡头 4 中磁刷卡头 5 魔方蓝牙刷卡头 6怡丰刷卡头

    @AfterViews
    void initData() {
        tv_title_des.setText("选择终端类型");
        terminal_type = StorageAppInfoUtil.getInfo("terminal_type", this);
        if ("0".equals(terminal_type)) {
            rb_shuakatou.setChecked(true);
        } else if ("1".equals(terminal_type)) {
            rb_dianfubao.setChecked(true);
        } else if ("2".equals(terminal_type)) {
            rb_huixing.setChecked(true);
        } else if ("3".equals(terminal_type)) {
            rb_mofang.setChecked(true);
        }else if ("4".equals(terminal_type)) {
            rb_zhongci.setChecked(true);
        }else if("5".equals(terminal_type)){
            rb_mofangblue.setChecked(true);
        }else if("6".equals(terminal_type)){
            rb_yifeng.setChecked(true);
        }else if("7".equals(terminal_type)){
            rb_xinnuo.setChecked(true);
        }else if("8".equals(terminal_type)){
            rb_m368.setChecked(true);
        }else if("9".equals(terminal_type)){
            rb_bbpos.setChecked(true);
        }else if("10".equals(terminal_type)){
            rb_xinlianda.setChecked(true);
        }
    }


    @Click({R.id.ll_back, R.id.confirm_save})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_back:
                ViewUtils.overridePendingTransitionBack(SelectTerminalActivity.this);
                break;
            case R.id.confirm_save:
                if (rg_select_terminal_type.getCheckedRadioButtonId() == R.id.rb_shuakatou) {
                    StorageAppInfoUtil.putInfo(SelectTerminalActivity.this, "terminal_type", 0 + "");
                } else if (rg_select_terminal_type.getCheckedRadioButtonId() == R.id.rb_dianfubao) {
                    StorageAppInfoUtil.putInfo(SelectTerminalActivity.this, "terminal_type", 1 + "");
                } else if (rg_select_terminal_type.getCheckedRadioButtonId() == R.id.rb_huixing) {
                    StorageAppInfoUtil.putInfo(SelectTerminalActivity.this, "terminal_type", 2 + "");
                } else if (rg_select_terminal_type.getCheckedRadioButtonId() == R.id.rb_mofang) {
                    StorageAppInfoUtil.putInfo(SelectTerminalActivity.this, "terminal_type", 3 + "");
                } else if (rg_select_terminal_type.getCheckedRadioButtonId() == R.id.rb_zhongci) {
                    StorageAppInfoUtil.putInfo(SelectTerminalActivity.this, "terminal_type", 4 + "");
                } else if (rg_select_terminal_type.getCheckedRadioButtonId() == R.id.rb_mofangblue){
                    StorageAppInfoUtil.putInfo(SelectTerminalActivity.this, "terminal_type", 5 + "");
                } else if(rg_select_terminal_type.getCheckedRadioButtonId() == R.id.rb_yifeng){
                    StorageAppInfoUtil.putInfo(SelectTerminalActivity.this, "terminal_type", 6 + "");
                }else if(rg_select_terminal_type.getCheckedRadioButtonId() == R.id.rb_xinnuo){
                    StorageAppInfoUtil.putInfo(SelectTerminalActivity.this, "terminal_type", 7 + "");
                }else if(rg_select_terminal_type.getCheckedRadioButtonId() == R.id.rb_m368){
                    StorageAppInfoUtil.putInfo(SelectTerminalActivity.this, "terminal_type", 8 + "");
                }else if(rg_select_terminal_type.getCheckedRadioButtonId() == R.id.rb_bbpos){
                    StorageAppInfoUtil.putInfo(SelectTerminalActivity.this, "terminal_type", 9 + "");
                }else if(rg_select_terminal_type.getCheckedRadioButtonId() == R.id.rb_xinlianda){
                    StorageAppInfoUtil.putInfo(SelectTerminalActivity.this, "terminal_type", 10 + "");
                }
                ViewUtils.overridePendingTransitionBack(SelectTerminalActivity.this);
                break;
        }
    }
}



