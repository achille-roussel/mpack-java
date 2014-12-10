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

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import java.io.IOException;
import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class tests {

  private String makeString(int length) {
    final StringBuilder s = new StringBuilder(length);
    for (int i = 0; i != length; ++i) {
      s.append('a');
    }
    return s.toString();
  }

  private byte[] makeBinary(int length) {
    final byte[] bytes = new byte[length];
    for (int i = 0; i != length; ++i) {
      bytes[i] = (byte) (i % 256);
    }
    return bytes;
  }

  private List<?> makeList(int length) {
    final ArrayList<Long> list = new ArrayList<Long>(length);
    for (int i = 0; i != length; ++i) {
      list.add((long) i);
    }
    return list;
  }

  private Map<?, ?> makeMap(int length) {
    final HashMap<String, Long> map = new HashMap<String, Long>(length);
    for (int i = 0; i != length; ++i) {
      map.put("key." + i, (long) i);
    }
    return map;
  }

  private void testEncodeDecode(Object object) throws IOException {
    testEncodeDecode(object, object);
  }

  private void testEncodeDecode(Object object, Object result) throws IOException {
    assertEquals(result, mpack.decode(mpack.encode(object)));
  }

  @Test
  public void testEncodeDecodeNil() throws IOException {
    testEncodeDecode(null);
  }

  @Test
  public void testEncodeDecodeTrue() throws IOException {
    testEncodeDecode(true);
  }

  @Test
  public void testEncodeDecodeFalse() throws IOException {
    testEncodeDecode(false);
  }

  @Test
  public void testEncodeDecodePositiveFixnum() throws IOException {
    testEncodeDecode(0L);
    testEncodeDecode(1L);
    testEncodeDecode(127L);
  }

  @Test
  public void testEncodeDecodeNegativeFixnum() throws IOException {
    testEncodeDecode(-1L);
    testEncodeDecode(-31L);
  }

  @Test
  public void testEncodeDecodeUint8() throws IOException {
    testEncodeDecode(128L);
    testEncodeDecode(255L);
  }

  @Test
  public void testEncodeDecodeUint16() throws IOException {
    testEncodeDecode(256L);
    testEncodeDecode(65535L);
  }

  @Test
  public void testEncodeDecodeUint32() throws IOException {
    testEncodeDecode(65536L);
    testEncodeDecode(4294967295L);
  }

  @Test
  public void testEncodeDecodeUint64() throws IOException {
    testEncodeDecode(4294967296L);
    testEncodeDecode(9223372036854775807L);
  }

  @Test
  public void testEncodeDecodeInt8() throws IOException {
    testEncodeDecode(-32L);
    testEncodeDecode(-128L);
  }

  @Test
  public void testEncodeDecodeInt16() throws IOException {
    testEncodeDecode(-129L);
    testEncodeDecode(-32768L);
  }

  @Test
  public void testEncodeDecodeInt32() throws IOException {
    testEncodeDecode(-32769L);
    testEncodeDecode(-2147483648L);
  }

  @Test
  public void testEncodeDecodeInt64() throws IOException {
    testEncodeDecode(-2147483649L);
    testEncodeDecode(-9223372036854775808L);
  }

  @Test
  public void testEncodeDecodeFloat32() throws IOException {
    testEncodeDecode(1.234f);
  }

  @Test
  public void testEncodeDecodeFloat64() throws IOException {
    testEncodeDecode(1.234);
  }

  @Test
  public void testEncodeDecodeFixstr() throws IOException {
    testEncodeDecode("");
    testEncodeDecode("Hello\u2022World!");
  }

  @Test
  public void testEncodeDecodeStr8() throws IOException {
    testEncodeDecode(makeString(20));
    testEncodeDecode(makeString(255));
  }

  @Test
  public void testEncodeDecodeStr16() throws IOException {
    testEncodeDecode(makeString(1000));
  }

  @Test
  public void testEncodeDecodeStr32() throws IOException {
    testEncodeDecode(makeString(100000));
  }
  
  @Test
  public void testEncodeDecodeBin8() throws IOException {
    final byte[] base = makeBinary(20);
    final byte[] copy = (byte[]) mpack.decode(mpack.encode(base));
    assertEquals(true, Arrays.equals(base, copy));
  }

  @Test
  public void testEncodeDecodeBin16() throws IOException {
    final byte[] base = makeBinary(1000);
    final byte[] copy = (byte[]) mpack.decode(mpack.encode(base));
    assertEquals(true, Arrays.equals(base, copy));
  }

  @Test
  public void testEncodeDecodeBin32() throws IOException {
    final byte[] base = makeBinary(100000);
    final byte[] copy = (byte[]) mpack.decode(mpack.encode(base));
    assertEquals(true, Arrays.equals(base, copy));
  }

  @Test
  public void testEncodeDecodeFixArray() throws IOException {
    final List<?> base = makeList(10);
    final List<?> copy = (List<?>) mpack.decode(mpack.encode(base));

    final Iterator<?> it = base.iterator();
    final Iterator<?> jt = copy.iterator();

    while (it.hasNext() && jt.hasNext()) {
      assertEquals(it.next(), jt.next());
    }

    assertEquals(it.hasNext(), jt.hasNext());
  }

  @Test
  public void testEncodeDecodeArray16() throws IOException {
    final List<?> base = makeList(1000);
    final List<?> copy = (List<?>) mpack.decode(mpack.encode(base));

    final Iterator<?> it = base.iterator();
    final Iterator<?> jt = copy.iterator();

    while (it.hasNext() && jt.hasNext()) {
      assertEquals(it.next(), jt.next());
    }

    assertEquals(it.hasNext(), jt.hasNext());
  }

  @Test
  public void testEncodeDecodeArray32() throws IOException {
    final List<?> base = makeList(100000);
    final List<?> copy = (List<?>) mpack.decode(mpack.encode(base));

    final Iterator<?> it = base.iterator();
    final Iterator<?> jt = copy.iterator();

    while (it.hasNext() && jt.hasNext()) {
      assertEquals(it.next(), jt.next());
    }

    assertEquals(it.hasNext(), jt.hasNext());
  }

  @Test
  public void testEncodeDecodeFixMap() throws IOException {
    final Map<?, ?> base = makeMap(10);
    final Map<?, ?> copy = (Map<?, ?>) mpack.decode(mpack.encode(base));

    assertEquals(base.size(), copy.size());

    for (Object key : copy.keySet()) {
      final Object v1 = base.get(key);
      final Object v2 = copy.get(key);
      assertEquals(v1, v2);
    }
  }

  @Test
  public void testEncodeDecodeFixExt1() throws IOException {
    final mpack.Extended base = new mpack.Extended(42, new byte[1]);
    final mpack.Extended copy = (mpack.Extended) mpack.decode(mpack.encode(base));

    assertEquals(base.type, copy.type);
    assertEquals(base.data.length, copy.data.length);
  }

  @Test
  public void testEncodeDecodeFixExt2() throws IOException {
    final mpack.Extended base = new mpack.Extended(42, new byte[2]);
    final mpack.Extended copy = (mpack.Extended) mpack.decode(mpack.encode(base));

    assertEquals(base.type, copy.type);
    assertEquals(base.data.length, copy.data.length);
  }

  @Test
  public void testEncodeDecodeFixExt4() throws IOException {
    final mpack.Extended base = new mpack.Extended(42, new byte[4]);
    final mpack.Extended copy = (mpack.Extended) mpack.decode(mpack.encode(base));

    assertEquals(base.type, copy.type);
    assertEquals(base.data.length, copy.data.length);
  }

  @Test
  public void testEncodeDecodeFixExt8() throws IOException {
    final mpack.Extended base = new mpack.Extended(42, new byte[8]);
    final mpack.Extended copy = (mpack.Extended) mpack.decode(mpack.encode(base));

    assertEquals(base.type, copy.type);
    assertEquals(base.data.length, copy.data.length);
  }

  @Test
  public void testEncodeDecodeFixExt16() throws IOException {
    final mpack.Extended base = new mpack.Extended(42, new byte[16]);
    final mpack.Extended copy = (mpack.Extended) mpack.decode(mpack.encode(base));

    assertEquals(base.type, copy.type);
    assertEquals(base.data.length, copy.data.length);
  }

  @Test
  public void testEncodeDecodeExt8() throws IOException {
    final mpack.Extended base = new mpack.Extended(42, new byte[20]);
    final mpack.Extended copy = (mpack.Extended) mpack.decode(mpack.encode(base));

    assertEquals(base.type, copy.type);
    assertEquals(base.data.length, copy.data.length);
  }

  @Test
  public void testEncodeDecodeExt16() throws IOException {
    final mpack.Extended base = new mpack.Extended(42, new byte[1000]);
    final mpack.Extended copy = (mpack.Extended) mpack.decode(mpack.encode(base));

    assertEquals(base.type, copy.type);
    assertEquals(base.data.length, copy.data.length);
  }

  @Test
  public void testEncodeDecodeExt32() throws IOException {
    final mpack.Extended base = new mpack.Extended(42, new byte[100000]);
    final mpack.Extended copy = (mpack.Extended) mpack.decode(mpack.encode(base));

    assertEquals(base.type, copy.type);
    assertEquals(base.data.length, copy.data.length);
  }

}

