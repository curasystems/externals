/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunterze@gmail.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4che2.tool.dcmdir;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.DicomObjectToStringParam;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.StopTagInputHandler;
import org.dcm4che2.media.ApplicationProfile;
import org.dcm4che2.media.DicomDirReader;
import org.dcm4che2.media.DicomDirWriter;
import org.dcm4che2.media.FileSetInformation;
import org.dcm4che2.media.StdGenJPEGApplicationProfile;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 13837 $ $Date: 2010-08-02 14:17:56 +0200 (Mon, 02 Aug 2010) $
 * @since Jul 20, 2006
 */
public class DcmDir {

    private static final int DEF_MAX_WIDTH = 78;
    private static final int MIN_MAX_WIDTH = 32;
    private static final int MAX_MAX_WIDTH = 512;
    private static final int DEF_MAX_VAL_LEN = 64;
    private static final int MIN_MAX_VAL_LEN = 16;
    private static final int MAX_MAX_VAL_LEN = 512;      
    private static final String USAGE = 
	"dcmdir -{acdptz} <dicomdir> [Options] [<file>..][<directory>..]";
    private static final String DESCRIPTION = 
	"Dump/Create/Update/Compact DICOM directory file\nOptions:";
    private static final String EXAMPLE = 
	"--\nExample 1: to dump content of DICOMDIR to stdout:" +
	"\n$ dicomdir -t /media/cdrom/DICOMDIR" +
	"\n--\nExample 2: to create a new directory file with specified File-set" +
	" ID and Descriptor File, referencing all DICOM Files in directory" +
	" disk99/DICOM:" +
	"\n$ dicomdir -c disk99/DICOMDIR -id DISK99 -desc disk99/README" +
	" disk99/DICOM\n" +
	"\n--\nExample 3: to add directory records referencing all DICOM files in" +
	" directory disk99/DICOM/CT1 to existing directory file:" +
	"\n$ dicomdir -a disk99/DICOMDIR disk99/DICOM/CT1" +
	"\n--\nExample 4: to delete/deactivate directory records referencing" +
	" DICOM files in directory disk99/DICOM/CT2:" +
	"\n$ dicomdir -d disk99/DICOMDIR disk99/DICOM/CT2" +
	"\n--\nExample 5: to purge directory records without child records" +
	" referencing any DICOM file:" +
	"\n$ dicomdir -p disk99/DICOMDIR" +
	"\n--\nExample 6: to compact DICOMDIR by removing inactive records:" +
	"\n$ dicomdir -z disk99/DICOMDIR";

    private final File file;
    private DicomDirReader dicomdir;
    private FileSetInformation fsinfo;
    private ApplicationProfile ap = new StdGenJPEGApplicationProfile();
    private int maxValLen = DEF_MAX_VAL_LEN;
    private int maxWidth = DEF_MAX_WIDTH;
    private boolean checkDuplicate = false;

    public DcmDir(File file) throws IOException {
        this.file = file.getCanonicalFile();
    }

    private DicomDirWriter writer() {
	return ((DicomDirWriter) dicomdir);
    }
    
    public final void setMaxValLen(int maxValLen) {
        this.maxValLen = maxValLen;
    }

    public final void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public final void setCheckDuplicate(boolean b) {
	this.checkDuplicate = b;	
    }
    
    public final void fsinfo(FileSetInformation fsinfo) {
	BasicDicomObject dest = new BasicDicomObject();
	fsinfo.getDicomObject().copyTo(dest);
	this.fsinfo = new FileSetInformation(dest);
    }
    
    public FileSetInformation fsinfo() {
        if (fsinfo == null) {
            fsinfo = new FileSetInformation();
            fsinfo.init();
        }
        return fsinfo;
    }

    public void create() throws IOException {
        dicomdir = new DicomDirWriter(file, fsinfo());        
    }

    public void openRO() throws IOException {
        dicomdir = new DicomDirReader(file);        
        fsinfo = dicomdir.getFileSetInformation();
    }

    public void open() throws IOException {
        dicomdir = new DicomDirWriter(file); 
        fsinfo = dicomdir.getFileSetInformation();
    }
    
    public void setShowInactiveRecords(boolean b) {
	dicomdir.setShowInactiveRecords(b);
    }

    public void dump() throws IOException {
	DicomObjectToStringParam param = new DicomObjectToStringParam(true,
		maxValLen, Integer.MAX_VALUE, maxWidth,
		Integer.MAX_VALUE, "",
		System.getProperty("line.separator", "\n"));
	StringBuffer sb = new StringBuffer(512);	
        FileSetInformation fileSetInfo = dicomdir.getFileSetInformation();
	fileSetInfo.getDicomObject().toStringBuffer(sb, param);
        System.out.println(sb.toString());
        dump(dicomdir.findFirstRootRecord(), param, "", sb);
    }

    private void dump(DicomObject firstRec, DicomObjectToStringParam param, 
	    String id, StringBuffer sb) throws IOException {
	int i = 1;
	for (DicomObject rec = firstRec; rec != null;
		rec = dicomdir.findNextSiblingRecord(rec), ++i) {
	    sb.setLength(0);
	    rec.toStringBuffer(sb, param);
	    System.out.println("" + rec.getItemOffset() + ": "
		    + rec.getString(Tag.DirectoryRecordType) + " - " + id + i);
	    System.out.println(sb.toString());
	    dump(dicomdir.findFirstChildRecord(rec), param,
		    id + i + '.', sb);
	}
    }

    public int purge() throws IOException {
	return writer().purge();
    }
        
    public void setSpecificCharacterSetofFileSetDescriptorFile(String cs) {       
        fsinfo().setSpecificCharacterSetofFileSetDescriptorFile(cs);
    }

    public void setFileSetDescriptorFileID(String fname) {
        fsinfo().setFileSetDescriptorFile(new File(fname), file.getParentFile());
    }

    public void setMediaStorageSOPInstanceUID(String ui) {
        fsinfo().setMediaStorageSOPInstanceUID(ui);
    }

    public void setFileSetID(String cs) {
        fsinfo().setFileSetID(cs);
    }

    public void setExplicitItemLength(boolean b) {
	writer().setExplicitItemLength(b);    
    }

    public void setExplicitSequenceLength(boolean b) {
       writer().setExplicitSequenceLength(b);    
    }

    public int addFile(File f) throws IOException {
        f = f.getCanonicalFile();
        // skip adding DICOMDIR
        if (f.equals(file)) return 0;
        int n = 0;
        if (f.isDirectory()) {
            File[] fs = f.listFiles();
            for (int i = 0; i < fs.length; i++) {
                n += addFile(fs[i]);
            }
            return n;
        }
        DicomInputStream in = new DicomInputStream(f);
        in.setHandler(new StopTagInputHandler(Tag.PixelData));
        DicomObject dcmobj =  in.readDicomObject();
        DicomObject patrec = ap.makePatientDirectoryRecord(dcmobj);
        DicomObject styrec = ap.makeStudyDirectoryRecord(dcmobj);
        DicomObject serrec = ap.makeSeriesDirectoryRecord(dcmobj);
        DicomObject instrec = 
            	ap.makeInstanceDirectoryRecord(dcmobj, dicomdir.toFileID(f));

        DicomObject rec = writer().addPatientRecord(patrec);
        if (rec == patrec) {
            ++n;
        }
        rec = writer().addStudyRecord(rec, styrec);
        if (rec == styrec) {
            ++n;
        }
        rec = writer().addSeriesRecord(rec, serrec);
        if (rec == serrec) {
            ++n;
        }
        if (n == 0 && checkDuplicate) {
            String iuid = dcmobj.getString(Tag.MediaStorageSOPInstanceUID);
            if (dicomdir.findInstanceRecord(rec, iuid) != null) {
                System.out.print('D');
        	return 0;
            }
        }
        writer().addChildRecord(rec, instrec);
        System.out.print('.');
        return n + 1;
    }


    public int delFile(File f) throws IOException {
        if (f.isDirectory()) {
            int n = 0;
            File[] fs = f.listFiles();
            for (int i = 0; i < fs.length; i++) {
                n += delFile(fs[i]);
            }
            return n;
        }
        DicomInputStream in = new DicomInputStream(f);
        in.setHandler(new StopTagInputHandler(Tag.SeriesInstanceUID + 1));
        DicomObject dcmobj =  in.readDicomObject();
        String pid = dcmobj.getString(Tag.PatientID);
        DicomObject pat = dicomdir.findPatientRecord(pid);
        if (pat == null) {
            return 0;
        }
        String styuid = dcmobj.getString(Tag.StudyInstanceUID);
        DicomObject sty = dicomdir.findStudyRecord(pat, styuid);
        if (sty == null) {
            return 0;
        }
        String seruid = dcmobj.getString(Tag.SeriesInstanceUID);
        DicomObject ser = dicomdir.findSeriesRecord(sty, seruid);
        if (ser == null) {
            return 0;
        }
        String iuid = dcmobj.getString(Tag.MediaStorageSOPInstanceUID);
        DicomObject rec = dicomdir.findInstanceRecord(ser, iuid);
        if (rec == null) {
            return 0;
        }
        writer().deleteRecord(rec);
        System.out.print('x');
        return 1;
    }

    public void copyFrom(DcmDir other) throws IOException {
	DicomDirReader r = other.dicomdir;
	for (DicomObject src = r.findFirstRootRecord(); src != null;
		src = r.findNextSiblingRecord(src)) {
	    BasicDicomObject dst = new BasicDicomObject();
	    src.copyTo(dst);
	    writer().addRootRecord(dst);
	    copyChildRecords(r, src, dst);
	}
    }
    
    private void copyChildRecords(DicomDirReader r, DicomObject srcParent,
	    DicomObject dstParent) throws IOException {
	for (DicomObject src = r.findFirstChildRecord(srcParent); src != null;
		src = r.findNextSiblingRecord(src)) {
	    BasicDicomObject dst = new BasicDicomObject();
	    src.copyTo(dst);
	    writer().addChildRecord(dstParent, dst);
	    copyChildRecords(r, src, dst);
	}
    }

    public void close() throws IOException {
        dicomdir.close();
    }

    private static CommandLine parse(String[] args) {
        Options opts = new Options();
        OptionGroup cmdOpt = new OptionGroup();
        OptionBuilder.withArgName("dicomdir");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
        	"create new directory file <dicomdir> for DICOM file-set " + 
        	"specified by file.. or directory.. arguments");
        cmdOpt.addOption(OptionBuilder.create("c"));
        OptionBuilder.withArgName("dicomdir");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
        	"read directory file <dicomdir> and dump content to stdout");
        cmdOpt.addOption(OptionBuilder.create("t"));
        OptionBuilder.withArgName("dicomdir");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
        	"add references to specified files to existing directory file " +
        	"<dicomdir>");
        cmdOpt.addOption(OptionBuilder.create("a"));
        OptionBuilder.withArgName("dicomdir");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
        	"delete references to specified files from existing directory " +
        	"file <dicomdir>");
        cmdOpt.addOption(OptionBuilder.create("d"));
        OptionBuilder.withArgName("dicomdir");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
        	"purge records without file references from directory file " +
        	"<dicomdir>.");
        cmdOpt.addOption(OptionBuilder.create("p"));
        OptionBuilder.withArgName("dicomdir");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
        	"compact existing directory file <dicomdir> by removing unused entries");
        cmdOpt.addOption(OptionBuilder.create("z"));
        opts.addOptionGroup(cmdOpt);
        OptionBuilder.withArgName("txtfile");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("specify File-set Descriptor File");
        opts.addOption(OptionBuilder.create("desc"));
        OptionBuilder.withArgName("code");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
        	"Character Set used in File-set Descriptor File" +
        	"(\"ISO_IR 100\" = ISO Latin 1).");
        opts.addOption(OptionBuilder.create("desccs"));
        OptionBuilder.withArgName("id");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("specify File-set ID");
        opts.addOption(OptionBuilder.create("id"));
        OptionBuilder.withArgName("uid");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("specify File-set UID");
        opts.addOption(OptionBuilder.create("uid"));
        OptionBuilder.withArgName("max");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
        	"maximal number of characters per line, by default: 80");
        opts.addOption(OptionBuilder.create("w"));
        OptionBuilder.withArgName("max");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
        	"limit value prompt to <maxlen> characters, by default: 64");
        opts.addOption(OptionBuilder.create("W"));
        opts.addOption("inactive", false, 
                "dump also inactive records.");
        opts.addOption("S", false, 
                "encode Sequences with undefined length," +
                "encode with explicit length by default.");
        opts.addOption("I", false, 
                "encode Sequence Items with undefined length," +
                "encode with explicit length by default.");
        opts.addOption("h", "help", false, "print this message");
        opts.addOption("V", "version", false,
                "print the version information and exit");
        CommandLine cl = null;
        try {
            cl = new GnuParser().parse(opts, args);
        } catch (ParseException e) {
            exit("dcmdir: " + e.getMessage());
            throw new RuntimeException("unreachable");
        }
        if (cl.hasOption('V')) {
            Package p = DcmDir.class.getPackage();
            System.out.println("dcmdir v" + p.getImplementationVersion());
            System.exit(0);
        }
        if (cl.hasOption('h')
                || !(cl.hasOption("a")
                        || cl.hasOption("c")
                        || cl.hasOption("d")
                        || cl.hasOption("p")
                        || cl.hasOption("t")
                        || cl.hasOption("z"))) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(USAGE, DESCRIPTION, opts, EXAMPLE);
            System.exit(0);
        }
        return cl;
    }

    private static void exit(String msg) {
        System.err.println(msg);
        System.err.println("Try 'dcmdir -h' for more information.");
        System.exit(1);
    }

    private static int parseInt(String s, String opt, int min, int max) {
        try {
            int i = Integer.parseInt(s);
            if (i >= min && i <= max)
                return i;
        } catch (NumberFormatException e) {
            // parameter is not a valid integer; fall through to exit
        }
        exit("illegal argument for option -" + opt);
        throw new RuntimeException();
    }
    
    public static void main(String[] args) throws IOException {
	CommandLine cl = parse(args);
	List argList = cl.getArgList();
	long start = System.currentTimeMillis();
	if (cl.hasOption("t")) {
	    DcmDir dcmdir = new DcmDir(new File(cl.getOptionValue("t")));
	    if (cl.hasOption("w"))
		dcmdir.setMaxWidth(parseInt(cl.getOptionValue("w"), "w",
			MIN_MAX_WIDTH, MAX_MAX_WIDTH));
	    if (cl.hasOption("W"))
		dcmdir.setMaxValLen(parseInt(cl.getOptionValue("W"), "W",
			MIN_MAX_VAL_LEN, MAX_MAX_VAL_LEN));
	    dcmdir.openRO();
	    dcmdir.setShowInactiveRecords(cl.hasOption("inactive"));
	    dcmdir.dump();
	    dcmdir.close();
	} else if (cl.hasOption("p")) {
	    String fpath = cl.getOptionValue("p");
	    File f = new File(fpath);
	    DcmDir dcmdir = new DcmDir(f);
	    dcmdir.open();
	    int count = dcmdir.purge();
	    dcmdir.close();
	    long end = System.currentTimeMillis();
	    System.out.println("Purge " + count + " directory records from "
		    + f + " in " + (end - start) + "ms.");
	} else if (cl.hasOption("z")) {
	    String fpath = cl.getOptionValue("z");
	    File f = new File(fpath);
	    File out = new File(fpath + ".NEW");
	    File bak = new File(fpath + "~");
	    DcmDir dcmdir1 = new DcmDir(f);
	    dcmdir1.openRO();
	    DcmDir dcmdir2 = new DcmDir(out);
	    dcmdir2.fsinfo(dcmdir1.fsinfo());
	    createDicomDir(dcmdir2, cl);
	    dcmdir2.copyFrom(dcmdir1);
	    dcmdir2.close();
	    dcmdir1.close();
	    if (!f.renameTo(bak)) {
		throw new IOException("Failed to rename " + f + " to " + bak);
	    }
	    if (!out.renameTo(f)) {
		throw new IOException("Failed to rename " + out + " to " + f);
	    }
	    long end = System.currentTimeMillis();
	    System.out.println("Compact " + f + " from " + bak.length() 
		    + " to " + f.length() + " bytes in " + (end - start) 
		    + "ms.");
	} else if (cl.hasOption("d")) {
	    File f = new File(cl.getOptionValue("d"));
	    DcmDir dcmdir = new DcmDir(f);
	    dcmdir.open();
	    int num = 0;
	    for (int i = 0, n = argList.size(); i < n; ++i) {
		num += dcmdir.delFile(new File((String) argList.get(i)));
	    }
	    dcmdir.close();
	    long end = System.currentTimeMillis();
	    System.out.println();
	    System.out.println("Remove " + num + " references from " + f
		    + " in " + (end - start) + "ms.");
	} else {
	    DcmDir dcmdir;
	    File f;
	    if (cl.hasOption("c")) {
		f = new File(cl.getOptionValue("c"));
		dcmdir = new DcmDir(f);
		createDicomDir(dcmdir, cl);
		dcmdir.setCheckDuplicate(false);
	    } else { // cl.hasOption("a") 
		f = new File(cl.getOptionValue("a"));
		dcmdir = new DcmDir(f);
		dcmdir.open();
		dcmdir.setCheckDuplicate(true);
	    }
	    int num = 0;
	    for (int i = 0, n = argList.size(); i < n; ++i) {
		num += dcmdir.addFile(new File((String) argList.get(i)));
	    }
	    dcmdir.close();
	    long end = System.currentTimeMillis();
	    System.out.println();
	    System.out.println("Add " + num
		    + (cl.hasOption("c") 
			    ? " directory records to new directory file "
			    : " directory records to existing directory file ")
			    + f + " in " + (end - start) + "ms.");
	}
    }

    private static void createDicomDir(DcmDir dcmdir, CommandLine cl) throws IOException {
	if (cl.hasOption("id")) {
	    dcmdir.setFileSetID(cl.getOptionValue("id"));
	}
	if (cl.hasOption("uid")) {
	    dcmdir.setMediaStorageSOPInstanceUID(
		    cl.getOptionValue("uid"));
	}
	if (cl.hasOption("desc")) {
	    dcmdir.setFileSetDescriptorFileID(
		    cl.getOptionValue("desc"));
	    if (cl.hasOption("desccs")) {
		dcmdir.setSpecificCharacterSetofFileSetDescriptorFile(
			cl.getOptionValue("desccs"));
	    }
	}
	dcmdir.create();
	dcmdir.setExplicitSequenceLength(!cl.hasOption("S"));
	dcmdir.setExplicitItemLength(!cl.hasOption("I"));
    }
}
