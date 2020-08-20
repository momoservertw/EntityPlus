package tw.momocraft.entityplus.utils;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import tw.momocraft.entityplus.handlers.ServerHandler;

public class CustomCommands {

    public static void executeCommands(Player player, String input) {
        if (player != null && !(player instanceof ConsoleCommandSender)) {
            if (input.startsWith("log:")) {
                input = input.replace("log: ", "");
                ServerHandler.sendConsoleMessage(input);
                return;
            } else if (input.startsWith("broadcast:")) {
                input = input.replace("broadcast: ", "");
                Bukkit.broadcastMessage(input);
                return;
            } else if (input.startsWith("console:")) {
                input = input.replace("console: ", "");
                dispatchConsoleCommand(player, input, true);
                return;
            } else if (input.startsWith("op:")) {
                input = input.replace("op: ", "");
                dispatchOpCommand(player, input, true);
                return;
            } else if (input.startsWith("player:")) {
                input = input.replace("player: ", "");
                dispatchPlayerCommand(player, input, true);
                return;
            } else if (input.startsWith("message:")) {
                input = input.replace("message: ", "");
                dispatchMessageCommand(player, input, true);
                return;
            } else if (input.startsWith("bungee:")) {
                input = input.replace("bungee: ", "");
                dispatchBungeeCordCommand(player, input, true);
                return;
            }
            dispatchConsoleCommand(null, input);
        } else {
            executeCommands(input);
        }
    }

    public static void executeCommands(String input) {
        if (input.startsWith("log:")) {
            input = input.replace("log: ", "");
            ServerHandler.sendConsoleMessage(input);
            return;
        } else if (input.startsWith("broadcast:")) {
            input = input.replace("broadcast: ", "");
            Bukkit.broadcastMessage(input);
            return;
        } else if (input.startsWith("console:")) {
            input = input.replace("console: ", "");
            dispatchConsoleCommand(null, input, true);
            return;
        } else if (input.startsWith("op:")) {
            ServerHandler.sendErrorMessage("&cThere is an error while execute command \"&eop: " + input + "&c\" &8- &cCan not find the execute target.");
            return;
        } else if (input.startsWith("player:")) {
            ServerHandler.sendErrorMessage("&cThere is an error while execute command \"&eplayer:" + input + "&c\" &8- &cCan not find the execute target.");
            return;
        } else if (input.startsWith("message:")) {
            ServerHandler.sendErrorMessage("&cThere is an error while execute command \"&emessage: " + input + "&c\" &8- &cCan not find the execute target.");
            return;
        } else if (input.startsWith("bungee:")) {
            ServerHandler.sendErrorMessage("&cThere is an error while execute command \"&ebungee: " + input + "&c\" &8- &cCan not find the execute target.");
            return;
        }
        dispatchConsoleCommand(null, input);
    }

    private static void dispatchConsoleCommand(Player player, String command) {
        try {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), Utils.translateLayout(command, player));
        } catch (Exception e) {
            ServerHandler.sendErrorMessage("&cThere was an issue executing a console command, if this continues please report it to the developer!");
            ServerHandler.sendDebugTrace(e);
        }
    }

    private static void dispatchConsoleCommand(Player player, String command, boolean placeholder) {
        if (player != null) {
            try {
                if (placeholder) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), Utils.translateLayout(command, player));
                } else {
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
                }
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), Utils.translateLayout(command, player));
            } catch (Exception e) {
                ServerHandler.sendErrorMessage("&cThere was an issue executing a console command, if this continues please report it to the developer!");
                ServerHandler.sendDebugTrace(e);
            }
        } else {
            try {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
            } catch (Exception e) {
                ServerHandler.sendErrorMessage("&cThere was an issue executing a console command, if this continues please report it to the developer!");
                ServerHandler.sendDebugTrace(e);
            }
        }
    }

    private static void dispatchOpCommand(Player player, String command) {
        boolean isOp = player.isOp();
        try {
            player.setOp(true);
            player.chat("/" + command);
        } catch (Exception e) {
            ServerHandler.sendDebugTrace(e);
            player.setOp(isOp);
            ServerHandler.sendErrorMessage("&cAn error has occurred while setting " + player.getName() + " status on the OP list, to ensure server security they have been removed as an OP.");
        } finally {
            player.setOp(isOp);
        }
    }

    private static void dispatchOpCommand(Player player, String command, boolean placeholder) {
        boolean isOp = player.isOp();
        try {
            player.setOp(true);
            if (placeholder) {
                player.chat("/" + Utils.translateLayout(command, player));
            } else {
                player.chat("/" + command);
            }
        } catch (Exception e) {
            ServerHandler.sendDebugTrace(e);
            player.setOp(isOp);
            ServerHandler.sendErrorMessage("&cAn error has occurred while setting " + player.getName() + " status on the OP list, to ensure server security they have been removed as an OP.");
        } finally {
            player.setOp(isOp);
        }
    }

    private static void dispatchPlayerCommand(Player player, String command) {
        try {
            player.chat("/" + command);
        } catch (Exception e) {
            ServerHandler.sendErrorMessage("&cThere was an issue executing a player command, if this continues please report it to the developer!");
            ServerHandler.sendDebugTrace(e);
        }
    }

    private static void dispatchPlayerCommand(Player player, String command, boolean placeholder) {
        try {
            if (placeholder) {
                player.chat("/" + Utils.translateLayout(command, player));
            } else {
                player.chat("/" + command);
            }
        } catch (Exception e) {
            ServerHandler.sendErrorMessage("&cThere was an issue executing a player command, if this continues please report it to the developer!");
            ServerHandler.sendDebugTrace(e);
        }
    }


    private static void dispatchChatCommand(Player player, String command) {
        try {
            player.chat(command);
        } catch (Exception e) {
            ServerHandler.sendErrorMessage("&cThere was an issue executing a player command, if this continues please report it to the developer!");
            ServerHandler.sendDebugTrace(e);
        }
    }

    private static void dispatchChatCommand(Player player, String command, boolean placeholder) {
        try {
            if (placeholder) {
                player.chat(Utils.translateLayout(command, player));
            } else {
                player.chat(command);
            }
        } catch (Exception e) {
            ServerHandler.sendErrorMessage("&cThere was an issue executing a player command, if this continues please report it to the developer!");
            ServerHandler.sendDebugTrace(e);
        }
    }

    private static void dispatchMessageCommand(Player player, String command) {
        try {
            player.sendMessage(command);
        } catch (Exception e) {
            ServerHandler.sendErrorMessage("&cThere was an issue executing a command to send a message, if this continues please report it to the developer!");
            ServerHandler.sendDebugTrace(e);
        }
    }

    private static void dispatchMessageCommand(Player player, String command, boolean placeholder) {
        try {
            if (placeholder) {
                player.sendMessage(Utils.translateLayout(command, player));
            } else {
                player.sendMessage(command);
            }
        } catch (Exception e) {
            ServerHandler.sendErrorMessage("&cThere was an issue executing a command to send a message, if this continues please report it to the developer!");
            ServerHandler.sendDebugTrace(e);
        }
    }

    private static void dispatchBungeeCordCommand(Player player, String command) {
        try {
            BungeeCord.ExecuteCommand(player, command);
        } catch (Exception e) {
            ServerHandler.sendErrorMessage("&cThere was an issue executing an item's command to BungeeCord, if this continues please report it to the developer!");
            ServerHandler.sendDebugTrace(e);
        }
    }

    private static void dispatchBungeeCordCommand(Player player, String command, boolean placeholder) {
        try {
            if (placeholder) {
                BungeeCord.ExecuteCommand(player, Utils.translateLayout(command, player));
            } else {
                BungeeCord.ExecuteCommand(player, command);
            }
        } catch (Exception e) {
            ServerHandler.sendErrorMessage("&cThere was an issue executing an item's command to BungeeCord, if this continues please report it to the developer!");
            ServerHandler.sendDebugTrace(e);
        }
    }
}
