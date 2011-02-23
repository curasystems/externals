package org.dcm4che2.image;

import static org.junit.Assert.*;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.junit.Test;

public class VOIUtilsTest {
    @Test
    public void testSelectVOIObjectShouldReturnImageIfOnlyWindowWidthAndCenter() {
        DicomObject obj = createWindowCenterObject();

        assertSame(obj, VOIUtils.selectVoiObject(obj, null, 1));
    }

    @Test
    public void testSelectVOIObjectShouldReturnNullIfWindowWidthWithoutCenter() {
        DicomObject obj = new BasicDicomObject();
        obj.putFloat(Tag.WindowWidth, VR.DS, 10.0f);

        assertNull(VOIUtils.selectVoiObject(obj, null, 1));
    }

    @Test
    public void testSelectVOIObjectShouldReturnNullIfWindowCenterWithoutWidth() {
        DicomObject obj = new BasicDicomObject();
        obj.putFloat(Tag.WindowWidth, VR.DS, 10.0f);

        assertNull(VOIUtils.selectVoiObject(obj, null, 1));
    }

    @Test
    public void testSelectVOIObjectShouldReturnImageIfValidVoiLutSequence() {
        DicomObject obj = new BasicDicomObject();
        DicomElement sq = obj.putSequence(Tag.VOILUTSequence);
        DicomObject item = new BasicDicomObject();
        item.putInts(Tag.LUTDescriptor, VR.US, new int[] { 1, 0, 8 });
        item.putInts(Tag.LUTData, VR.US, new int[] { 255 });
        sq.addDicomObject(item);

        assertSame(obj, VOIUtils.selectVoiObject(obj, null, 1));
    }

    @Test
    public void testSelectVOIObjectShouldReturnFrameIfPresentWithWindowCenterWidth() {
        DicomObject frameVoiLut = createWindowCenterObject();
        DicomObject perFrameGroup = createFrameGroups(frameVoiLut);
        DicomObject obj = new BasicDicomObject();
        obj.putNestedDicomObject(Tag.PerFrameFunctionalGroupsSequence, perFrameGroup);

        assertSame(frameVoiLut, VOIUtils.selectVoiObject(obj, null, 1));
    }

    @Test
    public void testSelectVOIObjectShouldReturnSecondFrameWhenAskedFor() {
        DicomObject frameVoiLut1 = createWindowCenterObject();
        DicomObject frameVoiLut2 = createWindowCenterObject();
        DicomObject obj = new BasicDicomObject();
        DicomElement frameGroupSequence = obj.putSequence(Tag.PerFrameFunctionalGroupsSequence);
        addFrame(frameGroupSequence, frameVoiLut1);
        addFrame(frameGroupSequence, frameVoiLut2);

        assertSame(frameVoiLut2, VOIUtils.selectVoiObject(obj, null, 2));
    }

    @Test
    public void testSelectVOIObjectShouldReturnSharedFrameDataIfPerFrameDataIsNotPresent() {
        DicomObject sharedVoiLut = createWindowCenterObject();
        DicomObject obj = new BasicDicomObject();
        DicomElement frameGroupSequence = obj.putSequence(Tag.PerFrameFunctionalGroupsSequence);
        addFrame(frameGroupSequence, null);
        addFrame(frameGroupSequence, null);
        obj.putNestedDicomObject(Tag.SharedFunctionalGroupsSequence,
                createFrameGroups(sharedVoiLut));

        assertSame(sharedVoiLut, VOIUtils.selectVoiObject(obj, null, 2));
    }
    
    @Test
    public void testSelectVOIObjectShouldReturnNullIfNoVoiDataIsFound() {
        assertNull(VOIUtils.selectVoiObject(new BasicDicomObject(), null, 1));
    }
    
    @Test
    public void testGetLUTShouldReturnTheSpecifiedLutSequence() {
        DicomObject mlut = new BasicDicomObject();
        mlut.putInts(Tag.LUTDescriptor, VR.US, new int[] { 1, 0, 8 });
        mlut.putInts(Tag.LUTData, VR.US, new int[] { 255 });
        mlut.putString(Tag.ModalityLUTSequence, VR.LO, "HU");
        
        DicomObject obj = new BasicDicomObject();
        obj.putNestedDicomObject(Tag.ModalityLUTSequence, mlut);
        
        assertSame(mlut, VOIUtils.getLUT(obj, Tag.ModalityLUTSequence));
    }
    
    @Test
    public void testGetLUTShouldReturnNullIfLutDataIsMissing() {
        DicomObject mlut = new BasicDicomObject();
        mlut.putInts(Tag.LUTDescriptor, VR.US, new int[] { 1, 0, 8 });
        mlut.putString(Tag.ModalityLUTSequence, VR.LO, "HU");
        
        DicomObject obj = new BasicDicomObject();
        obj.putNestedDicomObject(Tag.ModalityLUTSequence, mlut);
        
        assertNull(VOIUtils.getLUT(obj, Tag.ModalityLUTSequence));
    }
    
    @Test
    public void testGetLUTShouldReturnNullIfLutDescriptorIsMissing() {
        DicomObject mlut = new BasicDicomObject();
        mlut.putInts(Tag.LUTData, VR.US, new int[] { 255 });
        mlut.putString(Tag.ModalityLUTSequence, VR.LO, "HU");
        
        DicomObject obj = new BasicDicomObject();
        obj.putNestedDicomObject(Tag.ModalityLUTSequence, mlut);
        
        assertNull(VOIUtils.getLUT(obj, Tag.ModalityLUTSequence));
    }

    private void addFrame(DicomElement frameGroupSequence, DicomObject voiLutGroup) {
        frameGroupSequence.addDicomObject(createFrameGroups(voiLutGroup));
    }

    private DicomObject createFrameGroups(DicomObject voiLutGroup) {
        DicomObject perFrameGroup = new BasicDicomObject();
        if (voiLutGroup != null) {
            perFrameGroup.putNestedDicomObject(Tag.FrameVOILUTSequence, voiLutGroup);
        }
        return perFrameGroup;
    }

    private DicomObject createWindowCenterObject() {
        DicomObject frameVoiLut = new BasicDicomObject();
        frameVoiLut.putFloat(Tag.WindowWidth, VR.DS, 10.0f);
        frameVoiLut.putFloat(Tag.WindowCenter, VR.DS, 0.0f);
        return frameVoiLut;
    }
}
