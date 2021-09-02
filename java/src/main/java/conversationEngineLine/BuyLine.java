package conversationEngineLine;

import java.util.HashMap;
import java.util.LinkedList;

import conversationEngineInporter.CEStory;
import conversationEngineInporter.ConverzationNode;
import conversationEngineInporter.NPC;

public class BuyLine extends ConversationLine {

	String getItem;
	int getAmmount;
	String payItem;
	int payAmmount;

	public BuyLine(String getItem, int getAmmount, String payItem, int payAmmount, ConverzationNode node) {
		super(node);
		this.getItem = getItem;
		this.getAmmount = getAmmount;
		this.payItem = payItem;
		this.payAmmount = payAmmount;
	}

	public String toCommand(HashMap<String, ConverzationNode> nodes, CEStory ceStory, NPC npc, LinkedList<String> condition,
			String con) {
		String s =String.format("    scoreboard players set @s CE_buy_count 0\n%sif score @s CE_resend matches 0 store success score @s CE_buy_count run clear @s %s 0\n", con,payItem); 
		return String.format("%s    execute if score @s CE_buy_count matches %d.. run clear @s %s %d\n    execute if score @s CE_buy_count matches %d.. run give @s %s %d\n", s, payAmmount,payItem,payAmmount,payAmmount,getItem,getAmmount);
	}

}