Add-Type -AssemblyName System.IO.Compression.FileSystem
$jar = ".gradle-user/caches/fabric-loom/26.1.2/minecraft-merged.jar"
$zip = [System.IO.Compression.ZipFile]::OpenRead((Resolve-Path $jar))
$zip.Entries | ForEach-Object { $_.FullName } | Where-Object { $_ -like "*server/permissions/*" } | Sort-Object -Unique
$zip.Dispose()
