package aspects.logTime;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public aspect LogTimeInConsoleAspect_1 {
	
	pointcut logTime() : call(* *.println(..));

	
	void around() : logTime() {
		System.out.print(getTime() + " : ");
		  proceed();
	      
	}

	
	private String getTime(){
		Calendar cal = Calendar.getInstance();
    	cal.getTime();
    	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    	return sdf.format(cal.getTime());
	}
	
}
