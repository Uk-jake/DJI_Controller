package com.dji.importSDKDemo;

import android.Manifest;
import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dji.importSDKDemo.view.DemoListView;

import java.util.concurrent.atomic.AtomicBoolean;

import dji.log.GlobalConfig;
import dji.sdk.base.BaseComponent;
import dji.sdk.sdkmanager.BluetoothProductConnector;
import dji.sdk.sdkmanager.DJISDKManager;

public class MainContent extends RelativeLayout {

    public static final String TAG = MainContent.class.getName();
    private static final String[] REQUIRED_PERMISSION_LIST = new String[] {
            Manifest.permission.VIBRATE, // Gimbal rotation
            Manifest.permission.INTERNET, // API requests
            Manifest.permission.ACCESS_WIFI_STATE, // WIFI connected products
            Manifest.permission.ACCESS_COARSE_LOCATION, // Maps
            Manifest.permission.ACCESS_NETWORK_STATE, // WIFI connected products
            Manifest.permission.ACCESS_FINE_LOCATION, // Maps
            Manifest.permission.CHANGE_WIFI_STATE, // Changing between WIFI and USB connection
            Manifest.permission.WRITE_EXTERNAL_STORAGE, // Log files
            Manifest.permission.BLUETOOTH, // Bluetooth connected products
            Manifest.permission.BLUETOOTH_ADMIN, // Bluetooth connected products
            Manifest.permission.READ_EXTERNAL_STORAGE, // Log files
            Manifest.permission.READ_PHONE_STATE, // Device UUID accessed upon registration
            Manifest.permission.RECORD_AUDIO // Speaker accessory
    };
    private static final int REQUEST_PERMISSION_CODE = 12345;
    private AtomicBoolean isRegistrationInProgress = new AtomicBoolean(false);
    private int lastProcess = -1;
    private Handler mHander = new Handler();

    private BaseComponent.ComponentListener mDJIComponentListener = new BaseComponent.ComponentListener() {

        @Override
        public void onConnectivityChange(boolean isConnected) {
            Log.d(TAG, "onComponentConnectivityChanged: " + isConnected);
            notifyStatusChange();
        }
    };

    private ProgressBar progressBar;
    private static BluetoothProductConnector connector = null;
    private TextView mTextConnectionStatus;
    private TextView mTextProduct;
    private TextView mTextModelAvailable;
    private Button mBtnRegisterApp;
    private Button mBtnOpen;
    private Button mBtnBluetooth;
    private ViewWrapper componentList =
            new ViewWrapper(new DemoListView(getContext()), R.string.activity_component_list);

    public MainContent(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (isInEditMode()) {
            return;
        }
        DJISampleApplication.getEventBus().register(this);
        initUI();
    }
    private void initUI() {
        Log.v(TAG, "initUI");

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mTextConnectionStatus = (TextView) findViewById(R.id.text_connection_status);
        mTextModelAvailable = (TextView) findViewById(R.id.text_model_available);
        mTextProduct = (TextView) findViewById(R.id.text_product_info);
        mBtnRegisterApp = (Button) findViewById(R.id.btn_registerApp);
        mBtnOpen = (Button) findViewById(R.id.btn_open);
        mBtnBluetooth = (Button) findViewById(R.id.btn_bluetooth);


        mBtnRegisterApp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                checkAndRequestPermissions();
            }
        });
        getmBtnRegisterAppForLDM.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isregisterForLDM = true;
                checkAndRequestPermissions();
            }
        });
        mBtnOpen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GeneralUtils.isFastDoubleClick()) {
                    return;
                }
                DJISampleApplication.getEventBus().post(componentList);
            }
        });
        mBtnBluetooth.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GeneralUtils.isFastDoubleClick()) {
                    return;
                }
                if (DJISampleApplication.getBluetoothProductConnector() == null) {
                    ToastUtils.setResultToToast("pls wait the sdk initiation finished");
                    return;
                }
                bluetoothView =
                        new ViewWrapper(new BluetoothView(getContext()), R.string.component_listview_bluetooth);
                DJISampleApplication.getEventBus().post(bluetoothView);
            }
        });
        mBridgeModeEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event != null
                        && event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (event != null && event.isShiftPressed()) {
                        return false;
                    } else {
                        // the user is done typing.
                        handleBridgeIPTextChange();
                    }
                }
                return false; // pass on to other listeners.
            }
        });
        mBridgeModeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.toString().contains("\n")) {
                    // the user is done typing.
                    // remove new line characcter
                    final String currentText = mBridgeModeEditText.getText().toString();
                    mBridgeModeEditText.setText(currentText.substring(0, currentText.indexOf('\n')));
                    handleBridgeIPTextChange();
                }
            }
        });
        ((TextView) findViewById(R.id.text_version)).setText(getResources().getString(R.string.sdk_version,
                DJISDKManager.getInstance().getRegistrationSDKVersion()
                        + " Debug:"
                        + GlobalConfig.DEBUG));
    }





    private void notifyStatusChange() {
        DJISampleApplication.getEventBus().post(new MainActivity.ConnectivityChangeEvent());
    }
}
