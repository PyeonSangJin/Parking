package com.example.vustk.parking2;

/**
 * Created by vustk on 2018-02-11.
 */

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Bluetooth extends AppCompatActivity {
    private static final int CONNECTED = 1;
    private static final int DISCONNECTED = 2;
    private static final int MESSAGE_READ = 3;
    private static final int MESSAGE_WRITE = 4;

    private static final int REQUEST_CONNECT_DEVICE = 1;

    private ArrayAdapter<String> messageAdapter;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothChat chat;
    private Button buttonConnect;
    private Button buttonSend;

    private EditText editText;

    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    boolean isConnectBt = true;
    private int pariedDeviceCnt = 0;
    private Set<BluetoothDevice> devices;

    BluetoothDevice mRemoteDevice;

   public void runToBt() {

        if ((bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()) == null) {
            Toast.makeText(Bluetooth.this, "Bluetooth is not supported", Toast.LENGTH_SHORT).show();
            isConnectBt = false; //직접결제 ㄱㄱ
        }
        // ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION permission is required for Bluetooth from Marshmallow
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }


     //cr
       if (!bluetoothAdapter.isEnabled()) { // 블루투스 지원하며 비활성 상태인 경우.
           Toast.makeText(getApplicationContext(), "현재 블루투스가 비활성 상태입니다.", Toast.LENGTH_LONG).show();
           Intent requestBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
           // REQUEST_ENABLE_BT : 블루투스 활성 상태의 변경 결과를 App 으로 알려줄 때 식별자로 사용(0이상)
           /**
            startActivityForResult 함수 호출후 다이얼로그가 나타남
            "예" 를 선택하면 시스템의 블루투스 장치를 활성화 시키고
            "아니오" 를 선택하면 비활성화 상태를 유지 한다.
            선택 결과는 onActivityResult 콜백 함수에서 확인할 수 있다.
            */
           startActivityForResult(requestBluetooth, 20);
       }//else
           /////////////////////////////////////////////////////////////////여기 터ㅕㅅ다 ㅁㄻ


/*
        buttonConnect = (Button) findViewById(R.id.button_connect);
        buttonConnect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (bluetoothAdapter.isEnabled() == false) {
                    // Request to enable bluetooth
                    startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
                    return;
                }
                if (chat == null) {
                    // Launch DeviceListActivity to search bluetooth device
                    startActivityForResult(new Intent(Bluetooth.this, DeviceListActivity.class), REQUEST_CONNECT_DEVICE);
                } else {
                    chat.close();
                }
            }
        });
*/

        buttonSend = (Button) findViewById(R.id.button_send);
        buttonSend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                TextView textView = (TextView) findViewById(R.id.edit_text);

                chat.send(textView.getText().toString().getBytes());
                editText.setText("");
            }
        });

        editText = (EditText) findViewById(R.id.edit_text);
        messageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        ListView messageView = (ListView) findViewById(R.id.message_view);
        messageView.setAdapter(messageAdapter);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() { //검색 시간을 줘야 하지 않나..
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                devices.add(device);

                pariedDeviceCnt = devices.size();
            }
        }
    };


    void selectDevice() {
        // getBondedDevices() : 페어링된 장치 목록 얻어오는 함수
        devices = bluetoothAdapter.getBondedDevices();

        IntentFilter filterDevices = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver,filterDevices);

        if (pariedDeviceCnt == 0) { // 페어링된 장치가 없는 경우.
            Toast.makeText(getApplicationContext(), "주변에 주차기기가 없습니다.", Toast.LENGTH_LONG).show();
            isConnectBt = false; // 직접결제만 가능 -> 블루투스 사용 불가.
        }

        bluetoothAdapter.cancelDiscovery(); // 검색중단
        // 페어링된 장치가 있는 경우.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("블루투스 장치 선택");

        List<String> listItems = new ArrayList<String>();
        for (BluetoothDevice device : devices) {
            // device.getName() : 단말기의 Bluetooth Adapter 이름을 반환.
            listItems.add(device.getName());
        }
        listItems.add("취소");  // 취소 항목 추가.

        // CharSequence : 변경 가능한 문자열.
        // toArray : List형태로 넘어온것 배열로 바꿔서 처리하기 위한 toArray() 함수.
        final CharSequence[] items = listItems.toArray(new CharSequence[listItems.size()]);
        // toArray 함수를 이용해서 size만큼 배열이 생성 되었다.
        listItems.toArray(new CharSequence[listItems.size()]);

        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {
                // TODO Auto-generated method stub
                if (item == pariedDeviceCnt) { // 연결할 장치를 선택하지 않고 '취소' 를 누른 경우.
                    Toast.makeText(getApplicationContext(), "연결할 장치를 선택하지 않았습니다.\n 직접결제를 하세요.", Toast.LENGTH_LONG).show();
                    isConnectBt = false;
                } else { // 연결할 장치를 선택한 경우, 선택한 장치와 연결을 시도함.
                //    connectToSelectedDevice(items[item].toString());
                }
            }
            //////////////////////////////////////////////////////////////////////////////
        });
        builder.setCancelable(false);  // 뒤로 가기 버튼 사용 금지.
        AlertDialog alert = builder.create();
        alert.show();
    }


//startActivityForResult -- onActivityResult


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (chat != null) {
            chat.close();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // When DeviceListActivity returns with a device to connect
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    // MAC address
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

                    BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
                    BluetoothSocket socket;

                    try {
                        socket = device.createRfcommSocketToServiceRecord(uuid);
                    } catch (IOException e) {
                        break;
                    }
                    chat = new BluetoothChat(socket, handler);
                    chat.start();
                }
                break;
        }
    }

    // The Handler that gets information back from the BluetoothChat
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CONNECTED:
                    buttonConnect.setText("Disconnect");
                    buttonSend.setEnabled(true);
                    break;
                case DISCONNECTED:
                    buttonConnect.setText("Connect to bluetooth device");
                    buttonSend.setEnabled(false);
                    chat = null;
                    break;
                case MESSAGE_READ:
                    try {
                        // Encoding with "EUC-KR" to read 한글
                        messageAdapter.add("< " + new String((byte[]) msg.obj, 0, msg.arg1, "EUC-KR"));
                    } catch (UnsupportedEncodingException e) {
                    }
                    break;
                case MESSAGE_WRITE:
                    messageAdapter.add("> " + new String((byte[]) msg.obj));
                    break;
            }
        }
    };

    // This class connect with a bluetooth device and perform data transmissions when connected.
    private class BluetoothChat extends Thread {
        private BluetoothSocket socket;
        private Handler handler;
        private InputStream inputStream;
        private OutputStream outputStream;

        public BluetoothChat(BluetoothSocket socket, Handler handler) {
            this.socket = socket;
            this.handler = handler;
        }

        public void run() {
            try {
                socket.connect();
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (Exception e) {
                close();
                return;
            }
            handler.obtainMessage(CONNECTED, -1, -1).sendToTarget();

            while (true) {
                try {
                    byte buffer[] = new byte[1024];

                    int bytes = 0;

                    // Read single byte until '\0' is found
                    for (; (buffer[bytes] = (byte) inputStream.read()) != '\0'; bytes++) ;
                    handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    close();
                    break;
                }
            }
        }

        public void close() {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
                handler.obtainMessage(DISCONNECTED, -1, -1).sendToTarget();
            }
        }

        public void send(byte[] buffer) {
            try {
                outputStream.write(buffer);
                outputStream.write('\n');
                handler.obtainMessage(MESSAGE_WRITE, buffer.length, -1, buffer).sendToTarget();
            } catch (IOException e) {
                close();
            }
        }
    }
}