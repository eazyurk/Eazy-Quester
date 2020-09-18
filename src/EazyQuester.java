
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.constants.Banks;
import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;
import java.util.function.BooleanSupplier;

@ScriptManifest(name = "Eazy Goblin Diplomacy", author = "eazyurk", info = "Finishes Goblin Diplomacy", version = 0.1, logo = "")
public final class EazyQuester extends Script {
	private Area goblinVillage;
    private Area goblinLoot;
    private Area wyson;
    private Area aggie;
    private Area redberries;
    private Area onionFarm;
    private Boolean itemsGathered;

    private Area[] area = {
    		new Area(2954, 3512, 2961, 3508), // goblin village
    		new Area(3241, 3250, 3264, 3225), // goblin loot
    		new Area(3024, 3382, 3029, 3377), // wyson
    		new Area(3083, 3261, 3088, 3256), // aggie
    		new Area(3275, 3375, 3286, 3370), // redberries
    		new Area(3184, 3269, 3192, 3265), // onion farm
    		new Area(3210, 3219, 3207, 3217).setPlane(2) // Lumbridge Bank
    };


    public EazyQuester() {
        goblinVillage = area[0];
        goblinLoot = area[1];
        wyson = area[2];
        aggie = area[3];
        redberries = area[4];
        onionFarm = area[5];
        itemsGathered = false;
    }
	
    @Override
    public final int onLoop() throws InterruptedException {
    	switch (getConfigs().get(62)) {
		case 0:
			if (itemsGathered == false ) {
				if (getInventory().getAmount("Onion") < 2 && (!getInventory().contains("Yellow dye") && !getInventory().contains("Orange dye"))) {
    				getOnion();
    			} else if (getInventory().getAmount("Goblin mail") < 3) {
    				lootGoblinArea("Goblin mail");
    			} else if (getInventory().getAmount("Coins") < 35 && (getInventory().getAmount("Woad leaf") < 2 && !getInventory().contains("Blue dye"))) {
    				lootGoblinArea("Coins");
    			} else if (getInventory().getAmount("Redberries") < 3 && (!getInventory().contains("Red dye") && !getInventory().contains("Orange dye"))) {
    				getRedberry();
    			} else if (getInventory().getAmount("Woad leaf") < 2 && (!getInventory().contains("Blue dye"))) {
    				getWoadLeaves();
    			} else if (!getInventory().contains("Blue dye") || !getInventory().contains("Orange dye")) {
    				getDyes();
    			} else {
    				itemsGathered = true;
    			}
			} else {
				startQuest();
			}
			break;
		case 3:
			if (!isOnCutScene()) {
    			if (!getInventory().contains("Orange goblin mail")) {
    				dyeGoblinMail("Orange");
    			} else {
    				String[] options = new String[] {
    						"I have some orange armour here"
    				};
    				talkToNpc("General Wartface", options);
    			}
			} else {
				getDialogues().completeDialogue();
			}
			break;
		case 4:
			if (!isOnCutScene()) {
    			if (!getInventory().contains("Blue goblin mail")) {
    				dyeGoblinMail("Blue");
    			} else {
    				String[] options = new String[] {
    						"I have some blue armour here"
    				};
    				talkToNpc("General Wartface", options);
    			}
			} else {
				getDialogues().completeDialogue();
			}
			break;
		case 5:
			if (!isOnCutScene()) {
    			String[] options = new String[] {
						"I have some brown armour here"
				};
				talkToNpc("General Wartface", options);
			} else {
				getDialogues().completeDialogue();
			}
			break;
		case 6:
			getWidgets().closeOpenInterface();
			log("Quest has already been completed");
		default:
			return 6;
	}
    return 1;
    }
    
    private void walkToArea(Area area, String location) {
        log("Walking to: " + location);
        if (!area.contains(myPosition())) {
            getWalking().webWalk(area);
            new ConditionalSleep(5000) {
                @Override
                public boolean condition() throws InterruptedException {
                    return area.contains(myPosition());
                }
            }.sleep();
        } else {
            log("Already in Area: " + location);
        }
    }


    private void startQuest() throws InterruptedException {
    	if (!goblinVillage.contains(myPosition())) {
    		walkToArea(goblinVillage, "Goblin village");
    	} else {
    		RS2Object largeDoor = objects.closest("Large Door");
    		
    		if (largeDoor != null && largeDoor.hasAction("Open")) {
    			largeDoor.interact("Open");
    		} else {
        		String[] options = new String[]{
        				"No, he doesn't look fat",
                        "Do you want me to pick an armour colour for you?",
                        "What about a different colour?",
        				};
    			talkToNpc("General Wartface", options);
    		}
    	}
    }
    
    private void getOnion() {
    	if (!onionFarm.contains(myPosition())) {
        	walkToArea(onionFarm, "Onion farm");
    	} else {
    		RS2Object farmGate = objects.closest("Gate");
    		RS2Object item = objects.closest("Onion");
    		
    		if (farmGate != null && farmGate.hasAction("Open")) {
    			farmGate.interact("Open");
    		} else if (item != null) {
                if (item.interact("Pick")) {
                    // sleep until the inventory contains a the item picked
                    new ConditionalSleep(5000) {
                        @Override
                        public boolean condition() throws InterruptedException {
                            return getInventory().contains("Onion");
                        }
                    }.sleep();
                    log("Onion picked!");
                }
            } else {
                log("Onion not found!");
            }
    	}
    }
    
    private void lootGoblinArea(String itemName) {
    	if (!goblinLoot.contains(myPosition())) {
    		walkToArea(goblinLoot, "Goblin loot");
    	} else {
    		GroundItem item = groundItems.closest(goblinLoot, itemName);
    		RS2Object door = objects.closest("Door");
    		if (door != null && door.hasAction("Open")) {
    			door.interact("Open");
    		} else if (item != null && item.exists()) {
				item.interact("Take");
			} else {
				walking.webWalk(goblinLoot.getRandomPosition());
			}
    	}
    }
    
    private void getRedberry() {
    	if (!redberries.contains(myPosition())) {
    		walkToArea(redberries, "Redberries");
    	} else {
    		RS2Object berryBush = objects.closest("Redberry bush");
    		
    		if (berryBush != null) {
    			 if (berryBush.interact("Pick-from")) {
			    	new ConditionalSleep(5000) {
			            @Override
			            public boolean condition() throws InterruptedException {
			                return getInventory().contains("Cadava berries");
			            }
			        }.sleep();
			    }
    		}
    	}
    }
    
    private void getWoadLeaves() throws InterruptedException {
    	if (!wyson.contains(myPosition())) {
    		walkToArea(wyson, "Wyson the farmer");
    	} else {
    		String[] options = new String[]{
                    "Yes please, I need woad leaves.",
                    "How about 20 coins?"};
    		talkToNpc("Wyson the gardener", options);
    	}
    }
    
    private void getDyes() throws InterruptedException {
    	if (!aggie.contains(myPosition())) {
    		walkToArea(aggie, "Aggie");
    	} else {
    		RS2Object door = objects.closest("Door");
    		if (door != null && door.hasAction("Open")) {
    			door.interact("Open");
    		} else {
    			if (!getInventory().contains("Blue dye")) {
        			String[] options = new String[]{
                            "Can you make dyes for me please?",
                            "What do you need to make blue dye?",
                            "Okay, make me some blue dye please."};
        			talkToNpc("Aggie", options);
    			} else if (!getInventory().contains("Yellow dye")) {
        			String[] options = new String[]{
                            "Can you make dyes for me please?",
                            "What do you need to make yellow dye?",
                            "Okay, make me some yellow dye please."};
        			talkToNpc("Aggie", options);
    			} else if (!getInventory().contains("Red dye")) {
        			String[] options = new String[]{
                            "Can you make dyes for me please?",
                            "What do you need to make red dye?",
                            "Okay, make me some red dye please."};
        			talkToNpc("Aggie", options);
    			} else {
    				getInventory().interactWithNameThatContains("Use", "Red dye");
    				getInventory().interactWithNameThatContains("Use", "Yellow dye");
    			}
    		}
    	}
    }
    
    private void talkToNpc(String npcName, String[] options) throws InterruptedException {
    	NPC npc = npcs.closest(npcName);
		
		if (npc != null) {
            if (!getDialogues().inDialogue()) {
                log("Not talking to " + npcName);
                npc.interact("Talk-to");
                log("Sleeping until conversation starts!");
                new ConditionalSleep(5000) {
                    @Override
                    public boolean condition() throws InterruptedException {
                        return getDialogues().inDialogue();
                    }
                }.sleep();
            }
            if (getDialogues().completeDialogue(options)) {
                log("Dialogue complete successfully!");
            } else {
                log("Dialogue failed!");
            }
		}
    }
    
    private void dyeGoblinMail(String color) {
    	getInventory().interactWithNameThatContains("Use", color + " dye");
    	getInventory().interactWithNameThatContains("Use", "Goblin mail");
    }
    
    private boolean isOnCutScene() {
    	return Tab.INVENTORY.isDisabled(this.getBot());
    }
}