Devin Sandhu DSS2255
Sam Speck SXS59
Po-Chen Yang PC22369

Instructions:
  Tap Data Analysis to be taken to the Data Analysis Menu
    There are two spinners at the top, one selects the type of analysis
    and the other the scope of the analysis
    Tapping the date next to FROM and TO lets you limit the analysis
    to messages in that time range
    Tapping the Contacts button opens up a picker to select contacts
    and typing in the text box will autocomplete contact names
    In either case the analysis will only be run on messages with those
    contacts (default is all contacts when none are selected)
    After tapping Analyze the data will be displayed in a bar graph,
    pie graph, and text dump all in a scroll view
  Tap Friend Battle to be taken to the Friend Battle Menu
    Add 2 contacts and pick a time span before tapping battle
    The winner of the battle is the contact from which you have received
    more messages within the time frame specified

Features Completed from Prototype:
  Several analysis types
    Most frequently used words
    Most frequently texted contacts
    Average longest length of message
    Average shortest length of message
  Constraining analyses by a time range
  Constraining analyses to a list of contacts
  Constraining analyses to sent, received, or all messages
  Visualization of data using a bar graph and pie chart
  Basic implementation of friend battle

Incomplete Features from Prototype:
  Several analysis types:
    Time between texts (time to respond)
    Time of day when texting
  Several visualizations
    Line graph
    Scoreboard

Other Features Completed:
  Autocompleting contact names in Data Analysis Menu

Code Referenced:
  Android Plot Graphing Library: http://androidplot.com/

Code Completed Ourselves:
  AnalysisMenuActivity.java
  AnalysisResultActivity.java
  Analyzer.java
  BattleMenuActivity.java
  BattleResultActivity.java
  MainActivity.java
