<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:mmax="org.eml.MMAX2.discourse.MMAX2DiscourseLoader"
                xmlns:sentence="de.tudarmstadt.dkpro/NameSpaces/Sentence">
 <xsl:output method="text" indent="no" omit-xml-declaration="yes"/>
<xsl:strip-space elements="*"/>

<xsl:template match="words">
<xsl:text>
</xsl:text>
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="word">
 <xsl:value-of select="mmax:registerDiscourseElement(@id)"/>
 <xsl:value-of select="mmax:setDiscourseElementStart()"/>
  <xsl:apply-templates/>
 <xsl:value-of select="mmax:setDiscourseElementEnd()"/>
 <xsl:text> </xsl:text>
</xsl:template>

<xsl:template match="sentence:markable" mode="closing">
 <xsl:text>
 </xsl:text>
</xsl:template>

</xsl:stylesheet>
