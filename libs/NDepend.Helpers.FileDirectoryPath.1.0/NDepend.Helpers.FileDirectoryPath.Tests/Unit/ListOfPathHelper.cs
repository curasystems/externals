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
using System;
namespace NDepend.Helpers.FileDirectoryPath {
   [TestFixture]
   public class TestListOfPathHelper {

      [SetUp]
      public void SetUp() {
         NDepend.Test.TestHelper.SetUpTests();
      }

      [Test]
      public void Test_ListOfPathsEquals1() {
         List<DirectoryPath> list1 = null;
         List<DirectoryPath> list2 = null;
         Assert.IsTrue(ListOfPathHelper.ListOfPathsEquals(list1, list2));
      }

      [Test]
      public void Test_ListOfPathsEquals2() {
         List<FilePathAbsolute> list1 = null;
         List<FilePathAbsolute> list2 = new List<FilePathAbsolute>();
         Assert.IsFalse(ListOfPathHelper.ListOfPathsEquals(list1, list2));
      }

      [Test]
      public void Test_ListOfPathsEquals3() {
         List<FilePathAbsolute> list1 = new List<FilePathAbsolute>();
         List<FilePathAbsolute> list2 = new List<FilePathAbsolute>();
         list2.Add(new FilePathAbsolute(@"C:\Dir1\File.txt"));
         Assert.IsFalse(ListOfPathHelper.ListOfPathsEquals(list1, list2));
      }

      [Test]
      public void Test_ListOfPathsEquals4() {
         List<BasePath> list1 = new List<BasePath>();
         list1.Add(new FilePathAbsolute(@"C:\Dir1\File.txt"));
         list1.Add(new FilePathRelative(@"..\Dir1\File.txt"));
         list1.Add(new DirectoryPathAbsolute(@"C:\Dir1\Dir2"));
         list1.Add(new DirectoryPathRelative(@"..\Dir1\Dir1"));
         list1.Add(new DirectoryPathRelative(@"..\Dir1\Dir1\Dir2"));
         List<BasePath> list2 = new List<BasePath>();
         list2.Add(new FilePathAbsolute(@"c:\dir1\File.txt"));
         list2.Add(new FilePathRelative(@"..\dir1\file.txt"));
         list2.Add(new DirectoryPathAbsolute(@"c:\dir1\dir2"));
         list2.Add(new DirectoryPathRelative(@"..\Dir1\Dir1"));
         list2.Add(new DirectoryPathRelative(@"..\dir1\dir1\Dir2"));

         Assert.IsTrue(ListOfPathHelper.ListOfPathsEquals(list1, list2));
      }

      [Test]
      public void Test_ListOfPathsEquals5() {
         List<BasePath> list1 = new List<BasePath>();
         list1.Add(new FilePathAbsolute(@"C:\Dir1\File.txt"));
         list1.Add(new FilePathRelative(@"..\Dir1\File.txt"));
         List<BasePath> list2 = new List<BasePath>();
         list2.Add(new FilePathAbsolute(@"c:\dir1\File.txt"));
         list2.Add(new FilePathRelative(@"..\dir1\Dir2\file.txt"));
         Assert.IsFalse(ListOfPathHelper.ListOfPathsEquals(list1, list2));
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
      public void Test_GetListOfUniqueDirsAndUniqueFileNames() {
         List<FilePathAbsolute> listIn = new List<FilePathAbsolute>();
         List<string> listOfFileNames;
         List<DirectoryPathAbsolute> listOfDirs;

         // Null input
         ListOfPathHelper.GetListOfUniqueDirsAndUniqueFileNames(
            null, out listOfDirs, out listOfFileNames);
         Assert.IsTrue(listOfDirs.Count == 0);
         Assert.IsTrue(listOfFileNames.Count == 0);

         // empty list input
         ListOfPathHelper.GetListOfUniqueDirsAndUniqueFileNames(
            listIn, out listOfDirs, out listOfFileNames);
         Assert.IsTrue(listOfDirs.Count == 0);
         Assert.IsTrue(listOfFileNames.Count == 0);

         // list contains a null ro empty path
         listIn.Add(null);
         ListOfPathHelper.GetListOfUniqueDirsAndUniqueFileNames(
           listIn, out listOfDirs, out listOfFileNames);
         Assert.IsTrue(listOfDirs.Count == 0);
         Assert.IsTrue(listOfFileNames.Count == 0);

         listIn.Clear();
         listIn.Add(FilePathAbsolute.Empty);
         ListOfPathHelper.GetListOfUniqueDirsAndUniqueFileNames(
            listIn, out listOfDirs, out listOfFileNames);
         Assert.IsTrue(listOfDirs.Count == 0);
         Assert.IsTrue(listOfFileNames.Count == 0);


         // Only one path
         listIn.Add(new FilePathAbsolute(@"E:\Path1\Path2\File1.txt"));
         ListOfPathHelper.GetListOfUniqueDirsAndUniqueFileNames(
            listIn, out listOfDirs, out listOfFileNames);
         Assert.IsTrue(listOfDirs.Count == 1);
         Assert.IsTrue(listOfDirs[0].Equals(new DirectoryPathAbsolute(@"E:\Path1\Path2")));
         Assert.IsTrue(listOfFileNames.Count == 1);
         Assert.IsTrue(listOfFileNames[0] == @"File1.txt");

         listIn.Add(new FilePathAbsolute(@"E:\Path1\Path2\File2.txt"));
         ListOfPathHelper.GetListOfUniqueDirsAndUniqueFileNames(
            listIn, out listOfDirs, out listOfFileNames);
         Assert.IsTrue(listOfDirs.Count == 1);
         Assert.IsTrue(listOfDirs[0].Equals(new DirectoryPathAbsolute(@"E:\Path1\Path2")));
         Assert.IsTrue(listOfFileNames.Count == 2);
         Assert.IsTrue(listOfFileNames[0] == @"File1.txt");
         Assert.IsTrue(listOfFileNames[1] == @"File2.txt");

         listIn.Add(new FilePathAbsolute(@"E:\Path3\Path4\file2.txt"));
         ListOfPathHelper.GetListOfUniqueDirsAndUniqueFileNames(
            listIn, out listOfDirs, out listOfFileNames);
         Assert.IsTrue(listOfDirs.Count == 2);
         Assert.IsTrue(listOfDirs[0].Equals(new DirectoryPathAbsolute(@"E:\Path1\Path2")));
         Assert.IsTrue(listOfDirs[1].Equals(new DirectoryPathAbsolute(@"E:\Path3\Path4")));
         Assert.IsTrue(listOfFileNames.Count == 2);
         Assert.IsTrue(listOfFileNames[0] == @"File1.txt");
         Assert.IsTrue(listOfFileNames[1] == @"File2.txt");
      }

      [Test]
      public void Test_ListOfPathContains() {

         // Null and empty lists
         Assert.IsFalse(ListOfPathHelper.Contains(null, new FilePathAbsolute(@"E:\Path1\Path2\File.txt")));
         List<FilePathAbsolute> listIn = new List<FilePathAbsolute>();
         Assert.IsFalse(ListOfPathHelper.Contains(listIn, new FilePathAbsolute(@"E:\Path1\Path2\File.txt")));

         // List contains null
         listIn.Add(null);
         Assert.IsTrue(ListOfPathHelper.Contains(listIn, null));

         // List contains empty path
         listIn.Add(FilePathAbsolute.Empty);
         Assert.IsTrue(ListOfPathHelper.Contains(listIn, FilePathAbsolute.Empty));

         listIn.Add(new FilePathAbsolute(@"E:\Path1\Path2\File.txt"));
         Assert.IsTrue(ListOfPathHelper.Contains(listIn, new FilePathAbsolute(@"E:\Path1\Path2\File.txt")));


         List<DirectoryPath> listIn1 = new List<DirectoryPath>();
         listIn1.Add(new DirectoryPathAbsolute(@"E:\Path1\Path2"));
         listIn1.Add(new DirectoryPathRelative(@"..\Path1\Path2"));
         Assert.IsTrue(ListOfPathHelper.Contains<DirectoryPath>(listIn1, new DirectoryPathRelative(@"..\Path1\Path2")));
      }

      [Test]
      public void Test_GetHashCodeThroughDictionary() {
         Dictionary<BasePath, string> dico = new Dictionary<BasePath, string>();
         DirectoryPathAbsolute directoryPath = new DirectoryPathAbsolute(@"C:\Dir1");
         FilePathAbsolute filePath = new FilePathAbsolute(@"c:\dir1\Dir2\file.txt");

         dico.Add(directoryPath, directoryPath.Path);
         dico.Add(filePath, filePath.Path);
         Assert.IsTrue(dico[filePath] == @"c:\dir1\Dir2\file.txt");
      }

      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_GetHashCodeThroughDictionary_ErrorOnPathValue() {
         Dictionary<BasePath, string> dico = new Dictionary<BasePath, string>();
         FilePathAbsolute filePath1 = new FilePathAbsolute(@"C:\Dir1\File.txt");
         FilePathAbsolute filePath2 = new FilePathAbsolute(@"c:\dir1\file.txt");
         Assert.IsTrue(filePath1 == filePath2);
         Assert.IsTrue(filePath1.GetHashCode() == filePath2.GetHashCode());
         dico.Add(filePath1, filePath1.Path);
         dico.Add(filePath2, filePath2.Path); // <- filePath1 & filePath2 are 2 different object with same value
      }
   }
}
