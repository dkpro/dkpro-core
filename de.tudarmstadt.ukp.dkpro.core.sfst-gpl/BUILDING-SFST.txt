Change the Makefile of sfst to built it on different platforms as outlined below.

Built the fst-infl2 binary using the command: 'make fst-infl2'

It is not necessary to build the other binaries. E.g. the 'fst-infl2-daemon' does not build when
cross-compiling to win32 on a Linux system.

== OS X ==

- Comment out the READLINE lines. We do not need readline support in DKPro Core
- Note: OS X does not support static binaries. Apple promotes dynamic linking.

== Debian Linux 64 bit ==

- Comment out the READLINE lines. We do not need readline support in DKPro Core
- Set the variable LDFLAGS to '-static'

== Debian Linux 32 bit cross compile (on a 64 bit system) ==

- Comment out the READLINE lines. We do not need readline support in DKPro Core
- Add parameter "-m32" to the CFLAGS variable
- Set the variable LDFLAGS to '-m32'
- Debian Wheezy does not appear to ship a static version of libstdc++ anymore, so we cannot link
  statically.

== Debian Linux Win32 cross compile (on a 64 bit system) ==

- Comment out the READLINE lines. We do not need readline support in DKPro Core
- Set the variables CXX and CC to 'i586-mingw32msvc-g++'
- Set the variable LDFLAGS to '-static-libgcc -static-libstdc++'
