Get-ChildItem -Path ".gradle-user" -Recurse -Filter "*.jar" |
    Where-Object { $_.FullName -match "minecraft|26\.1\.2|yarn|mojang|intermediary" } |
    Select-Object -ExpandProperty FullName
