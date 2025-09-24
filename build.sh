echo "Compiling Java sources..."
javac -cp "lib/*:." -d . src/*.java

echo "Creating output directory..."
mkdir -p out

echo "Building JAR file..."
jar cfm out/RadioPlan.jar src/META-INF/MANIFEST.MF *.class src/images/ lib/

echo "Build complete! JAR file created at: out/RadioPlan.jar"
echo "Run with: java -jar out/RadioPlan.jar"