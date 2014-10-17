/*
 * Copyright 1998-2009 University Corporation for Atmospheric Research/Unidata
 *
 * Portions of this software were developed by the Unidata Program at the
 * University Corporation for Atmospheric Research.
 *
 * Access and use of this software shall impose the following obligations
 * and understandings on the user. The user is granted the right, without
 * any fee or cost, to use, copy, modify, alter, enhance and distribute
 * this software, and any derivative works thereof, and its supporting
 * documentation for any purpose whatsoever, provided that this entire
 * notice appears in all copies of the software, derivative works and
 * supporting documentation.  Further, UCAR requests that the user credit
 * UCAR/Unidata in any publications that result from the use of this
 * software or in any product that includes this software. The names UCAR
 * and/or Unidata, however, may not be used in any advertising or publicity
 * to endorse or promote any products or commercial entity unless specific
 * written permission is obtained from UCAR/Unidata. The user also
 * understands that UCAR/Unidata is not obligated to provide the user with
 * any support, consulting, training or assistance of any kind with regard
 * to the use, operation and performance of this software nor to provide
 * the user with any updates, revisions, new versions or "bug fixes."
 *
 * THIS SOFTWARE IS PROVIDED BY UCAR/UNIDATA "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL UCAR/UNIDATA BE LIABLE FOR ANY SPECIAL,
 * INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING
 * FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 * NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION
 * WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package ucar.nc2.iosp.hdf5;

import java.io.*;

import org.junit.*;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.NCdump;
import ucar.ma2.Section;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Array;
import ucar.nc2.util.DebugFlagsImpl;
import ucar.unidata.test.util.TestDir;

/**
 * Read all hdf4 files in cdmUnitTestDir + "formats/hdf5/"
 * *
 */

public class TestH5read {

  @AfterClass
  static public void after() {
    H5header.setDebugFlags(new DebugFlagsImpl(""));  // make sure debug flags are off
  }

  String testDir = TestDir.cdmUnitTestDir + "formats/hdf5/";


  @org.junit.Test
  public void testH5data() throws IOException {
    H5header.setDebugFlags(new ucar.nc2.util.DebugFlagsImpl(""));

    //TestAll.readAllDir (TestH5.testDir, new FileFilter() {
    TestDir.readAllDir(testDir, new FileFilter() {
      public boolean accept(File file) {
        String name = file.getPath();
        return (name.endsWith(".h5") || name.endsWith(".H5") || name.endsWith(".he5") || name.endsWith(".nc"));
      }
    });

  }

  public void problemV() throws IOException {
    H5header.setDebugFlags(new ucar.nc2.util.DebugFlagsImpl("H5header/header"));
    String filename = testDir + "ssec-h5/MYD04_L2.A2006188.1830.005.2006194121515.hdf";
    NetcdfFile ncfile = NetcdfFile.open(filename);
    Variable v = ncfile.findVariable("/U-MARF/EPS/IASI_xxx_1C/DATA/SPECT_LAT_ARRAY");
    Array data = v.read();
    System.out.println("\n**** testReadNetcdf4 done\n\n" + ncfile);
    NCdump.printArray(data, "primary_cloud", System.out, null);
    ncfile.close();
  }

  public void utestProblem() {
    //H5header.setDebugFlags(new ucar.nc2.util.DebugFlagsImpl("H5header/header"));
    //readAllData( "C:\\data\\testdata2\\hdf5\\samples\\opaque.h5");
    readAllData(testDir + "compressCompoundBarrowdale.h5");
  }

  public static void readAllDir(String dirName) {
    System.out.println("---------------Reading directory " + dirName);
    File allDir = new File(dirName);
    File[] allFiles = allDir.listFiles();
    if (null == allFiles) {
      System.out.println("---------------INVALID " + dirName);
      return;
    }

    for (int i = 0; i < allFiles.length; i++) {
      String name = allFiles[i].getAbsolutePath();
      if (name.endsWith(".h5") || name.endsWith(".H5") || name.endsWith(".he5") || name.endsWith(".nc"))
        readAllData(name);
    }

    for (int i = 0; i < allFiles.length; i++) {
      File f = allFiles[i];
      if (f.isDirectory())
        readAllDir(allFiles[i].getAbsolutePath());
    }

  }

  static public void readAllData(String filename) {
    System.out.println("\n------Reading filename " + filename);
    try {
      NetcdfFile ncfile = TestH5.open(filename);
      //System.out.println("\n"+ncfile);

      for (Variable v : ncfile.getVariables()) {
        if (v.getSize() > max_size) {
          Section s = makeSubset(v);
          System.out.println("  Try to read variable " + v.getNameAndDimensions() + " size= " + v.getSize() + " section= " + s);
          v.read(s);
        } else {
          System.out.println("  Try to read variable " + v.getNameAndDimensions() + " size= " + v.getSize());
          v.read();
        }
      }
      ncfile.close();
    } catch (Exception e) {
      e.printStackTrace();
      //assert false;
    }
  }

  static int max_size = 1000 * 1000 * 10;

  static Section makeSubset(Variable v) throws InvalidRangeException {
    int[] shape = v.getShape();
    shape[0] = 1;
    Section s = new Section(shape);
    long size = s.computeSize();
    shape[0] = (int) Math.max(1, max_size / size);
    return new Section(shape);
  }

}
