#!/bin/sh
cd release/Mac

export title="Tracer"
export version=`perl -e 'while($s=<>) {if ($s=~/"version"/) {$s =~ /value="([^"]*)"/; print $1;}}' < ../../build.xml`
export applicationName="${title} v${version}"
export size=1g
export backgroundPictureName=install.png
export finalDMGName="${title} v${version}"
export source="${title} v${version}"

echo ${version}

hdiutil create -srcfolder "${source}" -volname "${title}" -fs HFS+ \
      -fsargs "-c c=64,a=16,e=16" -format UDRW -size ${size}k pack.temp.dmg

export device=$(hdiutil attach -readwrite -noverify -noautoopen "pack.temp.dmg" | \
         egrep '^/dev/' | sed 1q | awk '{print $1}')


echo '
   tell application "Finder"
     tell disk "'${title}'"
           open
           set current view of container window to icon view
           set toolbar visible of container window to false
           set statusbar visible of container window to false
           set the bounds of container window to {400, 100, 885, 430}
           set theViewOptions to the icon view options of container window
           set arrangement of theViewOptions to not arranged
           set background picture of theViewOptions to file "'${backgroundPictureName}'"
           set label position of theViewOptions to bottom
           set icon size of theViewOptions to 96
           make new alias file at container window to POSIX file "/Applications" with properties {name:"Applications"}
           set position of item "'${applicationName}'" of container window to {100, 120}
           set position of item "'${backgroundPictureName}'" of container window to {800, 120}
           set position of item "Applications" of container window to {375, 120}
			close
			open
           update without registering applications
           delay 5
#           eject
     end tell
   end tell
' | osascript

echo "OSASCRIPT DONE"
 
 
chmod -Rf go-w /Volumes/"${title}"
sync
sync
hdiutil detach ${device}
hdiutil convert "pack.temp.dmg" -format UDZO -imagekey zlib-level=9 -o "../${finalDMGName}"
rm -f /pack.temp.dmg
