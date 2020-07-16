-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

-- -----------------------------------------------------
-- Table `auction`.`ROLE`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `ROLE` (
  `ID` INT(2) NOT NULL AUTO_INCREMENT,
  `NAME` VARCHAR(45) CHARACTER SET 'utf8' COLLATE 'utf8_unicode_ci' NOT NULL,
  `CREATE_TIME` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_unicode_ci;


-- -----------------------------------------------------
-- Table `auction`.`USER`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `USER` (
  `ID` BIGINT(11) NOT NULL AUTO_INCREMENT,
  `CREATE_TIME` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `USERNAME` VARCHAR(16) CHARACTER SET 'utf8' COLLATE 'utf8_unicode_ci' NOT NULL,
  `EMAIL` VARCHAR(255) CHARACTER SET 'utf8' COLLATE 'utf8_unicode_ci' NOT NULL,
  `PASSWORD` VARCHAR(32) CHARACTER SET 'utf8' COLLATE 'utf8_unicode_ci' NOT NULL,
  PRIMARY KEY (`ID`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_unicode_ci;


-- -----------------------------------------------------
-- Table `auction`.`USER_ROLE`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `USER_ROLE` (
  `ID` BIGINT(11) NOT NULL AUTO_INCREMENT,
  `CREATE_TIME` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `USER_ID` BIGINT(11) NOT NULL,
  `ROLE_ID` INT(2) NOT NULL,
  PRIMARY KEY (`ID`),
  INDEX `fk_USER_ROLE_USER1_idx` (`USER_ID` ASC),
  INDEX `fk_USER_ROLE_ROLE1_idx` (`ROLE_ID` ASC),
  CONSTRAINT `fk_USER_ROLE_USER1`
    FOREIGN KEY (`USER_ID`)
    REFERENCES `auction`.`USER` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_USER_ROLE_ROLE1`
    FOREIGN KEY (`ROLE_ID`)
    REFERENCES `auction`.`ROLE` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_unicode_ci;


-- -----------------------------------------------------
-- Table `auction`.`TORRENT`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `TORRENT` (
  `ID` BIGINT NOT NULL,
  `NAME` VARCHAR(1024) NOT NULL,
  `LOCAL_PATH` VARCHAR(16) NOT NULL,
  `TOTAL_SIZE` BIGINT NOT NULL,
  PRIMARY KEY (`ID`))
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
