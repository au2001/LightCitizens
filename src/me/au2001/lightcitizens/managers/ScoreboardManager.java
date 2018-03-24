package me.au2001.lightcitizens.managers;

import me.au2001.lightcitizens.FakeEntity;
import me.au2001.lightcitizens.packets.PacketPlayOutScoreboardScore;
import me.au2001.lightcitizens.packets.PacketPlayOutScoreboardScore.EnumScoreboardAction;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ScoreboardManager extends Manager {

    // TODO: Intercept packets and send fake ones not to interfere with real Scoreboard.

    private static final int UPDATE_THRESHOLD = 1 * 20;

    private int updateTicks = 0;
    private String entryName;
    private List<Objective> automaticScores = new ArrayList<Objective>();

    public ScoreboardManager(FakeEntity entity) {
        super(entity);

        entryName = entity.getName();
    }

    public void tick() {
        if (updateTicks++ >= UPDATE_THRESHOLD) {
            updateTicks = 0;

            for (Objective objective : automaticScores) updateScore(objective);
        }
    }

    public Team getTeam(Scoreboard scoreboard) {
        return scoreboard.getEntryTeam(entryName);
    }

    public Set<Score> getScores(Scoreboard scoreboard) {
        return scoreboard.getScores(entryName);
    }

    public void resetScores(Scoreboard scoreboard) {
        scoreboard.resetScores(entryName);
    }

	public Score getScore(Objective objective) {
	    return objective.getScore(entryName);
    }

    public void joinTeam(Team team) {
        team.addEntry(entryName);
    }

    public void leaveTeam(Team team) {
        team.removeEntry(entryName);
    }

    public boolean isInTeam(Team team) {
        return team.hasEntry(entryName);
    }

    public void addAutomaticScore(Objective objective) {
        if (automaticScores.contains(objective)) return;

        automaticScores.add(objective);
        updateScore(objective);
    }

    public void removeAutomaticScore(Objective objective) {
        if (automaticScores.contains(objective)) automaticScores.remove(objective);
    }

    public void updateScore(Objective objective) {
        int value = calculateScore(objective);

//        Score score = getScore(objective);
//        if (score != null && score.getScore() != value) score.setScore(value);

        PacketPlayOutScoreboardScore score = new PacketPlayOutScoreboardScore();
        score.set("a", entryName);
        score.set("b", objective.getName());
        score.set("c", value);
        score.set("d", EnumScoreboardAction.CHANGE);
        for (Player observer : entity.getObservers()) score.send(observer);
    }

    public int calculateScore(Objective objective) {
        int value = 0;

        switch (objective.getCriteria().toLowerCase()) {
            case "deathcount":
                if (entity.hasManager(DamageableManager.class))
                    value = entity.getManager(DamageableManager.class).getDeathCount();
                break;

            case "playerkillcount":
                if (entity.hasManager(AttackEntityManager.class))
                    value = entity.getManager(AttackEntityManager.class).getPlayerKillCount();
                break;

            case "totalkillcount":
                if (entity.hasManager(AttackEntityManager.class))
                    value = entity.getManager(AttackEntityManager.class).getTotalKillCount();
                break;

            case "health":
                if (entity.hasManager(DamageableManager.class))
                    value = (int) entity.getManager(DamageableManager.class).getHealth();
                break;

            case "xp":
                // TODO
                break;

            case "level":
                // TODO
                break;

            case "food":
                // TODO
                break;

            case "air":
                // TODO
                break;

            case "armor":
                // TODO
                break;

            case "dummy":
            case "trigger":
            default:
                // Nothing to do.
                break;
        }

        // TODO: teamkill.<team>
        // TODO: killedbyteam.<team>
        // TODO: stat.<stat>
        // TODO: achievement.<achievement>

        return value;
    }
	
}
