param(
    [string]$NestedJarPath,
    [string]$OutputPath
)

Add-Type -AssemblyName System.IO.Compression.FileSystem
$outerPath = Resolve-Path ".gradle-user/caches/modules-2/files-2.1/net.fabricmc.fabric-api/fabric-api/0.152.1+26.1.2/e48e773ec41ec1267fd8833fe90c256648cf74d0/fabric-api-0.152.1+26.1.2.jar"
$outer = [System.IO.Compression.ZipFile]::OpenRead($outerPath)
try {
    $entry = $outer.GetEntry($NestedJarPath)
    if (-not $entry) { throw "Nested jar not found: $NestedJarPath" }
    $dir = Split-Path -Parent $OutputPath
    if ($dir -and -not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir | Out-Null }
    $stream = $entry.Open()
    $fs = [System.IO.File]::Open($OutputPath, [System.IO.FileMode]::Create, [System.IO.FileAccess]::Write)
    try { $stream.CopyTo($fs) } finally { $stream.Dispose(); $fs.Dispose() }
} finally {
    $outer.Dispose()
}
