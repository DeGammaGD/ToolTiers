param(
    [string]$NestedJarPath,
    [string[]]$Patterns
)

Add-Type -AssemblyName System.IO.Compression.FileSystem
$outerPath = Resolve-Path ".gradle-user/caches/modules-2/files-2.1/net.fabricmc.fabric-api/fabric-api/0.152.1+26.1.2/e48e773ec41ec1267fd8833fe90c256648cf74d0/fabric-api-0.152.1+26.1.2.jar"
$outer = [System.IO.Compression.ZipFile]::OpenRead($outerPath)
try {
    $entry = $outer.GetEntry($NestedJarPath)
    if (-not $entry) {
        Write-Output "Nested jar not found: $NestedJarPath"
        exit 1
    }

    $ms = New-Object System.IO.MemoryStream
    $es = $entry.Open()
    $es.CopyTo($ms)
    $es.Dispose()
    $ms.Position = 0

    $inner = New-Object System.IO.Compression.ZipArchive($ms, [System.IO.Compression.ZipArchiveMode]::Read)
    try {
        foreach ($p in $Patterns) {
            Write-Output "=== $p ==="
            $inner.Entries |
                ForEach-Object { $_.FullName } |
                Where-Object { $_ -like "*$p*" } |
                Sort-Object -Unique
        }
    } finally {
        $inner.Dispose()
        $ms.Dispose()
    }
} finally {
    $outer.Dispose()
}
