-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema oasip
-- -----------------------------------------------------
CREATE USER 'ssi2dev'@'%' IDENTIFIED BY 'ssi2@22@54@109';
GRANT SELECT,UPDATE,INSERT,DELETE ON oasip.* TO 'ssi2dev'@'%';
FLUSH PRIVILEGES;
-- -----------------------------------------------------
-- Schema oasip
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `oasip` DEFAULT CHARACTER SET utf8 ;
USE `oasip` ;

-- -----------------------------------------------------
-- Table `oasip`.`event_category`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `oasip`.`event_category` (
  `eventCatagoryName` VARCHAR(100) NOT NULL,
  `eventCategoryDesc` VARCHAR(500) NULL,
  `eventDuration` INT NOT NULL,
	CHECK (eventDuration BETWEEN 1 AND 480),
  PRIMARY KEY (`eventCatagoryName`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `oasip`.`event`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `oasip`.`event` (
  `bookingId` INT NOT NULL AUTO_INCREMENT,
  `bookingName` VARCHAR(100) NOT NULL,
  `bookingEmail` VARCHAR(50) NOT NULL,
  `eventCategory` VARCHAR(100) NOT NULL,
  `eventStartTime` DATETIME NOT NULL,
  `eventDuration` INT NOT NULL,
  `eventNotes` LONGTEXT NULL,
  INDEX `fk_event_event_category_idx` (`eventCategory` ASC) VISIBLE,
  PRIMARY KEY (`bookingId`),
  CONSTRAINT `fk_event_event_category`
    FOREIGN KEY (`eventCategory`)
    REFERENCES `oasip`.`event_category` (`eventCatagoryName`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

