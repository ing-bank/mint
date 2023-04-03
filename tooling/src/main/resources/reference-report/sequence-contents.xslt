<!--
    Note that because we execute this in the browser, we are bound to XSLT version 1

    Next to that, this XSLT is serialised into the body of all of the HTML documents
    produced by the 'sequence.xslt'. This means that CSS classes, relative document structure etc
    are all derived from the parent document. We store it as an individual file in order to retain
    syntax highlighting etc.
-->
<xsl:stylesheet
    version="1.0"
    xmlns:h="http://www.w3.org/1999/xhtml"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:action="http://org.mint/espresso/action"
    xmlns:rule="http://org.mint/espresso/rule"
    xmlns:report="http://org.mint/report"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <!-- index AndroidLoop elements by their session id -->
    <xsl:key name="loopBySession" match="h:body/h:div[@id = 'data']//AndroidLoop" use="@session"/>
    <!-- index AndroidLoop elements by their sequence id -->
    <xsl:key name="loopBySequence" match="h:body/h:div[@id = 'data']//AndroidLoop" use="@sequence"/>
    <xsl:variable name="OracleVerdictOrder">FAIL|WARNING|INFO|OK|DONT_KNOW</xsl:variable>

    <xsl:template match="/">
        <div>
            <table>
                <xsl:variable name="enabled" select="h:body//h:div[@id ='initial']//h:input[@id = 'filter']/@is_checked"/>
                <xsl:variable name="className" select="h:body//h:div[@id ='initial']//h:input[@id = 'classtext']/@textvalue"/>

                <xsl:for-each select="h:body/h:div[@id = 'data']//AndroidLoop[generate-id() = generate-id(key('loopBySession', @session)[1])]">
                    <xsl:sort select="@session" data-type="text" order="ascending"/>

                    <xsl:variable name="ses" select="./@session"  />

                    <xsl:for-each select="/h:body/h:div[@id = 'data']//AndroidLoop[generate-id() = generate-id(key('loopBySequence', @sequence)[1])]">
                        <xsl:sort select="@sequence" data-type="number" order="ascending"/>

                        <xsl:variable name="seq" select="./@sequence" />
                        <!-- Collect all relevant AndroidLoop elements -->
                        <xsl:variable name="row" select="/h:body/h:div[@id = 'data']//AndroidLoop[@session = $ses and @sequence = $seq]"/>

                        <tr class="results-row">
                            <xsl:for-each select="$row">
                                <xsl:sort select="@step" data-type="number" order="ascending"/>

                                <xsl:variable name="w" select="number(../Screenshot/@width)"/>
                                <xsl:variable name="h" select="number(../Screenshot/@height)"/>
                                <xsl:variable name="f" select="$w div 240"/>

                                <!-- Note that we increment with one so that the anchors correspond to 1-based counting -->
                                <td id="step-{@step + 1}">
                                    <div>
                                        <!-- Make a distinction between a 'step action' (i.e. something the user defined in a step() block)
                                         and an exploratory action (i.e. what MINT does by itself) -->
                                        <xsl:choose>
                                            <xsl:when test="count(..//*[@stepAction='true']) = 1">
                                                <button type="button" class="collapsible">Step <xsl:value-of select="@step + 1"/> (scripted)</button>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <button type="button" class="collapsible">Step <xsl:value-of select="@step + 1"/></button>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                        <div class="content">
                                            <textarea class="step-snippet"><xsl:copy-of select=".."/></textarea>
                                        </div>
                                    </div>
                                    <!-- Let's not put a fixed height here, but let it scale for itself height="75vh /*{$h div $f}*/" -->
                                    <svg width="{$w div $f}" viewBox="0 0 {$w} {$h}">
                                        <xsl:apply-templates select="../Screenshot">
                                            <xsl:with-param name="f" select="$f"/>
                                            <xsl:with-param name="w" select="$w"/>
                                            <xsl:with-param name="h" select="$h"/>
                                        </xsl:apply-templates>

                                        <rect x="0" y="0" width="{$w}" height="{$h}" stroke="black" stroke-width="5" fill="none"/>

                                        <xsl:for-each select="../Application/Window/View">
                                            <xsl:apply-templates select=".">
                                                <xsl:with-param name="f" select="$f"/>
                                                <xsl:with-param name="w" select="$w"/>
                                                <xsl:with-param name="h" select="$h"/>
                                                <xsl:with-param name="enabled" select="$enabled"/>
                                                <xsl:with-param name="classname" select="$className"/>
                                            </xsl:apply-templates>
                                        </xsl:for-each>
                                    </svg>
                                </td>
                            </xsl:for-each>
                        </tr>
                        <tr class="results-row">
                            <xsl:for-each select="..//*[@errorMessage]">
                                <tr class="error-message">
                                <a>Error: </a>
                                <a class="error-message"><xsl:value-of select="@errorMessage"/></a>
                                </tr>
                            </xsl:for-each>
                            <xsl:for-each select="$row">
                                <xsl:sort select="@step" data-type="number" order="ascending"/>
                                <xsl:variable name="step" select="@step" />
                                <td class="left-align-verdicts">
                                    <div id="verdicts-fail-{$step}" style="display: none">
                                        <center>
                                            <h2>FAIL</h2>
                                        </center>
                                        <ol class="verdicts" id="list-verdicts-fail-{$step}" />
                                    </div>
                                    <div id="verdicts-warn-{$step}" style="display: none">
                                        <center>
                                            <h2>WARN</h2>
                                        </center>
                                        <ol class="verdicts" id="list-verdicts-warn-{$step}" />
                                    </div>
                                    <div id="verdicts-info-{$step}" style="display: none">
                                        <center>
                                            <h2>INFO</h2>
                                        </center>
                                        <ol class="verdicts" id="list-verdicts-info-{$step}" />
                                    </div>
                                    <div id="verdicts-ok-{$step}" style="display: none">
                                        <center>
                                            <h2>OK</h2>
                                        </center>
                                        <ol class="verdicts" id="list-verdicts-ok-{$step}" />
                                    </div>
                                    <div id="verdicts-dont-know-{$step}" style="display: none">
                                        <center>
                                            <h2>DONT_KNOW</h2>
                                        </center>
                                        <ol class="verdicts" id="list-verdicts-dont-know-{$step}" />
                                    </div>
                                    <data class="verdict-data" step="{$step}" style="display: none">
                                        <!-- Note that we use ..//*[name() = 'verdict'] instead of ..//verdict because of namespaces -->
                                        <xsl:for-each select="..//*[name() = 'verdict']">
                                            <xsl:variable name="decision" select="@decision" />
                                            <verdict decision="{$decision}">
                                                <div>
                                                    <li class="verdict-info">
                                                        <a>
                                                            <xsl:attribute name="onclick">exampleQueryStr("//<xsl:value-of select="name(../..)"/>[./ancestor::SystemUnderTest[./AndroidLoop[@step = '<xsl:value-of select="$step"/>']] and @positionInViewHierarchy = '<xsl:value-of select="../../@positionInViewHierarchy"/>']");</xsl:attribute>
                                                            <xsl:value-of select="name(..)"/>: <xsl:value-of select="@decision"/> ⬅ <xsl:value-of select="name(../..)"/> (<xsl:value-of select="@message"/>)
                                                        </a>
                                                    </li>
                                                    <button type="button" class="collapsible left-align-verdicts">Context</button>
                                                    <div class="content">
                                                        <xsl:choose>
                                                            <xsl:when test="@decision != 'OK'">
                                                                <textarea class="verdict-snippet"><xsl:copy-of select="../.."/></textarea>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                                <textarea class="verdict-snippet">&lt;<xsl:copy-of select="name(../..)"/>&gt; ⬅ <xsl:copy-of select=".."/></textarea>
                                                            </xsl:otherwise>
                                                        </xsl:choose>
                                                    </div>
                                                </div>
                                            </verdict>
                                        </xsl:for-each>
                                    </data>
                                </td>
                            </xsl:for-each>
                        </tr>
                    </xsl:for-each>
                </xsl:for-each>
            </table>
        </div>
    </xsl:template>

    <xsl:template match="View" mode="view">
        <tr>
            <td class="selected"><xsl:value-of select="@class"/></td>
        </tr>
    </xsl:template>

    <xsl:template match="Screenshot">
        <xsl:param name="f"/>
        <xsl:param name="w"/>
        <xsl:param name="h"/>
        <image x="0" y="0" width="{$w}" height="{$h}" xlink:href="{.}"/>
    </xsl:template>

    <xsl:template match="View">
        <xsl:param name="f"/>
        <xsl:param name="w"/>
        <xsl:param name="h"/>
        <xsl:param name="enabled"/>
        <xsl:param name="className"/>

        <xsl:variable name="report" select="count(report:match)"/>

        <xsl:if test="@screenX and @screenY">
            <xsl:variable name="action" select="./rule:rule-group//action:*[@selected = 'true']"/>
            <xsl:choose>
                <xsl:when test="$action">
                    <g id="component" transform="translate({@screenX}, {@screenY})">
                        <rect class="st1{$report}" x="-25" y="-25" width="{@width+50}" height="{@height+50}"/>
                        <foreignobject class="tooltip" x="{-(@screenX div $f)}" y="{($h div $f) - (@screenY div $f)}" width="500" height="100%" transform="scale({$f} {$f})">
                            <div class="action-selected-popup">
                                <xsl:apply-templates select="$action" mode="inspect">
                                    <xsl:with-param name="view" select="."/>
                                </xsl:apply-templates>
                            </div>
                        </foreignobject>
                    </g>
                </xsl:when>
                <xsl:otherwise>
                    <g id="component" transform="translate({@screenX}, {@screenY})">
                        <rect class="st0{$report}" width="{@width}" height="{@height}"/>
                        <foreignobject class="tooltip" x="{-(@screenX div $f)}" y="{($h div $f) - (@screenY div $f)}" width="500" height="100%" transform="scale({$f} {$f})">
                            <div class="element-hover-popup">
                                <xsl:apply-templates select="." mode="inspect"/>
                            </div>
                        </foreignobject>
                    </g>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
        <xsl:apply-templates select="View">
            <xsl:with-param name="f" select="$f"/>
            <xsl:with-param name="w" select="$w"/>
            <xsl:with-param name="h" select="$h"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="action:*" mode="inspect">
        <xsl:param name="view"/>
        <table>
            <tr>
                <td class="widget-action-taken">Action taken: </td>
                <td><xsl:value-of select="name(.)"/></td>
            </tr>
            <tr>
                <td>class:</td>
                <td><xsl:value-of select="$view/@class"/></td>
            </tr>
            <tr>
                <td>resourceName:</td>
                <td><xsl:value-of select="@resourceName"/></td>
            </tr>
            <tr>
                <td>priority:</td>
                <td><xsl:value-of select="@derived-priority"/></td>
            </tr>
            <xsl:if test="name(.) = 'input' or name(.) = 'timePickerInput' or name(.) = 'datePickerInput' ">
                <tr>
                <td>input:</td>
                <td><xsl:value-of select="@text"/></td>
            </tr>
            </xsl:if>
            <xsl:if test="name(.) = 'clickOnSpinnerItem' or name(.) = 'clickOnAdapterViewItem'">
            <tr>
                <td>position:</td>
                <td><xsl:value-of select="@position"/></td>
            </tr>
            </xsl:if>
        </table>
    </xsl:template>

    <xsl:template match="View" mode="inspect">
        <xsl:choose>
            <xsl:when test="count(report:match) = 0">
                <table>
                    <tr>
                        <td class="widget-element"/>
                        <td>Identified element</td>
                    </tr>
                    <tr>
                        <td>class:</td>
                        <td><xsl:value-of select="@class"/></td>
                    </tr>
                    <tr>
                        <td>resourceName:</td>
                        <td><xsl:value-of select="@resourceName"/></td>
                    </tr>
                    <tr>
                        <td>priority*:</td>
                        <td><xsl:value-of select="sum(./rule:rule-group//action:*/@derived-priority)"/></td>
                    </tr>
                </table>
            </xsl:when>
            <xsl:otherwise>
                <table>
                    <tr>
                        <td class="widget-queried-element"/>
                        <td>Matched queried element</td>
                    </tr>
                    <tr>
                        <td>class:</td>
                        <td><xsl:value-of select="@class"/></td>
                    </tr>
                    <tr>
                        <td>resourceName:</td>
                        <td><xsl:value-of select="@resourceName"/></td>
                    </tr>
                    <tr>
                        <td>priority*:</td>
                        <td><xsl:value-of select="sum(./rule:rule-group//action:*/@derived-priority)"/></td>
                    </tr>
                </table>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>