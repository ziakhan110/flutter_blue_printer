import 'dart:async';

import 'package:flutter_blue_printer/blue_device.dart';
import 'package:flutter/services.dart';

class BluePrinter {
  static const MethodChannel _channel = MethodChannel('flutter_blue_printer');

  static final BluePrinter _instance = BluePrinter._();
  BluePrinter._();

  static BluePrinter get instance => _instance;

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  Future<bool> isAvailable() async {
    return await _channel.invokeMethod('isAvailable');
  }

  Future<bool> get isOn async => await _channel.invokeMethod('isOn');

  Future<bool> get openSettings async =>
      await _channel.invokeMethod('openSettings');

  ///getBondedDevices()
  Future<List<BlueDevice>> getBondedDevices() async {
    final List list = await (_channel.invokeMethod('getBondedDevices'));
    return list.map((map) => BlueDevice.fromMap(map)).toList();
  }

  ///isDeviceConnected(BluetoothDevice device)
  Future<bool> isDeviceConnected(String address) async {
    return await _channel
        .invokeMethod('isDeviceConnected', {"address": address});
  }

  ///connect(BluetoothDevice device)
  Future<bool> connect(String address) async {
    return await _channel.invokeMethod('connect', {"address": address});
  }

  ///disconnect()
  Future<bool> disconnect(String address) async {
    return await _channel.invokeMethod('disconnect', {"address": address});
  }

  ///write(String message)
  Future<bool> writeBytes(Uint8List data, String address) async {
    return await _channel.invokeMethod('write', {
      "address": address,
      "data": data,
    });
  }
}
