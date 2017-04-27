package de.sfk.spicycurry.data;

import java.sql.Timestamp;

import javax.persistence.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Embeddable
public class Attachment {

	private static Logger logger = LogManager.getLogger(Attachment.class);
	
	@Transient
	private static final long serialVersionUID = 1L;
	
	// @Id
	// @Column(name="attachment_uid")
	// @GeneratedValue(strategy = GenerationType.IDENTITY)
    // private Long uid;
	
	
	@Column(name="attachment_id", length = 2048)
	private String id;
	
	@Column(name="attachment_url",length = 5120)
	private String url;
	
	@Column(name="attachment_filename",length = 1024)
	private String filename;
	
	@Column(name="attachment_uri", length = 5012)
	private String uri;
	
	/**
	 * constructor
	 */
	public Attachment() {
		super();
	}
	/**
	 * constructor
	 * @param id
	 * @param url
	 * @param filename
	 * @param uri
	 */
	public Attachment(String id, String url, String filename, String uri) {
		super();
		this.setId(id);
		this.setUrl(url);
		this.setFilename(filename);
		this.setUri(uri);
	}
	/**
	 * @return the uid
	 */
	// protected Long getUid() {
	// 	return uid;
	//}
	/**
	 * @param uid the uid to set
	 */
	// protected void setUid(Long uid) {
	//  this.uid = uid;
	// }
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * @return the url
	 */
	public String getUrl() {
		return this.url;
	}
	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}
	/**
	 * @param filename the filename to set
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}
	/**
	 * @return the uri
	 */
	public String getUri() {
		return this.uri;
	}
	/**
	 * @param uri the uri to set
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}
	
}
