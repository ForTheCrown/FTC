const OBJECTIVE_NAME = "trials_far_reach";
const FINISHED_SCORE = -1;

function onUse(user) {
  let score = getScore(user.getPlayer());

  if (score == null) {
    return;
  }

  if (score.getScore() != FINISHED_SCORE) {
    score.setScore(0);
  }
}

function getScore(player) {
  let scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
  let obj = scoreboard.getObjective(OBJECTIVE_NAME);

  if (obj == null) {
    return null;
  }

  return obj.getScore(player);
}