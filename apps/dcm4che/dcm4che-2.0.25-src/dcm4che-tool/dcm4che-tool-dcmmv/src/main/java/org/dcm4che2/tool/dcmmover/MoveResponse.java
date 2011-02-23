package org.dcm4che2.tool.dcmmover;

/**
 * @author gpotter (gcac96@gmail.com)
 * @version $Revision$
 */
public interface MoveResponse {
    
    public boolean moveSuccessful();
    
    public int getNumberOfStudyObjectsMoved();
    
    public String getMoveSourceAeTitle();
    
    public String getMoveDestinationAeTitle();
    
    /**
     * Returns a study/series/object uid mapping XML document as a string. Document Template:
     * <xsd:element name='study' minOccurs='1' maxOccurs='1'>
     *   <xsd:complexType>
     *     <xsd:attribute name='oldUid' type='string'/>
     *     <xsd:attribute name='newUid' type='string'/>
     *     <xsd:element name='series' minOccurs='1' maxOccurs='unbounded'>
     *       <xsd:complexType>
     *         <xsd:attribute name='oldUid' type='string'/>
     *         <xsd:attribute name='newUid' type='string'/>
     *         <xsd:element name='object' minOccurs='1' maxOccurs='unbounded'>
     *           <xsd:complexType>
     *             <xsd:attribute name='oldUid' type='string'/>
     *             <xsd:attribute name='newUid' type='string'/>
     *           </xsd:complexType>
     *         </xsd:element>
     *       </xsd:complexType>
     *     </xsd:element>
     *   </xsd:complexType>
     * </xsd:element>
     * @return An XML document as a string. Can be an empty string if the study was not anonymized.
     */
    public String getUidMappingDoc();
    
    /**
     * Returns
     * <xsd:element name='failedStorageCommitment' minOccurs='1' maxOccurs='unbounded'>
     * 	<xsd:attribute name='objectUid' type='string'/>
     * 	<xsd:attribute name='reason' type='string'/>
     * </xsd:element>
     * @return An XML document as a string. Can be an empty string if no failures.
     */
    public String getStorageCommitFailuresDoc();
    
    public String getError();
}
