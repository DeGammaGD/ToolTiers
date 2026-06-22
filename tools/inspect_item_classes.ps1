Add-Type -AssemblyName System.IO.Compression.FileSystem
$jar = ".gradle-user/caches/fabric-loom/26.1.2/minecraft-merged.jar"
$zip = [System.IO.Compression.ZipFile]::OpenRead((Resolve-Path $jar))
$patterns = @("TieredItem.class","ArmorItem.class","Equipable.class","Equippable.class")
foreach ($p in $patterns) {
  "=== $p ==="
  $zip.Entries | ForEach-Object { $_.FullName } | Where-Object { $_ -like "*$p*" } | Sort-Object -Unique
}
$zip.Dispose()
