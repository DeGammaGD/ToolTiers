$javap = "C:\Program Files\Java\jdk-25.0.3\bin\javap.exe"
$jar = Resolve-Path "tools/fabric-networking-api-v1.jar"
& $javap -classpath $jar "net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry"
