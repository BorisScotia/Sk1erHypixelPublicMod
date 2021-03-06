package club.sk1er.mods.publicmod;

import club.sk1er.mods.publicmod.commands.*;
import club.sk1er.mods.publicmod.config.DataSaveType;
import club.sk1er.mods.publicmod.config.DataSaving;
import club.sk1er.mods.publicmod.config.PublicModConfig;
import club.sk1er.mods.publicmod.display.DisplayConfig;
import club.sk1er.mods.publicmod.display.DisplayElement;
import club.sk1er.mods.publicmod.display.ElementRenderer;
import club.sk1er.mods.publicmod.display.gui.DisplayGuiConfig;
import club.sk1er.mods.publicmod.handlers.KeyInput;
import club.sk1er.mods.publicmod.handlers.api.Sk1erApiHandler;
import club.sk1er.mods.publicmod.handlers.chat.Sk1erChatHandler;
import club.sk1er.mods.publicmod.handlers.server.PlayerJoinLeaveServer;
import net.hypixel.api.GameType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Mod(modid = Sk1erPublicMod.MODID, version = Sk1erPublicMod.VERSION, name = Sk1erPublicMod.NAME)
public class Sk1erPublicMod {


    /*
    Quest: http://i.imgur.com/tGIXJAX.png
     */
    public static final String MODID = "Sk1er-Public";
    public static final String VERSION = "1.0";
    public static final String NAME = "Sk1er Public Mod";
    private static Sk1erPublicMod instance;
    public final boolean DEV = false;
    private GameType currentGame = GameType.UNKNOWN;
    private AtomicInteger seconds = new AtomicInteger();
    private DataSaving dataSaving;
    private Sk1erMod sk1erMod;
    private Sk1erChatHandler chatHandler;
    private String uuid;
    private String name;
    private Sk1erApiHandler apiHandler;
    private KeyInput keyInput;
    private boolean isHypixel = false;
    private DisplayConfig displayConfig;
    private boolean tasksStarted = false;
    private PublicModConfig config;

    public static Sk1erPublicMod getInstance() {
        return instance;
    }

    public Sk1erChatHandler getChatHandler() {
        return chatHandler;
    }

    public Sk1erApiHandler getApiHandler() {
        return apiHandler;
    }

    public String getUuid() {
        return uuid;
    }

    public String getPlayerName() {
        return name;
    }

    @EventHandler
    public void init(FMLPreInitializationEvent event) {
        instance = this;
        sk1erMod = new Sk1erMod(MODID, VERSION, NAME);
        sk1erMod.checkStatus();
        name = Minecraft.getMinecraft().getSession().getProfile().getName();
        uuid = Minecraft.getMinecraft().getSession().getPlayerID().replace("-", "");
        apiHandler = new Sk1erApiHandler(sk1erMod, this);
        chatHandler = new Sk1erChatHandler(this);
        keyInput = new KeyInput(this);
        displayConfig = new DisplayConfig(new File(DataSaveType.PERM.getPath() + "displayconfig.json"));
        config = new PublicModConfig(new File(DataSaveType.PERM.getPath() + "modconfig.json"));
        dataSaving = new DataSaving();
        //'Register Events
        registerConfigAndEvent(chatHandler);
        registerConfigAndEvent(keyInput);
        registerConfigAndEvent(new ElementRenderer(this));
        registerConfigAndEvent(new PlayerJoinLeaveServer(this));
        registerConfigAndEvent(apiHandler);

        ClientCommandHandler.instance.registerCommand(new CommandTest());

        ClientCommandHandler.instance.registerCommand(new CommandGuildChat());
        ClientCommandHandler.instance.registerCommand(new CommandLoginHypixel());
        ClientCommandHandler.instance.registerCommand(new CommandPartyChat());
        ClientCommandHandler.instance.registerCommand(new CommandGetFriends());
        ClientCommandHandler.instance.registerCommand(new CommandGetGuild());

        //Start background tasks
        Multithreading.runAsync(() -> {
            apiHandler.fetchTimings();
            apiHandler.fetchQuests();
            startTasks();
        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            config.save();
            dataSaving.saveData();
            saveDisplayConfig();
        }));
    }

    private void registerConfigAndEvent(Object o) {
        MinecraftForge.EVENT_BUS.register(o);
        config.register(o);
    }

    public boolean isHypixel() {
        return isHypixel;
    }

    public GameType getCurrentGame() {
        return currentGame;
    }

    public void joinedHypixel() {
        isHypixel = true;
        Multithreading.runAsync(() -> {
            apiHandler.fetchPlayerGuild();
            apiHandler.fetchQuests();
            apiHandler.refreshPersonalBoosters();
            apiHandler.refreshPlayerData();

        });
    }

    private void startTasks() {
        if (!tasksStarted) {
            tasksStarted = true;
            //Game detection
            Multithreading.schedule(() -> {
                if (isHypixel()) {
                    try {
                        Scoreboard scoreboard = Minecraft.getMinecraft().thePlayer.getWorldScoreboard();
                        if (scoreboard != null) {
                            ScoreObjective objectiveInDisplaySlot = scoreboard.getObjectiveInDisplaySlot(1);
                            if (objectiveInDisplaySlot != null) {
                                String GAME_NAME = EnumChatFormatting.getTextWithoutFormattingCodes(objectiveInDisplaySlot.getDisplayName());
                                GameType type = GameType.parse(GAME_NAME);
                                if ( !type.equals(GameType.UNKNOWN)) {
                                    this.currentGame = type;
                                }
                            }
                        }
                    } catch (Exception e) {
                    }
                    int time = seconds.incrementAndGet();
                    boolean fetchedBoosters = false;
                    if (time % apiHandler.getTiming("boosters_live") == 0 || DEV) {
                        if (getApiHandler().hasBoostrs()) {
                            getApiHandler().refreshPersonalBoosters();
                            fetchedBoosters = true;
                        }
                    }
                    if (time % apiHandler.getTiming("watchdog_players") == 0 || DEV) {
                        getApiHandler().refreshWatchdogStats();
                    }
                    if (time % apiHandler.getTiming("player_profile") == 0 || DEV) {
                        getApiHandler().refreshPlayerData();
                    }
                    if (time % apiHandler.getTiming("boosters_check") == 0 || DEV) {
                        if (!fetchedBoosters)
                            getApiHandler().refreshPersonalBoosters();
                    }
                    if (time % apiHandler.getTiming("guild") == 0 || DEV) {
                        getApiHandler().fetchPlayerGuild();
                    }

                }
            }, 1, 1, TimeUnit.SECONDS);

        }
    }

    public void leaveHypixel() {
        //TODO

    }

    public List<DisplayElement> getDisplayElements() {
        return displayConfig.getElements();
    }

    public void saveDisplayConfig() {
        displayConfig.saveConfig();
    }

    public GuiScreen getConfigGuiInstance() {
        return new DisplayGuiConfig(this);
    }

    public DataSaving getDatSaving() {
        return dataSaving;
    }
}
