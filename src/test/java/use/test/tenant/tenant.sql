create schema  urp;
create sequence urp.id;
select nextval('urp.id');

create table urp."UrpUser"(
id bigint,fid bigint,name varchar,
"tenantId" bigint,del bool, creator bigint, "createAt" timestamptz,updater bigint, "updateAt" timestamptz,"deleteAt" timestamptz
) partition by list ("tenantId");

create table urp."UrpRole"(
id   bigint,name varchar,
"tenantId" bigint,del bool, creator bigint, "createAt" timestamptz,updater bigint, "updateAt" timestamptz,"deleteAt" timestamptz
) partition by list ("tenantId");

create table urp."UrpPerm"(
id bigint,pid bigint,name varchar,perm varchar,"isMenu" boolean,
"tenantId" bigint,del bool, creator bigint, "createAt" timestamptz,updater bigint, "updateAt" timestamptz,"deleteAt" timestamptz
) partition by list ("tenantId");

create table urp."UrpUserRole"(
  id bigint,"userId" bigint,"roleId" bigint
,"tenantId" bigint,del bool, creator bigint, "createAt" timestamptz,updater bigint, "updateAt" timestamptz,"deleteAt" timestamptz
)  partition by list ("tenantId");

create table urp."UrpRolePerm"(
  id bigint,"roleId" bigint,"permId" bigint
,"tenantId" bigint,del bool, creator bigint, "createAt" timestamptz,updater bigint, "updateAt" timestamptz,"deleteAt" timestamptz
) partition by list ("tenantId");


create table if not exists  urp."UrpUser0" partition of urp."UrpUser" for values  IN (0);
create table if not exists  urp."UrpRole0" partition of urp."UrpRole" for values  IN (0);
create table if not exists  urp."UrpPerm0" partition of urp."UrpPerm" for values  IN (0);
create table if not exists  urp."UrpUserRole0" partition of urp."UrpUserRole" for values  IN (0);
create table if not exists  urp."UrpRolePerm0" partition of urp."UrpRolePerm" for values  IN (0);

-- 继承和分区不能一起用
/*create table public."Tenant"(id bigint, name text);

create table public."Record"(
"tenantId" bigint
,del bool, creator bigint, "createAt" timestamptz,updater bigint, "updateAt" timestamptz,"deleteAt" timestamptz);

create table urp."UrpUser"(
 id bigint,fid bigint,name varchar
 )inherits (public."Record");

create table "UrpRole"(
    id   bigint,
    name varchar
)inherits (public."Record");

create table "UrpPerm"(
 id bigint,pid bigint,name varchar,perm varchar,"isMenu" boolean
)inherits (public."Record");

create table UrpUserRole(
 id bigint,"userId" bigint,"roleId" bigint
)inherits (public."Record");

create table "UrpRolePerm"(
 id bigint,"roleId" bigint,"permId" bigint
)inherits (public."Record");


*/