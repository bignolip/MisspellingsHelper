import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;


public class MisspellingCorrectionGenerator {

	private static File inFile; 
	private static File outFile;
	
	private static String correctSpelling;
	
	public static void main(String args[])
	{
		// There must be three arguments
		if (args.length != 3)
		{
			System.exit(0);
		}
		else 
		{
			// The first argument is the input file path 
			MisspellingCorrectionGenerator.inFile = new File(args[0]);

			// The second argument is the output file path 
			MisspellingCorrectionGenerator.outFile = new File(args[1]);
			
			// The third argument is the correct spelling
			MisspellingCorrectionGenerator.correctSpelling = args[2];
		}
		
		try
		{
			// Open the input file stream
			BufferedReader inputStream = new BufferedReader(new FileReader(MisspellingCorrectionGenerator.inFile));
						
			// Open the output file stream
			BufferedWriter outputStream = new BufferedWriter(new FileWriter(MisspellingCorrectionGenerator.outFile));
			
			String misspellingsString = inputStream.readLine();
			
			inputStream.close();
			
			StringTokenizer misspellingTokenizer = new StringTokenizer(misspellingsString, ", ");
			
			while (misspellingTokenizer.hasMoreElements())
			{
				outputStream.append("\"" + misspellingTokenizer.nextToken() + "\" : \"" + MisspellingCorrectionGenerator.correctSpelling + "\",");
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
}