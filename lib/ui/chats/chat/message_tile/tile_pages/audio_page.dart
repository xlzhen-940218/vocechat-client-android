import 'dart:io';

import 'package:audioplayers/audioplayers.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:vocechat_client/l10n/app_localizations.dart';
import 'package:vocechat_client/app.dart';
import 'package:vocechat_client/ui/app_colors.dart';
import 'package:vocechat_client/ui/app_text_styles.dart';

/// In-app preview and playback for audio files.
class AudioPage extends StatefulWidget {
  final String fileName;
  final File file;

  const AudioPage({required this.fileName, required this.file, Key? key})
      : super(key: key);

  @override
  State<AudioPage> createState() => _AudioPageState();
}

class _AudioPageState extends State<AudioPage> {
  final AudioPlayer _player = AudioPlayer();

  Duration _duration = Duration.zero;
  Duration _position = Duration.zero;
  bool _isPlaying = false;
  bool _isDragging = false;
  double _dragValue = 0;

  @override
  void initState() {
    super.initState();

    _player.onDurationChanged.listen((duration) {
      if (mounted) setState(() => _duration = duration);
    });

    _player.onPositionChanged.listen((position) {
      if (mounted && !_isDragging) setState(() => _position = position);
    });

    _player.onPlayerComplete.listen((_) {
      if (!mounted) return;
      setState(() {
        _isPlaying = false;
        _position = Duration.zero;
      });
    });

    _prepare();
  }

  Future<void> _prepare() async {
    try {
      await _player.setReleaseMode(ReleaseMode.stop);
      await _player.setSource(DeviceFileSource(widget.file.path));
    } catch (e) {
      App.logger.severe(e);
    }
  }

  @override
  void dispose() {
    _player.dispose();
    super.dispose();
  }

  Future<void> _togglePlay() async {
    try {
      if (_isPlaying) {
        await _player.pause();
      } else {
        if (_position >= _duration && _duration > Duration.zero) {
          await _player.seek(Duration.zero);
        }
        await _player.resume();
      }
      if (mounted) setState(() => _isPlaying = !_isPlaying);
    } catch (e) {
      App.logger.severe(e);
    }
  }

  String _formatDuration(Duration duration) {
    String twoDigits(int n) => n.toString().padLeft(2, "0");
    final minutes = twoDigits(duration.inMinutes.remainder(60));
    final seconds = twoDigits(duration.inSeconds.remainder(60));
    return duration.inHours > 0
        ? "${twoDigits(duration.inHours)}:$minutes:$seconds"
        : "$minutes:$seconds";
  }

  @override
  Widget build(BuildContext context) {
    final maxValue = _duration.inMilliseconds.toDouble();
    final sliderMax = maxValue <= 0 ? 1.0 : maxValue;
    final value = _isDragging
        ? _dragValue
        : _position.inMilliseconds
            .clamp(0, _duration.inMilliseconds)
            .toDouble();

    return Scaffold(
      appBar: AppBar(
        elevation: 0,
        backgroundColor: AppColors.barBg,
        title: Text(
          AppLocalizations.of(context)!.file,
          style: AppTextStyles.titleLarge,
        ),
        leading: CupertinoButton(
            padding: EdgeInsets.zero,
            onPressed: () => Navigator.pop(context),
            child: Icon(Icons.arrow_back_ios_new, color: AppColors.grey97)),
        centerTitle: true,
      ),
      body: SafeArea(
        child: Center(
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 24),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Icon(Icons.audiotrack,
                    size: 96, color: AppColors.primaryBlue),
                const SizedBox(height: 24),
                Text(
                  widget.fileName,
                  maxLines: 3,
                  textAlign: TextAlign.center,
                  overflow: TextOverflow.ellipsis,
                  style: AppTextStyles.titleLarge,
                ),
                const SizedBox(height: 32),
                Slider(
                  value: value.clamp(0.0, sliderMax).toDouble(),
                  max: sliderMax,
                  onChanged: maxValue == 0
                      ? null
                      : (newValue) {
                          setState(() {
                            _isDragging = true;
                            _dragValue = newValue;
                          });
                        },
                  onChangeEnd: maxValue == 0
                      ? null
                      : (newValue) {
                          setState(() {
                            _isDragging = false;
                            _position = Duration(milliseconds: newValue.round());
                          });
                          _player.seek(_position);
                        },
                ),
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 8),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text(
                          _formatDuration(_isDragging
                              ? Duration(
                                  milliseconds: _dragValue.round())
                              : _position),
                          style: AppTextStyles.labelMedium),
                      Text(_formatDuration(_duration),
                          style: AppTextStyles.labelMedium),
                    ],
                  ),
                ),
                const SizedBox(height: 16),
                IconButton(
                    iconSize: 64,
                    onPressed: maxValue == 0 ? null : _togglePlay,
                    icon: Icon(
                        _isPlaying
                            ? Icons.pause_circle_filled
                            : Icons.play_circle_filled,
                        color: AppColors.primaryBlue)),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
