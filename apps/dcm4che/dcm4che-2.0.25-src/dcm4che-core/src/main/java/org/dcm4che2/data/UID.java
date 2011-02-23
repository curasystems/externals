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

package org.dcm4che2.data;

/** Provides tag constants.*/
public final class UID {

    /** Private constructor */
    private UID() {
        // private to avoid instantiation
    }
    
    public static final String forName(String name) {
       try {
          return (String) UID.class.getField(name).get(null);
       } catch (IllegalAccessException e) {
          throw new Error(e);
       } catch (NoSuchFieldException e) {
          throw new IllegalArgumentException("Unknown UID Name: " + name);
       }
    }

    /** Dcm4che Attributes Modification Notification SOP Class - SOP Class */
    public static final String Dcm4cheAttributesModificationNotificationSOPClass = "1.2.40.0.13.1.3.1.2.3.1.1";

    /** Private Study Root Query/Retrieve Information Model - FIND - SOP Class */
    public static final String PrivateStudyRootQueryRetrieveInformationModelFIND = "1.2.40.0.13.1.5.1.4.1.2.2.1";

    /** Private Blocked Study Root Query/Retrieve Information Model - FIND - SOP Class */
    public static final String PrivateBlockedStudyRootQueryRetrieveInformationModelFIND = "1.2.40.0.13.1.5.1.4.1.2.2.1.1";

    /** Private Virtual Multiframe Study Root Query/Retrieve Information Model - FIND - SOP Class */
    public static final String PrivateVirtualMultiframeStudyRootQueryRetrieveInformationModelFIND = "1.2.40.0.13.1.5.1.4.1.2.2.1.2";

    /** Verification SOP Class - SOP Class */
    public static final String VerificationSOPClass = "1.2.840.10008.1.1";

    /** Implicit VR Little Endian - Transfer Syntax */
    public static final String ImplicitVRLittleEndian = "1.2.840.10008.1.2";

    /** Explicit VR Little Endian - Transfer Syntax */
    public static final String ExplicitVRLittleEndian = "1.2.840.10008.1.2.1";

    /** Deflated Explicit VR Little Endian - Transfer Syntax */
    public static final String DeflatedExplicitVRLittleEndian = "1.2.840.10008.1.2.1.99";

    /** Explicit VR Big Endian - Transfer Syntax */
    public static final String ExplicitVRBigEndian = "1.2.840.10008.1.2.2";

    /** MPEG2 Main Profile @ Main Level - Transfer Syntax */
    public static final String MPEG2 = "1.2.840.10008.1.2.4.100";

    /** MPEG2 Main Profile @ High Level - Transfer Syntax */
    public static final String MPEG2MainProfileHighLevel = "1.2.840.10008.1.2.4.101";

    /** JPEG Baseline (Process 1) - Transfer Syntax */
    public static final String JPEGBaseline1 = "1.2.840.10008.1.2.4.50";

    /** JPEG Extended (Process 2 & 4) - Transfer Syntax */
    public static final String JPEGExtended24 = "1.2.840.10008.1.2.4.51";

    /** JPEG Extended (Process 3 & 5) (Retired) - Transfer Syntax */
    public static final String JPEGExtended35Retired = "1.2.840.10008.1.2.4.52";

    /** JPEG Spectral Selection, Non-Hierarchical (Process 6 & 8) (Retired) - Transfer Syntax */
    public static final String JPEGSpectralSelectionNonHierarchical68Retired = "1.2.840.10008.1.2.4.53";

    /** JPEG Spectral Selection, Non-Hierarchical (Process 7 & 9) (Retired) - Transfer Syntax */
    public static final String JPEGSpectralSelectionNonHierarchical79Retired = "1.2.840.10008.1.2.4.54";

    /** JPEG Full Progression, Non-Hierarchical (Process 10 & 12) (Retired) - Transfer Syntax */
    public static final String JPEGFullProgressionNonHierarchical1012Retired = "1.2.840.10008.1.2.4.55";

    /** JPEG Full Progression, Non-Hierarchical (Process 11 & 13) (Retired) - Transfer Syntax */
    public static final String JPEGFullProgressionNonHierarchical1113Retired = "1.2.840.10008.1.2.4.56";

    /** JPEG Lossless, Non-Hierarchical (Process 14) - Transfer Syntax */
    public static final String JPEGLosslessNonHierarchical14 = "1.2.840.10008.1.2.4.57";

    /** JPEG Lossless, Non-Hierarchical (Process 15) (Retired) - Transfer Syntax */
    public static final String JPEGLosslessNonHierarchical15Retired = "1.2.840.10008.1.2.4.58";

    /** JPEG Extended, Hierarchical (Process 16 & 18) (Retired) - Transfer Syntax */
    public static final String JPEGExtendedHierarchical1618Retired = "1.2.840.10008.1.2.4.59";

    /** JPEG Extended, Hierarchical (Process 17 & 19) (Retired) - Transfer Syntax */
    public static final String JPEGExtendedHierarchical1719Retired = "1.2.840.10008.1.2.4.60";

    /** JPEG Spectral Selection, Hierarchical (Process 20 & 22) (Retired) - Transfer Syntax */
    public static final String JPEGSpectralSelectionHierarchical2022Retired = "1.2.840.10008.1.2.4.61";

    /** JPEG Spectral Selection, Hierarchical (Process 21 & 23) (Retired) - Transfer Syntax */
    public static final String JPEGSpectralSelectionHierarchical2123Retired = "1.2.840.10008.1.2.4.62";

    /** JPEG Full Progression, Hierarchical (Process 24 & 26) (Retired) - Transfer Syntax */
    public static final String JPEGFullProgressionHierarchical2426Retired = "1.2.840.10008.1.2.4.63";

    /** JPEG Full Progression, Hierarchical (Process 25 & 27) (Retired) - Transfer Syntax */
    public static final String JPEGFullProgressionHierarchical2527Retired = "1.2.840.10008.1.2.4.64";

    /** JPEG Lossless, Hierarchical (Process 28) (Retired) - Transfer Syntax */
    public static final String JPEGLosslessHierarchical28Retired = "1.2.840.10008.1.2.4.65";

    /** JPEG Lossless, Hierarchical (Process 29) (Retired) - Transfer Syntax */
    public static final String JPEGLosslessHierarchical29Retired = "1.2.840.10008.1.2.4.66";

    /** JPEG Lossless, Non-Hierarchical, First-Order Prediction (Process 14 [Selection Value 1]) - Transfer Syntax */
    public static final String JPEGLossless = "1.2.840.10008.1.2.4.70";

    /** JPEG-LS Lossless Image Compression - Transfer Syntax */
    public static final String JPEGLSLossless = "1.2.840.10008.1.2.4.80";

    /** JPEG-LS Lossy (Near-Lossless) Image Compression - Transfer Syntax */
    public static final String JPEGLSLossyNearLossless = "1.2.840.10008.1.2.4.81";

    /** JPEG 2000 Image Compression (Lossless Only) - Transfer Syntax */
    public static final String JPEG2000LosslessOnly = "1.2.840.10008.1.2.4.90";

    /** JPEG 2000 Image Compression - Transfer Syntax */
    public static final String JPEG2000 = "1.2.840.10008.1.2.4.91";

    /** JPEG 2000 Part 2 Multi-component Image Compression (Lossless Only) - Transfer Syntax */
    public static final String JPEG2000Part2MulticomponentLosslessOnly = "1.2.840.10008.1.2.4.92";

    /** JPEG 2000 Part 2 Multi-component Image Compression - Transfer Syntax */
    public static final String JPEG2000Part2Multicomponent = "1.2.840.10008.1.2.4.93";

    /** JPIP Referenced - Transfer Syntax */
    public static final String JPIPReferenced = "1.2.840.10008.1.2.4.94";

    /** JPIP Referenced Deflate - Transfer Syntax */
    public static final String JPIPReferencedDeflate = "1.2.840.10008.1.2.4.95";

    /** No Pixel Data - Transfer Syntax */
    public static final String NoPixelData = "1.2.840.10008.1.2.4.96";

    /** No Pixel Data Deflate - Transfer Syntax */
    public static final String NoPixelDataDeflate = "1.2.840.10008.1.2.4.97";

    /** RLE Lossless - Transfer Syntax */
    public static final String RLELossless = "1.2.840.10008.1.2.5";

    /** RFC 2557 MIME encapsulation - Transfer Syntax */
    public static final String RFC2557MIMEencapsulation = "1.2.840.10008.1.2.6.1";

    /** XML Encoding - Transfer Syntax */
    public static final String XMLEncoding = "1.2.840.10008.1.2.6.2";

    /** Storage Commitment Push Model SOP Class - SOP Class */
    public static final String StorageCommitmentPushModelSOPClass = "1.2.840.10008.1.20.1";

    /** Storage Commitment Push Model SOP Instance - Well-known SOP Instance */
    public static final String StorageCommitmentPushModelSOPInstance = "1.2.840.10008.1.20.1.1";

    /** Storage Commitment Pull Model SOP Class (Retired) - SOP Class */
    public static final String StorageCommitmentPullModelSOPClassRetired = "1.2.840.10008.1.20.2";

    /** Storage Commitment Pull Model SOP Instance (Retired) - Well-known SOP Instance */
    public static final String StorageCommitmentPullModelSOPInstanceRetired = "1.2.840.10008.1.20.2.1";

    /** Media Storage Directory Storage - SOP Class */
    public static final String MediaStorageDirectoryStorage = "1.2.840.10008.1.3.10";

    /** Talairach Brain Atlas Frame of Reference - Well-known frame of reference */
    public static final String TalairachBrainAtlasFrameofReference = "1.2.840.10008.1.4.1.1";

    /** SPM2 GRAY Frame of Reference - Well-known frame of reference */
    public static final String SPM2GRAYFrameofReference = "1.2.840.10008.1.4.1.10";

    /** SPM2 WHITE Frame of Reference - Well-known frame of reference */
    public static final String SPM2WHITEFrameofReference = "1.2.840.10008.1.4.1.11";

    /** SPM2 CSF Frame of Reference - Well-known frame of reference */
    public static final String SPM2CSFFrameofReference = "1.2.840.10008.1.4.1.12";

    /** SPM2 BRAINMASK Frame of Reference - Well-known frame of reference */
    public static final String SPM2BRAINMASKFrameofReference = "1.2.840.10008.1.4.1.13";

    /** SPM2 AVG305T1 Frame of Reference - Well-known frame of reference */
    public static final String SPM2AVG305T1FrameofReference = "1.2.840.10008.1.4.1.14";

    /** SPM2 AVG152T1 Frame of Reference - Well-known frame of reference */
    public static final String SPM2AVG152T1FrameofReference = "1.2.840.10008.1.4.1.15";

    /** SPM2 AVG152T2 Frame of Reference - Well-known frame of reference */
    public static final String SPM2AVG152T2FrameofReference = "1.2.840.10008.1.4.1.16";

    /** SPM2 AVG152PD Frame of Reference - Well-known frame of reference */
    public static final String SPM2AVG152PDFrameofReference = "1.2.840.10008.1.4.1.17";

    /** SPM2 SINGLESUBJT1 Frame of Reference - Well-known frame of reference */
    public static final String SPM2SINGLESUBJT1FrameofReference = "1.2.840.10008.1.4.1.18";

    /** SPM2 T1 Frame of Reference - Well-known frame of reference */
    public static final String SPM2T1FrameofReference = "1.2.840.10008.1.4.1.2";

    /** SPM2 T2 Frame of Reference - Well-known frame of reference */
    public static final String SPM2T2FrameofReference = "1.2.840.10008.1.4.1.3";

    /** SPM2 PD Frame of Reference - Well-known frame of reference */
    public static final String SPM2PDFrameofReference = "1.2.840.10008.1.4.1.4";

    /** SPM2 EPI Frame of Reference - Well-known frame of reference */
    public static final String SPM2EPIFrameofReference = "1.2.840.10008.1.4.1.5";

    /** SPM2 FIL T1 Frame of Reference - Well-known frame of reference */
    public static final String SPM2FILT1FrameofReference = "1.2.840.10008.1.4.1.6";

    /** SPM2 PET Frame of Reference - Well-known frame of reference */
    public static final String SPM2PETFrameofReference = "1.2.840.10008.1.4.1.7";

    /** SPM2 TRANSM Frame of Reference - Well-known frame of reference */
    public static final String SPM2TRANSMFrameofReference = "1.2.840.10008.1.4.1.8";

    /** SPM2 SPECT Frame of Reference - Well-known frame of reference */
    public static final String SPM2SPECTFrameofReference = "1.2.840.10008.1.4.1.9";

    /** ICBM 452 T1 Frame of Reference - Well-known frame of reference */
    public static final String ICBM452T1FrameofReference = "1.2.840.10008.1.4.2.1";

    /** ICBM Single Subject MRI Frame of Reference - Well-known frame of reference */
    public static final String ICBMSingleSubjectMRIFrameofReference = "1.2.840.10008.1.4.2.2";

    /** Procedural Event Logging SOP Class - SOP Class */
    public static final String ProceduralEventLoggingSOPClass = "1.2.840.10008.1.40";

    /** Procedural Event Logging SOP Instance - Well-known SOP Instance */
    public static final String ProceduralEventLoggingSOPInstance = "1.2.840.10008.1.40.1";

    /** Substance Administration Logging SOP Class - SOP Class */
    public static final String SubstanceAdministrationLoggingSOPClass = "1.2.840.10008.1.42";

    /** Substance Administration Logging SOP Instance - Well-known SOP Instance */
    public static final String SubstanceAdministrationLoggingSOPInstance = "1.2.840.10008.1.42.1";

    /** Hot Iron Color Palette SOP Instance - Well-known SOP Instance */
    public static final String HotIronColorPaletteSOPInstance = "1.2.840.10008.1.5.1";

    /** PET Color Palette SOP Instance - Well-known SOP Instance */
    public static final String PETColorPaletteSOPInstance = "1.2.840.10008.1.5.2";

    /** Hot Metal Blue Color Palette SOP Instance - Well-known SOP Instance */
    public static final String HotMetalBlueColorPaletteSOPInstance = "1.2.840.10008.1.5.3";

    /** PET 20 Step Color Palette SOP Instance - Well-known SOP Instance */
    public static final String PET20StepColorPaletteSOPInstance = "1.2.840.10008.1.5.4";

    /** Basic Study Content Notification SOP Class (Retired) - SOP Class */
    public static final String BasicStudyContentNotificationSOPClassRetired = "1.2.840.10008.1.9";

    /** dicomDeviceName - LDAP OID */
    public static final String dicomDeviceName = "1.2.840.10008.15.0.3.1";

    /** dicomAssociationInitiator - LDAP OID */
    public static final String dicomAssociationInitiator = "1.2.840.10008.15.0.3.10";

    /** dicomAssociationAcceptor - LDAP OID */
    public static final String dicomAssociationAcceptor = "1.2.840.10008.15.0.3.11";

    /** dicomHostname - LDAP OID */
    public static final String dicomHostname = "1.2.840.10008.15.0.3.12";

    /** dicomPort - LDAP OID */
    public static final String dicomPort = "1.2.840.10008.15.0.3.13";

    /** dicomSOPClass - LDAP OID */
    public static final String dicomSOPClass = "1.2.840.10008.15.0.3.14";

    /** dicomTransferRole - LDAP OID */
    public static final String dicomTransferRole = "1.2.840.10008.15.0.3.15";

    /** dicomTransferSyntax - LDAP OID */
    public static final String dicomTransferSyntax = "1.2.840.10008.15.0.3.16";

    /** dicomPrimaryDeviceType - LDAP OID */
    public static final String dicomPrimaryDeviceType = "1.2.840.10008.15.0.3.17";

    /** dicomRelatedDeviceReference - LDAP OID */
    public static final String dicomRelatedDeviceReference = "1.2.840.10008.15.0.3.18";

    /** dicomPreferredCalledAETitle - LDAP OID */
    public static final String dicomPreferredCalledAETitle = "1.2.840.10008.15.0.3.19";

    /** dicomDescription - LDAP OID */
    public static final String dicomDescription = "1.2.840.10008.15.0.3.2";

    /** dicomTLSCyphersuite - LDAP OID */
    public static final String dicomTLSCyphersuite = "1.2.840.10008.15.0.3.20";

    /** dicomAuthorizedNodeCertificateReference - LDAP OID */
    public static final String dicomAuthorizedNodeCertificateReference = "1.2.840.10008.15.0.3.21";

    /** dicomThisNodeCertificateReference - LDAP OID */
    public static final String dicomThisNodeCertificateReference = "1.2.840.10008.15.0.3.22";

    /** dicomInstalled - LDAP OID */
    public static final String dicomInstalled = "1.2.840.10008.15.0.3.23";

    /** dicomStationName - LDAP OID */
    public static final String dicomStationName = "1.2.840.10008.15.0.3.24";

    /** dicomDeviceSerialNumber - LDAP OID */
    public static final String dicomDeviceSerialNumber = "1.2.840.10008.15.0.3.25";

    /** dicomInstitutionName - LDAP OID */
    public static final String dicomInstitutionName = "1.2.840.10008.15.0.3.26";

    /** dicomInstitutionAddress - LDAP OID */
    public static final String dicomInstitutionAddress = "1.2.840.10008.15.0.3.27";

    /** dicomInstitutionDepartmentName - LDAP OID */
    public static final String dicomInstitutionDepartmentName = "1.2.840.10008.15.0.3.28";

    /** dicomIssuerOfPatientID - LDAP OID */
    public static final String dicomIssuerOfPatientID = "1.2.840.10008.15.0.3.29";

    /** dicomManufacturer - LDAP OID */
    public static final String dicomManufacturer = "1.2.840.10008.15.0.3.3";

    /** dicomPreferredCallingAETitle - LDAP OID */
    public static final String dicomPreferredCallingAETitle = "1.2.840.10008.15.0.3.30";

    /** dicomSupportedCharacterSet - LDAP OID */
    public static final String dicomSupportedCharacterSet = "1.2.840.10008.15.0.3.31";

    /** dicomManufacturerModelName - LDAP OID */
    public static final String dicomManufacturerModelName = "1.2.840.10008.15.0.3.4";

    /** dicomSoftwareVersion - LDAP OID */
    public static final String dicomSoftwareVersion = "1.2.840.10008.15.0.3.5";

    /** dicomVendorData - LDAP OID */
    public static final String dicomVendorData = "1.2.840.10008.15.0.3.6";

    /** dicomAETitle - LDAP OID */
    public static final String dicomAETitle = "1.2.840.10008.15.0.3.7";

    /** dicomNetworkConnectionReference - LDAP OID */
    public static final String dicomNetworkConnectionReference = "1.2.840.10008.15.0.3.8";

    /** dicomApplicationCluster - LDAP OID */
    public static final String dicomApplicationCluster = "1.2.840.10008.15.0.3.9";

    /** dicomConfigurationRoot - LDAP OID */
    public static final String dicomConfigurationRoot = "1.2.840.10008.15.0.4.1";

    /** dicomDevicesRoot - LDAP OID */
    public static final String dicomDevicesRoot = "1.2.840.10008.15.0.4.2";

    /** dicomUniqueAETitlesRegistryRoot - LDAP OID */
    public static final String dicomUniqueAETitlesRegistryRoot = "1.2.840.10008.15.0.4.3";

    /** dicomDevice - LDAP OID */
    public static final String dicomDevice = "1.2.840.10008.15.0.4.4";

    /** dicomNetworkAE - LDAP OID */
    public static final String dicomNetworkAE = "1.2.840.10008.15.0.4.5";

    /** dicomNetworkConnection - LDAP OID */
    public static final String dicomNetworkConnection = "1.2.840.10008.15.0.4.6";

    /** dicomUniqueAETitle - LDAP OID */
    public static final String dicomUniqueAETitle = "1.2.840.10008.15.0.4.7";

    /** dicomTransferCapability - LDAP OID */
    public static final String dicomTransferCapability = "1.2.840.10008.15.0.4.8";

    /** DICOM Controlled Terminology - Coding Scheme */
    public static final String DICOMControlledTerminology = "1.2.840.10008.2.16.4";

    /** DICOM UID Registry - DICOM UIDs as a Coding Scheme */
    public static final String DICOMUIDRegistry = "1.2.840.10008.2.6.1";

    /** DICOM Application Context Name - Application Context Name */
    public static final String DICOMApplicationContextName = "1.2.840.10008.3.1.1.1";

    /** Detached Patient Management SOP Class (Retired) - SOP Class */
    public static final String DetachedPatientManagementSOPClassRetired = "1.2.840.10008.3.1.2.1.1";

    /** Detached Patient Management Meta SOP Class (Retired) - Meta SOP Class */
    public static final String DetachedPatientManagementMetaSOPClassRetired = "1.2.840.10008.3.1.2.1.4";

    /** Detached Visit Management SOP Class (Retired) - SOP Class */
    public static final String DetachedVisitManagementSOPClassRetired = "1.2.840.10008.3.1.2.2.1";

    /** Detached Study Management SOP Class (Retired) - SOP Class */
    public static final String DetachedStudyManagementSOPClassRetired = "1.2.840.10008.3.1.2.3.1";

    /** Study Component Management SOP Class (Retired) - SOP Class */
    public static final String StudyComponentManagementSOPClassRetired = "1.2.840.10008.3.1.2.3.2";

    /** Modality Performed Procedure Step SOP Class - SOP Class */
    public static final String ModalityPerformedProcedureStepSOPClass = "1.2.840.10008.3.1.2.3.3";

    /** Modality Performed Procedure Step Retrieve SOP Class - SOP Class */
    public static final String ModalityPerformedProcedureStepRetrieveSOPClass = "1.2.840.10008.3.1.2.3.4";

    /** Modality Performed Procedure Step Notification SOP Class - SOP Class */
    public static final String ModalityPerformedProcedureStepNotificationSOPClass = "1.2.840.10008.3.1.2.3.5";

    /** Detached Results Management SOP Class (Retired) - SOP Class */
    public static final String DetachedResultsManagementSOPClassRetired = "1.2.840.10008.3.1.2.5.1";

    /** Detached Results Management Meta SOP Class (Retired) - Meta SOP Class */
    public static final String DetachedResultsManagementMetaSOPClassRetired = "1.2.840.10008.3.1.2.5.4";

    /** Detached Study Management Meta SOP Class (Retired) - Meta SOP Class */
    public static final String DetachedStudyManagementMetaSOPClassRetired = "1.2.840.10008.3.1.2.5.5";

    /** Detached Interpretation Management SOP Class (Retired) - SOP Class */
    public static final String DetachedInterpretationManagementSOPClassRetired = "1.2.840.10008.3.1.2.6.1";

    /** Storage Service Class - Service Class */
    public static final String StorageServiceClass = "1.2.840.10008.4.2";

    /** Basic Film Session SOP Class - SOP Class */
    public static final String BasicFilmSessionSOPClass = "1.2.840.10008.5.1.1.1";

    /** Print Job SOP Class - SOP Class */
    public static final String PrintJobSOPClass = "1.2.840.10008.5.1.1.14";

    /** Basic Annotation Box SOP Class - SOP Class */
    public static final String BasicAnnotationBoxSOPClass = "1.2.840.10008.5.1.1.15";

    /** Printer SOP Class - SOP Class */
    public static final String PrinterSOPClass = "1.2.840.10008.5.1.1.16";

    /** Printer Configuration Retrieval SOP Class - SOP Class */
    public static final String PrinterConfigurationRetrievalSOPClass = "1.2.840.10008.5.1.1.16.376";

    /** Printer SOP Instance - Well-known Printer SOP Instance */
    public static final String PrinterSOPInstance = "1.2.840.10008.5.1.1.17";

    /** Printer Configuration Retrieval SOP Instance - Well-known Printer SOP Instance */
    public static final String PrinterConfigurationRetrievalSOPInstance = "1.2.840.10008.5.1.1.17.376";

    /** Basic Color Print Management Meta SOP Class - Meta SOP Class */
    public static final String BasicColorPrintManagementMetaSOPClass = "1.2.840.10008.5.1.1.18";

    /** Referenced Color Print Management Meta SOP Class (Retired) - Meta SOP Class */
    public static final String ReferencedColorPrintManagementMetaSOPClassRetired = "1.2.840.10008.5.1.1.18.1";

    /** Basic Film Box SOP Class - SOP Class */
    public static final String BasicFilmBoxSOPClass = "1.2.840.10008.5.1.1.2";

    /** VOI LUT Box SOP Class - SOP Class */
    public static final String VOILUTBoxSOPClass = "1.2.840.10008.5.1.1.22";

    /** Presentation LUT SOP Class - SOP Class */
    public static final String PresentationLUTSOPClass = "1.2.840.10008.5.1.1.23";

    /** Image Overlay Box SOP Class (Retired) - SOP Class */
    public static final String ImageOverlayBoxSOPClassRetired = "1.2.840.10008.5.1.1.24";

    /** Basic Print Image Overlay Box SOP Class (Retired) - SOP Class */
    public static final String BasicPrintImageOverlayBoxSOPClassRetired = "1.2.840.10008.5.1.1.24.1";

    /** Print Queue SOP Instance (Retired) - Well-known Print Queue SOP Instance */
    public static final String PrintQueueSOPInstanceRetired = "1.2.840.10008.5.1.1.25";

    /** Print Queue Management SOP Class (Retired) - SOP Class */
    public static final String PrintQueueManagementSOPClassRetired = "1.2.840.10008.5.1.1.26";

    /** Stored Print Storage SOP Class (Retired) - SOP Class */
    public static final String StoredPrintStorageSOPClassRetired = "1.2.840.10008.5.1.1.27";

    /** Hardcopy Grayscale Image Storage SOP Class (Retired) - SOP Class */
    public static final String HardcopyGrayscaleImageStorageSOPClassRetired = "1.2.840.10008.5.1.1.29";

    /** Hardcopy Color Image Storage SOP Class (Retired) - SOP Class */
    public static final String HardcopyColorImageStorageSOPClassRetired = "1.2.840.10008.5.1.1.30";

    /** Pull Print Request SOP Class (Retired) - SOP Class */
    public static final String PullPrintRequestSOPClassRetired = "1.2.840.10008.5.1.1.31";

    /** Pull Stored Print Management Meta SOP Class (Retired) - Meta SOP Class */
    public static final String PullStoredPrintManagementMetaSOPClassRetired = "1.2.840.10008.5.1.1.32";

    /** Media Creation Management SOP Class UID - SOP Class */
    public static final String MediaCreationManagementSOPClassUID = "1.2.840.10008.5.1.1.33";

    /** Basic Grayscale Image Box SOP Class - SOP Class */
    public static final String BasicGrayscaleImageBoxSOPClass = "1.2.840.10008.5.1.1.4";

    /** Basic Color Image Box SOP Class - SOP Class */
    public static final String BasicColorImageBoxSOPClass = "1.2.840.10008.5.1.1.4.1";

    /** Referenced Image Box SOP Class (Retired) - SOP Class */
    public static final String ReferencedImageBoxSOPClassRetired = "1.2.840.10008.5.1.1.4.2";

    /** Basic Grayscale Print Management Meta SOP Class - Meta SOP Class */
    public static final String BasicGrayscalePrintManagementMetaSOPClass = "1.2.840.10008.5.1.1.9";

    /** Referenced Grayscale Print Management Meta SOP Class (Retired) - Meta SOP Class */
    public static final String ReferencedGrayscalePrintManagementMetaSOPClassRetired = "1.2.840.10008.5.1.1.9.1";

    /** Computed Radiography Image Storage - SOP Class */
    public static final String ComputedRadiographyImageStorage = "1.2.840.10008.5.1.4.1.1.1";

    /** Digital X-Ray Image Storage - For Presentation - SOP Class */
    public static final String DigitalXRayImageStorageForPresentation = "1.2.840.10008.5.1.4.1.1.1.1";

    /** Digital X-Ray Image Storage - For Processing - SOP Class */
    public static final String DigitalXRayImageStorageForProcessing = "1.2.840.10008.5.1.4.1.1.1.1.1";

    /** Digital Mammography X-Ray Image Storage - For Presentation - SOP Class */
    public static final String DigitalMammographyXRayImageStorageForPresentation = "1.2.840.10008.5.1.4.1.1.1.2";

    /** Digital Mammography X-Ray Image Storage - For Processing - SOP Class */
    public static final String DigitalMammographyXRayImageStorageForProcessing = "1.2.840.10008.5.1.4.1.1.1.2.1";

    /** Digital Intra-oral X-Ray Image Storage - For Presentation - SOP Class */
    public static final String DigitalIntraoralXRayImageStorageForPresentation = "1.2.840.10008.5.1.4.1.1.1.3";

    /** Digital Intra-oral X-Ray Image Storage - For Processing - SOP Class */
    public static final String DigitalIntraoralXRayImageStorageForProcessing = "1.2.840.10008.5.1.4.1.1.1.3.1";

    /** Standalone Modality LUT Storage (Retired) - SOP Class */
    public static final String StandaloneModalityLUTStorageRetired = "1.2.840.10008.5.1.4.1.1.10";

    /** Encapsulated PDF Storage - SOP Class */
    public static final String EncapsulatedPDFStorage = "1.2.840.10008.5.1.4.1.1.104.1";

    /** Encapsulated CDA Storage - SOP Class */
    public static final String EncapsulatedCDAStorage = "1.2.840.10008.5.1.4.1.1.104.2";

    /** Standalone VOI LUT Storage (Retired) - SOP Class */
    public static final String StandaloneVOILUTStorageRetired = "1.2.840.10008.5.1.4.1.1.11";

    /** Grayscale Softcopy Presentation State Storage SOP Class - SOP Class */
    public static final String GrayscaleSoftcopyPresentationStateStorageSOPClass = "1.2.840.10008.5.1.4.1.1.11.1";

    /** Color Softcopy Presentation State Storage SOP Class - SOP Class */
    public static final String ColorSoftcopyPresentationStateStorageSOPClass = "1.2.840.10008.5.1.4.1.1.11.2";

    /** Pseudo-Color Softcopy Presentation State Storage SOP Class - SOP Class */
    public static final String PseudoColorSoftcopyPresentationStateStorageSOPClass = "1.2.840.10008.5.1.4.1.1.11.3";

    /** Blending Softcopy Presentation State Storage SOP Class - SOP Class */
    public static final String BlendingSoftcopyPresentationStateStorageSOPClass = "1.2.840.10008.5.1.4.1.1.11.4";

    /** XA/XRF Grayscale Softcopy Presentation State Storage - SOP Class */
    public static final String XAXRFGrayscaleSoftcopyPresentationStateStorage = "1.2.840.10008.5.1.4.1.1.11.5";

    /** X-Ray Angiographic Image Storage - SOP Class */
    public static final String XRayAngiographicImageStorage = "1.2.840.10008.5.1.4.1.1.12.1";

    /** Enhanced XA Image Storage - SOP Class */
    public static final String EnhancedXAImageStorage = "1.2.840.10008.5.1.4.1.1.12.1.1";

    /** X-Ray Radiofluoroscopic Image Storage - SOP Class */
    public static final String XRayRadiofluoroscopicImageStorage = "1.2.840.10008.5.1.4.1.1.12.2";

    /** Enhanced XRF Image Storage - SOP Class */
    public static final String EnhancedXRFImageStorage = "1.2.840.10008.5.1.4.1.1.12.2.1";

    /** X-Ray Angiographic Bi-Plane Image Storage (Retired) - SOP Class */
    public static final String XRayAngiographicBiPlaneImageStorageRetired = "1.2.840.10008.5.1.4.1.1.12.3";

    /** Positron Emission Tomography Image Storage - SOP Class */
    public static final String PositronEmissionTomographyImageStorage = "1.2.840.10008.5.1.4.1.1.128";

    /** Standalone PET Curve Storage (Retired) - SOP Class */
    public static final String StandalonePETCurveStorageRetired = "1.2.840.10008.5.1.4.1.1.129";

    /** X-Ray 3D Angiographic Image Storage - SOP Class */
    public static final String XRay3DAngiographicImageStorage = "1.2.840.10008.5.1.4.1.1.13.1.1";

    /** X-Ray 3D Craniofacial Image Storage - SOP Class */
    public static final String XRay3DCraniofacialImageStorage = "1.2.840.10008.5.1.4.1.1.13.1.2";

    /** Breast Tomosynthesis Image Storage - SOP Class */
    public static final String BreastTomosynthesisImageStorage = "1.2.840.10008.5.1.4.1.1.13.1.3";

    /** Enhanced PET Image Storage - SOP Class */
    public static final String EnhancedPETImageStorage = "1.2.840.10008.5.1.4.1.1.130";

    /** Basic Structured Display Storage - SOP Class */
    public static final String BasicStructuredDisplayStorage = "1.2.840.10008.5.1.4.1.1.131";

    /** CT Image Storage - SOP Class */
    public static final String CTImageStorage = "1.2.840.10008.5.1.4.1.1.2";

    /** Enhanced CT Image Storage - SOP Class */
    public static final String EnhancedCTImageStorage = "1.2.840.10008.5.1.4.1.1.2.1";

    /** Nuclear Medicine Image Storage - SOP Class */
    public static final String NuclearMedicineImageStorage = "1.2.840.10008.5.1.4.1.1.20";

    /** Ultrasound Multi-frame Image Storage (Retired) - SOP Class */
    public static final String UltrasoundMultiframeImageStorageRetired = "1.2.840.10008.5.1.4.1.1.3";

    /** Ultrasound Multi-frame Image Storage - SOP Class */
    public static final String UltrasoundMultiframeImageStorage = "1.2.840.10008.5.1.4.1.1.3.1";

    /** MR Image Storage - SOP Class */
    public static final String MRImageStorage = "1.2.840.10008.5.1.4.1.1.4";

    /** Enhanced MR Image Storage - SOP Class */
    public static final String EnhancedMRImageStorage = "1.2.840.10008.5.1.4.1.1.4.1";

    /** MR Spectroscopy Storage - SOP Class */
    public static final String MRSpectroscopyStorage = "1.2.840.10008.5.1.4.1.1.4.2";

    /** Enhanced MR Color Image Storage - SOP Class */
    public static final String EnhancedMRColorImageStorage = "1.2.840.10008.5.1.4.1.1.4.3";

    /** RT Image Storage - SOP Class */
    public static final String RTImageStorage = "1.2.840.10008.5.1.4.1.1.481.1";

    /** RT Dose Storage - SOP Class */
    public static final String RTDoseStorage = "1.2.840.10008.5.1.4.1.1.481.2";

    /** RT Structure Set Storage - SOP Class */
    public static final String RTStructureSetStorage = "1.2.840.10008.5.1.4.1.1.481.3";

    /** RT Beams Treatment Record Storage - SOP Class */
    public static final String RTBeamsTreatmentRecordStorage = "1.2.840.10008.5.1.4.1.1.481.4";

    /** RT Plan Storage - SOP Class */
    public static final String RTPlanStorage = "1.2.840.10008.5.1.4.1.1.481.5";

    /** RT Brachy Treatment Record Storage - SOP Class */
    public static final String RTBrachyTreatmentRecordStorage = "1.2.840.10008.5.1.4.1.1.481.6";

    /** RT Treatment Summary Record Storage - SOP Class */
    public static final String RTTreatmentSummaryRecordStorage = "1.2.840.10008.5.1.4.1.1.481.7";

    /** RT Ion Plan Storage - SOP Class */
    public static final String RTIonPlanStorage = "1.2.840.10008.5.1.4.1.1.481.8";

    /** RT Ion Beams Treatment Record Storage - SOP Class */
    public static final String RTIonBeamsTreatmentRecordStorage = "1.2.840.10008.5.1.4.1.1.481.9";

    /** Nuclear Medicine Image Storage (Retired) - SOP Class */
    public static final String NuclearMedicineImageStorageRetired = "1.2.840.10008.5.1.4.1.1.5";

    /** Ultrasound Image Storage (Retired) - SOP Class */
    public static final String UltrasoundImageStorageRetired = "1.2.840.10008.5.1.4.1.1.6";

    /** Ultrasound Image Storage - SOP Class */
    public static final String UltrasoundImageStorage = "1.2.840.10008.5.1.4.1.1.6.1";

    /** Enhanced US Volume Storage - SOP Class */
    public static final String EnhancedUSVolumeStorage = "1.2.840.10008.5.1.4.1.1.6.2";

    /** Raw Data Storage - SOP Class */
    public static final String RawDataStorage = "1.2.840.10008.5.1.4.1.1.66";

    /** Spatial Registration Storage - SOP Class */
    public static final String SpatialRegistrationStorage = "1.2.840.10008.5.1.4.1.1.66.1";

    /** Spatial Fiducials Storage - SOP Class */
    public static final String SpatialFiducialsStorage = "1.2.840.10008.5.1.4.1.1.66.2";

    /** Deformable Spatial Registration Storage - SOP Class */
    public static final String DeformableSpatialRegistrationStorage = "1.2.840.10008.5.1.4.1.1.66.3";

    /** Segmentation Storage - SOP Class */
    public static final String SegmentationStorage = "1.2.840.10008.5.1.4.1.1.66.4";

    /** Surface Segmentation Storage - SOP Class */
    public static final String SurfaceSegmentationStorage = "1.2.840.10008.5.1.4.1.1.66.5";

    /** Real World Value Mapping Storage - SOP Class */
    public static final String RealWorldValueMappingStorage = "1.2.840.10008.5.1.4.1.1.67";

    /** Secondary Capture Image Storage - SOP Class */
    public static final String SecondaryCaptureImageStorage = "1.2.840.10008.5.1.4.1.1.7";

    /** Multi-frame Single Bit Secondary Capture Image Storage - SOP Class */
    public static final String MultiframeSingleBitSecondaryCaptureImageStorage = "1.2.840.10008.5.1.4.1.1.7.1";

    /** Multi-frame Grayscale Byte Secondary Capture Image Storage - SOP Class */
    public static final String MultiframeGrayscaleByteSecondaryCaptureImageStorage = "1.2.840.10008.5.1.4.1.1.7.2";

    /** Multi-frame Grayscale Word Secondary Capture Image Storage - SOP Class */
    public static final String MultiframeGrayscaleWordSecondaryCaptureImageStorage = "1.2.840.10008.5.1.4.1.1.7.3";

    /** Multi-frame True Color Secondary Capture Image Storage - SOP Class */
    public static final String MultiframeTrueColorSecondaryCaptureImageStorage = "1.2.840.10008.5.1.4.1.1.7.4";

    /** VL Image Storage - Trial (Retired) -  */
    public static final String VLImageStorageTrialRetired = "1.2.840.10008.5.1.4.1.1.77.1";

    /** VL Endoscopic Image Storage - SOP Class */
    public static final String VLEndoscopicImageStorage = "1.2.840.10008.5.1.4.1.1.77.1.1";

    /** Video Endoscopic Image Storage - SOP Class */
    public static final String VideoEndoscopicImageStorage = "1.2.840.10008.5.1.4.1.1.77.1.1.1";

    /** VL Microscopic Image Storage - SOP Class */
    public static final String VLMicroscopicImageStorage = "1.2.840.10008.5.1.4.1.1.77.1.2";

    /** Video Microscopic Image Storage - SOP Class */
    public static final String VideoMicroscopicImageStorage = "1.2.840.10008.5.1.4.1.1.77.1.2.1";

    /** VL Slide-Coordinates Microscopic Image Storage - SOP Class */
    public static final String VLSlideCoordinatesMicroscopicImageStorage = "1.2.840.10008.5.1.4.1.1.77.1.3";

    /** VL Photographic Image Storage - SOP Class */
    public static final String VLPhotographicImageStorage = "1.2.840.10008.5.1.4.1.1.77.1.4";

    /** Video Photographic Image Storage - SOP Class */
    public static final String VideoPhotographicImageStorage = "1.2.840.10008.5.1.4.1.1.77.1.4.1";

    /** Ophthalmic Photography 8 Bit Image Storage - SOP Class */
    public static final String OphthalmicPhotography8BitImageStorage = "1.2.840.10008.5.1.4.1.1.77.1.5.1";

    /** Ophthalmic Photography 16 Bit Image Storage - SOP Class */
    public static final String OphthalmicPhotography16BitImageStorage = "1.2.840.10008.5.1.4.1.1.77.1.5.2";

    /** Stereometric Relationship Storage - SOP Class */
    public static final String StereometricRelationshipStorage = "1.2.840.10008.5.1.4.1.1.77.1.5.3";

    /** Ophthalmic Tomography Image Storage - SOP Class */
    public static final String OphthalmicTomographyImageStorage = "1.2.840.10008.5.1.4.1.1.77.1.5.4";

    /** VL Multi-frame Image Storage - Trial (Retired) -  */
    public static final String VLMultiframeImageStorageTrialRetired = "1.2.840.10008.5.1.4.1.1.77.2";

    /** Lensometry Measurements Storage - SOP Class */
    public static final String LensometryMeasurementsStorage = "1.2.840.10008.5.1.4.1.1.78.1";

    /** Autorefraction Measurements Storage - SOP Class */
    public static final String AutorefractionMeasurementsStorage = "1.2.840.10008.5.1.4.1.1.78.2";

    /** Keratometry Measurements Storage - SOP Class */
    public static final String KeratometryMeasurementsStorage = "1.2.840.10008.5.1.4.1.1.78.3";

    /** Subjective Refraction Measurements Storage - SOP Class */
    public static final String SubjectiveRefractionMeasurementsStorage = "1.2.840.10008.5.1.4.1.1.78.4";

    /** Visual Acuity Measurements - SOP Class */
    public static final String VisualAcuityMeasurements = "1.2.840.10008.5.1.4.1.1.78.5";

    /** Spectacle Prescription Reports Storage - SOP Class */
    public static final String SpectaclePrescriptionReportsStorage = "1.2.840.10008.5.1.4.1.1.78.6";

    /** Macular Grid Thickness and Volume Report Storage - SOP Class */
    public static final String MacularGridThicknessandVolumeReportStorage = "1.2.840.10008.5.1.4.1.1.79.1";

    /** Standalone Overlay Storage (Retired) - SOP Class */
    public static final String StandaloneOverlayStorageRetired = "1.2.840.10008.5.1.4.1.1.8";

    /** Text SR Storage - Trial (Retired) - SOP Class */
    public static final String TextSRStorageTrialRetired = "1.2.840.10008.5.1.4.1.1.88.1";

    /** Basic Text SR Storage - SOP Class */
    public static final String BasicTextSRStorage = "1.2.840.10008.5.1.4.1.1.88.11";

    /** Audio SR Storage - Trial (Retired) - SOP Class */
    public static final String AudioSRStorageTrialRetired = "1.2.840.10008.5.1.4.1.1.88.2";

    /** Enhanced SR Storage - SOP Class */
    public static final String EnhancedSRStorage = "1.2.840.10008.5.1.4.1.1.88.22";

    /** Detail SR Storage - Trial (Retired) - SOP Class */
    public static final String DetailSRStorageTrialRetired = "1.2.840.10008.5.1.4.1.1.88.3";

    /** Comprehensive SR Storage - SOP Class */
    public static final String ComprehensiveSRStorage = "1.2.840.10008.5.1.4.1.1.88.33";

    /** Comprehensive SR Storage - Trial (Retired) - SOP Class */
    public static final String ComprehensiveSRStorageTrialRetired = "1.2.840.10008.5.1.4.1.1.88.4";

    /** Procedure Log Storage - SOP Class */
    public static final String ProcedureLogStorage = "1.2.840.10008.5.1.4.1.1.88.40";

    /** Mammography CAD SR Storage - SOP Class */
    public static final String MammographyCADSRStorage = "1.2.840.10008.5.1.4.1.1.88.50";

    /** Key Object Selection Document Storage - SOP Class */
    public static final String KeyObjectSelectionDocumentStorage = "1.2.840.10008.5.1.4.1.1.88.59";

    /** Chest CAD SR Storage - SOP Class */
    public static final String ChestCADSRStorage = "1.2.840.10008.5.1.4.1.1.88.65";

    /** X-Ray Radiation Dose SR Storage - SOP Class */
    public static final String XRayRadiationDoseSRStorage = "1.2.840.10008.5.1.4.1.1.88.67";

    /** Colon CAD SR Storage - SOP Class */
    public static final String ColonCADSRStorage = "1.2.840.10008.5.1.4.1.1.88.69";

    /** Standalone Curve Storage (Retired) - SOP Class */
    public static final String StandaloneCurveStorageRetired = "1.2.840.10008.5.1.4.1.1.9";

    /** Waveform Storage - Trial (Retired) - SOP Class */
    public static final String WaveformStorageTrialRetired = "1.2.840.10008.5.1.4.1.1.9.1";

    /** 12-lead ECG Waveform Storage - SOP Class */
    public static final String _12leadECGWaveformStorage = "1.2.840.10008.5.1.4.1.1.9.1.1";

    /** General ECG Waveform Storage - SOP Class */
    public static final String GeneralECGWaveformStorage = "1.2.840.10008.5.1.4.1.1.9.1.2";

    /** Ambulatory ECG Waveform Storage - SOP Class */
    public static final String AmbulatoryECGWaveformStorage = "1.2.840.10008.5.1.4.1.1.9.1.3";

    /** Hemodynamic Waveform Storage - SOP Class */
    public static final String HemodynamicWaveformStorage = "1.2.840.10008.5.1.4.1.1.9.2.1";

    /** Cardiac Electrophysiology Waveform Storage - SOP Class */
    public static final String CardiacElectrophysiologyWaveformStorage = "1.2.840.10008.5.1.4.1.1.9.3.1";

    /** Basic Voice Audio Waveform Storage - SOP Class */
    public static final String BasicVoiceAudioWaveformStorage = "1.2.840.10008.5.1.4.1.1.9.4.1";

    /** General Audio Waveform Storage - SOP Class */
    public static final String GeneralAudioWaveformStorage = "1.2.840.10008.5.1.4.1.1.9.4.2";

    /** Arterial Pulse Waveform Storage - SOP Class */
    public static final String ArterialPulseWaveformStorage = "1.2.840.10008.5.1.4.1.1.9.5.1";

    /** Respiratory Waveform Storage - SOP Class */
    public static final String RespiratoryWaveformStorage = "1.2.840.10008.5.1.4.1.1.9.6.1";

    /** Patient Root Query/Retrieve Information Model - FIND - SOP Class */
    public static final String PatientRootQueryRetrieveInformationModelFIND = "1.2.840.10008.5.1.4.1.2.1.1";

    /** Patient Root Query/Retrieve Information Model - MOVE - SOP Class */
    public static final String PatientRootQueryRetrieveInformationModelMOVE = "1.2.840.10008.5.1.4.1.2.1.2";

    /** Patient Root Query/Retrieve Information Model - GET - SOP Class */
    public static final String PatientRootQueryRetrieveInformationModelGET = "1.2.840.10008.5.1.4.1.2.1.3";

    /** Study Root Query/Retrieve Information Model - FIND - SOP Class */
    public static final String StudyRootQueryRetrieveInformationModelFIND = "1.2.840.10008.5.1.4.1.2.2.1";

    /** Study Root Query/Retrieve Information Model - MOVE - SOP Class */
    public static final String StudyRootQueryRetrieveInformationModelMOVE = "1.2.840.10008.5.1.4.1.2.2.2";

    /** Study Root Query/Retrieve Information Model - GET - SOP Class */
    public static final String StudyRootQueryRetrieveInformationModelGET = "1.2.840.10008.5.1.4.1.2.2.3";

    /** Patient/Study Only Query/Retrieve Information Model - FIND (Retired) - SOP Class */
    public static final String PatientStudyOnlyQueryRetrieveInformationModelFINDRetired = "1.2.840.10008.5.1.4.1.2.3.1";

    /** Patient/Study Only Query/Retrieve Information Model - MOVE (Retired) - SOP Class */
    public static final String PatientStudyOnlyQueryRetrieveInformationModelMOVERetired = "1.2.840.10008.5.1.4.1.2.3.2";

    /** Patient/Study Only Query/Retrieve Information Model - GET (Retired) - SOP Class */
    public static final String PatientStudyOnlyQueryRetrieveInformationModelGETRetired = "1.2.840.10008.5.1.4.1.2.3.3";

    /** Composite Instance Root Retrieve - MOVE - SOP Class */
    public static final String CompositeInstanceRootRetrieveMOVE = "1.2.840.10008.5.1.4.1.2.4.2";

    /** Composite Instance Root Retrieve - GET - SOP Class */
    public static final String CompositeInstanceRootRetrieveGET = "1.2.840.10008.5.1.4.1.2.4.3";

    /** Composite Instance Retrieve Without Bulk Data - GET - SOP Class */
    public static final String CompositeInstanceRetrieveWithoutBulkDataGET = "1.2.840.10008.5.1.4.1.2.5.3";

    /** Modality Worklist Information Model - FIND - SOP Class */
    public static final String ModalityWorklistInformationModelFIND = "1.2.840.10008.5.1.4.31";

    /** General Purpose Worklist Management Meta SOP Class - Meta SOP Class */
    public static final String GeneralPurposeWorklistManagementMetaSOPClass = "1.2.840.10008.5.1.4.32";

    /** General Purpose Worklist Information Model - FIND - SOP Class */
    public static final String GeneralPurposeWorklistInformationModelFIND = "1.2.840.10008.5.1.4.32.1";

    /** General Purpose Scheduled Procedure Step SOP Class - SOP Class */
    public static final String GeneralPurposeScheduledProcedureStepSOPClass = "1.2.840.10008.5.1.4.32.2";

    /** General Purpose Performed Procedure Step SOP Class - SOP Class */
    public static final String GeneralPurposePerformedProcedureStepSOPClass = "1.2.840.10008.5.1.4.32.3";

    /** Instance Availability Notification SOP Class - SOP Class */
    public static final String InstanceAvailabilityNotificationSOPClass = "1.2.840.10008.5.1.4.33";

    /** RT Beams Delivery Instruction Storage (Supplement 74 Frozen Draft) - SOP Class */
    public static final String RTBeamsDeliveryInstructionStorageSupplement74FrozenDraft = "1.2.840.10008.5.1.4.34.1";

    /** RT Conventional Machine Verification (Supplement 74 Frozen Draft) - SOP Class */
    public static final String RTConventionalMachineVerificationSupplement74FrozenDraft = "1.2.840.10008.5.1.4.34.2";

    /** RT Ion Machine Verification (Supplement 74 Frozen Draft) - SOP Class */
    public static final String RTIonMachineVerificationSupplement74FrozenDraft = "1.2.840.10008.5.1.4.34.3";

    /** Unified Worklist and Procedure Step Service Class - Service Class */
    public static final String UnifiedWorklistandProcedureStepServiceClass = "1.2.840.10008.5.1.4.34.4";

    /** Unified Procedure Step - Push SOP Class - SOP Class */
    public static final String UnifiedProcedureStepPushSOPClass = "1.2.840.10008.5.1.4.34.4.1";

    /** Unified Procedure Step - Watch SOP Class - SOP Class */
    public static final String UnifiedProcedureStepWatchSOPClass = "1.2.840.10008.5.1.4.34.4.2";

    /** Unified Procedure Step - Pull SOP Class - SOP Class */
    public static final String UnifiedProcedureStepPullSOPClass = "1.2.840.10008.5.1.4.34.4.3";

    /** Unified Procedure Step - Event SOP Class - SOP Class */
    public static final String UnifiedProcedureStepEventSOPClass = "1.2.840.10008.5.1.4.34.4.4";

    /** Unified Worklist and Procedure Step SOP Instance - Well-known SOP Instance */
    public static final String UnifiedWorklistandProcedureStepSOPInstance = "1.2.840.10008.5.1.4.34.5";

    /** General Relevant Patient Information Query - SOP Class */
    public static final String GeneralRelevantPatientInformationQuery = "1.2.840.10008.5.1.4.37.1";

    /** Breast Imaging Relevant Patient Information Query - SOP Class */
    public static final String BreastImagingRelevantPatientInformationQuery = "1.2.840.10008.5.1.4.37.2";

    /** Cardiac Relevant Patient Information Query - SOP Class */
    public static final String CardiacRelevantPatientInformationQuery = "1.2.840.10008.5.1.4.37.3";

    /** Hanging Protocol Storage - SOP Class */
    public static final String HangingProtocolStorage = "1.2.840.10008.5.1.4.38.1";

    /** Hanging Protocol Information Model - FIND - SOP Class */
    public static final String HangingProtocolInformationModelFIND = "1.2.840.10008.5.1.4.38.2";

    /** Hanging Protocol Information Model - MOVE - SOP Class */
    public static final String HangingProtocolInformationModelMOVE = "1.2.840.10008.5.1.4.38.3";

    /** Hanging Protocol Information Model - GET - SOP Class */
    public static final String HangingProtocolInformationModelGET = "1.2.840.10008.5.1.4.38.4";

    /** Color Palette Storage - Transfer */
    public static final String ColorPaletteStorage = "1.2.840.10008.5.1.4.39.1";

    /** Color Palette Information Model - FIND - Query/Retrieve */
    public static final String ColorPaletteInformationModelFIND = "1.2.840.10008.5.1.4.39.2";

    /** Color Palette Information Model - MOVE - Query/Retrieve */
    public static final String ColorPaletteInformationModelMOVE = "1.2.840.10008.5.1.4.39.3";

    /** Color Palette Information Model - GET - Query/Retrieve */
    public static final String ColorPaletteInformationModelGET = "1.2.840.10008.5.1.4.39.4";

    /** Product Characteristics Query SOP Class - SOP Class */
    public static final String ProductCharacteristicsQuerySOPClass = "1.2.840.10008.5.1.4.41";

    /** Substance Approval Query SOP Class - SOP Class */
    public static final String SubstanceApprovalQuerySOPClass = "1.2.840.10008.5.1.4.42";

    /** Siemens CSA Non-Image Storage - SOP Class */
    public static final String SiemensCSANonImageStorage = "1.3.12.2.1107.5.9.1";

}