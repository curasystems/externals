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
using System.Diagnostics;

namespace NDepend.Helpers.FileDirectoryPath {
   [TestFixture]
   public class FilePathTester {
      [SetUp]
      public void SetUp() {
         NDepend.Test.TestHelper.SetUpTests();
      }

      [Test]
      public void Test_EmptyFilePathAbsolute() {
         //Debug.Assert(false);
         FilePathAbsolute filePath = FilePathAbsolute.Empty;
         Assert.IsTrue(filePath.IsEmpty);
         Assert.IsTrue(filePath.IsAbsolutePath);
         Assert.IsTrue(filePath.IsFilePath);
         Assert.IsFalse(filePath.IsDirectoryPath);
      }
      [Test]
      public void Test_EmptyFilePathRelative() {
         FilePathRelative filePath = FilePathRelative.Empty;
         Assert.IsTrue(filePath.IsEmpty);
         Assert.IsTrue(filePath.IsRelativePath);
         Assert.IsTrue(filePath.IsFilePath);
         Assert.IsFalse(filePath.IsDirectoryPath);
      }


      [Test, ExpectedException(typeof(ArgumentNullException))]
      public void Test_InvalidInputPathNull() {
         string reason;
         Assert.IsFalse(PathHelper.IsValidAbsolutePath(null, out reason));
         FilePath filePath = new FilePathAbsolute(null);
      }
      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_InvalidInputPathEmpty() {
         string reason;
         Assert.IsFalse(PathHelper.IsValidAbsolutePath(string.Empty, out reason));
         FilePath filePath = new FilePathAbsolute(string.Empty);
      }

      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_InvalidInputAbsolutePath1() {
         string reason;
         Assert.IsFalse(PathHelper.IsValidAbsolutePath("C", out reason));
         FilePath filePath = new FilePathAbsolute("C");
      }

      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_InvalidInputAbsolutePath2() {
         string reason;
         Assert.IsFalse(PathHelper.IsValidAbsolutePath(@"C\File.txt", out reason));
         FilePath filePath = new FilePathAbsolute(@"C\File.txt");
      }

      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_InvalidInputAbsolutePath3() {
         string reason;
         Assert.IsFalse(PathHelper.IsValidAbsolutePath(@"1:\File.txt", out reason));
         FilePath filePath = new FilePathAbsolute(@"1:\File.txt");
      }

      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_InvalidInputAbsoluteURNPath() {
         string reason;
         Assert.IsFalse(PathHelper.IsValidAbsolutePath(@"http://www.NDepend.com/File.txt", out reason));
         FilePath filePath = new FilePathAbsolute(@"http://www.NDepend.com/File.txt");
      }

      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_InvalidInputAbsolutePath4() {
         FilePath filePath = new FilePathAbsolute(@"C:File.txt");
      }

      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_InvalidInputPathNoParentDir() {
         string reason;
         Assert.IsFalse(PathHelper.IsValidAbsolutePath(@"File.txt", out reason));
         FilePath filePath = new FilePathAbsolute("File.txt");
      }

      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_InvalidInputPathBadFormatting1() {
         string reason;
         Assert.IsFalse(PathHelper.IsValidAbsolutePath(@"C:\..\File.txt", out reason));
         FilePath filePath = new FilePathAbsolute(@"C:\..\File.txt");
      }

      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_InvalidInputPathBadFormatting3() {
         string reason;
         Assert.IsFalse(PathHelper.IsValidAbsolutePath(@"C:\..\Dir1\File.txt", out reason));
         FilePath filePath = new FilePathAbsolute(@"C:\..\Dir1\File.txt");
      }





      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_InvalidInputPathBadFormatting10() {
         string reason;
         Assert.IsFalse(PathHelper.IsValidRelativePath(@".\Dir1\..\..\File.txt", out reason));
         FilePath filePath = new FilePathRelative(@".\Dir1\..\..\File.txt");
      }



      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_InvalidInputPathBadFormatting14() {
         string reason;
         Assert.IsFalse(PathHelper.IsValidRelativePath(@"..\Dir1\..\..\Dir1\File.txt", out reason));
         FilePath filePath = new FilePathRelative(@"..\Dir1\..\..\Dir1\File.txt");
      }



      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_IncoherentPathModeException1() {
         string reason;
         Assert.IsFalse(PathHelper.IsValidAbsolutePath(@".\File.txt", out reason));
         FilePath filePath = new FilePathAbsolute(@".\File.txt");
      }
      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_IncoherentPathModeException2() {
         string reason;
         Assert.IsFalse(PathHelper.IsValidRelativePath(@"C:\File.txt", out reason));
         FilePath filePath = new FilePathRelative(@"C:\File.txt");
      }

      [Test]
      public void Test_PathModeOk() {
         FilePath path = new FilePathRelative(@".\File.txt");
         Assert.IsTrue(path.IsRelativePath);
         //Assert.IsTrue(path.ToString() == @".\File.txt");

         path = new FilePathAbsolute(@"C:\File.txt");
         Assert.IsTrue(path.IsAbsolutePath);

         path = new FilePathAbsolute(@"c:\File.txt");
         Assert.IsTrue(path.IsAbsolutePath);

         path = new FilePathRelative(@".\dir...1\File.txt");
         Assert.IsTrue(path.IsRelativePath);

         path = new FilePathAbsolute(@"C:\dir...1\File.txt");
         Assert.IsTrue(path.IsAbsolutePath);
      }

      [Test]
      public void Test_BuildFilePath() {
         FilePath path = PathHelper.BuildFilePath(@".\Dir1\File.txt");
         Assert.IsTrue(path.IsRelativePath);

         path = PathHelper.BuildFilePath(@"C:\Dir1\File.txt");
         Assert.IsTrue(path.IsAbsolutePath);
      }


      [Test]
      public void Test_NormalizePath() {
         FilePath path = new FilePathRelative(@".\File.txt");
         Assert.IsTrue(path.Path == @".\File.txt");

         path = new FilePathRelative(@".\\File.txt\\");
         Assert.IsTrue(path.Path == @".\File.txt");

         path = new FilePathRelative(@".\/dir1\//\dir2\/dir3///\File.txt/");
         Assert.IsTrue(path.Path == @".\dir1\dir2\dir3\File.txt");

         path = new FilePathAbsolute(@"C:/dir1/dir2/\File.txt");
         Assert.IsTrue(path.Path == @"C:\dir1\dir2\File.txt");
      }

      [Test]
      public void Test_ParentDirectoryPath() {
         DirectoryPath path = new FilePathRelative(@".\File.txt").ParentDirectoryPath;
         Assert.IsTrue(path.Path == @".");

         path = new FilePathAbsolute(@"C:\File.txt").ParentDirectoryPath;
         Assert.IsTrue(path.Path == @"C:");

         path = new FilePathRelative(@".\\File.txt").ParentDirectoryPath;
         Assert.IsTrue(path.Path == @".");

         path = new FilePathAbsolute(@"C:\\\\File.txt").ParentDirectoryPath;
         Assert.IsTrue(path.Path == @"C:");

         path = new FilePathAbsolute(@"C:\dir1\\//dir2\File.txt").ParentDirectoryPath;
         Assert.IsTrue(path.Path == @"C:\dir1\dir2");
      }

      [Test]
      public void Test_FileName() {
         string fileName = new FilePathRelative(@".\File.txt").FileName;
         Assert.IsTrue(fileName == @"File.txt");

         fileName = new FilePathAbsolute(@"C:\File.txt").FileName;
         Assert.IsTrue(fileName == @"File.txt");

         fileName = new FilePathRelative(@".\\File.txt").FileName;
         Assert.IsTrue(fileName == @"File.txt");

         fileName = new FilePathAbsolute(@"C:\\\\File.txt").FileName;
         Assert.IsTrue(fileName == @"File.txt");

         fileName = new FilePathAbsolute(@"C:\dir1\\//dir2\File.txt").FileName;
         Assert.IsTrue(fileName == @"File.txt");
      }


      [Test]
      public void Test_FileExtension() {
         FilePath filePath = new FilePathRelative(@".\File.txt");
         Assert.IsTrue(filePath.FileExtension == @".txt");
         Assert.IsTrue(filePath.HasExtension(@".txt"));
         Assert.IsTrue(filePath.HasExtension(@".TxT"));
         Assert.IsTrue(filePath.HasExtension(@".TXT"));

         filePath = new FilePathRelative(@".\File");
         Assert.IsTrue(filePath.FileExtension == string.Empty);

         filePath = new FilePathRelative(@".\File.");
         Assert.IsTrue(filePath.FileExtension == string.Empty);

         filePath = new FilePathAbsolute(@"C:\dir1\\//dir2\File.txt.Exe");
         Assert.IsTrue(filePath.FileExtension == @".Exe");
         Assert.IsTrue(filePath.HasExtension(@".exe"));
      }

      [Test]
      public void Test_FileNameWithoutExtension() {
         FilePath filePath = new FilePathRelative(@".\File.txt");
         Assert.IsTrue(filePath.FileNameWithoutExtension == "File");

         filePath = new FilePathRelative(@".\File");
         Assert.IsTrue(filePath.FileNameWithoutExtension == "File");

         filePath = new FilePathRelative(@".\File.tmp.exe");
         Assert.IsTrue(filePath.FileNameWithoutExtension == "File.tmp");
      }

      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_HasExtensionError1() {
         FilePath filePath = new FilePathRelative(@".\File");
         Assert.IsTrue(filePath.HasExtension(string.Empty));
      }
      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_HasExtensionError2() {
         FilePath filePath = new FilePathRelative(@".\File.txt");
         Assert.IsTrue(filePath.HasExtension("txt"));
      }
      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_HasExtensionError3() {
         FilePath filePath = new FilePathRelative(@".\File.");
         Assert.IsTrue(filePath.HasExtension("."));
      }


      //
      //  TestInnerSpecialDir
      //
      [Test]
      public void Test_InnerSpecialDir1() {
         Assert.IsTrue(new FilePathAbsolute(@"C:\Dir1\..\File.txt").Path == @"C:\File.txt");
         Assert.IsTrue(new FilePathAbsolute(@"C:\Dir1\..\Dir2\..\File.txt").Path == @"C:\File.txt");
         Assert.IsTrue(new FilePathAbsolute(@"C:\Dir1\..\Dir2\..\.\File.txt").Path == @"C:\File.txt");
         Assert.IsTrue(new FilePathAbsolute(@"C:\.\Dir1\..\File.txt").Path == @"C:\File.txt");
         Assert.IsTrue(new FilePathAbsolute(@"C:\.\Dir1\Dir2\Dir3\..\..\..\File.txt").Path == @"C:\File.txt");

         Assert.IsTrue(new FilePathRelative(@".\Dir1\..\File.txt").Path == @".\File.txt");
         Assert.IsTrue(new FilePathRelative(@".\Dir1\..\Dir2\..\File.txt").Path == @".\File.txt");
         Assert.IsTrue(new FilePathRelative(@".\Dir1\..\Dir2\..\.\File.txt").Path == @".\File.txt");
         Assert.IsTrue(new FilePathRelative(@".\.\Dir1\..\File.txt").Path == @".\File.txt");
         Assert.IsTrue(new FilePathRelative(@".\.\Dir1\Dir2\Dir3\..\..\..\File.txt").Path == @".\File.txt");

         Assert.IsTrue(new DirectoryPathAbsolute(@"C:\Dir1\..\").Path == @"C:");
         Assert.IsTrue(new DirectoryPathAbsolute(@"C:\Dir1\..\Dir2\..\").Path == @"C:");
         Assert.IsTrue(new DirectoryPathAbsolute(@"C:\Dir1\..\Dir2\..\.\").Path == @"C:");
         Assert.IsTrue(new DirectoryPathAbsolute(@"C:\.\Dir1\..\").Path == @"C:");
         Assert.IsTrue(new DirectoryPathAbsolute(@"C:\.\Dir1\Dir2\Dir3\..\..\..\").Path == @"C:");

         Assert.IsTrue(new DirectoryPathRelative(@".\Dir1\..\File.txt").Path == @".\File.txt");
         Assert.IsTrue(new DirectoryPathRelative(@".\Dir1\..\Dir2\..\File.txt").Path == @".\File.txt");
         Assert.IsTrue(new DirectoryPathRelative(@".\Dir1\..\Dir2\..\.\File.txt").Path == @".\File.txt");
         Assert.IsTrue(new DirectoryPathRelative(@".\.\Dir1\..\File.txt").Path == @".\File.txt");
         Assert.IsTrue(new FilePathRelative(@".\.\Dir1\Dir2\Dir3\..\..\..\File.txt").Path == @".\File.txt");

         Assert.IsTrue(new DirectoryPathRelative(@".\.").Path == @".");
         Assert.IsTrue(new DirectoryPathRelative(@"./.\./.\").Path == @".");

         Assert.IsTrue(new DirectoryPathRelative(@".\..\Dir1\..").Path == @".\..");
         Assert.IsTrue(new DirectoryPathRelative(@".\Dir1\Dir2\..\..").Path == @".");
         Assert.IsTrue(new DirectoryPathRelative(@".\.\.\Dir1\Dir2\..\..").Path == @".");
         Assert.IsTrue(new DirectoryPathRelative(@".\.\.\..\..").Path == @".\..\..");
         Assert.IsTrue(new DirectoryPathRelative(@"..\.\..\Dir1").Path == @"..\..\Dir1");
         Assert.IsTrue(new DirectoryPathRelative(@"..\Dir1\.\..\Dir2\").Path == @"..\Dir2");


      }

      // @"The path {" + path + @"} references a non-existing parent dir \..\, it cannot be resolved";
      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_InnerSpecialDir_Error10() {
         BasePath path = new DirectoryPathRelative(@"..\Dir1\..\..");
      }


      // @"The path {" + path + @"} references the parent dir \..\ of the root dir {" + pathDirs[0] + "}, it cannot be resolved";
      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_InnerSpecialDir_Error20() {
         BasePath path = new DirectoryPathAbsolute(@"C:\..");
      }
      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_InnerSpecialDir_Error21() {
         BasePath path = new DirectoryPathAbsolute(@"C:\Dir1\..\..");
      }
      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_InnerSpecialDir_Error22() {
         BasePath path = new DirectoryPathAbsolute(@"C:\Dir1\..\..");
      }

      //@"The path {" + path + @"} references the parent dir \..\ of the current root dir .\, it cannot be resolved"
      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_InnerSpecialDir_Error30() {
         BasePath path = new DirectoryPathRelative(@".\Dir1\..\..");
      }
      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_InnerSpecialDir_Error31() {
         BasePath path = new DirectoryPathRelative(@".\Dir1\Dir2\..\..\..");
      }


      // @"The path {" + path + @"} references the parent dir \..\ of a parent dir \..\, it cannot be resolved"
      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_InnerSpecialDir_Error40() {
         BasePath path = new DirectoryPathRelative(@".\..\Dir1\..\..");
      }
      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_InnerSpecialDir_Error41() {
         BasePath path = new DirectoryPathAbsolute(@"C:\..\Dir1\..\..");
      }



      //
      //  GetRelativePath
      //
      [Test]
      public void Test_GetRelativePath() {
         FilePathAbsolute filePathTo;
         DirectoryPathAbsolute directoryPathFrom;
         ;

         filePathTo = new FilePathAbsolute(@"C:\Dir1\File.txt");
         directoryPathFrom = new DirectoryPathAbsolute(@"C:\Dir1");
         Assert.IsTrue(filePathTo.GetPathRelativeFrom(directoryPathFrom).Path == @".\File.txt");
         Assert.IsTrue(filePathTo.CanGetPathRelativeFrom(directoryPathFrom));

         filePathTo = new FilePathAbsolute(@"C:\Dir1\Dir2\File.txt");
         directoryPathFrom = new DirectoryPathAbsolute(@"C:\Dir1\Dir3");
         Assert.IsTrue(filePathTo.GetPathRelativeFrom(directoryPathFrom).Path == @"..\Dir2\File.txt");
         Assert.IsTrue(filePathTo.CanGetPathRelativeFrom(directoryPathFrom));

         filePathTo = new FilePathAbsolute(@"C:\Dir1\File.txt");
         directoryPathFrom = new DirectoryPathAbsolute(@"C:\Dir2");
         Assert.IsTrue(filePathTo.GetPathRelativeFrom(directoryPathFrom).Path == @"..\Dir1\File.txt");
         Assert.IsTrue(filePathTo.CanGetPathRelativeFrom(directoryPathFrom));

         filePathTo = new FilePathAbsolute(@"C:\Dir1\Dir2\File.txt");
         directoryPathFrom = new DirectoryPathAbsolute(@"C:\Dir1");
         Assert.IsTrue(filePathTo.GetPathRelativeFrom(directoryPathFrom).Path == @".\Dir2\File.txt");
         Assert.IsTrue(filePathTo.CanGetPathRelativeFrom(directoryPathFrom));
      }


      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_GetRelativePathWithError3() {
         FilePathAbsolute filePathTo = new FilePathAbsolute(@"C:\Dir1\File.txt");
         DirectoryPathAbsolute directoryPathFrom = new DirectoryPathAbsolute(@"D:\Dir1");
         Assert.IsFalse(filePathTo.CanGetPathRelativeFrom(directoryPathFrom));
         filePathTo.GetPathRelativeFrom(directoryPathFrom);
      }

      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_GetRelativePathWithError4() {
         FilePathAbsolute filePathTo = FilePathAbsolute.Empty;
         DirectoryPathAbsolute directoryPathFrom = new DirectoryPathAbsolute(@"D:\Dir1");
         Assert.IsFalse(filePathTo.CanGetPathRelativeFrom(directoryPathFrom));
         filePathTo.GetPathRelativeFrom(directoryPathFrom);
      }

      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_GetRelativePathWithError5() {
         FilePathAbsolute filePathTo = new FilePathAbsolute(@"C:\Dir1\File.txt");
         DirectoryPathAbsolute directoryPathFrom = DirectoryPathAbsolute.Empty;
         Assert.IsFalse(filePathTo.CanGetPathRelativeFrom(directoryPathFrom));
         filePathTo.GetPathRelativeFrom(directoryPathFrom);
      }

      [Test, ExpectedException(typeof(ArgumentNullException))]
      public void Test_GetRelativePathWithError6() {
         FilePathAbsolute filePathTo = new FilePathAbsolute(@"C:\Dir1\File.txt");
         Assert.IsFalse(filePathTo.CanGetPathRelativeFrom(null));
         filePathTo.GetPathRelativeFrom(null);
      }


      //
      //  GetAbsolutePath
      //
      [Test]
      public void Test_GetAbsolutePath() {
         FilePathRelative filePathTo;
         DirectoryPathAbsolute directoryPathFrom;

         filePathTo = new FilePathRelative(@"..\File.txt");
         directoryPathFrom = new DirectoryPathAbsolute(@"C:\Dir1");
         Assert.IsTrue(filePathTo.GetAbsolutePathFrom(directoryPathFrom).Path == @"C:\File.txt");
         Assert.IsTrue(filePathTo.CanGetAbsolutePathFrom(directoryPathFrom));

         filePathTo = new FilePathRelative(@".\File.txt");
         directoryPathFrom = new DirectoryPathAbsolute(@"C:\Dir1");
         Assert.IsTrue(filePathTo.GetAbsolutePathFrom(directoryPathFrom).Path == @"C:\Dir1\File.txt");
         Assert.IsTrue(filePathTo.CanGetAbsolutePathFrom(directoryPathFrom));

         filePathTo = new FilePathRelative(@"..\Dir2\File.txt");
         directoryPathFrom = new DirectoryPathAbsolute(@"C:\Dir1");
         Assert.IsTrue(filePathTo.GetAbsolutePathFrom(directoryPathFrom).Path == @"C:\Dir2\File.txt");
         Assert.IsTrue(filePathTo.CanGetAbsolutePathFrom(directoryPathFrom));

         filePathTo = new FilePathRelative(@"..\..\Dir4\Dir5\File.txt");
         directoryPathFrom = new DirectoryPathAbsolute(@"C:\Dir1\Dir2\Dir3");
         Assert.IsTrue(filePathTo.GetAbsolutePathFrom(directoryPathFrom).Path == @"C:\Dir1\Dir4\Dir5\File.txt");
         Assert.IsTrue(filePathTo.CanGetAbsolutePathFrom(directoryPathFrom));

         filePathTo = new FilePathRelative(@".\..\Dir4\Dir5\File.txt");
         directoryPathFrom = new DirectoryPathAbsolute(@"C:\Dir1\Dir2\Dir3");
         Assert.IsTrue(filePathTo.GetAbsolutePathFrom(directoryPathFrom).Path == @"C:\Dir1\Dir2\Dir4\Dir5\File.txt");
         Assert.IsTrue(filePathTo.CanGetAbsolutePathFrom(directoryPathFrom));
      }


      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_GetAbsolutePathPathWithError3() {
         FilePathRelative directoryPathTo = new FilePathRelative(@"..\..\Dir1\File.txt");
         DirectoryPathAbsolute directoryPathFrom = new DirectoryPathAbsolute(@"C:\Dir1");
         Assert.IsFalse(directoryPathTo.CanGetAbsolutePathFrom(directoryPathFrom));
         directoryPathTo.GetAbsolutePathFrom(directoryPathFrom);
      }

      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_GetAbsolutePathPathWithError4() {
         FilePathRelative directoryPathTo = FilePathRelative.Empty;
         DirectoryPathAbsolute directoryPathFrom = new DirectoryPathAbsolute(@"C:\Dir1");
         Assert.IsFalse(directoryPathTo.CanGetAbsolutePathFrom(directoryPathFrom));
         directoryPathTo.GetAbsolutePathFrom(directoryPathFrom);
      }

      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_GetAbsolutePathPathWithError5() {
         FilePathRelative directoryPathTo = new FilePathRelative(@"..\..\Dir1\File.txt");
         DirectoryPathAbsolute directoryPathFrom = DirectoryPathAbsolute.Empty;
         Assert.IsFalse(directoryPathTo.CanGetAbsolutePathFrom(directoryPathFrom));
         directoryPathTo.GetAbsolutePathFrom(directoryPathFrom);
      }

      [Test, ExpectedException(typeof(ArgumentNullException))]
      public void Test_GetAbsolutePathPathWithError6() {
         FilePathRelative directoryPathTo = new FilePathRelative(@"..\..\Dir1\File.txt");
         Assert.IsFalse(directoryPathTo.CanGetAbsolutePathFrom(null));
         directoryPathTo.GetAbsolutePathFrom(null);
      }


      //
      //  PathComparison
      //
      [Test]
      public void Test_PathEquality() {
         //
         // filePathRelative 
         //
         FilePathRelative filePathRelative1 = new FilePathRelative(@"..\Dir1\File.txt");
         FilePathRelative filePathRelative2 = new FilePathRelative(@"..\\dir1//file.TXT");
         Assert.IsTrue(filePathRelative1.Equals(filePathRelative2));
         Assert.IsTrue(filePathRelative1 == filePathRelative2);

         filePathRelative1 = new FilePathRelative(@"..\Dir1\File.txt");
         filePathRelative2 = new FilePathRelative(@".\Dir1\File.txt");
         Assert.IsFalse(filePathRelative1.Equals(filePathRelative2));
         Assert.IsFalse(filePathRelative1 == filePathRelative2);

         filePathRelative1 = new FilePathRelative(@"..\Dir1\File.txt");
         filePathRelative2 = new FilePathRelative(@"..\Dir1\Dir2\File.txt");
         Assert.IsFalse(filePathRelative1.Equals(filePathRelative2));
         Assert.IsFalse(filePathRelative1 == filePathRelative2);

         filePathRelative1 = new FilePathRelative(@"..\Dir1\File.txt");
         filePathRelative2 = new FilePathRelative(@"..\Dir1\File.tx");
         Assert.IsFalse(filePathRelative1.Equals(filePathRelative2));
         Assert.IsFalse(filePathRelative1 == filePathRelative2);

         //
         // filePathAbsolute 
         //
         FilePathAbsolute filePathAbsolute1 = new FilePathAbsolute(@"C:\Dir1\File.txt");
         FilePathAbsolute filePathAbsolute2 = new FilePathAbsolute(@"C:\\dir1//file.TXT");
         Assert.IsTrue(filePathAbsolute1.Equals(filePathAbsolute2));
         Assert.IsTrue(filePathAbsolute1 == filePathAbsolute2);

         filePathAbsolute1 = new FilePathAbsolute(@"C:\Dir1\File.txt");
         filePathAbsolute2 = new FilePathAbsolute(@"D:\Dir1\File.txt");
         Assert.IsFalse(filePathAbsolute1.Equals(filePathAbsolute2));
         Assert.IsFalse(filePathAbsolute1 == filePathAbsolute2);

         filePathAbsolute1 = new FilePathAbsolute(@"C:\Dir1\File.txt");
         filePathAbsolute2 = new FilePathAbsolute(@"C:\Dir1\Dir2\File.txt");
         Assert.IsFalse(filePathAbsolute1.Equals(filePathAbsolute2));
         Assert.IsFalse(filePathAbsolute1 == filePathAbsolute2);

         filePathAbsolute1 = new FilePathAbsolute(@"C:\Dir1\File.txt");
         filePathAbsolute2 = new FilePathAbsolute(@"C:\Dir1\File.tx");
         Assert.IsFalse(filePathAbsolute1.Equals(filePathAbsolute2));
         Assert.IsFalse(filePathAbsolute1 == filePathAbsolute2);

         //
         // Mix between filePathAbsolute and filePathRelative
         //
         filePathRelative1 = new FilePathRelative(@"..\Dir1\File.txt");
         filePathAbsolute1 = new FilePathAbsolute(@"C:\Dir1\File.txt");
         Assert.IsFalse(filePathAbsolute1.Equals(filePathRelative1));
         Assert.IsFalse(filePathAbsolute1 == filePathRelative1);

         //
         // Mix between directoryPath and filePath
         //
         DirectoryPathAbsolute directoryPathAbsolute1 = new DirectoryPathAbsolute(@"C:\Dir1\File");
         filePathAbsolute1 = new FilePathAbsolute(@"C:\Dir1\File");
         Assert.IsFalse(directoryPathAbsolute1.Equals(filePathAbsolute1));
         Assert.IsFalse(filePathAbsolute1.Equals(directoryPathAbsolute1));
         Assert.IsFalse(filePathAbsolute1 == directoryPathAbsolute1);

         DirectoryPathRelative directoryPathRelative1 = new DirectoryPathRelative(@"..\Dir1\File");
         filePathRelative1 = new FilePathRelative(@"..\Dir1\File");
         Assert.IsFalse(directoryPathRelative1.Equals(filePathRelative1));
         Assert.IsFalse(filePathRelative1.Equals(directoryPathRelative1));
         Assert.IsFalse(filePathRelative1 == directoryPathRelative1);
      }





      //
      //  GetBrother
      //
      [Test, ExpectedException(typeof(ArgumentNullException))]
      public void Test_GetBrotherWithName_Error10() {
         new FilePathAbsolute(@"C:\Dir1\File.txt").GetBrotherDirectoryWithName(null);
      }
      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_GetBrotherWithName_Error11() {
         new FilePathAbsolute(@"C:\Dir1\File.txt").GetBrotherDirectoryWithName(string.Empty);
      }
      [Test, ExpectedException(typeof(InvalidOperationException))]
      public void Test_GetBrotherWithName_Error12() {
         FilePathAbsolute.Empty.GetBrotherDirectoryWithName("Dir1");
      }

      [Test, ExpectedException(typeof(ArgumentNullException))]
      public void Test_GetBrotherWithName_Error20() {
         new FilePathRelative(@"..\Dir1\File.txt").GetBrotherDirectoryWithName(null);
      }
      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_GetBrotherWithName_Error21() {
         new FilePathRelative(@"..\Dir1\File.txt").GetBrotherDirectoryWithName(string.Empty);
      }
      [Test, ExpectedException(typeof(InvalidOperationException))]
      public void Test_GetBrotherWithName_Error22() {
         FilePathRelative.Empty.GetBrotherDirectoryWithName("Dir1");
      }


      [Test, ExpectedException(typeof(ArgumentNullException))]
      public void Test_GetBrotherWithName_Error30() {
         new FilePathAbsolute(@"C:\Dir1\File.txt").GetBrotherFileWithName(null);
      }
      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_GetBrotherWithName_Error31() {
         new FilePathAbsolute(@"C:\Dir1\File.txt").GetBrotherFileWithName(string.Empty);
      }
      [Test, ExpectedException(typeof(InvalidOperationException))]
      public void Test_GetBrotherWithName_Error32() {
         FilePathAbsolute.Empty.GetBrotherFileWithName("File.txt");
      }

      [Test, ExpectedException(typeof(ArgumentNullException))]
      public void Test_GetBrotherWithName_Error40() {
         new FilePathRelative(@"..\Dir1\File.txt").GetBrotherFileWithName(null);
      }
      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_GetBrotherWithName_Error41() {
         new FilePathRelative(@"..\Dir1\File.txt").GetBrotherFileWithName(string.Empty);
      }
      [Test, ExpectedException(typeof(InvalidOperationException))]
      public void Test_GetBrotherWithName_Error42() {
         FilePathRelative.Empty.GetBrotherFileWithName("File.txt");
      }


      [Test]
      public void Test_GetBrotherWithName() {
         Assert.IsTrue(new FilePathAbsolute(@"C:\Dir1\File.txt").GetBrotherDirectoryWithName("Dir3") ==
            new DirectoryPathAbsolute(@"C:\Dir1\Dir3"));

         Assert.IsTrue(new FilePathRelative(@"..\Dir1\File.txt").GetBrotherDirectoryWithName("Dir3") ==
            new DirectoryPathRelative(@"..\Dir1\Dir3"));

         Assert.IsTrue(new FilePathAbsolute(@"C:\Dir1\File.txt").GetBrotherFileWithName("File.exe") ==
            new FilePathAbsolute(@"C:\Dir1\File.exe"));

         Assert.IsTrue(new FilePathRelative(@"..\Dir1\File.txt").GetBrotherFileWithName("File.exe") ==
            new FilePathRelative(@"..\Dir1\File.exe"));
      }



      //
      //  Change Extension
      //


      [Test, ExpectedException(typeof(ArgumentNullException))]
      public void Test_ChangeExtension_Error40() {
         new FilePathRelative(@"..\Dir1\File.txt").ChangeExtension(null);
      }
      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_ChangeExtension_Error41() {
         new FilePathRelative(@"..\Dir1\File.txt").ChangeExtension("exe");
      }
      [Test, ExpectedException(typeof(InvalidOperationException))]
      public void Test_ChangeExtension_Error42() {
         FilePathRelative.Empty.ChangeExtension(".exe");
      }



      [Test, ExpectedException(typeof(ArgumentNullException))]
      public void Test_ChangeExtension_Error50() {
         new FilePathAbsolute(@"C:\Dir1\File.txt").ChangeExtension(null);
      }
      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_ChangeExtension_Error51() {
         new FilePathAbsolute(@"C:\Dir1\File.txt").ChangeExtension("exe");
      }
      [Test, ExpectedException(typeof(InvalidOperationException))]
      public void Test_ChangeExtension_Error52() {
         FilePathAbsolute.Empty.ChangeExtension(".exe");
      }



      [Test]
      public void Test_ChangeExtension() {
         Assert.IsTrue(new FilePathRelative(@"..\Dir1\File.txt").ChangeExtension(".exe") ==
            new FilePathRelative(@"..\Dir1\File.exe"));
         Assert.IsTrue(new FilePathRelative(@"..\Dir1\File").ChangeExtension(".exe") ==
            new FilePathRelative(@"..\Dir1\File.exe"));
         Assert.IsTrue(new FilePathRelative(@"..\Dir1\File.txt.bmp").ChangeExtension(".exe") ==
            new FilePathRelative(@"..\Dir1\File.txt.exe"));
         Assert.IsTrue(new FilePathRelative(@"..\Dir1\File").ChangeExtension(".exe") ==
            new FilePathRelative(@"..\Dir1\File.exe"));

         Assert.IsTrue(new FilePathAbsolute(@"C:\Dir1\File.txt").ChangeExtension(".exe") ==
            new FilePathAbsolute(@"C:\Dir1\File.exe"));
         Assert.IsTrue(new FilePathAbsolute(@"C:\Dir1\File").ChangeExtension(".exe") ==
            new FilePathAbsolute(@"C:\Dir1\File.exe"));
         Assert.IsTrue(new FilePathAbsolute(@"C:\Dir1\File.txt.bmp").ChangeExtension(".exe") ==
            new FilePathAbsolute(@"C:\Dir1\File.txt.exe"));
         Assert.IsTrue(new FilePathAbsolute(@"C:\Dir1\File").ChangeExtension(".exe") ==
            new FilePathAbsolute(@"C:\Dir1\File.exe"));
      }

      [Test]
      public void Test_ExplicitConversionFromPathToString() {
         Assert.IsTrue(((string)new FilePathRelative(@"..\Dir1\File.txt")) == @"..\Dir1\File.txt");
      }

   }
}
