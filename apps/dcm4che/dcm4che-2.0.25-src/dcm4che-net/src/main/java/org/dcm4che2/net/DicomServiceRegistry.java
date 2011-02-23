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

package org.dcm4che2.net;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.net.service.CEchoSCP;
import org.dcm4che2.net.service.CFindSCP;
import org.dcm4che2.net.service.CGetSCP;
import org.dcm4che2.net.service.CMoveSCP;
import org.dcm4che2.net.service.CStoreSCP;
import org.dcm4che2.net.service.DicomService;
import org.dcm4che2.net.service.NActionSCP;
import org.dcm4che2.net.service.NCreateSCP;
import org.dcm4che2.net.service.NDeleteSCP;
import org.dcm4che2.net.service.NEventReportSCU;
import org.dcm4che2.net.service.NGetSCP;
import org.dcm4che2.net.service.NSetSCP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date: 2008-06-13 09:36:38 +0200 (Fri, 13 Jun 2008) $
 * @since Oct 3, 2005
 *
 */
class DicomServiceRegistry 
{
    static Logger log = LoggerFactory.getLogger(DicomServiceRegistry.class);
    private final HashSet<String> sopCUIDs = new HashSet<String>();
    private final HashMap<String, DicomService> cstoreSCP = new HashMap<String, DicomService>();
    private final HashMap<String, DicomService> cgetSCP = new HashMap<String, DicomService>();
    private final HashMap<String, DicomService> cmoveSCP = new HashMap<String, DicomService>();
    private final HashMap<String, DicomService> cfindSCP = new HashMap<String, DicomService>();
    private final HashMap<String, DicomService> cechoSCP = new HashMap<String, DicomService>(
            1);
    private final HashMap<String, DicomService> neventReportSCU = new HashMap<String, DicomService>();
    private final HashMap<String, DicomService> ngetSCP = new HashMap<String, DicomService>();
    private final HashMap<String, DicomService> nsetSCP = new HashMap<String, DicomService>();
    private final HashMap<String, DicomService> nactionSCP = new HashMap<String, DicomService>();
    private final HashMap<String, DicomService> ncreateSCP = new HashMap<String, DicomService>();
    private final HashMap<String, DicomService> ndeleteSCP = new HashMap<String, DicomService>();

    private void registerInto(HashMap<String, DicomService> registry, DicomService service)
    {
        final String[] sopClasses = service.getSopClasses();
        for (int i = 0; i < sopClasses.length; i++)
        {
            registry.put(sopClasses[i], service);
            sopCUIDs.add(sopClasses[i]);
        }
        final String serviceClass = service.getServiceClass();
        if (serviceClass != null)
        {
            registry.put(serviceClass, service);
            sopCUIDs.add(serviceClass);
        }
    }

    private void unregisterFrom(HashMap<String, DicomService> registry, DicomService service)
    {
        for (Iterator<Map.Entry<String, DicomService>> iter = registry.entrySet().iterator(); iter.hasNext();)
        {
            Map.Entry<String, DicomService> element = iter.next();
            if (element.getValue() == service)
                iter.remove();
        }
    }
    
    public void register(DicomService service) {
        if (service instanceof CStoreSCP)
            registerInto(cstoreSCP, service);
        if (service instanceof CGetSCP)
            registerInto(cgetSCP, service);
        if (service instanceof CMoveSCP)
            registerInto(cmoveSCP, service);
        if (service instanceof CFindSCP)
            registerInto(cfindSCP, service);
        if (service instanceof CEchoSCP)
            registerInto(cechoSCP, service);
        if (service instanceof NEventReportSCU)
            registerInto(neventReportSCU, service);
        if (service instanceof NGetSCP)
            registerInto(ngetSCP, service);
        if (service instanceof NSetSCP)
            registerInto(nsetSCP, service);
        if (service instanceof NActionSCP)
            registerInto(nactionSCP, service);
        if (service instanceof NCreateSCP)
            registerInto(ncreateSCP, service);
        if (service instanceof NDeleteSCP)
            registerInto(ndeleteSCP, service);
    }

    public void unregister(DicomService service) {
        if (service instanceof CStoreSCP)
            unregisterFrom(cstoreSCP, service);
        if (service instanceof CGetSCP)
            unregisterFrom(cgetSCP, service);
        if (service instanceof CMoveSCP)
            unregisterFrom(cmoveSCP, service);
        if (service instanceof CFindSCP)
            unregisterFrom(cfindSCP, service);
        if (service instanceof CEchoSCP)
            unregisterFrom(cechoSCP, service);
        if (service instanceof NEventReportSCU)
            unregisterFrom(neventReportSCU, service);
        if (service instanceof NGetSCP)
            unregisterFrom(ngetSCP, service);
        if (service instanceof NSetSCP)
            unregisterFrom(nsetSCP, service);
        if (service instanceof NActionSCP)
            unregisterFrom(nactionSCP, service);
        if (service instanceof NCreateSCP)
            unregisterFrom(ncreateSCP, service);
        if (service instanceof NDeleteSCP)
            unregisterFrom(ndeleteSCP, service);
    }
    

    private Object getFrom(HashMap<String, DicomService> registry, DicomObject cmd, int tag)
    throws DicomServiceException {
        String cuid = cmd.getString(tag);
        Object scp = registry.get(cuid);
        if (scp == null) {
            throw new DicomServiceException(cmd, sopCUIDs.contains(cuid) 
                    ? Status.UnrecognizedOperation : Status.NoSuchSOPclass);
        }
        return scp;
    }
    
    private CStoreSCP getCStoreSCP(DicomObject cmd)
            throws DicomServiceException {
        return (CStoreSCP) getFrom(cstoreSCP, cmd, Tag.AffectedSOPClassUID);
    }

    private CGetSCP getCGetSCP(DicomObject cmd)
            throws DicomServiceException {
        return (CGetSCP) getFrom(cgetSCP, cmd, Tag.AffectedSOPClassUID);
    }

    private CFindSCP getCFindSCP(DicomObject cmd)
            throws DicomServiceException {
        return (CFindSCP) getFrom(cfindSCP, cmd, Tag.AffectedSOPClassUID);
    }

    private CMoveSCP getCMoveSCP(DicomObject cmd)
            throws DicomServiceException {
        return (CMoveSCP) getFrom(cmoveSCP, cmd, Tag.AffectedSOPClassUID);
    }

    private CEchoSCP getCEchoSCP(DicomObject cmd)
            throws DicomServiceException {
        return (CEchoSCP) getFrom(cechoSCP, cmd, Tag.AffectedSOPClassUID);
    }

    private NEventReportSCU getNEventReportSCU(DicomObject cmd)
            throws DicomServiceException {
        return (NEventReportSCU) getFrom(neventReportSCU, cmd, Tag.AffectedSOPClassUID);

    }

    private NGetSCP getNGetSCP(DicomObject cmd)
            throws DicomServiceException {
        return (NGetSCP) getFrom(ngetSCP, cmd, Tag.RequestedSOPClassUID);
    }

    private NSetSCP getNSetSCP(DicomObject cmd)
            throws DicomServiceException {
        return (NSetSCP) getFrom(nsetSCP, cmd, Tag.RequestedSOPClassUID);
    }

    private NActionSCP getNActionSCP(DicomObject cmd)
            throws DicomServiceException {
        return (NActionSCP) getFrom(nactionSCP, cmd, Tag.RequestedSOPClassUID);
    }

    private NCreateSCP getNCreateSCP(DicomObject cmd)
            throws DicomServiceException {
        return (NCreateSCP) getFrom(ncreateSCP, cmd, Tag.AffectedSOPClassUID);
    }


    private NDeleteSCP getNDeleteSCP(DicomObject cmd)
            throws DicomServiceException {
        return (NDeleteSCP) getFrom(ndeleteSCP, cmd, Tag.RequestedSOPClassUID);
    }
    
    public void process(Association as, int pcid, DicomObject cmd,
            PDVInputStream dataStream, String tsuid) throws IOException
    {
        try {
            final int cmdfield = cmd.getInt(Tag.CommandField);
            if (cmdfield == CommandUtils.C_STORE_RQ) {
                getCStoreSCP(cmd).cstore(as, pcid, cmd,
                        dataStream, tsuid);
            } else {
                DicomObject dataset = null;
                if (dataStream != null) {
                    dataset = dataStream.readDataset();
                    if (log.isDebugEnabled())
                        log.debug("Dataset:\n" + dataset);
                }
                switch (cmdfield) {
                case CommandUtils.C_GET_RQ:
                    getCGetSCP(cmd).cget(as, pcid, cmd, dataset);
                    break;
                case CommandUtils.C_FIND_RQ:
                    getCFindSCP(cmd).cfind(as, pcid, cmd, dataset);
                    break;
                case CommandUtils.C_MOVE_RQ:
                    getCMoveSCP(cmd).cmove(as, pcid, cmd, dataset);
                    break;
                case CommandUtils.C_ECHO_RQ:
                    getCEchoSCP(cmd).cecho(as, pcid, cmd);
                    break;
                case CommandUtils.N_EVENT_REPORT_RQ:
                    getNEventReportSCU(cmd).neventReport(as, pcid, cmd,
                            dataset);
                    break;
                case CommandUtils.N_GET_RQ:
                    getNGetSCP(cmd).nget(as, pcid, cmd, dataset);
                    break;
                case CommandUtils.N_SET_RQ:
                    getNSetSCP(cmd).nset(as, pcid, cmd, dataset);
                    break;
                case CommandUtils.N_ACTION_RQ:
                    getNActionSCP(cmd).naction(as, pcid, cmd, dataset);
                    break;
                case CommandUtils.N_CREATE_RQ:
                    getNCreateSCP(cmd).ncreate(as, pcid, cmd,
                            dataStream != null ? dataset : null);
                    break;
                case CommandUtils.N_DELETE_RQ:
                    getNDeleteSCP(cmd).ndelete(as, pcid, cmd, dataset);
                    break;
                case CommandUtils.C_CANCEL_RQ:
                    as.onCancelRQ(cmd);
                    break;
                default:
                    throw new DicomServiceException(cmd,
                            Status.UnrecognizedOperation);
                }
            }
        } catch (DicomServiceException e) {
            as.writeDimseRSP(pcid, e.getCommand(), e.getDataset());
        }
    }

}
