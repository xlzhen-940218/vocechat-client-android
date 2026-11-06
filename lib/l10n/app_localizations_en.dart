// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for English (`en`).
class AppLocalizationsEn extends AppLocalizations {
  AppLocalizationsEn([String locale = 'en']) : super(locale);

  @override
  String get serverPageWelcomeFirstLine => 'Welcome to';

  @override
  String get serverPageWelcomeSecondLine => 'VoceChat.';

  @override
  String get serverPageHostServer => 'Host Server URL';

  @override
  String get serverPageHistory => 'History';

  @override
  String get registerNamingPageWhatsYourName => 'What\'s your name';

  @override
  String get registerNamingPageWhatsYourNameContent =>
      'Enter a name so people know how you\'d like to be called. Your name will only be visible to others in spaces you joined.';

  @override
  String get registerNamingPageInvalidNameFormat => 'Invalid Name Format';

  @override
  String get registerNamingPageSignUpFailed => 'Sign Up Failed';

  @override
  String get registerNamingPageSignUpFailedContent =>
      'Something wrong happened. Please try again later or contact server owner for help.';

  @override
  String get chatServerHelperServerConnectionError => 'Server Connection Error';

  @override
  String get chatServerHelperServerConnectionErrorContent =>
      'VoceChat can\'t retrieve server info. You may check URL format, such as \'https\' and \'http\', or port number, or contact server owner for help.';

  @override
  String get chatServerHelperServerNotInitialized => 'Server Not Initialized';

  @override
  String get chatServerHelperServerNotInitializedContent =>
      'Please use web client for initialization.';

  @override
  String get inputInvitationLinkPageInput => 'Input';

  @override
  String get inputInvitationLinkPageInvitationLink => 'Invitation Link';

  @override
  String get invalidInvitationLinkWarning => 'Invalid Invitation Link';

  @override
  String get invalidInvitationLinkWarningContent =>
      'Please contact server admin for a new link or help.';

  @override
  String get loginPageTitle => 'Login to';

  @override
  String get loginPageEmail => 'Email';

  @override
  String get loginPageLogin => 'Log In';

  @override
  String get loginPageLoggingin => 'Logging In';

  @override
  String get loginPageNoAccount => 'Don\'t have an account?';

  @override
  String get loginPageSignUp => 'Sign Up';

  @override
  String get loginPageOnlyInvitedDes =>
      'Only invited user can sign up to this server. Please seek help from server admin for sign up link.';

  @override
  String get passwordRegisterPageInvalidEmailFormat => 'Invalid Email Format';

  @override
  String get passwordRegisterPageInvalidPasswordFormat =>
      'Invalid Password Format';

  @override
  String get passwordRegisterPagePasswordNotMatch => 'Passwords do not match';

  @override
  String get passwordRegisterPageEmailAlreadyExists => 'Email Already Exists';

  @override
  String get passwordRegisterPageEmailAlreadyExistsContent =>
      'This email has been registered. Please go to sign in page.';

  @override
  String get chatsDrawerServerAndAccount => 'Server and Account';

  @override
  String get chatsDrawerServerAndAccountPl => 'Servers and Accounts';

  @override
  String get chatsDrawerAddNewAccount => 'Add new account';

  @override
  String get chatsPageSearchHint => 'Search for messages or users';

  @override
  String get contactsPageSearchHint => 'Search for users';

  @override
  String get tabChats => 'Chats';

  @override
  String get tabContacts => 'Contacts';

  @override
  String get tabSettings => 'Settings';

  @override
  String get titleContacts => 'Contacts';

  @override
  String get contactDetailPageRemove => 'Remove from server';

  @override
  String get chatTextFieldHint => 'Message';

  @override
  String get settingsPageTitle => 'Settings';

  @override
  String get settingsPageEdit => 'Edit';

  @override
  String get settingsPageServer => 'Server';

  @override
  String get settingsPageServerOverview => 'Overview';

  @override
  String get settingsPageServerFooter =>
      'Please use web client for member settings.';

  @override
  String get settingsPageConfig => 'Configuration';

  @override
  String get settingsPageConfigFirebase => 'Firebase';

  @override
  String get settingsPageConfigFooter =>
      'Please use web client for SMTP, Login Methods and Third-party App Settings.';

  @override
  String get addContact => 'Add Contact';

  @override
  String get addContacts => 'Add Contacts';

  @override
  String get archive => 'Archive';

  @override
  String get archiveBubbleAnd => 'and';

  @override
  String get archiveBubbleMore => 'more...';

  @override
  String get audioMessage => 'Audio Message';

  @override
  String get autoDeleteMessage => 'Auto-delete Messages';

  @override
  String get autoDeleteMessageTitle =>
      'Delete messages sent by myself in this chat after a certain time period.';

  @override
  String get autoDeleteMessageDes =>
      'This will remove the message data stored on both the server and client side.';

  @override
  String get avatar => 'Avatar';

  @override
  String get birthday => 'Birthday';

  @override
  String get block => 'Block';

  @override
  String get blockWarning =>
      'The user will be removed from the contact list and the two parties will not be able to chat. All messages and data will be preserved.';

  @override
  String get call => 'Call';

  @override
  String get cancel => 'Cancel';

  @override
  String get changePassword => 'Change Password';

  @override
  String get changePasswordFail =>
      'Failed to change password. Please check old password or network condition.';

  @override
  String get channel => 'Channel';

  @override
  String get clearLocalData => 'Clear local data';

  @override
  String get clearLocalDataContent =>
      'VoceChat will be terminated. All your data will be deleted locally.';

  @override
  String get continueStr => 'Continue';

  @override
  String get copy => 'Copy';

  @override
  String get copyUrl => 'Copy URL';

  @override
  String get day => 'day';

  @override
  String get days => 'days';

  @override
  String get delete => 'Delete';

  @override
  String get deleteAccount => 'Delete Account';

  @override
  String get deleteAccountWarning => 'Are you sure to delete account?';

  @override
  String get deleteAccountWarningContent =>
      'All your data will be deleted locally. Your account information will be deleted on server, but message contents will still be visible to other users. This can not be undone.';

  @override
  String get deleteAccountFailWarning => 'Account Deletion Failed';

  @override
  String get deleteAccountFailWarningContent =>
      'Please try again later or contact admin for help.';

  @override
  String get deleteChannel => 'Delete Channel';

  @override
  String get deleteChannelWarningDes =>
      'This channel will be dismissed. All members will have no access to its data.';

  @override
  String get description => 'Description';

  @override
  String get done => 'Done';

  @override
  String get download => 'Download';

  @override
  String get edit => 'Edit';

  @override
  String get edited => 'edited';

  @override
  String get email => 'Email';

  @override
  String get emailConflictError => 'Email conflicts with an existing account.';

  @override
  String get error => 'Error';

  @override
  String get female => 'Female';

  @override
  String get fetchingServerInfo => 'Fetching Server Info';

  @override
  String get fetchingServerInfoError =>
      'Failed to obtain server information, please check server url or your network condition.';

  @override
  String get file => 'File';

  @override
  String get fileUploadError => 'File Upload Error';

  @override
  String get fileSizeTooLargeDes => 'File size exceeds server limit.';

  @override
  String get forward => 'Forward';

  @override
  String get forwardAsImage => 'Forward as image';

  @override
  String get forwardAsArchive => 'Forward as archive';

  @override
  String get forwarded => 'forwarded';

  @override
  String get forwardTo => 'Forward to';

  @override
  String get gender => 'Gender';

  @override
  String get goToSettings => 'Go to Settings';

  @override
  String get hide => 'Hide';

  @override
  String get holdAndSpeak => 'Hold and Speak';

  @override
  String get hour => 'hour';

  @override
  String get hours => 'hours';

  @override
  String get idNumber => 'ID Number';

  @override
  String get image => 'Image';

  @override
  String get imageBeDeletedDes => 'Image might have been deleted.';

  @override
  String get inputInvitationLink => 'Input Invitation Link';

  @override
  String get invalidLinkFormat => 'Invalid Link Format';

  @override
  String get invalidLinkFormatContent =>
      'Please check your link format or contact server owner for help.';

  @override
  String get invalidServerLink => 'Invalid Server Link';

  @override
  String get invalidServerLinkContent =>
      'Please check your server link or contact server owner for help.';

  @override
  String get invitationLink => 'Invitation Link';

  @override
  String get invitationLinkError => 'Invitation Link Error';

  @override
  String get invitationLinkGenerationError =>
      'An error occurred when generating invitation link.\nPlease check your network condition or contact server owner for help.';

  @override
  String get invitationLinkUrlNotMatch =>
      'This app has a default server address. Invitation links from other servers will not be accepted.';

  @override
  String get inviteNewUsers => 'Invite New Users';

  @override
  String get language => 'Language';

  @override
  String get logIn => 'Log in';

  @override
  String get logInTo => 'Login To';

  @override
  String get logOut => 'Log Out';

  @override
  String get logoutWarning => 'Are you sure to log out';

  @override
  String get logoutWarningWithQM => 'Are you sure to log out?';

  @override
  String get male => 'Male';

  @override
  String get markdown => 'Markdown';

  @override
  String get message => 'Message';

  @override
  String get messageClearTitle => 'Clear Messages';

  @override
  String get messageClearDes =>
      'Your server admin cleared all messages in server. Your local messages will be cleared as well.';

  @override
  String get messageWQuantifier => 'Message';

  @override
  String get messagesWQuantifier => 'Messages';

  @override
  String get messageDetail => 'Message Detail';

  @override
  String get messageDeletionFailed => 'Message deletion failed';

  @override
  String get messageHasBeenDeleted => 'This message has been deleted.';

  @override
  String get members => 'Members';

  @override
  String get microphonePermissionRequired => 'Microphone Permission Required';

  @override
  String get microphonePermissionRequiredDes =>
      'Microphone permission is required to record voice messages. Please go to Settings to grant permission.';

  @override
  String get minute => 'minute';

  @override
  String get minutes => 'minutes';

  @override
  String get mute => 'Mute';

  @override
  String get muteNotification => 'Mute Notification';

  @override
  String get name => 'Name';

  @override
  String get networkError => 'Network Error';

  @override
  String get networkErrorDes => 'Please check your network settings.';

  @override
  String get newPassword => 'New Password';

  @override
  String get newPrivateChannel => 'New Private Channel';

  @override
  String get newPublicChannel => 'New Public Channel';

  @override
  String get next => 'Next';

  @override
  String get nickname => 'Nick name';

  @override
  String get noContent => 'No Content';

  @override
  String get noQrCodeDetected =>
      'No QR Code Detected. Please check original image, or convert it to .jpg format.';

  @override
  String get notificationLanguage => 'Notification Language';

  @override
  String get other => 'Other';

  @override
  String get off => 'Off';

  @override
  String get ok => 'OK';

  @override
  String get oldPassword => 'Old Password';

  @override
  String get open => 'Open';

  @override
  String get openWithOtherApps => 'Open with other Apps';

  @override
  String get onlyAndAdminCanSendMessage =>
      'Only owner and admin can send message';

  @override
  String get paste => 'Paste';

  @override
  String get password => 'Password';

  @override
  String get confirmPassword => 'Confirm Password';

  @override
  String get permissionRequired => 'Permission Required';

  @override
  String get permissionRequiredDes =>
      'Please go to Settings to grant permission.';

  @override
  String get pin => 'Pin';

  @override
  String get pinChat => 'Pin Chat';

  @override
  String get privateChannelToPublicTitle => 'Change to Public Channel';

  @override
  String get privateChannelToPublicContent =>
      'It will invite all members in this server to this channel. If you are not an admin, you will lose ownership of this channel.';

  @override
  String get publicChannel => 'Public Channel';

  @override
  String get publicChannelToPrivateTitle => 'Change to Private Channel';

  @override
  String get publicChannelToPrivateContent =>
      'This channel will not be visible to new server members. Current members will not be affected.';

  @override
  String get qrCode => 'QR Code';

  @override
  String get registrationSuccess => 'Registration Success';

  @override
  String get registerEmailSentDes =>
      'A registration email has been sent, please check your email inbox.';

  @override
  String get registrationFailed => 'Registration Failed';

  @override
  String get registrationFailedDes =>
      'An error occurred during registration. Please try again later or contact server admins for help.';

  @override
  String get releaseToCancel => 'Release to cancel';

  @override
  String get releaseToSend => 'Release to send';

  @override
  String get rememberMe => 'Remember me';

  @override
  String get removeContact => 'Remove Contact';

  @override
  String get removeContactWarning =>
      'The contact will be removed from Contact List but you can still chat. All messages and data will be preserved.';

  @override
  String get removeFromServer => 'Remove from server';

  @override
  String get removeFromServerWarning =>
      'The user will be removed from the server.';

  @override
  String get reply => 'Reply';

  @override
  String get repliedMessageDeleted => 'The replied message has been deleted.';

  @override
  String get replyingTo => 'Replying to';

  @override
  String get retry => 'Retry';

  @override
  String get resend => 'Resend';

  @override
  String get resendNotSupported =>
      'Resending this type of message is not supported.';

  @override
  String get resetFcmToken => 'Reset Push Notification';

  @override
  String get resetFcmTokenFooter =>
      'You may re-register Firebase device token for Push Notifications, if a push notification error occurred during login process. Please check your network condition, as it needs to connect Google service.';

  @override
  String get save => 'Save';

  @override
  String get savedItems => 'Saved Items';

  @override
  String get saveToDownloads => 'Save to Downloads Folder';

  @override
  String get saveToAlbum => 'Save to Photo Album';

  @override
  String get savedSuccessfully => 'File has been saved successfully';

  @override
  String get saveFailed => 'File save failed';

  @override
  String get saving => 'Saving...';

  @override
  String get scanQrCode => 'Scan QR Code';

  @override
  String get scanQrCodeToRegister => 'Scan this QR Code to register';

  @override
  String get searching => 'Searching';

  @override
  String get second => 'second';

  @override
  String get seconds => 'seconds';

  @override
  String get select => 'Select';

  @override
  String get setNewAvatar => 'Set a new avatar';

  @override
  String get send => 'Send';

  @override
  String get sendMessage => 'Send Message';

  @override
  String get share => 'Share';

  @override
  String get shareInvitationLink => 'Share Invitation Link';

  @override
  String get shareQrCode => 'Share QR Code';

  @override
  String get showInFileSystem => 'You may go to Files App to view this file.';

  @override
  String get signIn => 'Sign In';

  @override
  String get signUp => 'Sign Up';

  @override
  String get signUpTo => 'Sign Up to';

  @override
  String get tick => '✓';

  @override
  String get switchServer => 'Switch Server';

  @override
  String get tapForMore => 'Tap for more';

  @override
  String get text => 'Text';

  @override
  String get to => 'to';

  @override
  String get today => 'Today';

  @override
  String get transferOwnership => 'Transfer Ownership';

  @override
  String get unblock => 'Unblock';

  @override
  String get unmute => 'Unmute';

  @override
  String get unpin => 'Unpin';

  @override
  String get unsupportedMessageType => 'Unsupported Message Type';

  @override
  String get uploadError => 'Upload Error';

  @override
  String get uploadErrorContent => 'Avatar size exceeds limit.';

  @override
  String get userBlocked => 'This user has been blocked by you.';

  @override
  String get userEmail => 'User Email';

  @override
  String get userId => 'User ID';

  @override
  String get userInfo => 'User Info';

  @override
  String get userName => 'User Name';

  @override
  String get userNotContact => 'This user is not in your contact list.';

  @override
  String get userSearchNoResult =>
      'User does not exist, or is not allowed to be searched.';

  @override
  String get userSettings => 'User Settings';

  @override
  String get version => 'Version';

  @override
  String get video => 'Video';

  @override
  String get viewChatHistory => 'View Chat History';

  @override
  String get voice => 'Voice';

  @override
  String get week => 'week';

  @override
  String get weeks => 'weeks';

  @override
  String get yesterday => 'yesterday';

  @override
  String get you => 'You';

  @override
  String get leave => 'Leave';

  @override
  String get leaveChannel => 'Leave Channel';

  @override
  String get leaveChannelWarningDes =>
      'This channel and its messages will be deleted from all your devices.';

  @override
  String get leaveChannelTransferOwnerDes =>
      'As Owner, you have to transfer ownership before leaving.';

  @override
  String get chatsBarNewChannel => 'New Channel';

  @override
  String get chatsBarNewDm => 'New Direct Message';

  @override
  String get newChannelPageTitle => 'Create Channel';

  @override
  String get newChannelPageName => 'Channel Name';

  @override
  String get newChannelPageDes => 'Description';

  @override
  String get newChannelPagePrivateChannel => 'Private Channel';

  @override
  String get newChannelPagePrivateEnableDes =>
      'Only selected members will be able to view this channel.';

  @override
  String get newChannelPagePrivateDisableDes =>
      'All members in this server will be able to view this channel.';

  @override
  String get newChannelPagePrivateDes =>
      'Only admins are allowed to create public channels.';

  @override
  String get newPrivateChannelSelectTitle => 'Create Private Channel';

  @override
  String get newDmPageTitle => 'New Direct Message';

  @override
  String get serverInfoSettingTitle => 'Server Overview';

  @override
  String get serverInfoSettingNameHint => 'Server Name';

  @override
  String get serverInfoSettingDesHint => 'Server Description';

  @override
  String get serverInfoSettingFooter =>
      'Above updates will only be seen after reopening App or logging back in.';

  @override
  String get aboutPageTitle => 'About VoceChat';

  @override
  String get aboutPageCheckUpdates => 'Check Updates';

  @override
  String get aboutPageChangeLog => 'changelog';

  @override
  String get aboutPageUpdateTitleUpToDate => 'Current Version is Up-to-date';

  @override
  String get aboutPageUpdateTitleUpToDateContent =>
      'You are using the latest VoceChat App.';

  @override
  String get aboutPageCopyRight => '© Privoce Inc. All Rights Reserved.';

  @override
  String get firebaseEnable => 'Enable';

  @override
  String get savedItemEmptyChatDes => 'This chat doesn\'t have any saved item.';

  @override
  String get savedItemEmptyChannelDes =>
      'This channel doesn\'t have any saved item.';

  @override
  String get pinnedMsgPageEmptyChannelDes =>
      'This channel doesn\'t have any pinned message.';

  @override
  String get pinnedMsgPageUnPinTitle => 'Unpin Message';

  @override
  String get pinnedMsgPageUnPinContent => 'Are you sure to unpin this message?';

  @override
  String get pinnedMsgPageUnPinErrorTitle => 'Unpin Message Error';

  @override
  String get pinnedMsgPageUnPinErrorContent =>
      'Something wrong when unpinning this pinned message.';

  @override
  String get memberAddPageTitle => 'Add Members';

  @override
  String get memberAddPageAdd => 'Add';

  @override
  String get memberAddPageAddMembers => 'Add Members';

  @override
  String get memberAddPageInvitationLink => 'Invitation Link';

  @override
  String get memberRemovePageTitle => 'Remove Members';

  @override
  String get memberRemovePageRemove => 'Remove';

  @override
  String get chatPageSelectionWarning =>
      'Can\'t forward or save an Audio/forwarded/unsuccessful message in selection mode.';

  @override
  String get chatPageCantFindFileWarning => 'Can\'t Find File';

  @override
  String get chatPageCantFindFileWarningContent =>
      'File might have been deleted. Please send a new message.';

  @override
  String get chatPageCantFindImageWarning => 'Can\'t Find Image File';

  @override
  String get chatPageCantFindImageWarningContent =>
      'Image might have been deleted. Please send a new message.';

  @override
  String get chatPageResendWarning => 'Resend Message';

  @override
  String get chatPageResendWarningContent =>
      'Do you want to resend this message?';

  @override
  String get savedPageDeleteTitle => 'Delete Saved Item';

  @override
  String get savedPageDeleteContent =>
      'Are you sure to delete this saved item?';

  @override
  String get savedPageDeleteErrorTitle => 'Delete Saved Item Error';

  @override
  String get savedPageDeleteErrorContent =>
      'Something wrong with saved item deletion.';

  @override
  String get magicLinkLoginSendError => 'Magic Link Error';

  @override
  String get magicLinkLoginSendErrorContent =>
      'There was an error when sending magic link to the provided email. Please try again or report an issue to us.';

  @override
  String get appQrCodeScanPageInvalidCodeTitle => 'Invalid QR Code';

  @override
  String get appQrCodeScanPageInvalidCodeContent =>
      'Please ask for a new invitation link, or check your network.';

  @override
  String get settingsAboutPageNetworkError => 'Network Error';

  @override
  String get settingsAboutPageNetworkErrorContent =>
      'VoceChat cannot fetch latest version information. You may try it later.';

  @override
  String get chatsBarNetworkError => 'Network Error';

  @override
  String get chatsBarNetworkErrorContent =>
      'Please check your network settings, or try to log in again.';

  @override
  String get chatsBarLoginStatusExpired => 'Login Status Expired';

  @override
  String get chatsBarLoginStatusExpiredContent => 'Please login again.';

  @override
  String get filePageCantOpenFile => 'Can\'t Open File';

  @override
  String get filePageCantOpenFileContent =>
      'VoceChat does not support this file type. Please open with other Apps.';

  @override
  String get filePageDownloadFailed => 'Download Failed';

  @override
  String get filePageDownloadFailedContent =>
      'VoceChat can\'t download this file. It might have been deleted from server.';

  @override
  String get filePageCantFindFile => 'Can\'t Open File';

  @override
  String get filePageCantFindFileContent =>
      'VoceChat can\'t find this file on your device or from the server. It might have been deleted.';

  @override
  String get chatTextFieldCameraMicrophonePermission =>
      'VoceChat needs camera and microphone permissions';

  @override
  String get chatTextFieldCameraMicrophonePermissionContent =>
      'Grant camera and microphone permissions for sending image and video messages.';

  @override
  String get chatTextFieldPhotoPermission =>
      'VoceChat needs Photo Album permission';

  @override
  String get chatTextFieldPhotoPermissionContent =>
      'Please go to settings -> VoceChat for permission settings.';

  @override
  String get loginSessionExpires => 'Login Session Expires';

  @override
  String get loginSessionExpiresContent =>
      'If you didn\'t login from another device, please change password immediately, or contact server admins for help.';

  @override
  String get loginError => 'Login Error';

  @override
  String get loginErrorContent401 => 'Invalid account or password.';

  @override
  String get loginErrorContent403 => 'Login method is not supported.';

  @override
  String get loginErrorContent404 => 'User does not exist.';

  @override
  String get loginErrorContent409 => 'Email collision.';

  @override
  String get loginErrorContent423 => 'User has been frozen.';

  @override
  String get loginErrorContent451 =>
      'License has an issue. Please contact server admin for help.';

  @override
  String get loginErrorContentOther => 'An error occurred during login.';

  @override
  String get loginErrorCopy => 'Copy Error';

  @override
  String get appTextSelectionControlPasteImage => 'Paste and Send Image';

  @override
  String get fcmError => 'FCM Error';

  @override
  String get fcmErrorContent => 'FCM Configuration Failed.';

  @override
  String get fcmErrorCopyErrorLog => 'Copy Error Log';

  @override
  String get invitationLinkViewSend => 'Send invitation link to your friends.';

  @override
  String get noFCMTokenLoginTitle => 'Push Notification';

  @override
  String get noFCMTokenLoginDes =>
      'Login process will continue, but Push notification service will not be available. You may check your network condition (VoceChat needs to visit Google service).';

  @override
  String get noFCMTokenReloadTitle => 'Push Notification';

  @override
  String get noFCMTokenReloadDes =>
      'Failed to register device info. Push notification service will not be available. You may check your network condition (VoceChat needs to visit Google service).';

  @override
  String get settingsPageMyInfo => 'My Info';

  @override
  String get settingsPageAccountSettings => 'Account Settings';

  @override
  String get settingsPageAbout => 'About';

  @override
  String get settingsPageLogout => 'Logout';
}
