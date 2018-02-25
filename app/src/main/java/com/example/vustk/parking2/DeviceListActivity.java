package com.example.vustk.parking2;

/**
 * Created by vustk on 2018-02-11.
 */


        import android.app.Activity;
        import android.bluetooth.BluetoothAdapter;
        import android.bluetooth.BluetoothDevice;
        import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;
        import android.content.IntentFilter;
        import android.os.Bundle;
        import android.support.v7.app.AppCompatActivity;
        import android.view.View;
        import android.widget.AdapterView;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.ListView;
        import android.widget.TextView;

        import java.util.Set;

public class DeviceListActivity extends AppCompatActivity {
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    private ArrayAdapter<String> nearbyDeviceAdapter;
    private ArrayAdapter<String> pairedDeviceAdapter;

    private BluetoothAdapter bluetoothAdapter;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_list);

        setResult(Activity.RESULT_CANCELED);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.cancelDiscovery();
                    button.setText("Search Device");
                    return;
                }
                nearbyDeviceAdapter.clear();
                bluetoothAdapter.startDiscovery();
                button.setText("CANCEL");
            }
        });

        nearbyDeviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1); // for newly discovered devices
        pairedDeviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1); // for already paired devices

        ListView pairedDeviceView = (ListView) findViewById(R.id.paired_devices);
        pairedDeviceView.setAdapter(pairedDeviceAdapter);
        pairedDeviceView.setOnItemClickListener(deviceClickListener);

        ListView nearbyDeviceView = (ListView) findViewById(R.id.nearby_devices);
        nearbyDeviceView.setAdapter(nearbyDeviceAdapter);
        nearbyDeviceView.setOnItemClickListener(deviceClickListener);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);   // Register for broadcasts when device is discovered
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);           // Register for broadcasts when discovery has finished
        this.registerReceiver(receiver, filter);

        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDeviceSet = bluetoothAdapter.getBondedDevices();

        if (pairedDeviceSet.size() > 0) {
            for (BluetoothDevice device : pairedDeviceSet) {
                pairedDeviceAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        this.unregisterReceiver(receiver);
    }

    // The on-click listener for all devices in the ListViews
    private AdapterView.OnItemClickListener deviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String addr = info.substring(info.length() - 17);

            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, addr);

            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

    // The BroadcastReceiver that listens for discovered devices
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    nearbyDeviceAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                button.setText("Search Device");
            }
        }
    };
}