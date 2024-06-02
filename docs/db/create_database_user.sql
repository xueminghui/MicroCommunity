create database hc_community default character set utf8mb4;
create user 'hc_community'@'%' IDENTIFIED BY 'hc12345678';
GRANT CREATE,DROP,ALTER,INSERT,UPDATE,SELECT,DELETE on hc_community.* to 'hc_community'@'%' with grant OPTION;

create database TT default character set utf8mb4;
create user 'TT'@'%' IDENTIFIED BY 'hc12345678';
GRANT CREATE,DROP,ALTER,INSERT,UPDATE,SELECT,DELETE on TT.* to 'TT'@'%' with grant OPTION;

FLUSH PRIVILEGES;

