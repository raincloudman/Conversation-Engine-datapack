package conversationEngineLine;

import java.util.HashMap;
import java.util.LinkedList;

import conversationEngineInporter.CEStory;
import conversationEngineInporter.ConverzationNode;

public class IfScoreLine extends ConversationLine {

	private String score;
	private int target;

	public IfScoreLine(String score, int target, ConverzationNode node) {
		super(node);
		this.score = score;
		this.target = target;
	}

	public String toCommand(HashMap<String, ConverzationNode> nodes, CEStory ceStory, LinkedList<String> condition,
			String con) {
		int ifId = condition.size() - 1;// get the number of if statements at this time (-1 for the standard condition)
		ceStory.setNoNestedIfStatements(ifId); // update the max id (max behaviour is defined in the set method)
		condition.addLast(String.format("if score @s CE_if_%02d matches 1 ", ifId));
		return String.format(
				"    # if %s is %d\n%sif score @s CE_resend matches 0 run scoreboard players set @s CE_if_%02d 0\n%sif score @s CE_resend matches 0 if score @s %s matches %d run scoreboard players set @s CE_if_%02d 1\n",
				score, target, con, ifId, con, score, target, ifId);
	}

}