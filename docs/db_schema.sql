-- create database yubico;
-- grant all privileges on yubico.* to 'readwrite'@'localhost' identified by 'password';
-- flush privileges;

drop table if exists yubikeys; 
drop table if exists clients; 
drop table if exists perms; 

create table perms (
       id int not null auto_increment,

       verify_otp boolean default false,

       add_clients boolean default false,
       delete_clients boolean default false,

       add_keys boolean default false,
       delete_keys boolean default false,
       
       primary key (id)
) ENGINE=InnoDB;


create table clients (
       id int not null auto_increment,
       perm_id int not null,

       active boolean default false,
       created datetime not null,
       email varchar(255) unique not null,
       secret varchar(60) not null,

       primary key (id),
       foreign key (perm_id) references perms(id)
) ENGINE=InnoDB;


create table yubikeys (
       id int not null auto_increment,
       client_id int not null,

       active boolean default false,
       created datetime not null,
       accessed datetime not null,

       tokenId varchar(60) unique not null,
       userId varchar(60) unique not null,
       secret varchar(60) not null,

       counter int default 0,
       low int default 0,
       high int default 0,

       primary key (id),
       foreign key (client_id) references clients(id)
) ENGINE=InnoDB;

alter table yubikeys change tokenId tokenId varchar(60) binary;

insert into perms values(1,true,true,true,true,true);

insert into clients values(1,1,true,"1970-01-01T00:00:00Z",
      "root@yubico.com","571dmZQ9MJ5T983eDqhuOplnHk8=");

insert into clients values(2,1,true,"1970-01-01T00:00:00Z",
      "demokeys@yubico.com","kNapft02c1a81N4MEMDcC/mgcGc=");

