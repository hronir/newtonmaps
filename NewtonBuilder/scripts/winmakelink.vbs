Dim ArgObj, linkName, linkCommand, linkArguments

Set ArgObj = WScript.Arguments 
linkName = ArgObj(0)
linkCommand = ArgObj(1) 
linkArguments = ArgObj(2)

set WshShell = WScript.CreateObject("WScript.Shell" )
set oShellLink = WshShell.CreateShortcut(linkName)
oShellLink.TargetPath = linkCommand
oShellLink.Arguments = linkArguments
oShellLink.WindowStyle = 1
oShellLink.Description = ""
oShellLink.WorkingDirectory = ""
oShellLink.Save 