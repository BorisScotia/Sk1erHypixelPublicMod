package net.hypixel.api;

import club.sk1er.mods.hypixel.handlers.game.Sk1erGameHandler;
import club.sk1er.mods.hypixel.handlers.game.games.*;

/**
 * Created by Mitchell Katz on 12/8/2016.
 */
public enum GameType {
    QUAKECRAFT("Quakecraft", "Quake", 2, new QuakecraftGameHandler()),
    WALLS("Walls", "Walls", 3, new Walls2GameHandler()),
    PAINTBALL("Paintball", "Paintball", 4, new PaintballGameHandler()),
    SURVIVAL_GAMES("Blitz Survival Games", "HungerGames", 5,new BlitzGameHandler()),
    TNTGAMES("The TNT Games", "TNTGames", 6, new TNTGamesHandler()),
    VAMPIREZ("VampireZ", "VampireZ", 7, new VampireZGameHandler()),
    WALLS3("Mega Walls", "Walls3", 13, new Walls3GameHandler()),
    ARCADE("Arcade", "Arcade", 14, new ArcadeGameHandler()),
    ARENA("Arena Brawl", "Arena", 17, new ArenaGameHandler()),
    MCGO("Cops and Crims", "MCGO", 21, new CVCGameHandler()),
    UHC("UHC Champions", "UHC", 20, new UHCGameHandler()),
    BATTLEGROUND("Warlords", "Battleground", 23, new WarlordsGameHandler()),
    SUPER_SMASH("Smash Heroes", "SuperSmash", 24, new SmashHeroesGameHandler()),
    GINGERBREAD("Turbo Kart Racers", "GingerBread", 25, new TKRGameHandler()),
    HOUSING("Housing", "Housing", 26, new BlankGameHandler()),
    SKYWARS("SkyWars", "SkyWars", 51, new SkyWarsGameHandler()),
    TRUE_COMBAT("Crazy Walls", "TrueCombat", 52, new CrazyWallsGameHandler()),
    SPEED_UHC("Speed UHC", "SpeedUHC", 54, new SpeedUHCGameHandler()),
    UNKNOWN("Unknown", "null", 51, new BlankGameHandler()),
    SKYCLASH("SkyClash", "SkyClash", 55, new SkyClashGameHandler());

    private static final GameType[] v = values();

    public Sk1erGameHandler getGameHandler() {
        return gameHandler;
    }

    private Sk1erGameHandler gameHandler;
    private final String name, dbName;
    private final Integer id;


    GameType(String name, String dbName, Integer id, Sk1erGameHandler handler) {
        this.name = name;
        this.dbName = dbName;
        this.id = id;
        this.gameHandler = handler;
    }


    GameType(String name, String dbName, Integer id) {
        this.name = name;
        this.dbName = dbName;
        this.id = id;
    }

    /**
     * @param id The internal id
     * @return The GameType associated with that id, or null if there isn't one.
     */
    public static GameType fromId(int id) {
        for (GameType gameType : v) {
            if (gameType.id == id) {
                return gameType;
            }
        }
        return null;
    }

    public static GameType fromRealPersonName(String name) {
        for (GameType g : v) {
            if (g.getGameHandler() != null) {
                if (g.getGameHandler().isGame(name)) {
                    return g;
                }
            } else {
                if (g.getName().equalsIgnoreCase(name))
                    return g;
            }
        }
        return null;
    }

    public String getDatabaseName() {
        return dbName;
    }

    /**
     * @param dbName The key used in the database
     * @return The GameType associated with that key, or null if there isn't one.
     */
    public static GameType fromDatabase(String dbName) {
        for (GameType gameType : v) {
            if (gameType.dbName.equals(dbName)) {
                return gameType;
            }
        }
        return null;
    }

    /**
     * @return The official name of the GameType
     */
    public String getName() {
        return name;
    }

    /**
     * @return The internal ID that is occasionally used in various database schemas
     */
    public int getId() {
        return id;
    }
}