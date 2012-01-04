package net.nabaal.majiir.realtimerenderscpcommit;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.configuration.Configuration;

import net.nabaal.majiir.realtimerender.commit.RealtimeRenderCommitPlugin;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.xfer.FileSystemFile;

public class RealtimeRenderSCPCommit extends RealtimeRenderCommitPlugin {

	private static final Logger log = Logger.getLogger("Minecraft");
	
	private String hostname;
	private String username;
	private File keyFile;
	private String passphrase;
	private String remotePath;
	
	@Override
	public void onDisable() {
		super.onDisable();
		log.info("RealtimeRenderSCPCommit disabled.");
	}
	
	@Override
	public void onEnable() {
		super.onEnable();
		
		if (this.getDataFolder().mkdir()) {
			log.info(String.format("%s: created plugin data directory.", this.getDescription().getName()));
		}
		
		if (!new File(this.getDataFolder(), "config.yml").exists()) {
			this.saveDefaultConfig();
		}
		
		Configuration config = this.getConfig();
		
		hostname = config.getString("hostname");
		username = config.getString("username");
		keyFile = new File(this.getDataFolder(), config.getString("keyFile"));
		passphrase = config.getString("passphrase");
		remotePath = config.getString("remotePath");
		
		log.info("RealtimeRenderSCPCommit enabled.");
	}
	
	@Override
	public void commitFiles(Iterable<File> files) {
		SSHClient ssh = new SSHClient();
		try {
			try {
				ssh.loadKnownHosts();
				ssh.connect(hostname);
				ssh.authPublickey(username, ssh.loadKeys(keyFile.getAbsolutePath(), passphrase));
				ssh.useCompression();
				for (File file : files) {
					ssh.newSCPFileTransfer().upload(new FileSystemFile(file), remotePath);
				}
			} finally {
				ssh.disconnect();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
