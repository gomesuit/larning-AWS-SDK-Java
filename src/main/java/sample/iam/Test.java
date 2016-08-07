package sample.iam;

import java.time.Instant;
import java.util.Date;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.AccessKeyLastUsed;
import com.amazonaws.services.identitymanagement.model.AccessKeyMetadata;
import com.amazonaws.services.identitymanagement.model.GetAccessKeyLastUsedRequest;
import com.amazonaws.services.identitymanagement.model.GetAccessKeyLastUsedResult;
import com.amazonaws.services.identitymanagement.model.ListAccessKeysRequest;
import com.amazonaws.services.identitymanagement.model.User;

import net.gpedro.integrations.slack.SlackApi;
import net.gpedro.integrations.slack.SlackMessage;

public class Test {

	public static void main(String[] args) {
		if(args.length != 1){
			return;
		}
		
		String webHook = args[0];
		String message = "";

		AWSCredentialsProvider cp = new EnvironmentVariableCredentialsProvider();
		AmazonIdentityManagement iam = new AmazonIdentityManagementClient(cp);

		Region region = Region.getRegion(Regions.AP_NORTHEAST_1);
		iam.setRegion(region);

		for (User user : iam.listUsers().getUsers()) {
			System.out.println(user);
			ListAccessKeysRequest listAccessKeysRequest = new ListAccessKeysRequest().withUserName(user.getUserName());

			for (AccessKeyMetadata key : iam.listAccessKeys(listAccessKeysRequest).getAccessKeyMetadata()) {
				System.out.println(key);

				GetAccessKeyLastUsedRequest GetAccessKeyLastUsedRequest = new GetAccessKeyLastUsedRequest()
						.withAccessKeyId(key.getAccessKeyId());
				GetAccessKeyLastUsedResult result = iam.getAccessKeyLastUsed(GetAccessKeyLastUsedRequest);
				System.out.println(result);

				System.out.println("user : " + user.getUserName());
				System.out.println("key : " + key.getAccessKeyId());
				System.out.println("used : " + result.getAccessKeyLastUsed().getLastUsedDate());

				message = message + getMessageRow(key, result.getAccessKeyLastUsed());
			}
		}
		// sendSlack(webHook, message);
		System.out.println(message);
	}

	private static void sendSlack(String webHook, String message) {
		SlackApi api = new SlackApi(webHook);
		api.call(new SlackMessage("#general", "Mafagafo", "```" + message + "```"));
	}

	private static String getMessageRow(AccessKeyMetadata key, AccessKeyLastUsed used) {
		Date now = new Date();
		Date lastUsed = used.getLastUsedDate();
		
		long diff = 999;
		if(lastUsed != null){
			diff = (now.getTime() - lastUsed.getTime()) / (60 * 60 * 24 * 1000);
		}
		
		return String.format("%-20s\t%-20s\t%-7s\t%-35s\t%-35s\t%3d\n", key.getUserName(), key.getAccessKeyId(),
				key.getStatus(), key.getCreateDate(), used.getLastUsedDate(), diff);
	}
}
