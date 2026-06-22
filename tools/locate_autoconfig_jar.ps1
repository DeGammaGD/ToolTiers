Get-ChildItem -Path ".gradle-user/caches/modules-2/files-2.1" -Recurse -Filter "*.jar" |
  Where-Object { $_.FullName -like "*cloth-config*" -or $_.FullName -like "*autoconfig*" } |
  Select-Object -ExpandProperty FullName
