/**
 * 
 */
package de.sfk.spicycurry.data;

import de.sfk.spicycurry.Setting;

/**
 * @author boris.schneider
 *
 */
public class JiraParameter {
	public final static String PROPERTY_JIRA_BASEURL = "JIRA.BaseUrl";
	public final static String PROPERTY_JIRA_USERNAME = "JIRA.UserName";
	public final static String PROPERTY_JIRA_PASSWORD = "JIRA.Password";
	
	// singleton
	public static JiraParameter Default = new JiraParameter();
	
	private String baseUrl ; 
	private String userName ; 
	private String passWord ; 

	/**
	 * @return the passWord
	 */
	public String getPassWord() {
		if (this.passWord == null) this.passWord =  Setting.Default.get(PROPERTY_JIRA_PASSWORD);
		return this.passWord;
	}

	/**
	 * @param passWord the passWord to set
	 */
	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}

	/**
	 * get the username
	 * 
	 * @return the userName
	 */
	public String getUserName() {
		if (this.userName == null) this.userName = Setting.Default.get(PROPERTY_JIRA_USERNAME);
		return userName;
	}

	/**
	 * set the username
	 * 
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the baseurl
	 */
	public String getBaseUrl() {
		if (this.baseUrl == null) this.baseUrl = Setting.Default.get(PROPERTY_JIRA_BASEURL, "http://JIRA3.technisat-digital/");
		return baseUrl;
	}
}
