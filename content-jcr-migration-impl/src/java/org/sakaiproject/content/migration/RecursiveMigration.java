package org.sakaiproject.content.migration;

import java.util.List;

import javax.jcr.LoginException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.migration.api.ContentToJCRCopier;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.jcr.api.JCRService;

/**
 * This is a version of the JCR Migration that just walks through the entire
 * Resources tree recursively and copies everything.  There is not persistent
 * queue, so this would only work if you have your system turned off from all
 * users.
 * 
 * In reality, this is mostly so I can debug and analyze some threading issues
 * I keep running into with the mixture of CHS and JackRabbit both being used
 * when copying.
 * 
 * @author sgithens
 *
 */
public class RecursiveMigration {
	private ContentHostingService contentHostingService;
	private JCRService jcrService;
	private ContentToJCRCopier contentToJCRCopier;

	public void runRecursiveMigration() throws IdUnusedException, TypeException, PermissionException, LoginException, RepositoryException {
		System.out.println("Running Recursive Migration");
		System.out.println("+ root");
		
		Session jcrSession = jcrService.getSession();
		
		ContentCollection root = contentHostingService.getCollection("/");
		List<String> children = root.getMembers();
		for (String id: children) {
			doContentResource(jcrSession, id, 1);
		}
	}
	
	public void doContentResource(Session session, String path, int depth) throws IdUnusedException, TypeException, PermissionException {
		StringBuilder strBuilder = new StringBuilder();
		for (int i = 0; i < depth*2; i++) {
			strBuilder.append(" ");
		}
		
		if (path.endsWith("/")) {
			String[] parts = path.split("/");
			strBuilder.append("+ " + parts[parts.length-1]);
			contentToJCRCopier.copyCollectionFromCHStoJCR(session, path);
			ContentCollection collection = contentHostingService.getCollection(path);
			List<String> children = collection.getMembers();
			for (String id: children) {
				doContentResource(session, id, depth+1);
			}
		}
		else {
			strBuilder.append("- " + path.substring(path.lastIndexOf("/")+1));
			contentToJCRCopier.copyResourceFromCHStoJCR(session, path);
		}
		
		System.out.println(strBuilder.toString());
	}
	
	/*
	 * Boilerplate Getters/Setters Below
	 */
	public void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}

	public void setJcrService(JCRService jcrService) {
		this.jcrService = jcrService;
	}

	public void setContentToJCRCopier(ContentToJCRCopier contentToJCRCopier) {
		this.contentToJCRCopier = contentToJCRCopier;
	}
	
}
