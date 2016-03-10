<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="xs" version="2.0"
    xpath-default-namespace="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns="http://www.daisy.org/z3986/2005/dtbook/">

    <xsl:output indent="yes"/>

    <xsl:param name="braille-standard" select="'(dots:6)(grade:0)'"/>
    <xsl:variable name="contraction-grade" select="replace($braille-standard,'.*\(grade:(.*)\).*','$1')"/>

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
        <level1 class="first-page" style="text-align: center; page-break-inside:avoid ; flow: front; ">

            <xsl:variable name="author" select="//meta[@name eq 'dc:Creator']/@content"/>
            <xsl:for-each select="$author[position() &lt;= 3]">
                <xsl:choose>
                    <xsl:when test="position() = 1">
                        <p class="author" style="display: block;  margin-top: 3; ">
                            <xsl:value-of select="."/>
                        </p>
                    </xsl:when>
                    <xsl:when test="position() = 3 and count($author) > 3">
                        <p class="author" style="display: block;">
                            <xsl:value-of select="count($author) - 2"/>
                            <xsl:text> flere</xsl:text>
                        </p>
                    </xsl:when>
                    <xsl:otherwise>
                        <p class="author" style="display: block;">
                            <xsl:value-of select="."/>
                        </p>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
            <p class="title" style="display: block; margin-top: 1;">
                <xsl:value-of select="//meta[@name eq 'dc:Title']/@content"/>
            </p>


            <xsl:variable name="contributor" select="//meta[@name eq 'dc:Contributor']/@content"/>
            <xsl:for-each select="$contributor[position() &lt;= 3]">
                <xsl:choose>
                    <xsl:when test="position() = 1">
                        <p class="translater" style="display: block;  margin-top: 2; ">
                            <xsl:value-of select="."/>
                        </p>
                    </xsl:when>
                    <xsl:when test="position() = 3 and count($contributor) > 3">
                        <p class="translater" style="display: block;">
                            <xsl:value-of select="count($contributor) - 2"/>
                            <xsl:text> flere</xsl:text>
                        </p>
                    </xsl:when>
                    <xsl:otherwise>
                        <p class="translater" style="display: block;">
                            <xsl:value-of select="."/>
                        </p>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>

            <p class="nlb" style="display: block; margin-top: 4;">NLB</p>
            <p class="year" style="display: block; margin-bottom: 2;">
                <xsl:value-of select="concat('Norge ', format-dateTime(current-dateTime(), '[Y]'))"
                />
            </p>
            <p class="bind"
                style="display: block; page-break-after: always; @page margin-bottom: 1; content: 'Bind ' -obfl-evaluate($volume) ' of ' -obfl-evaluate($volumes) ;">1 av
                1</p>
        </level1>
        <xsl:choose>
            <xsl:when test="exists(//frontmatter/level1[@class eq 'colophon'])">
                <level1 class="second-page"
                    style=" margin-top: 1; margin-bottom: 1; margin-left: 1; margin-right: 1;">
                    <h1 style="display: block;  page-break-before: always;  ">Colophon</h1>
                    <xsl:copy-of select="//frontmatter/level1[@class eq 'colophon']/descendant::p"/>
                </level1>
            </xsl:when>
            <xsl:otherwise>
                <xsl:comment>No colophon!</xsl:comment>
            </xsl:otherwise>
        </xsl:choose>
        <level1 class="third-page"
            style=" margin-top: 1; margin-bottom: 1; margin-left: 1; margin-right: 1;">
            <h1 style="display: block;  page-break-before: always; ">Om boka</h1>
            <p class="contraction" style="display: block; margin-top: 1;">
                <xsl:choose>
                    <xsl:when test="$contraction-grade = '0'">Fullskrift</xsl:when>
                </xsl:choose>
                <xsl:choose>
                    <xsl:when test="$contraction-grade = '1'">Kortskrift nivå 1</xsl:when>
                </xsl:choose>
                <xsl:choose>
                    <xsl:when test="$contraction-grade = '2'">Kortskrift nivå 2</xsl:when>
                </xsl:choose>
                <xsl:choose>
                    <xsl:when test="$contraction-grade = '3'">Kortskrift nivå 3</xsl:when>
                </xsl:choose>
            </p>
            <p class="pages" style="display: block;">Antall Sider: </p>
            <p class="return" style="display: block;">Boka skal ikke returneres.</p>
            <p class="contact" style="display: block; page-break-after: always;">Feil eller mangler
                kan meldes til punkt@nlb.no.</p>

        </level1>
    </xsl:template>



</xsl:stylesheet>
