package minchu.com.dataagent;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Deva on 11/19/2016.
 */

public class BluetoothDataReader {
    private final Context context;

    public BluetoothDataReader(Context context) {
        this.context = context;
    }

    public void startReading() {
        BluetoothAdapter btAdapter = getBluetoothAdapter();
        if (btAdapter == null) return;

        BluetoothLeScanner scanner = btAdapter.getBluetoothLeScanner();
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        scanner.startScan(Collections.<ScanFilter>emptyList(), settings, new ScanRecordReader());
    }

    public void uploadScanBytes(SensorDataUploader sensorDataUploader, int count) {
        BluetoothAdapter btAdapter = getBluetoothAdapter();
        if (btAdapter == null) return;

        BluetoothLeScanner scanner = btAdapter.getBluetoothLeScanner();
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        scanner.startScan(Collections.<ScanFilter>emptyList(), settings, new LimitedScanRecordReader(sensorDataUploader, count, scanner));
    }

    @Nullable
    private BluetoothAdapter getBluetoothAdapter() {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if(btAdapter == null){
            Log.i(BluetoothDataReader.class.getName(), "No bluetooth adapter available");
            return null;
        }

        if(!btAdapter.isEnabled()){
            Log.i(BluetoothDataReader.class.getName(), "Enable bluetooth adapter");
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivity(enableBluetooth);
        }
        return btAdapter;
    }

    private class LimitedScanRecordReader extends ScanCallback {
        private final int limit;
        private final BluetoothLeScanner scanner;
        private int scanRecordRead = 0;
        private final SensorDataUploader sensorDataUploader;

        private LimitedScanRecordReader( SensorDataUploader sensorDataUploader, int limit, BluetoothLeScanner scanner) {
            this.limit = limit;
            this.scanner = scanner;
            this.sensorDataUploader = sensorDataUploader;
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if(scanRecordRead++ < limit) {
                long timestamp = System.currentTimeMillis() -
                        SystemClock.elapsedRealtime() +
                        result.getTimestampNanos() / 1000000;
                byte[] rawBytes = result.getScanRecord().getBytes();
                Log.i(DataTransferService.class.getName(), "Raw bytes: " + byteArrayToHex(rawBytes));
                sensorDataUploader.upload(timestamp, rawBytes);
            }else {
                scanner.stopScan(this);
            }
        }
        public String byteArrayToHex(byte[] a) {
            StringBuilder sb = new StringBuilder(a.length * 2);
            for(byte b: a)
                sb.append(String.format("%02x", b & 0xff));
            return sb.toString();
        }

        public void onScanFailed(int errorCode) {
            Log.i(DataTransferService.class.getName(), "Error code is:" + errorCode);
        }

        public void onBatchScanResults(java.util.List<android.bluetooth.le.ScanResult> results) {
            Log.i(DataTransferService.class.getName(), "Batch scan results");
        }
    }

    private class ScanRecordReader extends ScanCallback {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            byte []rawBytes = result.getScanRecord().getBytes();
            Log.i(DataTransferService.class.getName(), "Raw bytes: " + byteArrayToHex(rawBytes ));
//            Map<ParcelUuid, byte[]> serviceData = result.getScanRecord().getServiceData();
//            for(ParcelUuid uuid : serviceData.keySet()) {
//                Log.i(DataTransferService.class.getName(), uuid.toString() + ":" +  byteArrayToHex(serviceData.get(uuid)));
//            }
//            Log.i(DataTransferService.class.getName(),result.toString());
        }
        public String byteArrayToHex(byte[] a) {
            StringBuilder sb = new StringBuilder(a.length * 2);
            for(byte b: a)
                sb.append(String.format("%02x", b & 0xff));
            return sb.toString();
        }

        public void onScanFailed(int errorCode) {
            Log.i(DataTransferService.class.getName(), "Error code is:" + errorCode);
        }

        public void onBatchScanResults(java.util.List<android.bluetooth.le.ScanResult> results) {
            Log.i(DataTransferService.class.getName(), "Batch scan results");
        }
    }
}
