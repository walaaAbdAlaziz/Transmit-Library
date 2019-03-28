package com.receet.virtualbeacon

import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import com.example.transmitlibrary.R
import java.util.*

class VirtualBeaconTransmitter(private val context: Context){
    val receetServiceUUID = UUID.fromString(context.getString(R.string.receet_service_UUID))
    val charstrsticUUID = UUID.fromString(context.getString(R.string.receet_beacon_ID_charastrastic_UUID))

    private lateinit var bluetoothManager: BluetoothManager
    private var bluetoothGattServer: BluetoothGattServer? = null


    init {

    }

    fun startTransmitting(){
        startBLEAdvertising()
        startGATTServer()
    }


    private val callba = object : BluetoothGattServerCallback() {
        override fun onCharacteristicReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic?){
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
            bluetoothGattServer?.sendResponse(device,requestId, BluetoothGatt.GATT_SUCCESS,0,"Os6z99".toByteArray())
        }
    }
    private fun startGATTServer(){
        bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothGattServer = bluetoothManager.openGattServer(context, callba)

        val service = BluetoothGattService(receetServiceUUID,
            BluetoothGattService.SERVICE_TYPE_PRIMARY)


        val currentTime = BluetoothGattCharacteristic(charstrsticUUID,
            //Read-only characteristic, supports notifications
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ)


        service.addCharacteristic(currentTime)


        bluetoothGattServer?.addService(service)
            ?: Log.w("gatt", "Unable to create GATT server")

    }

    private fun startBLEAdvertising(){
        val advertiser = BluetoothAdapter.getDefaultAdapter().bluetoothLeAdvertiser

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true)
            .build()



        val pUuid = ParcelUuid(receetServiceUUID)



        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .setIncludeTxPowerLevel(false)
            .addServiceUuid(pUuid)
            .build()



        val advertisingCallback = object : AdvertiseCallback() {
            override fun onStartFailure(errorCode: Int) {
                Log.e("BLE", "Advertising onStartFailure: $errorCode")
                super.onStartFailure(errorCode)
            }
        }

        advertiser.startAdvertising(settings, data, advertisingCallback)
    }
}