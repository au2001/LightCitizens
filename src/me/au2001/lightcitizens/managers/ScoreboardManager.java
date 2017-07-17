package me.au2001.lightcitizens.managers;

import me.au2001.lightcitizens.FakeEntity;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ScoreboardManager extends Manager {

    private String entryName;
    private List<Objective> automaticScores = new ArrayList<Objective>();

    public ScoreboardManager(FakeEntity entity) {
		super(entity);

		// entryName = entity.getUUID().toString();
        entryName = entity.getName();
	}

    public void tick() {
        for (Objective objective : automaticScores) calculateScore(objective);
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
        if (!automaticScores.contains(objective)) {
            automaticScores.add(objective);
            calculateScore(objective);
        }
    }

    public void removeAutomaticScore(Objective objective) {
        if (automaticScores.contains(objective)) automaticScores.remove(objective);
    }

    public int calculateScore(Objective objective) {
        int value = 0;

        switch (objective.getCriteria().toLowerCase()) {
            case "deathCount":
                if (entity.hasManager(DamageableManager.class))
                    value = entity.getManager(DamageableManager.class).getDeathCount();
                break;
            case "playerKillCount":
                if (entity.hasManager(AttackEntityManager.class))
                    value = entity.getManager(AttackEntityManager.class).getPlayerKillCount();
                break;
            case "totalKillCount":
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
        // TODO: killedByTeam.<team>
        // TODO: stat.<stat>
        // TODO: achievement.<achievement>

        Score score = getScore(objective);
        if (score != null && score.getScore() != value) score.setScore(value);
        return value;
    }
	
}
