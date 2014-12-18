import java.io.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.*;


public class xdir {

//	System.err.println("rttlverdict v1.03, 21.05.2013");
//	final static String version = "rttlverdict v1.04, 02.08.2013";
//	final static String version = "rttlverdict v1.05, 30.08.2013";
//	final static String version = "rttlverdict v1.06, 25.02.2014";
	final static String version = "rttlverdict v1.07, 25.11.2014";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.err.println(version);
		System.out.println(version);
		
		if (args.length != 1) {
			System.err.println ("usage: xdir <path>\n");
			System.exit(-1);
		};
		
		final File folder = new File (args[0]);
        if (folder.isDirectory()) {
        	parseDir (0, folder);
        } else {
        	System.err.println("not a directory: "+args[0]);
			System.exit(-1);
      }

        print_verdict ();
		System.exit(0);
	}
	
	static void parseDir (int level, final File folder)
	{
	    for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	            parseDir (level+1, fileEntry);
	        } else if (fileEntry.isFile()) {
	        	processFile (level+1, fileEntry);
	        } else {
	        	System.err.println(fileEntry.getAbsolutePath());
	        }
	    }
	}

	final static Pattern p = Pattern.compile(".*\\\\TESTS\\\\([a-zA-Z_-]+)\\\\([0-9a-zA-Z_]+)\\\\(.+)");
	static HashMap<String,TestcaseInfo> testcases = new HashMap<String,TestcaseInfo>();

	static void processFile (int level, final File file)
	{
		Matcher m = p.matcher(file.getAbsolutePath());
		if (m.matches()) {
			
			String group = m.group(1);
			String number = m.group(2);
			String fname = m.group(3);
			String key = group + "\\" + number;
			
			TestcaseInfo v;
			if (testcases.containsKey(key)){
				v = testcases.get(key);
			} else {
				System.err.println(key);  // just to indicate progress on console
				v = new TestcaseInfo(group, number);
			}
			
			if (fname.equals("specs\\caller.rts") || fname.equals("specs\\" +number + ".rts"))
			{
				v.caller_rts = true;
			}
			
			if (fname.equals("rttlsrc\\rttlight.log"))
			{
				v.rttlight_log = true;
				v.log_last_modified = file.lastModified();
				parse_rttlight_log (file, v);
			}

			testcases.put(key,v);
		}
	}
	
	static void parse_rttlight_log (final File file, TestcaseInfo info)
	{
		final String p0 = "Testbeginn[:]\\s*(\\d+)\\.\\s*([a-zA-Z]+)\\s*(\\d+),\\s*(\\d+)[:](\\d+)[:](\\d+)";
//		final String p1 = "\\r\\n[|]\\s*Zusammenfassung\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+([A-Z]+)\\s*[|]\\r\\n" + "[+][-]{129}[+]\\r\\n\\z";
		final String p1 = "\\r\\n[|]\\s*Zusammenfassung(\\s+\\d+){4}\\s+([A-Z]+)\\s*[|]\\r\\n[+][-]{129}[+]\\r\\n\\z";

		try {
			
			Scanner sc = new Scanner(file);
			
			// Header
			String m00 = sc.findWithinHorizon("\\A[+][-]{129}[+]", 0);
			String m01 = sc.findWithinHorizon("\\r\\n[|] cppth \\- Testfunktionen V", 0);
			String m02 = sc.findWithinHorizon(p0,0);
			String m03 = sc.findWithinHorizon("[+][-]{129}[+]", 0);
			
			// Footer
			String m10 = sc.findWithinHorizon("[+][-]{129}[+]", 0);
			String m11 = sc.findWithinHorizon("[+][-]{129}[+]", 0);
			String m12 = sc.findWithinHorizon("[+][-]{129}[+]", 0);
			String m13 = sc.findWithinHorizon("[+][-]{129}[+]", 0);
			String m14 = sc.findWithinHorizon("[+][-]{129}[+]", 0);
			String m15 = sc.findWithinHorizon("[+][-]{129}[+]", 0);
			String m16 = sc.findWithinHorizon(p1, 0);
			
			sc.close();

			boolean parse_ok = true;
			parse_ok = parse_ok && (m00 != null) && (!m00.equals(""));
			parse_ok = parse_ok && (m01 != null) && (!m01.equals(""));
			parse_ok = parse_ok && (m02 != null) && (!m02.equals(""));
			parse_ok = parse_ok && (m03 != null) && (!m03.equals(""));
			parse_ok = parse_ok && (m10 != null) && (!m10.equals(""));
			parse_ok = parse_ok && (m11 != null) && (!m11.equals(""));
			parse_ok = parse_ok && (m12 != null) && (!m12.equals(""));
			parse_ok = parse_ok && (m13 != null) && (!m13.equals(""));
			parse_ok = parse_ok && (m14 != null) && (!m14.equals(""));
			parse_ok = parse_ok && (m15 != null) && (!m15.equals(""));
			parse_ok = parse_ok && (m16 != null) && (!m16.equals(""));
			
			info.log_parse_result = parse_ok;
			
			if (parse_ok)
			{
				String datetime_testbeginn;
				Pattern pp0 = Pattern.compile(p0);
				Matcher mm0 = pp0.matcher(m02);
				if (mm0.matches()) {
					datetime_testbeginn = mm0.group(1)+"."+mm0.group(2)+" "+mm0.group(3)+", "+mm0.group(4)+":"+mm0.group(5)+":"+mm0.group(6);
				}
				else {
					datetime_testbeginn = "??.??? ????, ??:??:??";
				}
				info.datetime_testbeginn = datetime_testbeginn;
	
				Pattern pp1 = Pattern.compile(p1);
				Matcher mm1 = pp1.matcher(m16);
				if (mm1.matches()) {
					String result = mm1.group(2);
					info.log_result = result.equals("BESTANDEN");
				}
				else {
					info.log_result = false;
				}
			}

		} catch (FileNotFoundException e) {
			info.rttlight_log = false;
		}
	}

	public static void print_verdict ()
	{
		int total = 0, ok = 0, failed = 0;
		boolean rttlight_log_min = false;
		boolean rttlight_log_max = false;
		long min_log_last_modified = 0;
		long max_log_last_modified = 0;
		
		for (String s : testcases.keySet())
		{
			TestcaseInfo v = testcases.get(s);
			
			total +=1;
			boolean success = v.caller_rts && v.rttlight_log && v.log_parse_result && v.log_result; 
			if (success)
			{
				ok +=1;
			} else
			{
				failed +=1;
			}
			
			if (v.rttlight_log)
			{
				if ((v.log_last_modified < min_log_last_modified) || (!rttlight_log_min))
				{
					min_log_last_modified = v.log_last_modified;
				}
				if ((v.log_last_modified > max_log_last_modified) || (!rttlight_log_max))
				{
					max_log_last_modified = v.log_last_modified;
				}
				rttlight_log_min = true;
				rttlight_log_max = true;
			}
			
			System.out.println ((success?"++  ":"--  ") + v);
		}
		
		String str_min_log_last_modified = "?";
		String str_max_log_last_modified = "?";
		SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		if (rttlight_log_min)
		{
			str_min_log_last_modified = formatter.format(min_log_last_modified);
		}
		if (rttlight_log_max)
		{
			str_max_log_last_modified = formatter.format(max_log_last_modified);
		}
		
		System.out.println("#Testcases: Total="+total+"; ok="+ok+"; failed="+failed+"; oldest_rttlight_log=\""+str_min_log_last_modified+"\"; newest_rttlight_log=\""+str_max_log_last_modified+"\"");
	}
}
