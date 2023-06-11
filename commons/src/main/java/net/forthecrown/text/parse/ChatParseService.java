package net.forthecrown.text.parse;

import org.bukkit.permissions.Permissible;

public interface ChatParseService {

  ChatParser.Builder parserBuilder();

  ChatParser parser(Permissible permissible);

  ChatParser defaultParser();
}