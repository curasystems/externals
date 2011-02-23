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
 * Damien Evans <damien.daddy@gmail.com>
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

package org.dcm4che2.net.service;

/**
 * Base class for DICOM services.
 * 
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date: 2007-11-23 13:23:46 +0100 (Fri, 23 Nov 2007) $
 * @since Oct 3, 2005
 */
public abstract class DicomService
{
    private final String[] sopClasses;
    private final String serviceClass;

    /**
     * Constructor. The parameters of this constructor define the manner in
     * which the service will be looked up from the
     * <code>DicomServiceRegistry</code>. The <code>sopClasses</code> array
     * defines the specific SOP class UIDs that this <code>DicomService</code>
     * will respond to, while the <code>serviceClass</code> can define a
     * higher level SOP class UID that the service will respond to. The
     * <code>serviceClass</code> may be null.
     * 
     * @param sopClasses A String array containing the SOP class UIDs that this
     *            service responds to.
     * @param serviceClass The service class of the DicomDevice. For example,
     *            <code>UID.StorageServiceClass</code>. May be null.
     */
    protected DicomService(String[] sopClasses, String serviceClass) {
        this.sopClasses = sopClasses.clone();
        this.serviceClass = serviceClass;
    }

    /**
     * Constructor. The parameter of this constructor defines the manner in
     * which the service will be looked up from the
     * <code>DicomServiceRegistry</code>. The <code>sopClasses</code> array
     * defines the specific SOP class UIDs that this <code>DicomService</code>
     * will respond to.
     * 
     * @param sopClasses A String array containing the SOP class UIDs that this
     *            service responds to.
     */
    protected DicomService(String[] sopClasses)
    {
        this(sopClasses, null);
    }

    /**
     * Constructor. The parameter of this constructor defines the manner in
     * which the service will be looked up from the
     * <code>DicomServiceRegistry</code>. The <code>sopClass</code> defines
     * the specific SOP class UID that this <code>DicomService</code> will
     * respond to.
     * 
     * @param sopClasses A String array containing the SOP class UIDs that this
     *            service responds to.
     */
    protected DicomService(String sopClass)
    {
        if (sopClass == null)
            throw new NullPointerException("sopClass");
        this.sopClasses = new String[] { sopClass };
        this.serviceClass = null;
    }

    /**
     * Get the SOP classes that this service responds to.
     * 
     * @return String array containing the SOP Class UIDs.
     */
    public final String[] getSopClasses()
    {
        return sopClasses.clone();
    }

    /**
     * Get the SOP class that this service responds to. If there are multiple
     * SOP classes, the first one is returned.
     * 
     * @return String containing the SOP Class UID.
     */
    public final String getSopClass()
    {
        return sopClasses[0];
    }

    /**
     * Get the service class that this service belongs to.
     * 
     * @return String containing the SOP Class UID.
     */
    public final String getServiceClass()
    {
        return serviceClass;
    }

}
