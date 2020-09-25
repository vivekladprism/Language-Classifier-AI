import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TreeNode implements Serializable
{
	int colNum;
	TreeNode ifTrue;
	TreeNode ifFalse;
	String forTrue;
	Map<Integer, String> parents;
	String decision = null;
	boolean leafNode = false;
	double amountOfSay = 0;
	
	public TreeNode()
	{
		
	}
	public TreeNode(int colNum, TreeNode prev, String s)
	{
		parents = new HashMap<>();
		if(prev!=null)
		{
			for(Integer i : prev.parents.keySet())
			{
				parents.put(i,prev.parents.get(i));
			}
		}
		if(s!= null)
			parents.put(prev.colNum, s);
		this.colNum = colNum;
	}
	
	public String toString()
	{
		String ans = "";
		if(parents == null)
			return ans+ this.colNum;
		for(int i: parents.keySet())
		{
			ans = ans + "  " + i + " -> " + parents.get(i)+ " ";
		}
		return ans;
	}
}
