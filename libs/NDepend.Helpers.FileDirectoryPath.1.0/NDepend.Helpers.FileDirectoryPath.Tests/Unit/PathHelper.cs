//-------------------------------------------------------
//
//  These sources are provided by the company SMACCHIA.COM SARL
//  Download the trial edition of our flagship product: http://www.NDepend.com
//  NDepend is a tool for .NET developers.
//  It is a static analyzer that simplifies managing a complex .NET code base
//
//-------------------------------------------------------
using System.Collections.Generic;
using NUnit.Framework;
namespace NDepend.Helpers.FileDirectoryPath {
   [TestFixture]
   public class TestPathHelper {

      [SetUp]
      public void SetUp() {
         NDepend.Test.TestHelper.SetUpTests();
      }




      [Test]
      public void Test_TryRebasePath() {
         DirectoryPathAbsolute rebasedPath = null;


         // originalPath "A:\X1\X2\X3"  validPath "B:\Y1\X1"  result "B:\Y1\X1\X2\X3"   deeperCommonDirName ="X1"
         Assert.IsTrue(PathHelper.TryRebasePath(
            new DirectoryPathAbsolute(@"A:\X1\X2\X3"), new DirectoryPathAbsolute(@"B:\Y1\X1"), out rebasedPath));
         Assert.IsTrue(rebasedPath.Path == @"B:\Y1\X1\X2\X3");

         // originalPath "A:\X1\X2\X3"  validPath "B:\Y1\Y2"  result null               deeperCommonDirName =null
         Assert.IsFalse(PathHelper.TryRebasePath(
            new DirectoryPathAbsolute(@"A:\X1\X2\X3"), new DirectoryPathAbsolute(@"B:\Y1\Y2"), out rebasedPath));

         // originalPath "A:\X1\X2\X3"  validPath "B:\X1\X2"  result "B:\X1\X2\X3"      deeperCommonDirName ="X2
         Assert.IsTrue(PathHelper.TryRebasePath(
            new DirectoryPathAbsolute(@"A:\X1\X2\X3"), new DirectoryPathAbsolute(@"B:\X1\X2"), out rebasedPath));
         Assert.IsTrue(rebasedPath.Path == @"B:\X1\X2\X3");

         // originalPath "A:\X1\X2\X3"  validPath "B:\X2\X3"  result "B:\X2\X3"         deeperCommonDirName ="X3"
         Assert.IsTrue(PathHelper.TryRebasePath(
            new DirectoryPathAbsolute(@"A:\X1\X2\X3"), new DirectoryPathAbsolute(@"B:\X2\X3"), out rebasedPath));
         Assert.IsTrue(rebasedPath.Path == @"B:\X2\X3");


         // originalPath "A:\X1\X2\X3"  validPath "B:\X2"     result "B:\X2\X3"         deeperCommonDirName ="X2"
         Assert.IsTrue(PathHelper.TryRebasePath(
            new DirectoryPathAbsolute(@"A:\X1\X2\X3"), new DirectoryPathAbsolute(@"B:\X2"), out rebasedPath));
         Assert.IsTrue(rebasedPath.Path == @"B:\X2\X3");


         // originalPath "A:\X1\X2\X3"  validPath "B:\X3\X2"  result "B:\X3\X2\X3"      deeperCommonDirName ="X2"
         Assert.IsTrue(PathHelper.TryRebasePath(
            new DirectoryPathAbsolute(@"A:\X1\X2\X3"), new DirectoryPathAbsolute(@"B:\X3\X2"), out rebasedPath));
         Assert.IsTrue(rebasedPath.Path == @"B:\X3\X2\X3");


         // originalPath "A:\X1\X2\X3"  validPath "B:\X3\Y1"  result "B:\X3"            deeperCommonDirName ="X3"
         Assert.IsTrue(PathHelper.TryRebasePath(
            new DirectoryPathAbsolute(@"A:\X1\X2\X3"), new DirectoryPathAbsolute(@"B:\X3\Y1"), out rebasedPath));
         Assert.IsTrue(rebasedPath.Path == @"B:\X3");


         // originalPath "A:\X1\X2\X3"  validPath "B:\X3\Y1\Y2\Y3"  result "B:\X3"            deeperCommonDirName ="X3"
         Assert.IsTrue(PathHelper.TryRebasePath(
            new DirectoryPathAbsolute(@"A:\X1\X2\X3"), new DirectoryPathAbsolute(@"B:\X3\Y1\Y2\Y3"), out rebasedPath));
         Assert.IsTrue(rebasedPath.Path == @"B:\X3");


         // originalPath "A:\X1\X2\X3"  validPath "B:\X3\Y1\Y2\Y3\X3"  result "B:\X3"            deeperCommonDirName ="X3"
         Assert.IsTrue(PathHelper.TryRebasePath(
            new DirectoryPathAbsolute(@"A:\X1\X2\X3"), new DirectoryPathAbsolute(@"B:\X3\Y1\Y2\Y3\X3"), out rebasedPath));
         Assert.IsTrue(rebasedPath.Path == @"B:\X3\Y1\Y2\Y3\X3");


         // originalPath "A:\X1\X2\X3"  validPath "B:\X3\Y1\Y2\Y3\X2"  result "B:\X3"            deeperCommonDirName ="X2"
         Assert.IsTrue(PathHelper.TryRebasePath(
            new DirectoryPathAbsolute(@"A:\X1\X2\X3"), new DirectoryPathAbsolute(@"B:\X3\Y1\Y2\Y3\X2"), out rebasedPath));
         Assert.IsTrue(rebasedPath.Path == @"B:\X3\Y1\Y2\Y3\X2\X3");


         // originalPath "A:\X1\X2\X3"  validPath "B:\X3\Y1\Y2\Y3\X2\Y4"  result "B:\X3"            deeperCommonDirName ="X2"
         Assert.IsTrue(PathHelper.TryRebasePath(
            new DirectoryPathAbsolute(@"A:\X1\X2\X3"), new DirectoryPathAbsolute(@"B:\X3\Y1\Y2\Y3\X2\Y4"), out rebasedPath));
         Assert.IsTrue(rebasedPath.Path == @"B:\X3\Y1\Y2\Y3\X2\X3");


         // originalPath "A:\X1\X2\X3"  validPath "A:\"       result null               deeperCommonDirName =null
         Assert.IsFalse(PathHelper.TryRebasePath(
            new DirectoryPathAbsolute(@"A:\X1\X2\X3"), new DirectoryPathAbsolute(@"A:\"), out rebasedPath));


         // originalPath "A:\X1\X2\X3"  validPath "A:\Y1"     result null               deeperCommonDirName =null
         Assert.IsFalse(PathHelper.TryRebasePath(
            new DirectoryPathAbsolute(@"A:\X1\X2\X3"), new DirectoryPathAbsolute(@"A:\Y1"), out rebasedPath));



         // Test null / empty input path
         Assert.IsFalse(PathHelper.TryRebasePath(
            new DirectoryPathAbsolute(@"A:\X1\X2\X3"), null, out rebasedPath));
         Assert.IsFalse(PathHelper.TryRebasePath(
            new DirectoryPathAbsolute(@"A:\X1\X2\X3"), DirectoryPathAbsolute.Empty, out rebasedPath));
         Assert.IsFalse(PathHelper.TryRebasePath(
            null, new DirectoryPathAbsolute(@"A:\X1\X2\X3"), out rebasedPath));
         Assert.IsFalse(PathHelper.TryRebasePath(
            DirectoryPathAbsolute.Empty, new DirectoryPathAbsolute(@"A:\X1\X2\X3"), out rebasedPath));


      }


      [Test]
      public void Test_TryGetCommonRootDirectory() {
         DirectoryPathAbsolute commonRootDirectory = null;

         // Test when list is null or empty
         Assert.IsFalse(ListOfPathHelper.TryGetCommonRootDirectory(null, out commonRootDirectory));

         List<DirectoryPathAbsolute> list = new List<DirectoryPathAbsolute>();
         Assert.IsFalse(ListOfPathHelper.TryGetCommonRootDirectory(list, out commonRootDirectory));

         // Test when only one dir
         list.Add(new DirectoryPathAbsolute(@"C:\File"));
         Assert.IsTrue(ListOfPathHelper.TryGetCommonRootDirectory(list, out commonRootDirectory));
         Assert.IsTrue(commonRootDirectory.Path == @"C:\File");

         // Test when all dir are the same
         list.Add(new DirectoryPathAbsolute(@"C:\File"));
         list.Add(new DirectoryPathAbsolute(@"C:\File"));
         Assert.IsTrue(ListOfPathHelper.TryGetCommonRootDirectory(list, out commonRootDirectory));
         Assert.IsTrue(commonRootDirectory.Path == @"C:\File");

         // Test when a dir has a wrong drive
         list.Add(new DirectoryPathAbsolute(@"D:\File"));
         Assert.IsFalse(ListOfPathHelper.TryGetCommonRootDirectory(list, out commonRootDirectory));

         // Test when the list contains a null or empty dir
         list.Clear();
         list.Add(new DirectoryPathAbsolute(@"C:\File"));
         list.Add(null);
         Assert.IsFalse(ListOfPathHelper.TryGetCommonRootDirectory(list, out commonRootDirectory));

         list.Clear();
         list.Add(new DirectoryPathAbsolute(@"C:\File"));
         list.Add(DirectoryPathAbsolute.Empty);
         Assert.IsFalse(ListOfPathHelper.TryGetCommonRootDirectory(list, out commonRootDirectory));

         // Test when the common root dir is in the list
         list.Clear();
         list.Add(new DirectoryPathAbsolute(@"C:\File\Debug"));
         list.Add(new DirectoryPathAbsolute(@"C:\File\Debug\Dir1\Dir2"));
         list.Add(new DirectoryPathAbsolute(@"C:\File\Debug\Dir1\Dir2\Dir3"));
         Assert.IsTrue(ListOfPathHelper.TryGetCommonRootDirectory(list, out commonRootDirectory));
         Assert.IsTrue(commonRootDirectory.Path == @"C:\File\Debug");

         list.Add(new DirectoryPathAbsolute(@"C:\File"));
         Assert.IsTrue(ListOfPathHelper.TryGetCommonRootDirectory(list, out commonRootDirectory));
         Assert.IsTrue(commonRootDirectory.Path == @"C:\File");

         list.Add(new DirectoryPathAbsolute(@"C:"));
         Assert.IsTrue(ListOfPathHelper.TryGetCommonRootDirectory(list, out commonRootDirectory));
         Assert.IsTrue(commonRootDirectory.Path == @"C:");

         // Test when the common root dir is not in the list
         list.Clear();
         list.Add(new DirectoryPathAbsolute(@"C:\File\Debug\Dir4"));
         list.Add(new DirectoryPathAbsolute(@"C:\File\Debug\Dir1\Dir2\Dir3"));
         Assert.IsTrue(ListOfPathHelper.TryGetCommonRootDirectory(list, out commonRootDirectory));
         Assert.IsTrue(commonRootDirectory.Path == @"C:\File\Debug");

      }



      [Test]
      public void Test_TryRemoveDllOrExeExtension() {
         Assert.IsTrue(PathHelper.TryRemoveDllOrExeExtension(null) == string.Empty);
         Assert.IsTrue(PathHelper.TryRemoveDllOrExeExtension(string.Empty) == string.Empty);

         Assert.IsTrue(PathHelper.TryRemoveDllOrExeExtension(
            "file.exe") == "file");
         Assert.IsTrue(PathHelper.TryRemoveDllOrExeExtension(
            "file.ExE") == "file");
         Assert.IsTrue(PathHelper.TryRemoveDllOrExeExtension(
            "file.dll") == "file");
         Assert.IsTrue(PathHelper.TryRemoveDllOrExeExtension(
            "file.DLL") == "file");

         Assert.IsTrue(PathHelper.TryRemoveDllOrExeExtension(
            @"C:\dir1\dir2\file.exe") == @"C:\dir1\dir2\file");
         Assert.IsTrue(PathHelper.TryRemoveDllOrExeExtension(
            @"C:\dir1\dir2\file.ExE") == @"C:\dir1\dir2\file");
         Assert.IsTrue(PathHelper.TryRemoveDllOrExeExtension(
            @"C:\dir1\dir2\file.dll") == @"C:\dir1\dir2\file");
         Assert.IsTrue(PathHelper.TryRemoveDllOrExeExtension(
            @"C:\dir1\dir2\file.DLL") == @"C:\dir1\dir2\file");

         Assert.IsTrue(PathHelper.TryRemoveDllOrExeExtension(
            @"C:\dir1\dir2\file.exe  ") == @"C:\dir1\dir2\file");
         Assert.IsTrue(PathHelper.TryRemoveDllOrExeExtension(
            @" C:\dir1\dir2\file.exe") == @"C:\dir1\dir2\file");
         Assert.IsTrue(PathHelper.TryRemoveDllOrExeExtension(
            @"    C:\dir1\dir2\file.exe       ") == @"C:\dir1\dir2\file");


         Assert.IsTrue(PathHelper.TryRemoveDllOrExeExtension(
            @".exe") == @".exe");
         Assert.IsTrue(PathHelper.TryRemoveDllOrExeExtension(
            @".xe") == @".xe");

         Assert.IsTrue(PathHelper.TryRemoveDllOrExeExtension(
            @"C:\dir1\dir2\file.tmp") == @"C:\dir1\dir2\file.tmp");



         Assert.IsTrue(PathHelper.TryRemoveDllOrExeExtension(
            string.Empty) == string.Empty);
         Assert.IsTrue(PathHelper.TryRemoveDllOrExeExtension(
            null) == string.Empty);
      }
   }
}
