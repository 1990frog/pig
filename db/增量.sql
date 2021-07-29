alter table pig.sys_user
    add email varchar(255) null comment '邮箱';

alter table pig.sys_user
    add sys_class varchar(255) null comment '系统标识';

alter table pig.sys_role
    add sys_class varchar(255) null comment '所属系统';

