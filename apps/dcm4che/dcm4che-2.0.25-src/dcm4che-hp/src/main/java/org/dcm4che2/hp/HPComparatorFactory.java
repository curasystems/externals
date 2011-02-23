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

package org.dcm4che2.hp;

import java.util.Date;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.hp.plugins.AlongAxisComparator;
import org.dcm4che2.hp.plugins.ByAcqTimeComparator;
import org.dcm4che2.hp.spi.HPComparatorSpi;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 12635 $ $Date: 2010-01-18 14:15:50 +0100 (Mon, 18 Jan 2010) $
 * @since Aug 1, 2005
 * 
 */
public class HPComparatorFactory {

    /**
     * Selector Value Number constant for indicating that the frame number
     * shall be used for indexing the value of the Selector Attribute for
     * sorting.
     */
    public static final int FRAME_INDEX = 0xffff;
    
    /**
     * Create HPComparator from Sorting Operations Sequence (0072,0600) item.
     * The created HPComparator is backed by the given item
     * {@link #getDicomObject DicomObject}.
     * 
     * @param item
     *            DicomObject of Sorting Operations Sequence (0072,0600)
     * @return the new HPComparator
     */
    public static HPComparator createHPComparator(DicomObject sortingOp) {
        if (sortingOp.containsValue(Tag.SortByCategory))
            return HPComparatorFactory.createSortByCategory(sortingOp);
        HPComparator cmp = new SortByAttribute(sortingOp);
        cmp = addSequencePointer(cmp);
        cmp = addFunctionalGroupPointer(cmp);
        return cmp;
    }

    private static HPComparator createSortByCategory(DicomObject sortingOp) {
        HPComparatorSpi spi = HangingProtocol.getHPComparatorSpi(sortingOp
                .getString(Tag.SortByCategory));
        if (spi == null)
            throw new IllegalArgumentException("Unsupported Sort-by Category: "
                    + sortingOp.get(Tag.SortByCategory));
        return spi.createHPComparator(sortingOp);
    }

    /**
     * Create Sort By Attribute Comparator. A new
     * {@link #getDicomObject DicomObject}, representing the according Sorting
     * Operations Sequence (0072,0600) item is allocated and initialized.
     * 
     * @param privateCreator
     *            Selector Attribute Private Creator, if Selector Attribute is
     *            contained by a Private Group, otherwise <code>null</code>.
     * @param tag
     *            Selector Attribute
     * @param sortingDirection
     *            {@link CodeString#INCREASING} or {@link CodeString#DECREASING}
     * @return the new Comparator
     */
    public static HPComparator createSortByAttribute(String privateCreator,
            int tag, int valueNumber, String sortingDirection) {
        return new SortByAttribute(privateCreator, tag, valueNumber,
                sortingDirection);
    }

    /**
     * Create Sort Category Comparator with Sort-by Category ALONG_AXIS. A new
     * {@link #getDicomObject DicomObject}, representing the according Sorting
     * Operations Sequence (0072,0600) item is allocated and initialized.
     * 
     * @param sortingDirection
     *            {@link CodeString#INCREASING} or {@link CodeString#DECREASING}
     * @return the new Comparator
     */
    public static HPComparator createSortAlongAxis(String sortingDirection) {
        return new AlongAxisComparator(sortingDirection);
    }

    /**
     * Create Sort Category Comparator with Sort-by Category BY_ACQ_TIME. A new
     * {@link #getDicomObject DicomObject}, representing the according Sorting
     * Operations Sequence (0072,0600) item is allocated and initialized.
     * 
     * @param sortingDirection
     *            {@link CodeString#INCREASING} or {@link CodeString#DECREASING}
     * @return the new Comparator
     */
    public static HPComparator createSortByAcqTime(String sortingDirection) {
        return new ByAcqTimeComparator(sortingDirection);
    }

    /**
     * Decorate Sort By Attribute Comparator with Selector Sequence Pointer,
     * defining the Selector Attribute as nested in a Sequence. If the Sequence
     * is itself nested in a Frunctional Group Sequence, the returned Comparator
     * has to be additional decorated by {@link #addFunctionalGroupPointer}.
     * The associated {@link #getDicomObject DicomObject} is updated
     * correspondingly. If
     * <code>tag = 0</tag>, the given comparator is returned unmodified.
     * 
     * @param privateCreator Selector Sequence Pointer Private Creator, if 
     *        Selector Sequence Pointer is contained by a Private Group,
     *        otherwise <code>null</code>.
     * @param tag Functional Group Pointer
     * @param comparator to decorate
     * @return the decorated Comparator
     */
    public static HPComparator addSequencePointer(String privCreator, int tag,
            HPComparator comparator) {
        if (tag == 0)
            return comparator;

        if (comparator.getSelectorSequencePointer() != 0)
            throw new IllegalArgumentException("Sequence Pointer already added");

        if (comparator.getFunctionalGroupPointer() != 0)
            throw new IllegalArgumentException(
                    "Functional Group Pointer already added");

        comparator.getDicomObject().putInt(Tag.SelectorSequencePointer, VR.AT,
                tag);
        if (privCreator != null) {
            comparator.getDicomObject().putString(
                    Tag.SelectorSequencePointerPrivateCreator, VR.LO,
                    privCreator);
        }
        return new Seq(privCreator, tag, comparator);
    }

    /**
     * Decorate Sort By Attribute Comparator with Functional Group Pointer,
     * defining the Selector Attribute as nested in a Functional Group Sequence.
     * The associated {@link #getDicomObject DicomObject} is updated
     * correspondingly. If
     * <code>tag = 0</tag>, the given comparator is returned unmodified.
     * 
     * @param privateCreator Functional Group Private Creator, if Functional
     *        Group Pointer is contained by a Private Group,
     *        otherwise <code>null</code>.
     * @param tag Functional Group Pointer
     * @param comparator to decorate
     * @return decorated Comparator
     */
    public static HPComparator addFunctionalGroupPointer(String privCreator,
            int tag, HPComparator comparator) {
        if (tag == 0)
            return comparator;

        if (comparator.getFunctionalGroupPointer() != 0)
            throw new IllegalArgumentException(
                    "Functional Group Pointer already added");

        comparator.getDicomObject().putInt(Tag.FunctionalGroupPointer, VR.AT,
                tag);
        if (privCreator != null) {
            comparator.getDicomObject().putString(
                    Tag.FunctionalGroupPrivateCreator, VR.LO, privCreator);
        }
        return new FctGrp(tag, privCreator, comparator);
    }

    private static HPComparator addSequencePointer(HPComparator cmp) {
        int tag = cmp.getSelectorSequencePointer();
        if (tag != 0) {
            String privCreator = cmp.getSelectorSequencePointerPrivateCreator();
            cmp = new Seq(privCreator, tag, cmp);
        }
        return cmp;
    }

    private static HPComparator addFunctionalGroupPointer(HPComparator cmp) {
        int tag = cmp.getFunctionalGroupPointer();
        if (tag != 0) {
            String privCreator = cmp.getFunctionalGroupPrivateCreator();
            cmp = new FctGrp(tag, privCreator, cmp);
        }
        return cmp;
    }

    private static abstract class AttributeComparator extends
            AbstractHPComparator {
        protected final int tag;

        protected final String privateCreator;

        AttributeComparator(int tag, String privateCreator) {
            if (tag == 0) {
                throw new IllegalArgumentException("tag: 0");
            }
            this.tag = tag;
            this.privateCreator = privateCreator;
        }

        protected int resolveTag(DicomObject dcmobj) {
            return privateCreator == null ? tag : dcmobj.resolveTag(tag,
                    privateCreator);
        }
    }

    private static class SortByAttribute extends AttributeComparator {
        private final DicomObject sortingOp;

        private final int valueNumber;

        private final int sign;

        SortByAttribute(String privateCreator, int tag, int valueNumber,
                String sortingDirection) {
            super(tag, privateCreator);
            if (valueNumber == 0) {
                throw new IllegalArgumentException("valueNumber = 0");
            }
            this.valueNumber = valueNumber;
            this.sign = CodeString.sortingDirectionToSign(sortingDirection);
            sortingOp = new BasicDicomObject();
            sortingOp.putInt(Tag.SelectorAttribute, VR.AT, tag);
            if (privateCreator != null)
                sortingOp.putString(Tag.SelectorAttributePrivateCreator, VR.LO,
                        privateCreator);
            sortingOp.putInt(Tag.SelectorValueNumber, VR.US, valueNumber);
            sortingOp.putString(Tag.SortingDirection, VR.CS, sortingDirection);
        }

        SortByAttribute(DicomObject sortingOp) {
            super(getSelectorAttribute(sortingOp), sortingOp
                    .getString(Tag.SelectorAttributePrivateCreator));
            this.valueNumber = sortingOp.getInt(Tag.SelectorValueNumber);
            if (valueNumber == 0) {
                throw new IllegalArgumentException(
                        "Missing or invalid (0072,0028) Selector Value Number: "
                                + sortingOp.get(Tag.SelectorValueNumber));
            }
            String cs = sortingOp.getString(Tag.SortingDirection);
            if (cs == null) {
                throw new IllegalArgumentException(
                        "Missing (0072,0604) Sorting Direction");
            }
            this.sign = CodeString.sortingDirectionToSign(cs);
            this.sortingOp = sortingOp;
        }

        private static int getSelectorAttribute(DicomObject sortingOp) {
            int tag = sortingOp.getInt(Tag.SelectorAttribute);
            if (tag == 0) {
                throw new IllegalArgumentException(
                        "Missing (0072,0026) Selector Attribute");
            }
            return tag;
        }

        public DicomObject getDicomObject() {
            return sortingOp;
        }

        public int compare(DicomObject o1, int frame1, DicomObject o2,
                int frame2) {
            DicomElement e1 = o1.get(resolveTag(o1));
            if (e1 == null)
                return 0;
            DicomElement e2 = o2.get(resolveTag(o2));
            if (e2 == null)
                return 0;
            if (e1.vr() != e2.vr())
                return 0;
            int i1 = frame1;
            int i2 = frame2;
            if (valueNumber != FRAME_INDEX) {
                i1 = i2 = valueNumber;
            }
            switch (e1.vr().code()) {
            case 0x4145: // AE
            case 0x4153: // AS
            case 0x4353: // CS
            case 0x4c4f: // LO
            case 0x4c54: // LT;
            case 0x504e: // PN;
            case 0x5348: // SH;
            case 0x5354: // ST;
            case 0x5549: // UI;
            case 0x5554: // UT;
                return sign * strcmp(
                        e1.getStrings(o1.getSpecificCharacterSet(), true), i1,
                        e2.getStrings(o2.getSpecificCharacterSet(), true), i2);
            case 0x4154: // AT
            case 0x554c: // UL;
            case 0x5553: // US;
                return sign * uintcmp(e1.getInts(true), i1, e2.getInts(true), i2);
            case 0x4441: // DA
            case 0x4454: // DT
            case 0x544d: // TM;
                return sign * datecmp(e1.getDates(true), i1, e2.getDates(true), i2);
            case 0x4453: // DS
            case 0x464c: // FL
                return sign * fltcmp(e1.getFloats(true), i1, e2.getFloats(true), i2);
            case 0x4644: // FD
                return sign * dblcmp(e1.getDoubles(true), i1, e2.getDoubles(true), i2);
            case 0x4953: // IS
            case 0x534c: // SL;
            case 0x5353: // SS;
                return sign * intcmp(e1.getInts(true), i1, e2.getInts(true), i2);
            case 0x5351: // SQ;
                return sign * codecmp(e1.getDicomObject(), e2.getDicomObject());
            }
            // no sort if VR = OB, OF, OW or UN
            return 0;
        }

    }

    private static int codecmp(DicomObject c1, DicomObject c2) {
        if (c1 == null || c2 == null)
            return 0;
        String v1 = c1.getString(Tag.CodeValue);
        String v2 = c2.getString(Tag.CodeValue);
        if (v1 == null || v2 == null)
            return 0;
        return v1.compareTo(v2);
    }

    private static int intcmp(int[] v1, int i1, int[] v2, int i2) {
        if (v1 == null || v2 == null || v1.length < i1 || v2.length < i2)
            return 0;
        return v1[i1 - 1] - v2[i2 - 1];
    }

    private static int dblcmp(double[] v1, int i1, double[] v2, int i2) {
        if (v1 == null || v2 == null || v1.length < i1 || v2.length < i2)
            return 0;
        double d = v1[i1 - 1] - v2[i2 - 1];
        return (d < 0) ? -1 : (d > 0) ? 1 : 0;
    }

    private static int fltcmp(float[] v1, int i1, float[] v2, int i2) {
        if (v1 == null || v2 == null || v1.length < i1 || v2.length < i2)
            return 0;
        float d = v1[i1 - 1] - v2[i2 - 1];
        return (d < 0) ? -1 : (d > 0) ? 1 : 0;
    }

    private static int datecmp(Date[] v1, int i1, Date[] v2, int i2) {
        if (v1 == null || v2 == null || v1.length < i1 || v2.length < i2)
            return 0;
        return v1[i1 - 1].compareTo(v2[i2 - 1]);
    }

    private static int uintcmp(int[] v1, int i1, int[] v2, int i2) {
        if (v1 == null || v2 == null || v1.length < i1 || v2.length < i2)
            return 0;
        long d = (v1[i1 - 1] & 0xffffffffL) - (v2[i2 - 1] & 0xffffffffL);
        return (d < 0) ? -1 : (d > 0) ? 1 : 0;
    }

    private static int strcmp(String[] v1, int i1, String[] v2, int i2) {
        if (v1 == null || v2 == null || v1.length < i1
                || v2.length < i2)
            return 0;
        return v1[i1 - 1].compareTo(v2[i2 - 1]);
    }
    
    private static abstract class AttributeComparatorDecorator extends
            AttributeComparator {
        protected final HPComparator cmp;

        AttributeComparatorDecorator(int tag, String privateCreator,
                HPComparator cmp) {
            super(tag, privateCreator);
            this.cmp = cmp;
        }

        public DicomObject getDicomObject() {
            return cmp.getDicomObject();
        }
    }

    private static class Seq extends AttributeComparatorDecorator {
        Seq(String privateCreator, int tag, HPComparator cmp) {
            super(tag, privateCreator, cmp);
        }

        public int compare(DicomObject o1, int frame1, DicomObject o2,
                int frame2) {
            DicomObject v1 = o1.getNestedDicomObject(resolveTag(o1));
            if (v1 == null)
                return 0;
            DicomObject v2 = o2.getNestedDicomObject(resolveTag(o2));
            if (v2 == null)
                return 0;
            return cmp.compare(v1, frame1, v2, frame2);
        }
    }

    private static class FctGrp extends AttributeComparatorDecorator {
        FctGrp(int tag, String privateCreator, HPComparator cmp) {
            super(tag, privateCreator, cmp);
        }

        public int compare(DicomObject o1, int frame1, DicomObject o2,
                int frame2) {
            DicomObject fg1 = fctGrp(o1, frame1);
            if (fg1 == null)
                return 0;
            DicomObject fg2 = fctGrp(o1, frame1);
            if (fg2 == null)
                return 0;
            return cmp.compare(fg1, frame1, fg2, frame2);
        }

        private DicomObject fctGrp(DicomObject o, int frame) {
            DicomObject sharedFctGrp = o
                    .getNestedDicomObject(Tag.SharedFunctionalGroupsSequence);
            if (sharedFctGrp != null) {
                DicomObject fctGrp = sharedFctGrp
                        .getNestedDicomObject(resolveTag(sharedFctGrp));
                if (fctGrp != null) {
                    return fctGrp;
                }
            }
            DicomElement frameFctGrpSeq = o
                    .get(Tag.PerFrameFunctionalGroupsSequence);
            if (frameFctGrpSeq == null)
                return null;
            DicomObject frameFctGrp = frameFctGrpSeq.getDicomObject(frame - 1);
            return frameFctGrp.getNestedDicomObject(resolveTag(frameFctGrp));
        }

    }

}
