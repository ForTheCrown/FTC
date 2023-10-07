import "net.kyori.adventure.text.event.ClickCallback$Options";
import "@ftc.text.UserClickCallback";
import "@ftc.utils.Audiences";

let user = Audiences.getUser(viewer);
let rhinePrice = goal;

let canAfford = user.hasBalance(rhinePrice);

let hover = canAfford ? text("Click to buy") : text("Cannot afford!");
let color = canAfford ? NamedTextColor.AQUA : NamedTextColor.GRAY;

Text.format(" [Pay {0, rhines}]", color, rhinePrice)
    .hoverEvent(hover)
    .clickEvent(challenge.triggerClickEvent)