drop database if exists FinalProject;
create database FinalProject;
use FinalProject;

drop table if exists songList;
create table songList
(
 songID int primary key not null AUTO_INCREMENT,
 songName varchar(50),
 overallScore float
);

drop table if exists userCred;
create table userCred
(
 userID int primary key not null AUTO_INCREMENT,
 userName varchar(50) unique,
 password varchar(50) 
);


INSERT INTO songlist (songname, overallscore)
VALUES
 ('a', 5),
 ('b', 5),
 ('c', 5),
 ('d', 5),
 ('e', 5),
 ('f', 5);

INSERT INTO userCred (username, password) 
VALUES
 ('a', 1),
 ('b', 2),
 ('c', 3),
 ('d', 4),
 ('e', 5),
 ('f', 6);


drop procedure if exists login;
DELIMITER $$
CREATE PROCEDURE login (IN name varchar(50), IN code varchar(50), OUT count1 INT)
BEGIN 
	SELECT COUNT(*)
	INTO count1 
	FROM usercred u
	WHERE u.userName = name
    AND u.password = code;
END$$
DELIMITER ;

drop procedure if exists register_check;
DELIMITER $$
CREATE PROCEDURE register_check(IN name varchar(50), OUT count2 INT)
BEGIN 
	SELECT COUNT(*)
	INTO count2 
	FROM usercred u
	WHERE u.userName = name;
END$$
DELIMITER ;

drop table if exists profiles;
create table profiles
(
 username varchar(50) ,
 songName varchar(50) ,
 Score float,
UNIQUE `unique_index`(`username`, `songname`)
);

SELECT * from songlist;
SELECT * from usercred;
SELECT * from profiles;


