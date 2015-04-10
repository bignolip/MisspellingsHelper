import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

public class MisspellingsHelper 
{
	private static File inFile; 
	private static File outFile;
	
	public static void main(String args[])
	{
		// There must be two arguments
		if (args.length != 2)
		{
			System.exit(0);
		}
		else 
		{
			// The first argument is the input file path 
			MisspellingsHelper.inFile = new File(args[0]);
			
			// The second argument is the output file path 
			MisspellingsHelper.outFile = new File(args[1]);
		}
		
		try 
		{
			// Open the input file stream
			BufferedReader inputStream = new BufferedReader(new FileReader(MisspellingsHelper.inFile));
			
			// Open the output file stream
			BufferedWriter outputStream = new BufferedWriter(new FileWriter(MisspellingsHelper.outFile));
		
			// Determine which words should have spaces after them
			String spacingInstructions = inputStream.readLine();
			
			StringTokenizer spacingInstructionsTokenizer = new StringTokenizer(spacingInstructions, ",");
			
			List<Integer> spacingPositions = new LinkedList<Integer>();
			
			while (spacingInstructionsTokenizer.hasMoreElements())
			{
				spacingPositions.add(Integer.parseInt(spacingInstructionsTokenizer.nextToken().trim()));
			}
			
			String curWordLine = inputStream.readLine();
			
			StringTokenizer curWordLineTokenizer;
			
			List<Set<String>> listOfMisspellingWordComponents = new LinkedList<Set<String>>();
			
			Set<String> curMisspellingWordComponents;
			
			while (curWordLine != null)
			{
				curWordLineTokenizer = new StringTokenizer(curWordLine, ", ");
				
				curMisspellingWordComponents = new HashSet<String>();
				
				while (curWordLineTokenizer.hasMoreElements())
				{
					curMisspellingWordComponents.add(curWordLineTokenizer.nextToken().trim());
				}
				
				listOfMisspellingWordComponents.add(curMisspellingWordComponents);
				
				curWordLine = inputStream.readLine();
			}
			
			inputStream.close();
			
			// Generate the master list of misspellings
			List<String> totalMisspellings = new LinkedList<String>();
			
			List<String> curMisspellings;
			
			String curTotalMisspellingsString;
			
			for (int i = 0 ; i < listOfMisspellingWordComponents.size() ; i++)
			{
				curMisspellings = new LinkedList<String>(totalMisspellings);
			
				if (curMisspellings.size() == 0)
				{
					for (int j = 0 ; j < listOfMisspellingWordComponents.get(i).size() ; j++)
					{
						curMisspellings.add((String)listOfMisspellingWordComponents.get(i).toArray()[j]);
					}
					
					totalMisspellings = curMisspellings;
				}
				else
				{
					totalMisspellings.clear();
					
					for (int j = 0 ; j < curMisspellings.size() ; j++)
					{
						for (int k = 0 ; k < listOfMisspellingWordComponents.get(i).size() ; k++)
						{
							curTotalMisspellingsString = curMisspellings.get(j);
							
							if (spacingPositions.contains(i))
							{
								curTotalMisspellingsString += " ";
							}
							
							curTotalMisspellingsString += listOfMisspellingWordComponents.get(i).toArray()[k];
					
							totalMisspellings.add(curTotalMisspellingsString);
						}
					}
				}
			}
			
			// Now sort the total misspellings list alphabetically
			Collections.sort(totalMisspellings);
			
			// Print the sorted list to the output file
			for (int i = 0 ; i < totalMisspellings.size() ; i++)
			{
				if (i < (totalMisspellings.size() - 1))
				{
					outputStream.append(totalMisspellings.get(i) + ", ");
				}
				else
				{
					outputStream.append(totalMisspellings.get(i));
				}
				
				outputStream.flush();
			}
			
			outputStream.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
