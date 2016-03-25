package net.TheDgtl.Stargate;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class pmListener implements PluginMessageListener {

	@Override
	public void onPluginMessageReceived(String channel, Player unused, byte[] message) {
		if (!Stargate.enableBungee || !channel.equals("BungeeCord")) return;
		
		// Get data from message
		String inChannel;
		byte[] data;
		try {
			DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
			inChannel = in.readUTF();
			short len = in.readShort();
			data = new byte[len];
			in.readFully(data);
		} catch (IOException ex) {
			Stargate.log.severe("[Stargate] Error receiving BungeeCord message");
			return;
		}
		
		// Verify that it's an SGBungee packet
		if (!inChannel.equals("SGBungee")) {
			return;
		}
		
		// Data should be player name, and destination gate name
		String msg = new String(data);
		String[] parts = msg.split("#@#");
		
		String playerName = parts[0];
		String destination = parts[1];
		
		// Check if the player is online, if so, teleport, otherwise, queue
		Player player = Stargate.server.getPlayer(playerName);
		if (player == null) {
			Stargate.bungeeQueue.put(playerName.toLowerCase(), destination);
		} else {
			Portal dest = Portal.getBungeeGate(destination);
			// Specified an invalid gate. For now we'll just let them connect at their current location
			if (dest == null) {
				Stargate.log.log(Level.INFO, "[Stargate] Bungee gate {0} does not exist", destination);
				return;
			}
			dest.teleport(player, dest, null);
		}
	}
}
