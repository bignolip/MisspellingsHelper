import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

/*
 * Arguments:
 * 
 * 1.  Input file path
 * 2.  Output file path
 * 
 * File format:
 * 
 * Generation Options
 * Word 1 [special misspelling 1, ..., special misspelling a], Inflection 1 [special misspelling 1, ..., special misspelling b], ..., Inflection i [special misspelling 1, ..., special misspelling c]  
 * ...
 * Word j, ...
 * 
 */
public class MisspellingsSectionGenerator 
{

	private static int num_qwerty_rows = 3;
	private static char[][] qwerty_key_array = new char[3][];
	private static Map<Character, int[]> letter_to_qwerty_row_and_column = new HashMap<Character, int[]>();
	private static int[] qwerty_key_array_lengths;
	
	static 
	{
		MisspellingsSectionGenerator.qwerty_key_array[0] = new char[]{'q','w','e','r','t','y','u','i','o','p'};
		MisspellingsSectionGenerator.qwerty_key_array[1] = new char[]{'a','s','d','f','g','h','j','k','l'};
		MisspellingsSectionGenerator.qwerty_key_array[2] = new char[]{'z','x','c','v','b','n','m'};
		
		MisspellingsSectionGenerator.letter_to_qwerty_row_and_column.put('a', new int[]{1, 0});
		MisspellingsSectionGenerator.letter_to_qwerty_row_and_column.put('b', new int[]{2, 4});
		MisspellingsSectionGenerator.letter_to_qwerty_row_and_column.put('c', new int[]{2, 2});
		MisspellingsSectionGenerator.letter_to_qwerty_row_and_column.put('d', new int[]{1, 2});
		MisspellingsSectionGenerator.letter_to_qwerty_row_and_column.put('e', new int[]{0, 2});
		MisspellingsSectionGenerator.letter_to_qwerty_row_and_column.put('f', new int[]{1, 3});
		MisspellingsSectionGenerator.letter_to_qwerty_row_and_column.put('g', new int[]{1, 4});
		MisspellingsSectionGenerator.letter_to_qwerty_row_and_column.put('h', new int[]{1, 5});
		MisspellingsSectionGenerator.letter_to_qwerty_row_and_column.put('i', new int[]{0, 7});
		MisspellingsSectionGenerator.letter_to_qwerty_row_and_column.put('j', new int[]{1, 6});
		MisspellingsSectionGenerator.letter_to_qwerty_row_and_column.put('k', new int[]{1, 7});
		MisspellingsSectionGenerator.letter_to_qwerty_row_and_column.put('l', new int[]{1, 8});
		MisspellingsSectionGenerator.letter_to_qwerty_row_and_column.put('m', new int[]{2, 6});
		MisspellingsSectionGenerator.letter_to_qwerty_row_and_column.put('n', new int[]{2, 5});
		MisspellingsSectionGenerator.letter_to_qwerty_row_and_column.put('o', new int[]{0, 8});
		MisspellingsSectionGenerator.letter_to_qwerty_row_and_column.put('p', new int[]{0, 9});
		MisspellingsSectionGenerator.letter_to_qwerty_row_and_column.put('q', new int[]{0, 0});
		MisspellingsSectionGenerator.letter_to_qwerty_row_and_column.put('r', new int[]{0, 3});
		MisspellingsSectionGenerator.letter_to_qwerty_row_and_column.put('s', new int[]{1, 1});
		MisspellingsSectionGenerator.letter_to_qwerty_row_and_column.put('t', new int[]{0, 4});
		MisspellingsSectionGenerator.letter_to_qwerty_row_and_column.put('u', new int[]{0, 6});
		MisspellingsSectionGenerator.letter_to_qwerty_row_and_column.put('v', new int[]{2, 3});
		MisspellingsSectionGenerator.letter_to_qwerty_row_and_column.put('w', new int[]{0, 1});
		MisspellingsSectionGenerator.letter_to_qwerty_row_and_column.put('x', new int[]{2, 1});
		MisspellingsSectionGenerator.letter_to_qwerty_row_and_column.put('y', new int[]{0, 5});
		MisspellingsSectionGenerator.letter_to_qwerty_row_and_column.put('z', new int[]{2, 0});
		
		MisspellingsSectionGenerator.qwerty_key_array_lengths = 
				new int[]{
					MisspellingsSectionGenerator.qwerty_key_array[0].length, 
					MisspellingsSectionGenerator.qwerty_key_array[1].length, 
					MisspellingsSectionGenerator.qwerty_key_array[2].length};
	};
	
	private static File inFile; 
	private static File outFile;
	
	private static boolean shouldGenerateDoubleKeyPresses;
	private static boolean shouldGenerateMissedKeyPresses;
	private static boolean shouldGenerateWrongKeyReplacementPresses;
	private static boolean shouldGenerateWrongKeyInsertionPresses;
	private static boolean shouldGenerateTranscribedKeyPresses;
	private static boolean shouldGenerateDeletedSpaces;
	
	private static void setGenerationFlags(String generationOptions)
	{
		MisspellingsSectionGenerator.shouldGenerateDoubleKeyPresses = 
			generationOptions.contains("-dkp");
		MisspellingsSectionGenerator.shouldGenerateMissedKeyPresses = 
			generationOptions.contains("-mkp");
		MisspellingsSectionGenerator.shouldGenerateWrongKeyReplacementPresses =
			generationOptions.contains("-wkrp");
		MisspellingsSectionGenerator.shouldGenerateWrongKeyInsertionPresses =
			generationOptions.contains("-wkip");
		MisspellingsSectionGenerator.shouldGenerateTranscribedKeyPresses = 
			generationOptions.contains("-tkp");
		MisspellingsSectionGenerator.shouldGenerateDeletedSpaces =
			generationOptions.contains("-ds");
	}
	
	public static int getDamerauLevenshteinDistance(String source, String target)
	{
	    if ((source == null) || source.isEmpty())
	    {
	        if ((target == null) || target.isEmpty())
	        {
	            return 0;
	        }
	        else
	        {
	            return target.length();
	        }
	    }
	    else if ((target == null) || target.isEmpty())
	    {
	        return source.length();
	    } 
	 
	    int[][] score = new int[source.length() + 2][target.length() + 2];
	 
	    int inf = source.length() + target.length();
	    score[0][0] = inf;
	    
	    for (int i = 0; i <= source.length(); i++) 
	    { 
	    	score[i + 1][1] = i; 
	    	score[i + 1][0] = inf; 
	    }
	    for (int j = 0; j <= target.length(); j++) 
	    { 
	    	score[1][j + 1] = j; 
	    	score[0][j + 1] = inf; 
	    }
	 
	    TreeMap<Character, Integer> treeMap = new TreeMap<Character, Integer>();
	    
	    for (Character letter : (source + target).toCharArray())
	    {
	        if (!treeMap.containsKey(letter))
	            treeMap.put(letter, 0);
	    }
	 
	    
	    for (int i = 1; i <= source.length(); i++)
	    {
	    	int curScore = 0;
	        
	        for (int j = 1; j <= target.length(); j++)
	        {
	            int i1 = treeMap.get(target.charAt(j - 1));
	            int j1 = curScore;
	 
	            if (source.charAt(i - 1) == target.charAt(j - 1))
	            {
	                score[i + 1][j + 1] = score[i][j];
	                curScore = j;
	            }
	            else
	            {
	                score[i + 1][j + 1] = Math.min(score[i][j], Math.min(score[i + 1][j], score[i][j + 1])) + 1;
	            }
	 
	            score[i + 1][j + 1] = Math.min(score[i + 1][j + 1], score[i1][j1] + (i - i1 - 1) + 1 + (j - j1 - 1));
	        }
	 
	        treeMap.put(source.charAt(i - 1),  i);
	    }
	 
	    return score[source.length() + 1][target.length() + 1];
	}
	
	private static String getClosestCorrectSpellingForMisspelling(
			String generatedMisspelling,
			String correctlySpelledWord,
			Map<String, String> totalMisspellingToCorrectSpelling,
			Set<String> totalCorrectSpellings,
			Map<String, String> inflectionToBaseWord,
			Map<String, Set<String>> basewordToInflections)
	{
		String curCorrectBaseWord;
		Set<String> curCorrectInflections; 
		
		int differenceBetweenBaseWordAndMisspelling;
		int curLowestDifference;
		int curDifferenceBetweenInflectionWordAndMisspelling;
		
		String curClosestWordForm;
		
		// If this misspelling has not yet been generated and is not actually
		// a correct spelling of any base word or inflection
		if (!totalMisspellingToCorrectSpelling.containsKey(generatedMisspelling)
				&& !totalCorrectSpellings.contains(generatedMisspelling))
		{
			// Look through the list of inflections to see which has the maximum
			// similarity to this misspelling
			//
			// Note:  this ignores the possibility that a misspelling might be closer
			//        to another base word than it is from the base word that it is
			//        to the base word that generated it.
			
			curCorrectBaseWord = inflectionToBaseWord.get(correctlySpelledWord);
			
			if (curCorrectBaseWord == null)
			{
				curCorrectBaseWord = correctlySpelledWord;
			}
			
			curCorrectInflections = basewordToInflections.get(curCorrectBaseWord);
			
			// Get the difference between the generated misspelling and the baseword form
			differenceBetweenBaseWordAndMisspelling =
					MisspellingsSectionGenerator.getDamerauLevenshteinDistance(
						curCorrectBaseWord, generatedMisspelling);
			
			if (differenceBetweenBaseWordAndMisspelling > 1)
			{
				curClosestWordForm = curCorrectBaseWord;
				curLowestDifference = differenceBetweenBaseWordAndMisspelling;
				
				for (String curInflectionForm : curCorrectInflections)
				{
					curDifferenceBetweenInflectionWordAndMisspelling =
						MisspellingsSectionGenerator.getDamerauLevenshteinDistance(
							curInflectionForm, generatedMisspelling);
					
					if (curDifferenceBetweenInflectionWordAndMisspelling < curLowestDifference)
					{
						curLowestDifference = curDifferenceBetweenInflectionWordAndMisspelling;
						curClosestWordForm = curInflectionForm;
					}
				}
				
				return curClosestWordForm;
			}
			else
			{
				return curCorrectBaseWord;
			}
		}
		
		return null;
	}
	
	private static void generateMisspelligsSection()
	{
		try
		{
			// Open the input file stream
			BufferedReader inputStream = new BufferedReader(new FileReader(MisspellingsSectionGenerator.inFile));

			// Open the output file stream
			BufferedWriter outputStream = new BufferedWriter(new FileWriter(MisspellingsSectionGenerator.outFile));

			// The first line contains the misspelling generator options
			String misspellingsOptionsString = inputStream.readLine();

			// Set the misspelling generation flags
			MisspellingsSectionGenerator.setGenerationFlags(misspellingsOptionsString);

			// Input the list of lexical entries, inflections, and any forced misspellings
			String curLine = inputStream.readLine();

			Set<String> totalCorrectSpellings = new HashSet<String>();
			Map<String, Set<String>> basewordToInflections = new HashMap<String, Set<String>>();
			Map<String, String> inflectionToBaseWord = new HashMap<String, String>();
			Map<String, String> correctSpellingToForcedMisspelling = new HashMap<String, String>();
			Map<String, String> forcedMisspellingToCorrectSpelling = new HashMap<String, String>();

			StringTokenizer inflectionLevelTokens;
			StringTokenizer misspellingLevelTokens;

			String curInflectionLevelToken;
			String curMisspellingLevelToken;

			String curBaseWord = null;
			String curCorrectSpelling = null;
			String curForcedMisspelling;

			int inflectionCount;
			int misspellingCount;

			Set<String> curInflectionSet = null;

			while (curLine != null)
			{
				inflectionCount = 0;

				inflectionLevelTokens = new StringTokenizer(curLine,"]");

				while (inflectionLevelTokens.hasMoreElements())
				{		
					curInflectionLevelToken = inflectionLevelTokens.nextToken();

					misspellingLevelTokens = new StringTokenizer(curInflectionLevelToken,"[,");

					misspellingCount = 0;

					while (misspellingLevelTokens.hasMoreElements())
					{
						curMisspellingLevelToken = misspellingLevelTokens.nextToken().trim();

						// The first token is the correctly spelled word
						if (misspellingCount == 0)
						{
							curCorrectSpelling = curMisspellingLevelToken;

							totalCorrectSpellings.add(curCorrectSpelling);

							// The first grouping is for the base form of a word
							if (inflectionCount == 0)
							{
								curInflectionSet = new HashSet<String>();

								curBaseWord = curMisspellingLevelToken;

								basewordToInflections.put(curBaseWord, curInflectionSet);
							}
							else
							{
								curInflectionSet.add(curMisspellingLevelToken);

								inflectionToBaseWord.put(curMisspellingLevelToken, curBaseWord);
							}
						}
						else
						{
							curForcedMisspelling = curMisspellingLevelToken;

							correctSpellingToForcedMisspelling.put(curCorrectSpelling, curForcedMisspelling);
							forcedMisspellingToCorrectSpelling.put(curForcedMisspelling, curCorrectSpelling);
						}

						misspellingCount++;
					}

					inflectionCount++;
				}

				curLine = inputStream.readLine();
			}

			inputStream.close();

			// Create the set of misspellings
			String curGeneratedMisspelling;
			String curClosestWordForm;

			Map<String, String> totalMisspellingToCorrectSpelling = 
				new HashMap<String, String>(forcedMisspellingToCorrectSpelling);

			for (String curCorrectlySpelledWord : totalCorrectSpellings) 
			{
				if (MisspellingsSectionGenerator.shouldGenerateDoubleKeyPresses)
				{
					for (int i = 0; i < curCorrectlySpelledWord.length(); i++)
					{
						curGeneratedMisspelling = "";

						for (int j = 0 ; j < curCorrectlySpelledWord.length(); j++)
						{
							curGeneratedMisspelling += curCorrectlySpelledWord.charAt(j);

							if (j == i)
							{
								curGeneratedMisspelling += curCorrectlySpelledWord.charAt(j);
							}
						}

						// Determine what version of the correctly spelled word is closest to the misspelling
						curClosestWordForm = 
							MisspellingsSectionGenerator.getClosestCorrectSpellingForMisspelling(
								curGeneratedMisspelling, curCorrectlySpelledWord, 
								totalMisspellingToCorrectSpelling, totalCorrectSpellings, 
								inflectionToBaseWord, basewordToInflections);
						
						if (curClosestWordForm != null)
						{
							totalMisspellingToCorrectSpelling.put(curGeneratedMisspelling, curClosestWordForm);
						}
					}
				}
				if (MisspellingsSectionGenerator.shouldGenerateMissedKeyPresses)
				{
					for (int i = 0; i < curCorrectlySpelledWord.length(); i++)
					{
						curGeneratedMisspelling = "";

						for (int j = 0 ; j < curCorrectlySpelledWord.length(); j++)
						{
							if (j != i)
							{
								curGeneratedMisspelling += curCorrectlySpelledWord.charAt(j);
							}
						}

						// Determine what version of the correctly spelled word is closest to the misspelling
						curClosestWordForm = 
							MisspellingsSectionGenerator.getClosestCorrectSpellingForMisspelling(
								curGeneratedMisspelling, curCorrectlySpelledWord, 
								totalMisspellingToCorrectSpelling, totalCorrectSpellings, 
								inflectionToBaseWord, basewordToInflections);
						
						if (curClosestWordForm != null)
						{
							totalMisspellingToCorrectSpelling.put(curGeneratedMisspelling, curClosestWordForm);
						}
					}
				}
				/*
				 * NOTE:  Will not work if '*' is allowed in a vocabulary word
				 */
				if (MisspellingsSectionGenerator.shouldGenerateWrongKeyReplacementPresses)
				{
					char curCharToBeReplaced = ' ';
					
					for (int i = 0; i < curCorrectlySpelledWord.length(); i++)
					{
						curGeneratedMisspelling = "";

						for (int j = 0 ; j < curCorrectlySpelledWord.length(); j++)
						{
							if (j == i)
							{
								curGeneratedMisspelling += "*";
								
								curCharToBeReplaced = curCorrectlySpelledWord.charAt(j);
							}
							else
							{
								curGeneratedMisspelling += curCorrectlySpelledWord.charAt(j);
							}
						}
						
						// Get the qwerty location of the letter to be replaced
						int[] rowAndCol = MisspellingsSectionGenerator.letter_to_qwerty_row_and_column.get(
								curCharToBeReplaced);
						
						char curReplacement;
						String curMisspellingWithReplacement;
						
						if ((rowAndCol == null) && curCharToBeReplaced == ' ')
						{
							curReplacement = 'x';
							
							curMisspellingWithReplacement = curGeneratedMisspelling.replace('*', curReplacement);
							
							// Determine what version of the correctly spelled word is closest to the misspelling
							curClosestWordForm = 
								MisspellingsSectionGenerator.getClosestCorrectSpellingForMisspelling(
									curMisspellingWithReplacement, curCorrectlySpelledWord, 
									totalMisspellingToCorrectSpelling, totalCorrectSpellings, 
									inflectionToBaseWord, basewordToInflections);
							
							if (curClosestWordForm != null)
							{
								totalMisspellingToCorrectSpelling.put(curMisspellingWithReplacement, curClosestWordForm);
							}
							
							curReplacement = 'c';
							
							curMisspellingWithReplacement = curGeneratedMisspelling.replace('*', curReplacement);
							
							// Determine what version of the correctly spelled word is closest to the misspelling
							curClosestWordForm = 
								MisspellingsSectionGenerator.getClosestCorrectSpellingForMisspelling(
									curMisspellingWithReplacement, curCorrectlySpelledWord, 
									totalMisspellingToCorrectSpelling, totalCorrectSpellings, 
									inflectionToBaseWord, basewordToInflections);
							
							if (curClosestWordForm != null)
							{
								totalMisspellingToCorrectSpelling.put(curMisspellingWithReplacement, curClosestWordForm);
							}
							
							curReplacement = 'v';
							
							curMisspellingWithReplacement = curGeneratedMisspelling.replace('*', curReplacement);
							
							// Determine what version of the correctly spelled word is closest to the misspelling
							curClosestWordForm = 
								MisspellingsSectionGenerator.getClosestCorrectSpellingForMisspelling(
									curMisspellingWithReplacement, curCorrectlySpelledWord, 
									totalMisspellingToCorrectSpelling, totalCorrectSpellings, 
									inflectionToBaseWord, basewordToInflections);
							
							if (curClosestWordForm != null)
							{
								totalMisspellingToCorrectSpelling.put(curMisspellingWithReplacement, curClosestWordForm);
							}
							
							curReplacement = 'b';
							
							curMisspellingWithReplacement = curGeneratedMisspelling.replace('*', curReplacement);
							
							// Determine what version of the correctly spelled word is closest to the misspelling
							curClosestWordForm = 
								MisspellingsSectionGenerator.getClosestCorrectSpellingForMisspelling(
									curMisspellingWithReplacement, curCorrectlySpelledWord, 
									totalMisspellingToCorrectSpelling, totalCorrectSpellings, 
									inflectionToBaseWord, basewordToInflections);
							
							if (curClosestWordForm != null)
							{
								totalMisspellingToCorrectSpelling.put(curMisspellingWithReplacement, curClosestWordForm);
							}
							
							curReplacement = 'n';
							
							curMisspellingWithReplacement = curGeneratedMisspelling.replace('*', curReplacement);
							
							// Determine what version of the correctly spelled word is closest to the misspelling
							curClosestWordForm = 
								MisspellingsSectionGenerator.getClosestCorrectSpellingForMisspelling(
									curMisspellingWithReplacement, curCorrectlySpelledWord, 
									totalMisspellingToCorrectSpelling, totalCorrectSpellings, 
									inflectionToBaseWord, basewordToInflections);
							
							if (curClosestWordForm != null)
							{
								totalMisspellingToCorrectSpelling.put(curMisspellingWithReplacement, curClosestWordForm);
							}
							
							curReplacement = 'm';
							
							curMisspellingWithReplacement = curGeneratedMisspelling.replace('*', curReplacement);
							
							// Determine what version of the correctly spelled word is closest to the misspelling
							curClosestWordForm = 
								MisspellingsSectionGenerator.getClosestCorrectSpellingForMisspelling(
									curMisspellingWithReplacement, curCorrectlySpelledWord, 
									totalMisspellingToCorrectSpelling, totalCorrectSpellings, 
									inflectionToBaseWord, basewordToInflections);
							
							if (curClosestWordForm != null)
							{
								totalMisspellingToCorrectSpelling.put(curMisspellingWithReplacement, curClosestWordForm);
							}
							
							continue;
						}
						
						int row = rowAndCol[0];
						int col = rowAndCol[1];
						
						int curNewRow;
						int curNewCol;
						
						curNewRow = row - 1;
						
						if (curNewRow >= 0)
						{
							curNewCol = col - 1;
							
							if (curNewCol >= 0)
							{
								curReplacement = MisspellingsSectionGenerator.qwerty_key_array[curNewRow][curNewCol];
								
								curMisspellingWithReplacement = curGeneratedMisspelling.replace('*', curReplacement);
								
								// Determine what version of the correctly spelled word is closest to the misspelling
								curClosestWordForm = 
									MisspellingsSectionGenerator.getClosestCorrectSpellingForMisspelling(
										curMisspellingWithReplacement, curCorrectlySpelledWord, 
										totalMisspellingToCorrectSpelling, totalCorrectSpellings, 
										inflectionToBaseWord, basewordToInflections);
								
								if (curClosestWordForm != null)
								{
									totalMisspellingToCorrectSpelling.put(curMisspellingWithReplacement, curClosestWordForm);
								}
							}
							
							curNewCol = col + 1;
							
							if (curNewCol < MisspellingsSectionGenerator.qwerty_key_array_lengths[curNewRow])
							{
								curReplacement = MisspellingsSectionGenerator.qwerty_key_array[curNewRow][curNewCol];
								
								curMisspellingWithReplacement = curGeneratedMisspelling.replace('*', curReplacement);
								
								// Determine what version of the correctly spelled word is closest to the misspelling
								curClosestWordForm = 
									MisspellingsSectionGenerator.getClosestCorrectSpellingForMisspelling(
										curMisspellingWithReplacement, curCorrectlySpelledWord, 
										totalMisspellingToCorrectSpelling, totalCorrectSpellings, 
										inflectionToBaseWord, basewordToInflections);
								
								if (curClosestWordForm != null)
								{
									totalMisspellingToCorrectSpelling.put(curMisspellingWithReplacement, curClosestWordForm);
								}
							}
							
							curNewCol = col;
							
							if (curNewCol < MisspellingsSectionGenerator.qwerty_key_array_lengths[curNewRow])
							{
								curReplacement = MisspellingsSectionGenerator.qwerty_key_array[curNewRow][curNewCol];
								
								curMisspellingWithReplacement = curGeneratedMisspelling.replace('*', curReplacement);
								
								// Determine what version of the correctly spelled word is closest to the misspelling
								curClosestWordForm = 
									MisspellingsSectionGenerator.getClosestCorrectSpellingForMisspelling(
										curMisspellingWithReplacement, curCorrectlySpelledWord, 
										totalMisspellingToCorrectSpelling, totalCorrectSpellings, 
										inflectionToBaseWord, basewordToInflections);
								
								if (curClosestWordForm != null)
								{
									totalMisspellingToCorrectSpelling.put(curMisspellingWithReplacement, curClosestWordForm);
								}
							}
						}
						
						curNewRow = row + 1;
						
						if (curNewRow  < MisspellingsSectionGenerator.num_qwerty_rows)
						{
							curNewCol = col - 1;
							
							if ((curNewCol >= 0) && (curNewCol < MisspellingsSectionGenerator.qwerty_key_array_lengths[curNewRow]))
							{
								curReplacement = MisspellingsSectionGenerator.qwerty_key_array[curNewRow][curNewCol];
								
								curMisspellingWithReplacement = curGeneratedMisspelling.replace('*', curReplacement);
								
								// Determine what version of the correctly spelled word is closest to the misspelling
								curClosestWordForm = 
									MisspellingsSectionGenerator.getClosestCorrectSpellingForMisspelling(
										curMisspellingWithReplacement, curCorrectlySpelledWord, 
										totalMisspellingToCorrectSpelling, totalCorrectSpellings, 
										inflectionToBaseWord, basewordToInflections);
								
								if (curClosestWordForm != null)
								{
									totalMisspellingToCorrectSpelling.put(curMisspellingWithReplacement, curClosestWordForm);
								}
							}
							
							curNewCol = col + 1;
							
							if (curNewCol < MisspellingsSectionGenerator.qwerty_key_array_lengths[curNewRow])
							{
								curReplacement = MisspellingsSectionGenerator.qwerty_key_array[curNewRow][curNewCol];
								
								curMisspellingWithReplacement = curGeneratedMisspelling.replace('*', curReplacement);
								
								// Determine what version of the correctly spelled word is closest to the misspelling
								curClosestWordForm = 
									MisspellingsSectionGenerator.getClosestCorrectSpellingForMisspelling(
										curMisspellingWithReplacement, curCorrectlySpelledWord, 
										totalMisspellingToCorrectSpelling, totalCorrectSpellings, 
										inflectionToBaseWord, basewordToInflections);
								
								if (curClosestWordForm != null)
								{
									totalMisspellingToCorrectSpelling.put(curMisspellingWithReplacement, curClosestWordForm);
								}
							}
							
							curNewCol = col;
							
							if (curNewCol < MisspellingsSectionGenerator.qwerty_key_array_lengths[curNewRow])
							{
								curReplacement = MisspellingsSectionGenerator.qwerty_key_array[curNewRow][curNewCol];
								
								curMisspellingWithReplacement = curGeneratedMisspelling.replace('*', curReplacement);
								
								// Determine what version of the correctly spelled word is closest to the misspelling
								curClosestWordForm = 
									MisspellingsSectionGenerator.getClosestCorrectSpellingForMisspelling(
										curMisspellingWithReplacement, curCorrectlySpelledWord, 
										totalMisspellingToCorrectSpelling, totalCorrectSpellings, 
										inflectionToBaseWord, basewordToInflections);
								
								if (curClosestWordForm != null)
								{
									totalMisspellingToCorrectSpelling.put(curMisspellingWithReplacement, curClosestWordForm);
								}
							}
						}
						
						curNewRow = row;
						
						curNewCol = col - 1;
						
						if ((curNewCol >= 0) && (curNewCol < MisspellingsSectionGenerator.qwerty_key_array_lengths[curNewRow]))
						{
							curReplacement = MisspellingsSectionGenerator.qwerty_key_array[curNewRow][curNewCol];
							
							curMisspellingWithReplacement = curGeneratedMisspelling.replace('*', curReplacement);
							
							// Determine what version of the correctly spelled word is closest to the misspelling
							curClosestWordForm = 
								MisspellingsSectionGenerator.getClosestCorrectSpellingForMisspelling(
									curMisspellingWithReplacement, curCorrectlySpelledWord, 
									totalMisspellingToCorrectSpelling, totalCorrectSpellings, 
									inflectionToBaseWord, basewordToInflections);
							
							if (curClosestWordForm != null)
							{
								totalMisspellingToCorrectSpelling.put(curMisspellingWithReplacement, curClosestWordForm);
							}
						}
						
						curNewCol = col + 1;
						
						if (curNewCol < MisspellingsSectionGenerator.qwerty_key_array_lengths[curNewRow])
						{
							curReplacement = MisspellingsSectionGenerator.qwerty_key_array[curNewRow][curNewCol];
							
							curMisspellingWithReplacement = curGeneratedMisspelling.replace('*', curReplacement);
							
							// Determine what version of the correctly spelled word is closest to the misspelling
							curClosestWordForm = 
								MisspellingsSectionGenerator.getClosestCorrectSpellingForMisspelling(
									curMisspellingWithReplacement, curCorrectlySpelledWord, 
									totalMisspellingToCorrectSpelling, totalCorrectSpellings, 
									inflectionToBaseWord, basewordToInflections);
							
							if (curClosestWordForm != null)
							{
								totalMisspellingToCorrectSpelling.put(curMisspellingWithReplacement, curClosestWordForm);
							}
						}
					}
				}
				/*
				 * NOTE:  Will not work if '*' or '|' is allowed in a vocabulary word
				 */
				if (MisspellingsSectionGenerator.shouldGenerateWrongKeyInsertionPresses)
				{
					char[] charToBeReplaced = {'*', '|'};
					
					String curGeneratedMisspellingWithBothWildcards;
					
					for (int i = 0; i < curCorrectlySpelledWord.length(); i++)
					{
						curGeneratedMisspellingWithBothWildcards = "";

						for (int j = 0 ; j < curCorrectlySpelledWord.length(); j++)
						{
							if (j == i)
							{
								curGeneratedMisspellingWithBothWildcards += charToBeReplaced[0];
								
								curGeneratedMisspellingWithBothWildcards += curCorrectlySpelledWord.charAt(j);
								
								curGeneratedMisspellingWithBothWildcards += charToBeReplaced[1];
							}
							else
							{
								curGeneratedMisspellingWithBothWildcards += curCorrectlySpelledWord.charAt(j);
							}
						}
						
						char curCharToBeMistyped;
						int positionOfFirstWildCard = curGeneratedMisspellingWithBothWildcards.indexOf(charToBeReplaced[0]);
						int positionOfSecondWildCard = curGeneratedMisspellingWithBothWildcards.indexOf(charToBeReplaced[1]);
						
						String curFirstSegment;
						String curSecondSegment;
						
						for (int position = 0; position < 2 ; position++)
						{
							curCharToBeMistyped = curGeneratedMisspellingWithBothWildcards.charAt(positionOfFirstWildCard+1);
							
							if (position == 0)
							{
								curFirstSegment = curGeneratedMisspellingWithBothWildcards.substring(
										0, positionOfSecondWildCard);
								curSecondSegment = curGeneratedMisspellingWithBothWildcards.substring(positionOfSecondWildCard+1, curGeneratedMisspellingWithBothWildcards.length());
								
								curGeneratedMisspelling = 
										curFirstSegment + curSecondSegment;
							}
							else
							{
								curFirstSegment = curGeneratedMisspellingWithBothWildcards.substring(
										0, positionOfFirstWildCard);
								curSecondSegment = curGeneratedMisspellingWithBothWildcards.substring(positionOfFirstWildCard+1, curGeneratedMisspellingWithBothWildcards.length());
								
								curGeneratedMisspelling = 
										curFirstSegment + curSecondSegment;
							}
							
							// Get the qwerty location of the letter to be replaced
							int[] rowAndCol = MisspellingsSectionGenerator.letter_to_qwerty_row_and_column.get(
									curCharToBeMistyped);
							
							char curReplacement;
							String curMisspellingWithReplacement;
							
							if ((rowAndCol == null) && curCharToBeMistyped == ' ')
							{
								curReplacement = 'x';
								
								curMisspellingWithReplacement = curGeneratedMisspelling.replace(charToBeReplaced[position], curReplacement);
								
								// Determine what version of the correctly spelled word is closest to the misspelling
								curClosestWordForm = 
									MisspellingsSectionGenerator.getClosestCorrectSpellingForMisspelling(
										curMisspellingWithReplacement, curCorrectlySpelledWord, 
										totalMisspellingToCorrectSpelling, totalCorrectSpellings, 
										inflectionToBaseWord, basewordToInflections);
								
								if (curClosestWordForm != null)
								{
									totalMisspellingToCorrectSpelling.put(curMisspellingWithReplacement, curClosestWordForm);
								}
								
								curReplacement = 'c';
								
								curMisspellingWithReplacement = curGeneratedMisspelling.replace(charToBeReplaced[position], curReplacement);
								
								// Determine what version of the correctly spelled word is closest to the misspelling
								curClosestWordForm = 
									MisspellingsSectionGenerator.getClosestCorrectSpellingForMisspelling(
										curMisspellingWithReplacement, curCorrectlySpelledWord, 
										totalMisspellingToCorrectSpelling, totalCorrectSpellings, 
										inflectionToBaseWord, basewordToInflections);
								
								if (curClosestWordForm != null)
								{
									totalMisspellingToCorrectSpelling.put(curMisspellingWithReplacement, curClosestWordForm);
								}
								
								curReplacement = 'v';
								
								curMisspellingWithReplacement = curGeneratedMisspelling.replace(charToBeReplaced[position], curReplacement);
								
								// Determine what version of the correctly spelled word is closest to the misspelling
								curClosestWordForm = 
									MisspellingsSectionGenerator.getClosestCorrectSpellingForMisspelling(
										curMisspellingWithReplacement, curCorrectlySpelledWord, 
										totalMisspellingToCorrectSpelling, totalCorrectSpellings, 
										inflectionToBaseWord, basewordToInflections);
								
								if (curClosestWordForm != null)
								{
									totalMisspellingToCorrectSpelling.put(curMisspellingWithReplacement, curClosestWordForm);
								}
								
								curReplacement = 'b';
								
								curMisspellingWithReplacement = curGeneratedMisspelling.replace(charToBeReplaced[position], curReplacement);
								
								// Determine what version of the correctly spelled word is closest to the misspelling
								curClosestWordForm = 
									MisspellingsSectionGenerator.getClosestCorrectSpellingForMisspelling(
										curMisspellingWithReplacement, curCorrectlySpelledWord, 
										totalMisspellingToCorrectSpelling, totalCorrectSpellings, 
										inflectionToBaseWord, basewordToInflections);
								
								if (curClosestWordForm != null)
								{
									totalMisspellingToCorrectSpelling.put(curMisspellingWithReplacement, curClosestWordForm);
								}
								
								curReplacement = 'n';
								
								curMisspellingWithReplacement = curGeneratedMisspelling.replace(charToBeReplaced[position], curReplacement);
								
								// Determine what version of the correctly spelled word is closest to the misspelling
								curClosestWordForm = 
									MisspellingsSectionGenerator.getClosestCorrectSpellingForMisspelling(
										curMisspellingWithReplacement, curCorrectlySpelledWord, 
										totalMisspellingToCorrectSpelling, totalCorrectSpellings, 
										inflectionToBaseWord, basewordToInflections);
								
								if (curClosestWordForm != null)
								{
									totalMisspellingToCorrectSpelling.put(curMisspellingWithReplacement, curClosestWordForm);
								}
								
								curReplacement = 'm';
								
								curMisspellingWithReplacement = curGeneratedMisspelling.replace(charToBeReplaced[position], curReplacement);
								
								// Determine what version of the correctly spelled word is closest to the misspelling
								curClosestWordForm = 
									MisspellingsSectionGenerator.getClosestCorrectSpellingForMisspelling(
										curMisspellingWithReplacement, curCorrectlySpelledWord, 
										totalMisspellingToCorrectSpelling, totalCorrectSpellings, 
										inflectionToBaseWord, basewordToInflections);
								
								if (curClosestWordForm != null)
								{
									totalMisspellingToCorrectSpelling.put(curMisspellingWithReplacement, curClosestWordForm);
								}
								
								continue;
							}
							
							int row = rowAndCol[0];
							int col = rowAndCol[1];
							
							int curNewRow;
							int curNewCol;
							
							curNewRow = row - 1;
							
							if (curNewRow >= 0)
							{
								curNewCol = col - 1;
								
								if ((curNewCol >= 0) && (curNewCol < MisspellingsSectionGenerator.qwerty_key_array_lengths[curNewRow]))
								{
									curReplacement = MisspellingsSectionGenerator.qwerty_key_array[curNewRow][curNewCol];
									
									curMisspellingWithReplacement = curGeneratedMisspelling.replace(charToBeReplaced[position], curReplacement);
									
									// Determine what version of the correctly spelled word is closest to the misspelling
									curClosestWordForm = 
										MisspellingsSectionGenerator.getClosestCorrectSpellingForMisspelling(
											curMisspellingWithReplacement, curCorrectlySpelledWord, 
											totalMisspellingToCorrectSpelling, totalCorrectSpellings, 
											inflectionToBaseWord, basewordToInflections);
									
									if (curClosestWordForm != null)
									{
										totalMisspellingToCorrectSpelling.put(curMisspellingWithReplacement, curClosestWordForm);
									}
								}
								
								curNewCol = col + 1;
								
								if (curNewCol < MisspellingsSectionGenerator.qwerty_key_array_lengths[curNewRow])
								{
									curReplacement = MisspellingsSectionGenerator.qwerty_key_array[curNewRow][curNewCol];
									
									curMisspellingWithReplacement = curGeneratedMisspelling.replace(charToBeReplaced[position], curReplacement);
									
									// Determine what version of the correctly spelled word is closest to the misspelling
									curClosestWordForm = 
										MisspellingsSectionGenerator.getClosestCorrectSpellingForMisspelling(
											curMisspellingWithReplacement, curCorrectlySpelledWord, 
											totalMisspellingToCorrectSpelling, totalCorrectSpellings, 
											inflectionToBaseWord, basewordToInflections);
									
									if (curClosestWordForm != null)
									{
										totalMisspellingToCorrectSpelling.put(curMisspellingWithReplacement, curClosestWordForm);
									}
								}
								
								curNewCol = col;
								
								if (curNewCol < MisspellingsSectionGenerator.qwerty_key_array_lengths[curNewRow])
								{
									curReplacement = MisspellingsSectionGenerator.qwerty_key_array[curNewRow][curNewCol];
									
									curMisspellingWithReplacement = curGeneratedMisspelling.replace(charToBeReplaced[position], curReplacement);
									
									// Determine what version of the correctly spelled word is closest to the misspelling
									curClosestWordForm = 
										MisspellingsSectionGenerator.getClosestCorrectSpellingForMisspelling(
											curMisspellingWithReplacement, curCorrectlySpelledWord, 
											totalMisspellingToCorrectSpelling, totalCorrectSpellings, 
											inflectionToBaseWord, basewordToInflections);
									
									if (curClosestWordForm != null)
									{
										totalMisspellingToCorrectSpelling.put(curMisspellingWithReplacement, curClosestWordForm);
									}
								}
							}
							
							curNewRow = row + 1;
							
							if (curNewRow  < MisspellingsSectionGenerator.num_qwerty_rows)
							{
								curNewCol = col - 1;
								
								if ((curNewCol >= 0) && (curNewCol < MisspellingsSectionGenerator.qwerty_key_array_lengths[curNewRow]))
								{
									curReplacement = MisspellingsSectionGenerator.qwerty_key_array[curNewRow][curNewCol];
									
									curMisspellingWithReplacement = curGeneratedMisspelling.replace(charToBeReplaced[position], curReplacement);
									
									// Determine what version of the correctly spelled word is closest to the misspelling
									curClosestWordForm = 
										MisspellingsSectionGenerator.getClosestCorrectSpellingForMisspelling(
											curMisspellingWithReplacement, curCorrectlySpelledWord, 
											totalMisspellingToCorrectSpelling, totalCorrectSpellings, 
											inflectionToBaseWord, basewordToInflections);
									
									if (curClosestWordForm != null)
									{
										totalMisspellingToCorrectSpelling.put(curMisspellingWithReplacement, curClosestWordForm);
									}
								}
								
								curNewCol = col + 1;
								
								if (curNewCol < MisspellingsSectionGenerator.qwerty_key_array_lengths[curNewRow])
								{
									curReplacement = MisspellingsSectionGenerator.qwerty_key_array[curNewRow][curNewCol];
									
									curMisspellingWithReplacement = curGeneratedMisspelling.replace(charToBeReplaced[position], curReplacement);
									
									// Determine what version of the correctly spelled word is closest to the misspelling
									curClosestWordForm = 
										MisspellingsSectionGenerator.getClosestCorrectSpellingForMisspelling(
											curMisspellingWithReplacement, curCorrectlySpelledWord, 
											totalMisspellingToCorrectSpelling, totalCorrectSpellings, 
											inflectionToBaseWord, basewordToInflections);
									
									if (curClosestWordForm != null)
									{
										totalMisspellingToCorrectSpelling.put(curMisspellingWithReplacement, curClosestWordForm);
									}
								}
								
								curNewCol = col;
								
								if (curNewCol < MisspellingsSectionGenerator.qwerty_key_array_lengths[curNewRow])
								{
									curReplacement = MisspellingsSectionGenerator.qwerty_key_array[curNewRow][curNewCol];
									
									curMisspellingWithReplacement = curGeneratedMisspelling.replace(charToBeReplaced[position], curReplacement);
									
									// Determine what version of the correctly spelled word is closest to the misspelling
									curClosestWordForm = 
										MisspellingsSectionGenerator.getClosestCorrectSpellingForMisspelling(
											curMisspellingWithReplacement, curCorrectlySpelledWord, 
											totalMisspellingToCorrectSpelling, totalCorrectSpellings, 
											inflectionToBaseWord, basewordToInflections);
									
									if (curClosestWordForm != null)
									{
										totalMisspellingToCorrectSpelling.put(curMisspellingWithReplacement, curClosestWordForm);
									}
								}
							}
							
							curNewRow = row;
							
							curNewCol = col - 1;
							
							if (curNewCol >= 0)
							{
								curReplacement = MisspellingsSectionGenerator.qwerty_key_array[curNewRow][curNewCol];
								
								curMisspellingWithReplacement = curGeneratedMisspelling.replace(charToBeReplaced[position], curReplacement);
								
								// Determine what version of the correctly spelled word is closest to the misspelling
								curClosestWordForm = 
									MisspellingsSectionGenerator.getClosestCorrectSpellingForMisspelling(
										curMisspellingWithReplacement, curCorrectlySpelledWord, 
										totalMisspellingToCorrectSpelling, totalCorrectSpellings, 
										inflectionToBaseWord, basewordToInflections);
								
								if (curClosestWordForm != null)
								{
									totalMisspellingToCorrectSpelling.put(curMisspellingWithReplacement, curClosestWordForm);
								}
							}
							
							curNewCol = col + 1;
							
							if (curNewCol < MisspellingsSectionGenerator.qwerty_key_array_lengths[curNewRow])
							{
								curReplacement = MisspellingsSectionGenerator.qwerty_key_array[curNewRow][curNewCol];
								
								curMisspellingWithReplacement = curGeneratedMisspelling.replace(charToBeReplaced[position], curReplacement);
								
								// Determine what version of the correctly spelled word is closest to the misspelling
								curClosestWordForm = 
									MisspellingsSectionGenerator.getClosestCorrectSpellingForMisspelling(
										curMisspellingWithReplacement, curCorrectlySpelledWord, 
										totalMisspellingToCorrectSpelling, totalCorrectSpellings, 
										inflectionToBaseWord, basewordToInflections);
								
								if (curClosestWordForm != null)
								{
									totalMisspellingToCorrectSpelling.put(curMisspellingWithReplacement, curClosestWordForm);
								}
							}
						}
					}
					if (MisspellingsSectionGenerator.shouldGenerateTranscribedKeyPresses)
					{
						char curSwapChar1;
						char curSwapChar2;
						
						for (int i = 0; i < curCorrectlySpelledWord.length()-1; i++)
						{
							curSwapChar1 = curCorrectlySpelledWord.charAt(i+1);
							curSwapChar2 = curCorrectlySpelledWord.charAt(i);
							
							curGeneratedMisspelling = 
								curCorrectlySpelledWord.substring(0, i) 
									+ curSwapChar1 
									+ curSwapChar2
									+ curCorrectlySpelledWord.substring(i+2, curCorrectlySpelledWord.length());
	
							// Determine what version of the correctly spelled word is closest to the misspelling
							curClosestWordForm = 
								MisspellingsSectionGenerator.getClosestCorrectSpellingForMisspelling(
									curGeneratedMisspelling, curCorrectlySpelledWord, 
									totalMisspellingToCorrectSpelling, totalCorrectSpellings, 
									inflectionToBaseWord, basewordToInflections);
							
							if (curClosestWordForm != null)
							{
								totalMisspellingToCorrectSpelling.put(curGeneratedMisspelling, curClosestWordForm);
							}
						}
					}
					/*
					 * Note:  Doesn't account for misspellings in addition to deleted spaces
					 *        Doesn't account for all combinations of missing spaces (if there is more than one space)
					 */
					if (MisspellingsSectionGenerator.shouldGenerateDeletedSpaces)
					{
						int curSpaceIndex = curCorrectlySpelledWord.indexOf(' ');
						
						while (curSpaceIndex != -1)
						{
							curGeneratedMisspelling = 
								curCorrectlySpelledWord.substring(0, curSpaceIndex)
								+ curCorrectlySpelledWord.substring(curSpaceIndex+1, curCorrectlySpelledWord.length());
							
							// Determine what version of the correctly spelled word is closest to the misspelling
							curClosestWordForm = 
								MisspellingsSectionGenerator.getClosestCorrectSpellingForMisspelling(
									curGeneratedMisspelling, curCorrectlySpelledWord, 
									totalMisspellingToCorrectSpelling, totalCorrectSpellings, 
									inflectionToBaseWord, basewordToInflections);
							
							if (curClosestWordForm != null)
							{
								totalMisspellingToCorrectSpelling.put(curGeneratedMisspelling, curClosestWordForm);
							}
							
							curSpaceIndex = curCorrectlySpelledWord.indexOf(' ', curSpaceIndex+1);
						}
					}
				}
			}

			String[] keyListArray = new String[0];

			List<String> keysInOrder = 
					Arrays.asList(totalMisspellingToCorrectSpelling.keySet().toArray(keyListArray));

			Collections.sort(keysInOrder);

			for (String curMisspelling : keysInOrder)
			{
				outputStream.append("\"" + curMisspelling + "\" : \"" + totalMisspellingToCorrectSpelling.get(curMisspelling) + "\",");
				outputStream.newLine();

				outputStream.flush();
			}

			outputStream.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		// There must be two args
		if (args.length != 2)
		{
			System.exit(0);
		}

		// The first argument is the input file path 
		MisspellingsSectionGenerator.inFile = new File(args[0]);

		// The second argument is the output file path 
		MisspellingsSectionGenerator.outFile = new File(args[1]);
		
		MisspellingsSectionGenerator.generateMisspelligsSection();
	}
	
	public static void test()
	{
		String first = "bass";
		String second = "bass";
		
		System.out.println("Distance between " + first + " and " + second + " = " +
			MisspellingsSectionGenerator.getDamerauLevenshteinDistance(first, second));
		
		first = "bass";
		second = "bases";
		
		System.out.println("Distance between " + first + " and " + second + " = " +
			MisspellingsSectionGenerator.getDamerauLevenshteinDistance(first, second));
			
		first = "bass";
		second = "base";
		
		System.out.println("Distance between " + first + " and " + second + " = " +
				MisspellingsSectionGenerator.getDamerauLevenshteinDistance(first, second));
		
		first = "basse";
		second = "base";
		
		System.out.println("Distance between " + first + " and " + second + " = " +
				MisspellingsSectionGenerator.getDamerauLevenshteinDistance(first, second));
		
		first = "basse";
		second = "bases";
		
		System.out.println("Distance between " + first + " and " + second + " = " +
				MisspellingsSectionGenerator.getDamerauLevenshteinDistance(first, second));
		
		first = "a";
		second = "bases";
		
		System.out.println("Distance between " + first + " and " + second + " = " +
				MisspellingsSectionGenerator.getDamerauLevenshteinDistance(first, second));
		
		first = "basss";
		second = "bases";
		
		System.out.println("Distance between " + first + " and " + second + " = " +
				MisspellingsSectionGenerator.getDamerauLevenshteinDistance(first, second));
		
		first = "basss";
		second = "base";
		
		System.out.println("Distance between " + first + " and " + second + " = " +
				MisspellingsSectionGenerator.getDamerauLevenshteinDistance(first, second));
	}
}