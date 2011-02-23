//-------------------------------------------------------
//
//  These sources are provided by the company SMACCHIA.COM SARL
//  Download the trial edition of our flagship product: http://www.NDepend.com
//  NDepend is a tool for .NET developers.
//  It is a static analyzer that simplifies managing a complex .NET code base
//
//-------------------------------------------------------
using System;
using System.Collections.Generic;
using System.Text;
using System.IO;
using System.Diagnostics;

namespace NDepend.Test.Unit {
   public static class DirForTest {
      public static string Dir {
         get {
            string executingAssemblyLocation = System.Reflection.Assembly.GetExecutingAssembly().Location;
            int index = executingAssemblyLocation.LastIndexOf(Path.DirectorySeparatorChar);
            Debug.Assert(index != -1);
            return executingAssemblyLocation.Substring(0,index+1) + "DirForTest";
         }
      }

      public static void EnsureDirForTestExistAndEmpty() {
         string dir = Dir;
         if (!Directory.Exists(dir)) {
            Directory.CreateDirectory(dir);
         }
         if (Directory.GetDirectories(dir).Length > 0 || Directory.GetFiles(dir).Length > 0) {
            Directory.Delete(dir, true);
            Directory.CreateDirectory(dir);
         }
      }
      public static void Delete() {
         string dir = Dir;
         if (Directory.Exists(dir)) {
            Directory.Delete(dir, true);
         }
      }

      public static string ExecutingAssemblyFilePathInDirForTest {
         get {
            string executingAssemblyFileLocation = System.Reflection.Assembly.GetExecutingAssembly().Location;
            return Dir + Path.DirectorySeparatorChar + Path.GetFileName(executingAssemblyFileLocation);
         }
      }

      public static void CopyExecutingAssemblyFileInDirForTest() {
         File.Copy(System.Reflection.Assembly.GetExecutingAssembly().Location,
            ExecutingAssemblyFilePathInDirForTest);
      }
   }
}
