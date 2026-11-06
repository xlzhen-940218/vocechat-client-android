import 'dart:io';

import 'package:device_info_plus/device_info_plus.dart';
import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';

enum MediaSaveType { image, video, audio, downloads }

/// Saves media through the Android MediaStore.
///
/// The `saver_gallery` plugin crashes on recent Android versions when saving
/// large video files, so Android uses the native implementation in
/// [MainActivity] instead.
class MediaSaver {
  static const MethodChannel _channel = MethodChannel('vocechat/media_store');

  static Future<bool> save({
    required File file,
    required MediaSaveType type,
    String? fileName,
    String? mimeType,
  }) async {
    if (!Platform.isAndroid) {
      return false;
    }

    final sdkInt = (await DeviceInfoPlugin().androidInfo).version.sdkInt;
    // Scoped storage (Android 10+) does not require a storage permission for
    // writing through MediaStore.
    if (sdkInt < 29) {
      final granted = await Permission.storage.request().isGranted;
      if (!granted) {
        return false;
      }
    }

    final result = await _channel.invokeMethod<String>('saveMedia', {
      'path': file.path,
      'fileName': fileName ?? file.path.split(Platform.pathSeparator).last,
      'mimeType': mimeType,
      'mediaType': type.name,
    });

    return result != null;
  }
}
