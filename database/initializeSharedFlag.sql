update fileheader 
set shared=true
where id in (
select fileheader_id
from fileheader_permission
group by fileheader_id
having count(permissions_id)>1
)
or readforall=true;


update folder 
set shared=true
where id in (
select folder_id
from folder_permission
group by folder_id
having count(permissions_id)>1
)
or readforall=true;
