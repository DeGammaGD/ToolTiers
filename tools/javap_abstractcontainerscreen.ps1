$jar = Resolve-Path ".gradle-user/caches/fabric-loom/26.1.2/minecraft-merged.jar"
$javap = "C:\Program Files\Java\jdk-25.0.3\bin\javap.exe"
& $javap -classpath $jar "net.minecraft.client.gui.screens.inventory.AbstractContainerScreen"
