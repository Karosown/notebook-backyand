create table note
(
    id         varchar(256)                       not null comment 'id'
        primary key,
    userId     bigint                             not null comment '所属用户ID',
    userNoteid bigint   default 0                 not null comment '用户日记ID',
    noteTitle  varchar(256)                       null,
    noteUrl    varchar(256)                       not null comment '日记地址',
    isPublic   tinyint  default 0                 not null comment '是否公开（0为否，1为公开）',
    viewNum    bigint   default 0                 not null comment '浏览量',
    IP         varchar(256)                       not null comment 'IP地址',
    thumbNum   bigint   default 0                 not null comment '点赞量',
    createTime datetime default CURRENT_TIMESTAMP not null comment '注册时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除（0否，1是）'
)
    comment '日记表';

create table notehistory
(
    id         varchar(256)                       not null comment '日记id',
    noteUrl    varchar(256)                       not null comment '日记地址',
    ip         varchar(256)                       not null comment 'ip地址',
    version    bigint                             not null comment '日记',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除（0否，1是）'
)
    comment '笔记历史';

create table notethumbrecords
(
    id        int auto_increment
        primary key,
    thumbTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '点赞时间',
    userId    bigint                             not null comment '用户ID',
    noteId    varchar(256)                       not null comment '笔记ID',
    constraint id
        unique (id)
)
    comment '笔记点赞记录表';

create table user
(
    id           bigint auto_increment comment 'id'
        primary key,
    userName     varchar(256)                           null comment '用户昵称',
    userAccount  varchar(256)                           not null comment '账号',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userMail     varchar(256)                           null comment '用户邮箱',
    gender       tinyint                                null comment '性别',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user / admin',
    userPassword varchar(512)                           not null comment '密码',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    constraint uni_userAccount
        unique (userAccount),
    constraint user_userMail_uindex
        unique (userMail)
)
    comment '用户';

