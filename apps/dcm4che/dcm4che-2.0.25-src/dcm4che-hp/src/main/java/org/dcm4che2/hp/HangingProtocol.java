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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.VR;
import org.dcm4che2.hp.spi.HPCategorySpi;
import org.dcm4che2.hp.spi.HPComparatorSpi;
import org.dcm4che2.hp.spi.HPRegistry;
import org.dcm4che2.hp.spi.HPSelectorSpi;
import org.dcm4che2.util.UIDUtils;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 6915 $ $Date: 2008-08-31 23:36:20 +0200 (Sun, 31 Aug 2008) $
 * @since Jul 30, 2005
 */
public class HangingProtocol {
    private final DicomObject dcmobj;

    private List<HPDefinition> definitions;
    private List<HPScreenDefinition> screenDefs;
    private List<HPImageSet> imageSets;
    private List<HPDisplaySet> displaySets;
    private List<HPScrollingGroup> scrollingGroups;
    private List<HPNavigationGroup> navigationGroups;
    private int maxPresGroup = 0;

    public HangingProtocol(DicomObject dcmobj) {
        this.dcmobj = dcmobj;
        init();
    }

    public HangingProtocol() {
        definitions = new ArrayList<HPDefinition>();
        screenDefs = new ArrayList<HPScreenDefinition>();
        imageSets = new ArrayList<HPImageSet>();
        displaySets = new ArrayList<HPDisplaySet>();
        dcmobj = new BasicDicomObject();
        dcmobj.putSequence(Tag.HangingProtocolDefinitionSequence);
        dcmobj.putSequence(Tag.NominalScreenDefinitionSequence);
        dcmobj.putSequence(Tag.ImageSetsSequence);
        dcmobj.putSequence(Tag.DisplaySetsSequence);
    }

    public HangingProtocol(HangingProtocol source) {
        dcmobj = new BasicDicomObject();
        source.getDicomObject().copyTo(dcmobj);
        init();
        String iuid = getSOPInstanceUID();
        if (iuid != null) {
            String cuid = getSOPClassUID();
            ReferencedSOP refSOP = new ReferencedSOP();
            refSOP.setReferencedSOPInstanceUID(iuid);
            refSOP.setReferencedSOPClassUID(cuid != null ? cuid
                    : UID.HangingProtocolStorage);
            setSourceHangingProtocol(refSOP);
            setSOPInstanceUID(UIDUtils.createUID());
        }
    }

    protected HPImageSet createImageSet(List<HPSelector> selectors,
            DicomObject dcmobj) {
        return new HPImageSet(selectors, dcmobj);
    }

    protected HPDisplaySet createDisplaySet(DicomObject ds, HPImageSet is) {
        return new HPDisplaySet(ds, is);
    }

    protected HPDefinition createHangingProtocolDefinition(DicomObject dcmobj) {
        return new HPDefinition(dcmobj);
    }

    protected HPScreenDefinition createNominalScreenDefinition(DicomObject item) {
        return new HPScreenDefinition(item);
    }

    protected HPNavigationGroup createNavigationGroup(DicomObject dcmobj) {
        return new HPNavigationGroup(dcmobj, displaySets);
    }

    protected HPScrollingGroup createScrollingGroup(DicomObject dssg) {
        return new HPScrollingGroup(dssg, displaySets);
    }

    /**
     * Returns the <tt>DicomObject</tt> that backs this <tt>HangingProtocol</tt>.
     * 
     * Direct modifications of the returned <tt>DicomObject</tt> is strongly
     * discouraged as it may cause inconsistencies in the internal state
     * of this object.
     * 
     * @return the <tt>DicomObject</tt> that backs this Hanging Protocol
     */
    public DicomObject getDicomObject() {
        return dcmobj;
    }

    public String getHangingProtocolName() {
        return dcmobj.getString(Tag.HangingProtocolName);
    }

    public void setHangingProtocolName(String name) {
        dcmobj.putString(Tag.HangingProtocolName, VR.SH, name);
    }

    public String getHangingProtocolDescription() {
        return dcmobj.getString(Tag.HangingProtocolDescription);
    }

    public void setHangingProtocolDescription(String description) {
        dcmobj.putString(Tag.HangingProtocolDescription, VR.LO, description);
    }

    public String getHangingProtocolLevel() {
        return dcmobj.getString(Tag.HangingProtocolLevel);
    }

    public void setHangingProtocolLevel(String level) {
        dcmobj.putString(Tag.HangingProtocolLevel, VR.CS, level);
    }

    public String getHangingProtocolCreator() {
        return dcmobj.getString(Tag.HangingProtocolCreator);
    }

    public void setHangingProtocolCreator(String creator) {
        dcmobj.putString(Tag.HangingProtocolCreator, VR.LO, creator);
    }

    public Date getHangingProtocolCreationDateTime() {
        return dcmobj.getDate(Tag.HangingProtocolCreationDateTime);
    }

    public void setHangingProtocolCreationDateTime(Date datetime) {
        dcmobj.putDate(Tag.HangingProtocolCreationDateTime, VR.DT, datetime);
    }

    public int getNumberOfPriorsReferenced() {
        return dcmobj.getInt(Tag.NumberOfPriorsReferenced);
    }

    public void setNumberOfPriorsReferenced(int priors) {
        dcmobj.putInt(Tag.NumberOfPriorsReferenced, VR.US, priors);
    }

    public int getNumberOfScreens() {
        return dcmobj.getInt(Tag.NumberOfScreens);
    }

    public void setNumberOfScreens(int screens) {
        dcmobj.putInt(Tag.NumberOfScreens, VR.US, screens);
    }

    public Code getHangingProtocolUserIdentificationCode() {
        DicomObject item = dcmobj
                .getNestedDicomObject(Tag.HangingProtocolUserIdentificationCodeSequence);
        return item != null ? new Code(item) : null;
    }

    public void setHangingProtocolUserIdentificationCodeSequence(Code user) {
        dcmobj.putNestedDicomObject(
                Tag.HangingProtocolUserIdentificationCodeSequence, user
                        .getDicomObject());
    }

    public ReferencedSOP getSourceHangingProtocol() {
        DicomObject item = dcmobj
                .getNestedDicomObject(Tag.SourceHangingProtocolSequence);
        return item != null ? new ReferencedSOP(item) : null;
    }

    public void setSourceHangingProtocol(ReferencedSOP sop) {
        dcmobj.putNestedDicomObject(Tag.SourceHangingProtocolSequence, sop
                .getDicomObject());
    }

    public String getHangingProtocolUserGroupName() {
        return dcmobj.getString(Tag.HangingProtocolUserGroupName);
    }

    public void setHangingProtocolUserGroupName(String name) {
        dcmobj.putString(Tag.HangingProtocolUserGroupName, VR.LO, name);
    }

    public String getPartialDataDisplayHandling() {
        return dcmobj.getString(Tag.PartialDataDisplayHandling);
    }

    public void setPartialDataDisplayHandling(String type) {
        dcmobj.putString(Tag.PartialDataDisplayHandling, VR.CS, type);
    }

    public List<HPDefinition> getHangingProtocolDefinitions() {
        return Collections.unmodifiableList(definitions);
    }

    public void addHangingProtocolDefinition(HPDefinition def) {
        if (def == null)
            throw new NullPointerException();

        dcmobj.get(Tag.HangingProtocolDefinitionSequence).addDicomObject(
                def.getDicomObject());
        definitions.add(def);
    }

    public boolean removeHangingProtocolDefinition(HPDefinition def) {
        if (def == null)
            throw new NullPointerException();

        int index = definitions.indexOf(def);
        if (index == -1) {
            return false;
        }

        dcmobj.get(Tag.HangingProtocolDefinitionSequence).removeDicomObject(
                index);
        definitions.remove(index);
        return true;
    }

    public void removeAllHangingProtocolDefinition() {
        dcmobj.putSequence(Tag.HangingProtocolDefinitionSequence);
        definitions.clear();
    }

    public List<HPImageSet> getImageSets() {
        return Collections.unmodifiableList(imageSets);
    }

    public HPImageSet addNewImageSet(HPImageSet shareSelectors) {
        HPImageSet is;
        if (shareSelectors != null) {
            if (shareSelectors.getDicomObject().getParent().getParent() != dcmobj) {
                throw new IllegalArgumentException(
                        "shareSelectors does not belongs to this HP object");
            }
            is = new HPImageSet(shareSelectors);
        } else {
            is = new HPImageSet();
            dcmobj.get(Tag.ImageSetsSequence).addDicomObject(
                    is.getDicomObject().getParent());
        }
        is.setImageSetNumber(imageSets.size() + 1);
        imageSets.add(is);
        return is;
    }

    public boolean removeImageSet(HPImageSet imageSet) {
        if (imageSet == null)
            throw new NullPointerException();

        int index = imageSets.indexOf(imageSet);
        if (index == -1) {
            return false;
        }

        for (Iterator<HPDisplaySet> iter = getDisplaySetsOfImageSet(imageSet)
                .iterator(); iter.hasNext();) {
            removeDisplaySet(iter.next());
        }

        DicomObject tbis = imageSet.getDicomObject();
        DicomObject is = tbis.getParent();
        DicomElement tbissq = is.get(Tag.TimeBasedImageSetsSequence);
        tbissq.removeDicomObject(tbis);
        if (tbissq.isEmpty()) {
            dcmobj.get(Tag.ImageSetsSequence).removeDicomObject(is);
        }
        imageSets.remove(index);

        for (; index < imageSets.size(); ++index) {
            HPImageSet otherImageSet = imageSets.get(index);
            otherImageSet.setImageSetNumber(index + 1);
            for (Iterator<HPDisplaySet> iter = getDisplaySetsOfImageSet(
                    otherImageSet).iterator(); iter.hasNext();) {
                iter.next().setImageSet(otherImageSet);
            }
        }

        return true;
    }

    public void removeAllImageSets() {
        dcmobj.putSequence(Tag.ImageSetsSequence);
        imageSets.clear();
        removeAllDisplaySets();
    }

    public List<HPScreenDefinition> getNominalScreenDefinitions() {
        return Collections.unmodifiableList(screenDefs);
    }

    public void addNominalScreenDefinition(HPScreenDefinition def) {
        if (def == null)
            throw new NullPointerException();

        dcmobj.get(Tag.NominalScreenDefinitionSequence).addDicomObject(
                def.getDicomObject());
        screenDefs.add(def);
    }

    public boolean removeNominalScreenDefinition(HPScreenDefinition def) {
        if (def == null)
            throw new NullPointerException();

        int index = screenDefs.indexOf(def);
        if (index == -1) {
            return false;
        }

        dcmobj.get(Tag.NominalScreenDefinitionSequence)
                .removeDicomObject(index);
        screenDefs.remove(index);
        return true;
    }

    public void removeAllNominalScreenDefinitions() {
        dcmobj.putSequence(Tag.NominalScreenDefinitionSequence);
        screenDefs.clear();
    }

    public int getNumberOfPresentationGroups() {
        return maxPresGroup;
    }

    public List<HPDisplaySet> getDisplaySetsOfPresentationGroup(int pgNo) {
        ArrayList<HPDisplaySet> result = new ArrayList<HPDisplaySet>(
                displaySets.size());
        for (int i = 0, n = displaySets.size(); i < n; i++) {
            HPDisplaySet ds = displaySets.get(i);
            if (ds.getDisplaySetPresentationGroup() == pgNo)
                result.add(ds);
        }
        return result;
    }

    public List<HPDisplaySet> getDisplaySetsOfImageSet(HPImageSet is) {
        ArrayList<HPDisplaySet> result = new ArrayList<HPDisplaySet>(
                displaySets.size());
        for (int i = 0, n = displaySets.size(); i < n; i++) {
            HPDisplaySet ds = displaySets.get(i);
            if (ds.getImageSet() == is)
                result.add(ds);
        }
        return result;
    }

    public String getDisplaySetPresentationGroupDescription(int pgNo) {
        for (int i = 0, n = displaySets.size(); i < n; i++) {
            HPDisplaySet ds = displaySets.get(i);
            if (ds.getDisplaySetPresentationGroup() == pgNo) {
                String desc = ds.getDisplaySetPresentationGroupDescription();
                if (desc != null)
                    return desc;
            }
        }
        return null;
    }

    public List<HPDisplaySet> getDisplaySets() {
        return Collections.unmodifiableList(displaySets);
    }

    public HPDisplaySet addNewDisplaySet(HPImageSet imageSet,
            HPDisplaySet prototype) {
        if (imageSet == null) {
            throw new NullPointerException("imageSet");
        }
        if (!imageSets.contains(imageSet)) {
            throw new IllegalArgumentException(
                    "imageSet does not belongs to this HP object");
        }
        DicomObject dcmobj = new BasicDicomObject();
        if (prototype != null) {
            prototype.getDicomObject().copyTo(dcmobj);
        } else {
            dcmobj.putSequence(Tag.ImageBoxesSequence);
            dcmobj.putSequence(Tag.FilterOperationsSequence);
            dcmobj.putSequence(Tag.SortingOperationsSequence);
        }
        dcmobj.putInt(Tag.ImageSetNumber, VR.US, imageSet.getImageSetNumber());
        HPDisplaySet displaySet = createDisplaySet(dcmobj, imageSet);
        doAddDisplaySet(displaySet);
        return displaySet;
    }

    /**
     * @deprecated use {@link addNewDisplaySet} instead
     */
    @Deprecated
    public void addDisplaySet(HPDisplaySet displaySet) {
        if (displaySet == null)
            throw new NullPointerException("displaySet");

        doAddDisplaySet(displaySet);
    }

    protected void doAddDisplaySet(HPDisplaySet displaySet) {
        displaySet.setDisplaySetNumber(displaySets.size() + 1);
        int group = displaySet.getDisplaySetPresentationGroup();
        if (group == 0) {
            group = Math.max(maxPresGroup, 1);
            displaySet.setDisplaySetPresentationGroup(group);
        }
        maxPresGroup = Math.max(maxPresGroup, group);
        dcmobj.get(Tag.DisplaySetsSequence).addDicomObject(
                displaySet.getDicomObject());
        displaySets.add(displaySet);
    }

    public boolean removeDisplaySet(HPDisplaySet displaySet) {
        if (displaySet == null)
            throw new NullPointerException();

        int index = displaySets.indexOf(displaySet);
        if (index == -1) {
            return false;
        }

        DicomElement displaySetsSeq = dcmobj.get(Tag.DisplaySetsSequence);
        displaySetsSeq.removeDicomObject(index);
        displaySets.remove(index);

        for (; index < displaySets.size(); ++index) {
            displaySets.get(index).setDisplaySetNumber(index + 1);
        }

        if (scrollingGroups != null) {
            int sgi = 0;
            for (Iterator<HPScrollingGroup> iter = scrollingGroups.iterator(); iter
                    .hasNext(); ++sgi) {
                HPScrollingGroup sg = iter.next();
                if (sg.removeDisplaySet(displaySet) && !sg.isValid()) {
                    dcmobj.get(Tag.SynchronizedScrollingSequence)
                            .removeDicomObject(sgi--);
                    iter.remove();
                } else {
                    sg.updateDicomObject();
                }
            }
        }

        if (navigationGroups != null) {
            int ngi = 0;
            for (Iterator<HPNavigationGroup> iter = navigationGroups.iterator(); iter
                    .hasNext(); ++ngi) {
                HPNavigationGroup ng = iter.next();
                if (ng.removeReferenceDisplaySet(displaySet) && !ng.isValid()
                        || ng.getNavigationDisplaySet() == displaySet) {
                    if (ng.getNavigationDisplaySet() == displaySet) {
                        ng.setNavigationDisplaySet(null);
                    }
                    dcmobj.get(Tag.NavigationIndicatorSequence)
                            .removeDicomObject(ngi--);
                    iter.remove();
                } else {
                    ng.updateDicomObject();
                }
            }
        }

        return true;
    }

    public void removeAllDisplaySets() {
        dcmobj.putSequence(Tag.DisplaySetsSequence);
        displaySets.clear();
        removeAllScrollingGroups();
        removeAllNavigationGroups();
        maxPresGroup = 0;
    }

    public List<HPScrollingGroup> getScrollingGroups() {
        return maskNull(scrollingGroups);
    }

    public void addScrollingGroup(HPScrollingGroup scrollingGroup) {
        DicomElement sq = dcmobj.get(Tag.SynchronizedScrollingSequence);
        if (sq == null)
            sq = dcmobj.putSequence(Tag.SynchronizedScrollingSequence);
        sq.addDicomObject(scrollingGroup.getDicomObject());
        if (scrollingGroups == null)
            scrollingGroups = new ArrayList<HPScrollingGroup>();
        scrollingGroups.add(scrollingGroup);
    }

    public boolean removeScrollingGroup(HPScrollingGroup scrollingGroup) {
        if (scrollingGroup == null)
            throw new NullPointerException();

        if (scrollingGroups == null) {
            return false;
        }

        int index = scrollingGroups.indexOf(scrollingGroup);
        if (index == -1) {
            return false;
        }

        dcmobj.get(Tag.SynchronizedScrollingSequence).removeDicomObject(index);
        scrollingGroups.remove(index);
        return true;
    }

    public void removeAllScrollingGroups() {
        dcmobj.remove(Tag.SynchronizedScrollingSequence);
        scrollingGroups = null;
    }

    public List<HPNavigationGroup> getNavigationGroups() {
        return maskNull(navigationGroups);
    }

    private <T> List<T> maskNull(List<T> list) {
        return list == null ? Collections.<T> emptyList() : Collections
                .unmodifiableList(list);
    }

    public void addNavigationGroup(HPNavigationGroup navigationGroup) {
        DicomElement sq = dcmobj.get(Tag.NavigationIndicatorSequence);
        if (sq == null)
            sq = dcmobj.putSequence(Tag.NavigationIndicatorSequence);
        sq.addDicomObject(navigationGroup.getDicomObject());
        if (navigationGroups == null)
            navigationGroups = new ArrayList<HPNavigationGroup>();
        navigationGroups.add(navigationGroup);
    }

    public boolean removeNavigationGroup(HPNavigationGroup navigationGroup) {
        if (navigationGroup == null)
            throw new NullPointerException();

        if (navigationGroups == null) {
            return false;
        }

        int index = navigationGroups.indexOf(navigationGroup);
        if (index == -1) {
            return false;
        }

        dcmobj.get(Tag.NavigationIndicatorSequence).removeDicomObject(index);
        navigationGroups.remove(index);
        return true;
    }

    public void removeAllNavigationGroups() {
        dcmobj.remove(Tag.NavigationIndicatorSequence);
        navigationGroups = null;
    }

    private void init() {
        initHangingProtocolDefinition();
        initNominalScreenDefinition();
        initImageSets();
        initDisplaySets();
        initScrollingGroups();
        initNavigationGroups();
    }

    private void initNavigationGroups() {
        DicomElement nis = dcmobj.get(Tag.NavigationIndicatorSequence);
        if (nis == null || nis.isEmpty())
            return;

        int numNavGroups = nis.countItems();
        navigationGroups = new ArrayList<HPNavigationGroup>(numNavGroups);
        for (int i = 0; i < numNavGroups; i++) {
            navigationGroups.add(createNavigationGroup(nis.getDicomObject(i)));
        }
    }

    private void initScrollingGroups() {
        DicomElement ssq = dcmobj.get(Tag.SynchronizedScrollingSequence);
        if (ssq == null || ssq.isEmpty())
            return;

        int numScrollingGroups = ssq.countItems();
        scrollingGroups = new ArrayList<HPScrollingGroup>(numScrollingGroups);
        for (int i = 0; i < numScrollingGroups; i++) {
            scrollingGroups.add(createScrollingGroup(ssq.getDicomObject(i)));
        }

    }

    private void initDisplaySets() {
        DicomElement dssq = dcmobj.get(Tag.DisplaySetsSequence);
        if (dssq == null)
            throw new IllegalArgumentException(
                    "Missing (0072,0200) Display Sets Sequence");
        // if (dssq.isEmpty())
        // throw new IllegalArgumentException(
        // "Empty (0072,0200) Display Sets Sequence");
        int numDisplaySets = dssq.countItems();
        displaySets = new ArrayList<HPDisplaySet>(numDisplaySets);
        for (int i = 0; i < numDisplaySets; i++) {
            DicomObject ds = dssq.getDicomObject(i);
            if (ds.getInt(Tag.DisplaySetNumber) != displaySets.size() + 1) {
                throw new IllegalArgumentException(
                        "Missing or invalid (0072,0202) Display Set Number: "
                                + ds.get(Tag.DisplaySetNumber));
            }
            final int dspg = ds.getInt(Tag.DisplaySetPresentationGroup);
            if (dspg == 0)
                throw new IllegalArgumentException(
                        "Missing or invalid (0072,0204) Display Set Presentation Group: "
                                + ds.get(Tag.DisplaySetPresentationGroup));
            maxPresGroup = Math.max(maxPresGroup, dspg);
            HPImageSet is;
            try {
                is = imageSets.get(ds.getInt(Tag.ImageSetNumber) - 1);
            } catch (IndexOutOfBoundsException e) {
                throw new IllegalArgumentException(
                        "Missing or invalid (0072,0032) Image Set Number: "
                                + ds.get(Tag.ImageSetNumber));
            }
            displaySets.add(createDisplaySet(ds, is));
        }
    }

    private void initImageSets() {
        DicomElement issq = dcmobj.get(Tag.ImageSetsSequence);
        if (issq == null)
            throw new IllegalArgumentException(
                    "Missing (0072,0020) Image Sets Sequence");
        // if (issq.isEmpty())
        // throw new IllegalArgumentException(
        // "Empty (0072,0020) Image Sets Sequence");
        imageSets = new ArrayList<HPImageSet>();
        for (int i = 0, n = issq.countItems(); i < n; i++) {
            DicomObject is = issq.getDicomObject(i);
            DicomElement isssq = is.get(Tag.ImageSetSelectorSequence);
            if (isssq == null)
                throw new IllegalArgumentException(
                        "Missing (0072,0022) Image Set Selector Sequence");
            // if (isssq.isEmpty())
            // throw new IllegalArgumentException(
            // "Empty (0072,0022) Image Set Selector Sequence");
            int isssqCount = isssq.countItems();
            List<HPSelector> selectors = new ArrayList<HPSelector>(isssqCount);
            for (int j = 0; j < isssqCount; j++) {
                selectors.add(HPSelectorFactory.createImageSetSelector(isssq
                        .getDicomObject(j)));
            }
            DicomElement tbissq = is.get(Tag.TimeBasedImageSetsSequence);
            if (tbissq == null)
                throw new IllegalArgumentException(
                        "Missing (0072,0030) Time Based Image Sets Sequence");
            // if (tbissq.isEmpty())
            // throw new IllegalArgumentException(
            // "Empty (0072,0030) Time Based Image Sets Sequence");
            for (int j = 0, m = tbissq.countItems(); j < m; j++) {
                DicomObject timeBasedSelector = tbissq.getDicomObject(j);
                if (timeBasedSelector.getInt(Tag.ImageSetNumber) != imageSets
                        .size() + 1) {
                    throw new IllegalArgumentException(
                            "Missing or invalid (0072,0032) Image Set Number: "
                                    + timeBasedSelector.get(Tag.ImageSetNumber));
                }
                imageSets.add(createImageSet(selectors, timeBasedSelector));
            }
        }
    }

    private void initNominalScreenDefinition() {
        DicomElement nsdsq = dcmobj.get(Tag.NominalScreenDefinitionSequence);
        if (nsdsq == null || nsdsq.isEmpty()) {
            screenDefs = Collections.emptyList();
        } else {
            int numScreenDef = nsdsq.countItems();
            screenDefs = new ArrayList<HPScreenDefinition>(numScreenDef);
            for (int i = 0; i < numScreenDef; i++) {
                screenDefs.add(createNominalScreenDefinition(nsdsq
                        .getDicomObject(i)));
            }
        }
    }

    private void initHangingProtocolDefinition() {
        DicomElement defsq = dcmobj.get(Tag.HangingProtocolDefinitionSequence);
        if (defsq == null)
            throw new IllegalArgumentException(
                    "Missing (0072,000C) Hanging Protocol Definition Sequence");
        // if (defsq.isEmpty())
        // throw new IllegalArgumentException(
        // "Empty (0072,000C) Hanging Protocol Definition Sequence");
        int numDefinitions = defsq.countItems();
        definitions = new ArrayList<HPDefinition>(numDefinitions);
        for (int i = 0; i < numDefinitions; i++) {
            definitions.add(createHangingProtocolDefinition(defsq
                    .getDicomObject(i)));
        }
    }

    public static void scanForPlugins(ClassLoader cl) {
        HPRegistry.getHPRegistry().registerApplicationClasspathSpis(cl);
    }

    public static HPSelectorSpi getHPSelectorSpi(String category) {
        return (HPSelectorSpi) getHPCategorySpi(HPSelectorSpi.class, category);
    }

    public static HPComparatorSpi getHPComparatorSpi(String category) {
        return (HPComparatorSpi) getHPCategorySpi(HPComparatorSpi.class,
                category);
    }

    private static HPCategorySpi getHPCategorySpi(Class<?> serviceClass,
            final String category) {
        Iterator<?> iter = HPRegistry.getHPRegistry().getServiceProviders(
                serviceClass, new HPRegistry.Filter() {
                    public boolean filter(Object provider) {
                        return ((HPCategorySpi) provider)
                                .containsCategory(category);
                    }
                }, true);
        return (HPCategorySpi) (iter.hasNext() ? iter.next() : null);
    }

    public static String[] getSupportedHPSelectorCategories() {
        return getSupportedHPCategories(HPSelectorSpi.class);
    }

    public static String[] getSupportedHPComparatorCategories() {
        return getSupportedHPCategories(HPComparatorSpi.class);
    }

    private static <T> String[] getSupportedHPCategories(Class<T> serviceClass) {
        Iterator<T> iter = HPRegistry.getHPRegistry().getServiceProviders(
                serviceClass, true);
        HashSet<String> set = new HashSet<String>();
        while (iter.hasNext()) {
            HPCategorySpi spi = (HPCategorySpi) iter.next();
            String[] ss = spi.getCategories();
            for (int i = 0; i < ss.length; i++) {
                set.add(ss[i]);
            }
        }
        return set.toArray(new String[set.size()]);
    }

    public String getSOPClassUID() {
        return dcmobj.getString(Tag.SOPClassUID);
    }

    public void setSOPClassUID(String uid) {
        dcmobj.putString(Tag.SOPClassUID, VR.UI, uid);
    }

    public String getSOPInstanceUID() {
        return dcmobj.getString(Tag.SOPInstanceUID);
    }

    public void setSOPInstanceUID(String uid) {
        dcmobj.putString(Tag.SOPInstanceUID, VR.UI, uid);
    }

    public String[] getSpecificCharacterSet() {
        return dcmobj.getStrings(Tag.SpecificCharacterSet);
    }

    public void setSpecificCharacterSet(String[] ss) {
        dcmobj.putStrings(Tag.SpecificCharacterSet, VR.CS, ss);
    }

    public Date getInstanceCreationDateTime() {
        return dcmobj.getDate(Tag.InstanceCreationDate,
                Tag.InstanceCreationTime);
    }

    public void setInstanceCreationDateTime(Date d) {
        dcmobj.putDate(Tag.InstanceCreationDate, VR.DA, d);
        dcmobj.putDate(Tag.InstanceCreationTime, VR.TM, d);
    }

    public String getInstanceCreatorUID() {
        return dcmobj.getString(Tag.InstanceCreatorUID);
    }

    public void setInstanceCreatorUID(String s) {
        dcmobj.putString(Tag.InstanceCreatorUID, VR.UI, s);
    }

    public String getRelatedGeneralSOPClassUID() {
        return dcmobj.getString(Tag.RelatedGeneralSOPClassUID);
    }

    public void setRelatedGeneralSOPClassUID(String s) {
        dcmobj.putString(Tag.RelatedGeneralSOPClassUID, VR.UI, s);
    }

    public String getOriginalSpecializedSOPClassUID() {
        return dcmobj.getString(Tag.OriginalSpecializedSOPClassUID);
    }

    public void setOriginalSpecializedSOPClassUID(String s) {
        dcmobj.putString(Tag.OriginalSpecializedSOPClassUID, VR.UI, s);
    }

    public String getInstanceNumber() {
        return dcmobj.getString(Tag.InstanceNumber);
    }

    public void setInstanceNumber(String s) {
        dcmobj.putString(Tag.InstanceNumber, VR.IS, s);
    }

}
