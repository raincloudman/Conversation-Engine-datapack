# each tick check if a player cicked a villager.
execute as @a[scores={CE_talking=1..}] run function conversation_engine:talking
# check if a conversation is going on
function conversation_engine:group/group
