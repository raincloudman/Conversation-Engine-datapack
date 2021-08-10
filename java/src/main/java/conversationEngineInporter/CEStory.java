package conversationEngineInporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * the Conversation Engine Story class stores the different npc's and is called
 * to generate the datapack.
 * 
 * @author Sijmen_v_b
 * 
 */
public class CEStory {
	private LinkedList<NPCGroup> groups;
	private static String name = "exported datapack";
	private HashMap<String, ConverzationNode> nodes;

	public CEStory(LinkedList<NPCGroup> groups, HashMap<String, ConverzationNode> nodes) {
		this.groups = groups;
		this.nodes = nodes;
	}

	public void generateDatapack() {
		deletePreviousDatapack();
		loadEmptyDatapack();
		createInitFunction();
		createPlayerLogOnFunction();// TODO
		createGroupFolder();
		createMessagesFolder();
		createVillagerFolder();
	}

	private void createInitFunction() {
		// add standard scoreboards
		String s = "# detect when a player richt clicks a villager\nscoreboard objectives add CE_talking minecraft.custom:minecraft.talked_to_villager\n# scoreboard for storing sucsesses of functions to do conditionals\nscoreboard objectives add CE_suc dummy\nscoreboard objectives add CE_suc2 dummy\nscoreboard objectives add CE_resend dummy\n# scoreboard for limiting recursion\nscoreboard objectives add CE_rec dummy\n\n# trigger scoreboard to be acsessed by players without permissions.\nscoreboard objectives add CE_trigger trigger\n# a way to store the current node in the dialouge tree. used to prevent players from jumping unexpectedly by using /trigger manually.\nscoreboard objectives add CE_current_node dummy\n";

		// Initialise all the groups
		s += "\n# groups for optimazation to be used by fake player CE_mannager\n";
		for (NPCGroup npcGroup : groups) {
			s += String.format("scoreboard objectives add CE_group_%02d dummy\n", npcGroup.getGroupId());
		}

		// Initialise all the groups
		s += "\n# scoreboard for villagers to be used by fake player CE_mannager\n";
		for (NPCGroup npcGroup : groups) {
			for (NPC npc : npcGroup.getNpcs()) {
				s += String.format("scoreboard objectives add %s dummy\n", npc.getName());
			}
		}

		// detect player joining
		s += "\n# scoreboard for detecting if a player joins the game\nexecute unless entity @a run scoreboard objectives remove CE_leave_game\n# not fully shure what the one above does but it got reccomended to me and it can't hurt.\nscoreboard objectives add CE_leave_game custom:leave_game\n";

		// reset the score for all players
		s += "\n# reset the scores for all players:\nexecute as @a run function conversation_engine:player_log_on\n";

		// Initialise message
		s += "\nsay conversation engine initialized";

		SaveAsFile(s, String.format("%s\\data\\conversation_engine\\functions\\init.mcfunction", name));
	}

	private void createPlayerLogOnFunction() {
		// set the leave scoreboard
		String s = "# called by players who just log in again.\n\n# reset the CE_leave_game scoreboard \nscoreboard players set @s CE_leave_game 0\n";

		// reset the groups scoreboards
		s += "\n# reset the scoreboards\n    # groups\n";
		for (NPCGroup npcGroup : groups) {
			s += String.format("scoreboard players set @s CE_group_%02d 0\n", npcGroup.getGroupId());
		}

		// reset the villager scoreboards
		s += "\n    # scoreboard for villagers\n";
		for (NPCGroup npcGroup : groups) {
			for (NPC npc : npcGroup.getNpcs()) {
				s += String.format("scoreboard players set @s %s 0\n", npc.getName());
			}
		}
		
		//welcome back message
		s += "\n\nsay welcome back!";
		
		SaveAsFile(s, String.format("%s\\data\\conversation_engine\\functions\\player_log_on.mcfunction", name));
	}

	// ---- delete previous datapack ----

	/**
	 * deletes previous datapack with same name if it exists.
	 * 
	 * @param name
	 */
	private void deletePreviousDatapack() {
		try {
			deleteDirectory(System.getProperty("user.dir") + "\\" + name);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("There was no previous datapack to delete.");
		}
	}

	// ---- delete previous datapack ----
	// ---- load empty datapack ----

	/**
	 * copys the empty datapack to the root directory
	 * 
	 * @param name
	 */
	private void loadEmptyDatapack() {
		try {
			copyDirectory(System.getProperty("user.dir") + "\\src\\main\\resources\\datapack empty",
					System.getProperty("user.dir") + "\\" + name);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// ---- load empty datapack ----
	// ---- create group folder ----

	private void createGroupFolder() {
		createDirectory(name + "\\data\\conversation_engine\\functions\\group");
		createGroupFunction();
		createOpenFunction();
		createCloseFunction();

	}

	private void createOpenFunction() {
		for (NPCGroup npcGroup : groups) {
			String s = npcGroup.createOpenFunction();

			// try to save the file.
			SaveAsFile(s, String.format("%s\\data\\conversation_engine\\functions\\group\\%03d.mcfunction", name,
					npcGroup.getGroupId()));

		}

	}

	private void createCloseFunction() {
		for (NPCGroup npcGroup : groups) {
			String s = npcGroup.createCloseFunction();

			// try to save the file.
			SaveAsFile(s, String.format("%s\\data\\conversation_engine\\functions\\group\\close_%03d.mcfunction", name,
					npcGroup.getGroupId()));

		}

	}

	private void createGroupFunction() {
		// start the function with some comments
		String s = "# run by server\n\n# this is for grouping of villagers so we don't have to check each villager each tick only each group.\n\n# check if there is a conversation in a group:\n";
		for (NPCGroup npcGroup : groups) {// for each group create the execute command with the correct group id.
			s += String.format(
					"execute if score CE_mannager CE_group_%02d matches 1 run function conversation_engine:group/%03d\n",
					npcGroup.getGroupId(), npcGroup.getGroupId());
		}

		// try to save the file.
		SaveAsFile(s, name + "\\data\\conversation_engine\\functions\\group\\group.mcfunction");

	}

	// ---- create group folder ----
	// ---- create messages folder ----

	private void createMessagesFolder() {
		createDirectory(name + "\\data\\conversation_engine\\functions\\messages");
		createTalkFunction();
		// for every villager create their named messages folder.
		for (NPCGroup npcGroup : groups) {
			for (NPC npc : npcGroup.getNpcs()) {
				createNpcFolder(npcGroup, npc);
			}
		}
	}

	private void createTalkFunction() {

		String s = "# always run by the NPC\n\n# this function checks wich NPC the player has clicked\n";

		// for each villager
		for (NPCGroup npcGroup : groups) {
			for (NPC npc : npcGroup.getNpcs()) {
				s += String.format("\nexecute at @s[tag=%s] run function conversation_engine:messages/%s/start",
						npc.getName(), npc.getName());
			}
		}

		SaveAsFile(s, name + "\\data\\conversation_engine\\functions\\messages\\talk.mcfunction");

	}

	private void createNpcFolder(NPCGroup npcGroup, NPC npc) {
		// create a directory with the name of the npc.
		createDirectory(String.format("%s\\data\\conversation_engine\\functions\\messages\\%s", name, npc.getName()));
		createNpcStartFunction(npcGroup, npc);
		createNpcEndFunction(npcGroup, npc);
		createNpcTickFunction(npcGroup, npc);// TODO

		// get all the nodes of this villager to functions

		for (ConverzationNode converzationNode : npc.getNodes()) {
			createNodeFunction(npcGroup, npc, converzationNode);
		}
	}

	private void createNpcStartFunction(NPCGroup npcGroup, NPC npc) {
		Boolean force1TalkingAtATime = true;
		int n = 0;
		if (force1TalkingAtATime) {
			n = 2;
		}

		String s = "# always run by the NPC\n\n# this function starts the conversation with a npc\n\n# reset the boolean.\n";
		s += "scoreboard players set bool CE_suc2 0\n# if the player is already talking to this villager\n";
		s += String.format(
				"    execute if entity @p[scores={CE_talking=1,%s=1}] run scoreboard players set bool CE_suc2 1\n    # make the tellraw the same as the last message so it repeats.\n    execute if score bool CE_suc2 matches 1 as @p[scores={CE_talking=1,%s=1}] run scoreboard players operation @s CE_trigger = @s CE_current_node\n",
				npc.getName(), npc.getName());
		s += String.format(
				"# unless there is already someone else talking to the villager  (note that 2 as boolean is also true) \n# TIP: turn the 2(on the next line) into a 0 if you want multiple people to talk to the same npc at the same time\nexecute if score bool CE_suc2 matches 0 if entity @a[scores={%s=1}] run scoreboard players set bool CE_suc2 %01d \n    execute if score bool CE_suc2 matches 2 run tellraw @p[scores={CE_talking=1}] [{\"selector\":\"@a[scores={%s=1}]\"},{\"text\":\"[someone is already talking to this NPC]\",\"color\":\"gray\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"you'll have to wait your turn.\"}]}}]\n",
				npc.getName(), n, npc.getName());
		s += String.format(
				"# else:\n\n    # start the %s conversation\n    execute if score bool CE_suc2 matches 0 run scoreboard players set CE_mannager CE_group_%02d 1\n    execute if score bool CE_suc2 matches 0 run scoreboard players set CE_mannager %s 1\n",
				npc.getName(), npcGroup.getGroupId(), npc.getName());
		s += String.format(
				"\n    # set the %s score to 1 for the player.\n    execute if score bool CE_suc2 matches 0 as @p[scores={CE_talking=1}] run scoreboard players set @s %s 1\n",
				npc.getName(), npc.getName());
		s += String.format(
				"\n    # also set the current node back to 0 \n    execute if score bool CE_suc2 matches 0 run scoreboard players set @p[scores={%s=1}] CE_current_node 0\n",
				npc.getName());

		// make sure you will start the correct node.
		s += String.format(
				"\n    # give the choises using the trigger command.\n    execute if score bool CE_suc2 matches 0 run scoreboard players set @p[scores={%s=1}] CE_trigger %d \n",
				npc.getName(), npc.GetStartingNodeId());

		s += "# set talking back to 0\nscoreboard players set @p[scores={CE_talking=1}] CE_talking 0";

		SaveAsFile(s, String.format("%s\\data\\conversation_engine\\functions\\messages\\%s\\start.mcfunction", name,
				npc.getName()));
	}

	private void createNpcEndFunction(NPCGroup npcGroup, NPC npc) {
		String s = "# run as the player\n\n# this function ends the conversation with a npc\n\n# stop the labrat conversation\n";
		s += String.format(
				"scoreboard players set CE_mannager CE_group_%02d 0\nscoreboard players set CE_mannager %s 0\n",
				npcGroup.getGroupId(), npc.getName());
		s += "\n# reset the last node \nscoreboard players set @s CE_current_node 0\n# also reset the trigger\nscoreboard players set @s CE_trigger 0\n\n# set lab_rat score of player that was talking to this villager back to 0\n";
		s += String.format("scoreboard players set @s %s 0\n\nsay [ended the converstaion]", npc.getName());

		SaveAsFile(s, String.format("%s\\data\\conversation_engine\\functions\\messages\\%s\\end.mcfunction", name,
				npc.getName()));

	}

	private void createNpcTickFunction(NPCGroup npcGroup, NPC npc) {
		String s = String.format(
				"# always run as the player talking with the villager (scores={%s = 1})\n\n# this function is run each tick if someone is talking to this NPC\n\n# check if the player is in range of the npc if not end the conversation.\n",
				npc.getName());
		s += String.format(
				"execute at @s unless entity @e[type=villager, distance = ..7, tag=%s] run function conversation_engine:messages/%s/end\n",
				npc.getName(), npc.getName());
		s += "\n# check for trigger\n";
		// check for all the triggers
		for (ConverzationNode converzationNode : npc.getNodes()) {
			s += String.format(
					"execute as @s[scores={CE_trigger = %d}] run function conversation_engine:messages/%s/%s\n",
					converzationNode.getId(), npc.getName(), converzationNode.getName());
		}
		s += "\n# set trigger back to 0\nscoreboard players set @s CE_trigger 0\n\n\n\n# set the current node, do not do this here\n";

		SaveAsFile(s, String.format("%s\\data\\conversation_engine\\functions\\messages\\%s\\tick.mcfunction", name,
				npc.getName()));
	}

	private void createNodeFunction(NPCGroup npcGroup, NPC npc, ConverzationNode converzationNode) {
		String s = String.format(
				"# run as the player\n\n# message id: %d\n\n# reset the sucsess scoreboard\nscoreboard players set bool CE_suc 0\nscoreboard players set bool CE_resend 0\n",
				converzationNode.getId());
		s += "\n# check if the player came from a valid previous node (to prevent manual use of /trigger)\n";
		// check if you come from a valid node (to prevent manual use of the /trigger
		// command)
		for (int id : converzationNode.getValidInpointerIds(nodes)) {
			s += String.format(
					"execute if score @s CE_current_node matches %d run scoreboard players set bool CE_suc 1\n", id);
		}
		s += "\n";

		// check if the previous node is itself
		s += String.format(
				"# special case in case the previous node is itself in that case CE_resend of bool is set to 1 (use this to prevent commands that for example give items are executed twice)\nexecute store success score bool CE_resend if score @s CE_current_node matches %d run scoreboard players set bool CE_suc 1\n\n",
				converzationNode.getId());

		// give the dialogue and choices
		s += "    # give the choices\n";
		s += converzationNode.toCommand(nodes);
		s += "\n\n";

		// update the last run node
		s += "    # update the last run node\n    execute if score bool CE_suc matches 1 run scoreboard players operation @s CE_current_node = @s CE_trigger\n";

		SaveAsFile(s, String.format("%s\\data\\conversation_engine\\functions\\messages\\%s\\%s.mcfunction", name,
				npc.getName(), converzationNode.getName()));
	}

	// ---- create messages folder ----
	// ---- create villager folder ----

	private void createVillagerFolder() {
		createDirectory(name + "\\data\\conversation_engine\\functions\\villager");
		createKillFolder();
		createSummonFolder();
	}

	private void createKillFolder() {
		createDirectory(name + "\\data\\conversation_engine\\functions\\villager\\kill");
		createkillAllFunction();
		// for every villager create kill function
		for (NPCGroup npcGroup : groups) {
			for (NPC npc : npcGroup.getNpcs()) {
				String s = String.format("# kill the labrat npc\nkill @e[type=villager,tag=CE_npc,tag=%s]",
						npc.getName());

				SaveAsFile(s, String.format("%s\\data\\conversation_engine\\functions\\villager\\kill\\%s.mcfunction",
						name, npc.getName()));

			}
		}
	}

	private void createkillAllFunction() {
		String s = "# kill all npc's\nkill @e[type=villager,tag=CE_npc]";
		SaveAsFile(s, name + "\\data\\conversation_engine\\functions\\villager\\kill\\all.mcfunction");
	}

	private void createSummonFolder() {
		createDirectory(name + "\\data\\conversation_engine\\functions\\villager\\summon");
		// for every villager create summon function
		for (NPCGroup npcGroup : groups) {
			for (NPC npc : npcGroup.getNpcs()) {
				String s = String.format(
						"# summon a villager with a name a tag equal to the name (space becomes _ ) and the CE_npc tag \nsummon villager ~ ~ ~ {Tags:[\"CE_npc\",\"%s\"],Invulnerable:1b,CustomNameVisible:1b,NoAI:1b,CanPickUpLoot:0b,CustomName:'{\"text\":\"%s\",\"color\":\"white\"}',VillagerData:{profession:\"minecraft:nitwit\"},Offers:{}}",
						npc.getName(), npc.getName().replace('_', ' '));

				SaveAsFile(s, String.format("%s\\data\\conversation_engine\\functions\\villager\\summon\\%s.mcfunction",
						name, npc.getName()));

			}
		}
	}

	// ---- create villager folder ----
	// ---- save string as file ----

	private void SaveAsFile(String s, String path) {
		try {
			PrintWriter out = new PrintWriter(path);
			out.write(s);
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// ---- save string as file ----
	// ---- create directory ----

	private void createDirectory(String dirpath) {
		File f = new File(dirpath);
		if (!f.exists()) {
			f.mkdir();
		}
	}

	// ---- create directory ----
	// ---- copy directory ---- source: https://www.baeldung.com/java-copy-directory

	public static void copyDirectory(String sourceDirectoryLocation, String destinationDirectoryLocation)
			throws IOException {
		Files.walk(Paths.get(sourceDirectoryLocation)).forEach(source -> {
			Path destination = Paths.get(destinationDirectoryLocation,
					source.toString().substring(sourceDirectoryLocation.length()));
			try {
				Files.copy(source, destination);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	// ---- copy directory ---- source: https://www.baeldung.com/java-copy-directory
	// ---- delete directory ---- source:
	// https://softwarecave.org/2018/03/24/delete-directory-with-contents-in-java/

	private void deleteDirectory(String dirpath) throws IOException {
		File file = new File(dirpath);
		deleteDirectoryRecursionJava6(file);
	}

	private void deleteDirectoryRecursionJava6(File file) throws IOException {
		if (file.isDirectory()) {
			File[] entries = file.listFiles();
			if (entries != null) {
				for (File entry : entries) {
					deleteDirectoryRecursionJava6(entry);
				}
			}
		}
		if (!file.delete()) {
			throw new IOException("Failed to delete " + file);
		}
	}
	// ---- delete directory ---- source:
	// https://softwarecave.org/2018/03/24/delete-directory-with-contents-in-java/
}
