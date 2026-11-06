import 'package:flutter/material.dart';
import 'package:vocechat_client/l10n/app_localizations.dart';
import 'package:flutter_markdown/flutter_markdown.dart';
import 'package:vocechat_client/dao/init_dao/chat_msg.dart';
import 'package:vocechat_client/shared_funcs.dart';
import 'package:vocechat_client/ui/app_colors.dart';

class VoceMdBubble extends StatelessWidget {
  final ChatMsgM chatMsgM;

  late final String? _mdText;

  late final bool _edited;

  VoceMdBubble({Key? key, required this.chatMsgM}) : super(key: key) {
    _mdText = chatMsgM.msgNormal?.content ?? chatMsgM.msgReply?.content;

    _edited = chatMsgM.reactionData?.hasEditedText ?? false;
  }

  static final RegExp _iframeTagRegExp =
      RegExp(r"<iframe[^>]*>", caseSensitive: false);
  static final RegExp _closingIframeRegExp =
      RegExp(r"</iframe\s*>", caseSensitive: false);
  static final RegExp _iframeSrcRegExp =
      RegExp(r"""src\s*=\s*["']([^"']+)["']""", caseSensitive: false);

  /// flutter_markdown has no support for embedded frames, so embedded pages
  /// are offered as links instead of being silently dropped (issue #151).
  List<String> _extractIframeUrls(String text) {
    final urls = <String>[];

    for (final match in _iframeTagRegExp.allMatches(text)) {
      final srcMatch = _iframeSrcRegExp.firstMatch(match.group(0) ?? "");
      final src = srcMatch?.group(1)?.trim();
      if (src != null && src.isNotEmpty) {
        urls.add(src);
      }
    }

    return urls;
  }

  @override
  Widget build(BuildContext context) {
    final text = _mdText ?? AppLocalizations.of(context)!.noContent;
    final iframeUrls = _extractIframeUrls(text);
    final markdownData = iframeUrls.isEmpty
        ? text
        : text
            .replaceAll(_iframeTagRegExp, "")
            .replaceAll(_closingIframeRegExp, "")
            .trim();

    return Wrap(children: [
      if (markdownData.isNotEmpty)
        MarkdownBody(
          data: markdownData,
          selectable: true,
          styleSheet: MarkdownStyleSheet(
              a: TextStyle(
                  fontWeight: FontWeight.w400,
                  fontSize: 14,
                  color: AppColors.coolGrey700)),
          onTapLink: (text, url, title) {
            if (url != null) {
              SharedFuncs.appLaunchUrl(Uri.parse(url));
            }
          },
        ),
      for (final url in iframeUrls)
        InkWell(
          onTap: () {
            final normalizedUrl =
                url.startsWith("//") ? "https:$url" : url;
            SharedFuncs.appLaunchUrl(Uri.parse(normalizedUrl));
          },
          child: Padding(
            padding: const EdgeInsets.symmetric(vertical: 2),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Icon(Icons.open_in_new,
                    size: 14, color: AppColors.coolGrey700),
                const SizedBox(width: 4),
                Flexible(
                  child: Text(
                    url,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: TextStyle(
                        fontSize: 14,
                        color: AppColors.coolGrey700,
                        decoration: TextDecoration.underline),
                  ),
                ),
              ],
            ),
          ),
        ),
      if (_edited)
        Text(" (${AppLocalizations.of(context)!.edited})",
            style: TextStyle(
                fontSize: 14,
                color: AppColors.navLink,
                fontWeight: FontWeight.w400))
    ]);
  }
}
