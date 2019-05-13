If starting from beginning, you will need to put all the files of the DUC2001_Dataset in Document_Converter\src\main\java\data\Summaries folder.
Run File_Converter.java

The class will output two folders of datasets:
The reference summaries for the Rogue calculator: Document_Converter\src\main\java\data\output folder. 
And the stripped document texts for summarizing: Document_Converter\src\main\java\data\text folder. 


To get the Java summaries: place the stripped documents from Document_Converter\src\main\java\data\text 
into Summarizer\src\main\java\texts 
Run FileSummation.java

To get PyTextRank summaries: Make sure the stripped documents from Document_Converter\src\main\java\data\text are still present
Run TexttoJson.java
The newly converted json files will be at Document_Converter\src\main\java\data\json
Move these json files into PyTextRank\jsonfiles
Run GetSummaries.py
Output will be in: PyTextRank\output

ROUGE calculation
To calculate the Rouge scores you must place the system summaries in: \rouge2.0-0.2-distribute\test-summarization\system
You must place the reference summaries in: \rouge2.0-0.2-distribute\test-summarization\reference
See rouge.properties to configure the N-Gram size.
Run Rouge2.0.jar
Output will be a result.csv file in the same folder.
Repeat for each N-Gram size and implementation

Get Average:
Place Csvs from rouge calculator in Csv_Reader\src\main\java\data\Csvs
run RougeAvgCalc
Output will be in Csv_Reader\src\main\java\data\Averages