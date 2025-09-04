import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_blue_printer/flutter_blue_printer.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {});

  tearDown(() {});

  test('getPlatformVersion', () async {
    expect(await BluePrinter.platformVersion, '42');
  });
}
