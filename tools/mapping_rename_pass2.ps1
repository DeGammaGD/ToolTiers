$files = Get-ChildItem -Path "src/main/java" -Recurse -Filter "*.java"

foreach ($f in $files) {
    $text = Get-Content -Raw -Path $f.FullName
    $new = $text

    $new = $new -replace 'import\s+net\.minecraft\.util\.Identifier;', 'import net.minecraft.resources.Identifier;'
    $new = $new -replace 'import\s+net\.minecraft\.client\.gui\.DrawContext;', 'import net.minecraft.client.gui.GuiGraphicsExtractor;'
    $new = $new -replace '\bDrawContext\b', 'GuiGraphicsExtractor'

    if ($new -ne $text) {
        [System.IO.File]::WriteAllText($f.FullName, $new)
    }
}
