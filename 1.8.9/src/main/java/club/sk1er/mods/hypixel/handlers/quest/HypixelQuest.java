package club.sk1er.mods.hypixel.handlers.quest;

import club.sk1er.mods.hypixel.Sk1erPublicMod;
import club.sk1er.mods.hypixel.utils.ChatUtils;
import club.sk1er.mods.hypixel.utils.Sk1erDateUtil;
import net.hypixel.api.GameType;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mitchellkatz on 2/10/17.
 */
public class HypixelQuest {


    public static List<HypixelQuest> allQuests = new ArrayList<>();
    private QuestType type;
    private String backendName;
    private long lastCompleted;
    private JSONObject quest_data;
    private GameType gameType;
    private boolean enabled;
    private boolean completed = false;

    public HypixelQuest(String backend) {
        this.backendName = backend;
        JSONObject quests = Sk1erPublicMod.getInstance().getApiHandler().getQuestInfo();
        quest_data = quests.optJSONObject(backend);
        int status = Sk1erPublicMod.getInstance().getDataSaving().getQuestStatus(backend);
        completed = status == 1;
        type = QuestType.valueOf(quest_data.optString("type"));
        gameType = GameType.fromDatabase(quest_data.optString("gameType"));
        enabled = quest_data.optBoolean("enabled");
        if (gameType == null) {
            System.out.println(quests.optString("gameType") + " is not a valid gametype!!!!." + backend);
        }
    }

    public static HypixelQuest fromBackend(String name) {
        for (HypixelQuest quest : allQuests) {
            if (quest.backendName.equalsIgnoreCase(name)) {
                return quest;
            }
        }
        HypixelQuest quest = new HypixelQuest(name);
        allQuests.add(quest);
        return quest;
    }

    @Deprecated
    public static HypixelQuest fromDisplayName(String displayName) {
        for (HypixelQuest quest : allQuests) {
            if (quest.getFrontEndName().equalsIgnoreCase(displayName))
                return quest;
        }
        ChatUtils.sendMessage("Please report this to Sk1er: Error type = Quest.NOT_REGISTERED. Id: " + displayName);
        return null;
    }

    public static HypixelQuest fromDisplayName(String displayName, GameType type) {
        for (HypixelQuest quest : allQuests) {
            if (quest.getFrontEndName().equalsIgnoreCase(displayName) && quest.getGameType().equals(type))
                return quest;
        }
        return fromDisplayName(displayName);
    }

    public static List<HypixelQuest> getQuestForGame(GameType type) {
        if (type == null) {
            return new ArrayList<>();
        }
        List<HypixelQuest> quests = new ArrayList<>();
        for (HypixelQuest quest : allQuests) {
            try {
                if (quest.getGameType().equals(type)) {
                    quests.add(quest);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return quests;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getBackendName() {
        return backendName;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void complete() {
        ChatUtils.sendMessage("You completed the " + getFrontEndName() + " quest!");
        completed = true;
        lastCompleted = System.currentTimeMillis();
        Sk1erPublicMod.getInstance().getDataSaving().applyQuestStatus(backendName, 1);

    }

    public boolean isDaily() {
        return type.equals(QuestType.DAILY);
    }

    public String getFrontEndName() {
        return quest_data.optString("displayName");
    }

    public boolean isActive() {
        JSONObject player = Sk1erPublicMod.getInstance().getApiHandler().getUser();
        if (player.optJSONObject("player").has("quests")) {
            JSONObject quests = player.optJSONObject("player").optJSONObject("quests");
            if (quests.has(backendName)) {
                JSONObject questInPlayer = quests.optJSONObject(backendName);
                if (questInPlayer.has("active")) {
                    long started = questInPlayer.getLong("started");
                    return Sk1erDateUtil.isToday(started);
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "HypixelQuest{" +
                "type=" + type +
                ", backendName='" + backendName + '\'' +
                ", lastCompleted=" + lastCompleted +
                ", quest_data=" + quest_data +
                ", gameType=" + gameType +
                ", completed=" + completed +
                '}';
    }

    public QuestType getType() {
        return type;
    }

    public GameType getGameType() {
        return gameType;
    }
}
