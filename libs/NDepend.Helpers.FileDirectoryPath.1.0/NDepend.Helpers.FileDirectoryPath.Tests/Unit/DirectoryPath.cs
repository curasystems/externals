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


namespace NDepend.Helpers.FileDirectoryPath {
   [TestFixture]
   public class DirectoryPathTest {


      [SetUp]
      public void SetUp() {
         NDepend.Test.TestHelper.SetUpTests();
      }

      [Test]
      public void Test_EmptyDirectoryPathAbsolute() {
         DirectoryPathAbsolute directoryPath = DirectoryPathAbsolute.Empty;
         Assert.IsTrue(directoryPath.IsEmpty);
         Assert.IsTrue(directoryPath.IsAbsolutePath);
         Assert.IsTrue(directoryPath.IsDirectoryPath);
         Assert.IsFalse(directoryPath.IsFilePath);
      }

      [Test]
      public void Test_EmptyDirectoryPathRelative() {
         DirectoryPathRelative directoryPath = DirectoryPathRelative.Empty;
         Assert.IsTrue(directoryPath.IsEmpty);
         Assert.IsTrue(directoryPath.IsRelativePath);
         Assert.IsTrue(directoryPath.IsDirectoryPath);
         Assert.IsFalse(directoryPath.IsFilePath);
      }

      [Test]
      public void Test_IsNullOrEmptyPath() {
         Assert.IsTrue(PathHelper.IsNullOrEmpty(null));
         Assert.IsTrue(PathHelper.IsNullOrEmpty(FilePathAbsolute.Empty));
         Assert.IsTrue(PathHelper.IsNullOrEmpty(DirectoryPathAbsolute.Empty));
         Assert.IsTrue(PathHelper.IsNullOrEmpty(FilePathRelative.Empty));
         Assert.IsTrue(PathHelper.IsNullOrEmpty(DirectoryPathRelative.Empty));
         Assert.IsFalse(PathHelper.IsNullOrEmpty(new DirectoryPathRelative(@"..\Dir1")));
      }

      [Test]
      public void Test_IsEmptyPath() {
         Assert.IsTrue(PathHelper.IsEmpty(FilePathAbsolute.Empty));
         Assert.IsTrue(PathHelper.IsEmpty(DirectoryPathAbsolute.Empty));
         Assert.IsTrue(PathHelper.IsEmpty(FilePathRelative.Empty));
         Assert.IsTrue(PathHelper.IsEmpty(DirectoryPathRelative.Empty));
         Assert.IsFalse(PathHelper.IsEmpty(new DirectoryPathRelative(@"..\Dir1")));
      }

      [Test, ExpectedException(typeof(ArgumentNullException))]
      public void Test_IsNullOrEmptyPathError() {
         Assert.IsTrue(PathHelper.IsEmpty(null));
      }


      [Test]
      public void Test_DoesPathHasThisPathMode() {
         Assert.IsTrue(PathHelper.DoesPathHasThisPathMode(FilePathAbsolute.Empty, PathMode.Absolute));
         Assert.IsTrue(PathHelper.DoesPathHasThisPathMode(DirectoryPathAbsolute.Empty, PathMode.Absolute));
         Assert.IsTrue(PathHelper.DoesPathHasThisPathMode(FilePathRelative.Empty, PathMode.Relative));
         Assert.IsTrue(PathHelper.DoesPathHasThisPathMode(DirectoryPathRelative.Empty, PathMode.Relative));
         Assert.IsTrue(PathHelper.DoesPathHasThisPathMode(new DirectoryPathRelative(@"..\Dir1"), PathMode.Relative));
         Assert.IsFalse(PathHelper.DoesPathHasThisPathMode(new DirectoryPathRelative(@"..\Dir1"), PathMode.Absolute));
         Assert.IsTrue(PathHelper.DoesPathHasThisPathMode(new DirectoryPathAbsolute(@"C:\Dir1"), PathMode.Absolute));
         Assert.IsFalse(PathHelper.DoesPathHasThisPathMode(new DirectoryPathAbsolute(@"C:\Dir1"), PathMode.Relative));
      }


      [Test, ExpectedException(typeof(ArgumentNullException))]
      public void Test_DoesPathHasThisPathModeError() {
         Assert.IsTrue(PathHelper.DoesPathHasThisPathMode(null, PathMode.Relative));
      }

      [Test, ExpectedException(typeof(ArgumentNullException))]
      public void Test_InvalidInputPathNull() {
         string reason;
         Assert.IsFalse(PathHelper.IsValidAbsolutePath(null, out reason));
         Assert.IsFalse(PathHelper.IsValidRelativePath(null, out reason));
         DirectoryPath directoryPath = new DirectoryPathAbsolute(null);
      }
      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_InvalidInputPathEmpty() {
         string reason;
         Assert.IsFalse(PathHelper.IsValidAbsolutePath(string.Empty, out reason));
         Assert.IsFalse(PathHelper.IsValidRelativePath(string.Empty, out reason));
         DirectoryPath directoryPath = new DirectoryPathAbsolute(string.Empty);
      }

      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_InvalidInputAbsolutePath1() {
         string reason;
         Assert.IsFalse(PathHelper.IsValidAbsolutePath("C", out reason));
         DirectoryPath directoryPath = new DirectoryPathAbsolute("C");
      }

      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_InvalidInputAbsolutePath2() {
         string reason;
         Assert.IsFalse(PathHelper.IsValidAbsolutePath(@"C\Dir1", out reason));
         DirectoryPath directoryPath = new DirectoryPathAbsolute(@"C\Dir1");
      }

      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_InvalidInputAbsolutePath3() {
         string reason;
         Assert.IsFalse(PathHelper.IsValidAbsolutePath(@"1:\Dir1", out reason));
         DirectoryPath directoryPath = new DirectoryPathAbsolute(@"1:\Dir1");
      }

      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_InvalidInputAbsoluteURNPath() {
         string reason;
         Assert.IsFalse(PathHelper.IsValidAbsolutePath(@"http://www.NDepend.com", out reason));
         DirectoryPath directoryPath = new DirectoryPathAbsolute(@"http://www.NDepend.com");
      }

      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_IncoherentPathModeException1() {
         string reason;
         Assert.IsFalse(PathHelper.IsValidAbsolutePath(@".", out reason));
         DirectoryPath directoryPath = new DirectoryPathAbsolute(@".");
      }
      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_IncoherentPathModeException2() {
         string reason;
         Assert.IsFalse(PathHelper.IsValidRelativePath(@"C:\", out reason));
         DirectoryPath directoryPath = new DirectoryPathRelative(@"C:\");
      }


      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_InvalidInputPathBadFormatting1() {
         string reason;
         Assert.IsFalse(PathHelper.IsValidAbsolutePath(@"C:\..", out reason));
         FilePath filePath = new FilePathAbsolute(@"C:\..");
      }

      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_InvalidInputPathBadFormatting3() {
         string reason;
         Assert.IsFalse(PathHelper.IsValidAbsolutePath(@"C:\..\Dir1\", out reason));
         FilePath filePath = new FilePathAbsolute(@"C:\..\Dir1\");
      }





      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_InvalidInputPathBadFormatting10() {
         string reason;
         Assert.IsFalse(PathHelper.IsValidRelativePath(@".\Dir1\..\..", out reason));
         FilePath filePath = new FilePathRelative(@".\Dir1\..\..");
      }



      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_InvalidInputPathBadFormatting14() {
         string reason;
         Assert.IsFalse(PathHelper.IsValidRelativePath(@"..\Dir1\..\..\Dir1\", out reason));
         FilePath filePath = new FilePathRelative(@"..\Dir1\..\..\Dir1\");
      }




      [Test]
      public void Test_PathModeOk() {
         DirectoryPath path = new DirectoryPathRelative(@".");
         Assert.IsTrue(path.IsRelativePath);

         path = new DirectoryPathAbsolute(@"C:\");
         Assert.IsTrue(path.IsAbsolutePath);

         path = new DirectoryPathRelative(@".\dir...1");
         Assert.IsTrue(path.IsRelativePath);

         path = new DirectoryPathAbsolute(@"C:\dir...1");
         Assert.IsTrue(path.IsAbsolutePath);
      }

      [Test]
      public void Test_BuildDirectoryPath() {
         DirectoryPath path = PathHelper.BuildDirectoryPath(@".\..\Dir1");
         Assert.IsTrue(path.IsRelativePath);

         path = PathHelper.BuildDirectoryPath(@"C:\Dir1\Dir2");
         Assert.IsTrue(path.IsAbsolutePath);
      }

      [Test]
      public void Test_NormalizePath() {
         DirectoryPath path = new DirectoryPathRelative(@".\");
         Assert.IsTrue(path.Path == ".");

         path = new DirectoryPathRelative(@".\\\");
         Assert.IsTrue(path.Path == ".");

         path = new DirectoryPathRelative(@".\\\..\\");
         Assert.IsTrue(path.Path == @".\..");

         path = new DirectoryPathRelative(@".\/dir1\//\dir2\/dir3///");
         Assert.IsTrue(path.Path == @".\dir1\dir2\dir3");

         path = new DirectoryPathAbsolute(@"C:/dir1/dir2");
         Assert.IsTrue(path.Path == @"C:\dir1\dir2");
      }


      [Test]
      public void Test_HasParentDir() {
         DirectoryPath path = new DirectoryPathRelative(@".\");
         Assert.IsFalse(path.HasParentDir);

         path = new DirectoryPathRelative(@".\Dir1");
         Assert.IsTrue(path.HasParentDir);

         path = new DirectoryPathRelative(@".\Dir1\Dir");
         Assert.IsTrue(path.HasParentDir);

         path = new DirectoryPathAbsolute(@"C:\\");
         Assert.IsFalse(path.HasParentDir);

         path = new DirectoryPathAbsolute(@"C:\Dir1");
         Assert.IsTrue(path.HasParentDir);
      }


      [Test, ExpectedException(typeof(InvalidOperationException))]
      public void Test_Error1OnParentDirectoryPath() {
         DirectoryPath path = new DirectoryPathRelative(@".\").ParentDirectoryPath;
      }

      [Test, ExpectedException(typeof(InvalidOperationException))]
      public void Test_Error2OnParentDirectoryPath() {
         DirectoryPath path = new DirectoryPathAbsolute(@"C:\").ParentDirectoryPath;
      }


      [Test]
      public void Test_ParentDirectoryPath() {
         DirectoryPath path = new FilePathRelative(@".\Dir1").ParentDirectoryPath;
         Assert.IsTrue(path.Path == @".");

         path = new DirectoryPathRelative(@".\Dir1\\Dir2").ParentDirectoryPath;
         Assert.IsTrue(path.Path == @".\Dir1");

         path = new DirectoryPathAbsolute(@"C:\Dir1").ParentDirectoryPath;
         Assert.IsTrue(path.Path == @"C:");

         path = new DirectoryPathRelative(@".\\Dir1").ParentDirectoryPath;
         Assert.IsTrue(path.Path == @".");

         path = new DirectoryPathAbsolute(@"C:\\\\Dir1\\Dir2").ParentDirectoryPath;
         Assert.IsTrue(path.Path == @"C:\Dir1");

         path = new DirectoryPathAbsolute(@"C:\dir1\\//dir2\").ParentDirectoryPath;
         Assert.IsTrue(path.Path == @"C:\dir1");
      }

      [Test]
      public void TestDirectoryName() {
         string directoryName = new DirectoryPathRelative(@".\").DirectoryName;
         Assert.IsTrue(directoryName == string.Empty);

         directoryName = new DirectoryPathAbsolute(@"C:\").DirectoryName;
         Assert.IsTrue(directoryName == string.Empty);

         directoryName = new DirectoryPathRelative(@".\\dir1\\/dir2").DirectoryName;
         Assert.IsTrue(directoryName == @"dir2");

         directoryName = new DirectoryPathAbsolute(@"C:\\\\dir1").DirectoryName;
         Assert.IsTrue(directoryName == @"dir1");

         directoryName = new DirectoryPathAbsolute(@"C:\dir1\\//dir2\").DirectoryName;
         Assert.IsTrue(directoryName == @"dir2");
      }

      //
      //  GetRelativePath
      //
      [Test]
      public void Test_GetRelativePath() {
         DirectoryPathAbsolute directoryPathTo, directoryPathFrom;


         directoryPathTo = new DirectoryPathAbsolute(@"C:\Dir1");
         directoryPathFrom = new DirectoryPathAbsolute(@"C:\Dir1");
         Assert.IsTrue(directoryPathTo.GetPathRelativeFrom(directoryPathFrom).Path == @".");
         Assert.IsTrue(directoryPathTo.CanGetPathRelativeFrom(directoryPathFrom));

         directoryPathTo = new DirectoryPathAbsolute(@"C:\Dir1\Dir2");
         directoryPathFrom = new DirectoryPathAbsolute(@"C:\Dir1\Dir3");
         Assert.IsTrue(directoryPathTo.GetPathRelativeFrom(directoryPathFrom).Path == @"..\Dir2");
         Assert.IsTrue(directoryPathTo.CanGetPathRelativeFrom(directoryPathFrom));

         directoryPathTo = new DirectoryPathAbsolute(@"C:\Dir1");
         directoryPathFrom = new DirectoryPathAbsolute(@"C:\Dir2");
         Assert.IsTrue(directoryPathTo.GetPathRelativeFrom(directoryPathFrom).Path == @"..\Dir1");
         Assert.IsTrue(directoryPathTo.CanGetPathRelativeFrom(directoryPathFrom));

         directoryPathTo = new DirectoryPathAbsolute(@"C:\Dir1\Dir2");
         directoryPathFrom = new DirectoryPathAbsolute(@"C:\Dir1");
         Assert.IsTrue(directoryPathTo.GetPathRelativeFrom(directoryPathFrom).Path == @".\Dir2");
         Assert.IsTrue(directoryPathTo.CanGetPathRelativeFrom(directoryPathFrom));
      }


      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_GetRelativePathWithError3() {
         DirectoryPathAbsolute directoryPathTo = new DirectoryPathAbsolute(@"C:\Dir1");
         DirectoryPathAbsolute directoryPathFrom = new DirectoryPathAbsolute(@"D:\Dir1");
         Assert.IsFalse(directoryPathTo.CanGetPathRelativeFrom(directoryPathFrom));
         directoryPathTo.GetPathRelativeFrom(directoryPathFrom);
      }

      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_GetRelativePathWithError4() {
         DirectoryPathAbsolute directoryPathTo = DirectoryPathAbsolute.Empty;
         DirectoryPathAbsolute directoryPathFrom = new DirectoryPathAbsolute(@"D:\Dir1");
         Assert.IsFalse(directoryPathTo.CanGetPathRelativeFrom(directoryPathFrom));
         directoryPathTo.GetPathRelativeFrom(directoryPathFrom);
      }

      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_GetRelativePathWithError5() {
         DirectoryPathAbsolute directoryPathTo = new DirectoryPathAbsolute(@"C:\Dir1");
         DirectoryPathAbsolute directoryPathFrom = DirectoryPathAbsolute.Empty;
         Assert.IsFalse(directoryPathTo.CanGetPathRelativeFrom(directoryPathFrom));
         directoryPathTo.GetPathRelativeFrom(directoryPathFrom);
      }

      [Test, ExpectedException(typeof(ArgumentNullException))]
      public void Test_GetRelativePathWithError6() {
         DirectoryPathAbsolute directoryPathTo = new DirectoryPathAbsolute(@"C:\Dir1");
         Assert.IsFalse(directoryPathTo.CanGetPathRelativeFrom(null));
         directoryPathTo.GetPathRelativeFrom(null);
      }

      //
      //  GetAbsolutePath
      //
      [Test]
      public void Test_GetAbsolutePath() {
         DirectoryPathRelative directoryPathTo;
         DirectoryPathAbsolute directoryPathFrom;

         directoryPathTo = new DirectoryPathRelative(@"..");
         directoryPathFrom = new DirectoryPathAbsolute(@"C:\Dir1");
         Assert.IsTrue(directoryPathTo.GetAbsolutePathFrom(directoryPathFrom).Path == @"C:");
         Assert.IsTrue(directoryPathTo.CanGetAbsolutePathFrom(directoryPathFrom));

         directoryPathTo = new DirectoryPathRelative(@".");
         directoryPathFrom = new DirectoryPathAbsolute(@"C:\Dir1");
         Assert.IsTrue(directoryPathTo.GetAbsolutePathFrom(directoryPathFrom).Path == @"C:\Dir1");
         Assert.IsTrue(directoryPathTo.CanGetAbsolutePathFrom(directoryPathFrom));

         directoryPathTo = new DirectoryPathRelative(@"..\Dir2");
         directoryPathFrom = new DirectoryPathAbsolute(@"C:\Dir1");
         Assert.IsTrue(directoryPathTo.GetAbsolutePathFrom(directoryPathFrom).Path == @"C:\Dir2");
         Assert.IsTrue(directoryPathTo.CanGetAbsolutePathFrom(directoryPathFrom));

         directoryPathTo = new DirectoryPathRelative(@"..\..\Dir4\Dir5");
         directoryPathFrom = new DirectoryPathAbsolute(@"C:\Dir1\Dir2\Dir3");
         Assert.IsTrue(directoryPathTo.GetAbsolutePathFrom(directoryPathFrom).Path == @"C:\Dir1\Dir4\Dir5");
         Assert.IsTrue(directoryPathTo.CanGetAbsolutePathFrom(directoryPathFrom));

         directoryPathTo = new DirectoryPathRelative(@".\..\Dir4\Dir5");
         directoryPathFrom = new DirectoryPathAbsolute(@"C:\Dir1\Dir2\Dir3");
         Assert.IsTrue(directoryPathTo.GetAbsolutePathFrom(directoryPathFrom).Path == @"C:\Dir1\Dir2\Dir4\Dir5");
         Assert.IsTrue(directoryPathTo.CanGetAbsolutePathFrom(directoryPathFrom));
      }

      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_GetAbsolutePathPathWithError3() {
         DirectoryPathRelative directoryPathTo = new DirectoryPathRelative(@"..\..\Dir1");
         DirectoryPathAbsolute directoryPathFrom = new DirectoryPathAbsolute(@"C:\Dir1");
         Assert.IsFalse(directoryPathTo.CanGetAbsolutePathFrom(directoryPathFrom));
         directoryPathTo.GetAbsolutePathFrom(directoryPathFrom);
      }

      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_GetAbsolutePathPathWithError4() {
         DirectoryPathRelative directoryPathTo = new DirectoryPathRelative(@"..\..\Dir1");
         DirectoryPathAbsolute directoryPathFrom = DirectoryPathAbsolute.Empty;
         Assert.IsFalse(directoryPathTo.CanGetAbsolutePathFrom(directoryPathFrom));
         directoryPathTo.GetAbsolutePathFrom(directoryPathFrom);
      }
      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_GetAbsolutePathPathWithError5() {
         DirectoryPathRelative directoryPathTo = DirectoryPathRelative.Empty;
         DirectoryPathAbsolute directoryPathFrom = new DirectoryPathAbsolute(@"C:\Dir1");
         Assert.IsFalse(directoryPathTo.CanGetAbsolutePathFrom(directoryPathFrom));
         directoryPathTo.GetAbsolutePathFrom(directoryPathFrom);
      }
      [Test, ExpectedException(typeof(ArgumentNullException))]
      public void Test_GetAbsolutePathPathWithError6() {
         DirectoryPathRelative directoryPathTo = new DirectoryPathRelative(@"..\..\Dir1");
         Assert.IsFalse(directoryPathTo.CanGetAbsolutePathFrom(null));
         directoryPathTo.GetAbsolutePathFrom(null);
      }
      //
      //  PathComparison
      //
      [Test]
      public void Test_PathEquality() {
         Assert.IsFalse(DirectoryPathAbsolute.Empty.Equals(null));
         Assert.IsFalse(DirectoryPathAbsolute.Empty == null);

         Assert.IsTrue(DirectoryPathAbsolute.Empty.Equals(DirectoryPathRelative.Empty));
         Assert.IsTrue(DirectoryPathAbsolute.Empty == DirectoryPathRelative.Empty);

         Assert.IsFalse(DirectoryPathAbsolute.Empty.Equals(new DirectoryPathRelative(@"..\Dir1\Dir2")));
         Assert.IsFalse(DirectoryPathAbsolute.Empty == new DirectoryPathRelative(@"..\Dir1\Dir2"));

         //
         // directoryPathRelative 
         //
         DirectoryPathRelative directoryPathRelative1 = new DirectoryPathRelative(@"..\Dir1\Dir2");
         DirectoryPathRelative directoryPathRelative2 = new DirectoryPathRelative(@"..\\dir1//DIR2/");
         Assert.IsTrue(directoryPathRelative1.Equals(directoryPathRelative2));
         Assert.IsTrue(directoryPathRelative1 == directoryPathRelative2);

         directoryPathRelative1 = new DirectoryPathRelative(@"..\Dir1\Dir2");
         directoryPathRelative2 = new DirectoryPathRelative(@".\Dir1\Dir2");
         Assert.IsFalse(directoryPathRelative1.Equals(directoryPathRelative2));
         Assert.IsFalse(directoryPathRelative1 == directoryPathRelative2);

         directoryPathRelative1 = new DirectoryPathRelative(@"..\Dir1\Dir2");
         directoryPathRelative2 = new DirectoryPathRelative(@"..\Dir1\Dir2\Dir3");
         Assert.IsFalse(directoryPathRelative1.Equals(directoryPathRelative2));
         Assert.IsTrue(directoryPathRelative1 != directoryPathRelative2);

         directoryPathRelative1 = new DirectoryPathRelative(@"..\Dir1\Dir2");
         directoryPathRelative2 = new DirectoryPathRelative(@"..\Dir1\Dir");
         Assert.IsFalse(directoryPathRelative1.Equals(directoryPathRelative2));
         Assert.IsTrue(directoryPathRelative1 != directoryPathRelative2);

         //
         // directoryPathAbsolute 
         //
         DirectoryPathAbsolute directoryPathAbsolute1 = new DirectoryPathAbsolute(@"C:\Dir1\Dir2");
         DirectoryPathAbsolute directoryPathAbsolute2 = new DirectoryPathAbsolute(@"C:\\dir1//Dir2\\");
         Assert.IsTrue(directoryPathAbsolute1.Equals(directoryPathAbsolute2));
         Assert.IsFalse(directoryPathAbsolute1 != directoryPathAbsolute2);

         directoryPathAbsolute1 = new DirectoryPathAbsolute(@"C:\Dir1\Dir2");
         directoryPathAbsolute2 = new DirectoryPathAbsolute(@"D:\Dir1\Dir2");
         Assert.IsFalse(directoryPathAbsolute1.Equals(directoryPathAbsolute2));
         Assert.IsFalse(directoryPathAbsolute1 == directoryPathAbsolute2);

         directoryPathAbsolute1 = new DirectoryPathAbsolute(@"C:\Dir1\Dir2");
         directoryPathAbsolute2 = new DirectoryPathAbsolute(@"C:\Dir1\Dir2\Dir2");
         Assert.IsFalse(directoryPathAbsolute1.Equals(directoryPathAbsolute2));
         Assert.IsFalse(directoryPathAbsolute1 == directoryPathAbsolute2);

         directoryPathAbsolute1 = new DirectoryPathAbsolute(@"C:\Dir1\Dir2");
         directoryPathAbsolute2 = new DirectoryPathAbsolute(@"C:\Dir1\Dir");
         Assert.IsFalse(directoryPathAbsolute1.Equals(directoryPathAbsolute2));
         Assert.IsFalse(directoryPathAbsolute1 == directoryPathAbsolute2);

         //
         // Mix between directoryPathAbsolute and directoryPathRelative
         //
         directoryPathRelative1 = new DirectoryPathRelative(@"..\Dir1\Dir2");
         directoryPathAbsolute1 = new DirectoryPathAbsolute(@"C:\Dir1\Dir2");
         Assert.IsFalse(directoryPathAbsolute1.Equals(directoryPathRelative1));
         Assert.IsFalse(directoryPathAbsolute1 == directoryPathRelative1);
      }



      //
      //  Get Child With Name
      //
      [Test]
      public void Test_GetChildWithName() {
         DirectoryPathAbsolute directoryPathAbsolute = new DirectoryPathAbsolute(@"C:\Dir1\Dir2");
         Assert.IsTrue(directoryPathAbsolute.GetChildFileWithName("File.txt").Path == @"C:\Dir1\Dir2\File.txt");
         Assert.IsTrue(directoryPathAbsolute.GetChildDirectoryWithName("Dir3").Path == @"C:\Dir1\Dir2\Dir3");

         DirectoryPathRelative directoryPathRelative = new DirectoryPathRelative(@"..\..\Dir1\Dir2");
         Assert.IsTrue(directoryPathRelative.GetChildFileWithName("File.txt").Path == @"..\..\Dir1\Dir2\File.txt");
         Assert.IsTrue(directoryPathRelative.GetChildDirectoryWithName("Dir3").Path == @"..\..\Dir1\Dir2\Dir3");
      }

      [Test, ExpectedException(typeof(ArgumentNullException))]
      public void Test_GetChildWithName_Error1() {
         DirectoryPathAbsolute directoryPathAbsolute = new DirectoryPathAbsolute(@"C:\Dir1\Dir2");
         directoryPathAbsolute.GetChildFileWithName(null);
      }

      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_GetChildWithName_Error2() {
         DirectoryPathAbsolute directoryPathAbsolute = new DirectoryPathAbsolute(@"C:\Dir1\Dir2");
         directoryPathAbsolute.GetChildFileWithName(String.Empty);
      }

      [Test, ExpectedException(typeof(ArgumentNullException))]
      public void Test_GetChildWithName_Error3() {
         DirectoryPathAbsolute directoryPathAbsolute = new DirectoryPathAbsolute(@"C:\Dir1\Dir2");
         directoryPathAbsolute.GetChildDirectoryWithName(null);
      }

      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_GetChildWithName_Error4() {
         DirectoryPathAbsolute directoryPathAbsolute = new DirectoryPathAbsolute(@"C:\Dir1\Dir2");
         directoryPathAbsolute.GetChildDirectoryWithName(String.Empty);
      }

      [Test, ExpectedException(typeof(InvalidOperationException))]
      public void Test_GetChildWithName_Error5() {
         DirectoryPathAbsolute.Empty.GetChildFileWithName(@"File.txt");
      }

      [Test, ExpectedException(typeof(InvalidOperationException))]
      public void Test_GetChildWithName_Error55() {
         DirectoryPathAbsolute.Empty.GetChildDirectoryWithName(@"Dir");
      }



      [Test, ExpectedException(typeof(ArgumentNullException))]
      public void Test_GetChildWithName_Error6() {
         DirectoryPathRelative directoryPathRelative = new DirectoryPathRelative(@"..\Dir1\Dir2");
         directoryPathRelative.GetChildFileWithName(null);
      }

      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_GetChildWithName_Error7() {
         DirectoryPathRelative directoryPathRelative = new DirectoryPathRelative(@"..\Dir1\Dir2");
         directoryPathRelative.GetChildFileWithName(String.Empty);
      }

      [Test, ExpectedException(typeof(ArgumentNullException))]
      public void Test_GetChildWithName_Error8() {
         DirectoryPathRelative directoryPathRelative = new DirectoryPathRelative(@"..\Dir1\Dir2");
         directoryPathRelative.GetChildDirectoryWithName(null);
      }

      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_GetChildWithName_Error9() {
         DirectoryPathRelative directoryPathRelative = new DirectoryPathRelative(@"..\Dir1\Dir2");
         directoryPathRelative.GetChildDirectoryWithName(String.Empty);
      }

      [Test, ExpectedException(typeof(InvalidOperationException))]
      public void Test_GetChildWithName_Error10() {
         DirectoryPathRelative.Empty.GetChildFileWithName(@"File.txt");
      }

      [Test, ExpectedException(typeof(InvalidOperationException))]
      public void Test_GetChildWithName_Error100() {
         DirectoryPathRelative.Empty.GetChildDirectoryWithName(@"Dir");
      }



      //
      //  IsChildDirectoryOf
      //
      [Test, ExpectedException(typeof(ArgumentNullException))]
      public void Test_GetBrotherWithName_Error1() {
         new DirectoryPathAbsolute(@"C:\Dir1\Dir2").IsChildDirectoryOf(null);
      }
      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_GetBrotherWithName_Error2() {
         new DirectoryPathAbsolute(@"C:\Dir1\Dir2").IsChildDirectoryOf(DirectoryPathAbsolute.Empty);
      }
      [Test]
      public void Test_IsChildDirectoryOf() {
         Assert.IsTrue(new DirectoryPathAbsolute(@"C:\Dir1\Dir2\Dir3").IsChildDirectoryOf(
            new DirectoryPathAbsolute(@"C:\dir1\dir2\")));
         Assert.IsTrue(new DirectoryPathAbsolute(@"C:\Dir1\Dir2\Dir3").IsChildDirectoryOf(
            new DirectoryPathAbsolute(@"c:\Dir1\")));
         Assert.IsTrue(new DirectoryPathAbsolute(@"C:\Dir1\Dir2\Dir3").IsChildDirectoryOf(
            new DirectoryPathAbsolute(@"c:\")));

         Assert.IsFalse(new DirectoryPathAbsolute(@"C:\Dir1\Dir2\Dir3").IsChildDirectoryOf(
            new DirectoryPathAbsolute(@"E:\")));
         Assert.IsFalse(new DirectoryPathAbsolute(@"C:\Dir1\Dir2\Dir3").IsChildDirectoryOf(
            new DirectoryPathAbsolute(@"C:\Dir2")));
         Assert.IsFalse(DirectoryPathAbsolute.Empty.IsChildDirectoryOf(
            new DirectoryPathAbsolute(@"C:\Dir2")));

      }


      //
      //  GetBrother
      //
      [Test, ExpectedException(typeof(ArgumentNullException))]
      public void Test_GetBrotherWithName_Error10() {
         new DirectoryPathAbsolute(@"C:\Dir1\Dir2").GetBrotherDirectoryWithName(null);
      }
      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_GetBrotherWithName_Error11() {
         new DirectoryPathAbsolute(@"C:\Dir1\Dir2").GetBrotherDirectoryWithName(string.Empty);
      }
      [Test, ExpectedException(typeof(InvalidOperationException))]
      public void Test_GetBrotherWithName_Error12() {
         DirectoryPathAbsolute.Empty.GetBrotherDirectoryWithName("Dir1");
      }

      [Test, ExpectedException(typeof(ArgumentNullException))]
      public void Test_GetBrotherWithName_Error20() {
         new DirectoryPathRelative(@"..\Dir1\Dir2").GetBrotherDirectoryWithName(null);
      }
      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_GetBrotherWithName_Error21() {
         new DirectoryPathRelative(@"..\Dir1\Dir2").GetBrotherDirectoryWithName(string.Empty);
      }
      [Test, ExpectedException(typeof(InvalidOperationException))]
      public void Test_GetBrotherWithName_Error22() {
         DirectoryPathRelative.Empty.GetBrotherDirectoryWithName("Dir1");
      }


      [Test, ExpectedException(typeof(ArgumentNullException))]
      public void Test_GetBrotherWithName_Error30() {
         new DirectoryPathAbsolute(@"C:\Dir1\Dir2").GetBrotherFileWithName(null);
      }
      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_GetBrotherWithName_Error31() {
         new DirectoryPathAbsolute(@"C:\Dir1\Dir2").GetBrotherFileWithName(string.Empty);
      }
      [Test, ExpectedException(typeof(InvalidOperationException))]
      public void Test_GetBrotherWithName_Error32() {
         DirectoryPathAbsolute.Empty.GetBrotherFileWithName("File.txt");
      }

      [Test, ExpectedException(typeof(ArgumentNullException))]
      public void Test_GetBrotherWithName_Error40() {
         new DirectoryPathRelative(@"..\Dir1\Dir2").GetBrotherFileWithName(null);
      }
      [Test, ExpectedException(typeof(ArgumentException))]
      public void Test_GetBrotherWithName_Error41() {
         new DirectoryPathRelative(@"..\Dir1\Dir2").GetBrotherFileWithName(string.Empty);
      }
      [Test, ExpectedException(typeof(InvalidOperationException))]
      public void Test_GetBrotherWithName_Error42() {
         DirectoryPathRelative.Empty.GetBrotherFileWithName("File.txt");
      }


      [Test]
      public void Test_GetBrotherWithName() {
         Assert.IsTrue(new DirectoryPathAbsolute(@"C:\Dir1\Dir2").GetBrotherDirectoryWithName("Dir3") ==
            new DirectoryPathAbsolute(@"C:\Dir1\Dir3"));

         Assert.IsTrue(new DirectoryPathRelative(@"..\Dir1\Dir2").GetBrotherDirectoryWithName("Dir3") ==
            new DirectoryPathRelative(@"..\Dir1\Dir3"));

         Assert.IsTrue(new DirectoryPathAbsolute(@"C:\Dir1\Dir2").GetBrotherFileWithName("File.txt") ==
            new FilePathAbsolute(@"C:\Dir1\File.txt"));

         Assert.IsTrue(new DirectoryPathRelative(@"..\Dir1\Dir2").GetBrotherFileWithName("File.txt") ==
            new FilePathRelative(@"..\Dir1\File.txt"));
      }

   }
}
