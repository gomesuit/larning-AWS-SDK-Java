package sample.iam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
		if (args.length != 1) {
			return;
		}

		String webHook = args[0];
		List<Message> messages = new ArrayList<>();

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

				messages.add(createMessage(key, result.getAccessKeyLastUsed()));
			}
		}
		// sendSlack(webHook, message);
		printSlack(messages);
	}

	private static void printSlack(List<Message> messages) {
		String msg = "";
		Collections.reverse(messages);
		for (Message message : messages) {
			msg = msg + formatMessage(message);
		}
		System.out.println(msg);
	}

	private static void sendSlack(String webHook, List<Message> messages) {
		String msg = "";
		Collections.reverse(messages);
		for (Message message : messages) {
			msg = msg + formatMessage(message);
		}

		SlackApi api = new SlackApi(webHook);
		api.call(new SlackMessage("#general", "Mafagafo", "```" + msg + "```"));
	}

	private static String formatMessage(Message message) {
		return String.format("%-20s\t%-20s\t%-7s\t%-35s\t%-35s\t%3d\n", message.getUserName(), message.getAccessKeyId(),
				message.getStatus(), message.getCreateDate(), message.getLastUsedDate(), message.getDiff());
	}

	private static Message createMessage(AccessKeyMetadata key, AccessKeyLastUsed used) {
		Date now = new Date();
		Date lastUsed = used.getLastUsedDate();

		long diff = 999;
		if (lastUsed != null) {
			diff = (now.getTime() - lastUsed.getTime()) / (60 * 60 * 24 * 1000);
		}

		Message message = new Message(key.getUserName(), key.getAccessKeyId());
		message.setStatus(key.getStatus());
		message.setCreateDate(key.getCreateDate());
		message.setLastUsedDate(used.getLastUsedDate());
		message.setDiff(diff);

		return message;
	}

	private static class Message implements Comparable<Message> {
		private String userName;
		private String accessKeyId;
		private String status;
		private Date createDate;
		private Date lastUsedDate;
		private long diff;

		public Message(String userName, String accessKeyId) {
			this.userName = userName;
			this.accessKeyId = accessKeyId;
		}

		public String getUserName() {
			return userName;
		}

		public String getAccessKeyId() {
			return accessKeyId;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public Date getCreateDate() {
			return createDate;
		}

		public void setCreateDate(Date createDate) {
			this.createDate = createDate;
		}

		public Date getLastUsedDate() {
			return lastUsedDate;
		}

		public void setLastUsedDate(Date lastUsedDate) {
			this.lastUsedDate = lastUsedDate;
		}

		public long getDiff() {
			return diff;
		}

		public void setDiff(long diff) {
			this.diff = diff;
		}

		@Override
		public int compareTo(Message other) {
			return (int) (this.diff - other.getDiff());
		}
	}
}
