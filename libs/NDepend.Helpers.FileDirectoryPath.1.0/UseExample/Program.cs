//-------------------------------------------------------
//
//  These sources are provided by the company SMACCHIA.COM SARL
//  Download the trial edition of our flagship product: http://www.NDepend.com
//  NDepend is a tool for .NET developers.
//  It is a static analyzer that simplifies managing a complex .NET code base
//
//-------------------------------------------------------

using NDepend.Helpers.FileDirectoryPath;
using System.Diagnostics;
using System.Collections.Generic;

class Program {
   static void Main(string[] args) {
      FilePathAbsolute filePathAbsolute1, filePathAbsolute2;
      FilePathRelative filePathRelative1;
      DirectoryPathAbsolute directoryPathAbsolute1;
      DirectoryPathRelative directoryPathRelative1;



      //  Path normalization
      filePathAbsolute1 = new FilePathAbsolute(@"C:/Dir1\\File.txt");
      Debug.Assert(filePathAbsolute1.Path == @"C:\Dir1\File.txt");

      directoryPathAbsolute1 = new DirectoryPathAbsolute(@"C:/Dir1\\Dir2\");
      Debug.Assert(directoryPathAbsolute1.Path == @"C:\Dir1\Dir2");

      directoryPathAbsolute1 = new DirectoryPathAbsolute(@"C:\Dir1\..\Dir2\.");
      Debug.Assert(directoryPathAbsolute1.Path == @"C:\Dir2");



      // Path comparison
      filePathAbsolute1 = new FilePathAbsolute(@"C:/Dir1\\File.txt");
      filePathAbsolute2 = new FilePathAbsolute(@"C:\DIR1\FILE.TXT");
      Debug.Assert(filePathAbsolute1.Equals(filePathAbsolute2));
      Debug.Assert(filePathAbsolute1 == filePathAbsolute2);



      // Relative -> Absolute path conversion
      filePathRelative1 = new FilePathRelative(@"..\..\Dir1\File.txt");
      directoryPathAbsolute1 = new DirectoryPathAbsolute(@"C:\Dir2\Dir3\Dir4");
      filePathAbsolute1 = filePathRelative1.GetAbsolutePathFrom(directoryPathAbsolute1);
      Debug.Assert( filePathAbsolute1.Path == @"C:\Dir2\Dir1\File.txt");



      // Absolute -> Relative path conversion
      filePathAbsolute1 = new FilePathAbsolute(@"C:\Dir1\File.txt");
      directoryPathAbsolute1 = new DirectoryPathAbsolute(@"C:\Dir2\Dir3\Dir4");
      filePathRelative1 = filePathAbsolute1.GetPathRelativeFrom(directoryPathAbsolute1);
      Debug.Assert(filePathRelative1.Path == @"..\..\..\Dir1\File.txt");



      // Path string validation
      string reason;
      Debug.Assert(PathHelper.IsValidAbsolutePath(@"C:\Dir2\Dir1", out reason));
      Debug.Assert(!PathHelper.IsValidAbsolutePath(@"C:\..\Dir1", out reason));
      Debug.Assert(!PathHelper.IsValidAbsolutePath(@".\Dir1", out reason));
      Debug.Assert(!PathHelper.IsValidAbsolutePath(@"1:\Dir1", out reason));
      Debug.Assert(PathHelper.IsValidRelativePath(@".\Dir1\Dir2", out reason));
      Debug.Assert(PathHelper.IsValidRelativePath(@"..\Dir1\Dir2", out reason));
      Debug.Assert(PathHelper.IsValidRelativePath(@".\Dir1\..\Dir2", out reason));
      Debug.Assert(!PathHelper.IsValidRelativePath(@".\Dir1\..\..\Dir2", out reason));
      Debug.Assert(!PathHelper.IsValidRelativePath(@"C:\Dir1\Dir2", out reason));



      // File name & extension
      filePathAbsolute1 = new FilePathAbsolute(@"C:\Dir1\File.cs.Txt");
      Debug.Assert(filePathAbsolute1.FileName == "File.cs.Txt");
      Debug.Assert(filePathAbsolute1.FileNameWithoutExtension == "File.cs");
      Debug.Assert(filePathAbsolute1.FileExtension == ".Txt");
      Debug.Assert(filePathAbsolute1.HasExtension(".txt"));



      // Path browsing
      filePathAbsolute1 = new FilePathAbsolute(@"C:\Dir1\File.cs.Txt");
      Debug.Assert(filePathAbsolute1.ParentDirectoryPath.Path == @"C:\Dir1");
      Debug.Assert(filePathAbsolute1.GetBrotherFileWithName("File.xml").Path == @"C:\Dir1\File.xml");
      Debug.Assert(filePathAbsolute1.ParentDirectoryPath.GetChildDirectoryWithName("Dir2").Path == @"C:\Dir1\Dir2");
      Debug.Assert(filePathAbsolute1.ParentDirectoryPath.GetChildDirectoryWithName("..").Path == @"C:");

      directoryPathRelative1 = new DirectoryPathRelative(@"..\Dir1\Dir2");
      Debug.Assert(directoryPathRelative1.ParentDirectoryPath.Path == @"..\Dir1");



      // Path rebasing
      directoryPathAbsolute1 = new DirectoryPathAbsolute(@"C:\Dir1\Dir2\Dir3");
      DirectoryPathAbsolute directoryPathAbsolute2 = new DirectoryPathAbsolute(@"E:\Dir4\Dir1");
      DirectoryPathAbsolute rebasedPath;
      PathHelper.TryRebasePath(directoryPathAbsolute1, directoryPathAbsolute2, out rebasedPath);
      Debug.Assert(rebasedPath.Path == @"E:\Dir4\Dir1\Dir2\Dir3");


      // List of path  ListOfPathsEquals \ Contains \ TryGetCommonRootDirectory 
      List<DirectoryPathAbsolute> list1 = new List<DirectoryPathAbsolute>();
      List<DirectoryPathAbsolute> list2 = new List<DirectoryPathAbsolute>();
      list1.Add(new DirectoryPathAbsolute(@"C:\Dir1\Dir2"));
      list2.Add(new DirectoryPathAbsolute(@"c:\dir1\dir2"));
      list1.Add(new DirectoryPathAbsolute(@"C:\Dir1\Dir3\Dir4"));
      list2.Add(new DirectoryPathAbsolute(@"c:\dir1\dir3\dir4"));
      Debug.Assert(ListOfPathHelper.ListOfPathsEquals(list1, list2));
      Debug.Assert(ListOfPathHelper.Contains(list1, new DirectoryPathAbsolute(@"C:\Dir1\dir2")));
      ListOfPathHelper.TryGetCommonRootDirectory(list1, out directoryPathAbsolute1);
      Debug.Assert(directoryPathAbsolute1.Path == @"C:\Dir1");


      // List of path   GetListOfUniqueDirsAndUniqueFileNames
      List<FilePathAbsolute> list = new List<FilePathAbsolute>();
      list.Add(new FilePathAbsolute(@"E:\Dir1\Dir2\File1.txt"));
      list.Add(new FilePathAbsolute(@"E:\dir1\dir2\File2.txt"));
      list.Add(new FilePathAbsolute(@"E:\Dir1\Dir2\Dir3\file2.txt"));
      List<DirectoryPathAbsolute> listOfUniqueDirs;
      List<string> listOfUniqueFileNames;
      ListOfPathHelper.GetListOfUniqueDirsAndUniqueFileNames(list, out listOfUniqueDirs, out listOfUniqueFileNames);
      Debug.Assert(listOfUniqueDirs.Count == 2);
      Debug.Assert(listOfUniqueDirs[0].Path == @"E:\Dir1\Dir2");
      Debug.Assert(listOfUniqueDirs[1].Path == @"E:\Dir1\Dir2\Dir3");
      Debug.Assert(listOfUniqueFileNames.Count == 2);
      Debug.Assert(listOfUniqueFileNames[0] == "File1.txt");
      Debug.Assert(listOfUniqueFileNames[1] == "File2.txt");


      // Interaction with System.IO API
      filePathAbsolute1 = new FilePathAbsolute(
         System.Reflection.Assembly.GetExecutingAssembly().Location);
      Debug.Assert(filePathAbsolute1.Exists);
      System.IO.FileInfo fileInfo = filePathAbsolute1.FileInfo;

      directoryPathAbsolute1 = filePathAbsolute1.ParentDirectoryPath as DirectoryPathAbsolute;
      Debug.Assert(directoryPathAbsolute1.Exists);
      System.IO.DirectoryInfo directoryInfo = directoryPathAbsolute1.DirectoryInfo;

      List<DirectoryPathAbsolute> listSubDir = directoryPathAbsolute1.ChildrenDirectoriesPath;
      List<FilePathAbsolute> listSubFile = directoryPathAbsolute1.ChildrenFilesPath;

   }
}

