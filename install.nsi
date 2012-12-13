!define GET_JAVA_URL "http://www.java.com"
!define ICON_NAME "ico\TalkAbroad Icons ICO 100x100.ico"

!define APP_NAME "Talk Abroad LogKeeper v1.4"


Name "${APP_NAME}"
Icon "${ICON_NAME}"
OutFile "LogKeeper v1.4_Installer.exe" ; The file to write
InstallDir "$PROGRAMFILES\LogKeeper v1.4" ; Set the default Installation Directory

; Set the text which prompts the user to enter the installation directory
DirText "Please choose a directory to which you'd like to install this application."

RequestExecutionLevel user

Section "find java" FINDJAVA
  
	SetRegView 64
	ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
	StrCmp "" $R1 Check32BitRegistry CheckVersion

	Check32BitRegistry:
	SetRegView 32
	ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
	StrCmp "" $R1 OpenBrowserToGetJava CheckVersion

	CheckVersion:
	StrCmp "1.7" $R1 Done OpenBrowserToGetJava
	
	OpenBrowserToGetJava:
	messageBox MB_OK "Required Java version 1.7 not found. $\nCannot proceed."
	Exec '"explorer.exe" ${GET_JAVA_URL}'
	Abort
	

	Done:
	ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$R1" "JavaHome"
SectionEnd


Section 
	SetOutPath $INSTDIR\ico
	File "${ICON_NAME}"

	SetOutPath $INSTDIR\lib
	File lib\skype-java-api-1.4.jar
	File lib\swt.jar
	File lib\tritonus_mp3.jar
	File lib\tritonus_remaining-0.3.6.jar
	File lib\tritonus_share-0.3.6.jar
	File lib\winp-1.5.jar
	
	SetOutPath $INSTDIR
	File config.txt
	File "TalkAbroad Log Keeper 1.4.jar"
	File lame_enc.dll
	File lametritonus.dll
	
	CreateDirectory "$INSTDIR\recordings"

	WriteUninstaller $INSTDIR\Uninstall.exe

	CreateDirectory "$SMPROGRAMS\${APP_NAME}"
	CreateShortCut "$SMPROGRAMS\${APP_NAME}\${APP_NAME}.lnk" "$R0\bin\javaw.exe" "-jar $\"TalkAbroad Log Keeper 1.4.jar$\" TalkAbroadLogKeeper" "$INSTDIR\${ICON_NAME}"
	CreateShortCut "$SMPROGRAMS\${APP_NAME}\Uninstall ${APP_NAME}.lnk" "$INSTDIR\Uninstall.exe"
	CreateShortCut "$DESKTOP\${APP_NAME}.lnk" "$R0\bin\javaw.exe" "-jar $\"TalkAbroad Log Keeper 1.4.jar$\" TalkAbroadLogKeeper" "$INSTDIR\${ICON_NAME}"

	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\LogKeeper v1.4" "DisplayName"\
	"LogKeeper v1.4 (remove only)"
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\LogKeeper v1.4" "UninstallString" \
	"$INSTDIR\Uninstall.exe"
SectionEnd

Section "Uninstall"
# Always delete uninstaller first
Delete $INSTDIR\uninstaller.exe
RMDIR /r $INSTDIR\lib
RMDIR /r $INSTDIR\recordings
RMDIR /r $INSTDIR\ico
Delete $INSTDIR\*.*
RMDIR /r "$SMPROGRAMS\${APP_NAME}"
Delete "$DESKTOP\${APP_NAME}.lnk"
DeleteRegValue HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\LogKeeper v1.4" "DisplayName"
DeleteRegValue HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\LogKeeper v1.4" "UninstallString"
DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\LogKeeper v1.4"

SectionEnd
