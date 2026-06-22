$files = Get-ChildItem -Path "src/main/java" -Recurse -Filter "*.java"
foreach ($f in $files) {
    $text = Get-Content -Raw -Path $f.FullName
    $new = $text
    $new = $new -replace 'Identifier\.of\(([^,\)]+),\s*([^\)]+)\)', 'Identifier.fromNamespaceAndPath($1, $2)'
    $new = $new -replace 'Identifier\.of\(([^,\)]+)\)', 'Identifier.parse($1)'
    if ($new -ne $text) {
        [System.IO.File]::WriteAllText($f.FullName, $new)
    }
}
