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
