package com.caihongcity.com.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.caihongcity.com.R;
import com.caihongcity.com.model.QueryModel;
import com.caihongcity.com.utils.StorageAppInfoUtil;
import com.caihongcity.com.utils.ViewUtils;
import com.itron.protol.android.BLECommandController;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.Set;

@EActivity(R.layout.activity_bluetooth_select)
public class BluetoothSelectActivity extends BaseActivity {
    @ViewById
    TextView tv_right;
    @ViewById
    TextView tv_title_des;
    @ViewById
    ListView lv_bluetooth;
    BLECommandController itcommm;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    @Extra
    String tradetype;
    @Extra
    String feeRate;
    @Extra
    String topFeeRate;
    @Extra
    String money;
    @Extra
    QueryModel queryModel;
    private BluetoothAdapter mBluetoothAdapter;
    private Intent intent;

    @AfterViews
    void initData() {
        // 获取所有已经绑定的蓝牙设备
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        openBlue();
        tv_title_des.setText("蓝牙列表");
        tv_right.setText("刷新");
        tv_right.setVisibility(View.VISIBLE);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        lv_bluetooth.setAdapter(mNewDevicesArrayAdapter);
        lv_bluetooth.setOnItemClickListener(mDeviceClickListener);
        // 注册用以接收到已搜索到的蓝牙设备的receiver
        IntentFilter mFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, mFilter);
        // 注册搜索完时的receiver
        mFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, mFilter);
        searchDevices();
    }

    private void openBlue() {
        if(mBluetoothAdapter != null) {
            mBluetoothAdapter.enable();
        }
    }

    void searchDevices() {
        // 如果正在搜索，就先取消搜索
        loadingDialogCanCancel = ViewUtils.createLoadingDialog(this, getString(R.string.loading_wait), true);
        loadingDialogCanCancel.show();
        mNewDevicesArrayAdapter.clear();
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        if (devices.size() > 0) {
            for (BluetoothDevice bluetoothDevice : devices) {
                mNewDevicesArrayAdapter.add(bluetoothDevice.getName() + "\n" + bluetoothDevice.getAddress());
            }
        }
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        // 开始搜索蓝牙设备,搜索到的蓝牙设备通过广播返回
        mBluetoothAdapter.startDiscovery();
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            connectedBluetoothDevice(address);

        }
    };

    @Background
    void connectedBluetoothDevice(String address) {

        String terminal_type = StorageAppInfoUtil.getInfo("terminal_type", BluetoothSelectActivity.this);
        if(terminal_type.equals("1")) {
            intent = new Intent(this, SwipeWaitBluetoothActivity_.class);
        }else if(terminal_type.equals("5")) {
            intent = new Intent(this, SwipeWaitMoFangBlueActivity_.class);
        }else if(terminal_type.equals("6")) {
            intent = new Intent(this, SwipeWaitYiFengBlueActivity_.class);
        }else if(terminal_type.equals("7")) {
            intent = new Intent(this, SwipeWaitXinNuoBlueActivity_.class);
        }else if(terminal_type.equals("8")) {
            intent = new Intent(this, SwipeWaitBBPoseBuleActivity_.class);
        }
        intent.putExtra("feeRate", feeRate);
        intent.putExtra("topFeeRate", topFeeRate);
        intent.putExtra("tradetype", tradetype);
        intent.putExtra("money", money);
        intent.putExtra("queryModel", queryModel);
        intent.putExtra("blue_address", address);

        startActivity(intent);

        finish();
        ViewUtils.overridePendingTransitionCome(this);

    }

    @Click({R.id.ll_back, R.id.tv_right})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_back:
                ViewUtils.overridePendingTransitionBack(BluetoothSelectActivity.this);
                break;
            case R.id.tv_right:
                searchDevices();
                break;
        }
    }

    @UiThread
    void toast(String msg) {
        ViewUtils.makeToast(this, msg, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // 获得已经搜索到的蓝牙设备
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // 搜索到的不是已经绑定的蓝牙设备
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    if (mNewDevicesArrayAdapter.getPosition(device.getName() + "\n" + device.getAddress()) == -1) {
                        mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    }
                }
                // 搜索完成
            } else if (action
                    .equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                if (loadingDialogCanCancel != null) loadingDialogCanCancel.dismiss();
            }
        }
    };
}
