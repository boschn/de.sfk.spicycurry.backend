/**
 * 
 */
package de.sfk.spicycurry.data;

import de.sfk.spicycurry.Setting;

/**
 * @author boris.schneider
 *
 */
public class PolarionParameter {

	public final static String PROPERTY_POLARION_BASEURL = "Polarion.BaseUrl";
	public final static String PROPERTY_POLARION_USERNAME = "Polarion.UserName";
	public final static String PROPERTY_POLARION_PASSWORD = "Polarion.Password";
	
	// singleton
	public static PolarionParameter Default = new PolarionParameter();
	
	private String baseUrl ; // "http://polarion1-automotive.server.technisat-digital/polarion/ws/services/";
	private String userName ; // "boris.schneider";
	private String passWord ; //  "Carconnect1!";
	
	private static final String[] standardFieldNames = {
			"id", 
			"title", 
			"description",
			"status", 
			"created", 
			"updated",
			"author.id",
			"project.id",
			"assignee.id",
			"type",
			"timePoint",
			"severity",
			"dueDate",
			"categories",
			"approvals",
			"comments",
			"workRecords",
			"linkedWorkItems",
			"linkedWorkItemsDerived",
			"externallyLinkedWorkItems",
			"linkedRevisions",
			"linkedRevisionsDerived"
	};
	
	private static String[] pleFieldNames = {
			// customfields
			"customFields.idKA",
			"customFields.descKA",
			"customFields.titleKA",
			"customFields.rif_lastUpdateKA",
			"customFields.customreqformKA.KEY",
			"customFields.brandOEM.KEY",
			"customFields.marketOEM.KEY",
			"customFields.validityKA",
			
			"customFields.updatePresent",
			"customFields.sourceModule",
			"customFields.kategorien.KEY", // Arbeitsgruppe
			"customFields.identification", // Quellen-ID
			
			"customFields.rif_FT_Tracking_Feature",
			"customFields.FT_Comment_1st-Tier",
			"customFields.FT_SOP1_Delivery_1st-Tier.KEY",
			"customFields.FT_SOP1_Asia_Delivery_1st-Tier.KEY",
			"customFields.FT_SOP1_EU_Delivery_1st-Tier.KEY",
			"customFields.rif_FT_SOP1_Status_OEM",
			
			"customFields.rif_Specific_1st-Tier",
			"customFields.SOP1_EU_1St-Tier.KEY",
			"customFields.SOP1_EU_OEM.KEY",
			"customFields.SOP1_NAR_1St-Tier.KEY",
			"customFields.SOP1_NAR_OEM.KEY",
			"customFields.SOP1_ASIA_1St-Tier.KEY",
			"customFields.SOP1_ASIA_OEM.KEY",
			"customFields.SOP_Comment_1st-Tier",
			"customFields.brand.KEY",
			"customFields.market.KEY",
			"customFields.updateType.KEY",
			"customFields.OPL_Responsible_1st-Tier.KEY",
			"customFields.rif_OPL_Status_OEM_1st-Tier.KEY",
			
			};

	private static String[] fullpleFieldNames = null;
	
	/**
	 * @return the internal polarion field names for ple
	 */
	public static String[] getPLEFieldNames()
	{
		if (fullpleFieldNames == null)
		{
			fullpleFieldNames = new String[standardFieldNames.length+pleFieldNames.length];
			System.arraycopy(standardFieldNames, 0, fullpleFieldNames, 0, standardFieldNames.length);
			System.arraycopy(pleFieldNames, 0, fullpleFieldNames, standardFieldNames.length, pleFieldNames.length);
		}
		
		return fullpleFieldNames;
	}
	/**
	 * @return the passWord
	 */
	public String getPassWord() {
		if (this.passWord == null) this.passWord =  Setting.Default.get(PROPERTY_POLARION_PASSWORD);
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
		if (this.userName == null) this.userName = Setting.Default.get(PROPERTY_POLARION_USERNAME);
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
		if (this.baseUrl == null) this.baseUrl = Setting.Default.get(PROPERTY_POLARION_BASEURL, "http://polarion1-automotive.server.technisat-digital/polarion/ws/services/");
		return baseUrl;
	}
}
