package minchu.com.dataagent;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataTransferService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String hostName = "sundeep.com";
        String port = "8080";
        final SensorDataUploader sensorUpdateLoader = new SensorDataUploader(hostName, port);
        final BluetoothDataReader reader = new BluetoothDataReader(getApplicationContext());
        Thread readerThread = new Thread() {
            @Override
            public void run() {
                reader.uploadScanBytes(sensorUpdateLoader, 10);
            }
        };
        readerThread.start();
        return Service.START_NOT_STICKY;
    }
}
