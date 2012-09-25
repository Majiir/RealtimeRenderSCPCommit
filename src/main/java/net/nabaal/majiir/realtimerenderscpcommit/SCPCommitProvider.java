package net.nabaal.majiir.realtimerenderscpcommit;

import java.io.File;
import java.io.IOException;

import net.nabaal.majiir.realtimerender.commit.CommitProvider;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.xfer.FileSystemFile;
import net.schmizz.sshj.xfer.scp.SCPFileTransfer;

public class SCPCommitProvider implements CommitProvider {
	
	private final String hostname;
	private final String remotePath;
	private final String username;
	private final String passphrase;
	private final File keyFile;
	private final File dataFolder;

	SCPCommitProvider(String hostname, String remotePath, String username, String passphrase, File keyFile, File dataFolder) {
		this.hostname = hostname;
		this.remotePath = remotePath;
		this.username = username;
		this.passphrase = passphrase;
		this.keyFile = keyFile;
		this.dataFolder = dataFolder;
	}

	@Override
	public void commitFiles(Iterable<File> files) {
		SSHClient ssh = new SSHClient();
		try {
			try {
				ssh.loadKnownHosts();
				ssh.connect(hostname);
				if (keyFile != null) {
					ssh.authPublickey(username, ssh.loadKeys(keyFile.getAbsolutePath(), passphrase));
				} else {
					ssh.authPassword(username, passphrase);
				}
				ssh.useCompression();
				SCPFileTransfer transfer = ssh.newSCPFileTransfer();
				SFTPClient client = ssh.newSFTPClient();
				for (File file : files) {
					String path = remotePath + dataFolder.toURI().relativize(file.getParentFile().toURI()).getPath();
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