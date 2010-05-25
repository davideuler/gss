/*
 * Copyright 2009 Electronic Business Systems Ltd.
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
package gr.ebs.gss.client.rest.resource;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;

/**
 * @author kman
 */
public class OthersResource extends RestResource {

	public OthersResource(String aUri) {
		super(aUri);
	}

	List<String> others = new ArrayList<String>();
	List<OtherUserResource> otherUsers = new ArrayList<OtherUserResource>();

	/**
	 * Retrieve the others.
	 *
	 * @return the others
	 */
	public List<String> getOthers() {
		return others;
	}

	/**
	 * Modify the others.
	 *
	 * @param newOthers the others to set
	 */
	public void setOthers(List<String> newOthers) {
		others = newOthers;
	}

	public List<OtherUserResource> getOtherUsers() {
		return otherUsers;
	}

	public void setOtherUsers(List<OtherUserResource> newOtherUsers) {
		otherUsers = newOtherUsers;
	}

	@Override
	public void createFromJSON(String text) {
		JSONArray array = (JSONArray) JSONParser.parse(text);
		if (array != null)
			for (int i = 0; i < array.size(); i++) {
				JSONObject js = array.get(i).isObject();
				if (js != null) {
					String othersUri = unmarshallString(js, "uri");
					String username = unmarshallString(js, "username");
					if(othersUri != null){
						getOthers().add(othersUri);
						OtherUserResource r = new OtherUserResource(othersUri);
						r.setUsername(username);
						getOtherUsers().add(r);
					}
				}
			}
	}

	public String getUsernameOfUri(String u){
		if(!u.endsWith("/"))
			u=u+"/";
		for(OtherUserResource o : getOtherUsers()){
			GWT.log("CHECKING USER URI:"+o.getUri(), null);
			String toCheck = o.getUri();
			if(!toCheck.endsWith("/"))
				toCheck=toCheck+"/";
			if(toCheck.equals(u))
				return o.getUsername();
		}
		return null;
	}

	@Override
	public String getLastModifiedSince() {
		return null;
	}
}
