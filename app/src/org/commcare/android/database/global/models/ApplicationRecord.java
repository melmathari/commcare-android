/**
 * 
 */
package org.commcare.android.database.global.models;

import org.commcare.android.storage.framework.MetaField;
import org.commcare.android.storage.framework.Persisted;
import org.commcare.android.storage.framework.Persisting;
import org.commcare.android.storage.framework.Table;

/**
 * An Application Record tracks an individual CommCare app on the current
 * install.
 * 
 * @author ctsims
 *
 */
@Table("app_record")
public class ApplicationRecord extends Persisted {
    public static final String STORAGE_KEY = "app_record";

    public static final String META_STATUS = "status";
    
    public static final int STATUS_UNINITIALIZED = 0;
    public static final int STATUS_INSTALLED = 1;
    /**
     * The app needs to be upgraded from an old version
     */
    public static final int STATUS_SPECIAL_LEGACY = 2;
    
    @Persisting(1)
    String applicationId;
    @Persisting(2)
    @MetaField(META_STATUS)
    int status;
	@Persisting(3)
	String uniqueId;
	@Persisting(4)
	String displayName;
	@Persisting(5)
	boolean resourcesValidated;
	@Persisting(6)
	boolean isArchived;
	
	public ApplicationRecord() {
	    
	}
	
	public ApplicationRecord(String applicationId, int status) {
		this.applicationId = applicationId;
		this.status = status;
	}
	
	public String getApplicationId() {
        return applicationId;
    }
    
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
    
    public String getUniqueId() {
        return uniqueId;
    }
	
	public void setUniqueId(String id) {
		this.uniqueId = id;
	}
	
	public String getDisplayName() {
		//return this.displayName;
		//return "TEST_DISPLAY_NAME";
	    return uniqueId;
	}
	
	public void setDisplayName(String appName) {
	    this.displayName = appName;
	}
	
	public void setArchiveStatus(boolean b) {
		this.isArchived = b;
	}
	
	public boolean isArchived() {
		return this.isArchived;
	}
	
	@Override
	public String toString() {
		//TODO: change this to return displayName when it is available
		return this.uniqueId;
	}
	
	public void setResourcesStatus(boolean b) {
		this.resourcesValidated = b;
	}
	
	public boolean resourcesValidated() {
	    System.out.println("resourcesValidated returning " + this.resourcesValidated + " for AppRecord " + uniqueId);
		return this.resourcesValidated;
	}
	
}