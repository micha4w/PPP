package com.Practice.Useful;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class PPPScoreboard {
    public Scoreboard scoreboard;
    public Objective objective;
    public String[] texts;

    public PPPScoreboard (Player player, int lines, String displayName) {
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        player.setScoreboard(scoreboard);
        this.objective = scoreboard.registerNewObjective(player.getName(), "dummy", displayName);
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        texts = new String[lines];
        for ( int i = 0; i < lines; i++ ) {
            Score score = this.objective.getScore("§" + i);
            this.texts[i] = score.getEntry();
            score.setScore(lines - i - 1);
        }
    }

    public void setLine (int line, String text) {
        removeLine(line);

        Score score = this.objective.getScore(text + "§" + line);
        score.setScore(texts.length - line - 1);
        this.texts[line] = score.getEntry();
    }

    public void removeLine (int line) {
        this.scoreboard.resetScores(texts[line]);
    }

    public void delete (Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        this.objective.unregister();
    }
}
