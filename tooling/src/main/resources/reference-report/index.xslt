<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">


    <xsl:template match="/">
        <html>
            <head>
                <style>
                    body {
                        background-color: #121212;
                        color: #F3F3F3;
                        font-family: Arial;
                        font-size: 14px;
                        padding-left: 5em;
                        overflow-wrap: normal;
                        max-width: 68em;
                        line-height: 1.5;
                    }
                    table {
                        background-color: #4B4B4B;
                    }
                    th {
                        border-bottom-style: solid;
                    }
                    td {
                        text-align: center;
                    }
                    th, td {
                        padding: 0.2em;
                        padding-left: 1em;
                        padding-right: 1em;
                    }
                    p.metadata {
                        white-space: pre-line;
                    }
                    a {
                        color: #70EFDE;
                    }
                    .align-right {
                        float: right;
                    }
                    ul.menu {
                        list-style-type: none;
                        padding: 1em;
                    }
                    ul.menu li {
                        float: left;
                        padding-right: 1em;
                    }
                </style>
            </head>
            <body id="body">
                <!-- this is a linebreak -->
                <xsl:text>&#xa;</xsl:text>
                <xsl:variable name="sessions" select="//sessions/session"/>
                <xsl:variable name="totalSteps" select="sum(//sessions/session/@steps)"/>
                <div>
                    <ul class="menu">
                        <li>MINT Reports</li>
                        <li><a href="verdicts.html">Search verdicts</a></li>
                        <li><a href="#manual">MINT manual</a></li>
                    </ul>
                </div>
                <div>
                    <div>
                        <p class="metadata">
                            Last updated at <xsl:value-of select="//metadata/@date"/> - <xsl:value-of select="//metadata/@time"/>
                            Tested <xsl:value-of select="//metadata/@appname"/>
                            <xsl:value-of select="count($sessions)"/> sessions
                            <xsl:value-of select="$totalSteps"/> steps in total
                            <xsl:value-of select="sum($sessions/@notOkVerdicts)"/>/<xsl:value-of select="sum($sessions/@verdicts)"/> (~<xsl:value-of select="floor(sum($sessions/@notOkVerdicts) div sum($sessions/@verdicts) * 100)"/> % not OK) verdicts found
                        </p>
                    </div>
                </div>
                <table>
                    <thead>
                        <tr>
                            <th>Start time</th>
                            <th>Sequence</th>
                            <th>#Steps</th>
                            <th>#Not OK verdicts</th>
                            <th>#Verdicts</th>
                            <th>Link (please use Chrome)</th>
                        </tr>
                    </thead>
                    <xsl:for-each select="$sessions">
                        <xsl:sort select="@notOkVerdicts" data-type="number" order="descending"/>
                        <tr>
                            <td><xsl:value-of select="@date" /> - <xsl:value-of select="@time" /></td>
                            <td><xsl:value-of select="@sequence" /></td>
                            <td><xsl:value-of select="@steps" /></td>
                            <td><xsl:value-of select="@notOkVerdicts" /></td>
                            <td><xsl:value-of select="@verdicts" /></td>
                            <td>
                                <a>
                                    <xsl:attribute name="href">
                                        <xsl:value-of select="@sequence" />-<xsl:value-of select="text()" />
                                    </xsl:attribute>
                                    <xsl:value-of select="text()" />
                                </a>
                            </td>
                        </tr>
                    </xsl:for-each>
                </table>
                <div id="manual">
                    <h2>MINT manual</h2>

                    <p>Mint works with 3 major constructs: a) Test runs and sequences, b) Rules and c) Oracles.</p>

                    <h3>Test runs and sequences</h3>

                    <p>Each test run consists of a number of sequences, and each sequence consists of a number of steps.</p>

                    <a href="https://dev.azure.com/IngEurCDaaS01/IngOne/_git/P16898-MINT?path=/docs/android.md&amp;version=GBmaster&amp;anchor=creating-your-first-test">How to set up test runs</a>

                    <p>While configuring a test run of MINT, you decide which rules or oracles you want to use and which to ignore.</p>

                    <h3>Rules</h3>

                    <p>MINT interacts with your app by following rules. These rules define which interactions are supported for your applications, such as clicking, providing input etc. Rules have a relative importance, together defining a model on how MINT progresses through the app in order to cover a broad range of states. Due to this approach, MINT is able to guide itself throughout your application without prior knowledge.</p>

                    <p>Rules can be defined with various intents or categories:</p>

                    <ol>
                        <li>A generic rule, applicable to any appliction (e.g. interacting with clickable elements).</li>
                        <li>A specific rule for a class of applications (e.g. providing email addresses for a text field that is contextualised as a field requiring an email address).</li>
                        <li>Domain specifc rules (e.g. internal account numbers or identifiers, only usable by your internally developed software).</li>
                    </ol>
                    Categories 1. and 2. are typically maintained and provided by the MINT community. You as an end user might want to add rules of category 3. on your own.

                    An excerpt of the default rules. <a href="https://dev.azure.com/IngEurCDaaS01/IngOne/_git/P16898-MINT?path=/mint-android/android-core/src/main/kotlin/org/mint/android/rule&amp;version=GBmaster">See here for a detailed list with all known rules</a>. We welcome you to add or enhance any rule you think is useful, see <a href="https://dev.azure.com/IngEurCDaaS01/IngOne/_git/P16898-MINT?path=/docs/contributing.md&amp;version=GBmaster">contributing</a> on how to reach out or provide an actual contribution.

                    Any rule can be enabled or disabled on its own.

                    <ol>
                        <xsl:for-each select="//rule">
                            <xsl:sort select="@name" data-type="text" order="ascending"/>
                            <li>
                                <xsl:value-of select="@name" /> - <xsl:value-of select="@description" />
                            </li>
                        </xsl:for-each>
                    </ol>

                    <h3>Oracles</h3>
                    <p>After each interaction MINT stores the application state. Oracles observe each state and produces verdicts. An example oracle would be an oracle that detects overlapping text, an error log line or a 'black hole state' (i.e. an application state that cannot be exited).</p>
                    <ol>
                        <xsl:for-each select="//oracle">
                            <xsl:sort select="@name" data-type="text" order="ascending"/>
                            <li>
                                <xsl:value-of select="@name" /> - <xsl:value-of select="categories/@name"/> - <xsl:value-of select="@description" />
                            </li>
                        </xsl:for-each>
                    </ol>

                    An excerpt of the default rules. <a href="https://dev.azure.com/IngEurCDaaS01/IngOne/_git/P16898-MINT?path=/mint-android/android-core/src/main/kotlin/org/mint/android/oracle&amp;version=GBmaster">See here for a detailed list with all known rules</a>. We welcome you to add or enhance any rule you think is useful, see <a href="https://dev.azure.com/IngEurCDaaS01/IngOne/_git/P16898-MINT?path=/docs/contributing.md&amp;version=GBmaster">contributing</a> on how to reach out or provide an actual contribution.

                    Any oracle can be enabled or disabled on its own.
                </div>
            </body>

        </html>
    </xsl:template>

</xsl:stylesheet>