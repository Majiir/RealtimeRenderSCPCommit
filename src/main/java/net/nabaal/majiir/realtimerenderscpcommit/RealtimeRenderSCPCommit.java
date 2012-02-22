package net.nabaal.majiir.realtimerenderscpcommit;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;

import net.nabaal.majiir.realtimerender.RealtimeRender;
import net.nabaal.majiir.realtimerender.commit.CommitProvider;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.xfer.FileSystemFile;
import net.schmizz.sshj.xfer.scp.SCPFileTransfer;

public class RealtimeRenderSCPCommit extends JavaPlugin implements CommitProvider {

	private static final Logger log = Logger.getLogger("Minecraft");
	
	private String hostname;
	private String username;
	private File keyFile;
	private String passphrase;
	private String remotePath;
	private RealtimeRender plugin;
	
	@Override
	public void onDisable() {
		log.info("RealtimeRenderSCPCommit disabled.");
	}
	
	@Override
	public void onEnable() {		
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
		
		if (!remotePath.endsWith(File.separator)) {
			remotePath += File.separator;
		}
		
		plugin = ((RealtimeRender)this.getServer().getPluginManager().getPlugin("RealtimeRender")); 
		plugin.registerCommitPlugin(this);
		
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
				SCPFileTransfer transfer = ssh.newSCPFileTransfer();
				SFTPClient client = ssh.newSFTPClient();
				for (File file : files) {
					String path = remotePath + plugin.getDataFolder().toURI().relativize(file.getParentFile().toURI()).getPath();
					client.mkdirs(path);
					transfer.upload(new FileSystemFile(file), path);
				}
			} finally {
				ssh.disconnect();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
