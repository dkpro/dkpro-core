The fao30.model.gz was trained from the FAO30 dataaset:

  https://code.google.com/archive/p/maui-indexer/downloads#makechanges

using

  java -jar maui-standalone-1.1-SNAPSHOT.jar train -l fao30/documents/ -m fao30.model -v none -o 2

With the ".key" files from indexer "iic1" copied into the "documents" folder prior to training.

The `input.txt` file contains a few (modified) sentences from the file `a0011e00.txt`.

This data is used for testing purposes only. 