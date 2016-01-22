<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="xs" version="2.0"
    xpath-default-namespace="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns="http://www.daisy.org/z3986/2005/dtbook/">
    <xsl:template match="frontmatter/docauthor">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </xsl:copy>
        <xsl:call-template name="add-information-based-from-metadata"/>
    </xsl:template>
    <xsl:template match="node()" mode="#all" priority="-5">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates mode="#current"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template name="add-information-based-from-metadata">
        <level1 class="first-page">
            <p class="author">
                <xsl:value-of select="//meta[@name eq 'dc:Creator']/@content"/>
            </p>
            <p class="title"> 
                <xsl:value-of select="//meta[@name eq 'dc:Title']/@content"/>
            </p>
            <p class="translater">
                <xsl:value-of select="//meta[@name eq 'dc:Contributor']/@content"/> 
            </p>
            <p class="nlb">NLB</p>
            <p class="year">
                <xsl:value-of select="concat('Norge ',format-dateTime(current-dateTime(), '[Y]'))"/>
            </p>
            <p class="bind">X av Y</p>
        </level1>
        <xsl:choose>
            <xsl:when test="exists(//frontmatter/level1[@class eq 'colophon'])">
                <level1 class="second-page">
                    <h1>Colophon</h1>
                    <xsl:copy-of select="//frontmatter/level1[@class eq 'colophon']/descendant::p"/>
                </level1>
            </xsl:when>
            <xsl:otherwise>
                <xsl:comment>No colophon!</xsl:comment>
            </xsl:otherwise>
        </xsl:choose>
        <level1 class="third-page">
            <h1>Om boka</h1>
            <p class="contraction">Fullskrift</p>
            <p class="pages">Antall Sider:</p>
            <p class="return">Boka skal ikke returneres.</p>
            <p class="contact">Feil eller mangler kan meldes til punkt@nlb.no.</p>
            
        </level1>
    </xsl:template>
</xsl:stylesheet>
