CREATE TABLE IF NOT EXISTS Guild (GuildID UNSIGNED BIGINT, GuildProperties JSON, PRIMARY KEY (GuildID)); 
--Guild json is as follows: {long[] DunceRoles, bool DunceActive, string DunceName, long JailRole, long JailChannel, bool MalwareActive, bool LoudActive}
CREATE UNIQUE INDEX IF NOT EXISTS Guilds on Guild (GuildID);
CREATE TABLE IF NOT EXISTS RoleList (MemberID UNSIGNED BIGINT, Roles JSON, PRIMARY KEY (MemberID));
--Roles json is as follows: { guild: {
--										ID: long,
--										Roles: long[]
--									 }[]
--							}

CREATE INDEX IF NOT EXISTS MemberRoles on RoleList (MemberID);
