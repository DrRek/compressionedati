run:
	java -cp out/production/compressionedati/ compressione.Tester ${c}
compile:
	javac -d out/production/compressionedati/ src/compressione/*.java
cr:
	make compile
	make run
compilelzma:
	javac -cp src/ -d out/production/ src/SevenZip/*.java
runlzma:
	java -cp out/production/ SevenZip.Tester
lzma:
	make compilelzma
	make runlzma	
