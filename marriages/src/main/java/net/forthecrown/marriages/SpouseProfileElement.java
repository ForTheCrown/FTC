package net.forthecrown.marriages;

import net.forthecrown.text.TextWriter;
import net.forthecrown.user.User;
import net.forthecrown.user.name.DisplayContext;
import net.forthecrown.user.name.FieldPlacement;
import net.forthecrown.user.name.ProfileDisplayElement;

public class SpouseProfileElement implements ProfileDisplayElement {

  @Override
  public void write(TextWriter writer, User user, DisplayContext context) {
    if (!context.profileViewable()) {
      return;
    }

    var spouse = Marriages.getSpouse(user);

    if (spouse == null) {
      return;
    }

    var factory = context.factory();
    DisplayContext ctx
        = factory.createContext(spouse, context.viewer(), spouse.defaultRenderFlags())
        .withIntent(context.intent());

    var displayName = factory.formatDisplayName(spouse, ctx);
    writer.field("Spouse", displayName);
  }

  @Override
  public FieldPlacement placement() {
    return FieldPlacement.ALL;
  }
}
