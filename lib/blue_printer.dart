import 'dart:async';
import 'dart:typed_data';

import 'package:blue_printer/blue_device.dart';
import 'package:flutter/services.dart';

class BluePrinter {
  static const MethodChannel _channel = MethodChannel('blue_printer');
  
  static final BluePrinter _instance = BluePrinter._();
  BluePrinter._();

  static BluePrinter get instance => _instance;

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  Future<bool> isAvailable(BlueDevice device) async {
    return await _channel.invokeMethod('isAvailable', device.toMap());
  }

  Future<bool> get isOn async => await _channel.invokeMethod('isOn');

  Future<bool> isConnected(BlueDevice device) async =>
      await _channel.invokeMethod('isConnected', device.toMap());

  Future<bool> get openSettings async =>
      await _channel.invokeMethod('openSettings');

  ///getBondedDevices()
  Future<List<BlueDevice>> getBondedDevices() async {
    final List list = await (_channel.invokeMethod('getBondedDevices'));
    return list.map((map) => BlueDevice.fromMap(map)).toList();
  }

  ///isDeviceConnected(BluetoothDevice device)
  Future<bool> isDeviceConnected(BlueDevice device) async {
    return await _channel.invokeMethod('isDeviceConnected', device.toMap());
  }

  ///connect(BluetoothDevice device)
  Future<bool> connect(BlueDevice device) async {
    return await _channel.invokeMethod('connect', device.toMap());
  }

  ///disconnect()
  Future<bool> disconnect(BlueDevice device) async {
    return await _channel.invokeMethod('disconnect', device.toMap());
  }

  ///write(String message)
  Future<bool> writeBytes(Uint8List data, BlueDevice device) async {
    final arguments = device.toMap();
    arguments['data'] = data;
    return await _channel.invokeMethod('write', arguments);
  }
}
