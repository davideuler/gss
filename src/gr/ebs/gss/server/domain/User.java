/*
 * Copyright 2007, 2008, 2009, 2010 Electronic Business Systems Ltd.
 *
 * This file is part of GSS.
 *
 * GSS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
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
package gr.ebs.gss.server.domain;

import static gr.ebs.gss.server.configuration.GSSConfigurationFactory.getConfiguration;
import gr.ebs.gss.server.domain.dto.UserDTO;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Version;


/**
 * The class that holds information about a particular user of the system.
 *
 * @author past
 */
@Entity
public class User implements Serializable {

	/**
	 * Length of generated random password.
	 */
	private static final int PASSWORD_LENGTH = 15;

	/**
	 * These characters will be used to generate random password.
	 */
	private static final String allowedPasswordCharacters =
			"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

	/**
	 * The authentication token size in bytes.
	 */
	private static final int TOKEN_SIZE = 40;

	/**
	 * The persistence ID of the object.
	 * XXX: we must generate unique ids ourselves, if type is not ObjectId,
	 * so we do it in the constructor
	 */
	@Id
	private Long id;

	/**
	 * XXX: The constructor is only necessary for enforcing unique ids. If/When
	 * id is converted to ObjectId this will no longer be necessary.
	 */
	public User() {
		id = new Random().nextLong();
	}

	/**
	 * Version field for optimistic locking.
	 * XXX: this is not yet supported by Morphia.
	 */
	@SuppressWarnings("unused")
	@Version
	private long version;

	/**
	 * The audit information.
	 */
	@Embedded
	private AuditInfo auditInfo;

	/**
	 * The first name of the user.
	 */
	private String firstname;

	/**
	 * The last name of the user.
	 */
	private String lastname;

	/**
	 * The full name of the user.
	 */
	private String name;

	/**
	 * The username of the user.
	 */
//	@Column(unique = true)
	private String username;

	/**
	 * The e-mail address of the user.
	 */
	private String email;

	/**
	 * A unique ID provided by the Shibboleth IdP.
	 */
	@SuppressWarnings("unused")
	private String identityProviderId;

	/**
	 * The IdP URL.
	 */
	@SuppressWarnings("unused")
	private String identityProvider;

	/**
	 * The date and time the user last logged into the service.
	 */
//	@Temporal(TemporalType.TIMESTAMP)
	private Date lastLogin;

	/**
	 * The list of groups that have been specified by this user.
	 */
//	@OneToMany(cascade = CascadeType.ALL, mappedBy = "owner")
//	@OrderBy("name")
	private List<Group> groupsSpecified;

	/**
	 * The set of groups of which this user is member.
	 */
//	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "members")
	private Set<Group> groupsMember;

	/**
	 * The list of all tags this user has specified on all files.
	 */
//	@OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
//	@OrderBy("tag")
	private List<FileTag> fileTags;

	/**
	 * The user class to which this user belongs.
	 */
//	@ManyToOne
	private UserClass userClass;

	/**
	 * The authentication token issued for this user.
	 */
	private byte[] authToken;

	/**
	 * The time that the user's issued authentication token
	 * will expire.
	 */
//	@Temporal(TemporalType.TIMESTAMP)
	private Date authTokenExpiryDate;

	/**
	 * The active nonce issued for logging in this user, in
	 * Base64 encoding.
	 */
	private String nonce;

	/**
	 * The active nonce expiry date.
	 */
	private Date nonceExpiryDate;

	/**
	 * Flag that denotes whether the user has accepted the terms and
	 * conditions of the service.
	 * XXX: the columnDefinition is postgres specific, if deployment
	 * database is changed this shall be changed too
	 */
//	@Column(columnDefinition=" boolean DEFAULT false")
	private boolean acceptedPolicy;

	/**
	 * A flag that denotes whether the user is active or not. Users may be
	 * administratively forbidden to use the service by setting this flag to
	 * false.
	 */
//	@Column(columnDefinition=" boolean DEFAULT true")
	private boolean active;

	/**
	 * Password for WebDAV
	 */
	private String webDAVPassword;

	/**
	 * Retrieve the firstname.
	 *
	 * @return the firstname
	 */
	public String getFirstname() {
		return firstname;
	}

	/**
	 * Modify the firstname.
	 *
	 * @param newFirstname the firstname to set
	 */
	public void setFirstname(final String newFirstname) {
		firstname = newFirstname;
	}

	/**
	 * Retrieve the lastname.
	 *
	 * @return the lastname
	 */
	public String getLastname() {
		return lastname;
	}

	/**
	 * Modify the lastname.
	 *
	 * @param newLastname the lastname to set
	 */
	public void setLastname(final String newLastname) {
		lastname = newLastname;
	}

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
	 * @param newName the name to set
	 */
	public void setName(final String newName) {
		name = newName;
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
	 * @param newEmail the email to set
	 */
	public void setEmail(final String newEmail) {
		email = newEmail;
	}

	/**
	 * Retrieve the id.
	 *
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Retrieve the groups specified by this user.
	 *
	 * @return the groups
	 */
	public List<Group> getGroupsSpecified() {
		return groupsSpecified;
	}

	/**
	 * Modify the groups specified by this user.
	 *
	 * @param newGroupsSpecified the groups to set
	 */
	public void setGroupsSpecified(final List<Group> newGroupsSpecified) {
		groupsSpecified = newGroupsSpecified;
	}

	/**
	 * Retrieve the groups of which this user is member.
	 *
	 * @return the groups
	 */
	public Set<Group> getGroupsMember() {
		return groupsMember;
	}

	/**
	 * Modify the groups of which this user is member.
	 *
	 * @param newGroupsMember the groups to set
	 */
	public void setGroupsMember(final Set<Group> newGroupsMember) {
		groupsMember = newGroupsMember;
	}

	/**
	 * Retrieve the audit info.
	 *
	 * @return the audit info
	 */
	public AuditInfo getAuditInfo() {
		return auditInfo;
	}

	/**
	 * Modify the audit info.
	 *
	 * @param newAuditInfo the new audit info
	 */
	public void setAuditInfo(final AuditInfo newAuditInfo) {
		auditInfo = newAuditInfo;
	}

	/**
	 * Retrieve the file tags.
	 *
	 * @return a list of file tags
	 */
	public List<FileTag> getFileTags() {
		return fileTags;
	}

	/**
	 * Replace the list of file tags.
	 *
	 * @param newFileTags the new file tags
	 */
	public void setFileTags(final List<FileTag> newFileTags) {
		fileTags = newFileTags;
	}

	/**
	 * Retrieve the user class.
	 *
	 * @return the user class
	 */
	public UserClass getUserClass() {
		return userClass;
	}

	/**
	 * Modify the user class.
	 *
	 * @param newUserClass the new user class
	 */
	public void setUserClass(final UserClass newUserClass) {
		userClass = newUserClass;
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
	 * @param aUsername the username to set
	 */
	public void setUsername(String aUsername) {
		username = aUsername;
	}

	/**
	 * Modify the lastLogin.
	 *
	 * @param aLastLogin the lastLogin to set
	 */
	public void setLastLogin(Date aLastLogin) {
		lastLogin = aLastLogin;
	}

	/**
	 * Retrieve the lastLogin.
	 *
	 * @return the lastLogin
	 */
	public Date getLastLogin() {
		return lastLogin;
	}

	/**
	 * Retrieve the acceptedPolicy flag.
	 *
	 * @return the acceptedPolicy
	 */
	public boolean hasAcceptedPolicy() {
		return acceptedPolicy;
	}
	/**
	 * Modify the acceptedPolicy flag.
	 *
	 * @param newAcceptedPolicy the acceptedPolicy to set
	 */
	public void setAcceptedPolicy(boolean newAcceptedPolicy) {
		acceptedPolicy = newAcceptedPolicy;
	}


	public String getWebDAVPassword() {
		return webDAVPassword;
	}
	public void setWebDAVPassword(String aWebDAVPassword1) {
		webDAVPassword = aWebDAVPassword1;
	}

	// ********************** Business Methods ********************** //




	/**
	 * Modify the identityProviderId.
	 *
	 * @param anIdentityProviderId the identityProviderId to set
	 */
	public void setIdentityProviderId(String anIdentityProviderId) {
		identityProviderId = anIdentityProviderId;
	}


	/**
	 * Modify the identityProvider.
	 *
	 * @param anIdentityProvider the identityProvider to set
	 */
	public void setIdentityProvider(String anIdentityProvider) {
		identityProvider = anIdentityProvider;
	}

	/**
	 * Retrieve the authentication token. If it is not valid
	 * or non-existent, this method returns null. Therefore, call
	 * sites must request a regeneration of the authentication
	 * token in both cases.
	 *
	 * @return the authToken
	 */
	public byte[] getAuthToken() {
		if (isAuthTokenValid())
			return authToken;
		return null;
	}

	/**
	 * Add a tag from this user to specified file.
	 *
	 * @param file the file
	 * @param tag the tag string
	 */
	public void addTag(final FileHeader file, final String tag) {
		@SuppressWarnings("unused")
		final FileTag fileTag = new FileTag(this, file, tag);
		// Cascade should take care of saving here.
	}

	/**
	 * Return a Data Transfer Object for this User object.
	 *
	 * @return a user DTO
	 */
	public UserDTO getDTO() {
		UserDTO u = new UserDTO();
		u.setId(id);
		u.setName(name);
		u.setLastname(lastname);
		u.setFirstname(firstname);
		u.setEmail(email);
		u.setUsername(username);
		u.setActive(active);
		if(userClass!= null)
			u.setUserClass(userClass.getDTOWithoutUsers());
		u.setLastLoginDate(lastLogin);
		return u;
	}

	/**
	 * Removes a group from this user's specified groups list.
	 *
	 * @param group the group to remove
	 * @throws IllegalArgumentException if group is null
	 */
	public void removeSpecifiedGroup(final Group group) {
		if (group == null)
			throw new IllegalArgumentException("Can't remove a null group.");
		getGroupsSpecified().remove(group);
		group.setOwner(null);
	}

	/**
	 * @param name2
	 */
	public void createGroup(final String name2) {
		final Group group = new Group(name2);
		group.setOwner(this);
		final Date now = new Date();
		final AuditInfo ai = new AuditInfo();
		ai.setCreatedBy(this);
		ai.setCreationDate(now);
		ai.setModifiedBy(this);
		ai.setModificationDate(now);
		group.setAuditInfo(ai);
		groupsSpecified.add(group);
	}

	/**
	 * Removes the specified tag from this user
	 *
	 * @param tag
	 */
	public void removeTag(FileTag tag) {
		fileTags.remove(tag);
		tag.setUser(null);
	}

	/**
	 * Creates a new authentication token and resets
	 * its expiry date.
	 */
	public void generateAuthToken() {
		SecureRandom random = new SecureRandom();
		authToken = new byte[TOKEN_SIZE];
		random.nextBytes(authToken);
		Calendar cal = Calendar.getInstance();
		// Set token time-to-live to the number of days specified in
		// gss.properties.
		cal.add(Calendar.DAY_OF_MONTH, getConfiguration().getInt("tokenTTL", 1));
		authTokenExpiryDate = cal.getTime();
	}

	/**
	 * Return true if the authentication token is usable, or false
	 * if a new one must be regenerated.
	 *
	 * @return true if the authentication token is valid
	 */
	private boolean isAuthTokenValid() {
		if (authToken == null)
			return false;
		if (authTokenExpiryDate == null)
			return false;
		if (authTokenExpiryDate.before(new Date()))
			return false;
		return true;
	}

	/**
	 * Request the invalidation of the authentication token.
	 * After this method is called, a new token must be generated.
	 */
	public void invalidateAuthToken() {
		authToken = null;
		authTokenExpiryDate = null;
	}

	/**
	 * Retrieve the nonce. If it is not valid or non-existent,
	 * this method returns null.
	 *
	 * @return the nonce
	 */
	public String getNonce() {
		if (isNonceValid())
			return nonce;
		return null;
	}

	/**
	 * Return true if the nonce is usable, or false
	 * if not.
	 *
	 * @return true if the nonce is valid
	 */
	private boolean isNonceValid() {
		if (nonce == null)
			return false;
		if (nonceExpiryDate == null)
			return false;
		if (nonceExpiryDate.before(new Date()))
			return false;
		return true;
	}

	/**
	 * Modify the nonce.
	 *
	 * @param aNonce the nonce to set
	 */
	public void setNonce(String aNonce) {
		nonce = aNonce;
	}

	/**
	 * Modify the nonce expiry date.
	 *
	 * @param aNonceExpiryDate the nonce expiry date to set
	 */
	public void setNonceExpiryDate(Date aNonceExpiryDate) {
		nonceExpiryDate = aNonceExpiryDate;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean isActive) {
		active = isActive;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof User)) return false;
		User user = (User) o;
		return user.getUsername().equals(username) && user.getName().equals(name);
	}

	@Override
	public int hashCode() {
		return 37 * username.hashCode() + name.hashCode();
	}

	public void generateWebDAVPassword() {
		Random random = new Random();
		StringBuilder sb = new StringBuilder();
		int length = allowedPasswordCharacters.length();
		for (int i=0; i<PASSWORD_LENGTH; i++) {
			int j = random.nextInt(length);
			sb.append(allowedPasswordCharacters.charAt(j));
		}
		webDAVPassword = sb.toString();
	}
}
