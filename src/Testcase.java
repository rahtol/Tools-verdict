
public class Testcase extends Object {
	
	public String group;
	public String number;
	
	Testcase (String grp, String num)
	{
		group = grp;
		number = num;
	}

	public boolean equals (Object x)
	{
		return group.equals (((Testcase)x).group) && number.equals (((Testcase)x).number);
	}
	
	public int hashCode ()
	{
		return (group+number).hashCode();
	}

}
