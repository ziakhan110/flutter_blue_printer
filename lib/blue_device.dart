class BlueDevice {
  late String name;
  late String address;
  int type = 0;
  bool connected = false;

  BlueDevice({
    required this.name,
    required this.address,
  });

  BlueDevice.fromMap(Map map) {
    name = map['name'] ?? "";
    address = map['address'] ?? "";
    type = map['type'] ?? 0;
    connected = map['connected'] ?? false;
  }

  Map<String, dynamic> toMap() => {
        'name': name,
        'address': address,
        'type': type,
        'connected': connected,
      };

  @override
  operator ==(covariant BlueDevice other) {
    return other.address == address;
  }

  @override
  int get hashCode => address.hashCode;
}
