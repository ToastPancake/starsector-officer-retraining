mkdir jars\bin
"jdk\jdk-17.0.2\bin\javac.exe" -cp "C:\Program Files (x86)\Fractal Softworks\Starsector v0.98\starsector-core\*" -d jars\bin officer_retraining\src\officerretraining\*.java
"jdk\jdk-17.0.2\bin\jar.exe" cf officer_retraining\jars\officer_retraining.jar -C jars\bin .
