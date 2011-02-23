<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="text" />
    <xsl:template match="/dicom">
        <xsl:text>PID:</xsl:text>
        <xsl:value-of select="attr[@tag='00100020']" /> 
        <xsl:text>|Name:</xsl:text>
        <xsl:value-of select="attr[@tag='00100010']" /> 
    </xsl:template>
</xsl:stylesheet>