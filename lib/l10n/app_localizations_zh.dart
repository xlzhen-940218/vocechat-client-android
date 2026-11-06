// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Chinese (`zh`).
class AppLocalizationsZh extends AppLocalizations {
  AppLocalizationsZh([String locale = 'zh']) : super(locale);

  @override
  String get serverPageWelcomeFirstLine => '欢迎使用';

  @override
  String get serverPageWelcomeSecondLine => 'VoceChat.';

  @override
  String get serverPageHostServer => '服务器 URL';

  @override
  String get serverPageHistory => '历史';

  @override
  String get registerNamingPageWhatsYourName => '您的用户名';

  @override
  String get registerNamingPageWhatsYourNameContent =>
      '您希望别人怎样称呼您？此用户名只会对加入本服务器的用户可见。';

  @override
  String get registerNamingPageInvalidNameFormat => '无效的用户名格式';

  @override
  String get registerNamingPageSignUpFailed => '注册失败';

  @override
  String get registerNamingPageSignUpFailedContent =>
      '注册过程中遇到错误，请稍后再试或联系服务器所有者寻求帮助。';

  @override
  String get chatServerHelperServerConnectionError => '服务端连接错误';

  @override
  String get chatServerHelperServerConnectionErrorContent =>
      'VoceChat 无法获取服务端信息。您可以检查链接格式，如“http”、”https“, 或端口号，或联系服务端拥有者寻求帮助。';

  @override
  String get chatServerHelperServerNotInitialized => '服务器未初始化';

  @override
  String get chatServerHelperServerNotInitializedContent => '请使用网页端完成初始化。';

  @override
  String get inputInvitationLinkPageInput => '输入';

  @override
  String get inputInvitationLinkPageInvitationLink => '邀请链接';

  @override
  String get invalidInvitationLinkWarning => '无效的邀请链接';

  @override
  String get invalidInvitationLinkWarningContent => '请联系您的服务端管理员提供新的邀请链接或帮助。';

  @override
  String get loginPageTitle => '登入';

  @override
  String get loginPageEmail => '邮箱';

  @override
  String get loginPageLogin => '登入';

  @override
  String get loginPageLoggingin => '正在登入';

  @override
  String get loginPageNoAccount => '还没有账号？';

  @override
  String get loginPageSignUp => '注册';

  @override
  String get loginPageOnlyInvitedDes => '只有被邀请的用户可以在此服务器注册。请向服务器成员索要邀请链接。';

  @override
  String get passwordRegisterPageInvalidEmailFormat => '邮箱格式无效';

  @override
  String get passwordRegisterPageInvalidPasswordFormat => '密码格式无效';

  @override
  String get passwordRegisterPagePasswordNotMatch => '密码不相同';

  @override
  String get passwordRegisterPageEmailAlreadyExists => '邮箱已经存在';

  @override
  String get passwordRegisterPageEmailAlreadyExistsContent =>
      '此邮箱已经被注册过，请移步登入页面。';

  @override
  String get chatsDrawerServerAndAccount => '服务器及账户';

  @override
  String get chatsDrawerServerAndAccountPl => '服务器及账户';

  @override
  String get chatsDrawerAddNewAccount => '添加新账户';

  @override
  String get chatsPageSearchHint => '搜索消息及用户';

  @override
  String get contactsPageSearchHint => '搜索用户';

  @override
  String get tabChats => '聊天';

  @override
  String get tabContacts => '联系人';

  @override
  String get tabSettings => '设置';

  @override
  String get titleContacts => '联系人';

  @override
  String get contactDetailPageRemove => '从服务器移除用户';

  @override
  String get chatTextFieldHint => '发消息';

  @override
  String get settingsPageTitle => '设置';

  @override
  String get settingsPageEdit => '编辑';

  @override
  String get settingsPageServer => '服务器资料设置';

  @override
  String get settingsPageServerOverview => '概览';

  @override
  String get settingsPageServerFooter => '请使用网页端管理成员。';

  @override
  String get settingsPageConfig => '配置';

  @override
  String get settingsPageConfigFirebase => 'Firebase';

  @override
  String get settingsPageConfigFooter => '请使用网页端来设置 SMTP、登录方式及第三方App接入。';

  @override
  String get addContact => '添加联系人';

  @override
  String get addContacts => '添加联系人';

  @override
  String get archive => '归档';

  @override
  String get archiveBubbleAnd => '还有';

  @override
  String get archiveBubbleMore => '条...';

  @override
  String get audioMessage => '语音消息';

  @override
  String get autoDeleteMessage => '自动删除消息';

  @override
  String get autoDeleteMessageTitle => '在一段时间后自动删除本会话中由我发出的消息';

  @override
  String get autoDeleteMessageDes => '消息将在客户端和服务端彻底删除。';

  @override
  String get avatar => '头像';

  @override
  String get birthday => '生日';

  @override
  String get block => '屏蔽';

  @override
  String get blockWarning => '用户将从联系人列表中被移除，且双方不能聊天。所有消息和数据都将保留。';

  @override
  String get call => '语音通话';

  @override
  String get cancel => '取消';

  @override
  String get changePassword => '更改密码';

  @override
  String get changePasswordFail => '更改密码失败,请检查旧密码或网络状况。';

  @override
  String get channel => '频道';

  @override
  String get clearLocalData => '清空本地数据';

  @override
  String get clearLocalDataContent => 'VoceChat 将会终止运行。所有本地数据都会被移除。';

  @override
  String get continueStr => '继续';

  @override
  String get copy => '复制';

  @override
  String get copyUrl => '拷贝 URL';

  @override
  String get day => '天';

  @override
  String get days => '天';

  @override
  String get delete => '删除';

  @override
  String get deleteAccount => '删除账户';

  @override
  String get deleteAccountWarning => '您确认要删除账户吗？';

  @override
  String get deleteAccountWarningContent =>
      '您的所有数据都将在本地被删除。您的账户信息会从服务器删除，但已发出的消息仍能被其他成员看到。此操作不可逆。';

  @override
  String get deleteAccountFailWarning => '删除账户失败';

  @override
  String get deleteAccountFailWarningContent => '请稍后重试或联系服务器管理员寻求帮助。';

  @override
  String get deleteChannel => '删除频道';

  @override
  String get deleteChannelWarningDes => '此频道将被删除。所有成员都将被移除，且不再可以看到其中的数据。';

  @override
  String get description => '描述';

  @override
  String get done => '完成';

  @override
  String get download => '下载';

  @override
  String get edit => '编辑';

  @override
  String get edited => '已编辑';

  @override
  String get email => '邮箱';

  @override
  String get emailConflictError => '电子邮件地址和现有账户冲突。';

  @override
  String get error => '错误';

  @override
  String get female => '女';

  @override
  String get fetchingServerInfo => '正在获取服务器信息';

  @override
  String get fetchingServerInfoError => '服务器信息获取失败，请检查服务器URL及网络状况。';

  @override
  String get file => '文件';

  @override
  String get fileUploadError => '文件上传错误';

  @override
  String get fileSizeTooLargeDes => '文件大小超过服务端设置。';

  @override
  String get forward => '转发';

  @override
  String get forwardAsImage => '转发图片';

  @override
  String get forwardAsArchive => '以归档转发';

  @override
  String get forwarded => '被转发';

  @override
  String get forwardTo => '转发到';

  @override
  String get gender => '性别';

  @override
  String get goToSettings => '前往设置';

  @override
  String get hide => '隐藏';

  @override
  String get holdAndSpeak => '按住说话';

  @override
  String get hour => '小时';

  @override
  String get hours => '小时';

  @override
  String get idNumber => 'ID号';

  @override
  String get image => '图片';

  @override
  String get imageBeDeletedDes => '图片可能已被删除。';

  @override
  String get inputInvitationLink => '输入邀请链接';

  @override
  String get invalidLinkFormat => '无效的链接格式';

  @override
  String get invalidLinkFormatContent => '请检查您的链接格式是否正确，或联系服务器拥有者寻求帮助。';

  @override
  String get invalidServerLink => '无效的服务器链接';

  @override
  String get invalidServerLinkContent => '请检查您的服务器链接是否正确，或联系服务器拥有者寻求帮助。';

  @override
  String get invitationLink => '邀请链接';

  @override
  String get invitationLinkError => '邀请链接错误';

  @override
  String get invitationLinkGenerationError =>
      '生成邀请链接时出现问题。\n请检查网络状况或联系服务器所有者获取帮助。';

  @override
  String get invitationLinkUrlNotMatch => '本应用程序已有预设的服务地址，故其他服务器的邀请链接将不被接受。';

  @override
  String get inviteNewUsers => '邀请新用户';

  @override
  String get language => '语言';

  @override
  String get logIn => '登入';

  @override
  String get logInTo => '登入中';

  @override
  String get logOut => '登出';

  @override
  String get logoutWarning => '您确认要登出';

  @override
  String get logoutWarningWithQM => '您确认要登出吗？';

  @override
  String get male => '男';

  @override
  String get markdown => 'Markdown';

  @override
  String get message => '发消息';

  @override
  String get messageClearTitle => '清空消息';

  @override
  String get messageClearDes => '您的服务端管理员清空了消息，您的本地消息也将一并清除。';

  @override
  String get messageWQuantifier => '条消息';

  @override
  String get messagesWQuantifier => '条消息';

  @override
  String get messageDetail => '消息全文';

  @override
  String get messageDeletionFailed => '消息删除失败。';

  @override
  String get messageHasBeenDeleted => '此消息已被删除。';

  @override
  String get members => '成员';

  @override
  String get microphonePermissionRequired => '需要麦克风权限';

  @override
  String get microphonePermissionRequiredDes => '需要麦克风权限以发送语音消息。请前往设置中授权。';

  @override
  String get minute => '分钟';

  @override
  String get minutes => '分钟';

  @override
  String get mute => '静音';

  @override
  String get muteNotification => '静默通知';

  @override
  String get name => '名字';

  @override
  String get networkError => '网络错误';

  @override
  String get networkErrorDes => '请检查您的网络设置。';

  @override
  String get newPassword => '新密码';

  @override
  String get newPrivateChannel => '新私有频道';

  @override
  String get newPublicChannel => '新公有频道';

  @override
  String get next => '下一步';

  @override
  String get nickname => '昵称';

  @override
  String get noContent => '没有内容';

  @override
  String get noQrCodeDetected => '没有检测到二维码，请检查原图或将原图转换成jpg格式尝试。';

  @override
  String get notificationLanguage => '通知语言';

  @override
  String get other => '其他';

  @override
  String get off => '关闭';

  @override
  String get ok => '好';

  @override
  String get oldPassword => '旧密码';

  @override
  String get open => '打开';

  @override
  String get openWithOtherApps => '用其他 App 打开';

  @override
  String get onlyAndAdminCanSendMessage => '只有群主和管理员可以发送消息';

  @override
  String get paste => '粘贴';

  @override
  String get password => '密码';

  @override
  String get confirmPassword => '确认密码';

  @override
  String get permissionRequired => '需要权限';

  @override
  String get permissionRequiredDes => '请前往设置中授权。';

  @override
  String get pin => '钉选';

  @override
  String get pinChat => '置顶会话';

  @override
  String get privateChannelToPublicTitle => '转为公有频道';

  @override
  String get privateChannelToPublicContent =>
      '这将邀请本服务器的所有用户加入此频道。如果您不是管理员，您会失去此频道的所有权。';

  @override
  String get publicChannel => '公有频道';

  @override
  String get publicChannelToPrivateTitle => '转为私有频道';

  @override
  String get publicChannelToPrivateContent =>
      '此频道将不再对新加入此服务器的用户可见。当前的频道成员不会收到影响。';

  @override
  String get qrCode => '二维码';

  @override
  String get registrationSuccess => '注册成功';

  @override
  String get registerEmailSentDes => '一封注册邮件已经发送，请检查您的收件箱。';

  @override
  String get registrationFailed => '注册失败';

  @override
  String get registrationFailedDes => '注册过程中发生错误。请稍后再试或联系服务器管理员寻求协助。';

  @override
  String get releaseToCancel => '松开取消';

  @override
  String get releaseToSend => '松开发送';

  @override
  String get rememberMe => '记住我';

  @override
  String get removeContact => '移除联系人';

  @override
  String get removeContactWarning => '联系人将从列表中被移除，但您仍然可以聊天。所有消息和数据都将保留。';

  @override
  String get removeFromServer => '从服务器移除';

  @override
  String get removeFromServerWarning => '用户将从服务器中被移除。';

  @override
  String get reply => '回复';

  @override
  String get repliedMessageDeleted => '被回复的消息已被删除。';

  @override
  String get replyingTo => '回复给';

  @override
  String get retry => '重试';

  @override
  String get resend => '重发';

  @override
  String get resendNotSupported => '此类型消息不支持重发。';

  @override
  String get resetFcmToken => '重设通知';

  @override
  String get resetFcmTokenFooter =>
      '如果登录过程中出现通知错误提示，您可以在这里尝试重新向服务端注册设备信息。此过程需要连接谷歌服务，请确认您的网络状况。';

  @override
  String get save => '保存';

  @override
  String get savedItems => '保存的消息';

  @override
  String get saveToDownloads => '保存到下载文件夹';

  @override
  String get saveToAlbum => '保存到相册';

  @override
  String get savedSuccessfully => '保存成功';

  @override
  String get saveFailed => '保存失败';

  @override
  String get saving => '保存中...';

  @override
  String get scanQrCode => '扫描二维码';

  @override
  String get scanQrCodeToRegister => '扫描二维码来注册';

  @override
  String get searching => '搜索中';

  @override
  String get second => '秒';

  @override
  String get seconds => '秒';

  @override
  String get select => '选择';

  @override
  String get setNewAvatar => '设置新头像';

  @override
  String get send => '发送';

  @override
  String get sendMessage => '发送消息';

  @override
  String get share => '分享';

  @override
  String get shareInvitationLink => '分享邀请链接';

  @override
  String get shareQrCode => '分享二维码';

  @override
  String get showInFileSystem => '您可前往文件App查看。';

  @override
  String get signIn => '登入';

  @override
  String get signUp => '注册';

  @override
  String get signUpTo => '注册';

  @override
  String get tick => '✓';

  @override
  String get switchServer => '切换服务器';

  @override
  String get tapForMore => '点按查看更多';

  @override
  String get text => '文本';

  @override
  String get to => '至';

  @override
  String get today => '今天';

  @override
  String get transferOwnership => '转让所有权';

  @override
  String get unblock => '解除屏蔽';

  @override
  String get unmute => '取消静音';

  @override
  String get unpin => '取消钉选';

  @override
  String get unsupportedMessageType => '不支持的消息类型';

  @override
  String get uploadError => '上传错误';

  @override
  String get uploadErrorContent => '头像图片大小超过限制。';

  @override
  String get userBlocked => '该用户已被您屏蔽。';

  @override
  String get userEmail => '用户邮箱';

  @override
  String get userId => '用户ID';

  @override
  String get userInfo => '用户信息';

  @override
  String get userName => '用户名';

  @override
  String get userNotContact => '该用户不在您的联系人列表中。';

  @override
  String get userSearchNoResult => '用户不存在，或者不允许被搜索。';

  @override
  String get userSettings => '用户设置';

  @override
  String get version => '版本';

  @override
  String get video => '视频通话';

  @override
  String get viewChatHistory => '查看历史消息';

  @override
  String get voice => '语音通话';

  @override
  String get week => '周';

  @override
  String get weeks => '周';

  @override
  String get yesterday => '昨天';

  @override
  String get you => '你';

  @override
  String get leave => '离开';

  @override
  String get leaveChannel => '离开频道';

  @override
  String get leaveChannelWarningDes => '此频道及其所有消息都将从您的设备中删除。';

  @override
  String get leaveChannelTransferOwnerDes => '作为频道所有者，您需要在离开前转让频道所有权。';

  @override
  String get chatsBarNewChannel => '新频道';

  @override
  String get chatsBarNewDm => '新私聊';

  @override
  String get newChannelPageTitle => '创建频道';

  @override
  String get newChannelPageName => '频道名';

  @override
  String get newChannelPageDes => '描述';

  @override
  String get newChannelPagePrivateChannel => '私有频道';

  @override
  String get newChannelPagePrivateEnableDes => '只有指定用户能查看此频道。';

  @override
  String get newChannelPagePrivateDisableDes => '所有用户都查看此频道。';

  @override
  String get newChannelPagePrivateDes => '只有管理员能创建公共频道。';

  @override
  String get newPrivateChannelSelectTitle => '创建新私有频道';

  @override
  String get newDmPageTitle => '创建新私聊';

  @override
  String get serverInfoSettingTitle => '服务器概览';

  @override
  String get serverInfoSettingNameHint => '服务器名';

  @override
  String get serverInfoSettingDesHint => '服务器描述';

  @override
  String get serverInfoSettingFooter => '以上更新仅会在重新启动 App 或重新登入后可见。';

  @override
  String get aboutPageTitle => '关于 VoceChat';

  @override
  String get aboutPageCheckUpdates => '检查更新';

  @override
  String get aboutPageChangeLog => '更新日志';

  @override
  String get aboutPageUpdateTitleUpToDate => '此版本为最新';

  @override
  String get aboutPageUpdateTitleUpToDateContent => '您使用的是最新版本的 VoceChat。';

  @override
  String get aboutPageCopyRight => '© Privoce Inc. 保留所有权利。';

  @override
  String get firebaseEnable => '启用';

  @override
  String get savedItemEmptyChatDes => '此会话暂无保存的信息。';

  @override
  String get savedItemEmptyChannelDes => '此频道暂无保存的信息。';

  @override
  String get pinnedMsgPageEmptyChannelDes => '此频道暂无钉选的信息。';

  @override
  String get pinnedMsgPageUnPinTitle => '取消钉选';

  @override
  String get pinnedMsgPageUnPinContent => '您确定要取消钉选此消息？';

  @override
  String get pinnedMsgPageUnPinErrorTitle => '取消钉选错误';

  @override
  String get pinnedMsgPageUnPinErrorContent => '在取消钉选的过程中发生了错误。';

  @override
  String get memberAddPageTitle => '添加频道成员';

  @override
  String get memberAddPageAdd => '添加';

  @override
  String get memberAddPageAddMembers => '添加频道成员';

  @override
  String get memberAddPageInvitationLink => '邀请链接';

  @override
  String get memberRemovePageTitle => '移除频道成员';

  @override
  String get memberRemovePageRemove => '移除';

  @override
  String get chatPageSelectionWarning => '语音/被转发/发送失败的消息无法被多选转发或保存。';

  @override
  String get chatPageCantFindFileWarning => '无法找到文件';

  @override
  String get chatPageCantFindFileWarningContent => '文件也许已被删除。请发一条新消息。';

  @override
  String get chatPageCantFindImageWarning => '无法找到图片';

  @override
  String get chatPageCantFindImageWarningContent => '图片也许已被删除。请发一条新消息。';

  @override
  String get chatPageResendWarning => '重发消息';

  @override
  String get chatPageResendWarningContent => '您确认要重发这条消息吗？';

  @override
  String get savedPageDeleteTitle => '删除保存的项目';

  @override
  String get savedPageDeleteContent => '您确定要删除这个保存的项目吗？';

  @override
  String get savedPageDeleteErrorTitle => '删除保存的项目时出错';

  @override
  String get savedPageDeleteErrorContent => '删除保存项目时出现了问题。';

  @override
  String get magicLinkLoginSendError => 'Magic Link 错误';

  @override
  String get magicLinkLoginSendErrorContent =>
      '在发送 Magic Link 到邮箱地址时出错。请稍后再试或向我们报告问题。';

  @override
  String get appQrCodeScanPageInvalidCodeTitle => '无效的二维码';

  @override
  String get appQrCodeScanPageInvalidCodeContent => '请获取一个新的邀请链接二维码，或检查您的网络状况。';

  @override
  String get settingsAboutPageNetworkError => '网络错误';

  @override
  String get settingsAboutPageNetworkErrorContent =>
      'VoceChat 无法获取最新的版本信息。请您稍后再试。';

  @override
  String get chatsBarNetworkError => '网络错误';

  @override
  String get chatsBarNetworkErrorContent => '请检查您的网络状况，或稍后再试。';

  @override
  String get chatsBarLoginStatusExpired => '登入状态过期';

  @override
  String get chatsBarLoginStatusExpiredContent => '请重新登入。';

  @override
  String get filePageCantOpenFile => '无法打开文件';

  @override
  String get filePageCantOpenFileContent => 'VoceChat 不支持此文件类型，请尝试用其他 App 打开。';

  @override
  String get filePageDownloadFailed => '下载失败';

  @override
  String get filePageDownloadFailedContent => 'VoceChat 无法下载此文件。它可能已从服务器删除。';

  @override
  String get filePageCantFindFile => '无法找到文件';

  @override
  String get filePageCantFindFileContent =>
      'VoceChat 无法在本机或服务器找到此文件。它可能已从服务器删除。';

  @override
  String get chatTextFieldCameraMicrophonePermission => 'VoceChat 需要相机和麦克风权限';

  @override
  String get chatTextFieldCameraMicrophonePermissionContent =>
      '授权相机和麦克风权限以发送图片及视频消息。';

  @override
  String get chatTextFieldPhotoPermission => 'VoceChat 需要系统相册访问权限';

  @override
  String get chatTextFieldPhotoPermissionContent => '请到设置 -> VoceChat 开启权限。';

  @override
  String get loginSessionExpires => '登入状态过期';

  @override
  String get loginSessionExpiresContent =>
      '如果您没有在另一个设备登入，请立刻更改密码，或联系服务器管理员寻求帮助。';

  @override
  String get loginError => '登入错误';

  @override
  String get loginErrorContent401 => '账户或密码无效。';

  @override
  String get loginErrorContent403 => '不支持此登录方式。';

  @override
  String get loginErrorContent404 => '用户不存在。';

  @override
  String get loginErrorContent409 => '邮箱地址重复。';

  @override
  String get loginErrorContent423 => '用户被冻结。';

  @override
  String get loginErrorContent451 => '证书存在问题。请联系服务器管理员寻求帮助。';

  @override
  String get loginErrorContentOther => '登录过程中出现问题。';

  @override
  String get loginErrorCopy => '复制错误信息';

  @override
  String get appTextSelectionControlPasteImage => '粘贴并发送图片';

  @override
  String get fcmError => 'FCM 错误';

  @override
  String get fcmErrorContent => 'FCM 配置失败。';

  @override
  String get fcmErrorCopyErrorLog => '拷贝错误记录。';

  @override
  String get invitationLinkViewSend => '向朋友发送邀请链接。';

  @override
  String get noFCMTokenLoginTitle => '推送通知';

  @override
  String get noFCMTokenLoginDes => '登录流程将继续，但推送服务将会不可用。您可检查网络环境（即需要能访问谷歌服务）。';

  @override
  String get noFCMTokenReloadTitle => '推送通知';

  @override
  String get noFCMTokenReloadDes => '注册通知信息时出错，推送服务将会不可用。您可检查网络环境（即需要能访问谷歌服务）。';

  @override
  String get settingsPageMyInfo => '我的信息';

  @override
  String get settingsPageAccountSettings => '账号设置';

  @override
  String get settingsPageAbout => '关于我们';

  @override
  String get settingsPageLogout => '退出登录';
}
