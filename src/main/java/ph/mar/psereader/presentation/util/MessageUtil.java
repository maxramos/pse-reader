package ph.mar.psereader.presentation.util;

import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;

import org.primefaces.context.RequestContext;

public class MessageUtil {

	public static void sendInfo(String... messages) {
		send(FacesMessage.SEVERITY_INFO, messages);
	}

	public static void sendError(String... messages) {
		send(FacesMessage.SEVERITY_ERROR, messages);
	}

	public static void sendInfo(List<String> messages) {
		send(FacesMessage.SEVERITY_INFO, messages.toArray(new String[0]));
	}

	public static void sendError(List<String> messages) {
		send(FacesMessage.SEVERITY_ERROR, messages.toArray(new String[0]));
	}

	private static void send(Severity severity, String... messages) {
		StringBuilder builder = new StringBuilder();
		builder.append("<div style=\"float: right;\">");

		for (int i = 0; i < messages.length; i++) {
			builder.append(messages[i]);

			if (i < messages.length - 1) {
				builder.append("<br />");
			}
		}

		builder.append("</div>");
		RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(severity, "", builder.toString()));
	}
}
