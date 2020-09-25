import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Train
{
	public static String inputFileName;
	public static String outputFileName;
	public static String algorithmToUse;
	public static List<String> sentences;
	public static String dataset[][];
	public static int numOfFeatures = 11;
	public static double entropyDS = 0;
	public static double p = 0;
	public static double n = 0;
	public static double total = 0;
	public static int rowCount = 0;
	public static int colCount = 0;
	public static int rootNodeNum = -1;
	public static String decision;
	public static TreeNode treeRoot;
	public static double sampleWeight[];
	public static double correct = 0;
	public static double incorrect = 0;
	public static double sumOfSampleWeights = 0;
	public static Set<Integer> t = new HashSet<>();
	public static int sumArray[];
	public static Map<TreeNode, Double> map = new HashMap<>();
	public static List<TreeNode> toFile = new ArrayList<>();
	
	public static void readFromCommandLine(String args[])
	{
		inputFileName = args[0];
		outputFileName = args[1];
		algorithmToUse = args[2];
	}
	
	public static void readSentencesFromFile() throws IOException
	{
		sentences = new ArrayList<>();
		File file = new File(inputFileName);
		BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
		String sentence;
		int sentencesIndex = 0 ;
		
		while((sentence = bufferedReader.readLine()) != null)
		{
			sentences.add(sentence);
		}
		
		bufferedReader.close();
	}
	
	public static void createDatasetTable()
	{
		dataset = new String[sentences.size()][numOfFeatures + 2];
		initializeDataset();
		int sentenceIndex = 0;
		for(String sentence : sentences)
		{
			dataset[sentenceIndex][0] = "sentence-" + (sentenceIndex + 1);
			sentence = sentence.replaceAll("[^a-zA-Z0-9]", " ");
			
			int index = 0;
			for(String word: sentence.split("\\s+"))
			{
				if(word.toLowerCase().equals("een"))
					dataset[sentenceIndex][1] = "T";
				if(word.toLowerCase().equals("a") || word.toLowerCase().equals("an"))
					dataset[sentenceIndex][2] = "T";
				if(word.length() >= 11)
					dataset[sentenceIndex][3] = "T";
				if(word.toLowerCase().equals("the"))
					dataset[sentenceIndex][4] = "T";
				if(word.toLowerCase().equals("de"))
					dataset[sentenceIndex][5] = "T";
				if(word.toLowerCase().equals("het"))
					dataset[sentenceIndex][6] = "T";
				if(word.toLowerCase().equals("hij") || word.toLowerCase().equals("zij"))
					dataset[sentenceIndex][7] = "T";
				if(word.toLowerCase().equals("jij") || word.toLowerCase().equals("u"))
					dataset[sentenceIndex][8] = "T";
				if(word.toLowerCase().contains("ij"))
					dataset[sentenceIndex][9] = "T";
				if(word.toLowerCase().equals("are") )
					dataset[sentenceIndex][10] = "T";

				if(word.toLowerCase().equals("that") || word.toLowerCase().equals("there") || word.toLowerCase().equals("these") || word.toLowerCase().equals("this") || word.toLowerCase().equals("them"))
					dataset[sentenceIndex][11] = "T";
				
				if(index == 0)
				{
					if(word.equals("en"))
					{
						dataset[sentenceIndex][numOfFeatures + 1] = "en";
						p++;
					}
					else
					{
						dataset[sentenceIndex][numOfFeatures + 1] = "nl";
						n++;
					}
					total++;
				}
				index++;
			}
			sentenceIndex++;
		}
		rowCount = dataset.length;
		colCount = dataset[0].length;
	}
	
	public static void initializeDataset()
	{
		for(int row = 0; row < dataset.length; row++)
		{
			for(int col = 0 ; col < dataset[row].length; col++)
			{
				dataset[row][col] = "F";
			}
		}
	}
	
	public static void displayDataset()
	{
		for(int row = 0; row < dataset.length; row++)
		{
			for(int col = 0 ; col < dataset[row].length; col++)
			{
				System.out.print(dataset[row][col]+ "  ");
			}
			System.out.println();
		}
	}
	
	public static double calculateEntropyOfDataset(double p, double n, double total)
	{
		double e1 = p / (1.0 * (p + n));
		double e2 = n / (1.0 * ( p + n));
		double entropy = 0;

		if(e1 == 0 && e2 != 0)
		{
			entropy = -e2 * ( Math.log(e2) / (1.0 * Math.log(2) ) );
		}
			
		else if(e2 == 0 && e1 != 0)
		{
			entropy = -e1*( Math.log(e1) / (1.0 * Math.log(2) ));
		}
			
		else
		{
			entropy = -e1*( Math.log(e1) / (1.0 * Math.log(2) )) - e2 * ( Math.log(e2) / (1.0 * Math.log(2) ) );
		}
		return entropy;
	}
	
	public static int findRoot()
	{
		double gain = Double.MIN_VALUE;
		int root = -1;
		
		
		for(int col = 1; col < dataset[0].length -1; col ++)
		{
			double avgEntropy = 0.0;
			double total2 = 0.0;
			for(int bin = 0; bin < 2 ; bin++)
			{
				String comp = "F";
				if(bin == 1)
					comp = "T";
				double total1 = 0.0;
				double p1 = 0;
				double n1 = 0;
				for(int row = 0 ; row < dataset.length; row++)
				{
					if(dataset[row][col].equals(comp) && dataset[row][colCount-1].equals("en"))
					{
						p1++;
						total1 ++;
						total2 ++;
					}
						
					else if(dataset[row][col].equals(comp) && dataset[row][colCount-1].equals("nl"))
					{
						n1 ++;
						total1 ++;
						total2 ++;
					}
						
				} 
				if(total1 == 0)
					continue;
				
				double entropy = calculateEntropyOfDataset(p1, n1, total1);
				avgEntropy += ((p1 + n1) / (1.0 * total2)) * entropy;
			}
			
			if(entropyDS - avgEntropy > gain)
			{
				gain = entropyDS - avgEntropy;
				root = col;
			}
		}
		return root;
	}
	
	public static void createTree()
	{
		TreeNode root = new TreeNode(rootNodeNum,null,null);
		treeRoot = root;
		treeHelperFunc(root);
	}
	
	public static void treeHelperFunc(TreeNode root)
	{
		if(root.colNum == -1)
			return;
		root.ifTrue = new TreeNode(findNextFeature(root,"T"),root,"T");
		if(root.ifTrue.colNum == -1)
		{
			root.ifTrue.decision = decision;
			root.ifTrue.leafNode = true;
		}
		treeHelperFunc(root.ifTrue);
		
		
		root.ifFalse = new TreeNode(findNextFeature(root,"F"),root,"F");
		
		if(root.ifFalse.colNum == -1)
		{
			root.ifFalse.decision = decision;
			root.ifFalse.leafNode = true;
		}

		treeHelperFunc(root.ifFalse);
		
	}
	public static int findNextFeature(TreeNode node, String val)
	{
		Set<Integer> rowNums = new HashSet<>();

		for(int rowNum = 0; rowNum < rowCount; rowNum++)
		{
			boolean add = true;
			for(int colNum : node.parents.keySet())
			{
				if(!dataset[rowNum][colNum].equals(node.parents.get(colNum)))
				{
					add = false;
					break;
				}
				
			}
			if(!dataset[rowNum][node.colNum].equals(val))
				continue;
			if(!add)
				continue;
			rowNums.add(rowNum);

		}
		
		double gain = Double.MIN_VALUE;
		int root = -1;

		String x = "";
		
		for(int col = 1; col < dataset[0].length -1; col ++)
		{
			if(node.parents.containsKey(col) || col == node.colNum)
				continue;
			double avgEntropy = 0.0;
			double total2 = 0.0;
			for(int bin = 0; bin < 2 ; bin ++)
			{
				String comp = "F";
				if(bin == 1)
					comp = "T";
				double total1 = 0.0;
				double p1 = 0;
				double n1 = 0;
				for(int row : rowNums)
				{
					if(dataset[row][col].equals(comp) && dataset[row][colCount-1].equals("en"))
					{
						p1 ++;
						total1 ++;
						total2 ++;
					}
						
					else if(dataset[row][col].equals(comp) && dataset[row][colCount-1].equals("nl"))
					{
						n1 ++;
						total1 ++;
						total2 ++;
					}
						
				} 
				if(total1 == 0)
					continue;
				
				if(p1 >= n1 && val.equals(comp))
				{
					x = "T";
				}
				else if(n1 > p1 && val.equals(comp))
				{
					x = "F";
				}
				double entropy = calculateEntropyOfDataset(p1, n1, total1);
				avgEntropy += ((p1 + n1) / (1.0 * total2)) * entropy;
			}
			
			if(entropyDS - avgEntropy > gain)
			{
				gain = entropyDS - avgEntropy;
				root = col;
				decision = x;
			}
		}
		return root;
	}
	
	public static void writeToFile() throws IOException
	{
		OutputStream file = new FileOutputStream( outputFileName);
		OutputStream os = new BufferedOutputStream( file );
		ObjectOutput oo = new ObjectOutputStream( os );

		if(algorithmToUse.equals("dt"))
			oo.writeObject(treeRoot);
		else
			oo.writeObject(toFile);
		oo.close();

	}
	
	public static void createAndAssignSampleWeights()
	{
		sampleWeight = new double[sentences.size()];
		for(int i = 0 ; i < sampleWeight.length; i++)
		{
			sampleWeight[i] = 1/(1.0*sentences.size());
		}
	}
	
	public static void calculateSumOfSampleWeights()
	{
		sumOfSampleWeights = 0;
		for(int i = 0; i < sampleWeight.length; i++)
		{
			sumOfSampleWeights += sampleWeight[i];
		}
	}
	public static void addDutchFeaturesToSet()
	{
		t.add(1);t.add(3);t.add(5);t.add(6);t.add(7);t.add(8);t.add(9);
	}
	public static int findMinStump()
	{
		TreeNode x = new TreeNode();
		double min = Double.MAX_VALUE;
		int col = -1;
		ArrayList<Integer> incorrectIndex = new ArrayList<>();
		for(int j = 1; j < colCount; j++)
		{
			double p1 = 0;
			double n1 = 0;
			double entropy = 0;
			double total1 = 0;
			ArrayList<Integer> temp = new ArrayList<>();
			
			for(int i = 0; i<rowCount; i++)
			{
				if(t.contains(j))
				{
					x.forTrue = "nl";
					if(dataset[i][j].equals("T") && dataset[i][12].equals("nl"))
					{
						p1 ++;
						total1 ++;
					}
					else if(dataset[i][j].equals("T") && dataset[i][12].equals("en"))
					{
						n1 ++;
						total1 ++;
						temp.add(i);
					}
					else if(dataset[i][j].equals("F") && dataset[i][12].equals("en"))
					{
						p1 ++;
						total1 ++;
					}
					else if(dataset[i][j].equals("F") && dataset[i][12].equals("nl"))
					{
						n1 ++;
						total1 ++;
						temp.add(i);
					}
				}
				else
				{
					x.forTrue = "en";
					if(dataset[i][j].equals("T") && dataset[i][12].equals("en"))
					{
						p1 ++;
						total1 ++;
					}
					else if(dataset[i][j].equals("T") && dataset[i][12].equals("nl"))
					{
						n1 ++;
						total1 ++;
						temp.add(i);
					}
					else if(dataset[i][j].equals("F") && dataset[i][12].equals("nl"))
					{
						p1 ++;
						total1 ++;
					}
					else if(dataset[i][j].equals("F") && dataset[i][12].equals("en"))
					{
						n1 ++;
						total1 ++;
						temp.add(i);
					}
					
				}
				
			}
			if(total1 == 0)
				continue;
			
			entropy = calculateEntropyOfDataset(p1, n1, total1);
			if(entropy < min)
			{
				min = entropy;
				col = j;
				correct = p1;
				incorrect = n1;
				incorrectIndex = temp;
				
			}
			
			
		}
		
		x.colNum = col;
		
		double error = 0;
		for(int index : incorrectIndex)
		{
			 error += sampleWeight[index];
		}
		
		error = 0.5 * (Math.log((1 - error) / (1.0 * error)));
		x.amountOfSay = error;
		
		for(int row = 0; row < rowCount ; row++)
		{
			if(incorrectIndex.contains(row))
			{
				sampleWeight[row] = sampleWeight[row] * Math.pow(Math.E, error);
			}
			else 
				sampleWeight[row] = sampleWeight[row] * Math.pow(Math.E, -error);
		}
		calculateSumOfSampleWeights();
		sumArray = new int[sentences.size()];
		int sum = 0;
		
		
		for(int row = 0; row < rowCount ; row++)
		{
			sampleWeight[row] = sampleWeight[row] / sumOfSampleWeights;
			sum += sampleWeight[row];
			sumArray[row] = sum;
		}
		
		String newDataset[][] = new String[dataset.length][dataset[0].length];
		for(int i = 0; i < dataset.length; i++)
		{
			double random = Math.random();
			int j = 0;
			for(j = sumArray.length-1; j>0; j--)
			{
				if(sumArray[j - 1] < random)
				{
					newDataset[i] = dataset[j];
					break;
				}
			}
			if(j == 0)
				newDataset[i] = dataset[0];
		}
		dataset = newDataset;
		toFile.add(x);

		return col;
	}
	
	public static void decisionTree() throws IOException
	{
		entropyDS = calculateEntropyOfDataset(p, n, total);
		rootNodeNum = findRoot();
		createTree();
		writeToFile();
	}
	
	public static void adaBoost() throws IOException
	{
		createAndAssignSampleWeights();
		calculateSumOfSampleWeights();
		addDutchFeaturesToSet();
		
		for(int i = 0; i < 25; i++)
		{
			findMinStump();
		}

	}
	public static void main(String[] args) throws IOException 
	{
		readFromCommandLine(args);
		readSentencesFromFile();
		createDatasetTable();
		displayDataset();
		if(algorithmToUse.equals("dt"))
		{
			decisionTree();
		}
		else if(algorithmToUse.equals("ada"))
		{
			
			adaBoost();
			writeToFile();
						
		}
		
		
	}

}
