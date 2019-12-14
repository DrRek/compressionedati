run:
	java -cp out/production/compressionedati/:out/production/SeverZip/:externalLib/commons-lang3-3.9.jar compressione.Tester
compile:
	javac -d out/production/compressionedati/ -cp "src/:externalLib/commons-lang3-3.9.jar" src/compressione/*.java
cr:
	make compile
	make run
compilelzma:
	javac -cp src/ -d out/production/ src/SevenZip/*.java
runlzma:
	java -cp out/production/ SevenZip.Encoder ${src}
lzma:
	make compilelzma
	make runlzma
all:
	make compilelzma
	make cr
