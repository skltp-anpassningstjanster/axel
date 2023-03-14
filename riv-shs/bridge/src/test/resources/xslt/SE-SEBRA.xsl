<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:ns3="urn:riv-lv:lv:reporting:pharmacovigilance:ProcessIndividualCaseSafetyReportResponder:1"
	xmlns:ns2="urn:riv-lv:lv:reporting:pharmacovigilance:1"
	xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/"
	exclude-result-prefixes="ns2 ns3 SOAP-ENV">

	<xsl:output method="xml" indent="yes" encoding="utf-8" cdata-section-elements="parentmedicalrelevanttext" />
	
	<xsl:template match="ns3:ProcessIndividualCaseSafetyReport">
		<xsl:text disable-output-escaping="yes">
&lt;!DOCTYPE ichicsr SYSTEM "http://eudravigilance.ema.europa.eu/dtd/icsr21xml.dtd">
		</xsl:text>
		<xsl:apply-templates select="ns3:individualCaseSafetyReport/ns2:ichicsr" />
	</xsl:template>

	<xsl:template match="*">
		<!-- remove element prefix -->
		<xsl:element name="{local-name()}">
			<!-- process attributes -->
			<xsl:for-each select="@*">
				<!-- remove attribute prefix -->
				<xsl:attribute name="{local-name()}">
          <xsl:value-of select="." />
        </xsl:attribute>
			</xsl:for-each>
			<xsl:apply-templates />
		</xsl:element>
	</xsl:template>

  <xsl:template match="comment()">
    <xsl:comment>
      <xsl:value-of select="." />
    </xsl:comment>
  </xsl:template>
</xsl:stylesheet>


