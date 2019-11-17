run:
	java -cp out/production/compressionedati/ compressione.Tester ${c}
compile:
	javac -d out/production/compressionedati/ src/compressione/*.java
cr:
	make compile
	make run

