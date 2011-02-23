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

package org.dcm4che2.tool.chess3d;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.ContentHandlerAdapter;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.util.UIDUtils;
import org.xml.sax.SAXException;
/**
 * @author franz.willer@agfa.com
 * @version $Revision: $ $Date:  $
 * @since Okt 15, 2007
 *
 */
public class Chess3D {

    private static final String USAGE = 
        "chess3d [Options] [<xmlfile>] <number of slices>";
    private static final String DESCRIPTION = 
        "Create a 3D chess object by creating multiple DICOM images with approbiate slice position/slice thickness.\n"+
        "Use optional <xmlfile> (DICOM XML presentation) as template for DICOM attributes. Options:";
    private static final String EXAMPLE = 
        "chess3d -d /tmp/dcmfiles -l 10.0/5.0/-12.0 -t 2 10\n" +
        "=> create a 3D chess object with 10x10x5 squares, each square with 100 dots (x,y) and 10 slices (z):"+
        "=> 50 DICOM Objects with slice thickness of 2 "+ 
        "and a start image location of x=10.0, y=5.0 and z=-12. " +
        "The files are stored in directory /tmp/dcmfiles as test_0_0.dcm, test_0_1.dcm,... test_0_9.dcm, test_1_0, test_1_1,... test_4_9.dcm";
    
    private File destDir;
    private float thickness = 1;
    private float[] currentSlicePos = {0.0f,0.0f,0.0f};
    private int rectWidth = 100;
    private int rectHeight = 100;
    private int xRect=10;
    private int yRect=10;
    private int zRect=5;
    private byte white=(byte)225;
    private byte black=(byte)0;
	private int windowCenter = 128;
	private int windowWidth = 256;
    
    private byte[] lineBuffer;
	private int lineLength;
    private int[] offsets = new int[2];
    
    private String studyUID;
    private String seriesUID;

    public final void setDest(String dest) {
        this.destDir = new File(dest);
        if ( ! this.destDir.isDirectory() ) {
        	throw new IllegalArgumentException("Destination is not a directory! dest: " + dest);
        }
    }

    public void setRectWidth(int rectWidth) {
		this.rectWidth = rectWidth;
	}

	public void setRectHeight(int rectHeight) {
		this.rectHeight = rectHeight;
	}

	public void setXRect(int rect) {
		xRect = rect;
	}

	public void setYRect(int rect) {
		yRect = rect;
	}

	public void setZRect(int rect) {
		zRect = rect;
	}

	public final void setThickness(float f) {
        this.thickness = f;
    }

	public void setBlack(byte b) {
		black = b;
		
	}

	public void setWhite(byte w) {
		white = w;
	}
	
	public final void setLocation(String s) {
    	try {
    		int pos = s.indexOf('\\');
    		this.currentSlicePos[0] = Float.parseFloat(s.substring(0,pos));
    		int pos1 = s.indexOf('\\',++pos);
    		this.currentSlicePos[1] = Float.parseFloat(s.substring(pos,pos1++));
    		this.currentSlicePos[2] = Float.parseFloat(s.substring(pos1));
    	} catch ( Exception x ) {
    		x.printStackTrace();
    		throw new IllegalArgumentException("Format of Slice Location wrong:("+s+") Format:<x>\\<y>\\<z>; e.g.:'10.0\\12.23\\100'");
    	}
    }
    
	public void setStudyUID(String studyUID) {
		this.studyUID = studyUID;
	}

	public void setSeriesUID(String seriesUID) {
		this.seriesUID = seriesUID;
	}

	public void extrude(File xmlFile, int slices) throws IOException, ParserConfigurationException, SAXException {
		DicomObject obj = new BasicDicomObject();
		String fn;
		File parent;
		if ( xmlFile != null ) {
			SAXParserFactory f = SAXParserFactory.newInstance();
			SAXParser p = f.newSAXParser();
			ContentHandlerAdapter ch = new ContentHandlerAdapter(obj);
			p.parse(xmlFile, ch);
			fn = obj.getString(Tag.PatientID);
			if ( fn == null ) fn = xmlFile.getName();
			parent = destDir == null ? xmlFile.getParentFile() : destDir;
		} else {
			fn = "test";
			parent = destDir == null ? null : destDir;
		}
        File oFile;
        String ext;
        int pos = fn.lastIndexOf('.');
        if ( pos != -1 ) {
        	ext = fn.substring(pos);//we want '.' too
        	fn = fn.substring(0,pos);
        } else {
        	ext="";
        }
        fn += "_";
        obj = prepare(obj);
        prepareLineBuffer();
        int imageSize = lineLength*yRect*rectHeight;
        for ( int z = 0 ; z < zRect ; z++) {
	        for ( int i = 0 ; i < slices ; i++ ) {
	        	System.out.print("*");
	        	chgAttributes(obj,i);
	        	oFile = new File(parent, fn+z+"_"+(i)+ext);
	        	DicomOutputStream dos = new DicomOutputStream(oFile);
	        	dos.writeDicomFile(obj);
	        	dos.writeHeader(Tag.PixelData, VR.OB, (imageSize+1) & ~1);
	        	writePixelData(dos, z);
	            if ((imageSize & 1) != 0)
	                dos.write(0);
	        	dos.close();
	        }
        }
    }

	private void prepareLineBuffer() {
		lineLength = xRect*rectWidth;
		offsets = new int[] {0,rectWidth};
		int colorFields = xRect+1;
		int bufferLen = lineLength+rectWidth;
		lineBuffer=new byte[bufferLen];
		Arrays.fill(lineBuffer, 0, rectWidth, white);
		int destPos = rectWidth<<1;
		Arrays.fill(lineBuffer, rectWidth, --destPos, black);
		if ( xRect == 1 ) return;
		destPos++;//again 2*rectWidth
		int copyLen = destPos;
		int len = 0;
		for( int x = colorFields >> 1 ; x > 0 ; x = x >> 1) len++; 
		for ( int i = 1 ; i < len ; i++ ) {
			System.arraycopy(lineBuffer, 0, lineBuffer, destPos, copyLen);
			copyLen = copyLen << 1;
			destPos = destPos << 1;
		}	
		int remain = colorFields - (1 << len);
		if ( remain > 0 ) {
			copyLen = rectWidth * remain;
			System.arraycopy(lineBuffer, 0, lineBuffer, destPos, copyLen);
		}
	}

	private void writePixelData(DicomOutputStream dos, int select) throws IOException {
		for ( int i = 0; i < yRect ; i ++) {
			for ( int iy = 0 ; iy < rectHeight ; iy++) {
				dos.write(lineBuffer, offsets[select & 0x01], lineLength);
			}
		    select++;
		}
		
	}

	private DicomObject prepare(DicomObject obj) {
		if ( !obj.contains(Tag.PatientID)) obj.putString(Tag.PatientID, VR.LO, "test_chess"); 
		if ( !obj.contains(Tag.PatientName)) obj.putString(Tag.PatientName, VR.PN, "test^chess"); 
        if (!obj.containsValue(Tag.StudyInstanceUID)) obj.putString(Tag.StudyInstanceUID, VR.UI, UIDUtils.createUID());
        if (!obj.containsValue(Tag.SeriesInstanceUID)) obj.putString(Tag.SeriesInstanceUID, VR.UI, UIDUtils.createUID());
        
        if (!obj.containsValue(Tag.Modality)) obj.putString(Tag.Modality, VR.CS, "CT");
        
        if (!obj.containsValue(Tag.SOPClassUID)) obj.putString(Tag.SOPClassUID, VR.UI, UID.SecondaryCaptureImageStorage);

		obj.putInt(Tag.Rows, VR.US, yRect*rectHeight);
		obj.putInt(Tag.Columns, VR.US, xRect*rectWidth);
		obj.putFloat(Tag.WindowCenter, VR.DS, windowCenter);
		obj.putFloat(Tag.WindowWidth, VR.DS, windowWidth);
		obj.putInt(Tag.SamplesPerPixel, VR.US, 1);
		obj.putInt(Tag.NumberOfFrames, VR.IS, 1);
		obj.putInt(Tag.BitsAllocated, VR.US, 8);
		obj.putInt(Tag.BitsStored, VR.US, 8);
		obj.putInt(Tag.HighBit, VR.US, 7);
		obj.putInt(Tag.PlanarConfiguration, VR.US, 0);
		obj.putInt(Tag.PixelRepresentation, VR.US, 0);
		obj.putString(Tag.PhotometricInterpretation, VR.CS, "MONOCHROME2");
		obj.putString(Tag.TransferSyntaxUID, VR.UI, UID.ExplicitVRLittleEndian);
		obj.initFileMetaInformation(UID.ExplicitVRLittleEndian);
		return obj;
	}   
    
    private void chgAttributes(DicomObject obj, int i) {
		if ( studyUID != null ) 
			obj.putString(Tag.StudyInstanceUID, VR.UI, studyUID);
		if ( seriesUID != null )
			obj.putString(Tag.SeriesInstanceUID, VR.UI, seriesUID);
		obj.putString(Tag.SOPInstanceUID, VR.UI, UIDUtils.createUID());
		
		obj.putString(Tag.InstanceNumber, VR.IS, String.valueOf((int)currentSlicePos[2]));
		
		obj.putFloat(Tag.SliceThickness, VR.DS, this.thickness);
		obj.putFloats(Tag.SliceLocation, VR.DS, this.currentSlicePos);
		currentSlicePos[2] += thickness;
	}

    public static void main(String[] args) throws ParserConfigurationException, SAXException {
        try {
            CommandLine cl = parse(args);
            Chess3D extruder = new Chess3D();
            if (cl.hasOption("d")) {
                extruder.setDest(cl.getOptionValue("d"));
            }
            if (cl.hasOption("t")) {
                extruder.setThickness(Float.parseFloat(cl.getOptionValue("thickness")));
            }
            if (cl.hasOption("l")) {
                extruder.setLocation(cl.getOptionValue("l"));
            }
            if (cl.hasOption("x")) {
                extruder.setRectWidth(Integer.parseInt(cl.getOptionValue("x")));
            }
            if (cl.hasOption("y")) {
                extruder.setRectHeight(Integer.parseInt(cl.getOptionValue("y")));
            }
            if (cl.hasOption("X")) {
                extruder.setXRect(Integer.parseInt(cl.getOptionValue("X")));
            }
            if (cl.hasOption("Y")) {
                extruder.setYRect(Integer.parseInt(cl.getOptionValue("Y")));
            }
            if (cl.hasOption("Z")) {
                extruder.setZRect(Integer.parseInt(cl.getOptionValue("Z")));
            }
            if (cl.hasOption("w")) {
                extruder.setWhite( (byte)Integer.decode(cl.getOptionValue("w")).intValue());
            }
            if (cl.hasOption("b")) {
                extruder.setBlack( (byte)Integer.decode(cl.getOptionValue("b")).intValue());
            }
            if (cl.hasOption("win")) {
                extruder.setWindow( cl.getOptionValue("win"));
            }
            if (cl.hasOption("uid")) {
                UIDUtils.setRoot(cl.getOptionValue("uid"));
            }
            if (cl.hasOption("S")) {
                extruder.setStudyUID(UIDUtils.createUID());
            }
            if (cl.hasOption("s")) {
            	extruder.setSeriesUID(UIDUtils.createUID());
            }
            List argList = cl.getArgList();
            int idx = 0;
            File dcmFile = argList.size() > 1 ? new File((String) argList.get(idx++)) : null;
            int slices = Integer.parseInt((String) argList.get(idx));
            long start = System.currentTimeMillis();
            extruder.extrude(dcmFile, slices);
            long fin = System.currentTimeMillis();
            System.out.println("\n3D chess object created in "+(fin - start) + "ms. Chess cuboid: ("+extruder.xRect+","+extruder.yRect+","+extruder.zRect+"):\n"+ 
                    "width:"+extruder.rectWidth +" height:"+extruder.rectHeight + " slices/field:"+slices+"\n" +
            		"Files (all slices):"+(slices*extruder.zRect) +" slices ("+extruder.thickness +
            		" mm).\nBlack:"+(extruder.black & 0x0ff)+" White:"+(extruder.white&0x0ff) );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	private void setWindow(String s) {
		int pos = s.indexOf('\\');
		if ( pos == -1)
			throw new IllegalArgumentException("Wrong windows option value! Format:<window center>\\<window width>");
		this.windowCenter = Integer.parseInt(s.substring(0,pos));
		this.windowWidth = Integer.parseInt(s.substring(++pos));
		
	}

	private static CommandLine parse(String[] args) {
        Options opts = new Options();
 
        Option oThickness = new Option("t","thickness", true, "Slice Thickness, 1 by default");
        oThickness.setArgName("thickness");
        opts.addOption(oThickness);
        
        Option oLocation = new Option("l","location", true, "Slice Location of first image, 0.0\\0.0\\0.0 by default");
        oLocation.setArgName("location");
        opts.addOption(oLocation);
        
        Option ox = new Option("x","rectWidth", true, "Width of one chess rectangle (x-coord). 100 by default");
        ox.setArgName("x");
        opts.addOption(ox);
        Option oy = new Option("y","rectDepth", true, "Heigth of one chess rectangle (y-coord). 100 by default");
        oy.setArgName("y");
        opts.addOption(oy);

        Option oX = new Option("X","xRect", true, "Number of chess fields in x-coord, 10 by default");
        oX.setArgName("X");
        opts.addOption(oX);
        Option oY = new Option("Y","yRect", true, "Number of chess fields in y-coord, 10 by default");
        oY.setArgName("Y");
        opts.addOption(oY);
        Option oZ = new Option("Z","zRect", true, "Number of chess fields in z-coord, 5 by default");
        oZ.setArgName("Z");
        opts.addOption(oZ);

        Option oW = new Option("w","white", true, "White field value (0-255), 225 by default");
        oW.setArgName("w");
        opts.addOption(oW);
        Option oB = new Option("b","black", true, "Black field value (0-255), 0 by default");
        oB.setArgName("b");
        opts.addOption(oB);

        Option oWin = new Option("win","window", true, "Window level. Format: <center>\\<width>. 128\\256 by default");
        oWin.setArgName("win");
        opts.addOption(oWin);
        
        OptionBuilder.withArgName("dir");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "Destination directory, parent of xml file or current working directory by default");
        opts.addOption(OptionBuilder.create("d"));
        
        opts.addOption("S","studyUID",false,"Create new Study Instance UID. Only effective if xmlFile is specified and studyIUID is set");

        opts.addOption("s","seriesUID",false,"create new Series Instance UID. Only effective if xmlFile is specified and seriesIUID is set");

        OptionBuilder.withArgName("prefix");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("Generate UIDs with given prefix, 1.2.40.0.13.1.<host-ip> by default.");
        opts.addOption(OptionBuilder.create("uid"));
        
        opts.addOption("h", "help", false, "print this message");
        opts.addOption("V", "version", false,
                "print the version information and exit");
        CommandLine cl = null;
        try {
            cl = new GnuParser().parse(opts, args);
        } catch (ParseException e) {
            exit("chess3d: " + e.getMessage());
            throw new RuntimeException("unreachable");
        }
        if (cl.hasOption('V')) {
            Package p = Chess3D.class.getPackage();
            System.out.println("chess3d v" + p.getImplementationVersion());
            System.exit(0);
        }
        if (cl.hasOption('h') || cl.getArgList().size() < 1) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(USAGE, DESCRIPTION, opts, EXAMPLE);
            System.exit(0);
        }

        return cl;
    }

    private static void exit(String msg) {
        System.err.println(msg);
        System.err.println("Try 'chess3d -h' for more information.");
        System.exit(1);
    }
    
}
