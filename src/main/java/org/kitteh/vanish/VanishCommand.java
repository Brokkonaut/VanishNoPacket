package org.kitteh.vanish;

import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.kitteh.vanish.utils.Messages;

public final class VanishCommand implements CommandExecutor {
    private final VanishPlugin plugin;

    public VanishCommand(VanishPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // First, the short aliases
        if (label.length() == 2) {
            if (sender instanceof Player) {
                if (label.equals("np")) {
                    this.toggle((Player) sender, "nopickup");
                }
                if (label.equals("nf")) {
                    this.toggle((Player) sender, "nofollow");
                }
                if (label.equals("nh")) {
                    this.toggle((Player) sender, "nohunger");
                }
                if (label.equals("ni")) {
                    this.toggle((Player) sender, "nointeract");
                }
                if (label.equals("nc")) {
                    this.toggle((Player) sender, "nochat");
                }
            }
            return true;
        }
        // Plain /vanish
        if (args.length == 0) {
            if (sender instanceof Player) {
                if (VanishPerms.canVanish((Player) sender)) {
                    this.plugin.getManager().toggleVanish((Player) sender);
                } else {
                    this.denied(sender);
                }
            }
            return true;
        }
        // /vanish <goal> [maybe stuff here]
        final String goal = args[0];
        if (goal.equalsIgnoreCase("reload")) {
            if (VanishPerms.canReload(sender)) {
                this.plugin.reload();
                sender.sendMessage(Messages.prefixed("Users reloaded"));
                sender.sendMessage(Messages.prefixed("Some settings refreshed"));
            } else {
                this.denied(sender);
            }
            return true;
        }
        if (goal.equalsIgnoreCase("list")) {
            if (VanishPerms.canList(sender)) {
                final List<Component> list = new ArrayList<>();
                for (final Player player : this.plugin.getServer().getOnlinePlayers()) {
                    if ((player != null) && this.plugin.getManager().isVanished(player)) {
                        list.add(Component.text(player.getName(), NamedTextColor.AQUA));
                    }
                }
                sender.sendMessage(Messages.prefixed(Component.text("Vanished: "), Component.join(JoinConfiguration.separator(Component.text(',')), list)));
            } else {
                this.denied(sender);
            }
            return true;
        }
        // Goodbye console!
        if (!(sender instanceof Player)) {
            sender.sendMessage(Messages.prefixed(Component.text("Did you mean ", NamedTextColor.AQUA), Component.text("vanish reload", NamedTextColor.WHITE), Component.text(" or ", NamedTextColor.AQUA), Component.text("vanish list", NamedTextColor.WHITE), Component.text("?", NamedTextColor.AQUA)));
            return true;
        }
        // No more console options below this point
        final Player player = (Player) sender;

        // Check if I'm vanished
        if (goal.equalsIgnoreCase("check")) {
            if (this.plugin.getManager().isVanished(player)) {
                player.sendMessage(Messages.prefixed("You are invisible."));
            } else {
                player.sendMessage(Messages.prefixed("You are visible."));
            }
            return true;
        }
        // Toggling
        if (goal.equalsIgnoreCase("toggle") || goal.equalsIgnoreCase("t")) {
            // List my toggles
            if (args.length == 1) {
                final List<Component> toggleReply = new ArrayList<>();
                if (VanishPerms.canToggleSee(player)) {
                    toggleReply.add(this.colorize(VanishPerms.canSeeAll(player), "see"));
                }
                if (VanishPerms.canToggleNoPickup(player)) {
                    toggleReply.add(this.colorize(VanishPerms.canNotPickUp(player), "nopickup"));
                }
                if (VanishPerms.canToggleNoFollow(player)) {
                    toggleReply.add(this.colorize(VanishPerms.canNotFollow(player), "nofollow"));
                }
                if (VanishPerms.canToggleNoInteract(player)) {
                    toggleReply.add(this.colorize(VanishPerms.canNotInteract(player), "nointeract"));
                }
                if (VanishPerms.canToggleDamageIn(player)) {
                    toggleReply.add(this.colorize(VanishPerms.blockIncomingDamage(player), "damage-in"));
                }
                if (VanishPerms.canToggleDamageOut(player)) {
                    toggleReply.add(this.colorize(VanishPerms.blockOutgoingDamage(player), "damage-out"));
                }
                if (VanishPerms.canToggleNoChat(player)) {
                    toggleReply.add(this.colorize(VanishPerms.canNotChat(player), "nochat"));
                }
                if (VanishPerms.canToggleNoHunger(player)) {
                    toggleReply.add(this.colorize(VanishPerms.canNotHunger(player), "nohunger"));
                }
                if (VanishPerms.canToggleSilentChestReads(player)) {
                    toggleReply.add(this.colorize(VanishPerms.canReadChestsSilently(player), "chests"));
                }
                if (!toggleReply.isEmpty()) {
                    player.sendMessage(Messages.prefixed(Component.text("You can toggle: "), Component.join(JoinConfiguration.commas(true), toggleReply)));
                } else {
                    player.sendMessage(Messages.prefixed("You cannot toggle anything"));
                }
            } else {
                // I wish to toggle something
                this.toggle(player, args[1]);
            }
            return true;
        }

        if (goal.equalsIgnoreCase("effects") || goal.equalsIgnoreCase("e")) {
            // List my toggles
            if (args.length == 1) {
                final List<Component> toggleReply = new ArrayList<>();
                if (VanishPerms.canToggleSmoke(player)) {
                    toggleReply.add(this.colorize(VanishPerms.canEffectSmoke(player), "smoke"));
                }
                if (VanishPerms.canToggleEffectExplode(player)) {
                    toggleReply.add(this.colorize(VanishPerms.canEffectExplode(player), "explode"));
                }
                if (VanishPerms.canToggleEffectLightning(player)) {
                    toggleReply.add(this.colorize(VanishPerms.canEffectLightning(player), "lightning"));
                }
                if (VanishPerms.canToggleEffectFlames(player)) {
                    toggleReply.add(this.colorize(VanishPerms.canEffectFlames(player), "flames"));
                }
                if (VanishPerms.canToggleEffectBats(player)) {
                    toggleReply.add(this.colorize(VanishPerms.canEffectBats(player), "bats"));
                }
                if (!toggleReply.isEmpty()) {
                    player.sendMessage(Messages.prefixed(Component.text("You can toggle: "), Component.join(JoinConfiguration.commas(true), toggleReply)));
                } else {
                    player.sendMessage(Messages.prefixed("You cannot toggle any effects"));
                }
            } else {
                // I wish to toggle something
                this.toggle(player, args[1]);
            }
            return true;
        }

        // The non-toggles
        if (goal.equalsIgnoreCase("on")) {
            if (!VanishPerms.canVanishOn(player)) {
                this.denied(sender);
                return true;
            }
            if (!this.plugin.getManager().isVanished(player)) {
                this.plugin.getManager().toggleVanish(player);
                // Fake announce as well?
                if ((args.length > 1) && args[1].equalsIgnoreCase("fake") && VanishPerms.canFakeAnnounce(player)) {
                    this.plugin.getManager().getAnnounceManipulator().fakeQuit(player, false);
                }
            }
            return true;
        }
        if (goal.equalsIgnoreCase("off")) {
            if (!VanishPerms.canVanishOff(player)) {
                this.denied(sender);
                return true;
            }
            if (this.plugin.getManager().isVanished(player)) {
                this.plugin.getManager().toggleVanish(player);
                // Fake announce as well?
                if ((args.length > 1) && args[1].equalsIgnoreCase("fake") && VanishPerms.canFakeAnnounce(player)) {
                    this.plugin.getManager().getAnnounceManipulator().fakeJoin(player, false);
                }
            }
            return true;
        }

        // Below this point, user must be able to /vanish

        if (!VanishPerms.canVanish(player)) {
            this.denied(sender);
            return true;
        }

        // Fake announces. Requires vanish.fakeannounce
        if ((goal.equalsIgnoreCase("fakequit") || goal.equalsIgnoreCase("fq"))) {
            if (VanishPerms.canFakeAnnounce(player)) {
                if (!this.plugin.getManager().isVanished(player)) {
                    this.plugin.getManager().toggleVanish(player);
                } else {
                    player.sendMessage(Messages.prefixed(Component.text("Already invisible :)", NamedTextColor.RED)));
                }
                boolean forced = false;
                if ((args.length > 1) && (args[1].equalsIgnoreCase("f") || args[1].equalsIgnoreCase("force"))) {
                    forced = true;
                }
                this.plugin.getManager().getAnnounceManipulator().fakeQuit(player, forced);
            } else {
                this.denied(sender);
            }
            return true;
        }
        if ((goal.equalsIgnoreCase("fakejoin") || goal.equalsIgnoreCase("fj"))) {
            if (VanishPerms.canFakeAnnounce(player)) {
                if (this.plugin.getManager().isVanished(player)) {
                    this.plugin.getManager().toggleVanish(player);
                } else {
                    player.sendMessage(Messages.prefixed(Component.text("Already visible :)", NamedTextColor.RED)));
                }
                boolean forced = false;
                if ((args.length > 1) && (args[1].equalsIgnoreCase("f") || args[1].equalsIgnoreCase("force"))) {
                    forced = true;
                }
                this.plugin.getManager().getAnnounceManipulator().fakeJoin(player, forced);
            } else {
                this.denied(sender);
            }
            return true;
        }

        // Continue? 

        // 3

        // 2

        // 1

        return true;
    }

    private Component colorize(boolean has, String name) {
        return Component.text(name, has ? NamedTextColor.GREEN : NamedTextColor.RED);
    }

    private void denied(CommandSender sender) {
        sender.sendMessage(Messages.prefixed("Access denied."));
    }

    private void toggle(Player player, String toggle) {
        final StringBuilder message = new StringBuilder();
        boolean status = false;
        if (toggle.equalsIgnoreCase("see") && VanishPerms.canToggleSee(player)) {
            status = VanishPerms.toggleSeeAll(player);
            this.plugin.getManager().resetSeeing(player);
            message.append("see all");
        } else if (toggle.equalsIgnoreCase("nopickup") && VanishPerms.canToggleNoPickup(player)) {
            status = VanishPerms.toggleNoPickup(player);
            message.append("no pickup");
        } else if (toggle.equalsIgnoreCase("nofollow") && VanishPerms.canToggleNoFollow(player)) {
            status = VanishPerms.toggleNoFollow(player);
            message.append("no mob follow");
        } else if (toggle.equalsIgnoreCase("damage-in") && VanishPerms.canToggleDamageIn(player)) {
            status = VanishPerms.toggleDamageIn(player);
            message.append("block incoming damage");
        } else if (toggle.equalsIgnoreCase("damage-out") && VanishPerms.canToggleDamageOut(player)) {
            status = VanishPerms.toggleDamageOut(player);
            message.append("block outgoing damage");
        } else if (toggle.equalsIgnoreCase("nointeract") && VanishPerms.canToggleNoInteract(player)) {
            status = VanishPerms.toggleNoInteract(player);
            message.append("no interact");
        } else if (toggle.equalsIgnoreCase("nochat") && VanishPerms.canToggleNoChat(player)) {
            status = VanishPerms.toggleNoChat(player);
            message.append("no chat");
        } else if (toggle.equalsIgnoreCase("nohunger") && VanishPerms.canToggleNoHunger(player)) {
            status = VanishPerms.toggleNoHunger(player);
            message.append("no hunger");
        } else if (toggle.equalsIgnoreCase("chests") && VanishPerms.canToggleSilentChestReads(player)) {
            status = VanishPerms.toggleSilentChestReads(player);
            message.append("silent chest reads");
        } else if (toggle.equalsIgnoreCase("smoke") && VanishPerms.canToggleSmoke(player)) {
            status = VanishPerms.toggleEffectSmoke(player);
            message.append("smoke effect");
        } else if (toggle.equalsIgnoreCase("explode") && VanishPerms.canToggleEffectExplode(player)) {
            status = VanishPerms.toggleEffectExplode(player);
            message.append("explosion effect");
        } else if (toggle.equalsIgnoreCase("lightning") && VanishPerms.canToggleEffectLightning(player)) {
            status = VanishPerms.toggleEffectLightning(player);
            message.append("lightning effect");
        } else if (toggle.equalsIgnoreCase("flames") && VanishPerms.canToggleEffectFlames(player)) {
            status = VanishPerms.toggleEffectFlames(player);
            message.append("flames effect");
        } else if (toggle.equalsIgnoreCase("bats") && VanishPerms.canToggleEffectBats(player)) {
            status = VanishPerms.toggleEffectBats(player);
            message.append("bats effect");
        }
        if (message.length() > 0) {
            message.insert(0, "Status: ");
            message.append(": ");
            if (status) {
                message.append("enabled");
            } else {
                message.append("disabled");
            }
            player.sendMessage(Messages.prefixed(message.toString()));
        } else if (VanishPerms.canVanish(player)) {
            player.sendMessage(Messages.prefixed("You can't toggle that!"));
        }
    }
}