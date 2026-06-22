Add-Type -AssemblyName System.IO.Compression.FileSystem
$jar = ".gradle-user/caches/fabric-loom/26.1.2/minecraft-merged.jar"
$zip = [System.IO.Compression.ZipFile]::OpenRead((Resolve-Path $jar))
$targets = @(
    "ResourceLocation.class",
    "Identifier.class",
    "GuiGraphics.class",
    "DrawContext.class",
    "net/minecraft/resources/Resource",
    "net/minecraft/client/gui/Gui",
    "net/minecraft/client/gui/Draw"
)
foreach ($t in $targets) {
    "=== $t ==="
    $zip.Entries | ForEach-Object { $_.FullName } | Where-Object { $_ -like "*$t*" } | Sort-Object -Unique
}
$zip.Dispose()
