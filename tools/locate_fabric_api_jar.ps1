Get-ChildItem -Path ".gradle-user/caches/modules-2/files-2.1" -Recurse -Filter "*.jar" |
    Where-Object { $_.Name -like "*fabric-api*" -and $_.FullName -like "*0.152.1+26.1.2*" } |
    Select-Object -ExpandProperty FullName
