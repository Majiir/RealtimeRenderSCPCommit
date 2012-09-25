package net.nabaal.majiir.realtimerenderscpcommit;

import java.io.File;

import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;

import net.nabaal.majiir.realtimerender.RealtimeRender;
import net.schmizz.sshj.common.SecurityUtils;

public class RealtimeRenderSCPCommit extends JavaPlugin {

	@Override
	public void onDisable() {
		this.getLogger().info("Commit provider may remain active even though the registering plugin is disabled.");
	}
	
	@Override
	public void onEnable() {		
		if (this.getDataFolder().mkdir()) {
			this.getLogger().info(String.format("%s: created plugin data directory.", this.getDescription().getName()));
		}
		
		if (!new File(this.getDataFolder(), "config.yml").exists()) {
			this.saveDefaultConfig();
		}
		
		Configuration config = this.getConfig();
		
		String hostname = config.getString("hostname");
		String username = config.getString("username");
		boolean useBouncyCastle = config.getBoolean("useBouncyCastle");
		String keyFileString = config.getString("keyFile");
		File keyFile = keyFileString != null ? new File(this.getDataFolder(), keyFileString) : null; 
		String passphrase = config.getString("passphrase");
		String remotePath = config.getString("remotePath");
		
		if (!remotePath.endsWith(File.separator)) {
			remotePath += File.separator;
		}
		
		SecurityUtils.setRegisterBouncyCastle(Boolean.valueOf(useBouncyCastle));
		
		RealtimeRender plugin = ((RealtimeRender)this.getServer().getPluginManager().getPlugin("RealtimeRender")); 
		plugin.registerCommitPlugin(new SCPCommitProvider(hostname, remotePath, username, passphrase, keyFile, plugin.getDataFolder()));
	}
	
}
