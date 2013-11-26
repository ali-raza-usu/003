package aspects.logFile;
import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.CodeSignature;

import apps.Calculator;

public aspect LogOpsInFileAspect_2 {
	
	Logger _logger = Logger.getLogger(LogOpsInFileAspect_2.class);
	pointcut logOps() : call(int Calculator.*(..));

	
	int around() : logOps() {
		int val = proceed();
		printParameters(thisJoinPoint);
	     _logger.debug( thisJoinPointStaticPart.getSignature().getName() + " : result is "+ val);
	     return val;
	}

	
	
	 private void printParameters(JoinPoint jp)
	{
      Object[] args = jp.getArgs();
      String[] names = ((CodeSignature)jp.getSignature()).getParameterNames();
      Class[] types = ((CodeSignature)jp.getSignature()).getParameterTypes();
      for (int i = 0; i < args.length; i++) {
      	_logger.debug("  "  + i + ". " + names[i] +
               " : " +            types[i].getName() +
               " = " +            args[i]);
        }
  	}
}
