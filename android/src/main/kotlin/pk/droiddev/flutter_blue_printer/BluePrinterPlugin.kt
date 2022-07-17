package pk.droiddev.flutter_blue_printer

import android.app.Activity
import android.content.Intent
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.PrintWriter
import java.io.StringWriter

class BluePrinterPlugin : FlutterPlugin, ActivityAware, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private lateinit var bluePrinter: BluePrinter
    private lateinit var activity: Activity
    private lateinit var result: Result
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_blue_printer")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        this.result = result
        val arguments = call.arguments<Map<String, Any>>() ?: mapOf()
        when (call.method) {
            "isOn" -> {
                coroutineScope.launch {
                    try {
                        val resp = bluePrinter.isOn()
                        sendSuccess(resp)
                    } catch (ex: Exception) {
                        sendError("StatusError", ex)
                    }
                }
            }
            "isAvailable" -> {
                result.success(bluePrinter.isAvailable())
            }
            "getBondedDevices" -> {
                coroutineScope.launch {
                    try {
                        val devices = bluePrinter.getBondedDevices()
                        sendSuccess(devices)
                    } catch (ex: Exception) {
                        sendError("DeviceListError", ex)
                    }
                }
            }
            "isDeviceConnected" -> {
                if (arguments.containsKey("address")) {
                    val address = arguments["address"] as String
                    coroutineScope.launch {
                        try {
                            val resp = bluePrinter.isDeviceConnected(address)
                            sendSuccess(resp)
                        } catch (ex: Exception) {
                            sendError("StatusError", ex)
                        }
                    }
                } else {
                    result.error("InvalidArguments", "Address is required", null)
                }
            }
            "openSettings" -> {
                activity.startActivity(
                    Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS),
                    null
                )
                result.success(true)
            }
            "connect" -> {
                if (arguments.containsKey("address")) {
                    coroutineScope.launch {
                        try {
                            val address = arguments["address"].toString()
                            val resp = bluePrinter.connect(address)
                            sendSuccess(resp)
                        } catch (ex: Exception) {
                            sendError("ConnectError", ex)
                        }
                    }
                } else {
                    result.error("InvalidArguments", "Address is required", null)
                }
            }
            "disconnect" -> {
                try {
                    if (arguments.containsKey("address")) {
                        coroutineScope.launch {
                            try {
                                val resp = bluePrinter.disconnect(arguments["address"].toString())
                                sendSuccess(resp)
                            } catch (ex: Exception) {
                                sendError("ErrorDisconnect", ex)
                            }
                        }
                    } else {
                        result.error("InvalidArguments", "Address is required", null)
                    }
                } catch (ex: Exception) {
                    result.error("DisconnectError", ex.message, exceptionString(ex))
                }
            }
            "write" -> {
                if (arguments.containsKey("data") && arguments.containsKey("address")) {
                    coroutineScope.launch {
                        try {
                            val data = arguments["data"] as ByteArray
                            val address = arguments["address"].toString()
                            if (!bluePrinter.isOn()) {
                                sendError(
                                    "WriteError",
                                    Exception("NotOn", Throwable("Bluetooth device is not on."))
                                )
                                return@launch
                            }
                            if (!bluePrinter.isDeviceConnected(address)) {
                                sendError(
                                    "WriteError",
                                    Exception(
                                        "NotConnected",
                                        Throwable("The selected device is ont connected.")
                                    )
                                )
                                return@launch
                            }
                            bluePrinter.write(data, address)
                            sendSuccess(true)
                        } catch (ex: Exception) {
                            sendError("WriteError", ex)
                        }
                    }
                } else {
                    result.error("invalid_argument", "argument 'message' not found", null)
                }
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    private suspend fun sendSuccess(resp: Any) {
        withContext(Dispatchers.Main) {
            result.success(resp)
        }
    }

    private suspend fun sendError(title: String, ex: Exception) {
        withContext(Dispatchers.Main) {
            result.error(title, ex.message, exceptionString(ex))
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(activityBinding: ActivityPluginBinding) {
        bluePrinter = BluePrinter(activityBinding.activity)
        bluePrinter.setup()
    }

    override fun onDetachedFromActivityForConfigChanges() {
    }

    override fun onReattachedToActivityForConfigChanges(activityBinding: ActivityPluginBinding) {
    }

    override fun onDetachedFromActivity() {
        bluePrinter.close()
    }

    private fun exceptionString(ex: Exception): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        ex.printStackTrace(pw)
        return sw.toString()
    }
}
