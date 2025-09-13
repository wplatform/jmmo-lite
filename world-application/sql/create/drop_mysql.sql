REVOKE ALL PRIVILEGES ON * . * FROM 'rainbowland'@'localhost';

REVOKE ALL PRIVILEGES ON `world` . * FROM 'rainbowland'@'localhost';

REVOKE GRANT OPTION ON `world` . * FROM 'rainbowland'@'localhost';

REVOKE ALL PRIVILEGES ON `characters` . * FROM 'rainbowland'@'localhost';

REVOKE GRANT OPTION ON `characters` . * FROM 'rainbowland'@'localhost';

REVOKE ALL PRIVILEGES ON `auth` . * FROM 'rainbowland'@'localhost';

REVOKE GRANT OPTION ON `auth` . * FROM 'rainbowland'@'localhost';

REVOKE ALL PRIVILEGES ON `hotfixes` . * FROM 'rainbowland'@'localhost';

REVOKE GRANT OPTION ON `hotfixes` . * FROM 'rainbowland'@'localhost';

DROP USER 'rainbowland'@'localhost';

DROP DATABASE IF EXISTS `world`;

DROP DATABASE IF EXISTS `characters`;

DROP DATABASE IF EXISTS `auth`;

DROP DATABASE IF EXISTS `hotfixes`;
