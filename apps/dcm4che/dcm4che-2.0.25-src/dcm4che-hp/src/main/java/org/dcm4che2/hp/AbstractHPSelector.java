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

import org.dcm4che2.data.Tag;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 12635 $ $Date: 2010-01-18 14:15:50 +0100 (Mon, 18 Jan 2010) $
 * @since Oct 19, 2005
 *
 */
public abstract class AbstractHPSelector
implements HPSelector
{
    
    public String getImageSetSelectorUsageFlag()
    {
        return getDicomObject().getString(Tag.ImageSetSelectorUsageFlag);
    }

    public String getFilterByCategory()
    {
        return getDicomObject().getString(Tag.FilterByCategory);
    }
    
    public String getFilterByAttributePresence()
    {
        return getDicomObject().getString(Tag.FilterByAttributePresence);
    }
    
    public int getSelectorAttribute()
    {
        return getDicomObject().getInt(Tag.SelectorAttribute);
    }

    public String getSelectorAttributeVR()
    {
        return getDicomObject().getString(Tag.SelectorAttributeVR);
    }

    public int getSelectorSequencePointer()
    {
        return getDicomObject().getInt(Tag.SelectorSequencePointer);
    }

    public int getFunctionalGroupPointer()
    {
        return getDicomObject().getInt(Tag.FunctionalGroupPointer);
    }

    public String getSelectorSequencePointerPrivateCreator()
    {
        return getDicomObject().getString(Tag.SelectorSequencePointerPrivateCreator);
    }

    public String getFunctionalGroupPrivateCreator()
    {
        return getDicomObject().getString(Tag.FunctionalGroupPrivateCreator);
    }

    public String getSelectorAttributePrivateCreator()
    {
        return getDicomObject().getString(Tag.SelectorAttributePrivateCreator);
    }
    
    public Object getSelectorValue()
    {
        String vrStr =  getSelectorAttributeVR();
        if (vrStr == null || vrStr.length() != 2)
            return null;
        
        switch (vrStr.charAt(0) << 8 | vrStr.charAt(1))
        {
            case 0x4154:
                return getDicomObject().getInts(Tag.SelectorATValue);
            case 0x4353:
                return getDicomObject().getStrings(Tag.SelectorCSValue);
            case 0x4453:
                return getDicomObject().getFloats(Tag.SelectorDSValue);
            case 0x4644:
                return getDicomObject().getDoubles(Tag.SelectorFDValue);
            case 0x464c:
                return getDicomObject().getFloats(Tag.SelectorFLValue);
            case 0x4953:
                return getDicomObject().getInts(Tag.SelectorISValue);
            case 0x4c4f:
                return getDicomObject().getStrings(Tag.SelectorLOValue);
            case 0x4c54:
                return getDicomObject().getStrings(Tag.SelectorLTValue);
            case 0x504e:
                return getDicomObject().getStrings(Tag.SelectorPNValue);
            case 0x5348:
                return getDicomObject().getStrings(Tag.SelectorSHValue);
            case 0x534c:
                return getDicomObject().getInts(Tag.SelectorSLValue);
            case 0x5351:
                return Code.toArray(getDicomObject().get(Tag.SelectorCodeSequenceValue));
            case 0x5353:
                return getDicomObject().getInts(Tag.SelectorSSValue);
            case 0x5354:
                return getDicomObject().getStrings(Tag.SelectorSTValue);
            case 0x554c:
                return getDicomObject().getInts(Tag.SelectorULValue);
            case 0x5553:
                return getDicomObject().getInts(Tag.SelectorUSValue);
            case 0x5554:
                return getDicomObject().getStrings(Tag.SelectorUTValue);
        }
        return null;
    }

    public int getSelectorValueNumber()
    {
        return getDicomObject().getInt(Tag.SelectorValueNumber);
    }

    public String getFilterByOperator()
    {
        return getDicomObject().getString(Tag.FilterByOperator);
    }


}
