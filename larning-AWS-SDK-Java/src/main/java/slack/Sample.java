package slack;

import net.gpedro.integrations.slack.SlackApi;
import net.gpedro.integrations.slack.SlackMessage;

public class Sample {
	public static void main(String[] args) {
		SlackApi api = new SlackApi("https://hooks.slack.com/services/XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		api.call(new SlackMessage("#general", "Mafagafo", "`my message`"));
	}

}
