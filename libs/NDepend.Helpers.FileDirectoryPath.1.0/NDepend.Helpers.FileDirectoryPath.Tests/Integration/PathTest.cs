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
using NUnit.Framework;
using NDepend.Test.Unit;
using System.IO;

namespace NDepend.Helpers.FileDirectoryPath {
   [TestFixture]
   public class PathTest {

      [SetUp]
      public void SetUp() {
         NDepend.Test.TestHelper.SetUpTests();
      }

      [Test]
      public void Test_Drive() {
         DirectoryPathAbsolute directoryPathAbsolute = new DirectoryPathAbsolute(@"C:\Dir1");
         Assert.IsTrue(directoryPathAbsolute.Drive == "C");
         directoryPathAbsolute = new DirectoryPathAbsolute(@"c:\Dir1");
         Assert.IsTrue(directoryPathAbsolute.Drive == "c");

         FilePathAbsolute filePathAbsolute = new FilePathAbsolute(@"C:\Dir1\File.txt");
         Assert.IsTrue(filePathAbsolute.Drive == "C");
         filePathAbsolute = new FilePathAbsolute(@"c:\Dir1\File.txt");
         Assert.IsTrue(filePathAbsolute.Drive == "c");
      }

      [Test, ExpectedException(typeof(InvalidOperationException))]
      public void Test_DriveDirectoryEmpty() {
         DirectoryPathAbsolute directoryPathAbsolute = DirectoryPathAbsolute.Empty;
         string drive = directoryPathAbsolute.Drive;
      }

      [Test, ExpectedException(typeof(InvalidOperationException))]
      public void Test_DriveFileEmpty() {
         FilePathAbsolute filePathAbsolute = FilePathAbsolute.Empty;
         string drive = filePathAbsolute.Drive;
      }


      [Test]
      public void Test_Exist() {
         DirForTest.Delete();
         string dirForTestPath = DirForTest.Dir;
         DirectoryPathAbsolute directoryPathAbsolute = new DirectoryPathAbsolute(dirForTestPath);
         Assert.IsFalse(directoryPathAbsolute.Exists);

         DirForTest.EnsureDirForTestExistAndEmpty();
         Assert.IsTrue(directoryPathAbsolute.Exists);

         string dirForTestWithExecutingAssemblyFilePath = DirForTest.ExecutingAssemblyFilePathInDirForTest;
         FilePathAbsolute filePathAbsolute = new FilePathAbsolute(dirForTestWithExecutingAssemblyFilePath);
         Assert.IsFalse(filePathAbsolute.Exists);

         DirForTest.CopyExecutingAssemblyFileInDirForTest();
         Assert.IsTrue(filePathAbsolute.Exists);
      }

      [Test, ExpectedException(typeof(FileNotFoundException))]
      public void Test_DirDontExist() {
         DirForTest.Delete();
         string dirForTestPath = DirForTest.Dir;
         DirectoryPathAbsolute directoryPathAbsolute = new DirectoryPathAbsolute(dirForTestPath);
         DirectoryInfo directoryInfo = directoryPathAbsolute.DirectoryInfo;
      }

      [Test, ExpectedException(typeof(FileNotFoundException))]
      public void Test_FileDontExist() {
         DirForTest.Delete();
         string dirForTestWithExecutingAssemblyFilePath = DirForTest.ExecutingAssemblyFilePathInDirForTest;
         FilePathAbsolute filePathAbsolute = new FilePathAbsolute(dirForTestWithExecutingAssemblyFilePath);
         FileInfo fileInfo = filePathAbsolute.FileInfo;
      }

      [Test]
      public void Test_DirInfo_FileInfo_ChildrenOfDir() {
         DirForTest.EnsureDirForTestExistAndEmpty();
         string dirForTestPath = DirForTest.Dir;
         DirectoryPathAbsolute directoryPathAbsolute = new DirectoryPathAbsolute(dirForTestPath);
         DirectoryInfo directoryInfo = directoryPathAbsolute.DirectoryInfo;
         Assert.IsTrue(directoryInfo != null);

         DirForTest.CopyExecutingAssemblyFileInDirForTest();
         string dirForTestWithExecutingAssemblyFilePath = DirForTest.ExecutingAssemblyFilePathInDirForTest;
         FilePathAbsolute filePathAbsolute = new FilePathAbsolute(dirForTestWithExecutingAssemblyFilePath);
         FileInfo fileInfo = filePathAbsolute.FileInfo;
         Assert.IsTrue(fileInfo != null);
      }

      [Test]
      public void Test_ChildrenOfDir() {
         DirForTest.EnsureDirForTestExistAndEmpty();
         string dirForTestPath = DirForTest.Dir;
         DirectoryPathAbsolute directoryPathAbsolute = new DirectoryPathAbsolute(dirForTestPath);
         Assert.IsTrue(directoryPathAbsolute.ChildrenDirectoriesPath.Count == 0);
         Assert.IsTrue(directoryPathAbsolute.ChildrenFilesPath.Count == 0);

         Directory.CreateDirectory(dirForTestPath + System.IO.Path.DirectorySeparatorChar + "Dir1");
         Directory.CreateDirectory(dirForTestPath + System.IO.Path.DirectorySeparatorChar + "Dir2");
         Assert.IsTrue(directoryPathAbsolute.ChildrenDirectoriesPath.Count == 2);

         DirForTest.CopyExecutingAssemblyFileInDirForTest();
         Assert.IsTrue(directoryPathAbsolute.ChildrenFilesPath.Count == 1);
      }

   }
}
