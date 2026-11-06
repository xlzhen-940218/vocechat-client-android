import 'package:flutter/services.dart';

/// Limits the input length using the same "wide character counts as 2 bytes"
/// rule as the server side.
///
/// This replaces `VoceTextInputFormatter` from `voce_widgets`, which creates an
/// `EmojiParser` and compiles several regular expressions for every single
/// character on every keystroke. On long messages that made typing freeze
/// (see issue #198).
class VoceByteTextInputFormatter extends TextInputFormatter {
  const VoceByteTextInputFormatter(this.maxLength)
      : assert(maxLength == -1 || maxLength > 0);

  final int maxLength;

  @override
  TextEditingValue formatEditUpdate(
      TextEditingValue oldValue, TextEditingValue newValue) {
    if (maxLength <= 0 || bytesLength(newValue.text) <= maxLength) {
      return newValue;
    }

    if (bytesLength(oldValue.text) == maxLength) {
      return oldValue;
    }

    return truncate(newValue, maxLength);
  }

  static TextEditingValue truncate(TextEditingValue value, int maxLength) {
    final buffer = StringBuffer();
    int length = 0;

    for (final rune in value.text.runes) {
      final runeLength = isWideRune(rune) ? 2 : 1;
      if (length + runeLength > maxLength) {
        break;
      }
      buffer.writeCharCode(rune);
      length += runeLength;
    }

    final text = buffer.toString();
    return TextEditingValue(
      text: text,
      selection: TextSelection(
        baseOffset: _clamp(value.selection.baseOffset, text.length),
        extentOffset: _clamp(value.selection.extentOffset, text.length),
      ),
      composing: TextRange.empty,
    );
  }

  static int bytesLength(String value) {
    int count = 0;
    for (final rune in value.runes) {
      count += isWideRune(rune) ? 2 : 1;
    }
    return count;
  }

  /// CJK characters, emoji and a few other scripts are counted as 2 bytes,
  /// matching the previous behaviour without any per-character allocation.
  static bool isWideRune(int rune) {
    if (rune <= 0x7F) {
      return false;
    }

    return (rune >= 0x0600 && rune <= 0x06FF) || // Arabic
        (rune >= 0x1100 && rune <= 0x11FF) || // Hangul Jamo
        (rune >= 0x2E80 && rune <= 0x303F) || // CJK radicals & punctuation
        (rune >= 0x3040 && rune <= 0x33FF) || // Hiragana, Katakana, CJK symbols
        (rune >= 0x3400 && rune <= 0x4DBF) || // CJK extension A
        (rune >= 0x4E00 && rune <= 0x9FFF) || // CJK unified ideographs
        (rune >= 0xA960 && rune <= 0xA97F) || // Hangul Jamo extended-A
        (rune >= 0xAC00 && rune <= 0xD7AF) || // Hangul syllables
        (rune >= 0xF900 && rune <= 0xFAFF) || // CJK compatibility ideographs
        (rune >= 0xFE30 && rune <= 0xFE4F) || // CJK compatibility forms
        (rune >= 0xFF00 && rune <= 0xFFEF) || // Fullwidth forms
        (rune >= 0x1F000 && rune <= 0x1FAFF) || // Emoji & pictographs
        (rune >= 0x20000 && rune <= 0x3FFFF); // CJK extensions B and later
  }

  static int _clamp(int offset, int length) {
    if (offset < 0) return 0;
    if (offset > length) return length;
    return offset;
  }
}
