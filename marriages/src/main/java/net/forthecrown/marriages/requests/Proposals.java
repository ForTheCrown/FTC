package net.forthecrown.marriages.requests;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.command.request.RequestTable;
import net.forthecrown.command.request.RequestValidator;
import net.forthecrown.marriages.Marriages;
import net.forthecrown.user.User;
import net.forthecrown.utils.Audiences;
import net.kyori.adventure.audience.Audience;

public final class Proposals {
  private Proposals() {}

  public static final RequestTable<Proposal> TABLE;

  static {
    TABLE = new RequestTable<>();
    TABLE.setValidator(new ProposalValidator());
  }

  public static void propose(User source, User target) throws CommandSyntaxException {
    Proposal proposal = new Proposal(source.getUniqueId(), target.getUniqueId());
    TABLE.sendRequest(proposal);
  }

  static class ProposalValidator implements RequestValidator<Proposal> {

    @Override
    public void validate(Proposal request, Audience viewer) throws CommandSyntaxException {
      boolean isSender = Audiences.equals(viewer, request.getSender());

      User first = isSender ? request.getSender() : request.getTarget();
      User second = isSender ? request.getTarget() : request.getSender();

      Marriages.testCanMarry(first, second);
    }
  }
}
