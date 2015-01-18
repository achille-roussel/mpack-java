/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Achille Roussel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package mpack;

import java.lang.Boolean;
import java.lang.Byte;
import java.lang.Class;
import java.lang.ClassCastException;
import java.lang.Double;
import java.lang.Float;
import java.lang.IllegalArgumentException;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Short;
import java.lang.String;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MPack {
  private static final int NIL      = 0xc0;
  private static final int FALSE    = 0xc2;
  private static final int TRUE     = 0xc3;
  private static final int BIN8     = 0xc4;
  private static final int BIN16    = 0xc5;
  private static final int BIN32    = 0xc6;
  private static final int EXT8     = 0xc7;
  private static final int EXT16    = 0xc8;
  private static final int EXT32    = 0xc9;
  private static final int FLOAT32  = 0xca;
  private static final int FLOAT64  = 0xcb;
  private static final int UINT8    = 0xcc;
  private static final int UINT16   = 0xcd;
  private static final int UINT32   = 0xce;
  private static final int UINT64   = 0xcf;
  private static final int INT8     = 0xd0;
  private static final int INT16    = 0xd1;
  private static final int INT32    = 0xd2;
  private static final int INT64    = 0xd3;
  private static final int FIXEXT1  = 0xd4;
  private static final int FIXEXT2  = 0xd5;
  private static final int FIXEXT4  = 0xd6;
  private static final int FIXEXT8  = 0xd7;
  private static final int FIXEXT16 = 0xd8;
  private static final int STR8     = 0xd9;
  private static final int STR16    = 0xda;
  private static final int STR32    = 0xdb;
  private static final int ARRAY16  = 0xdc;
  private static final int ARRAY32  = 0xdd;
  private static final int MAP16    = 0xde;
  private static final int MAP32    = 0xdf;
  private static final int FIXARRAY = 0x90;
  private static final int FIXSTR   = 0xa0;
  private static final int FIXMAP   = 0x80;
  private static class FIXNUM {
    private static final int POSITIVE = 0x00;
    private static final int NEGATIVE = 0xe0;
  }

  public static class Extended {
    public int type;
    public byte[] data;

    public Extended() {
      this.type = 0;
      this.data = new byte[0];
    }

    public Extended(int type, byte[] data) {
      this.type = type;
      this.data = data;
    }
  }

  public static class Decoder {
    public final DataInputStream istream;

    public Decoder(InputStream istream) {
      this.istream = new DataInputStream(istream);
    }

    private final Long decodePositiveFixnum(int tag) {
      return (long) ((byte) tag);
    }

    private final Long decodeNegativeFixnum(int tag) {
      return (long) ((byte) tag);
    }

    private final Long decodeUint8() throws IOException {
      return (long) this.istream.readUnsignedByte();
    }

    private final Long decodeUint16() throws IOException {
      return (long) this.istream.readUnsignedShort();
    }

    private final Long decodeUint32() throws IOException {
      final long upper = this.istream.readUnsignedShort();
      final long lower = this.istream.readUnsignedShort();
      return (upper << 16) | lower;
    }

    private final Long decodeUint64() throws IOException {
      return this.istream.readLong();
    }

    private final Long decodeInt8() throws IOException {
      return (long) this.istream.readByte();
    }

    private final Long decodeInt16() throws IOException {
      return (long) this.istream.readShort();
    }

    private final Long decodeInt32() throws IOException {
      return (long) this.istream.readInt();
    }

    private final Long decodeInt64() throws IOException {
      return this.istream.readLong();
    }

    private final Float decodeFloat32() throws IOException {
      return this.istream.readFloat();
    }

    private final Double decodeFloat64() throws IOException {
      return this.istream.readDouble();
    }

    private final String decodeFixStr(int tag) throws IOException {
      final int length = tag & ~FIXSTR;
      final byte[] bytes = new byte[length];
      this.istream.readFully(bytes);
      return new String(bytes, "UTF-8");
    }

    private final String decodeString(int length) throws IOException {
      final byte[] bytes = new byte[length];
      this.istream.readFully(bytes);
      return new String(bytes, "UTF-8");
    }

    private final String decodeStr8() throws IOException {
      return this.decodeString(this.istream.readUnsignedByte());
    }

    private final String decodeStr16() throws IOException {
      return this.decodeString(this.istream.readUnsignedShort());
    }

    private final String decodeStr32() throws IOException {
      return this.decodeString(this.istream.readInt());
    }

    private final byte[] decodeBinary(int length) throws IOException {
      final byte[] bytes = new byte[length];
      this.istream.readFully(bytes);
      return bytes;
    }

    private final byte[] decodeBin8() throws IOException {
      return this.decodeBinary(this.istream.readUnsignedByte());
    }

    private final byte[] decodeBin16() throws IOException {
      return this.decodeBinary(this.istream.readUnsignedShort());
    }

    private final byte[] decodeBin32() throws IOException {
      return this.decodeBinary(this.istream.readInt());
    }

    private final List<?> decodeArray(int length) throws IOException {
      final ArrayList<Object> array = new ArrayList<Object>(length);
      while (length-- != 0) {
        array.add(this.decode());
      }
      return array;
    }

    private final List<?> decodeFixArray(int tag) throws IOException {
      return this.decodeArray(tag & ~FIXARRAY);
    }

    private final List<?> decodeArray16() throws IOException {
      return this.decodeArray(this.istream.readUnsignedShort());
    }

    private final List<?> decodeArray32() throws IOException {
      return this.decodeArray(this.istream.readInt());
    }

    private final Map<?, ?> decodeMap(int length) throws IOException {
      final HashMap<Object, Object> map = new HashMap<Object, Object>();
      while (length-- != 0) {
        final Object key = this.decode();
        final Object val = this.decode();
        map.put(key, val);
      }
      return map;
    }

    private final Map<?, ?> decodeFixMap(int tag) throws IOException {
      return this.decodeMap(tag & ~FIXMAP);
    }

    private final Map<?, ?> decodeMap16() throws IOException {
      return this.decodeMap(this.istream.readUnsignedShort());
    }

    private final Map<?, ?> decodeMap32() throws IOException {
      return this.decodeMap(this.istream.readInt());
    }

    private final Extended decodeExtended(int length) throws IOException {
      final byte[] data = new byte[length];
      final int type = this.istream.readUnsignedByte();
      this.istream.readFully(data);
      return new Extended(type, data);
    }

    private final Extended decodeFixExt1() throws IOException {
      return this.decodeExtended(1);
    }

    private final Extended decodeFixExt2() throws IOException {
      return this.decodeExtended(2);
    }

    private final Extended decodeFixExt4() throws IOException {
      return this.decodeExtended(4);
    }

    private final Extended decodeFixExt8() throws IOException {
      return this.decodeExtended(8);
    }

    private final Extended decodeFixExt16() throws IOException {
      return this.decodeExtended(16);
    }

    private final Extended decodeExt8() throws IOException {
      return this.decodeExtended(this.istream.readUnsignedByte());
    }

    private final Extended decodeExt16() throws IOException {
      return this.decodeExtended(this.istream.readUnsignedShort());
    }

    private final Extended decodeExt32() throws IOException {
      return this.decodeExtended(this.istream.readInt());
    }

    public final Object decode() throws IOException {
      final int tag = this.istream.readUnsignedByte();

      if ((tag & 0x80) == FIXNUM.POSITIVE) {
        return this.decodePositiveFixnum(tag);
      }

      if ((tag & 0xE0) == FIXNUM.NEGATIVE) {
        return this.decodeNegativeFixnum(tag);
      }

      if ((tag & 0xE0) == FIXSTR) {
        return this.decodeFixStr(tag);
      }

      if ((tag & 0xF0) == FIXARRAY) {
        return this.decodeFixArray(tag);
      }

      if ((tag & 0xF0) == FIXMAP) {
        return this.decodeFixMap(tag);
      }

      switch (tag) {
      case NIL:
        return null;

      case TRUE:
        return true;

      case FALSE:
        return false;

      case UINT8:
        return this.decodeUint8();

      case UINT16:
        return this.decodeUint16();

      case UINT32:
        return this.decodeUint32();

      case UINT64:
        return this.decodeUint64();

      case INT8:
        return this.decodeInt8();

      case INT16:
        return this.decodeInt16();

      case INT32:
        return this.decodeInt32();

      case INT64:
        return this.decodeInt64();

      case FLOAT32:
        return this.decodeFloat32();

      case FLOAT64:
        return this.decodeFloat64();

      case STR8:
        return this.decodeStr8();

      case STR16:
        return this.decodeStr16();

      case STR32:
        return this.decodeStr32();

      case BIN8:
        return this.decodeBin8();

      case BIN16:
        return this.decodeBin16();

      case BIN32:
        return this.decodeBin32();

      case ARRAY16:
        return this.decodeArray16();

      case ARRAY32:
        return this.decodeArray32();

      case MAP16:
        return this.decodeMap16();

      case MAP32:
        return this.decodeMap32();

      case FIXEXT1:
        return this.decodeFixExt1();

      case FIXEXT2:
        return this.decodeFixExt2();

      case FIXEXT4:
        return this.decodeFixExt4();

      case FIXEXT8:
        return this.decodeFixExt8();

      case FIXEXT16:
        return this.decodeFixExt16();

      case EXT8:
        return this.decodeExt8();

      case EXT16:
        return this.decodeExt16();

      case EXT32:
        return this.decodeExt32();

      default:
        throw new IOException("MPack: decoder found unknown tag: " + tag);
      }
    }

    public final boolean decodeBoolean() throws IOException {
      return (Boolean) this.decode();
    }

    public final byte decodeByte() throws IOException {
      return this.decodeNumber().byteValue();
    }

    public final short decodeShort() throws IOException {
      return this.decodeNumber().shortValue();
    }

    public final int decodeInt() throws IOException {
      return this.decodeNumber().intValue();
    }

    public final long decodeLong() throws IOException {
      return this.decodeNumber().longValue();
    }

    public final float decodeFloat() throws IOException {
      return this.decodeNumber().floatValue();
    }

    public final double decodeDouble() throws IOException {
      return this.decodeNumber().doubleValue();
    }

    public final Number decodeNumber() throws IOException {
      return (Number) this.decode();
    }

    public final String decodeString() throws IOException {
      return (String) this.decode();
    }

    public final byte[] decodeBinary() throws IOException {
      return (byte[]) this.decode();
    }

    public final Extended decodeExtended() throws IOException {
      return (Extended) this.decode();
    }

    @SuppressWarnings("unchecked")
    public final <T> List<T> decodeList(Class<T> elementClass) throws IOException {
      final List<?> list = (List) this.decode();
      for (Object obj : list) {
        if (!elementClass.isAssignableFrom(obj.getClass())) {
          throw new ClassCastException("unexpected type found while decoding list: " + obj.getClass().toString());
        }
      }
      return (List<T>) list;
    }

    @SuppressWarnings("unchecked")
    public final <K, V> Map<K, V> decodeMap(Class<K> keyClass, Class<V> valueClass) throws IOException {
      final Map<?, ?> map = (Map) this.decode();
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        final Object key = entry.getKey();
        final Object value = entry.getValue();
        if (!keyClass.isAssignableFrom(key.getClass())) {
          throw new ClassCastException("unexpected type found while decoding map key: " + key.getClass().toString());
        }
        if (!valueClass.isAssignableFrom(value.getClass())) {
          throw new ClassCastException("unexpected type found while decoding map value: " + value.getClass().toString());
        }
      }
      return (Map<K, V>) map;
    }
  }

  public static class Encoder {
    public final DataOutputStream ostream;

    public Encoder(OutputStream ostream) {
      this.ostream = new DataOutputStream(ostream);
    }

    private final void encodeNil() throws IOException {
      this.ostream.writeByte(NIL);
    }

    private final void encodeTrue() throws IOException {
      this.ostream.writeByte(TRUE);
    }

    private final void encodeFalse() throws IOException {
      this.ostream.writeByte(FALSE);
    }

    private final void encodePositiveFixnum(long object) throws IOException {
      this.ostream.writeByte((int) object);
    }

    private final void encodeNegativeFixnum(long object) throws IOException {
      this.ostream.writeByte((int) object);
    }

    private final void encodeUint8(long object) throws IOException {
      this.ostream.writeByte(UINT8);
      this.ostream.writeByte((int) object);
    }

    private final void encodeUint16(long object) throws IOException {
      this.ostream.writeByte(UINT16);
      this.ostream.writeShort((int) object);
    }

    private final void encodeUint32(long object) throws IOException {
      this.ostream.writeByte(UINT32);
      this.ostream.writeInt((int) object);
    }

    private final void encodeUint64(long object) throws IOException {
      this.ostream.writeByte(UINT64);
      this.ostream.writeLong(object);
    }

    private final void encodeInt8(long object) throws IOException {
      this.ostream.writeByte(INT8);
      this.ostream.writeByte((int) object);
    }

    private final void encodeInt16(long object) throws IOException {
      this.ostream.writeByte(INT16);
      this.ostream.writeShort((int) object);
    }

    private final void encodeInt32(long object) throws IOException {
      this.ostream.writeByte(INT32);
      this.ostream.writeInt((int) object);
    }

    private final void encodeInt64(long object) throws IOException {
      this.ostream.writeByte(INT64);
      this.ostream.writeLong(object);
    }

    private final void encodeFloat32(float object) throws IOException {
      this.ostream.writeByte(FLOAT32);
      this.ostream.writeFloat(object);
    }

    private final void encodeFloat64(double object) throws IOException {
      this.ostream.writeByte(FLOAT64);
      this.ostream.writeDouble(object);
    }

    private final void encodeFixStr(byte[] object) throws IOException {
      this.ostream.writeByte(FIXSTR | object.length);
      this.ostream.write(object);
    }

    private final void encodeStr8(byte[] object) throws IOException {
      this.ostream.writeByte(STR8);
      this.ostream.writeByte(object.length);
      this.ostream.write(object);
    }

    private final void encodeStr16(byte[] object) throws IOException {
      this.ostream.writeByte(STR16);
      this.ostream.writeShort(object.length);
      this.ostream.write(object);
    }

    private final void encodeStr32(byte[] object) throws IOException {
      this.ostream.writeByte(STR32);
      this.ostream.writeInt(object.length);
      this.ostream.write(object);
    }

    private final void encodeBin8(byte[] object) throws IOException {
      this.ostream.writeByte(BIN8);
      this.ostream.writeByte(object.length);
      this.ostream.write(object);
    }

    private final void encodeBin16(byte[] object) throws IOException {
      this.ostream.writeByte(BIN16);
      this.ostream.writeShort(object.length);
      this.ostream.write(object);
    }

    private final void encodeBin32(byte[] object) throws IOException {
      this.ostream.writeByte(BIN32);
      this.ostream.writeInt(object.length);
      this.ostream.write(object);
    }

    private final void encodeFixArray(int length) throws IOException {
      this.ostream.writeByte(FIXARRAY | length);
    }

    private final void encodeArray16(int length) throws IOException {
      this.ostream.writeByte(ARRAY16);
      this.ostream.writeShort(length);
    }

    private final void encodeArray32(int length) throws IOException {
      this.ostream.writeByte(ARRAY32);
      this.ostream.writeInt(length);
    }

    private final void encodeFixMap(int length) throws IOException {
      this.ostream.writeByte(FIXMAP | length);
    }

    private final void encodeMap16(int length) throws IOException {
      this.ostream.writeByte(MAP16);
      this.ostream.writeShort(length);
    }

    private final void encodeMap32(int length) throws IOException {
      this.ostream.writeByte(MAP32);
      this.ostream.writeInt(length);
    }

    private final void encodeBoolean(boolean object) throws IOException {
      if (object) {
        this.encodeTrue();
      }
      else {
        this.encodeFalse();
      }
    }

    private final void encodeInteger(byte object) throws IOException {
      this.encodeInteger((long) object);
    }

    private final void encodeInteger(short object) throws IOException {
      this.encodeInteger((long) object);
    }
    
    private final void encodeInteger(int object) throws IOException {
      this.encodeInteger((long) object);
    }

    private final void encodeInteger(long object) throws IOException {
      if (object >= 0L) {
        if (object <= 127L) {
          this.encodePositiveFixnum(object);
        }
        else if (object <= 255L) {
          this.encodeUint8(object);
        }
        else if (object <= 65535L) {
          this.encodeUint16(object);
        }
        else if (object <= 4294967295L) {
          this.encodeUint32(object);
        }
        else {
          this.encodeUint64(object);
        }
      }
      else {
        if (object >= -31L) {
          this.encodeNegativeFixnum(object);
        }
        else if (object >= -128L) {
          this.encodeInt8(object);
        }
        else if (object >= -32768L) {
          this.encodeInt16(object);
        }
        else if (object >= -2147483648L) {
          this.encodeInt32(object);
        }
        else {
          this.encodeInt64(object);
        }
      }
    }

    private final void encodeFloat(float object) throws IOException {
      this.encodeFloat32(object);
    }

    private final void encodeFloat(double object) throws IOException {
      this.encodeFloat64(object);
    }

    private final void encodeString(byte[] object) throws IOException {
      if (object.length <= 15) {
        this.encodeFixStr(object);
      }
      else if (object.length <= 255) {
        this.encodeStr8(object);
      }
      else if (object.length <= 65535) {
        this.encodeStr16(object);
      }
      else {
        this.encodeStr32(object);
      }
    }

    private final void encodeString(String object) throws IOException {
      this.encodeString(object.getBytes("UTF-8"));
    }

    private final void encodeBinary(byte[] object) throws IOException {
      if (object.length <= 255) {
        this.encodeBin8(object);
      }
      else if (object.length <= 65535) {
        this.encodeBin16(object);
      }
      else {
        this.encodeBin32(object);
      }
    }

    private final <T> void encodeArray(List<T> object) throws IOException {
      final int length = object.size();
      if (length <= 15) {
        this.encodeFixArray(length);
      }
      else if (length <= 65535) {
        this.encodeArray16(length);
      }
      else {
        this.encodeArray32(length);
      }
      for (T item : object) {
        this.encode(item);
      }
    }

    private final <K, V> void encodeMap(Map<K, V> object) throws IOException {
      final int length = object.size();
      if (length <= 15) {
        this.encodeFixMap(length);
      }
      else if (length <= 65535) {
        this.encodeMap16(length);
      }
      else {
        this.encodeMap32(length);
      }
      for (Map.Entry<K, V> entry : object.entrySet()) {
        this.encode(entry.getKey());
        this.encode(entry.getValue());
      }
    }

    private final void encodeExtendedDataType(Extended object) throws IOException {
      this.ostream.writeByte(object.type);
      this.ostream.write(object.data);
    }

    private final void encodeFixExt1(Extended object) throws IOException {
      this.ostream.writeByte(FIXEXT1);
      this.encodeExtendedDataType(object);
    }

    private final void encodeFixExt2(Extended object) throws IOException {
      this.ostream.writeByte(FIXEXT2);
      this.encodeExtendedDataType(object);
    }

    private final void encodeFixExt4(Extended object) throws IOException {
      this.ostream.writeByte(FIXEXT4);
      this.encodeExtendedDataType(object);
    }

    private final void encodeFixExt8(Extended object) throws IOException {
      this.ostream.writeByte(FIXEXT8);
      this.encodeExtendedDataType(object);
    }

    private final void encodeFixExt16(Extended object) throws IOException {
      this.ostream.writeByte(FIXEXT16);
      this.encodeExtendedDataType(object);
    }

    private final void encodeExt8(Extended object) throws IOException {
      this.ostream.writeByte(EXT8);
      this.ostream.writeByte(object.data.length);
      this.encodeExtendedDataType(object);
    }

    private final void encodeExt16(Extended object) throws IOException {
      this.ostream.writeByte(EXT16);
      this.ostream.writeShort(object.data.length);
      this.encodeExtendedDataType(object);
    }

    private final void encodeExt32(Extended object) throws IOException {
      this.ostream.writeByte(EXT32);
      this.ostream.writeInt(object.data.length);
      this.encodeExtendedDataType(object);
    }

    private final void encodeExtended(Extended object) throws IOException {
      switch (object.data.length) {
      case 1:
        this.encodeFixExt1(object);
        break;
        
      case 2:
        this.encodeFixExt2(object);
        break;
        
      case 4:
        this.encodeFixExt4(object);
        break;
        
      case 8:
        this.encodeFixExt8(object);
        break;

      case 16:
        this.encodeFixExt16(object);
        break;

      default:
        if (object.data.length <= 255) {
          this.encodeExt8(object);
        }
        else if (object.data.length <= 65535) {
          this.encodeExt16(object);
        }
        else {
          this.encodeExt32(object);
        }
      }
    }

    public final void encode(boolean object) throws IOException {
      this.encodeBoolean(object);
    }

    public final void encode(byte object) throws IOException {
      this.encodeInteger(object);
    }

    public final void encode(short object) throws IOException {
      this.encodeInteger(object);
    }

    public final void encode(int object) throws IOException {
      this.encodeInteger(object);
    }

    public final void encode(long object) throws IOException {
      this.encodeInteger(object);
    }

    public final void encode(float object) throws IOException {
      this.encodeFloat(object);
    }

    public final void encode(double object) throws IOException {
      this.encodeFloat(object);
    }

    public final void encode(Boolean object) throws IOException {
      this.encodeBoolean(object);
    }

    public final void encode(Byte object) throws IOException {
      this.encodeInteger(object);
    }

    public final void encode(Short object) throws IOException {
      this.encodeInteger(object);
    }

    public final void encode(Integer object) throws IOException {
      this.encodeInteger(object);
    }

    public final void encode(Long object) throws IOException {
      this.encodeInteger(object);
    }

    public final void encode(Float object) throws IOException {
      this.encodeFloat(object);
    }

    public final void encode(Double object) throws IOException {
      this.encodeFloat(object);
    }

    public final void encode(String object) throws IOException {
      this.encodeString(object);
    }

    public final void encode(byte[] object) throws IOException {
      this.encodeBinary(object);
    }

    public final <T> void encode(List<T> object) throws IOException {
      this.encodeArray(object);
    }

    public final <K, V> void encode(Map<K, V> object) throws IOException {
      this.encodeMap(object);
    }

    public final void encode(Extended object) throws IOException {
      this.encodeExtended(object);
    }

    public final void encode(Object object) throws IOException {
      if (object == null) {
        this.encodeNil();
      }
      else if (object instanceof Boolean) {
        this.encodeBoolean((Boolean) object);
      }
      else if (object instanceof Byte) {
        this.encodeInteger((Byte) object);
      }
      else if (object instanceof Short) {
        this.encodeInteger((Short) object);
      }
      else if (object instanceof Integer) {
        this.encodeInteger((Integer) object);
      }
      else if (object instanceof Long) {
        this.encodeInteger((Long) object);
      }
      else if (object instanceof Float) {
        this.encodeFloat((Float) object);
      }
      else if (object instanceof Double) {
        this.encodeFloat((Double) object);
      }
      else if (object instanceof String) {
        this.encodeString((String) object);
      }
      else if (object instanceof byte[]) {
        this.encodeBinary((byte[]) object);
      }
      else if (object instanceof List<?>) {
        this.encodeArray((List<?>) object);
      }
      else if (object instanceof Map<?, ?>) {
        this.encodeMap((Map<?, ?>) object);
      }
      else if (object instanceof Extended) {
        this.encodeExtended((Extended) object);
      }
      else {
        throw new IllegalArgumentException("MPack: no encoding available for objects of type " + object.getClass().toString());
      }
    }

    public final void flush() throws IOException {
      this.ostream.flush();
    }

  }

  private MPack() { }

  public static Object decode(byte[] bytes) throws IOException {
    return decode(new ByteArrayInputStream(bytes));
  }

  public static Object decode(InputStream istream) throws IOException {
    return (new Decoder(istream)).decode();
  }
  
  public static byte[] encode(Object object) throws IOException {
    final ByteArrayOutputStream ostream = new ByteArrayOutputStream();
    encode(object, ostream);
    return ostream.toByteArray();
  }

  public static void encode(Object object, OutputStream ostream) throws IOException {
    final Encoder encoder = new Encoder(ostream);
    encoder.encode(object);
    encoder.flush();
  }
}
