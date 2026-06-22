$files = Get-ChildItem -Path "src/main/java" -Recurse -Filter "*.java"

foreach ($f in $files) {
    $text = Get-Content -Raw -Path $f.FullName
    $new = $text
    $new = $new -replace 'import\s+net\.minecraft\.resources\.ResourceLocation;', 'import net.minecraft.util.Identifier;'
    $new = $new -replace '\bResourceLocation\b', 'Identifier'
    $new = $new -replace 'Identifier\.parse\(', 'Identifier.of('
    $new = $new -replace 'Identifier\.fromNamespaceAndPath\(', 'Identifier.of('

    $new = $new -replace 'import\s+net\.minecraft\.client\.gui\.GuiGraphics;', 'import net.minecraft.client.gui.DrawContext;'
    $new = $new -replace '\bGuiGraphics\b', 'DrawContext'

    if ($new -ne $text) {
        [System.IO.File]::WriteAllText($f.FullName, $new)
    }
}
