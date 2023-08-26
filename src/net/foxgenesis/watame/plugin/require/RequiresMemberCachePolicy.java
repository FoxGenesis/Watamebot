package net.foxgenesis.watame.plugin.require;

import net.dv8tion.jda.api.utils.MemberCachePolicy;

public interface RequiresMemberCachePolicy {

	public MemberCachePolicy getPolicy();
}
