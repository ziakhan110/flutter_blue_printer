package pk.droiddev.flutter_blue_printer

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.util.Log
import io.flutter.plugin.common.MethodChannel.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class BluePrinter(private val activity: Activity) {
    private var bluetoothAdapter: BluetoothAdapter? = null
    private lateinit var bluetoothManager: BluetoothManager
    private val connections = HashMap<String, ConnectedThread>()

    fun close() {
        this.connections.forEach {
            it.value.close()
        }
        this.connections.clear()
    }

    @Synchronized
    fun setup() {
        bluetoothManager = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
    }

    fun connect(address: String): Boolean {
        val device: BluetoothDevice = bluetoothAdapter?.getRemoteDevice(address) ?: return false
        val socket =
            device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
                ?: return false
        bluetoothAdapter?.cancelDiscovery()
        socket.connect()
        connections[address] = ConnectedThread(socket)
        return true
    }

    fun write(bytes: ByteArray, address: String) {
        connections[address]?.write(bytes)
    }

    fun getBondedDevices(): List<Map<String, Any>> {
        val list: MutableList<Map<String, Any>> = ArrayList()
        for (device in bluetoothAdapter!!.bondedDevices) {
            val ret: MutableMap<String, Any> = java.util.HashMap()
            ret["address"] = device.address
            ret["name"] = device.name
            ret["type"] = device.type
            list.add(ret)
        }
        return list
    }

    fun isDeviceConnected(address: String): Boolean {
        return connections[address] != null && BluetoothDevice.ACTION_ACL_CONNECTED == Intent(
            BluetoothDevice.ACTION_ACL_CONNECTED
        ).action
    }

    fun isOn(): Boolean {
        return bluetoothAdapter?.isEnabled ?: false
    }

    fun disconnect(address: String): Boolean {
        return if (connections.isNotEmpty() && connections[address] != null) {
            connections[address]?.close()
            connections.remove(address)
            true
        } else {
            false
        }
    }

    fun isAvailable(): Boolean {
        return this.bluetoothAdapter != null
    }

    private class ConnectedThread(private val socket: BluetoothSocket) {
        private val inputStream: InputStream?
        private val outputStream: OutputStream?

        init {
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null
            try {
                tmpIn = socket.inputStream
                tmpOut = socket.outputStream
            } catch (e: IOException) {
                e.printStackTrace()
            }
            inputStream = tmpIn
            outputStream = tmpOut
        }

        suspend fun read() {
            withContext(Dispatchers.IO) {
                val buffer = ByteArray(1024)
                val bytes = inputStream?.read(buffer)
            }
        }

        fun write(bytes: ByteArray?) {
            outputStream?.write(bytes)
        }

        fun close() {
            try {
                outputStream!!.flush()
                outputStream.close()
                inputStream!!.close()
                socket.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}