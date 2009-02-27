/*
 * Copyright 2008, 2009 Electronimport gr.ebs.gss.client.GSS;

import java.io.Serializable;
import java.util.Date;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;

 * (at your option) any later version.
 *
 * GSS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GSS.  If not, see <http://www.gnu.org/licenses/>.
 */
package gr.ebs.gss.client.rest.resource;

import java.util.Date;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;


/**
 * @author kman
 *
 */
public class UserResource extends RestResource{

	/**
	 * @param path
	 */
	public UserResource(String path) {
		super(path);
	}

	private String name;
	private String username;
	private String email;
	private Date creationDate;
	private Date modificationDate;
	private String filesPath;
	private String trashPath;
	private String sharedPath;
	private String othersPath;
	private String tagsPath;
	private String groupsPath;
	private QuotaHolder quota;

	/**
	 * Retrieve the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Modify the name.
	 *
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Retrieve the username.
	 *
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Modify the username.
	 *
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Retrieve the email.
	 *
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Modify the email.
	 *
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Retrieve the creationDate.
	 *
	 * @return the creationDate
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * Modify the creationDate.
	 *
	 * @param creationDate the creationDate to set
	 */
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * Retrieve the modificationDate.
	 *
	 * @return the modificationDate
	 */
	public Date getModificationDate() {
		return modificationDate;
	}

	/**
	 * Modify the modificationDate.
	 *
	 * @param modificationDate the modificationDate to set
	 */
	public void setModificationDate(Date modificationDate) {
		this.modificationDate = modificationDate;
	}

	/**
	 * Retrieve the filesPath.
	 *
	 * @return the filesPath
	 */
	public String getFilesPath() {
		return filesPath;
	}

	/**
	 * Modify the filesPath.
	 *
	 * @param filesPath the filesPath to set
	 */
	public void setFilesPath(String filesPath) {
		this.filesPath = filesPath;
	}

	/**
	 * Retrieve the trashPath.
	 *
	 * @return the trashPath
	 */
	public String getTrashPath() {
		return trashPath;
	}

	/**
	 * Modify the trashPath.
	 *
	 * @param trashPath the trashPath to set
	 */
	public void setTrashPath(String trashPath) {
		this.trashPath = trashPath;
	}

	/**
	 * Retrieve the sharedPath.
	 *
	 * @return the sharedPath
	 */
	public String getSharedPath() {
		return sharedPath;
	}

	/**
	 * Modify the sharedPath.
	 *
	 * @param sharedPath the sharedPath to set
	 */
	public void setSharedPath(String sharedPath) {
		this.sharedPath = sharedPath;
	}

	/**
	 * Retrieve the othersPath.
	 *
	 * @return the othersPath
	 */
	public String getOthersPath() {
		return othersPath;
	}

	/**
	 * Modify the othersPath.
	 *
	 * @param othersPath the othersPath to set
	 */
	public void setOthersPath(String othersPath) {
		this.othersPath = othersPath;
	}

	/**
	 * Retrieve the tagsPath.
	 *
	 * @return the tagsPath
	 */
	public String getTagsPath() {
		return tagsPath;
	}

	/**
	 * Modify the tagsPath.
	 *
	 * @param tagsPath the tagsPath to set
	 */
	public void setTagsPath(String tagsPath) {
		this.tagsPath = tagsPath;
	}

	/**
	 * Retrieve the groupsPath.
	 *
	 * @return the groupsPath
	 */
	public String getGroupsPath() {
		return groupsPath;
	}

	/**
	 * Modify the groupsPath.
	 *
	 * @param groupsPath the groupsPath to set
	 */
	public void setGroupsPath(String groupsPath) {
		this.groupsPath = groupsPath;
	}




	/**
	 * Retrieve the quota.
	 *
	 * @return the quota
	 */
	public QuotaHolder getQuota() {
		return quota;
	}



	/**
	 * Modify the quota.
	 *
	 * @param quota the quota to set
	 */
	public void setQuota(QuotaHolder quota) {
		this.quota = quota;
	}


	public void createFromJSON(String text){
		JSONObject json = (JSONObject) JSONParser.parse(text);
		email = json.get("email").isString().stringValue();
		name = json.get("name").isString().stringValue();
		username = json.get("username").isString().stringValue();
		filesPath = json.get("files").isString().stringValue();
		groupsPath = json.get("groups").isString().stringValue();
		othersPath = json.get("others").isString().stringValue();
		sharedPath = json.get("shared").isString().stringValue();
		tagsPath = json.get("tags").isString().stringValue();
		trashPath  = json.get("trash").isString().stringValue();
		if(json.get("creationDate") != null)
			creationDate = new Date(new Long(json.get("creationDate").toString()));
		if(json.get("modificationDate") != null)
			modificationDate = new Date(new Long(json.get("modificationDate").toString()));
		if(json.get("quota") != null){
			JSONObject qj = (JSONObject) json.get("quota");
			quota = new QuotaHolder();
			quota.setFileCount(new Long(qj.get("totalFiles").toString()));
			quota.setFileSize(new Long(qj.get("totalBytes").toString()));
			quota.setQuotaLeftSize(new Long(qj.get("bytesRemaining").toString()));
		}

	}

	public String toString(){
		String res = email+"\n"+name+"\n"+username+"\n"+filesPath+"\n"+groupsPath;
		return res;
	}


}
