# 🌿 [MINT](/README.md) | [Demo](demo.md) | Features | [Contributing](contributing.md) | [Get Started](android.md)

Mint works with 3 major constructs: a) Test runs and sequences, b) Rules and c) Oracles . 

### Test runs and sequences 

Each test run consists of a number of sequences, and each sequence consists of a number of steps. 

Default setup op the test run: 

- Number of sequences: <3> 
- Number of steps: <25> 

[How to set up test runs](/docs/android.md#creating-your-first-test)

While configuring a test run of MINT, you decide which rules or oracles you want to use and which to ignore. 

### Rules 

MINT interacts with your app by following `rules`. These `rules` define which interactions are supported for your applications, such as clicking, providing input etc. Rules have a relative importance, together defining a model on how MINT progresses through the app in order to cover a broad range of states. Due to this approach, MINT is able to guide itself throughout your application without prior knowledge.

Rules can be defined with various intents or categories:
1. A generic rule, applicable to any appliction (e.g. interacting with clickable elements).
2. A specific rule for a class of applications (e.g. providing email addresses for a text field that is contextualised as a field requiring an email address).
3. Domain specifc rules (e.g. internal account numbers or identifiers, only usable by your internally developed software).

Categories 1. and 2. are typically maintained and provided by the MINT community. You as an end user might want to add rules of category 3. on your own.

An excerpt of the default rules. [See here for a detailed list with all known rules](/android-core/src/main/kotlin/org/mint/android/rule). We welcome you to add or enhance any rule you think is useful, see [contributing](/docs/contributing.md) on how to reach out or provide an actual contribution. 

Any rule can be enabled or disabled on its own. 

#### Navigation rules
* Scroll to and click any widget that is clickable, not yet displayed and can be scrolled to.
* Scroll pager to a direction
* De-prioritized clicks that were already taken historically
* Click on any displayed, clickable widget 

#### Input rules
* Generate generic text for anything accepting text 
* Generate text in email address format for widgets accepting email addresses 
* Generate text for widgets accepting person name input (i.e. names of humans)
* Generate current, future or past date input for widgets accepting input as date 
* Generate postal address input (for geographic regions you are interested in) for widgets accepting input as postal address 
* Generate generic text (e.g. UTF8, Unicode, ...) for anything accepting any type of input 

### Oracles 
After each interaction MINT stores the application state. Oracles observe each state and produces verdicts. An example oracle would be an oracle that detects overlapping text, an error log line or a 'black hole state' (i.e. an application state that cannot be exited).

#### Default oracles 

* STABILITY 
- Oracle that considers the Android (system) log" name 

* PERFORMANCE 
- Android Device Oracle that monitors CPU usage, RAM usage, Network usage, and graphics (GPU). 

* ACCESSIBILITY 
- Oracle that detects missing content descriptions of icons and images. 

An excerpt of the default rules. [See here for a detailed list with all known rules](/android-core/src/main/kotlin/org/mint/android/oracle). We welcome you to add or enhance any rule you think is useful, see [contributing](/docs/contributing.md) on how to reach out or provide an actual contribution. 

Any oracle can be enabled or disabled individually. 
 