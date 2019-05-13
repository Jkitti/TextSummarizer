////Author: James Kitti
////Text summarization: Java implementation

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.swing.JFileChooser;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;


public class FileSummation {
	
	public JFileChooser fileChooser;
	public static TokenizerME tokenizer;
	public static SentenceDetectorME sentenceDetect;
	public  String filetext;
	
	public FileSummation()
	{
		fileChooser = new JFileChooser();
		filetext = "";
	}
	
	
	///Compares two collections and returns their intersections.
	public static <T> Collection <T> intersector (Collection <? extends T> a, Collection <? extends T> b)
	{
	    Collection <T> result = new ArrayList <T> ();

	    for (T t: a)
	    {
	        if (b.remove (t)) result.add (t);
	    }

	    return result;
	}
	
	//Intializes opennlp sentence detector
	public void IntializeSentenceDetector(){
		InputStream sentenceModelIS = this.getClass().getResourceAsStream("opennlpdata/en-sent.bin");
		SentenceModel model;
		try 
		{	
			model = new SentenceModel(sentenceModelIS);
			sentenceDetect = new SentenceDetectorME(model);
		} 
		catch (InvalidFormatException e) {	
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//Intializes opennlp tokenizer
	public void IntializeTokenizer(){
		InputStream tokenizerModelIS = this.getClass().getResourceAsStream("opennlpdata/en-token.bin"); 
		TokenizerModel tokenModel;
		try 
		{	
			tokenModel = new TokenizerModel(tokenizerModelIS);
		    tokenizer = new TokenizerME(tokenModel);
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	 
	//Method to open singular text 
	private String OpenFile() {
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		int result = fileChooser.showOpenDialog(null);
	    if (result == JFileChooser.APPROVE_OPTION) {
	        Path path = fileChooser.getSelectedFile().toPath();

	        try {
	        	System.out.println(path);
	        	System.out.println("\n");
	        	filetext = new Scanner(new File(path.toString())).useDelimiter("\\Z").next();
	            }
	        	catch (IOException e) {
	        		e.printStackTrace();
	        	}
	    	}
	    return filetext;
	    }
	
	//Split whole text string to paragraphs
	public static String[] splitToParagraphs(String content)
	{
    	String[] paragraphs = content.split("\n\r\n");
		return paragraphs;
	}
	
	//Split paragraph to sentence array.
	public static String[] splitToSentences(String content)
	{
		
		String[] sent = sentenceDetect.sentDetect(content);
		return sent;
	}
	
	//Retrieves the longest sentence length for score calc.
	public static float getLongestSentenceLength(String[] paragraphs)
	{
		float longest = 0;
    	for (String para : paragraphs)
    	{
    		String[] sentences = splitToSentences(para);
        	for (String sent : sentences)
        	{
        		float words = sent.split(" ").length;
        		if(words > longest){
        			words = longest;
        		}
        	}
    	}
		
		return longest;
	}
	
	///Breaks down paragraph and gets sentence scores.
	public static String getBestSentence(String paragraph, float longest)
	{
		String[] sentences = splitToSentences(paragraph);
		if(sentences == null || sentences.length <= 2)
			return "";
		
		float[][] intersectionMatrix = getSentenceIntersectionMatrix(sentences);
		
		float[] sentenceScores = getSentenceScores(sentences, intersectionMatrix, longest);
		
		return calcBestSentence(sentences,  sentenceScores);
	}
	
	
	//Calculates and returns scores for each sentence.
	private static float[] getSentenceScores(String[] sentences,float[][] scoreMatrix, float longest) {
		float[] scoresReturn = new float[sentences.length];
		
		for(int i=0; i<sentences.length; i++)
		{
			int sentenceScore = 0;
			for(int j=0; j<scoreMatrix[i].length; j++)
			{
				sentenceScore += scoreMatrix[i][j];	
			}
			sentenceScore += sentences[i].split(" ").length/longest;
			sentenceScore += getPositionScore(sentences, (float) i);
			scoresReturn[i] = sentenceScore;
		}
		
		return scoresReturn;
	}
	
	///Returns score based on sentence's position in paragraph.
	private static float getPositionScore(String[] sentences, float i) {
		float position = i/sentences.length;
		float score = 1 - position;
		
		return score;
	}
	
	
	
	//Utility method for array index retrival
	public static String calcBestSentence(String[] sentences, float[] scores)
	{	
		
		return sentences[MaxIndex(scores)];
		
	}
	
	///Get index of best scoring sentence.
	private static int MaxIndex(float[] scores) {
		int maxIndex = 0;
		float max = -1;
		for(int i=0; i<scores.length; i++)
		{
			if(scores[i]>max)
			{
				max = scores[i];
				maxIndex = i;
			}
			
		}
		return maxIndex;
	}

	//Check for word intersection between two sentences.
	public static float checkforIntersection (String sentence1, String sentence2)
	{
		String[] sent1 = tokenizer.tokenize(sentence1);
		String[] sent2 = tokenizer.tokenize(sentence2);
		
		if (sent1.length + sent2.length == 0)
			return 0;
		
		List<String> intersectArray = (List<String>) intersector (new ArrayList<String>(Arrays.asList(sent1)),new ArrayList<String>(Arrays.asList(sent2)));
		
		float result = ((float)(float)intersectArray.size() / ((float)sent1.length + ((float)sent2.length) / 2));
		
		return result;
	}
	
	///Returns 2nd array of all word intersections between sentence in a paragraph.
	private static float[][] getSentenceIntersectionMatrix(String[] sentences) {
		int n = sentences.length;
		float[][] intersectionMatrix= new float[n][n];
		for(int i = 0; i< n; i++)
		{
			for(int j = 0; j< n; j++)
			{
				try
				{
					if(i == j)
						continue;
					
				intersectionMatrix[i][j] = checkforIntersection(sentences[i], sentences[j]);	
				}
				catch(Exception e)
				{
					System.out.println(e.getMessage());
				}
			}
		}
		return intersectionMatrix;
	}
		
	
	///Writes summary to specified file.
	public void writeToSummaryFile(String fileName, StringBuilder summary){
		 try{    
			 FileWriter fw=new FileWriter("C:/Users/JKitt/workspace/Summarizer/src/main/java/summaries/" + fileName + "_syssum1.txt");    
			 fw.write(summary.toString());    
			 fw.close();    
	        }
		 catch(Exception e){
			 System.out.println(e);
	      } 
	}
		

	public static void main(String[] args) throws FileNotFoundException {
		FileSummation f = new FileSummation();
		f.IntializeSentenceDetector();
		f.IntializeTokenizer();
		File dir = new File("C:/Users/JKitt/workspace/Summarizer/src/main/java/texts");
	    File[] directoryListing = dir.listFiles();
	    int i = 0;
	    if (directoryListing != null) {
	    	 for (File child : directoryListing) {
	    		 String childName = "";
	    		 String text = new Scanner(child).useDelimiter("\\Z").next();;
	    		 String[] paragraphs = f.splitToParagraphs(text);
	    		 float longest = getLongestSentenceLength(paragraphs);
	    		 StringBuilder summary = new StringBuilder();
	    		 for(String p : paragraphs)
	    		 {
	    			 String bestSent = getBestSentence(p,longest);
	    			 if(bestSent != null && bestSent.length() > 0)
	    				 summary.append(bestSent + '\n');
	    		 }
		    	 StringTokenizer nt = new StringTokenizer(child.getName(), ",.:?! \t\n\r" );
		    	  while (nt.hasMoreTokens()) {
		    		  String nametoken = (String) nt.nextToken();
	    			     if(nametoken.equals("txt")){
	    			         break;
	    			     }
	    			     else{
	    			    	 childName = childName + nametoken;
	    			     }
		    	  }
		    	  f.writeToSummaryFile(childName, summary);
	    		 i+=1;
	    	System.out.println(summary);
	    }
	   }

	}

}
