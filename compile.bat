mkdir jars\bin
"C:\Program Files\Java\jdk-17\bin\javac.exe" -cp "C:\Program Files (x86)\Fractal Softworks\Starsector v0.98\starsector-core\*;C:\Program Files (x86)\Fractal Softworks\Starsector v0.98\mods\LunaLib-2.0.5\jars\*" -d jars\bin src\officerretraining\*.java
"C:\Program Files\Java\jdk-17\bin\jar.exe" cf jars\officer_retraining.jar -C jars\bin .
