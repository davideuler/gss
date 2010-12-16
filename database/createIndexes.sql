create INDEX idx_folder_owner_id ON folder (owner_id);
create INDEX idx_folder_parent_id ON folder (parent_id);
create index idx_folder_modifiedby_id on folder (modifiedby_id);
create index idx_folder_createdby_id on folder (createdby_id);

create INDEX idx_filetag_userid ON filetag ( userid);
create index idx_filetag_modifiedby_id on filetag (modifiedby_id);
create index idx_filetag_createdby_id on filetag (createdby_id);
create index idx_filetag_fileid on filetag (fileid);

create index idx_filebody_header_id on filebody (header_id );
create index idx_filebody_modifiedby_id on filebody (modifiedby_id );
create index idx_filebody_createdby_id on filebody (createdby_id);

create index idx_fileheader_modifiedby_id on fileheader(modifiedby_id);
create index idx_fileheader_folder_id on fileheader(folder_id);
create index idx_fileheader_currentbody_id on fileheader(currentbody_id);
create index idx_fileheader_createdby_id on fileheader(createdby_id);
create index idx_fileheader_owner_id on fileheader(owner_id);

CREATE INDEX idx_accountinginfo_user_id ON accountinginfo(user_id);

CREATE INDEX idx_fileheader_permission_fileheader_id ON fileheader_permission(fileheader_id);
CREATE INDEX idx_fileheader_permission_permissions_id ON fileheader_permission(permissions_id);

CREATE INDEX idx_fileuploadstatus_owner_id ON fileuploadstatus(owner_id);

CREATE INDEX idx_folder_permission_folder_id ON folder_permission(folder_id);
CREATE INDEX idx_folder_permission_permissions_id ON folder_permission(permissions_id);

CREATE INDEX idx_gss_user_userclass_id ON gss_user(userclass_id);
CREATE INDEX idx_gss_user_createdby_id ON gss_user(createdby_id);
CREATE INDEX idx_gss_user_modifiedby_id ON gss_user(modifiedby_id);

CREATE INDEX idx_gss_group_owner_id ON gss_group(owner_id);
CREATE INDEX idx_gss_group_modifiedby_id ON gss_group(modifiedby_id);
CREATE INDEX idx_gss_group_createdby_id ON gss_group(createdby_id);

CREATE INDEX idx_gss_group_gss_user_members_id ON gss_group_gss_user(members_id);
CREATE INDEX idx_gss_group_gss_user_groupsMember_id ON gss_group_gss_user(groupsmember_id);

CREATE INDEX idx_invitation_user_id ON invitation(user_id);

CREATE INDEX idx_nonce_userid ON nonce(userid);

CREATE INDEX idx_permission_group_id ON permission(group_id);
CREATE INDEX idx_permission_user_id ON permission(user_id);
