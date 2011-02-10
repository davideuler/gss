// This returns all subfolders (of any depth) of folder with id 3
with recursive subfolders(id) AS (
    select id from folder where parent_id=3
    UNION ALL
    select f.id from
    folder f, subfolders s
    where f.parent_id=s.id
)
select id from subfolders;

// This returns the line of ancestors of folder with id 1140
with recursive parent(id, parent_id) AS (
    select id, parent_id from folder where id=1140
    UNION ALL
    select f.id, f.parent_id from
    folder f, parent p
    where p.parent_id=f.id
)
select id from parent;