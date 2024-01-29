<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template match="/">
        <html>
            <head>
                <style>
                    <![CDATA[
                    body {
                        background-color: #121212;
                        color: #F3F3F3;
                        font-family: Arial;
                        font-size: 14px;
                        line-height: 1.5;
                    }

                    table {
                        color: #121212;
                    }

                    td {
                        text-align: center;
                        color: #F3F3F3;
                    }
                    th, td {
                        padding: 0.2em;
                    }
                    a {
                        color: #70EFDE;
                        cursor: pointer;
                    }
                    ul.menu {
                        list-style-type: none;
                        padding: 1em;
                    }
                    ul.menu li {
                        float: left;
                        padding-right: 1em;
                    }
                    #scrolldiv {
                        overflow: visible;
                        height: 85vh;
                    }
                    div.top-scrollbars {
                        transform: rotateX(180deg);
                    }

                    div.top-scrollbars * {
                        transform: rotateX(180deg);
                    }
                    .selected {
                        background: LightYellow;
                    }
                    /** Highlight that a given step is selected */
                    svg.selected-step {
                        border: 1px solid #18D6BB;
                        padding-left: 1em;
                    }
                    svg {
                        overflow: visible;
                        padding-right: 1.5em;
                        padding-top: 1.5em;
                        padding-bottom: 1.5em;
                        padding-left: 0em;
                        padding-bottom: 7.5em;
                    }
                    svg image {
                        outline: solid;
                        outline-width: 1.5em;
                        outline-color: darkgray;
                    }
                    #component .tooltip {
                        visibility: hidden;
                        overflow: visible;
                    }

                    .action-selected-popup {
                        background: #4B4B4B; /* secondary background color */
                        color: #DBB2FF; /* secondary text color */
                        fill: black;
                    }

                    .element-hover-popup {
                        background: #4B4B4B; /* secondary background color */
                        color: #DBB2FF; /* secondary text color */
                        fill: black;
                    }

                    .widget-queried-element {
                        background-color: LightYellow;
                        color: #F3F3F3;
                        color: #DBB2FF; /* secondary text color */
                    }

                    .widget-element {

                    }

                    .widget-action-taken {
                        background: rgb(112, 239, 222, 0.5);
                    }

                    #component:hover .tooltip {
                        visibility: visible;
                    }

                    .results-row {
                        outline: thin solid;
                    }

                    .sequence-label {
                        vertical-align:center;
                        text-align: center;
                        small-caps bold 24px/24px Courier New;
                    }

                    .st00 {
                        /* Needed to make sure the overlays don't hide the application */
                        opacity:0.0;
                    }
                    .st01 {
                        fill: yellow;
                        opacity:0.5;
                    }

                    .st00:hover {
                        fill: black;
                        opacity:0.25;
                    }
                    .st01:hover {
                        fill: black;
                        opacity:0.25;
                    }

                    .st10 {
                        fill: #70EFDE;
                        stroke:black;
                        stroke-width:2;
                        opacity:0.5;
                    }
                    .st11 {
                        fill: yellow;
                        stroke:black;
                        stroke-width:2;
                        opacity:0.7;
                    }

                    .st10:hover {
                        opacity:0.75;
                    }

                    .st11:hover {
                        opacity:0.75;
                    }

                    button, collapsible {
                        background-color: #18D6BB;
                        color: #121212;
                        border: solid;
                        display: inline-block;
                        margin-left: 1em;
                        cursor: pointer;
                    }

                    .input {
                        background-color: #2C2C2C;
                        border: solid;
                        border-width: 1px;
                        border-color: #18D6BB;
                        color: #F3F3F3;
                    }

                    /* Add a background color to the button if it is clicked on (add the .active class with JS), and when you move the mouse over it (hover) */
                    .active, .collapsible:hover, button:hover {
                        background-color: #14B8A1;
                    }

                    /* Style the collapsible content. Note: hidden by default */
                    .content {
                        background-color: #4B4B4B;
                        color: #F3F3F3;
                        font-family: Arial;
                        font-size: 14px;
                        line-height: 1.5;
                        display:none;
                        margin-top: 0.5em;
                        overflow: hidden;
                    }

                    .step-snippet {
                        background-color: #4B4B4B;
                        color: #F3F3F3;
                        font-family: Arial;
                        font-size: 14px;
                        line-height: 1.5;
                        width: 85vw;
                        height: 20vh;
                        overflow: none;
                    }

                    .verdict-snippet {
                        background-color: #4B4B4B;
                        color: #F3F3F3;
                        font-family: Arial;
                        font-size: 14px;
                        line-height: 1.5;
                        width: 85vw;
                        height: 20vh;
                        overflow: none;
                    }

                    .example-query, #xpath {
                        font-family: Courier New
                    }

                    .left-align-verdicts {
                        text-align: left;
                        margin-left: 0em;
                    }

                    .error-message {
                        text-align: left;
                        margin-left: 1em;
                        color: #D22B2B;
                        height: 2vh;
                    }

                    /* from https://css-tricks.com/the-cleanest-trick-for-autogrowing-textareas/ */
                    .grow-wrap {
                        /* easy way to plop the elements on top of each other and have them both sized based on the tallest one's height */
                        display: grid;
                    }
                    .grow-wrap::after {
                        /* Note the weird space! Needed to prevent jumpy behavior */
                        content: attr(data-replicated-value) " ";

                        /* This is how textarea text behaves */
                        white-space: pre-wrap;

                        /* Hidden from view, clicks, and screen readers */
                        visibility: hidden;
                    }
                    .grow-wrap > textarea {
                        /* You could leave this, but after a user resizes, then it ruins the auto sizing */
                        resize: none;

                        /* Firefox shows scrollbar on growth, you can hide like this. */
                        overflow: hidden;
                    }
                    .grow-wrap > textarea,
                    .grow-wrap::after {
                        /* Identical styling required!! */
                        border: 1px solid #18D6BB;
                        padding: 0.5rem;
                        margin-bottom: 0.5em;
                        font: inherit;

                        /* Place on top of each other */
                        grid-area: 1 / 1 / 2 / 2;
                    }

                    body {
                        margin: 2rem;
                    }

                    label {
                        display: block;
                        cursor: pointer;
                    }

                    #example-queries {
                        width: 100%;
                        padding-bottom: 0.5em;
                        margin-top: -1.25em;
                    }

                    #content {
                        margin-top: 1em;
                    }

                    .query-control {
                        display: inline-block;
                        margin-left: 1em;
                    }

                    #eval-query-button {
                        background-color: #DBB2FF;
                    }

                    /* Toggle switch stuff */
                    input[type=checkbox]{
                        height: 0;
                        width: 0;
                        visibility: hidden;
                    }

                    label.toggle {
                        cursor: pointer;
                        text-indent: -9999px;
                        width: 3em;
                        background: #4B4B4B;
                        display: block;
                        border-radius: 5em;
                        position: relative;
                    }

                    label.toggle:after {
                        content: '';
                        position: absolute;
                        top: 0.3em;
                        left: 0.3em;
                        width: 1em;
                        height: 1em;
                        background: #BFBFBF;
                        border-radius: 5em;
                        transition: 0.2s;
                    }

                    input:checked + label.toggle {
                        background: #18D6BB;
                    }

                    input:checked + label.toggle:after {
                        left: calc(100% - 0.3em);
                        transform: translateX(-100%);
                    }

                    label.toggle:active:after {
                        width: 1em;
                    }


                    #footer {
                        margin-bottom=-50px;
                    }
                    ]]>
                </style>
            </head>
            <body id="body">
                <div>
                    <ul class="menu">
                        <li><a href="index.html">MINT Reports</a></li>
                        <li><a href="verdicts.html">Search verdicts</a></li>
                        <li><a href="index.html#manual">MINT manual</a></li>
                    </ul>
                </div>
                <!-- this is a linebreak -->
                <xsl:text>&#xa;</xsl:text>
                <!-- This would be a API rest call to get data -->
                <xsl:element name="script">
                    <xsl:attribute name="id">dataset</xsl:attribute>
                    <xsl:attribute name="type">text/xmldata</xsl:attribute>

                    <xsl:copy-of select="serialize(.)"/>
                </xsl:element>

                <xsl:text>&#xa;</xsl:text>

                <!-- This would be an API rest call to get xsl transformers -->
                <xsl:element name="script">
                    <xsl:attribute name="id">view-xsl</xsl:attribute>
                    <xsl:attribute name="type">text/xmldata</xsl:attribute>

                    <xsl:copy-of select="serialize(document('sequence-contents.xslt'))"/>
                </xsl:element>

                <script>
                    <![CDATA[

                    const dataset = new DOMParser().
                    parseFromString(document.getElementById('dataset').innerHTML,"text/xml");
                    const view_processor = new XSLTProcessor();
                    var xslt = new DOMParser().
                    parseFromString(document.getElementById('view-xsl').innerHTML,"text/xml");
                    view_processor.importStylesheet(xslt);

                    function exampleQuery(self) {
                        exampleQueryStr(self.innerHTML);
                    }

                    function exampleQueryStr(str) {
                        enable_query();
                        var xpathBar = document.getElementById('xpath');
                        xpathBar.value = str;
                        xpathBar.onchange();
                        // fire an 'input' event to make sure the input text area is resized if needed
                        xpathBar.dispatchEvent(new Event('input', {bubbles:true}));
                    }

                    function enable_query() {
                        _query_toggle_state_enabled = true;
                        document.getElementById('query-toggle').checked = true;
                    }

                    function toggle_query(origin) {
                        if(_query_toggle_state_enabled) {
                            _query_toggle_state_enabled = false;
                            remove_highlights();
                        } else {
                            _query_toggle_state_enabled = true;
                            render_html(origin);
                        }
                    }

                    function remove_highlights() {
                        var box = document.getElementById('xpath');
                        var query = box.value;
                        var clearSelections = '/*[position() = 0]';
                        box.setAttribute('xpath', clearSelections);
                        render_html(this);
                        box.setAttribute('xpath', query);
                    }

                    function render_html(origin) {
                        deregister_event_handlers();

                        var s = new XMLSerializer().serializeToString(document);

                        var doc = new DOMParser().parseFromString(s, "text/xml");

                        var xpathElem = doc.getElementById('xpath');
                        if (xpathElem) {
                            var xpath = xpathElem.getAttribute("xpath");
                            var elems = doc.evaluate(xpath, doc, null, XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null);

                            var count = document.getElementById('xpath-match-count');
                            count.innerHTML = elems.snapshotLength;

                            for (var i = 0; i < elems.snapshotLength; i++) {
                                var r = doc.createElementNS("http://com.ing.mint/report", "match");
                                elems.snapshotItem(i).appendChild(r);
                            }
                        }
                        var transformed = view_processor.transformToDocument(doc.getElementById('body'));

                        var content = document.getElementById('content');
                        var result = new XMLSerializer().serializeToString(transformed);

                        content.innerHTML = result;

                        render_verdicts();
                        register_event_handlers();
                    }

                    function _toggle() {
                        this.classList.toggle("active");
                        var content = this.nextElementSibling;
                        if(content.tagName == 'LABEL') {
                            // The special case for the toggle switch, so move to a different element
                            content = this.parentElement.nextElementSibling
                        }
                        if (content.style.display === "block") {
                            content.style.display = "none";
                        } else {
                            content.style.display = "block";
                        }
                    }

                    function deregister_event_handlers() {
                        // Remove all eventhandlers because we recreate the HTML
                        var coll = document.getElementsByClassName("collapsible");
                        var i;

                        for (i = 0; i < coll.length; i++) {
                            coll[i].removeEventListener("click", _toggle);
                        }
                    }

                    function register_event_handlers() {
                        // Register event handlers for the collapsible elements
                        var coll = document.getElementsByClassName("collapsible");
                        var i;

                        for (i = 0; i < coll.length; i++) {
                            coll[i].addEventListener("click", _toggle);
                        }
                    }

                    let verdictData;
                    function render_verdicts() {
                     verdictData = {};
                     Array.from(document.getElementsByClassName('verdict-data')).forEach(data => {
                        let step = data.attributes["step"].value;
                        Array.from(data.children).forEach((verdict, verdictIndex) => {
                           let decision = verdict.attributes["decision"].value;
                           if (decision == "FAIL") {
                              _addVerdict("fail", step, verdict.innerHTML);
                           } else if (decision == "WARNING") {
                              _addVerdict("warn", step, verdict.innerHTML);
                           } else if (decision == "INFO") {
                              _addVerdict("info", step, verdict.innerHTML);
                           } else if (decision == "OK") {
                              _addVerdict("ok", step, verdict.innerHTML);
                           } else if (decision == "DONT_KNOW") {
                              _addVerdict("dont-know", step, verdict.innerHTML);
                           } else {
                              console.error('Unknown decision: ' + decision);
                           }
                        });
                     });
                     _doRenderVerdicts();
                    }

                    function _addVerdict(verdict, step, innerHTML) {
                     if (!verdictData[`list-verdicts-${verdict}-${step}`]) {
                        verdictData[`list-verdicts-${verdict}-${step}`] = "";
                     }
                     verdictData[`list-verdicts-${verdict}-${step}`] += innerHTML
                    }

                    function _doRenderVerdicts() {
                     let shownVerdictDivs = [];
                     Object.keys(verdictData).forEach(key => {
                        let divId = key.replace("list-", "");
                        if (!shownVerdictDivs.includes(divId)) {
                           _showVerdictDiv(divId);
                           shownVerdictDivs.push(divId);
                        }
                        let listElement = document.getElementById(key);
                        listElement.innerHTML = verdictData[key];
                     });
                    }

                    function _showVerdictDiv(id) {
                     let divElement = document.getElementById(id);
                     divElement.style.removeProperty("display");
                    }
                    ]]>
                </script>
                <div id="data" style="display: none;"/>
                <div id="initial">
                    <div>
                        <xsl:variable name="date" select="//AndroidLoop[@step = '0']/@date"/>
                        <xsl:variable name="time" select="//AndroidLoop[@step = '0']/@time"/>
                        <xsl:variable name="verdicts" select="count(//*[name() = 'verdict'])" />
                        <xsl:variable name="notOkVerdicts" select="count(//*[name() = 'verdict' and not(@decision = 'OK')])" />
                        <xsl:variable name="percentage" select="floor($notOkVerdicts div $verdicts * 100)" />
                        <xsl:variable name="steps" select="count(//SystemUnderTest)"/>
                        <p>Sequence: <xsl:value-of select="$date"/>, <xsl:value-of select="$time"/>, <xsl:value-of select="$steps"/> steps, <xsl:value-of select="$notOkVerdicts"/> / <xsl:value-of select="$verdicts"/>  not OK verdicts (~ <xsl:value-of select="$percentage"/>%)</p>
                    </div>
                    <div class="grow-wrap">
                        <textarea name="xpath" placeholder="Enter your xpath query" id="xpath" onInput="this.parentNode.dataset.replicatedValue = this.value" class="input" onchange="this.setAttribute('xpath', this.value); render_html(this);"></textarea>
                    </div>
                    <div id="example-queries">
                        <div class="query-control">
                        <label for="eval-query-button" >xpath:
                            <button onclick="this.setAttribute('xpath', ''); enable_query(); render_html(this);" type="button" id="eval-query-button">
                                Evaluate
                            </button>
                        </label>
                        </div>
                        <div class="query-control">
                            <label for="query-toggle">Highlight matches (<div id="xpath-match-count" style="display:inline;">0</div>)</label>
                        </div>
                        <div class="query-control">
                            <!-- Note that we use the setTimeout to delay redrawing the page, so the slider animation effect can finish, suggesting a very smooth UI experience -->
                            <input type="checkbox" id="query-toggle" class="toggle" onchange="setTimeout(function() {{ this.setAttribute('xpath', this.value); toggle_query(this); }}.bind(this), 200)" checked="true"/>
                            <label for="query-toggle" class="toggle">slider-placeholder</label>
                        </div>
                        <div class="query-control">
                            <label for="example-toggle">Show XPath examples</label>
                        </div>
                        <div class="query-control">
                            <input type="checkbox" id="example-toggle" class="toggle" onchange="_toggle.bind(this)();" checked=""/>
                            <label for="example-toggle" class="toggle">slider-placeholder</label>
                        </div>
                        <div class="content">
                            <ul>
                                <li><a onclick="exampleQuery(this);" class="example-query">//View[@isClickable='true']</a>: Show all clickable View elements</li>
                                <li><a onclick="exampleQuery(this);" class="example-query">//View[@isClickable='true' and @isDisplayed='false' and @isImageView='true']</a>: Find all Views that are clickable, not displayed and are also image views</li>
                                <li><a onclick="exampleQuery(this);" class="example-query">//View[./*/*[name() = 'verdict' and not(@decision='OK')]]</a>: Find Views that contain a verdict that is not OK</li>
                                <li><a onclick="exampleQuery(this);" class="example-query">//View[./*[name() = 'identical-speakable-text-oracle']/*[name() = 'verdict' and not(@decision='OK')]]</a>: Find all Views with an 'identical speakable text' oracle verdict that is not OK</li>
                            </ul>
                        </div>
                    </div>
                </div>
                <div id="scrolldiv">
                    <div id="content"/>
                </div>
                <script>
                    <![CDATA[
                    // Global state that tracks if the (interactive) query is enabled or not
                    var _query_toggle_state_enabled = true;

                    // install data (once)
                    var imp = document.importNode(dataset.documentElement, true);
                    var ds = document.getElementById('data');
                    ds.appendChild(imp);

                    // Remove the example toggle 'checked' status which is enabled by default
                    document.getElementById("example-toggle").checked = false;

                    // Render the initial page
                    render_html();

                    var xpathTextArea = document.getElementById('xpath');

                    xpathTextArea.addEventListener('keydown', function (e) {
                        // Get the code of pressed key
                        const keyCode = e.which || e.keyCode;

                        // 13 represents the Enter key
                        if (keyCode === 13 && !e.shiftKey) {
                            // Don't generate a new line
                            e.preventDefault();

                            var button = document.getElementById('eval-query-button');
                            button.click();

                            // Dispatch it.
                            xpathTextArea.dispatchEvent(new Event('change'));
                        }
                    });

                    // Finally, if we have an xpath query param, fill in the query in the xpath bar
                    var params = new URLSearchParams(window.location.search);
                    if(params.get('xpath') != null) {
                        xpathTextArea.textContent = params.get('xpath');
                        xpathTextArea.dispatchEvent(new Event('change'));
                    }
                    if(window.location.hash != '') {
                        var svg = document.getElementById(window.location.hash.substr(1)).getElementsByTagName('svg')[0];
                        svg.classList.toggle('selected-step');
                    }
                    ]]>
                </script>
            </body>
        </html>
    </xsl:template>

</xsl:stylesheet>