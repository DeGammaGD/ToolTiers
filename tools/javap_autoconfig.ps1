$javap = "C:\Program Files\Java\jdk-25.0.3\bin\javap.exe"
$jar = Resolve-Path ".gradle-user/caches/modules-2/files-2.1/me.shedaniel.cloth/cloth-config-fabric/26.1.154/832f9047bc4af13587709c74f7c37e1c8579d1eb/cloth-config-fabric-26.1.154.jar"
& $javap -classpath $jar "me.shedaniel.autoconfig.AutoConfig"
