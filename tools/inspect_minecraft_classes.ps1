Add-Type -AssemblyName System.IO.Compression.FileSystem
$jar = ".gradle-user/caches/fabric-loom/26.1.2/minecraft-merged.jar"
$zip = [System.IO.Compression.ZipFile]::OpenRead((Resolve-Path $jar))
$patterns = @(
    "ResourceLocation.class",
    "Identifier.class",
    "GuiGraphics.class",
    "DrawContext.class",
    "net/minecraft/resources/",
    "net/minecraft/util/"
)
$zip.Entries |
    ForEach-Object { $_.FullName } |
    Where-Object {
        $name = $_
        $patterns | ForEach-Object { if ($name -like "*$_*") { $true } } | Where-Object { $_ } | Select-Object -First 1
    } |
    Sort-Object -Unique
$zip.Dispose()
