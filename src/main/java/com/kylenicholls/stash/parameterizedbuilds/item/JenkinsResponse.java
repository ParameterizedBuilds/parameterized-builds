package com.kylenicholls.stash.parameterizedbuilds.item;

import java.util.LinkedHashMap;
import java.util.Map;

public class JenkinsResponse {
	private boolean error;
	private boolean prompt;
	private String messageText;

	public boolean getError() {
		return error;
	}

	public boolean getPrompt() {
		return prompt;
	}

	public String getMessageText() {
		return messageText;
	}
	
	public Map<String, Object> getMessage(){
		Map<String, Object> data = new LinkedHashMap<>();
		data.put("error", this.error);
		data.put("prompt", this.prompt);
		data.put("messageText", this.messageText);
		return data;
	}

	public static class JenkinsMessage {
		private boolean error = false;
		private boolean prompt = false;
		private String messageText = "";

		public JenkinsMessage error(boolean error) {
			this.error = error;
			return this;
		}

		public JenkinsMessage prompt(boolean prompt) {
			this.prompt = prompt;
			return this;
		}

		public JenkinsMessage messageText(String messageText) {
			this.messageText = messageText;
			return this;
		}

		public JenkinsResponse build() {
			return new JenkinsResponse(this);
		}
	}

	private JenkinsResponse(JenkinsMessage message) {
		this.error = message.error;
		this.prompt = message.prompt;
		this.messageText = message.messageText;
	}
}
