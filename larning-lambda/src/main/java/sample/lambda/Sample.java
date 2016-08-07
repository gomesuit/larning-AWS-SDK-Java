package sample.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class Sample implements RequestHandler<Integer, Integer> {
	public Integer handleRequest(Integer input, Context context) {
		LambdaLogger lambdaLogger = context.getLogger();
		lambdaLogger.log("count = " + input);
		return input;
	}
}
