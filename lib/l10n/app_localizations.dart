import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:intl/intl.dart' as intl;

import 'app_localizations_en.dart';
import 'app_localizations_zh.dart';

// ignore_for_file: type=lint

/// Callers can lookup localized strings with an instance of AppLocalizations
/// returned by `AppLocalizations.of(context)`.
///
/// Applications need to include `AppLocalizations.delegate()` in their app's
/// `localizationDelegates` list, and the locales they support in the app's
/// `supportedLocales` list. For example:
///
/// ```dart
/// import 'l10n/app_localizations.dart';
///
/// return MaterialApp(
///   localizationsDelegates: AppLocalizations.localizationsDelegates,
///   supportedLocales: AppLocalizations.supportedLocales,
///   home: MyApplicationHome(),
/// );
/// ```
///
/// ## Update pubspec.yaml
///
/// Please make sure to update your pubspec.yaml to include the following
/// packages:
///
/// ```yaml
/// dependencies:
///   # Internationalization support.
///   flutter_localizations:
///     sdk: flutter
///   intl: any # Use the pinned version from flutter_localizations
///
///   # Rest of dependencies
/// ```
///
/// ## iOS Applications
///
/// iOS applications define key application metadata, including supported
/// locales, in an Info.plist file that is built into the application bundle.
/// To configure the locales supported by your app, you’ll need to edit this
/// file.
///
/// First, open your project’s ios/Runner.xcworkspace Xcode workspace file.
/// Then, in the Project Navigator, open the Info.plist file under the Runner
/// project’s Runner folder.
///
/// Next, select the Information Property List item, select Add Item from the
/// Editor menu, then select Localizations from the pop-up menu.
///
/// Select and expand the newly-created Localizations item then, for each
/// locale your application supports, add a new item and select the locale
/// you wish to add from the pop-up menu in the Value field. This list should
/// be consistent with the languages listed in the AppLocalizations.supportedLocales
/// property.
abstract class AppLocalizations {
  AppLocalizations(String locale)
    : localeName = intl.Intl.canonicalizedLocale(locale.toString());

  final String localeName;

  static AppLocalizations? of(BuildContext context) {
    return Localizations.of<AppLocalizations>(context, AppLocalizations);
  }

  static const LocalizationsDelegate<AppLocalizations> delegate =
      _AppLocalizationsDelegate();

  /// A list of this localizations delegate along with the default localizations
  /// delegates.
  ///
  /// Returns a list of localizations delegates containing this delegate along with
  /// GlobalMaterialLocalizations.delegate, GlobalCupertinoLocalizations.delegate,
  /// and GlobalWidgetsLocalizations.delegate.
  ///
  /// Additional delegates can be added by appending to this list in
  /// MaterialApp. This list does not have to be used at all if a custom list
  /// of delegates is preferred or required.
  static const List<LocalizationsDelegate<dynamic>> localizationsDelegates =
      <LocalizationsDelegate<dynamic>>[
        delegate,
        GlobalMaterialLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
      ];

  /// A list of this localizations delegate's supported locales.
  static const List<Locale> supportedLocales = <Locale>[
    Locale('en'),
    Locale('zh'),
  ];

  /// No description provided for @serverPageWelcomeFirstLine.
  ///
  /// In en, this message translates to:
  /// **'Welcome to'**
  String get serverPageWelcomeFirstLine;

  /// No description provided for @serverPageWelcomeSecondLine.
  ///
  /// In en, this message translates to:
  /// **'VoceChat.'**
  String get serverPageWelcomeSecondLine;

  /// No description provided for @serverPageHostServer.
  ///
  /// In en, this message translates to:
  /// **'Host Server URL'**
  String get serverPageHostServer;

  /// No description provided for @serverPageHistory.
  ///
  /// In en, this message translates to:
  /// **'History'**
  String get serverPageHistory;

  /// No description provided for @registerNamingPageWhatsYourName.
  ///
  /// In en, this message translates to:
  /// **'What\'s your name'**
  String get registerNamingPageWhatsYourName;

  /// No description provided for @registerNamingPageWhatsYourNameContent.
  ///
  /// In en, this message translates to:
  /// **'Enter a name so people know how you\'d like to be called. Your name will only be visible to others in spaces you joined.'**
  String get registerNamingPageWhatsYourNameContent;

  /// No description provided for @registerNamingPageInvalidNameFormat.
  ///
  /// In en, this message translates to:
  /// **'Invalid Name Format'**
  String get registerNamingPageInvalidNameFormat;

  /// No description provided for @registerNamingPageSignUpFailed.
  ///
  /// In en, this message translates to:
  /// **'Sign Up Failed'**
  String get registerNamingPageSignUpFailed;

  /// No description provided for @registerNamingPageSignUpFailedContent.
  ///
  /// In en, this message translates to:
  /// **'Something wrong happened. Please try again later or contact server owner for help.'**
  String get registerNamingPageSignUpFailedContent;

  /// No description provided for @chatServerHelperServerConnectionError.
  ///
  /// In en, this message translates to:
  /// **'Server Connection Error'**
  String get chatServerHelperServerConnectionError;

  /// No description provided for @chatServerHelperServerConnectionErrorContent.
  ///
  /// In en, this message translates to:
  /// **'VoceChat can\'t retrieve server info. You may check URL format, such as \'https\' and \'http\', or port number, or contact server owner for help.'**
  String get chatServerHelperServerConnectionErrorContent;

  /// No description provided for @chatServerHelperServerNotInitialized.
  ///
  /// In en, this message translates to:
  /// **'Server Not Initialized'**
  String get chatServerHelperServerNotInitialized;

  /// No description provided for @chatServerHelperServerNotInitializedContent.
  ///
  /// In en, this message translates to:
  /// **'Please use web client for initialization.'**
  String get chatServerHelperServerNotInitializedContent;

  /// No description provided for @inputInvitationLinkPageInput.
  ///
  /// In en, this message translates to:
  /// **'Input'**
  String get inputInvitationLinkPageInput;

  /// No description provided for @inputInvitationLinkPageInvitationLink.
  ///
  /// In en, this message translates to:
  /// **'Invitation Link'**
  String get inputInvitationLinkPageInvitationLink;

  /// No description provided for @invalidInvitationLinkWarning.
  ///
  /// In en, this message translates to:
  /// **'Invalid Invitation Link'**
  String get invalidInvitationLinkWarning;

  /// No description provided for @invalidInvitationLinkWarningContent.
  ///
  /// In en, this message translates to:
  /// **'Please contact server admin for a new link or help.'**
  String get invalidInvitationLinkWarningContent;

  /// No description provided for @loginPageTitle.
  ///
  /// In en, this message translates to:
  /// **'Login to'**
  String get loginPageTitle;

  /// No description provided for @loginPageEmail.
  ///
  /// In en, this message translates to:
  /// **'Email'**
  String get loginPageEmail;

  /// No description provided for @loginPageLogin.
  ///
  /// In en, this message translates to:
  /// **'Log In'**
  String get loginPageLogin;

  /// No description provided for @loginPageLoggingin.
  ///
  /// In en, this message translates to:
  /// **'Logging In'**
  String get loginPageLoggingin;

  /// No description provided for @loginPageNoAccount.
  ///
  /// In en, this message translates to:
  /// **'Don\'t have an account?'**
  String get loginPageNoAccount;

  /// No description provided for @loginPageSignUp.
  ///
  /// In en, this message translates to:
  /// **'Sign Up'**
  String get loginPageSignUp;

  /// No description provided for @loginPageOnlyInvitedDes.
  ///
  /// In en, this message translates to:
  /// **'Only invited user can sign up to this server. Please seek help from server admin for sign up link.'**
  String get loginPageOnlyInvitedDes;

  /// No description provided for @passwordRegisterPageInvalidEmailFormat.
  ///
  /// In en, this message translates to:
  /// **'Invalid Email Format'**
  String get passwordRegisterPageInvalidEmailFormat;

  /// No description provided for @passwordRegisterPageInvalidPasswordFormat.
  ///
  /// In en, this message translates to:
  /// **'Invalid Password Format'**
  String get passwordRegisterPageInvalidPasswordFormat;

  /// No description provided for @passwordRegisterPagePasswordNotMatch.
  ///
  /// In en, this message translates to:
  /// **'Passwords do not match'**
  String get passwordRegisterPagePasswordNotMatch;

  /// No description provided for @passwordRegisterPageEmailAlreadyExists.
  ///
  /// In en, this message translates to:
  /// **'Email Already Exists'**
  String get passwordRegisterPageEmailAlreadyExists;

  /// No description provided for @passwordRegisterPageEmailAlreadyExistsContent.
  ///
  /// In en, this message translates to:
  /// **'This email has been registered. Please go to sign in page.'**
  String get passwordRegisterPageEmailAlreadyExistsContent;

  /// No description provided for @chatsDrawerServerAndAccount.
  ///
  /// In en, this message translates to:
  /// **'Server and Account'**
  String get chatsDrawerServerAndAccount;

  /// No description provided for @chatsDrawerServerAndAccountPl.
  ///
  /// In en, this message translates to:
  /// **'Servers and Accounts'**
  String get chatsDrawerServerAndAccountPl;

  /// No description provided for @chatsDrawerAddNewAccount.
  ///
  /// In en, this message translates to:
  /// **'Add new account'**
  String get chatsDrawerAddNewAccount;

  /// No description provided for @chatsPageSearchHint.
  ///
  /// In en, this message translates to:
  /// **'Search for messages or users'**
  String get chatsPageSearchHint;

  /// No description provided for @contactsPageSearchHint.
  ///
  /// In en, this message translates to:
  /// **'Search for users'**
  String get contactsPageSearchHint;

  /// No description provided for @tabChats.
  ///
  /// In en, this message translates to:
  /// **'Chats'**
  String get tabChats;

  /// No description provided for @tabContacts.
  ///
  /// In en, this message translates to:
  /// **'Contacts'**
  String get tabContacts;

  /// No description provided for @tabSettings.
  ///
  /// In en, this message translates to:
  /// **'Settings'**
  String get tabSettings;

  /// No description provided for @titleContacts.
  ///
  /// In en, this message translates to:
  /// **'Contacts'**
  String get titleContacts;

  /// No description provided for @contactDetailPageRemove.
  ///
  /// In en, this message translates to:
  /// **'Remove from server'**
  String get contactDetailPageRemove;

  /// No description provided for @chatTextFieldHint.
  ///
  /// In en, this message translates to:
  /// **'Message'**
  String get chatTextFieldHint;

  /// No description provided for @settingsPageTitle.
  ///
  /// In en, this message translates to:
  /// **'Settings'**
  String get settingsPageTitle;

  /// No description provided for @settingsPageEdit.
  ///
  /// In en, this message translates to:
  /// **'Edit'**
  String get settingsPageEdit;

  /// No description provided for @settingsPageServer.
  ///
  /// In en, this message translates to:
  /// **'Server'**
  String get settingsPageServer;

  /// No description provided for @settingsPageServerOverview.
  ///
  /// In en, this message translates to:
  /// **'Overview'**
  String get settingsPageServerOverview;

  /// No description provided for @settingsPageServerFooter.
  ///
  /// In en, this message translates to:
  /// **'Please use web client for member settings.'**
  String get settingsPageServerFooter;

  /// No description provided for @settingsPageConfig.
  ///
  /// In en, this message translates to:
  /// **'Configuration'**
  String get settingsPageConfig;

  /// No description provided for @settingsPageConfigFirebase.
  ///
  /// In en, this message translates to:
  /// **'Firebase'**
  String get settingsPageConfigFirebase;

  /// No description provided for @settingsPageConfigFooter.
  ///
  /// In en, this message translates to:
  /// **'Please use web client for SMTP, Login Methods and Third-party App Settings.'**
  String get settingsPageConfigFooter;

  /// No description provided for @addContact.
  ///
  /// In en, this message translates to:
  /// **'Add Contact'**
  String get addContact;

  /// No description provided for @addContacts.
  ///
  /// In en, this message translates to:
  /// **'Add Contacts'**
  String get addContacts;

  /// No description provided for @archive.
  ///
  /// In en, this message translates to:
  /// **'Archive'**
  String get archive;

  /// No description provided for @archiveBubbleAnd.
  ///
  /// In en, this message translates to:
  /// **'and'**
  String get archiveBubbleAnd;

  /// No description provided for @archiveBubbleMore.
  ///
  /// In en, this message translates to:
  /// **'more...'**
  String get archiveBubbleMore;

  /// No description provided for @audioMessage.
  ///
  /// In en, this message translates to:
  /// **'Audio Message'**
  String get audioMessage;

  /// No description provided for @autoDeleteMessage.
  ///
  /// In en, this message translates to:
  /// **'Auto-delete Messages'**
  String get autoDeleteMessage;

  /// No description provided for @autoDeleteMessageTitle.
  ///
  /// In en, this message translates to:
  /// **'Delete messages sent by myself in this chat after a certain time period.'**
  String get autoDeleteMessageTitle;

  /// No description provided for @autoDeleteMessageDes.
  ///
  /// In en, this message translates to:
  /// **'This will remove the message data stored on both the server and client side.'**
  String get autoDeleteMessageDes;

  /// No description provided for @avatar.
  ///
  /// In en, this message translates to:
  /// **'Avatar'**
  String get avatar;

  /// No description provided for @birthday.
  ///
  /// In en, this message translates to:
  /// **'Birthday'**
  String get birthday;

  /// No description provided for @block.
  ///
  /// In en, this message translates to:
  /// **'Block'**
  String get block;

  /// No description provided for @blockWarning.
  ///
  /// In en, this message translates to:
  /// **'The user will be removed from the contact list and the two parties will not be able to chat. All messages and data will be preserved.'**
  String get blockWarning;

  /// No description provided for @call.
  ///
  /// In en, this message translates to:
  /// **'Call'**
  String get call;

  /// No description provided for @cancel.
  ///
  /// In en, this message translates to:
  /// **'Cancel'**
  String get cancel;

  /// No description provided for @changePassword.
  ///
  /// In en, this message translates to:
  /// **'Change Password'**
  String get changePassword;

  /// No description provided for @changePasswordFail.
  ///
  /// In en, this message translates to:
  /// **'Failed to change password. Please check old password or network condition.'**
  String get changePasswordFail;

  /// No description provided for @channel.
  ///
  /// In en, this message translates to:
  /// **'Channel'**
  String get channel;

  /// No description provided for @clearLocalData.
  ///
  /// In en, this message translates to:
  /// **'Clear local data'**
  String get clearLocalData;

  /// No description provided for @clearLocalDataContent.
  ///
  /// In en, this message translates to:
  /// **'VoceChat will be terminated. All your data will be deleted locally.'**
  String get clearLocalDataContent;

  /// No description provided for @continueStr.
  ///
  /// In en, this message translates to:
  /// **'Continue'**
  String get continueStr;

  /// No description provided for @copy.
  ///
  /// In en, this message translates to:
  /// **'Copy'**
  String get copy;

  /// No description provided for @copyUrl.
  ///
  /// In en, this message translates to:
  /// **'Copy URL'**
  String get copyUrl;

  /// No description provided for @day.
  ///
  /// In en, this message translates to:
  /// **'day'**
  String get day;

  /// No description provided for @days.
  ///
  /// In en, this message translates to:
  /// **'days'**
  String get days;

  /// No description provided for @delete.
  ///
  /// In en, this message translates to:
  /// **'Delete'**
  String get delete;

  /// No description provided for @deleteAccount.
  ///
  /// In en, this message translates to:
  /// **'Delete Account'**
  String get deleteAccount;

  /// No description provided for @deleteAccountWarning.
  ///
  /// In en, this message translates to:
  /// **'Are you sure to delete account?'**
  String get deleteAccountWarning;

  /// No description provided for @deleteAccountWarningContent.
  ///
  /// In en, this message translates to:
  /// **'All your data will be deleted locally. Your account information will be deleted on server, but message contents will still be visible to other users. This can not be undone.'**
  String get deleteAccountWarningContent;

  /// No description provided for @deleteAccountFailWarning.
  ///
  /// In en, this message translates to:
  /// **'Account Deletion Failed'**
  String get deleteAccountFailWarning;

  /// No description provided for @deleteAccountFailWarningContent.
  ///
  /// In en, this message translates to:
  /// **'Please try again later or contact admin for help.'**
  String get deleteAccountFailWarningContent;

  /// No description provided for @deleteChannel.
  ///
  /// In en, this message translates to:
  /// **'Delete Channel'**
  String get deleteChannel;

  /// No description provided for @deleteChannelWarningDes.
  ///
  /// In en, this message translates to:
  /// **'This channel will be dismissed. All members will have no access to its data.'**
  String get deleteChannelWarningDes;

  /// No description provided for @description.
  ///
  /// In en, this message translates to:
  /// **'Description'**
  String get description;

  /// No description provided for @done.
  ///
  /// In en, this message translates to:
  /// **'Done'**
  String get done;

  /// No description provided for @download.
  ///
  /// In en, this message translates to:
  /// **'Download'**
  String get download;

  /// No description provided for @edit.
  ///
  /// In en, this message translates to:
  /// **'Edit'**
  String get edit;

  /// No description provided for @edited.
  ///
  /// In en, this message translates to:
  /// **'edited'**
  String get edited;

  /// No description provided for @email.
  ///
  /// In en, this message translates to:
  /// **'Email'**
  String get email;

  /// No description provided for @emailConflictError.
  ///
  /// In en, this message translates to:
  /// **'Email conflicts with an existing account.'**
  String get emailConflictError;

  /// No description provided for @error.
  ///
  /// In en, this message translates to:
  /// **'Error'**
  String get error;

  /// No description provided for @female.
  ///
  /// In en, this message translates to:
  /// **'Female'**
  String get female;

  /// No description provided for @fetchingServerInfo.
  ///
  /// In en, this message translates to:
  /// **'Fetching Server Info'**
  String get fetchingServerInfo;

  /// No description provided for @fetchingServerInfoError.
  ///
  /// In en, this message translates to:
  /// **'Failed to obtain server information, please check server url or your network condition.'**
  String get fetchingServerInfoError;

  /// No description provided for @file.
  ///
  /// In en, this message translates to:
  /// **'File'**
  String get file;

  /// No description provided for @fileUploadError.
  ///
  /// In en, this message translates to:
  /// **'File Upload Error'**
  String get fileUploadError;

  /// No description provided for @fileSizeTooLargeDes.
  ///
  /// In en, this message translates to:
  /// **'File size exceeds server limit.'**
  String get fileSizeTooLargeDes;

  /// No description provided for @forward.
  ///
  /// In en, this message translates to:
  /// **'Forward'**
  String get forward;

  /// No description provided for @forwardAsImage.
  ///
  /// In en, this message translates to:
  /// **'Forward as image'**
  String get forwardAsImage;

  /// No description provided for @forwardAsArchive.
  ///
  /// In en, this message translates to:
  /// **'Forward as archive'**
  String get forwardAsArchive;

  /// No description provided for @forwarded.
  ///
  /// In en, this message translates to:
  /// **'forwarded'**
  String get forwarded;

  /// No description provided for @forwardTo.
  ///
  /// In en, this message translates to:
  /// **'Forward to'**
  String get forwardTo;

  /// No description provided for @gender.
  ///
  /// In en, this message translates to:
  /// **'Gender'**
  String get gender;

  /// No description provided for @goToSettings.
  ///
  /// In en, this message translates to:
  /// **'Go to Settings'**
  String get goToSettings;

  /// No description provided for @hide.
  ///
  /// In en, this message translates to:
  /// **'Hide'**
  String get hide;

  /// No description provided for @holdAndSpeak.
  ///
  /// In en, this message translates to:
  /// **'Hold and Speak'**
  String get holdAndSpeak;

  /// No description provided for @hour.
  ///
  /// In en, this message translates to:
  /// **'hour'**
  String get hour;

  /// No description provided for @hours.
  ///
  /// In en, this message translates to:
  /// **'hours'**
  String get hours;

  /// No description provided for @idNumber.
  ///
  /// In en, this message translates to:
  /// **'ID Number'**
  String get idNumber;

  /// No description provided for @image.
  ///
  /// In en, this message translates to:
  /// **'Image'**
  String get image;

  /// No description provided for @imageBeDeletedDes.
  ///
  /// In en, this message translates to:
  /// **'Image might have been deleted.'**
  String get imageBeDeletedDes;

  /// No description provided for @inputInvitationLink.
  ///
  /// In en, this message translates to:
  /// **'Input Invitation Link'**
  String get inputInvitationLink;

  /// No description provided for @invalidLinkFormat.
  ///
  /// In en, this message translates to:
  /// **'Invalid Link Format'**
  String get invalidLinkFormat;

  /// No description provided for @invalidLinkFormatContent.
  ///
  /// In en, this message translates to:
  /// **'Please check your link format or contact server owner for help.'**
  String get invalidLinkFormatContent;

  /// No description provided for @invalidServerLink.
  ///
  /// In en, this message translates to:
  /// **'Invalid Server Link'**
  String get invalidServerLink;

  /// No description provided for @invalidServerLinkContent.
  ///
  /// In en, this message translates to:
  /// **'Please check your server link or contact server owner for help.'**
  String get invalidServerLinkContent;

  /// No description provided for @invitationLink.
  ///
  /// In en, this message translates to:
  /// **'Invitation Link'**
  String get invitationLink;

  /// No description provided for @invitationLinkError.
  ///
  /// In en, this message translates to:
  /// **'Invitation Link Error'**
  String get invitationLinkError;

  /// No description provided for @invitationLinkGenerationError.
  ///
  /// In en, this message translates to:
  /// **'An error occurred when generating invitation link.\nPlease check your network condition or contact server owner for help.'**
  String get invitationLinkGenerationError;

  /// No description provided for @invitationLinkUrlNotMatch.
  ///
  /// In en, this message translates to:
  /// **'This app has a default server address. Invitation links from other servers will not be accepted.'**
  String get invitationLinkUrlNotMatch;

  /// No description provided for @inviteNewUsers.
  ///
  /// In en, this message translates to:
  /// **'Invite New Users'**
  String get inviteNewUsers;

  /// No description provided for @language.
  ///
  /// In en, this message translates to:
  /// **'Language'**
  String get language;

  /// No description provided for @logIn.
  ///
  /// In en, this message translates to:
  /// **'Log in'**
  String get logIn;

  /// No description provided for @logInTo.
  ///
  /// In en, this message translates to:
  /// **'Login To'**
  String get logInTo;

  /// No description provided for @logOut.
  ///
  /// In en, this message translates to:
  /// **'Log Out'**
  String get logOut;

  /// No description provided for @logoutWarning.
  ///
  /// In en, this message translates to:
  /// **'Are you sure to log out'**
  String get logoutWarning;

  /// No description provided for @logoutWarningWithQM.
  ///
  /// In en, this message translates to:
  /// **'Are you sure to log out?'**
  String get logoutWarningWithQM;

  /// No description provided for @male.
  ///
  /// In en, this message translates to:
  /// **'Male'**
  String get male;

  /// No description provided for @markdown.
  ///
  /// In en, this message translates to:
  /// **'Markdown'**
  String get markdown;

  /// No description provided for @message.
  ///
  /// In en, this message translates to:
  /// **'Message'**
  String get message;

  /// No description provided for @messageClearTitle.
  ///
  /// In en, this message translates to:
  /// **'Clear Messages'**
  String get messageClearTitle;

  /// No description provided for @messageClearDes.
  ///
  /// In en, this message translates to:
  /// **'Your server admin cleared all messages in server. Your local messages will be cleared as well.'**
  String get messageClearDes;

  /// No description provided for @messageWQuantifier.
  ///
  /// In en, this message translates to:
  /// **'Message'**
  String get messageWQuantifier;

  /// No description provided for @messagesWQuantifier.
  ///
  /// In en, this message translates to:
  /// **'Messages'**
  String get messagesWQuantifier;

  /// No description provided for @messageDetail.
  ///
  /// In en, this message translates to:
  /// **'Message Detail'**
  String get messageDetail;

  /// No description provided for @messageDeletionFailed.
  ///
  /// In en, this message translates to:
  /// **'Message deletion failed'**
  String get messageDeletionFailed;

  /// No description provided for @messageHasBeenDeleted.
  ///
  /// In en, this message translates to:
  /// **'This message has been deleted.'**
  String get messageHasBeenDeleted;

  /// No description provided for @members.
  ///
  /// In en, this message translates to:
  /// **'Members'**
  String get members;

  /// No description provided for @microphonePermissionRequired.
  ///
  /// In en, this message translates to:
  /// **'Microphone Permission Required'**
  String get microphonePermissionRequired;

  /// No description provided for @microphonePermissionRequiredDes.
  ///
  /// In en, this message translates to:
  /// **'Microphone permission is required to record voice messages. Please go to Settings to grant permission.'**
  String get microphonePermissionRequiredDes;

  /// No description provided for @minute.
  ///
  /// In en, this message translates to:
  /// **'minute'**
  String get minute;

  /// No description provided for @minutes.
  ///
  /// In en, this message translates to:
  /// **'minutes'**
  String get minutes;

  /// No description provided for @mute.
  ///
  /// In en, this message translates to:
  /// **'Mute'**
  String get mute;

  /// No description provided for @muteNotification.
  ///
  /// In en, this message translates to:
  /// **'Mute Notification'**
  String get muteNotification;

  /// No description provided for @name.
  ///
  /// In en, this message translates to:
  /// **'Name'**
  String get name;

  /// No description provided for @networkError.
  ///
  /// In en, this message translates to:
  /// **'Network Error'**
  String get networkError;

  /// No description provided for @networkErrorDes.
  ///
  /// In en, this message translates to:
  /// **'Please check your network settings.'**
  String get networkErrorDes;

  /// No description provided for @newPassword.
  ///
  /// In en, this message translates to:
  /// **'New Password'**
  String get newPassword;

  /// No description provided for @newPrivateChannel.
  ///
  /// In en, this message translates to:
  /// **'New Private Channel'**
  String get newPrivateChannel;

  /// No description provided for @newPublicChannel.
  ///
  /// In en, this message translates to:
  /// **'New Public Channel'**
  String get newPublicChannel;

  /// No description provided for @next.
  ///
  /// In en, this message translates to:
  /// **'Next'**
  String get next;

  /// No description provided for @nickname.
  ///
  /// In en, this message translates to:
  /// **'Nick name'**
  String get nickname;

  /// No description provided for @noContent.
  ///
  /// In en, this message translates to:
  /// **'No Content'**
  String get noContent;

  /// No description provided for @noQrCodeDetected.
  ///
  /// In en, this message translates to:
  /// **'No QR Code Detected. Please check original image, or convert it to .jpg format.'**
  String get noQrCodeDetected;

  /// No description provided for @notificationLanguage.
  ///
  /// In en, this message translates to:
  /// **'Notification Language'**
  String get notificationLanguage;

  /// No description provided for @other.
  ///
  /// In en, this message translates to:
  /// **'Other'**
  String get other;

  /// No description provided for @off.
  ///
  /// In en, this message translates to:
  /// **'Off'**
  String get off;

  /// No description provided for @ok.
  ///
  /// In en, this message translates to:
  /// **'OK'**
  String get ok;

  /// No description provided for @oldPassword.
  ///
  /// In en, this message translates to:
  /// **'Old Password'**
  String get oldPassword;

  /// No description provided for @open.
  ///
  /// In en, this message translates to:
  /// **'Open'**
  String get open;

  /// No description provided for @openWithOtherApps.
  ///
  /// In en, this message translates to:
  /// **'Open with other Apps'**
  String get openWithOtherApps;

  /// No description provided for @onlyAndAdminCanSendMessage.
  ///
  /// In en, this message translates to:
  /// **'Only owner and admin can send message'**
  String get onlyAndAdminCanSendMessage;

  /// No description provided for @paste.
  ///
  /// In en, this message translates to:
  /// **'Paste'**
  String get paste;

  /// No description provided for @password.
  ///
  /// In en, this message translates to:
  /// **'Password'**
  String get password;

  /// No description provided for @confirmPassword.
  ///
  /// In en, this message translates to:
  /// **'Confirm Password'**
  String get confirmPassword;

  /// No description provided for @permissionRequired.
  ///
  /// In en, this message translates to:
  /// **'Permission Required'**
  String get permissionRequired;

  /// No description provided for @permissionRequiredDes.
  ///
  /// In en, this message translates to:
  /// **'Please go to Settings to grant permission.'**
  String get permissionRequiredDes;

  /// No description provided for @pin.
  ///
  /// In en, this message translates to:
  /// **'Pin'**
  String get pin;

  /// No description provided for @pinChat.
  ///
  /// In en, this message translates to:
  /// **'Pin Chat'**
  String get pinChat;

  /// No description provided for @privateChannelToPublicTitle.
  ///
  /// In en, this message translates to:
  /// **'Change to Public Channel'**
  String get privateChannelToPublicTitle;

  /// No description provided for @privateChannelToPublicContent.
  ///
  /// In en, this message translates to:
  /// **'It will invite all members in this server to this channel. If you are not an admin, you will lose ownership of this channel.'**
  String get privateChannelToPublicContent;

  /// No description provided for @publicChannel.
  ///
  /// In en, this message translates to:
  /// **'Public Channel'**
  String get publicChannel;

  /// No description provided for @publicChannelToPrivateTitle.
  ///
  /// In en, this message translates to:
  /// **'Change to Private Channel'**
  String get publicChannelToPrivateTitle;

  /// No description provided for @publicChannelToPrivateContent.
  ///
  /// In en, this message translates to:
  /// **'This channel will not be visible to new server members. Current members will not be affected.'**
  String get publicChannelToPrivateContent;

  /// No description provided for @qrCode.
  ///
  /// In en, this message translates to:
  /// **'QR Code'**
  String get qrCode;

  /// No description provided for @registrationSuccess.
  ///
  /// In en, this message translates to:
  /// **'Registration Success'**
  String get registrationSuccess;

  /// No description provided for @registerEmailSentDes.
  ///
  /// In en, this message translates to:
  /// **'A registration email has been sent, please check your email inbox.'**
  String get registerEmailSentDes;

  /// No description provided for @registrationFailed.
  ///
  /// In en, this message translates to:
  /// **'Registration Failed'**
  String get registrationFailed;

  /// No description provided for @registrationFailedDes.
  ///
  /// In en, this message translates to:
  /// **'An error occurred during registration. Please try again later or contact server admins for help.'**
  String get registrationFailedDes;

  /// No description provided for @releaseToCancel.
  ///
  /// In en, this message translates to:
  /// **'Release to cancel'**
  String get releaseToCancel;

  /// No description provided for @releaseToSend.
  ///
  /// In en, this message translates to:
  /// **'Release to send'**
  String get releaseToSend;

  /// No description provided for @rememberMe.
  ///
  /// In en, this message translates to:
  /// **'Remember me'**
  String get rememberMe;

  /// No description provided for @removeContact.
  ///
  /// In en, this message translates to:
  /// **'Remove Contact'**
  String get removeContact;

  /// No description provided for @removeContactWarning.
  ///
  /// In en, this message translates to:
  /// **'The contact will be removed from Contact List but you can still chat. All messages and data will be preserved.'**
  String get removeContactWarning;

  /// No description provided for @removeFromServer.
  ///
  /// In en, this message translates to:
  /// **'Remove from server'**
  String get removeFromServer;

  /// No description provided for @removeFromServerWarning.
  ///
  /// In en, this message translates to:
  /// **'The user will be removed from the server.'**
  String get removeFromServerWarning;

  /// No description provided for @reply.
  ///
  /// In en, this message translates to:
  /// **'Reply'**
  String get reply;

  /// No description provided for @repliedMessageDeleted.
  ///
  /// In en, this message translates to:
  /// **'The replied message has been deleted.'**
  String get repliedMessageDeleted;

  /// No description provided for @replyingTo.
  ///
  /// In en, this message translates to:
  /// **'Replying to'**
  String get replyingTo;

  /// No description provided for @retry.
  ///
  /// In en, this message translates to:
  /// **'Retry'**
  String get retry;

  /// No description provided for @resend.
  ///
  /// In en, this message translates to:
  /// **'Resend'**
  String get resend;

  /// No description provided for @resendNotSupported.
  ///
  /// In en, this message translates to:
  /// **'Resending this type of message is not supported.'**
  String get resendNotSupported;

  /// No description provided for @resetFcmToken.
  ///
  /// In en, this message translates to:
  /// **'Reset Push Notification'**
  String get resetFcmToken;

  /// No description provided for @resetFcmTokenFooter.
  ///
  /// In en, this message translates to:
  /// **'You may re-register Firebase device token for Push Notifications, if a push notification error occurred during login process. Please check your network condition, as it needs to connect Google service.'**
  String get resetFcmTokenFooter;

  /// No description provided for @save.
  ///
  /// In en, this message translates to:
  /// **'Save'**
  String get save;

  /// No description provided for @savedItems.
  ///
  /// In en, this message translates to:
  /// **'Saved Items'**
  String get savedItems;

  /// No description provided for @saveToDownloads.
  ///
  /// In en, this message translates to:
  /// **'Save to Downloads Folder'**
  String get saveToDownloads;

  /// No description provided for @saveToAlbum.
  ///
  /// In en, this message translates to:
  /// **'Save to Photo Album'**
  String get saveToAlbum;

  /// No description provided for @savedSuccessfully.
  ///
  /// In en, this message translates to:
  /// **'File has been saved successfully'**
  String get savedSuccessfully;

  /// No description provided for @saveFailed.
  ///
  /// In en, this message translates to:
  /// **'File save failed'**
  String get saveFailed;

  /// No description provided for @saving.
  ///
  /// In en, this message translates to:
  /// **'Saving...'**
  String get saving;

  /// No description provided for @scanQrCode.
  ///
  /// In en, this message translates to:
  /// **'Scan QR Code'**
  String get scanQrCode;

  /// No description provided for @scanQrCodeToRegister.
  ///
  /// In en, this message translates to:
  /// **'Scan this QR Code to register'**
  String get scanQrCodeToRegister;

  /// No description provided for @searching.
  ///
  /// In en, this message translates to:
  /// **'Searching'**
  String get searching;

  /// No description provided for @second.
  ///
  /// In en, this message translates to:
  /// **'second'**
  String get second;

  /// No description provided for @seconds.
  ///
  /// In en, this message translates to:
  /// **'seconds'**
  String get seconds;

  /// No description provided for @select.
  ///
  /// In en, this message translates to:
  /// **'Select'**
  String get select;

  /// No description provided for @setNewAvatar.
  ///
  /// In en, this message translates to:
  /// **'Set a new avatar'**
  String get setNewAvatar;

  /// No description provided for @send.
  ///
  /// In en, this message translates to:
  /// **'Send'**
  String get send;

  /// No description provided for @sendMessage.
  ///
  /// In en, this message translates to:
  /// **'Send Message'**
  String get sendMessage;

  /// No description provided for @share.
  ///
  /// In en, this message translates to:
  /// **'Share'**
  String get share;

  /// No description provided for @shareInvitationLink.
  ///
  /// In en, this message translates to:
  /// **'Share Invitation Link'**
  String get shareInvitationLink;

  /// No description provided for @shareQrCode.
  ///
  /// In en, this message translates to:
  /// **'Share QR Code'**
  String get shareQrCode;

  /// No description provided for @showInFileSystem.
  ///
  /// In en, this message translates to:
  /// **'You may go to Files App to view this file.'**
  String get showInFileSystem;

  /// No description provided for @signIn.
  ///
  /// In en, this message translates to:
  /// **'Sign In'**
  String get signIn;

  /// No description provided for @signUp.
  ///
  /// In en, this message translates to:
  /// **'Sign Up'**
  String get signUp;

  /// No description provided for @signUpTo.
  ///
  /// In en, this message translates to:
  /// **'Sign Up to'**
  String get signUpTo;

  /// No description provided for @tick.
  ///
  /// In en, this message translates to:
  /// **'✓'**
  String get tick;

  /// No description provided for @switchServer.
  ///
  /// In en, this message translates to:
  /// **'Switch Server'**
  String get switchServer;

  /// No description provided for @tapForMore.
  ///
  /// In en, this message translates to:
  /// **'Tap for more'**
  String get tapForMore;

  /// No description provided for @text.
  ///
  /// In en, this message translates to:
  /// **'Text'**
  String get text;

  /// No description provided for @to.
  ///
  /// In en, this message translates to:
  /// **'to'**
  String get to;

  /// No description provided for @today.
  ///
  /// In en, this message translates to:
  /// **'Today'**
  String get today;

  /// No description provided for @transferOwnership.
  ///
  /// In en, this message translates to:
  /// **'Transfer Ownership'**
  String get transferOwnership;

  /// No description provided for @unblock.
  ///
  /// In en, this message translates to:
  /// **'Unblock'**
  String get unblock;

  /// No description provided for @unmute.
  ///
  /// In en, this message translates to:
  /// **'Unmute'**
  String get unmute;

  /// No description provided for @unpin.
  ///
  /// In en, this message translates to:
  /// **'Unpin'**
  String get unpin;

  /// No description provided for @unsupportedMessageType.
  ///
  /// In en, this message translates to:
  /// **'Unsupported Message Type'**
  String get unsupportedMessageType;

  /// No description provided for @uploadError.
  ///
  /// In en, this message translates to:
  /// **'Upload Error'**
  String get uploadError;

  /// No description provided for @uploadErrorContent.
  ///
  /// In en, this message translates to:
  /// **'Avatar size exceeds limit.'**
  String get uploadErrorContent;

  /// No description provided for @userBlocked.
  ///
  /// In en, this message translates to:
  /// **'This user has been blocked by you.'**
  String get userBlocked;

  /// No description provided for @userEmail.
  ///
  /// In en, this message translates to:
  /// **'User Email'**
  String get userEmail;

  /// No description provided for @userId.
  ///
  /// In en, this message translates to:
  /// **'User ID'**
  String get userId;

  /// No description provided for @userInfo.
  ///
  /// In en, this message translates to:
  /// **'User Info'**
  String get userInfo;

  /// No description provided for @userName.
  ///
  /// In en, this message translates to:
  /// **'User Name'**
  String get userName;

  /// No description provided for @userNotContact.
  ///
  /// In en, this message translates to:
  /// **'This user is not in your contact list.'**
  String get userNotContact;

  /// No description provided for @userSearchNoResult.
  ///
  /// In en, this message translates to:
  /// **'User does not exist, or is not allowed to be searched.'**
  String get userSearchNoResult;

  /// No description provided for @userSettings.
  ///
  /// In en, this message translates to:
  /// **'User Settings'**
  String get userSettings;

  /// No description provided for @version.
  ///
  /// In en, this message translates to:
  /// **'Version'**
  String get version;

  /// No description provided for @video.
  ///
  /// In en, this message translates to:
  /// **'Video'**
  String get video;

  /// No description provided for @viewChatHistory.
  ///
  /// In en, this message translates to:
  /// **'View Chat History'**
  String get viewChatHistory;

  /// No description provided for @voice.
  ///
  /// In en, this message translates to:
  /// **'Voice'**
  String get voice;

  /// No description provided for @week.
  ///
  /// In en, this message translates to:
  /// **'week'**
  String get week;

  /// No description provided for @weeks.
  ///
  /// In en, this message translates to:
  /// **'weeks'**
  String get weeks;

  /// No description provided for @yesterday.
  ///
  /// In en, this message translates to:
  /// **'yesterday'**
  String get yesterday;

  /// No description provided for @you.
  ///
  /// In en, this message translates to:
  /// **'You'**
  String get you;

  /// No description provided for @leave.
  ///
  /// In en, this message translates to:
  /// **'Leave'**
  String get leave;

  /// No description provided for @leaveChannel.
  ///
  /// In en, this message translates to:
  /// **'Leave Channel'**
  String get leaveChannel;

  /// No description provided for @leaveChannelWarningDes.
  ///
  /// In en, this message translates to:
  /// **'This channel and its messages will be deleted from all your devices.'**
  String get leaveChannelWarningDes;

  /// No description provided for @leaveChannelTransferOwnerDes.
  ///
  /// In en, this message translates to:
  /// **'As Owner, you have to transfer ownership before leaving.'**
  String get leaveChannelTransferOwnerDes;

  /// No description provided for @chatsBarNewChannel.
  ///
  /// In en, this message translates to:
  /// **'New Channel'**
  String get chatsBarNewChannel;

  /// No description provided for @chatsBarNewDm.
  ///
  /// In en, this message translates to:
  /// **'New Direct Message'**
  String get chatsBarNewDm;

  /// No description provided for @newChannelPageTitle.
  ///
  /// In en, this message translates to:
  /// **'Create Channel'**
  String get newChannelPageTitle;

  /// No description provided for @newChannelPageName.
  ///
  /// In en, this message translates to:
  /// **'Channel Name'**
  String get newChannelPageName;

  /// No description provided for @newChannelPageDes.
  ///
  /// In en, this message translates to:
  /// **'Description'**
  String get newChannelPageDes;

  /// No description provided for @newChannelPagePrivateChannel.
  ///
  /// In en, this message translates to:
  /// **'Private Channel'**
  String get newChannelPagePrivateChannel;

  /// No description provided for @newChannelPagePrivateEnableDes.
  ///
  /// In en, this message translates to:
  /// **'Only selected members will be able to view this channel.'**
  String get newChannelPagePrivateEnableDes;

  /// No description provided for @newChannelPagePrivateDisableDes.
  ///
  /// In en, this message translates to:
  /// **'All members in this server will be able to view this channel.'**
  String get newChannelPagePrivateDisableDes;

  /// No description provided for @newChannelPagePrivateDes.
  ///
  /// In en, this message translates to:
  /// **'Only admins are allowed to create public channels.'**
  String get newChannelPagePrivateDes;

  /// No description provided for @newPrivateChannelSelectTitle.
  ///
  /// In en, this message translates to:
  /// **'Create Private Channel'**
  String get newPrivateChannelSelectTitle;

  /// No description provided for @newDmPageTitle.
  ///
  /// In en, this message translates to:
  /// **'New Direct Message'**
  String get newDmPageTitle;

  /// No description provided for @serverInfoSettingTitle.
  ///
  /// In en, this message translates to:
  /// **'Server Overview'**
  String get serverInfoSettingTitle;

  /// No description provided for @serverInfoSettingNameHint.
  ///
  /// In en, this message translates to:
  /// **'Server Name'**
  String get serverInfoSettingNameHint;

  /// No description provided for @serverInfoSettingDesHint.
  ///
  /// In en, this message translates to:
  /// **'Server Description'**
  String get serverInfoSettingDesHint;

  /// No description provided for @serverInfoSettingFooter.
  ///
  /// In en, this message translates to:
  /// **'Above updates will only be seen after reopening App or logging back in.'**
  String get serverInfoSettingFooter;

  /// No description provided for @aboutPageTitle.
  ///
  /// In en, this message translates to:
  /// **'About VoceChat'**
  String get aboutPageTitle;

  /// No description provided for @aboutPageCheckUpdates.
  ///
  /// In en, this message translates to:
  /// **'Check Updates'**
  String get aboutPageCheckUpdates;

  /// No description provided for @aboutPageChangeLog.
  ///
  /// In en, this message translates to:
  /// **'changelog'**
  String get aboutPageChangeLog;

  /// No description provided for @aboutPageUpdateTitleUpToDate.
  ///
  /// In en, this message translates to:
  /// **'Current Version is Up-to-date'**
  String get aboutPageUpdateTitleUpToDate;

  /// No description provided for @aboutPageUpdateTitleUpToDateContent.
  ///
  /// In en, this message translates to:
  /// **'You are using the latest VoceChat App.'**
  String get aboutPageUpdateTitleUpToDateContent;

  /// No description provided for @aboutPageCopyRight.
  ///
  /// In en, this message translates to:
  /// **'© Privoce Inc. All Rights Reserved.'**
  String get aboutPageCopyRight;

  /// No description provided for @firebaseEnable.
  ///
  /// In en, this message translates to:
  /// **'Enable'**
  String get firebaseEnable;

  /// No description provided for @savedItemEmptyChatDes.
  ///
  /// In en, this message translates to:
  /// **'This chat doesn\'t have any saved item.'**
  String get savedItemEmptyChatDes;

  /// No description provided for @savedItemEmptyChannelDes.
  ///
  /// In en, this message translates to:
  /// **'This channel doesn\'t have any saved item.'**
  String get savedItemEmptyChannelDes;

  /// No description provided for @pinnedMsgPageEmptyChannelDes.
  ///
  /// In en, this message translates to:
  /// **'This channel doesn\'t have any pinned message.'**
  String get pinnedMsgPageEmptyChannelDes;

  /// No description provided for @pinnedMsgPageUnPinTitle.
  ///
  /// In en, this message translates to:
  /// **'Unpin Message'**
  String get pinnedMsgPageUnPinTitle;

  /// No description provided for @pinnedMsgPageUnPinContent.
  ///
  /// In en, this message translates to:
  /// **'Are you sure to unpin this message?'**
  String get pinnedMsgPageUnPinContent;

  /// No description provided for @pinnedMsgPageUnPinErrorTitle.
  ///
  /// In en, this message translates to:
  /// **'Unpin Message Error'**
  String get pinnedMsgPageUnPinErrorTitle;

  /// No description provided for @pinnedMsgPageUnPinErrorContent.
  ///
  /// In en, this message translates to:
  /// **'Something wrong when unpinning this pinned message.'**
  String get pinnedMsgPageUnPinErrorContent;

  /// No description provided for @memberAddPageTitle.
  ///
  /// In en, this message translates to:
  /// **'Add Members'**
  String get memberAddPageTitle;

  /// No description provided for @memberAddPageAdd.
  ///
  /// In en, this message translates to:
  /// **'Add'**
  String get memberAddPageAdd;

  /// No description provided for @memberAddPageAddMembers.
  ///
  /// In en, this message translates to:
  /// **'Add Members'**
  String get memberAddPageAddMembers;

  /// No description provided for @memberAddPageInvitationLink.
  ///
  /// In en, this message translates to:
  /// **'Invitation Link'**
  String get memberAddPageInvitationLink;

  /// No description provided for @memberRemovePageTitle.
  ///
  /// In en, this message translates to:
  /// **'Remove Members'**
  String get memberRemovePageTitle;

  /// No description provided for @memberRemovePageRemove.
  ///
  /// In en, this message translates to:
  /// **'Remove'**
  String get memberRemovePageRemove;

  /// No description provided for @chatPageSelectionWarning.
  ///
  /// In en, this message translates to:
  /// **'Can\'t forward or save an Audio/forwarded/unsuccessful message in selection mode.'**
  String get chatPageSelectionWarning;

  /// No description provided for @chatPageCantFindFileWarning.
  ///
  /// In en, this message translates to:
  /// **'Can\'t Find File'**
  String get chatPageCantFindFileWarning;

  /// No description provided for @chatPageCantFindFileWarningContent.
  ///
  /// In en, this message translates to:
  /// **'File might have been deleted. Please send a new message.'**
  String get chatPageCantFindFileWarningContent;

  /// No description provided for @chatPageCantFindImageWarning.
  ///
  /// In en, this message translates to:
  /// **'Can\'t Find Image File'**
  String get chatPageCantFindImageWarning;

  /// No description provided for @chatPageCantFindImageWarningContent.
  ///
  /// In en, this message translates to:
  /// **'Image might have been deleted. Please send a new message.'**
  String get chatPageCantFindImageWarningContent;

  /// No description provided for @chatPageResendWarning.
  ///
  /// In en, this message translates to:
  /// **'Resend Message'**
  String get chatPageResendWarning;

  /// No description provided for @chatPageResendWarningContent.
  ///
  /// In en, this message translates to:
  /// **'Do you want to resend this message?'**
  String get chatPageResendWarningContent;

  /// No description provided for @savedPageDeleteTitle.
  ///
  /// In en, this message translates to:
  /// **'Delete Saved Item'**
  String get savedPageDeleteTitle;

  /// No description provided for @savedPageDeleteContent.
  ///
  /// In en, this message translates to:
  /// **'Are you sure to delete this saved item?'**
  String get savedPageDeleteContent;

  /// No description provided for @savedPageDeleteErrorTitle.
  ///
  /// In en, this message translates to:
  /// **'Delete Saved Item Error'**
  String get savedPageDeleteErrorTitle;

  /// No description provided for @savedPageDeleteErrorContent.
  ///
  /// In en, this message translates to:
  /// **'Something wrong with saved item deletion.'**
  String get savedPageDeleteErrorContent;

  /// No description provided for @magicLinkLoginSendError.
  ///
  /// In en, this message translates to:
  /// **'Magic Link Error'**
  String get magicLinkLoginSendError;

  /// No description provided for @magicLinkLoginSendErrorContent.
  ///
  /// In en, this message translates to:
  /// **'There was an error when sending magic link to the provided email. Please try again or report an issue to us.'**
  String get magicLinkLoginSendErrorContent;

  /// No description provided for @appQrCodeScanPageInvalidCodeTitle.
  ///
  /// In en, this message translates to:
  /// **'Invalid QR Code'**
  String get appQrCodeScanPageInvalidCodeTitle;

  /// No description provided for @appQrCodeScanPageInvalidCodeContent.
  ///
  /// In en, this message translates to:
  /// **'Please ask for a new invitation link, or check your network.'**
  String get appQrCodeScanPageInvalidCodeContent;

  /// No description provided for @settingsAboutPageNetworkError.
  ///
  /// In en, this message translates to:
  /// **'Network Error'**
  String get settingsAboutPageNetworkError;

  /// No description provided for @settingsAboutPageNetworkErrorContent.
  ///
  /// In en, this message translates to:
  /// **'VoceChat cannot fetch latest version information. You may try it later.'**
  String get settingsAboutPageNetworkErrorContent;

  /// No description provided for @chatsBarNetworkError.
  ///
  /// In en, this message translates to:
  /// **'Network Error'**
  String get chatsBarNetworkError;

  /// No description provided for @chatsBarNetworkErrorContent.
  ///
  /// In en, this message translates to:
  /// **'Please check your network settings, or try to log in again.'**
  String get chatsBarNetworkErrorContent;

  /// No description provided for @chatsBarLoginStatusExpired.
  ///
  /// In en, this message translates to:
  /// **'Login Status Expired'**
  String get chatsBarLoginStatusExpired;

  /// No description provided for @chatsBarLoginStatusExpiredContent.
  ///
  /// In en, this message translates to:
  /// **'Please login again.'**
  String get chatsBarLoginStatusExpiredContent;

  /// No description provided for @filePageCantOpenFile.
  ///
  /// In en, this message translates to:
  /// **'Can\'t Open File'**
  String get filePageCantOpenFile;

  /// No description provided for @filePageCantOpenFileContent.
  ///
  /// In en, this message translates to:
  /// **'VoceChat does not support this file type. Please open with other Apps.'**
  String get filePageCantOpenFileContent;

  /// No description provided for @filePageDownloadFailed.
  ///
  /// In en, this message translates to:
  /// **'Download Failed'**
  String get filePageDownloadFailed;

  /// No description provided for @filePageDownloadFailedContent.
  ///
  /// In en, this message translates to:
  /// **'VoceChat can\'t download this file. It might have been deleted from server.'**
  String get filePageDownloadFailedContent;

  /// No description provided for @filePageCantFindFile.
  ///
  /// In en, this message translates to:
  /// **'Can\'t Open File'**
  String get filePageCantFindFile;

  /// No description provided for @filePageCantFindFileContent.
  ///
  /// In en, this message translates to:
  /// **'VoceChat can\'t find this file on your device or from the server. It might have been deleted.'**
  String get filePageCantFindFileContent;

  /// No description provided for @chatTextFieldCameraMicrophonePermission.
  ///
  /// In en, this message translates to:
  /// **'VoceChat needs camera and microphone permissions'**
  String get chatTextFieldCameraMicrophonePermission;

  /// No description provided for @chatTextFieldCameraMicrophonePermissionContent.
  ///
  /// In en, this message translates to:
  /// **'Grant camera and microphone permissions for sending image and video messages.'**
  String get chatTextFieldCameraMicrophonePermissionContent;

  /// No description provided for @chatTextFieldPhotoPermission.
  ///
  /// In en, this message translates to:
  /// **'VoceChat needs Photo Album permission'**
  String get chatTextFieldPhotoPermission;

  /// No description provided for @chatTextFieldPhotoPermissionContent.
  ///
  /// In en, this message translates to:
  /// **'Please go to settings -> VoceChat for permission settings.'**
  String get chatTextFieldPhotoPermissionContent;

  /// No description provided for @loginSessionExpires.
  ///
  /// In en, this message translates to:
  /// **'Login Session Expires'**
  String get loginSessionExpires;

  /// No description provided for @loginSessionExpiresContent.
  ///
  /// In en, this message translates to:
  /// **'If you didn\'t login from another device, please change password immediately, or contact server admins for help.'**
  String get loginSessionExpiresContent;

  /// No description provided for @loginError.
  ///
  /// In en, this message translates to:
  /// **'Login Error'**
  String get loginError;

  /// No description provided for @loginErrorContent401.
  ///
  /// In en, this message translates to:
  /// **'Invalid account or password.'**
  String get loginErrorContent401;

  /// No description provided for @loginErrorContent403.
  ///
  /// In en, this message translates to:
  /// **'Login method is not supported.'**
  String get loginErrorContent403;

  /// No description provided for @loginErrorContent404.
  ///
  /// In en, this message translates to:
  /// **'User does not exist.'**
  String get loginErrorContent404;

  /// No description provided for @loginErrorContent409.
  ///
  /// In en, this message translates to:
  /// **'Email collision.'**
  String get loginErrorContent409;

  /// No description provided for @loginErrorContent423.
  ///
  /// In en, this message translates to:
  /// **'User has been frozen.'**
  String get loginErrorContent423;

  /// No description provided for @loginErrorContent451.
  ///
  /// In en, this message translates to:
  /// **'License has an issue. Please contact server admin for help.'**
  String get loginErrorContent451;

  /// No description provided for @loginErrorContentOther.
  ///
  /// In en, this message translates to:
  /// **'An error occurred during login.'**
  String get loginErrorContentOther;

  /// No description provided for @loginErrorCopy.
  ///
  /// In en, this message translates to:
  /// **'Copy Error'**
  String get loginErrorCopy;

  /// No description provided for @appTextSelectionControlPasteImage.
  ///
  /// In en, this message translates to:
  /// **'Paste and Send Image'**
  String get appTextSelectionControlPasteImage;

  /// No description provided for @fcmError.
  ///
  /// In en, this message translates to:
  /// **'FCM Error'**
  String get fcmError;

  /// No description provided for @fcmErrorContent.
  ///
  /// In en, this message translates to:
  /// **'FCM Configuration Failed.'**
  String get fcmErrorContent;

  /// No description provided for @fcmErrorCopyErrorLog.
  ///
  /// In en, this message translates to:
  /// **'Copy Error Log'**
  String get fcmErrorCopyErrorLog;

  /// No description provided for @invitationLinkViewSend.
  ///
  /// In en, this message translates to:
  /// **'Send invitation link to your friends.'**
  String get invitationLinkViewSend;

  /// No description provided for @noFCMTokenLoginTitle.
  ///
  /// In en, this message translates to:
  /// **'Push Notification'**
  String get noFCMTokenLoginTitle;

  /// No description provided for @noFCMTokenLoginDes.
  ///
  /// In en, this message translates to:
  /// **'Login process will continue, but Push notification service will not be available. You may check your network condition (VoceChat needs to visit Google service).'**
  String get noFCMTokenLoginDes;

  /// No description provided for @noFCMTokenReloadTitle.
  ///
  /// In en, this message translates to:
  /// **'Push Notification'**
  String get noFCMTokenReloadTitle;

  /// No description provided for @noFCMTokenReloadDes.
  ///
  /// In en, this message translates to:
  /// **'Failed to register device info. Push notification service will not be available. You may check your network condition (VoceChat needs to visit Google service).'**
  String get noFCMTokenReloadDes;

  /// No description provided for @settingsPageMyInfo.
  ///
  /// In en, this message translates to:
  /// **'My Info'**
  String get settingsPageMyInfo;

  /// No description provided for @settingsPageAccountSettings.
  ///
  /// In en, this message translates to:
  /// **'Account Settings'**
  String get settingsPageAccountSettings;

  /// No description provided for @settingsPageAbout.
  ///
  /// In en, this message translates to:
  /// **'About'**
  String get settingsPageAbout;

  /// No description provided for @settingsPageLogout.
  ///
  /// In en, this message translates to:
  /// **'Logout'**
  String get settingsPageLogout;
}

class _AppLocalizationsDelegate
    extends LocalizationsDelegate<AppLocalizations> {
  const _AppLocalizationsDelegate();

  @override
  Future<AppLocalizations> load(Locale locale) {
    return SynchronousFuture<AppLocalizations>(lookupAppLocalizations(locale));
  }

  @override
  bool isSupported(Locale locale) =>
      <String>['en', 'zh'].contains(locale.languageCode);

  @override
  bool shouldReload(_AppLocalizationsDelegate old) => false;
}

AppLocalizations lookupAppLocalizations(Locale locale) {
  // Lookup logic when only language code is specified.
  switch (locale.languageCode) {
    case 'en':
      return AppLocalizationsEn();
    case 'zh':
      return AppLocalizationsZh();
  }

  throw FlutterError(
    'AppLocalizations.delegate failed to load unsupported locale "$locale". This is likely '
    'an issue with the localizations generation tool. Please file an issue '
    'on GitHub with a reproducible sample app and the gen-l10n configuration '
    'that was used.',
  );
}
