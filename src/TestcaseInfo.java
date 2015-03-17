
public class TestcaseInfo {
	
	public boolean specs_dir;  // subdir "specs" exists and is not empty
	public boolean caller_rts;
	public boolean rttlight_log;
	public long log_last_modified;
	public boolean log_parse_result;
	public boolean log_result;
	public String datetime_testbeginn;
	public String group;
	public String number;

	TestcaseInfo (String group, String number)
	{
		this.group = group;
		this.number = number;
		specs_dir = false;
		caller_rts = false;
		rttlight_log = false;
		log_last_modified = 0;
		log_parse_result = false;
		log_result = false;
		datetime_testbeginn = "";
	}
	
	static char f (boolean x)
	{
		return (x? 'T' : 'F');
	}
	
	public String toString ()
	{
		return String.format("%-20s", group) + " " + String.format("%-4s", number) 
				+ "  " + f(caller_rts) + f(rttlight_log) + f(log_parse_result) + f(log_result) 
				+ "  " + datetime_testbeginn;
	}
	
}
