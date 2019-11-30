run:
	java -cp out/production/compressionedati/ compressione.Tester ${c} ${src}
compile:
	javac -d out/production/compressionedati/ src/compressione/*.java
cr:
	make compile
	make run
compilelzma:
	javac -cp src/ -d out/production/ src/SevenZip/*.java
runlzma:
	java -cp out/production/ SevenZip.Tester ${src}
lzma:
	make compilelzma
	make runlzma
all:
	make cr
	make lzma