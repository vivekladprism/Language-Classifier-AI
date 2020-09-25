import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Predict
{
	public static String inputModelFileName;
	public static String testDataFileName;
	public static TreeNode root;
	public static int numOfFeatures = 11;
	public static String decision;
	public static List<TreeNode> list;
	public static Set<Integer> set = new HashSet<>();
	
	public static void readArgumentsFromCommandLine(String args[])
	{
		inputModelFileName = args[0];
		testDataFileName = args[1];
	}
	
	public static void readTreeFromFile() throws ClassNotFoundException, IOException
	{
		InputStream file = new FileInputStream( inputModelFileName );
		InputStream is = new BufferedInputStream( file );
		ObjectInput oi = new ObjectInputStream( is );
		
		Object o = oi.readObject();
		if(o.getClass().getSimpleName().equals("ArrayList"))
			list = (ArrayList<TreeNode>)o;
		else
			root = (TreeNode)o;
	}
	
	public static void readFromInputFileDT() throws IOException
	{
		File file = new File(testDataFileName);
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		String s;
		int lineNum = 1;
		int countEn = 0;
		int countNl = 0;
		while((s=br.readLine())!=null)
		{
			s = s.replaceAll("[^a-zA-Z0-9]", " ");
			String c[] = new String[numOfFeatures+1];
			for(int i = 0 ; i < numOfFeatures+1; i++)
			{
				c[i] = "F";
			}
			for(String word: s.split("\\s+"))
			{
				if(word.toLowerCase().equals("een"))
					c[1] = "T";
				if(word.toLowerCase().equals("a") || word.toLowerCase().equals("an"))
					c[2] = "T";
				if(word.length() >= 11)
					c[3] = "T";
				if(word.toLowerCase().equals("the"))
					c[4] = "T";
				if(word.toLowerCase().equals("de"))
					c[5] = "T";
				if(word.toLowerCase().equals("het"))
					c[6] = "T";
				if(word.toLowerCase().equals("hij") || word.toLowerCase().equals("zij"))
					c[7] = "T";
				if(word.toLowerCase().equals("jij") || word.toLowerCase().equals("u"))
					c[8] = "T";
				if(word.toLowerCase().contains("ij"))
					c[9] = "T";
				if(word.toLowerCase().equals("are"))
					c[10] = "T";
				if(word.toLowerCase().equals("that") || word.toLowerCase().equals("there") || word.toLowerCase().equals("these") || word.toLowerCase().equals("this") || word.toLowerCase().equals("them"))
					c[11] = "T";


			}
			String dec = makeDecision(c);
			if(dec.equals("T"))
			{
				System.out.println("en");
				countEn++;
			}
				
			else
			{
				System.out.println("nl");
				countNl++;
			}

		}
	}
	
	public static String makeDecision(String c[])
	{
		TreeNode copy = root;
		helper(copy,c);
		return decision;
	}
	
	public static void helper(TreeNode root,String c[])
	{
		if(root.colNum == -1 )
		{
			decision = root.decision;
			return;
		}

		if(c[root.colNum].equals("F"))
		{
			helper(root.ifFalse,c);
		}
			
		if(c[root.colNum].equals("T"))
		{
			helper(root.ifTrue,c);
			
		}
			
	}
	
	public static void readFromInputFileADA() throws IOException
	{
		File file = new File(testDataFileName);
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		String s;
		int lineNum = 1;
		int countEn = 0;
		int countNl = 0;
		while((s=br.readLine())!=null)
		{
			s = s.replaceAll("[^a-zA-Z0-9]", " ");
			String c[] = new String[numOfFeatures+1];
			for(int i = 0 ; i < numOfFeatures+1; i++)
			{
				c[i] = "F";
			}
			for(String word: s.split("\\s+"))
			{
				if(word.toLowerCase().equals("een"))
					c[1] = "T";
				if(word.toLowerCase().equals("a") || word.toLowerCase().equals("an"))
					c[2] = "T";
				if(word.length() >= 11)
					c[3] = "T";
				if(word.toLowerCase().equals("the"))
					c[4] = "T";
				if(word.toLowerCase().equals("de"))
					c[5] = "T";
				if(word.toLowerCase().equals("het"))
					c[6] = "T";
				if(word.toLowerCase().equals("hij") || word.toLowerCase().equals("zij"))
					c[7] = "T";
				if(word.toLowerCase().equals("jij") || word.toLowerCase().equals("u"))
					c[8] = "T";
				if(word.toLowerCase().contains("ij"))
					c[9] = "T";
				if(word.toLowerCase().equals("are"))
					c[10] = "T";
				if(word.toLowerCase().equals("that") || word.toLowerCase().equals("there") || word.toLowerCase().equals("these") || word.toLowerCase().equals("this") || word.toLowerCase().equals("them"))
					c[11] = "T";


			}
			double p1 = 0;
			double n1 = 0;
			for(TreeNode n : list)
			{
				if(n.colNum == -1)
					continue;
				if(c[n.colNum].equals("T"))
				{
					if(set.contains(n.colNum))
						p1 += n.amountOfSay;
					else
						n1 += n.amountOfSay;
				}
				else
				{
					if(c[n.colNum].equals("F"))
					{
						if(set.contains(n.colNum))
						{
							n1 += n.amountOfSay;
						}
						else
							p1 += n.amountOfSay;
					}
				}
			}
			if(p1>n1)
				System.out.println("nl");
			else
				System.out.println("en");
		}
	}
	public static void addDutchFeaturesToSet()
	{
		set.add(1);set.add(3);set.add(5);set.add(6);set.add(7);set.add(8);set.add(9);
	}
	public static void main(String[] args) throws ClassNotFoundException, IOException
	{
		readArgumentsFromCommandLine(args);
		addDutchFeaturesToSet();
		readTreeFromFile();
		if(root != null)
			readFromInputFileDT();
		else
			readFromInputFileADA();
		
	}
}
