If the binaries for OS-X are updated make sure to replace fixed-local system
paths for the dynamic libraries with the following command:
 
"/usr/local/lib/"	(a possible) local installation directory

command:
install_name_tool -change "/usr/local/lib/libmecab.2.dylib" "@loader_path/libmecab.2.dylib" libMeCab.so

Otherwise your binaries will only work on systems that have mecab already installed at the exact same location.
It is advisable to ask a 3rd party to run the mecab-tests after your updated the binaries in order to be sure that everything is still working (for others too). 
 