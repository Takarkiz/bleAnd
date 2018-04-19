package com.tech.takhaki.sampblues;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
    //BluetoothManager bluetoothManager = (BluetoothManager)
    // BLEスキャンのタイムアウト時間
    private static final long SCAN_PERIOD = 10000;

    private ArrayList<BluetoothDevice> deviceList = new ArrayList<>();
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanCallback mScanCallback;
    private Handler mHandler = new Handler();

    private BluetoothGatt bluetoothGatt;
    //スキャン状態
    private Boolean isScanning = false;
    //取り出したServiceを格納
    private List<BluetoothGattService> serviceList;
    private BluetoothGattCharacteristic characteristic;

    public void scanStart(View v){
        scan(true);
    }


    // ScanCallbackの初期化
    private ScanCallback initCallbacks() {

        return new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);

                if (result != null && result.getDevice() != null) {
                    //スャンが完了した時
                    if (isAdded(result.getDevice())) {
                        // No add
                    } else {
                        saveDevice(result.getDevice());
                    }

                    //スキャンが完了したのでコネクトを開始
                    //connect();
                }

            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
            }

        };

    }

    // スキャン実施
    public void scan(boolean enable) {

        mScanCallback = initCallbacks();

        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isScanning = false;
                    mBluetoothLeScanner.stopScan(mScanCallback);
                }
            }, SCAN_PERIOD);

            isScanning = true;
            mBluetoothLeScanner.startScan(mScanCallback);
            // スキャンフィルタを設定するならこちら
            // mBluetoothLeScanner.startScan(filters, settings, mScanCallback);
        } else {
            isScanning = false;
            mBluetoothLeScanner.stopScan(mScanCallback);
        }

    }

    // スキャン停止
    public void stopScan() {

        if (mBluetoothLeScanner != null) {
            mBluetoothLeScanner.stopScan(mScanCallback);
        }

    }

    // スキャンしたデバイスのリスト保存
    public void saveDevice(BluetoothDevice device) {

        if (deviceList == null) {
            deviceList = new ArrayList<>();
        }

        deviceList.add(device);

    }

    // スキャンしたデバイスがリストに追加済みかどうかの確認
    public boolean isAdded(BluetoothDevice device) {

        if (deviceList != null && deviceList.size() > 0) {
            return deviceList.contains(device);
        } else {
            return false;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        // mBluetoothAdapterの取得
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // mBluetoothLeScannerの初期化
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
    }

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            // 接続成功し、サービス取得
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                bluetoothGatt = gatt;
                discoverService();
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            serviceList = gatt.getServices();

            for (BluetoothGattService s : serviceList) {
                // サービス一覧を取得したり探したりする処理
                // あとキャラクタリスティクスを取得したり探したりしてもよい
            }
        }
    };



    // Gattへの接続要求
    public void connect(Context context, BluetoothDevice device) {

        bluetoothGatt = device.connectGatt(context, false, mGattCallback);
        bluetoothGatt.connect();

    }

    // サービス取得要求
    public void discoverService() {

        if (bluetoothGatt != null) {
            bluetoothGatt.discoverServices();
        }

    }

    // キャラクタリスティック設定UUID
    String CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    private void notificationCharacteristic() {
        // Notification を要求する
        // characteristicに関しの記述未だ
        boolean registered = bluetoothGatt.setCharacteristicNotification(characteristic, true);

        // Characteristic の Notification 有効化
        if (registered) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(CHARACTERISTIC_CONFIG));

            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            bluetoothGatt.writeDescriptor(descriptor);
        }
    }


}
