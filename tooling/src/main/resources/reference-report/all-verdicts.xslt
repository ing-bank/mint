<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:variable name="OracleVerdictOrder">FAIL|WARNING|INFO|OK|DONT_KNOW</xsl:variable>

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
                        margin: 2rem;
                    }

                    a, th, .link {
                        color: #70EFDE;
                        cursor: pointer;
                    }

                    .sequence-label {
                        vertical-align:center;
                        text-align: center;
                        small-caps bold 24px/24px Courier New;
                    }

                    .down:after {
                        content: ' v'
                    }
                    .up:after {
                        content: ' ^'
                    }

                    .selected {
                        color: #18D6BB;
                    }

                    tr:hover {
                        background: #4B4B4B;
                    }

                    input {
                        background-color: #2C2C2C;
                        border: solid;
                        border-width: 1px;
                        border-color: #18D6BB;
                        color: #F3F3F3;
                        width: 100%;
                    }
                    ul.menu {
                        list-style-type: none;
                        padding: 1em;
                    }
                    ul.menu li {
                        float: left;
                        padding-right: 1em;
                    }
                    ]]>
                </style>
            </head>
            <data id="data" style="display: none">
                <!-- Note that we use //*[name() = 'verdict'] instead of ..//verdict because of namespaces -->
                <xsl:for-each select="//*[name() = 'verdict']">
                    <xsl:sort select="string-length(substring-before($OracleVerdictOrder, @decision))" data-type="number"/>
                    <xsl:sort select="./ancestor::SystemUnderTest/AndroidLoop/@session" data-type="text" order="ascending"/>
                    <xsl:sort select="./ancestor::SystemUnderTest/AndroidLoop/@step" data-type="text" order="ascending"/>

                    <!-- First get the parent SystemUnderTest node as this is what ultimately contains the verdict -->
                    <xsl:variable name="sut" select="./ancestor::SystemUnderTest" />
                    <row>
                        <name><xsl:value-of select="../name()" /></name>
                        <decision><xsl:value-of select="@decision" /></decision>
                        <message><xsl:value-of select="@message" /></message>
                        <!--                                    <td><xsl:value-of select="$sut/AndroidLoop/@session" /></td>-->
                        <step><xsl:value-of select="$sut/AndroidLoop/@step + 1" /></step>
                        <!--                                    <td><xsl:value-of select="../../@positionInViewHierarchy"/></td>-->
                        <location><xsl:value-of select="$sut/AndroidLoop/@sequence" />-<xsl:value-of select="$sut/AndroidLoop/@session" />.html?xpath=//View[./ancestor::SystemUnderTest[./AndroidLoop[@step = '<xsl:value-of select="$sut/AndroidLoop/@step"/>']] and @positionInViewHierarchy = '<xsl:value-of select="../../@positionInViewHierarchy"/>']#step-<xsl:value-of select="$sut/AndroidLoop/@step + 1"/></location>
                    </row>
                </xsl:for-each>
            </data>
            <body id="body">
                <div>
                    <ul class="menu">
                        <li><a href="index.html">MINT Reports</a></li>
                        <li>Search verdicts</li>
                        <li><a href="index.html#manual">MINT manual</a></li>
                    </ul>
                </div>
                <div>
                    <div class="grow-wrap">
                        <input name="filter" id="filter" type="text" placeholder="Enter your filter string"/>
                    </div>
                    <table>
                        <thead>
                            <tr>
                                <th id="name" class="table-column">Verdict name</th>
                                <th id="decision" class="table-column">Verdict decision</th>
                                <th id="message" class="table-column">Verdict message</th>
                                <th id="step" class="table-column">Step</th>
                                <th id="location" class="table-column">Link</th>
                            </tr>
                        </thead>
                        <tbody id="verdicts">
                        </tbody>
                    </table>
                </div>
                <script>
                    <![CDATA[
// Based on https://gist.github.com/wfng92/ad6af87204da8e36647a365ca90d5af8s
var data = [];
var dataElement = document.getElementById('data')
for(child of dataElement.children) {
    var row = new Map();
    for(column of child.children) {
        row.set(column.localName, column.textContent);
    }
    data.push(row);
}
var table = document.getElementById('verdicts');
var input = document.getElementById('filter');

var caretUpClassName = 'up';
var caretDownClassName = 'down';

const sort_by = (field, reverse, primer) => {

  const key = primer ?
    function(x) {
      //return primer(x[field]);
      return primer(x.get(field));
    } :
    function(x) {
      //return x[field];
      return x.get(field);
    };

  reverse = !reverse ? 1 : -1;

  return function(a, b) {
    return a = key(a), b = key(b), reverse * ((a > b) - (b > a));
  };
};

function populateTable() {
  table.innerHTML = '';
  for(r of data) {
      let row = table.insertRow(-1);
      row.classList.toggle("link");
      var idx = 0;
      var a = document.createElement('a');
      for([k,v] of r) {
        var cell = row.insertCell(idx);
        if(k === 'location') {
          a.appendChild(document.createTextNode('Permalink'));
          a.href = v;
          a.target = '_blank';
          cell.appendChild(a);
        } else {
          cell.innerHTML = v;
        }
        idx++;
      }
      row.addEventListener('click', function(event) {
        event.srcElement.parentElement.getElementsByTagName('a')[0].click();
      });
  }

  filterTable();
}

function clearArrow() {
  let carets = document.getElementsByClassName('caret');
  for (let caret of carets) {
    caret.className = 'caret';
  }
}

function toggleArrow(event) {
  let element = event.target;
  let caret, field, reverse;
    caret = element;
    field = element.id

  let iconClassName = caret.className;
  clearArrow();

  if (iconClassName.includes(caretUpClassName)) {
    caret.className = `caret ${caretDownClassName}`;
    reverse = false;
  } else {
    reverse = true;
    caret.className = `caret ${caretUpClassName}`;
  }

  data.sort(sort_by(field, reverse));
  populateTable();
}

function filterTable() {
  let filter = input.value.toUpperCase();
  rows = table.getElementsByTagName('tr');
  let flag = false;

  for (let row of rows) {
    let cells = row.getElementsByTagName('td');
    for (let cell of cells) {
      if (cell.textContent.toUpperCase().indexOf(filter) > -1) {
        if (filter) {
          cell.className = 'selected';
        } else {
          cell.className = '';
        }

        flag = true;
      } else {
        cell.className = '';
      }
    }

    if (flag) {
      row.style.display = '';
    } else {
      row.style.display = 'none';
    }

    flag = false;
  }
}

populateTable();

for (let column of document.getElementsByClassName('table-column')) {
  column.addEventListener('click', function(event) {
    toggleArrow(event);
  });
}

input.addEventListener('keyup', function(event) {
  filterTable();
});
                    ]]>
                </script>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>