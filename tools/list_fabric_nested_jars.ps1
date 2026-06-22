Add-Type -AssemblyName System.IO.Compression.FileSystem
$jar = ".gradle-user/caches/modules-2/files-2.1/net.fabricmc.fabric-api/fabric-api/0.152.1+26.1.2/e48e773ec41ec1267fd8833fe90c256648cf74d0/fabric-api-0.152.1+26.1.2.jar"
$zip = [System.IO.Compression.ZipFile]::OpenRead((Resolve-Path $jar))
$zip.Entries | ForEach-Object { $_.FullName } | Where-Object { $_ -like "META-INF/jars/*.jar" } | Sort-Object
$zip.Dispose()
