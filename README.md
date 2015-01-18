mpack-java
==========

mpack-java is a simple Java implementation of the MessagePack serialization
format.
mpack-java aims to provide a simple API that comes in a single file so it is
easy to integrate to any project and keep the code size very short to avoid
bloating mobile app executables.

Install
-------

To install mpack-java simple copy mpack/mpack.java in your project then import
the module:
```java
import mpack.MPack;

...
```

Usage
-----

Here's a quick example:
```java
// All encoding operations are made through the MPack.encode static method,
// there are overloads for most common Java types that have direct mapping
// to MessagePack types.
byte[] bytes = MPack.encode("Hello World!");

// To convert a serialized value back into a Java object simply use the
// MPack.decode static method.
// Note that you have to cast the result because the method returns a
// java.lang.Object instance.
String hello = (String) MPack.decode(bytes);
```

It is also possible to serialize and deserialize sequences of objects using
the MPack.Encoder and MPack.Decoder classes, these types work on standard
input and output streams so they can easily be integrated to Java projects:
```java
ByteArrayOutputStream ostream = new ByteArrayOutputStream();
MPack.Encoder encoder = new MPack.Encoder(ostream);

// Encode a message made of 3 objects.
encoder.encode(42);
encoder.encode(true);
encoder.encode("hello");
encoder.flush();

ByteArrayInputStream istream = new ByteArrayInputStream(ostream.toByteArray());
MPack.Decoder decoder = new MPack.Decoder(istream);

// Integral values are always returned as java.lang.Long objects.
Long obj1 = (Long) decoder.decode();

// Booleans are returned as java.lang.Boolean objects.
Boolean obj2 = (Boolean) decoder.decode();

// And obviously UTF-8 strings are returned as java.lang.String objects.
String obj3 = (String) decoder.decode(); 
```

To avoid casting the value returned by the generic decode method everytime the
MPack.Decoder class also exposes helper methods that automatically handle type
conversions, for example you can call:
```java
int x = decoder.decodeInt();
```

MessagePack Extensions
----------------------

MessagePack supports encoding extended types to embed arbitrary data into a
serialized message. Here's a quick example showing how to use extended types
with mpack-java:
```java
byte[] data = new byte[...]; // some pre-serialized data
int type = 42; // must be an integer in the range [-128; 127]

// Encode the given binary data as an extended data type using MessagePack
// extension support.
byte[] bytes = MPack.encode(new MPack.Extended(type, data));

// Decode works like any other data type, the returned object has two fields
// named data and type.
MPack.Extended object = (MPack.Extended) MPack.decode(bytes);

System.out.println(object.type); // 42
System.out.println(object.data); // byte[]
```

Test Suite
----------

mpack-java comes with a test suite that runs with JUnit. If you're interested in
running the test suite you can find instructions about how to setup JUnit at  
https://github.com/junit-team/junit/wiki/Download-and-Install

Then simply run:
```
javac -cp .:./junit.jar:./hamcrest-core.jar mpack/*.java
java -cp .:./junit.jar:./hamcrest-core.jar org.junit.runner.JUnitCore mpack.MPackTests
```
